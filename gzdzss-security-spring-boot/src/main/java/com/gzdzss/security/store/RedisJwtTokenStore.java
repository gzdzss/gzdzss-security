package com.gzdzss.security.store;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.gzdzss.security.service.GzdzssUserDetails;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.jwt.crypto.sign.MacSigner;
import org.springframework.util.StringUtils;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;


/**
 * @author <a href="mailto:zhouyanjie666666@gmail.com">zyj</a>
 * @date 2019/4/29
 */
public class RedisJwtTokenStore  {
    public static final String JWT_TO_ACCESS = "jwt_to_access:";

    private final StringRedisTemplate stringRedisTemplate;

    private final MacSigner macSigner;

    public RedisJwtTokenStore(StringRedisTemplate stringRedisTemplate, MacSigner macSigner) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.macSigner = macSigner;
    }

    private String serializeJwtKey(String tokenValue) {
        return JWT_TO_ACCESS + tokenValue;
    }




    public void storeJwtToken(String token, String jwtToken, Integer expiresIn) {
        String key = serializeJwtKey(token);
        stringRedisTemplate.opsForValue().set(key, jwtToken, expiresIn, TimeUnit.SECONDS);
    }

    public String readJwtTokenStr(String accessToken) {
        String jwtToken = stringRedisTemplate.opsForValue().get(JWT_TO_ACCESS + accessToken);
        if (StringUtils.hasText(jwtToken)) {
            return jwtToken;
        }
        return null;
    }



    public JSONObject readJwtToken(String accessToken) {
        String jwtToken = readJwtTokenStr(accessToken);
        if (StringUtils.hasText(jwtToken)) {
            Long authExpiresInSeconds = stringRedisTemplate.getExpire(JWT_TO_ACCESS + accessToken, TimeUnit.SECONDS);
            JSONObject respJson = new JSONObject();
            respJson.put("access_token", accessToken);
            respJson.put("token_type", "bearer");
            respJson.put("expires_in", authExpiresInSeconds);
            return respJson;
        }
        return null;
    }


    public String generateAccessKey(Authentication authentication) {
        GzdzssUserDetails gzdzssUserDetails = (GzdzssUserDetails) authentication.getPrincipal();
        Map<String, String> map = new HashMap<>(2);
        map.put("principal", JSON.toJSONString(gzdzssUserDetails));
        map.put("authorities", String.join(",", AuthorityUtils.authorityListToSet(authentication.getAuthorities())));
        return generateKey(map);
    }


    protected String generateKey(Map<String, String> values) {
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("MD5");
            byte[] bytes = digest.digest(values.toString().getBytes("UTF-8"));
            return String.format("%032x", new BigInteger(1, bytes));
        } catch (NoSuchAlgorithmException nsae) {
            throw new IllegalStateException("MD5 algorithm not available.  Fatal (should be in the JDK).", nsae);
        } catch (UnsupportedEncodingException uee) {
            throw new IllegalStateException("UTF-8 encoding not available.  Fatal (should be in the JDK).", uee);
        }
    }


}
