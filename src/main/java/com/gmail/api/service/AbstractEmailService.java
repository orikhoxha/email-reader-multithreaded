package com.gmail.api.service;

import com.gmail.api.domain.Email;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public abstract class AbstractEmailService<T> implements EmailService<T> {

    protected final Logger logger = LoggerFactory.getLogger(getClass());
    protected final ExecutorService executorService = Executors.newCachedThreadPool();

    protected abstract List<T> fetchMessagesForToken(String pageToken) throws IOException;

    protected abstract List<String> fetchPageTokens() throws IOException;

    protected abstract List<Email> processEmails(List<List<T>> messageLists);

    protected abstract Email constructEmail(T message);

    @Override
    public CompletableFuture<List<Email>> getAllEmails() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                List<String> pageTokens = fetchPageTokens();
                List<CompletableFuture<List<T>>> messageFutures = new ArrayList<>();

                for (String pageToken : pageTokens) {
                    CompletableFuture<List<T>> messageFuture = CompletableFuture.supplyAsync(() -> {
                        try {
                            return fetchMessagesForToken(pageToken);
                        } catch (IOException ex) {
                            throw new RuntimeException("Error fetching emails for token: " + pageToken, ex);
                        }
                    }, executorService);
                    messageFutures.add(messageFuture);
                }

                return messageFutures.stream()
                        .map(CompletableFuture::join)
                        .collect(Collectors.toList());
            } catch (IOException ex) {
                throw new RuntimeException("Error fetching emails", ex);
            }
        }, executorService).thenApplyAsync(this::processEmails, executorService);
    }
}
