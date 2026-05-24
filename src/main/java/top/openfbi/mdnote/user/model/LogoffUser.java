package top.openfbi.mdnote.user.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("logoff_user")
public class LogoffUser {
    /**
     * 注销表主键
     */
    @TableId(value = "id",type = IdType.AUTO)
    private Long id;

    /**
     * 用户id
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 用户名称
     */
    @TableField("user_name")
    private String userName;

    /**
     * 用户密码
     */
    @TableField("password")
    private String password;

    /**
     * 用户注册时间
     */
    @TableField("regist_time")
    private String registTime;

    /**
     * 用户最近一次登录时间
     */
    @TableField("last_login_time")
    private String lastLoginTime;

    /**
     * 用户注销时间
     */
    @TableField("logOff_time")
    private String logOffTime;

    /**
     * 用户状态：
     */
    @TableField("status")
    private int status;
}
