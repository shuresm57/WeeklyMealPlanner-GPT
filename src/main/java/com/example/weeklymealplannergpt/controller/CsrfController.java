package com.example.weeklymealplannergpt.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class CsrfController {
    
    @GetMapping("/csrf")
    public void getCsrfToken() {
    }
}
