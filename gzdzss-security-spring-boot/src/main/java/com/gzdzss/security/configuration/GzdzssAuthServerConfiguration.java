package com.gzdzss.security.configuration;

import com.gzdzss.security.adapter.GzdzssAuthServerConfigurerAdapter;
import org.springframework.context.annotation.Import;

/**
 * @author <a href="mailto:zhouyanjie666666@gmail.com">zyj</a>
 * @date 2019/4/29
 */

@Import({GzdzssAuthServerConfigurerAdapter.class})
public class GzdzssAuthServerConfiguration {


}
