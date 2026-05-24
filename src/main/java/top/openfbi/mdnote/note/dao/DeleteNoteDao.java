package top.openfbi.mdnote.note.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import top.openfbi.mdnote.note.model.DeleteNote;

@Mapper
public interface DeleteNoteDao extends BaseMapper<DeleteNote> {

}