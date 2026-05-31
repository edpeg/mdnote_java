package top.openfbi.mdnote.note.controller;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import top.openfbi.mdnote.user.service.UserTokenUsageService;
import top.openfbi.mdnote.common.ResultStatus;
import top.openfbi.mdnote.common.exception.ResultException;
import top.openfbi.mdnote.config.ResponseResultBody;
import top.openfbi.mdnote.note.model.Note;
import top.openfbi.mdnote.note.model.fe.EsNoteResult;
import top.openfbi.mdnote.note.model.fe.SimpleNote;
import top.openfbi.mdnote.note.service.NoteService;
import top.openfbi.mdnote.user.model.UserSession;
import top.openfbi.mdnote.user.util.Session;

import javax.validation.constraints.DecimalMin;
import java.util.List;
import java.util.Map;

/**
 * note请求
 */
@RestController
@RequestMapping("/note")
@ResponseResultBody
public class NoteController {

    @Autowired
    private UserTokenUsageService userTokenUsageService;

    @Autowired
    private NoteService noteService;

    private static final Logger logger
            = LoggerFactory.getLogger(NoteController.class);

    /**
     * Submit方法专门的返回值
     * 原因：springboot会把string返回值给识别成html结构，导致无法序列化为Result类型。抛出类型转换异常。
     * 所以使用类的方式返回数据
     * 数据最好使用对象形式返回
     */
    @Data
    @AllArgsConstructor
    static
    class SubmitResponse {
        private Long noteId;
    }

    /**
     * 提交保存笔记
     */
    @ResponseBody
    @PostMapping("/save")
    public SubmitResponse save(@RequestBody Note note) throws ResultException {
        // 保存笔记
        note.setUserId(Session.getUser().getId());
        return new SubmitResponse(noteService.save(note));
    }



    /**
     * 根据id删除笔记
     */
    @ResponseBody
    @PostMapping("/delete")
    // TODO 修改为Post方式，防止恶意删除笔记
    public void delete(@RequestBody Map<String, String> person) throws ResultException {
        Long id = Long.valueOf(person.get("id"));
        UserSession userSession = Session.getUser();
        if (userSession.getUserName().equals("demo")){
            throw new ResultException(ResultStatus.DEMO_ACCOUNT_NOTE_FORBID_LOGOFF);
        }
        noteService.delete(id, userSession.getId());
    }

    /**
     * 根据id修改笔记公开状态
     */
    @ResponseBody
    @PostMapping("/setOpen")
    // TODO 修改为Post请求方式，防止钓鱼链接
    public void setOpen(@RequestBody Map<String, String> person) throws ResultException {
        Long id = Long.valueOf(person.get("id"));
        boolean open = Boolean.parseBoolean(person.get("open"));
        noteService.setOpen(id,open, Session.getUser().getId());
    }

    /**
     * 根据id查询笔记
     */
    @ResponseBody
    @GetMapping("/info")
    public SimpleNote info(@DecimalMin(value = "0",message = "笔记ID参数不能小于0")Long id) throws ResultException {
        Note note = noteService.info(id, Session.getUser().getId());
        return new SimpleNote(note);
    }

    /**
     * 根据id查询公开笔记
     */
    @ResponseBody
    @GetMapping("/open")
    public SimpleNote open(@DecimalMin(value = "0",message = "笔记ID参数不能小于0")Long id) throws ResultException {
        Note note = noteService.open(id);
        return new SimpleNote(note);
    }

    /**
     * 查询用户全部笔记
     */
    @ResponseBody
    @GetMapping("/list")
    public EsNoteResult list() throws ResultException {
        //创建返回类
        List<Note> notes = noteService.getNoteList(Session.getUser().getId());

        EsNoteResult esNoteResult = new EsNoteResult();
        List<SimpleNote> feNotes = notes.stream().map(note -> new SimpleNote(note)).toList();
        esNoteResult.setNoteList(feNotes);
        return esNoteResult;
    }

