package top.openfbi.mdnote.note.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.stereotype.Service;
import top.openfbi.mdnote.common.ResultStatus;
import top.openfbi.mdnote.common.exception.ResultException;
import top.openfbi.mdnote.note.dao.NoteDao;
import top.openfbi.mdnote.note.model.ElasticSearchNote;
import top.openfbi.mdnote.note.model.Note;
import top.openfbi.mdnote.note.model.fe.EsNoteResult;
import top.openfbi.mdnote.note.model.fe.SimpleNote;
import top.openfbi.mdnote.utils.StringUtil;
import top.openfbi.mdnote.utils.Time;

import java.util.*;

/**
 * note笔记业务操作
 */
@Service
public class NoteService {
    @Autowired
    private NoteSearchService noteSearchService;
    @Autowired
    private NoteDao noteDao;
    private static int NOTE_CONTENT_CUT_LENGTH = 100;
    private static final Logger logger
            = LoggerFactory.getLogger(NoteService.class);

    /**
     * 保存笔记
     */
    public Long save(Note note) throws ResultException {
        if (note.getId() == null || note.getId() == 0) {
            // 笔记ID不存在  执行新增操作
            // 笔记创建时间
            note.setCreateTime(Time.current());
            note.setUpdateTime(Time.current());
            // 保存笔记
            if (noteDao.insert(note) == 0) {
                logger.error("保存笔记失败: {}", note);
                throw new ResultException(ResultStatus.INTERNAL_MYSQL_SQL_EXEC_FAIL);
            }
        } else {
            // 笔记ID存在   执行更新操作

            // 设置修改时间
            note.setUpdateTime(Time.current());
            // 判断mysql是否保存成功
            QueryWrapper<Note> wrapper = new QueryWrapper<>();
            //构造条件
            wrapper.eq("id", note.getId());
            wrapper.eq("user_id", note.getUserId());
            if (noteDao.update(note, wrapper) != 1) {
                logger.warn("保存笔记失败: {}", note);
                throw new ResultException(ResultStatus.INTERNAL_MYSQL_SQL_EXEC_FAIL);
            }
        }
        // 保存es
        saveToES(note);
        return note.getId();
    }

    private void saveToES(Note note) {
        // 创建es保存对象
        ElasticSearchNote elasticSearchNote = new ElasticSearchNote();
        // 设置es笔记对象信息
        //设置笔记esid
        elasticSearchNote.setId(note.getId());
        elasticSearchNote.setTitle(note.getTitle());
        elasticSearchNote.setContent(note.getContent());
        elasticSearchNote.setUserId(note.getUserId());
        //保存es笔记信息
        noteSearchService.save(elasticSearchNote);
    }

    /**
     * 根据id删除笔记
     */
    public void delete(Long id, Long userId) throws ResultException {
        // 获取笔记信息
        Note note = noteDao.selectById(id);
        // 判断笔记是否为空
        if (note == null) {
            logger.warn("查询笔记失败，笔记ID: {}", id);
            // 返回笔记不存在异常
            throw new ResultException(ResultStatus.NOTE_ID_NOT_EXIST);
        }
        // 判断用户信息是否和笔记中的用户信息一致
        if (!userId.equals(note.getUserId())) {
            logger.info("删除笔记失败原因，无权限用户ID: {}", userId);
            // 用户信息不一致，返回用户无权限访问异常
            throw new ResultException(ResultStatus.USER_NO_PERMISSION);
        }
        //判断mysql是否删除成功
        if (noteDao.deleteById(id) != 1) {
            logger.warn("删除笔记失败: {}", note);
            throw new ResultException(ResultStatus.INTERNAL_MYSQL_SQL_EXEC_FAIL);
        }
        noteSearchService.delete(note.getId());
    }

    /**
     * 删除用户所有笔记
     */
    public void deleteAllNoteOfUser(Long userId) {

        Map<String, Object> map = new HashMap<>();
        map.put("user_id", userId);
        // 删除拥护所有笔记
        noteDao.deleteByMap(map);
        noteSearchService.deleteAllNoteOfUser(userId);
    }

    /**
     * 根据id查询笔记
     */
    public Note info(Long id, Long userId) throws ResultException {
        // 查询用户笔记
        Note note = noteDao.selectById(id);
        // 判断笔记是否为空
        if (note == null) {
            logger.warn("查询笔记失败，笔记ID: {}", id);
            // 返回笔记不存在异常
            throw new ResultException(ResultStatus.NOTE_ID_NOT_EXIST);
        }
        // 判断笔记的用户id和用户信息是否一致
        if (!userId.equals(note.getUserId())) {
            logger.info("查询笔记失败原因，无权限用户ID: {}", userId);
            //返回用户无权限异常
            throw new ResultException(ResultStatus.USER_NO_PERMISSION);
        }
        return note;
    }

