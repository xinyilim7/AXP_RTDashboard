package com.example.dashboardbackend;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/public")
public class PublicController {
    @RequestMapping("/hello")
    public String hello() {
        return "Hello, World!";
    }
}