    /**
     * 使用es搜索笔记
     */
    @ResponseBody
    @GetMapping("/search")
    public EsNoteResult search(@RequestParam(name="q") String q) throws ResultException{
        return noteService.search(q, Session.getUser().getId());
    }

    /**
     * 根据id美化笔记
     */
    @ResponseBody
    @GetMapping("/beautify")
    public SubmitResponse beautify(@DecimalMin(value = "0",message = "笔记ID参数不能小于0")Long id) throws ResultException {
        Long noteId = noteService.beautify(id, Session.getUser().getId());
        return new SubmitResponse(noteId);
    }

    /**
     * 根据id总结笔记
     */
    @ResponseBody
    @GetMapping("/summary")
    public SubmitResponse summary(@DecimalMin(value = "0",message = "笔记ID参数不能小于0")Long id) throws ResultException {
        Long noteId = noteService.summary(id, Session.getUser().getId());
        return new SubmitResponse(noteId);
    }


    /**
     * Submit方法专门的返回值
     * 原因：springboot会把string返回值给识别成html结构，导致无法序列化为Result类型。抛出类型转换异常。
     * 所以使用类的方式返回数据
     * 数据最好使用对象形式返回
     */
    @Data
    @AllArgsConstructor
    static
    class SubmitResponseNoteContent {
        private String noteContent;
    }
    /**
     * 润色内容
     */
//    @ResponseBody
    @PostMapping("/polish")
    public SubmitResponseNoteContent polish(@RequestBody Map<String, String> request) throws ResultException {
        String noteContent = request.get("noteContent");
        String noteContentNew = noteService.polish(noteContent, Session.getUser().getId());
        return new SubmitResponseNoteContent(noteContentNew);
    }

    /**
     * 续写内容
     */
    @PostMapping("/expand")
    public SubmitResponseNoteContent expand(@RequestBody Map<String, String> request) throws ResultException {
        String noteContent = request.get("noteContent");
        String noteContentNew = noteService.expand(noteContent, Session.getUser().getId());
        return new SubmitResponseNoteContent(noteContentNew);
    }

    /**
     * 修正语法与错别字
     */
    @PostMapping("/grammar")
    public SubmitResponseNoteContent grammar(@RequestBody Map<String, String> request) throws ResultException {
        String noteContent = request.get("noteContent");
        String noteContentNew = noteService.grammar(noteContent, Session.getUser().getId());
        return new SubmitResponseNoteContent(noteContentNew);
    }

    /**
     * 提取文段摘要
     */
    @PostMapping("/summaryText")
    public SubmitResponseNoteContent summaryText(@RequestBody Map<String, String> request) throws ResultException {
        String noteContent = request.get("noteContent");
        String noteContentNew = noteService.summaryText(noteContent, Session.getUser().getId());
        return new SubmitResponseNoteContent(noteContentNew);
    }


    /**
     * 图片处理
     */
//    @ResponseBody
    @PostMapping("/imgAnalysis")
    public SubmitResponseNoteContent imgAnalysis(@RequestBody Map<String, String> request) throws ResultException {
        String imgUrl = request.get("imgUrl");
        String noteContentNew = noteService.imgAnalysis(imgUrl, Session.getUser().getId());
        return new SubmitResponseNoteContent(noteContentNew);
    }

    /**
     * 将笔记向量化存储
     */
    @ResponseBody
    @PostMapping("/vectorize")
    public void vectorize(@RequestBody Map<String, String> request) throws ResultException {
        Long id = Long.valueOf(request.get("id"));
        noteService.vectorize(id, Session.getUser().getId());
    }

