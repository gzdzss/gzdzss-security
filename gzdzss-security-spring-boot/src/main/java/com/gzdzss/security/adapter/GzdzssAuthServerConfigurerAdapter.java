package com.gzdzss.security.adapter;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.gzdzss.security.filter.JwtAuthenticationFilter;
import com.gzdzss.security.properties.GzdzssSecurityProperties;
import com.gzdzss.security.service.GzdzssAuthBaseService;
import com.gzdzss.security.service.GzdzssUserAuthenticationConverter;
import com.gzdzss.security.store.RedisJwtTokenStore;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.jwt.Jwt;
import org.springframework.security.jwt.JwtHelper;
import org.springframework.security.jwt.crypto.sign.MacSigner;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.util.StringUtils;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

/**
 * @author <a href="mailto:zhouyanjie666666@gmail.com">zyj</a>
 * @date 2019/4/29
 */

@Slf4j
public class GzdzssAuthServerConfigurerAdapter extends WebSecurityConfigurerAdapter {

    private final GzdzssSecurityProperties properties;

    private final GzdzssAuthBaseService baseService;

    private final MacSigner macSigner;

    private final GzdzssUserAuthenticationConverter gzdzssUserAuthenticationConverter;

    private final RedisJwtTokenStore redisJwtTokenStore;

    private final GzdzssSecurityProperties gzdzssSecurityProperties;

    public GzdzssAuthServerConfigurerAdapter(GzdzssSecurityProperties properties, GzdzssAuthBaseService baseService, MacSigner macSigner, GzdzssUserAuthenticationConverter gzdzssUserAuthenticationConverter, RedisJwtTokenStore redisJwtTokenStore, GzdzssSecurityProperties gzdzssSecurityProperties) {
        this.properties = properties;
        this.baseService = baseService;
        this.macSigner = macSigner;
        this.gzdzssUserAuthenticationConverter = gzdzssUserAuthenticationConverter;
        this.redisJwtTokenStore = redisJwtTokenStore;
        this.gzdzssSecurityProperties = gzdzssSecurityProperties;
    }


    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(baseService.gzdzssUserDetailsService()).passwordEncoder(baseService.passwordEncoder());
    }


    @Override
    public void configure(HttpSecurity http) throws Exception {
        //禁用csrf 使用前后端分离
        http.csrf().disable();
        //禁用session
        http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);
        //添加jwt过滤器
        http.addFilterBefore(new JwtAuthenticationFilter(redisJwtTokenStore, macSigner, gzdzssUserAuthenticationConverter), BasicAuthenticationFilter.class);
        //请求不鉴权的url
        String ignoreUris = properties.getIgnoreUris();
        if (StringUtils.hasText(properties.getIgnoreUris())) {
            String[] splits = ignoreUris.split(",");
            http.authorizeRequests().antMatchers(splits).permitAll();
        }
        //登录转rest
        http.authorizeRequests().anyRequest().authenticated().and().logout().logoutSuccessHandler(new LogoutSuccessHandler() {
            @Override
            public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
                log.info("注销");
            }
        }).and().formLogin()
                .failureHandler(new AuthenticationFailureHandler() {
                    @Override
                    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException, ServletException {
                        response.setStatus(HttpStatus.BAD_REQUEST.value());
                        JSONObject jsonObject = new JSONObject();
                        jsonObject.put("error_description", exception.getMessage());
                        writeResp(response, jsonObject);
                    }
                }).successHandler(new AuthenticationSuccessHandler() {
            @Override
            public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
                String accessToken = redisJwtTokenStore.generateAccessKey(authentication);
                JSONObject jwtObj = redisJwtTokenStore.readJwtToken(accessToken);
                if (jwtObj != null) {
                    writeResp(response, jwtObj);
                } else {
                    Integer authExpiresInSeconds = gzdzssSecurityProperties.getAuthExpiresInSeconds();
                    String jwtToken = encodeToken(authentication, macSigner, authExpiresInSeconds);
                    redisJwtTokenStore.storeJwtToken(accessToken, jwtToken, authExpiresInSeconds);
                    JSONObject respJson = new JSONObject();
                    respJson.put("access_token", accessToken);
                    respJson.put("token_type", "bearer");
                    respJson.put("expires_in", authExpiresInSeconds);
                    writeResp(response, respJson);
                }
            }
        }).and().exceptionHandling().accessDeniedHandler(new AccessDeniedHandler() {
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

    private String encodeToken(Authentication authentication, MacSigner macSigner, Integer authExpiresInSeconds) {
        Map map = gzdzssUserAuthenticationConverter.convertUserAuthentication(authentication);
        map.put("exp", (System.currentTimeMillis() / 1000) + authExpiresInSeconds);
        Jwt jwt = JwtHelper.encode(JSON.toJSONString(map), macSigner);
        return jwt.getEncoded();
    }


}
