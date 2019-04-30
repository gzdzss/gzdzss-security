package com.gzdzss.security.filter;

import com.alibaba.fastjson.JSON;
import com.gzdzss.security.service.GzdzssUserAuthenticationConverter;
import com.gzdzss.security.store.RedisJwtTokenStore;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.jwt.Jwt;
import org.springframework.security.jwt.JwtHelper;
import org.springframework.security.jwt.crypto.sign.MacSigner;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

/**
 * @author <a href="mailto:zhouyanjie666666@gmail.com">zyj</a>
 * @date 2019/4/19
 */


@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String AUTHORIZATION = "authorization";
    private static final String BEARER = "bearer ";
    private static final int BEARER_LENGTH = 7;
    private static final int SECOND_TRANS = 1000;

    private final RedisJwtTokenStore redisJwtTokenStore;

    private final MacSigner macSigner;

    private final GzdzssUserAuthenticationConverter gzdzssUserAuthenticationConverter;

    public JwtAuthenticationFilter(RedisJwtTokenStore redisJwtTokenStore, MacSigner macSigner, GzdzssUserAuthenticationConverter gzdzssUserAuthenticationConverter) {
        this.redisJwtTokenStore = redisJwtTokenStore;
        this.macSigner = macSigner;
        this.gzdzssUserAuthenticationConverter = gzdzssUserAuthenticationConverter;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
        SecurityContextHolder.getContext().setAuthentication(decodeToken(request, macSigner));
        chain.doFilter(request, response);
    }

    public Authentication decodeToken(HttpServletRequest request, MacSigner macSigner) {
        String accessToken = getToken(request);
        if (StringUtils.hasText(accessToken)) {
            String jwtToken = redisJwtTokenStore.readJwtTokenStr(accessToken);
            if (StringUtils.hasText(jwtToken)) {
                return decodeToken(jwtToken, macSigner);
            } else {
                log.info("accessToken:[{}]不存在", accessToken);
            }
        }
        return null;
    }


    public Authentication decodeToken(String token, MacSigner macSigner) {
        Jwt jwt = JwtHelper.decodeAndVerify(token, macSigner);
        Map map = JSON.parseObject(jwt.getClaims(), Map.class);
        Long exp = Long.valueOf(map.get("exp").toString());
        //如果token过期
        if ((System.currentTimeMillis() / SECOND_TRANS) > exp) {
            log.info("token:[{}]已过期", token);
            return null;
        }
        return gzdzssUserAuthenticationConverter.extractAuthentication(map);
    }

    private String getToken(HttpServletRequest request) {
        String header = request.getHeader(AUTHORIZATION);
        if (header != null && header.toLowerCase().startsWith(BEARER)) {
            return header.substring(BEARER_LENGTH);
        }
        return null;
    }


}
