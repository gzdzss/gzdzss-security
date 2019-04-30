package com.gzdzss.security.service;

import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

/**
 * @author <a href="mailto:zhouyanjie666666@gmail.com">zyj</a>
 * @date 2019/4/29
 */

public interface GzdzssUserDetailsService extends UserDetailsService {

    /**
     * 根据用户名获取 用户信息
     * @param username 用户名
     * @return 用户信息
     * @throws UsernameNotFoundException 用户名不存在
     */
    @Override
    GzdzssUserDetails loadUserByUsername(String username) throws UsernameNotFoundException;
}

