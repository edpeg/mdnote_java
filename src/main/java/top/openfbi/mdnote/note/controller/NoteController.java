package top.openfbi.mdnote.note.controller;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import top.openfbi.mdnote.common.ResultStatus;
import top.openfbi.mdnote.common.exception.ResultException;
import top.openfbi.mdnote.config.ResponseResultBody;
import top.openfbi.mdnote.note.model.Note;
import top.openfbi.mdnote.note.model.fe.EsNoteResult;
import top.openfbi.mdnote.note.model.fe.SimpleNote;
import top.openfbi.mdnote.note.service.NoteService;
import top.openfbi.mdnote.user.model.UserSession;
import top.openfbi.mdnote.user.util.Session;

import javax.validation.constraints.DecimalMin;
import java.util.List;
import java.util.Map;

/**
 * note请求
 */
@RestController
@RequestMapping("/note")
@ResponseResultBody
public class NoteController {
    @Autowired
    private NoteService noteService;

    private static final Logger logger
            = LoggerFactory.getLogger(NoteController.class);

    /**
     * Submit方法专门的返回值
     * 原因：springboot会把string返回值给识别成html结构，导致无法序列化为Result类型。抛出类型转换异常。
     * 所以使用类的方式返回数据
     * 数据最好使用对象形式返回
     */
    @Data
    @AllArgsConstructor
    static
    class SubmitResponse {
        private Long noteId;
    }

    /**
     * 提交保存笔记
     */
    @ResponseBody
    @PostMapping("/save")
    public SubmitResponse save(@RequestBody Note note) throws ResultException {
        // 保存笔记
        note.setUserId(Session.getUser().getId());
        return new SubmitResponse(noteService.save(note));
    }



    /**
     * 根据id删除笔记
     */
    @ResponseBody
    @PostMapping("/delete")
    // TODO 修改为Post方式，防止恶意删除笔记
    public void delete(@RequestBody Map<String, String> person) throws ResultException {
        Long id = Long.valueOf(person.get("id"));
        UserSession userSession = Session.getUser();
        if (userSession.getUserName().equals("demo")){
            throw new ResultException(ResultStatus.DEMO_ACCOUNT_NOTE_FORBID_LOGOFF);
        }
        noteService.delete(id, userSession.getId());
    }

    /**
     * 根据id修改笔记公开状态
     */
    @ResponseBody
    @PostMapping("/setOpen")
    // TODO 修改为Post请求方式，防止钓鱼链接
    public void setOpen(@RequestBody Map<String, String> person) throws ResultException {
        Long id = Long.valueOf(person.get("id"));
        boolean open = Boolean.parseBoolean(person.get("open"));
        noteService.setOpen(id,open, Session.getUser().getId());
    }

    /**
     * 根据id查询笔记
     */
    @ResponseBody
    @GetMapping("/info")
    public SimpleNote info(@DecimalMin(value = "0",message = "笔记ID参数不能小于0")Long id) throws ResultException {
        Note note = noteService.info(id, Session.getUser().getId());
        return new SimpleNote(note);
    }

    /**
     * 根据id查询公开笔记
     */
    @ResponseBody
    @GetMapping("/open")
    public SimpleNote open(@DecimalMin(value = "0",message = "笔记ID参数不能小于0")Long id) throws ResultException {
        Note note = noteService.open(id);
        return new SimpleNote(note);
    }

    /**
     * 查询用户全部笔记
     */
    @ResponseBody
    @GetMapping("/list")
    public EsNoteResult list() throws ResultException {
        //创建返回类
        List<Note> notes = noteService.getNoteList(Session.getUser().getId());

        EsNoteResult esNoteResult = new EsNoteResult();
        List<SimpleNote> feNotes = notes.stream().map(note -> new SimpleNote(note)).toList();
        esNoteResult.setNoteList(feNotes);
        return esNoteResult;
    }

    /**
     * 使用es搜索笔记
     */
    @ResponseBody
    @GetMapping("/search")
    public EsNoteResult search(@RequestParam(name="q") String q) throws ResultException{
        return noteService.search(q, Session.getUser().getId());
    }

    /**
     * 根据id美化笔记
     */
    @ResponseBody
    @GetMapping("/beautify")
    public SubmitResponse beautify(@DecimalMin(value = "0",message = "笔记ID参数不能小于0")Long id) throws ResultException {
        Long noteId = noteService.beautify(id, Session.getUser().getId());
        return new SubmitResponse(noteId);
    }

    /**
     * 根据id总结笔记
     */
    @ResponseBody
    @GetMapping("/summary")
    public SubmitResponse summary(@DecimalMin(value = "0",message = "笔记ID参数不能小于0")Long id) throws ResultException {
        Long noteId = noteService.summary(id, Session.getUser().getId());
        return new SubmitResponse(noteId);
    }


    /**
     * Submit方法专门的返回值
     * 原因：springboot会把string返回值给识别成html结构，导致无法序列化为Result类型。抛出类型转换异常。
     * 所以使用类的方式返回数据
     * 数据最好使用对象形式返回
     */
    @Data
    @AllArgsConstructor
    static
    class SubmitResponseNoteContent {
        private String noteContent;
    }
    /**
     * 润色内容
     */
//    @ResponseBody
    @PostMapping("/polish")
    public SubmitResponseNoteContent polish(@RequestBody Map<String, String> request) throws ResultException {
        String noteContent = request.get("noteContent");
        String noteContentNew = noteService.polish(noteContent);
        return new SubmitResponseNoteContent(noteContentNew);
    }


    /**
     * 图片处理
     */
//    @ResponseBody
    @PostMapping("/imgAnalysis")
    public SubmitResponseNoteContent imgAnalysis(@RequestBody Map<String, String> request) throws ResultException {
        String imgUrl = request.get("imgUrl");
        String noteContentNew = noteService.imgAnalysis(imgUrl);
        return new SubmitResponseNoteContent(noteContentNew);
    }
}
