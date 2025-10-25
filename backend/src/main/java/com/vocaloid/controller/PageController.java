package com.vocaloid.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PageController {

    /**
     * 猜歌游戏页面
     */
    @GetMapping("/")
    public String index() {
        return "forward:/index.html";
    }

    /**
     * 上传歌曲页面
     */
    @GetMapping("/upload")
    public String upload() {
        return "forward:/upload.html";
    }

    /**
     * 管理歌曲页面
     */
    @GetMapping("/manage")
    public String manage() {
        return "forward:/manage.html";
    }
}
