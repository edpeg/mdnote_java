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
@TableName("user")
public class User /* implements Serializable */  {
    /**
     * 用户ID
     */
    @TableId(value = "id",type = IdType.AUTO)
    private Long id;

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
     * 用户状态： 1 已注销
     */
    @TableField("status")
    private int status;
}
