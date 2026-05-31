package top.openfbi.mdnote.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.stereotype.Service;
import top.openfbi.mdnote.common.exception.ResultException;
import top.openfbi.mdnote.user.dao.UserTokenUsageDao;
import top.openfbi.mdnote.user.model.UserTokenUsage;
import top.openfbi.mdnote.user.service.UserTokenUsageService;
import top.openfbi.mdnote.user.dao.TokensDao;
import top.openfbi.mdnote.user.model.Tokens;

import org.springframework.beans.factory.annotation.Autowired;
import java.time.LocalDate;

/**
 * 用户 Token 限流与统计服务实现类
 */
@Service
public class UserTokenUsageServiceImpl implements UserTokenUsageService {

    @Autowired
    private UserTokenUsageDao userTokenUsageDao;

    @Autowired
    private TokensDao tokensDao;

    /**
     * 校验 Token 额度：在每次请求 AI 接口之前调用
     */
    @Override
    public void checkTokenLimit(Long userId) throws ResultException {
        if (userId == null) {
            return;
        }
        LocalDate today = LocalDate.now(); // 获取当前日期
        UserTokenUsage usage = getOrCreateUsage(userId, today);
        
        // 如果查到了当天的记录，且使用量已经大于等于限制，则拒绝服务
        if (usage.getTokensUsed() >= usage.getDailyLimit()) {
            throw new ResultException(top.openfbi.mdnote.common.ResultStatus.AI_TOKEN_EXHAUSTED);
        }
    }

    /**
     * 累加 Token 额度：在请求 AI 接口并拿到结果后调用
     */
    @Override
    public void addTokenUsage(Long userId, Long tokens) {
        // 如果入参非法或没有产生消耗，则直接跳过
        if (userId == null || tokens == null || tokens <= 0) {
            return;
        }
        LocalDate today = LocalDate.now();
        UserTokenUsage usage = getOrCreateUsage(userId, today);
        
        // 累加上本次花费的 token 并更新
        usage.setTokensUsed(usage.getTokensUsed() + tokens);
        userTokenUsageDao.updateById(usage);
    }

    /**
     * 获取用户当天的 Token 使用记录，如果不存在则自动创建（兜底机制）
     */
    private UserTokenUsage getOrCreateUsage(Long userId, LocalDate today) {
        QueryWrapper<UserTokenUsage> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", String.valueOf(userId)).eq("date", today);
        UserTokenUsage usage = userTokenUsageDao.selectOne(queryWrapper);
        
        if (usage == null) {
            Tokens globalConfig = tokensDao.selectById(1);
            long currentLimit = (globalConfig != null && globalConfig.getDailyLimit() != null) ? globalConfig.getDailyLimit() : 10000000L;

            usage = new UserTokenUsage();
            usage.setUserId(String.valueOf(userId));
            usage.setDate(today);
            usage.setTokensUsed(0L);
            usage.setDailyLimit(currentLimit);
            userTokenUsageDao.insert(usage);
        }
        return usage;
    }
}
