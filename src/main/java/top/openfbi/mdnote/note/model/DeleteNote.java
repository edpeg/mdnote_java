package top.openfbi.mdnote.note.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("delete_note")
public class DeleteNote implements Serializable {


    /**
     * ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 笔记ID
     */
    @TableField("note_id")
    private Long noteId;

    /**
     * 用户ID
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 笔记标题
     */
    @TableField("title")
    private String title;

    /**
     * 笔记内容
     */
    @TableField("content")
    private String content;

    /**
     * 创建时间
     */
    @TableField("create_time")
    private String createTime;

    /**
     * 修改时间
     */
    @TableField("update_time")
    private String updateTime;

    /**
     * 删除时间
     */
    @TableField("delete_time")
    private String deleteTime;

    /**
     * 笔记状态： 第二位为1：公开笔记,为0: 不公开笔记
     */
    @TableField("status")
    private int status;

}
