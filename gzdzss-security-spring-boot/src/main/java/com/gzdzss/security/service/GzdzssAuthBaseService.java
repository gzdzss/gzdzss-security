package com.gzdzss.security.service;

import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * @author <a href="mailto:zhouyanjie666666@gmail.com">zyj</a>
 * @date 2019/4/29
 */

public interface GzdzssAuthBaseService {

    /**
     * 获取用户
     *
     * @return 用户
     */
    GzdzssUserDetailsService gzdzssUserDetailsService();


    /**
     * 获取 加密机制
     *
     * @return 加密机制
     */
    PasswordEncoder passwordEncoder();

}
