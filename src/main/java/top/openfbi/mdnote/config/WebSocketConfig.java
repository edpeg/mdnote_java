package top.openfbi.mdnote.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {
    private static final Logger logger = LoggerFactory.getLogger(WebSocketConfig.class);
    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        logger.debug("WebSocket连接请求");
        // webSocket拦截器
        registry.addHandler(new SocketHandler(), "/api/websocket/**").setAllowedOrigins("*").addInterceptors(new WebSocketHandshakeInterceptor());
    }

    /**
     * 向spring容器注册javabean由spring容器来管理
     */
    @Bean
    public SocketHandler myHandler()
    {
        return new SocketHandler();
    }
}
