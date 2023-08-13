package top.openfbi.mdnote.note.controller;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import top.openfbi.mdnote.common.exception.ResultException;
import top.openfbi.mdnote.config.ResponseResultBody;
import top.openfbi.mdnote.note.model.Note;
import top.openfbi.mdnote.note.model.fe.EsNoteResult;
import top.openfbi.mdnote.note.model.fe.SimpleNote;
import top.openfbi.mdnote.note.service.NoteService;
import top.openfbi.mdnote.user.util.Session;

import javax.validation.constraints.DecimalMin;
import java.util.List;

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
    @GetMapping("/delete")
    public void delete(@DecimalMin(value = "0",message = "笔记ID参数不能小于0")Long id) throws ResultException {
        noteService.delete(id, Session.getUser().getId());
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
    public EsNoteResult search(String q) throws ResultException {
        return noteService.search(q, Session.getUser().getId());
    }

}
