package top.openfbi.mdnote.user.service;

import top.openfbi.mdnote.common.exception.ResultException;

/**
 * 用户 Token 限流与统计服务接口
 * 负责控制调用 AI 大模型的频率和用量
 */
public interface UserTokenUsageService {
    /**
     * 检查当前用户今天的 Token 是否已达到上限
     *
     * @param userId 用户ID
     * @throws ResultException 当 Token 额度用尽时，抛出业务异常中断后续流程
     */
    void checkTokenLimit(Long userId) throws ResultException;

    /**
     * 累加当前用户今天的 Token 消耗
     *
     * @param userId 用户ID
     * @param tokens 本次请求 AI 实际消耗的 Token 数量（通过 AI 节点返回）
     */
    void addTokenUsage(Long userId, Long tokens);
}
