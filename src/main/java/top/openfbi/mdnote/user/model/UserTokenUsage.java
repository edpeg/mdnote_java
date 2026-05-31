package top.openfbi.mdnote.user.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDate;

/**
 * 用户 Token 使用情况实体类
 * 用于记录用户每天消耗的 AI Token 数量
 */
@Data
@TableName("t_user_token_usage")
public class UserTokenUsage {
    /**
     * 主键ID，自增
     */
    @TableId(type = IdType.AUTO)
    private Integer id;

    /**
     * 用户ID
     */
    private String userId;

    /**
     * 记录日期 (每天零点后会自动生成新的一天的记录)
     */
    private LocalDate date;

    /**
     * 当天已使用的 Token 数量
     */
    private Long tokensUsed;

    /**
     * 当天分配的 Token 额度上限
     */
    private Long dailyLimit;
}
