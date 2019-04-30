package com.gzdzss.example.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author <a href="mailto:zhouyanjie666666@gmail.com">zyj</a>
 * @date 2019/4/30
 */

@RestController
public class TestController {


    @GetMapping("/test")
    public String test() {
        return "ok";
    }


    @GetMapping("/user")
    @PreAuthorize("hasAuthority('USER')")
    public Object user(Authentication authentication) {
        return authentication.getPrincipal();
    }

    @GetMapping("/admin")
    @PreAuthorize("hasAuthority('ADMIN')")
    public Object admin(Authentication authentication) {
        return authentication.getPrincipal();
    }
}
