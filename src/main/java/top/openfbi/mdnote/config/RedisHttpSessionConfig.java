package top.openfbi.mdnote.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisIndexedHttpSession;

@Configuration()
/**
 * 配置springSession使用redis为session为数据库。
 * 存储在Redis中的session以json形式存储
 * 配置sessiopn在Redis中的名称前缀
 * 配置session生命周期为一个月
 */
@EnableRedisIndexedHttpSession(redisNamespace = "userSession", maxInactiveIntervalInSeconds = 30 * 24 * 60 * 60)
public class RedisHttpSessionConfig {

    @Bean
    public RedisSerializer springSessionDefaultRedisSerializer() {
        return RedisSerializer.json();
    }

    @Bean
    public LettuceConnectionFactory redisConnectionFactory() {
        return new LettuceConnectionFactory();
    }


}
