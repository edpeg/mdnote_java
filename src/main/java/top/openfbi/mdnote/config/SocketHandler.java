package top.openfbi.mdnote.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import top.openfbi.mdnote.common.ResultStatus;
import top.openfbi.mdnote.common.exception.ResultException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SocketHandler extends TextWebSocketHandler {

    // TODO 修改为ConscurrentHashMap
    public Map<String, Map<String,WebSocketSession>> webSocketMap = new ConcurrentHashMap<>();
    private static final Logger logger = LoggerFactory.getLogger(SocketHandler.class);
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws ResultException, IOException {
        Map<String, Object> attributes = session.getAttributes();
        HttpHeaders httpHeaders = session.getHandshakeHeaders();
        String query = session.getUri().getQuery();
        if (query == null){
            session.close();
            logger.debug("WebSocket请求参数错误");
            throw new ResultException(ResultStatus.CLIENT_REQUEST_PARAMETERS_ILLEGAL);
        }
        String noteId = query.split("=")[1];
        if (noteId.equals("0")){
            session.close();
            return;
        }
        logger.debug("收到WebSocket申请连接信息: {},连接ID为: {}",noteId,session.getId());
        if (webSocketMap.get(noteId) == null){
            logger.debug("生成笔记{}的map，存储订阅该笔记变动的WebsocketSesson对象",noteId);
            Map<String,WebSocketSession> noteMap = new ConcurrentHashMap<>();
            noteMap.put(session.getId(),session);
            webSocketMap.put(noteId,noteMap);
//            String replyMessage = false;
            logger.debug("连接建立成功，回复: 0 ");
            session.sendMessage(new TextMessage("0"));
            return;
        }else {
//            map.get(payload).add(session);
            if (webSocketMap.get(noteId).get(session.getId()) == null){
                webSocketMap.get(noteId).put(session.getId(),session);
                logger.debug("连接建立成功，回复: 0 ");
                session.sendMessage(new TextMessage("0"));
            }
        }
        logger.debug("WebSocket连接已建立,ID为: "+session.getId());
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws IOException {
        // 处理接收到的WebSocket消息
        String noteId = message.getPayload();
        logger.debug("收到笔记更改信息: {},连接ID为: {}",noteId,session.getId());

        for (Map.Entry<String, WebSocketSession> entry: webSocketMap.get(noteId).entrySet()) {
            WebSocketSession s = entry.getValue();
            if (s.getId() == session.getId()){
             logger.info("消息本身，不需要再次通知");
             continue;
            }
            // 发送回复消息给客户端
//            String replyMessage = "是否重新查询笔记：" + true;
            logger.debug("回复笔记更改信息: 1 已更改");
            s.sendMessage(new TextMessage("1"));
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status){
        List<String> noteWebSocketKey = new ArrayList<>();
        // 删除此webSocket连接缓存,并判断此笔记的webSocket是否都已断开。
        for (Map.Entry<String, Map<String, WebSocketSession>> noteWebSocket : webSocketMap.entrySet()) {
            Map<String, WebSocketSession> s = noteWebSocket.getValue();
            if(s.get(session.getId()) != null){
                logger.debug("删除webSocket连接，ID: "+session.getId());
                s.remove(session.getId());
            }
            if (s.size() == 0){
                logger.debug("此笔记已无webSocket连接订阅，移入待删除数组。笔记ID为: "+noteWebSocket.getKey());
                noteWebSocketKey.add(noteWebSocket.getKey());
            }
        }
        // 删除笔记连接缓存。
        for (String noteID:noteWebSocketKey) {
            webSocketMap.remove(noteID);
            logger.debug("当前笔记已无webSocket连接订阅，删除map缓存。笔记ID为: "+noteID);
        }
        // 当WebSocket连接关闭时调用
        logger.debug("WebSocket连接已关闭，ID为: "+session.getId());
    }
}
