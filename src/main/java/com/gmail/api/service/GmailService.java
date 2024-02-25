package com.gmail.api.service;

import com.gmail.api.domain.Attachment;
import com.gmail.api.domain.Email;
import com.gmail.api.util.EmailDecoderUtil;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.ListMessagesResponse;
import com.google.api.services.gmail.model.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

@Service
public class GmailService {

    @Autowired
    private Gmail gmail;

    private static final Logger logger = LoggerFactory.getLogger(GmailService.class);

    private final ExecutorService executorService = Executors.newCachedThreadPool();

    public CompletableFuture<List<Email>> getAllEmails() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                List<String> pageTokens = fetchPageTokens(); // Fetch all page tokens in separate thread
                List<CompletableFuture<List<Message>>> messageFutures = new ArrayList<>();

                // Parse each token page on a separated task
                for (String pageToken : pageTokens) {
                    CompletableFuture<List<Message>> messageFuture = CompletableFuture.supplyAsync(() -> { // Fetch each page emails in different thread
                        try {
                            return fetchMessagesForToken(pageToken);
                        } catch (IOException ex) {
                            throw new RuntimeException("Error fetching emails for token: " + pageToken, ex);
                        }
                    }, executorService);
                    messageFutures.add(messageFuture);
                }

                // Wait for all CompletableFuture instances to complete and collect their results
                return messageFutures.stream()
                        .map(CompletableFuture::join)
                        .collect(Collectors.toList());
            } catch (IOException ex) {
                throw new RuntimeException("Error fetching emails", ex);
            }
        }, executorService).thenApplyAsync(this::processEmails, executorService);
    }

    private List<Message> fetchMessagesForToken(String pageToken) throws IOException{
        ListMessagesResponse response;
        if (pageToken == null) response = gmail.users().messages().list("me").execute();
        else                   response = gmail.users().messages().list("me").setPageToken(pageToken).execute();
        return response.getMessages();

    }

    private List<String> fetchPageTokens() throws IOException {
        List<String> pageTokens = new ArrayList<>();
        String nextPageToken = null;
        do {

            ListMessagesResponse response = gmail.users().messages().list("me")
                    .setPageToken(nextPageToken)
                    .setFields("nextPageToken") // Include only nextToken in response.
                    .execute();

            nextPageToken = response.getNextPageToken();
            pageTokens.add(nextPageToken);

        } while (nextPageToken != null);
        return pageTokens;
    }

    private List<Email> processEmails(List<List<Message>> messageLists) {
        return messageLists.stream()
                .flatMap(List::stream)
                .map(this::constructEmail)
                .collect(Collectors.toList());
    }

    private Email constructEmail(Message message) {
        Email email = new Email();
        String subject = EmailDecoderUtil.decodeHeader(message.getPayload().getHeaders(), "Subject");
        String from = EmailDecoderUtil.decodeHeader(message.getPayload().getHeaders(), "From");

        String body = "";
        if (message.getPayload().getParts() != null) {
            body = EmailDecoderUtil.decodeBody(message.getPayload());
            List<Attachment> attachments = EmailDecoderUtil.extractAttachments(message.getPayload());
            email.setAttachments(attachments);
        }

        email.setSubject(subject);
        email.setFrom(from);
        email.setBody(body);

        logger.info("Email received: {}", email.getFrom());

        return email;
    }
}
