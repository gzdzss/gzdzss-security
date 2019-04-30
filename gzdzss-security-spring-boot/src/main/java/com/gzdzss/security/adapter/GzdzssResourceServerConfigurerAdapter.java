package com.gzdzss.security.adapter;

import com.alibaba.fastjson.JSONObject;
import com.gzdzss.security.filter.JwtAuthenticationFilter;
import com.gzdzss.security.properties.GzdzssSecurityProperties;
import com.gzdzss.security.service.GzdzssUserAuthenticationConverter;
import com.gzdzss.security.store.RedisJwtTokenStore;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.jwt.crypto.sign.MacSigner;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.util.StringUtils;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author <a href="mailto:zhouyanjie666666@gmail.com">zyj</a>
 * @date 2019/4/30
 */

public class GzdzssResourceServerConfigurerAdapter extends WebSecurityConfigurerAdapter {

    private final GzdzssSecurityProperties properties;

    private final MacSigner macSigner;

    private final GzdzssUserAuthenticationConverter gzdzssUserAuthenticationConverter;

    private final RedisJwtTokenStore redisJwtTokenStore;

    public GzdzssResourceServerConfigurerAdapter(GzdzssSecurityProperties properties, MacSigner macSigner, GzdzssUserAuthenticationConverter gzdzssUserAuthenticationConverter, RedisJwtTokenStore redisJwtTokenStore) {
        this.properties = properties;
        this.macSigner = macSigner;
        this.gzdzssUserAuthenticationConverter = gzdzssUserAuthenticationConverter;
        this.redisJwtTokenStore = redisJwtTokenStore;
    }

    @Override
    public void configure(HttpSecurity http) throws Exception {
        //禁用csrf 使用前后端分离
        http.csrf().disable();
        //禁用session
        http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);
        String ignoreUris = properties.getIgnoreUris();
        if (StringUtils.hasText(properties.getIgnoreUris())) {
            String[] splits = ignoreUris.split(",");
            http.authorizeRequests().antMatchers(splits).permitAll();
        }

        //添加jwt过滤器
        http.addFilterBefore(new JwtAuthenticationFilter(redisJwtTokenStore, macSigner, gzdzssUserAuthenticationConverter), BasicAuthenticationFilter.class);
        http.authorizeRequests().anyRequest().authenticated();
        http.exceptionHandling().accessDeniedHandler(new AccessDeniedHandler() {
            @Override
            public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException, ServletException {
                response.setStatus(HttpStatus.FORBIDDEN.value());
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("error_description", accessDeniedException.getMessage());
                writeResp(response, jsonObject);
            }
        }).authenticationEntryPoint(new AuthenticationEntryPoint() {
            @Override
            public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {
                response.setStatus(HttpStatus.UNAUTHORIZED.value());
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("error_description", authException.getMessage());
                writeResp(response, jsonObject);
            }
        });
    }

    private void writeResp(HttpServletResponse response, JSONObject jsonObject) throws IOException {
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json");
        response.getWriter().write(jsonObject.toJSONString());
        response.getWriter().flush();
    }


}