    /**
     * AI问答接口，使用SSE转发流
     */
    @GetMapping(value = "/chat")
    public void chat(@RequestParam("query") String query, jakarta.servlet.http.HttpServletResponse response) {
        Long userId = Session.getUser().getId();
        try {
            // 在发起 SSE 转发之前，先校验用户的 Token 是否超过今日限制
            userTokenUsageService.checkTokenLimit(userId);
        } catch (ResultException e) {
            response.setContentType("text/event-stream;charset=UTF-8");
            response.setHeader("Cache-Control", "no-cache");
            response.setHeader("Connection", "keep-alive");
            try {
                java.io.PrintWriter out = response.getWriter();
                String errorJson = String.format("{\"type\": \"error\", \"code\": %d, \"message\": \"%s\"}", e.getResultStatus().getCode(), e.getResultStatus().getMessage());
                out.print("data: " + errorJson + "\n\n");
                out.flush();
            } catch (Exception ex) {}
            return;
        }
        response.setContentType("text/event-stream;charset=UTF-8");
        response.setHeader("Cache-Control", "no-cache");
        response.setHeader("Connection", "keep-alive");
        
        long startTime = System.currentTimeMillis();
        logger.info("[Java代理层耗时日志] 收到用户检索请求: '{}', 开始转发给 Python AI 服务...", query);
        
        try {
            java.net.URL url = new java.net.URL("http://127.0.0.1:8083/api/note/chat");
            java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Accept", "text-event-stream");
            conn.setDoOutput(true);
            
            String jsonInputString = "{\"query\": \"" + query.replace("\"", "\\\"").replace("\n", "\\n") + "\", \"userId\": \"" + userId + "\"}";
            
            try(java.io.OutputStream os = conn.getOutputStream()) {
                byte[] input = jsonInputString.getBytes("utf-8");
                os.write(input, 0, input.length);			
            }
            
            long requestSentTime = System.currentTimeMillis();
            logger.info("[Java代理层耗时日志] HTTP 请求发送完毕, 耗时: {} ms, 正在等待 AI 服务返回首字流数据...", requestSentTime - startTime);
            
            try(java.io.BufferedReader in = new java.io.BufferedReader(new java.io.InputStreamReader(conn.getInputStream(), "UTF-8"));
                java.io.PrintWriter out = response.getWriter()) {
                String line;
                boolean isFirstLine = true;
                while ((line = in.readLine()) != null) {
                    if (isFirstLine && !line.trim().isEmpty()) {
                        long firstLineTime = System.currentTimeMillis();
                        logger.info("[Java代理层耗时日志] 成功接收到 AI 服务的第一段数据包 (TTFT), 空等耗时: {} ms", firstLineTime - requestSentTime);
                        isFirstLine = false;
                    }
                    out.print(line + "\n");
                    out.flush();
                    
                    if (line.startsWith("data: ")) {
                        try {
                            String dataJson = line.substring(6);
                            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                            com.fasterxml.jackson.databind.JsonNode node = mapper.readTree(dataJson);
                            
                            // 监听 Python 端发来的最终结算信息：一旦发现 `tokens` 字段，说明问答已结束并给出了本次总消耗
                            if (node.has("tokens")) {
                                Long tokens = node.get("tokens").asLong();
                                // 在 Java 业务层中执行扣费落库逻辑
                                userTokenUsageService.addTokenUsage(userId, tokens);
                            }
                        } catch(Exception ex) {}
                    }
                }
                long endTime = System.currentTimeMillis();
                logger.info("[Java代理层耗时日志] SSE 数据流转发彻底结束, 数据传输持续期耗时: {} ms", endTime - requestSentTime);
                logger.info("[Java代理层耗时日志] Java 代理接口全链路总耗时: {} ms", endTime - startTime);
            }
        } catch (Exception e) {
            long errorTime = System.currentTimeMillis();
            logger.error("[Java代理层耗时日志] 转发流数据异常中断, 已耗时: {} ms, 错误: {}", errorTime - startTime, e.getMessage());
            try {
                java.io.PrintWriter out = response.getWriter();
                String errorJson = String.format("{\"type\": \"error\", \"code\": 10500, \"message\": \"%s\"}", "内部服务器错误");
                out.print("data: " + errorJson + "\n\n");
                out.flush();
            } catch (Exception ex) {}
            e.printStackTrace();
        }
    }
}
