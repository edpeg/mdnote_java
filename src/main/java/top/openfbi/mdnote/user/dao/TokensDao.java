package top.openfbi.mdnote.user.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import top.openfbi.mdnote.user.model.Tokens;

/**
 * 全局 Token 限制配置数据访问层
 */
@Mapper
public interface TokensDao extends BaseMapper<Tokens> {
}
