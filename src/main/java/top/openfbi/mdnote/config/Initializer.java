package top.openfbi.mdnote.config;

import org.springframework.session.web.context.AbstractHttpSessionApplicationInitializer;

public class Initializer extends AbstractHttpSessionApplicationInitializer {

    public Initializer() {
        super(RedisHttpSessionConfig.class);
    }
}