package top.openfbi.mdnote.user.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.springframework.stereotype.Repository;
import top.openfbi.mdnote.user.model.LogoffUser;

@Repository
public interface LogoffUserDao extends BaseMapper<LogoffUser> {
}
