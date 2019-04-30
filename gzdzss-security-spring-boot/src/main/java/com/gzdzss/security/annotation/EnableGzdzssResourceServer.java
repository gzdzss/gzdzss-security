package com.gzdzss.security.annotation;

import com.gzdzss.security.configuration.GzdzssBaseConfiguration;
import com.gzdzss.security.configuration.GzdzssResourceServerConfiguration;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * @author <a href="mailto:zhouyanjie666666@gmail.com">zyj</a>
 * @date 2019/4/29
 */

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import({GzdzssResourceServerConfiguration.class, GzdzssBaseConfiguration.class})
public @interface EnableGzdzssResourceServer {
}
