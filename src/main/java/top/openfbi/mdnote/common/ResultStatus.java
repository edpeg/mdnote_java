package top.openfbi.mdnote.common;

import lombok.Getter;
import lombok.ToString;

/**
 * 业务异常信息的描述
 */
@ToString
@Getter
public enum ResultStatus implements IResultStatus {

    // 0  操作成功
    SUCCESS(0, "操作成功"),

    // 10xx  用户业务逻辑错误
    USER_NOT_LOGIN(1000, "用户未登录"),
    USER_NAME_ILLEGAL(1001, "用户名非法"),
    USER_ID_NOT_EXIST(1002, "用户ID不存在"),
    USER_PASSWORD_LENGTH_ILLEGAL(1003, "密码长度非法"),
    USER_NAME_PRESENCE(1004, "用户名已存在"),
    USER_LOGGEN_IN(1005, "用户已登录"),
    USER_NO_PERMISSION(1006, "用户无权限查看此笔记"),
    USER_PASSWORD_FAIL(1007, "账户密码错误"),

    // 11xx 笔记业务逻辑错误
    NOTE_SAVE_FAIL(1101, "笔记保存失败"),
    NOTE_ID_NOT_EXIST(1102, "笔记不存在"),
    CLIENT_REQUEST_PARAMETERS_ILLEGAL(1103,"客户端请求参数非法"),

    // 12xx 文件上传失败
    FILE_CONTENT_EMPTY(1200, "文件为空，上传失败"),
    FILE_NAME_FALL(1201, "文件名为空，上传失败"),
    FILE_READ_FALL(1202, "文件读取错误，上传失败"),
    FILE_SAVE_FALL(1203, "文件保存错误，上传失败"),
    FILE_HASH_COLLISION_TOO_MUCH(1204, "图片hash冲突次数超限"),
    FILE_EXTENSION_EMPTY(1205, "文件不存在后缀名，上传失败"),
    FILE_EXTENSION_FORMAT_INACCURACY(1206, "文件后缀名格式不正确，上传失败"),
    QI_NIU_FILE_UPLOAD_FALL(1299, "七牛云图片上传错误"),

    // 90xx 内网服务错误
    INTERNAL_ELASTIC_CONNECT_FAIL(9000, "ES连接错误"),
    INTERNAL_MYSQ_CONNECT_FAIL(9001, "MYSQL连接错误"),
    INTERNAL_REDIS_CONNECT_FAIL(9002, "REDIS连接错误"),
    INTERNAL_SERVICE_CONNECT_FAIL(9003, "系统内部服务连接错误"),
    INTERNAL_SPRING_DATA_CONNECT_FAIL(9004, "SpringData连接错误，暂时未知那个数据框错误"),
    INTERNAL_ELASTIC_NO_DATA(9005, "ES未查到数据"),

    // 91xx  内网服务执行错误
    INTERNAL_MYSQL_SQL_EXEC_FAIL(9100, "SQL语句执行失败"),
    INTERNAL_ELASTIC_QUDRY_DSL_EXEC_FAIL(9101, "ES的QudryDSL语句执行失败"),

    //  101xx 公网服务错误
    EXTERNAL_QINIUYUN_CONNECT_FAIL(10100, "七牛云连接错误"),

    // 104xx 客户端请求错误
    CLIENT_IP_NOT_ALLOWED(10403, "禁止访问此接口"),
    CLIENT_REQUEST_ENTITY_TOO_LARGE(10413, "客户端请求体超限"),


    // 105XX 服务器异常状态
    SERVER_INTERNAL_ERROR(10500, "内部服务器错误"),
    SERVER_MAINTENANCE(10550, "系统维护中，暂停服务"),

    // 106xx 网络接口错误

    // 未知错误
    UNKNOWN(99999, "未知错误");

    /**
     * 业务异常码
     */
    private Integer code;
    /**
     * 业务异常信息描述
     */
    private String message;

    ResultStatus(Integer code, String message) {
        this.code = code;
        this.message = message;
    }
}