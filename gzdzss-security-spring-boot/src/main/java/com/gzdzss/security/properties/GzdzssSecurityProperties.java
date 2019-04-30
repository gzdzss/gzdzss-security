package com.gzdzss.security.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author <a href="mailto:zhouyanjie666666@gmail.com">zyj</a>
 * @date 2019/4/29
 */

@ConfigurationProperties(prefix = "gzdzss.security")
public class GzdzssSecurityProperties {
    @Setter
    @Getter
    private String signingKey = "gzdzss";

    @Setter
    @Getter
    private Integer authExpiresInSeconds =  60 * 60 * 12;

    @Setter
    @Getter
    private String ignoreUris;

}
