package top.openfbi.mdnote.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

public class WebSocketHandshakeInterceptor implements HandshakeInterceptor {

    private static final Logger logger
            = LoggerFactory.getLogger(WebSocketHandshakeInterceptor.class);
    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {
        //用户身份认证
//        if (request instanceof ServletServerHttpRequest) {
//            ServletServerHttpRequest serverHttpRequest = (ServletServerHttpRequest) request;
//            HttpSession httpSession = serverHttpRequest.getServletRequest().getSession();
//            // 判断请求是否携带session。判断session是否在系统中保存
//            if (httpSession != null && new Session(httpSession).getUser() != null) {
//                // 表示已经登录
//                UserSession user = JSONObject.parseObject((String) httpSession.getAttribute(FindByIndexNameSessionRepository.PRINCIPAL_NAME_INDEX_NAME),UserSession.class);
//                logger.info("用户建立webSocket连接,用户名: {}，用户ID: {}", user.getUserName(), user.getId());
//                return true;
//            }
//        }
        logger.debug("websocket拦截器");
        return true;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Exception exception) {

    }
}
