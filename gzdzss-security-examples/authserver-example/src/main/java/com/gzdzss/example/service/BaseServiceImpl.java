package com.gzdzss.example.service;

import com.gzdzss.security.service.GzdzssAuthBaseService;
import com.gzdzss.security.service.GzdzssUserDetails;
import com.gzdzss.security.service.GzdzssUserDetailsService;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

/**
 * @author <a href="mailto:zhouyanjie666666@gmail.com">zyj</a>
 * @date 2019/4/29
 */

@Component
public class BaseServiceImpl implements GzdzssAuthBaseService {

    private static final Long USER_ID = 1L;
    private static final String USER = "user";
    private static final String U_PASSWORD = "123456";
    private static final String AUTHORITY = "USER";


    @Override
    public GzdzssUserDetailsService gzdzssUserDetailsService() {
        return (String username) -> {
            if (USER.equals(username)) {
                Set<GrantedAuthority> authorities = new HashSet<GrantedAuthority>();
                authorities.add(new SimpleGrantedAuthority(AUTHORITY));
                GzdzssUserDetails gzdzssUserDetails = new GzdzssUserDetails(USER_ID, USER, passwordEncoder().encode(U_PASSWORD), true, authorities);
                return gzdzssUserDetails;
            }
            throw new UsernameNotFoundException("用户不存在");
        };
    }


    @Override
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }


}
