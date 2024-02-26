package com.gmail.api.controller;

import com.gmail.api.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/outlook")
public class OutlookController {

    @Autowired
    @Qualifier("outlookService")
    private EmailService outlookService;

    @GetMapping("/email")
    public ResponseEntity<String> getAllEmails() {
        outlookService.getAllEmails().join(); // Block while reading/processing emails
        return ResponseEntity.ok("Emails read");
    }
}