    /**
     * 查询用户全部笔记
     */
    public List<Note> getNoteList(Long userId) throws ResultException {
        //构造条件构造器
        QueryWrapper<Note> wrapper = new QueryWrapper<>();
        //构造条件
        wrapper.eq("user_id", userId);
        wrapper.last("limit 100");
        wrapper.orderByDesc("update_time");
        //使用提供的selectList默认方法进行结果查询
        List<Note> list = noteDao.selectList(wrapper);
        return list;
    }

    private String higlightOriginal(List<String> higlight, String original) {
        if (higlight != null) {
            // 获取标题内容
            return StringUtil.join(higlight, "  ");
        }
        return original;
    }

    private List<SimpleNote> completeNoteHighlightField(List<SearchHit<ElasticSearchNote>> elasticSearchNoteList, Map<Long, Note> noteMap) {
        List<SimpleNote> frontEndNoteList = new LinkedList<>();
        // 循环处理笔记信息
        for (int i = 0; i < elasticSearchNoteList.size(); i++) {
            SearchHit<ElasticSearchNote> searchHit = elasticSearchNoteList.get(i);
            // 根据es笔记id获取笔记信息
            Note note = noteMap.get(searchHit.getContent().getId());
            //如果笔记信息不存在则略过
            if (note == null) {
                continue;
            }
            SimpleNote simpleNote = new SimpleNote();
            simpleNote.setId(searchHit.getContent().getId());

            // 判断是否查询到高亮内容
            Map<String, List<String>> highlightFieldsMap = searchHit.getHighlightFields();

            // 设置笔记标题高亮
            List<String> titlelHiglightList = highlightFieldsMap.get("title");
            simpleNote.setTitle(higlightOriginal(titlelHiglightList, note.getTitle()));

            // 设置笔记内容高亮
            List<String> contentHighlightList = highlightFieldsMap.get("content");
            // 无笔记高亮内容，获取正文前100个字符作为笔记内容摘要。
            int digestLength = 100;
            String content = StringUtil.cut(note.getContent(), digestLength);
            simpleNote.setContent(higlightOriginal(contentHighlightList, content));

            frontEndNoteList.add(simpleNote);
        }
        return frontEndNoteList;
    }

    // 批量查询笔记
    private Map<Long, Note> batchQueryNote(List<Long> idList) {
        logger.trace("笔记ID列表: {}", idList);
        // 构造查询
        QueryWrapper<Note> wrapper = new QueryWrapper<>();
        wrapper.in("id", idList);
        // 查询出所有笔记信息
        List<Note> noteList = noteDao.selectList(wrapper);
        Map<Long, Note> noteMap = new HashMap<>();
        noteList.stream().forEach(note -> {
            noteMap.put(note.getId(), note);
        });
        return noteMap;
    }

    /**
     * 使用es搜索笔记
     */
    public EsNoteResult search(String q, Long userId) throws ResultException {
        if (q == null || q.equals("")) {
            // 如果查询关键字为空则返回用户全部笔记
            List<Note> notelist = this.getNoteList(userId);
            List<SimpleNote> feNotes = notelist.stream().map(note -> new SimpleNote(note)).toList();
            for (SimpleNote simpleNote : feNotes) {
                String content = StringUtil.cut(simpleNote.getContent(), NOTE_CONTENT_CUT_LENGTH);
                simpleNote.setContent(content);
            }

            EsNoteResult esNoteResult = new EsNoteResult();
            esNoteResult.setNoteList(feNotes);
            return esNoteResult;
        }

        // 使用es搜索笔记
        List<SearchHit<ElasticSearchNote>> elasticSearchNoteList = noteSearchService.search(q, userId);
        // 根据es结果查询出所有笔记信息
        if (elasticSearchNoteList.size() == 0) {
            return new EsNoteResult();
        }
        // 获取笔记ID的list
        List<Long> idList = elasticSearchNoteList.stream().map(searchHit -> searchHit.getContent().getId()).toList();
        Map<Long, Note> noteMap = batchQueryNote(idList);

        // 处理es查询结果
        EsNoteResult esNoteResult = new EsNoteResult();
        esNoteResult.setNoteList(completeNoteHighlightField(elasticSearchNoteList, noteMap));
        return esNoteResult;
    }
}