package top.openfbi.mdnote.config;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.session.FindByIndexNameSessionRepository;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import top.openfbi.mdnote.common.Result;
import top.openfbi.mdnote.common.ResultStatus;
import top.openfbi.mdnote.user.model.UserSession;
import top.openfbi.mdnote.user.util.Session;
import top.openfbi.mdnote.utils.JsonResponse;
/**
 * 用户登录拦截
 * 用户请求接口后会在此判断用户是否已登录，登录放行，未登录拦截请求，返回未登录状态码
 */

@Component
public class LoginInterceptor implements HandlerInterceptor {
    private static final Logger logger
            = LoggerFactory.getLogger(LoginInterceptor.class);

    /**
     * 拦截请求
     * 判断请求request中session是否存在，判断请求中的session是否在系统中存在。都存在即代表已登录。
     * 否则返回请登录状态码
     *
     * @param request  current HTTP request
     * @param response current HTTP response
     * @param handler  chosen handler to execute, for type and/or instance evaluation
     * @return
     * @throws Exception
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
                             Object handler) throws Exception {
        // 1.得到 HttpSession 对象
        HttpSession httpSession = request.getSession(false);

        // 判断请求是否携带session。判断session是否在系统中保存
        if (httpSession != null && new Session(httpSession).getUser() != null) {
            // 表示已经登录
            UserSession user = JSONObject.parseObject((String) httpSession.getAttribute(FindByIndexNameSessionRepository.PRINCIPAL_NAME_INDEX_NAME),UserSession.class);
            logger.info("用户已登录,用户名: {}，用户ID: {}", user.getUserName(), user.getId());
            return true;
        }
        // 执行到此代码表示未登录，未登录就跳转到登录页面
//        response.sendRedirect("/login.html");
        logger.info("请求被拦截，拦截URL: {}", request.getRequestURI());
        // 序列化状态码
        String json = JSON.toJSONString(Result.failure(ResultStatus.USER_NOT_LOGIN, ""));
        // 返回数据
        JsonResponse.returnJsonResponse(response, json, "登陆拦截返回客户端数据失败,异常信息");
        return false;
    }
}
