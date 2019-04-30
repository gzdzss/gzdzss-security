package com.gzdzss.security.configuration;

import com.gzdzss.security.properties.GzdzssSecurityProperties;
import com.gzdzss.security.service.GzdzssUserAuthenticationConverter;
import com.gzdzss.security.store.RedisJwtTokenStore;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.jwt.crypto.sign.MacSigner;

/**
 * @author <a href="mailto:zhouyanjie666666@gmail.com">zyj</a>
 * @date 2019/4/30
 */

@EnableConfigurationProperties(GzdzssSecurityProperties.class)
@Import({GzdzssUserAuthenticationConverter.class, RedisJwtTokenStore.class})
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class GzdzssBaseConfiguration {


    @Bean
    public MacSigner macSigner(GzdzssSecurityProperties properties) {
        return new MacSigner(properties.getSigningKey());
    }


}
