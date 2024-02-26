package com.gmail.api.service;

import com.gmail.api.domain.Email;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface EmailService<T> {

    CompletableFuture<List<Email>> getAllEmails();
}
