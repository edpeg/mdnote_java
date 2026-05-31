package top.openfbi.mdnote.user.service;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.session.FindByIndexNameSessionRepository;
import org.springframework.stereotype.Service;
import top.openfbi.mdnote.common.ResultStatus;
import top.openfbi.mdnote.common.exception.ResultException;
import top.openfbi.mdnote.config.ResponseResultBody;
import top.openfbi.mdnote.note.service.NoteService;
import top.openfbi.mdnote.user.dao.LogoffUserDao;
import top.openfbi.mdnote.user.dao.UserDao;
import top.openfbi.mdnote.user.dao.TokensDao;
import top.openfbi.mdnote.user.dao.UserTokenUsageDao;
import top.openfbi.mdnote.user.model.LogoffUser;
import top.openfbi.mdnote.user.model.User;
import top.openfbi.mdnote.user.model.Tokens;
import top.openfbi.mdnote.user.model.UserTokenUsage;
import top.openfbi.mdnote.user.model.UserSession;
import top.openfbi.mdnote.user.util.Session;
import top.openfbi.mdnote.utils.IntBytOperate;
import top.openfbi.mdnote.utils.Time;

import java.util.Map;

@Service
@ResponseResultBody
public class UserService {
    @Autowired
    private NoteService noteService;
    @Autowired
    private UserDao userDao;
    @Autowired
    private LogoffUserDao logoffUserDao;
    @Autowired
    private UserTokenUsageDao userTokenUsageDao;
    @Autowired
    private TokensDao tokensDao;

    @Autowired
    FindByIndexNameSessionRepository sessionRepository;

    private static final Logger logger
            = LoggerFactory.getLogger(UserService.class);

    // 登录账户
    public String login(User user) throws ResultException {
        //创建查询sql对象
        QueryWrapper<User> QR = new QueryWrapper();
        // 查询用户是否存在
        User selectUser = userDao.selectOne(QR.eq("user_name", user.getUserName()));
        if (selectUser == null || IntBytOperate.getBit(selectUser.getStatus(),1) == 1) {
            // 返回用户ID不存在异常
            throw new ResultException(ResultStatus.USER_ID_NOT_EXIST);
        }
        if (!selectUser.getPassword().equals(user.getPassword())) {
            // 返回用户密码错误
            throw new ResultException(ResultStatus.USER_PASSWORD_FAIL);
        }
        selectUser.setLastLoginTime(Time.current());
        UpdateWrapper updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("id",selectUser.getId());
        updateWrapper.set("last_login_time",selectUser.getLastLoginTime());
        int s = userDao.update(selectUser,updateWrapper);
        if( s!= 1){
            logger.warn("用户信息更新失败: {}", selectUser);
            throw new ResultException(ResultStatus.INTERNAL_MYSQL_SQL_EXEC_FAIL);
        }
        // 设置用户登录Session
        Session.setUser(new UserSession(selectUser));
        // 返回user名称
        return selectUser.getUserName();
    }

    // 退出登录
    public void logOut(Long userId) {
        // 删除用户Session
        new Session().invalidate();
        logger.info("用户退出成功，用户ID: {}", userId);
    }

    // 注销账户
    public void logOff(Long userId,UserSession userSession) throws ResultException {
        logger.info("删除账户，账户ID: {}", userId);
        // 删除用户所有笔记
        noteService.deleteAllNoteOfUser(userId);
        // 删除所有当前用户在线session
        Map<String, org.springframework.session.Session> map = (Map<String, org.springframework.session.Session>) sessionRepository.findByIndexNameAndIndexValue(FindByIndexNameSessionRepository.PRINCIPAL_NAME_INDEX_NAME, JSON.toJSONString(userSession));
        for (org.springframework.session.Session session:map.values()) {
            sessionRepository.deleteById(session.getId());
        }
        logOut(userId);
        //创建查询sql对象
        QueryWrapper<User> QR = new QueryWrapper();
        User selectUser = userDao.selectById(userId);
        LogoffUser logoffUser = new LogoffUser();
        logoffUser.setUserId(selectUser.getId());
        logoffUser.setUserName(selectUser.getUserName());
        logoffUser.setPassword(selectUser.getPassword());
        logoffUser.setRegistTime(selectUser.getRegistTime());
        logoffUser.setLastLoginTime(selectUser.getLastLoginTime());
        logoffUser.setStatus(selectUser.getStatus());
        logoffUser.setLogOffTime(Time.current());
        selectUser.setStatus(IntBytOperate.setBitToOne(selectUser.getStatus(),1));
        if( userDao.deleteById(selectUser.getId())!= 1){
            logger.error("删除用户失败，用户ID: {}", userId);
            // 返回mysql数据库异常
            throw new ResultException(ResultStatus.INTERNAL_MYSQL_SQL_EXEC_FAIL);
        }
        // 写入注销表
        logoffUserDao.insert(logoffUser);
    }

    // 注册账户
    public String registService(User user) throws ResultException {
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.in("user_name", user.getUserName());
        if (userDao.selectOne(wrapper) != null) {
            logger.info("用户名已存在: {}", user.getUserName());
            // 返回mysql数据库异常
            throw new ResultException(ResultStatus.USER_NAME_PRESENCE);
        }
        // 保存用户信息，创建账户
        user.setRegistTime(Time.current());
        user.setLastLoginTime(Time.current());
        userDao.insert(user);
        
        // 为新用户初始化当天的 Token 使用记录
        Tokens globalConfig = tokensDao.selectById(1);
        long currentLimit = (globalConfig != null && globalConfig.getDailyLimit() != null) ? globalConfig.getDailyLimit() : 10000000L;
        UserTokenUsage usage = new UserTokenUsage();
        usage.setUserId(String.valueOf(user.getId()));
        usage.setDate(java.time.LocalDate.now());
        usage.setTokensUsed(0L);
        usage.setDailyLimit(currentLimit);
        userTokenUsageDao.insert(usage);

        // 登录创建好的对象
        // 设置用户登录Session
        Session.setUser(new UserSession(user));
        return user.getUserName();
    }
}