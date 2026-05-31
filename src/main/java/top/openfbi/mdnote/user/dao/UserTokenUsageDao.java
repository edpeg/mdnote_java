package top.openfbi.mdnote.user.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import top.openfbi.mdnote.user.model.UserTokenUsage;

/**
 * 用户 Token 使用情况的数据访问层接口
 * 继承 MyBatis-Plus 的 BaseMapper，提供基础的增删改查方法
 */
@Mapper
public interface UserTokenUsageDao extends BaseMapper<UserTokenUsage> {
}
