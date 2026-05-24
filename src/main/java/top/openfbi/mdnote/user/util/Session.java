package top.openfbi.mdnote.user.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.session.FindByIndexNameSessionRepository;
import top.openfbi.mdnote.config.ResponseResultBody;
import top.openfbi.mdnote.user.model.UserSession;

/**
 * 用户登录操作类
 */
@ResponseResultBody
public class Session {

    private HttpSession httpSession;

    private static final Logger logger
            = LoggerFactory.getLogger(Session.class);
    /**
     * 获取用户Session
     */
    public Session() {
        // 获取当前链接session
        httpSession = SessionUtil.getHttpSession();
    }

    /**
     * 设置当前连接的HttpSession
     */
    public Session(HttpSession httpSession) {
        this.httpSession = httpSession;
    }

    /**
     * 设置用户登录Session
     * session的key为USER_SESSION_KEY
     */
    public static void setUser(UserSession userSession) {

        Session session = new Session();
//        session.httpSession.setAttribute(userSession.getUserName(), userSession);
        session.httpSession.setAttribute(FindByIndexNameSessionRepository.PRINCIPAL_NAME_INDEX_NAME, JSON.toJSONString(userSession));
        logger.debug("{}用户登录",userSession.getUserName());
    }

    /**
     * 获取当前登录用户信息
     */
    public static UserSession getUser() {
        //根据当前链接在spingsession中对应的name名称获取当前连接的用户信息
        Session session = new Session();
        UserSession userSession = JSONObject.parseObject((String) session.httpSession.getAttribute(FindByIndexNameSessionRepository.PRINCIPAL_NAME_INDEX_NAME),UserSession.class);
        return userSession;
    }

    /**
     * 删除当前连接Session，退出登录状态
     */
    public void invalidate() {
        httpSession.invalidate();
    }

}