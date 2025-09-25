package com.example.ossupload.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * 首页控制器
 */
@Controller
public class IndexController {
    
    /**
     * 首页
     */
    @GetMapping("/")
    public String index() {
        return "index";
    }
}