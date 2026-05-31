package top.openfbi.mdnote.user.task;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import top.openfbi.mdnote.user.dao.TokensDao;
import top.openfbi.mdnote.user.dao.UserDao;
import top.openfbi.mdnote.user.dao.UserTokenUsageDao;
import top.openfbi.mdnote.user.model.Tokens;
import top.openfbi.mdnote.user.model.User;
import top.openfbi.mdnote.user.model.UserTokenUsage;

import org.springframework.beans.factory.annotation.Autowired;
import java.time.LocalDate;
import java.util.List;

/**
 * 每天定时为所有用户生成当天的 Token 消耗记录
 */
@Component
public class TokenUsageTask {

    private static final Logger log = LoggerFactory.getLogger(TokenUsageTask.class);

    @Autowired
    private UserDao userDao;

    @Autowired
    private UserTokenUsageDao userTokenUsageDao;

    @Autowired
    private TokensDao tokensDao;

    /**
     * 每天凌晨0点执行一次，为所有正常用户初始化当天的Token使用记录
     */
    @Scheduled(cron = "0 0 0 * * ?")
    @Transactional(rollbackFor = Exception.class)
    public void initDailyTokenUsage() {
        log.info("开始执行每天的Token数据初始化任务...");
        LocalDate today = LocalDate.now();

        // 1. 获取全局配置的每日限额
        Tokens globalConfig = tokensDao.selectById(1);
        long currentLimit = (globalConfig != null && globalConfig.getDailyLimit() != null) ? globalConfig.getDailyLimit() : 10000000L;

        // 2. 获取所有正常状态的用户 (假设 status != 1 表示未注销)
        QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
        userQueryWrapper.ne("status", 1);
        List<User> userList = userDao.selectList(userQueryWrapper);

        if (userList != null && !userList.isEmpty()) {
            // 3. 为每个用户创建今天的 Token 使用记录
            for (User user : userList) {
                // 检查是否已经存在今天的记录，避免重复插入
                QueryWrapper<UserTokenUsage> usageQuery = new QueryWrapper<>();
                usageQuery.eq("user_id", String.valueOf(user.getId())).eq("date", today);
                
                if (userTokenUsageDao.selectCount(usageQuery) == 0) {
                    UserTokenUsage usage = new UserTokenUsage();
                    usage.setUserId(String.valueOf(user.getId()));
                    usage.setDate(today);
                    usage.setTokensUsed(0L);
                    usage.setDailyLimit(currentLimit);
                    userTokenUsageDao.insert(usage);
                }
            }
            log.info("已成功为 {} 个用户初始化了当天的Token数据", userList.size());
        } else {
            log.info("未找到需要初始化Token数据的用户。");
        }
    }
}
