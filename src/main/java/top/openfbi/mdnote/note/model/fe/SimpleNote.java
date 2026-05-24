package top.openfbi.mdnote.note.model.fe;

import lombok.Data;
import top.openfbi.mdnote.note.model.Note;

@Data
public class SimpleNote {

    private static int NOTE_CONTENT_CUT_LENGTH = 100;

    public SimpleNote() {
    }

    public SimpleNote(Note note) {
        this.setId(note.getId());
        this.setTitle(note.getTitle());
//        this.setContent(StringUtil.cut(note.getContent(), NOTE_CONTENT_CUT_LENGTH));
        this.setContent(note.getContent());
        this.setStatus(note.getStatus());
        this.setSummary(note.getSummary());
    }

    /**
     * ID
     */
    private Long id;

    /**
     * 笔记标题
     */
    private String title;

    /**
     * 笔记内容
     */
    private String content;

    /**
     * 笔记状态： 第一位为1：公开笔记,为0: 不公开笔记
     */
    private int status;

    /**
     * 笔记总结
     */
    private String summary;
}
