package com.gmail.api.controller;

import com.gmail.api.domain.Email;
import com.gmail.api.service.EmailService;
import com.gmail.api.service.GmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/gmail")
public class GmailController {

    @Autowired
    @Qualifier("gmailService")
    private EmailService gmailService;

    @GetMapping("/emails")
    public ResponseEntity<String> getAllEmails() {
        gmailService.getAllEmails().join(); // Block while reading/processing emails
        return ResponseEntity.ok("Emails read");
    }
}
