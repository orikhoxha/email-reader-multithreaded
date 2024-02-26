package com.gmail.api.service;

import com.gmail.api.domain.Attachment;
import com.gmail.api.domain.Email;
import com.microsoft.graph.models.Message;
import com.microsoft.graph.requests.GraphServiceClient;
import com.microsoft.graph.requests.MessageCollectionPage;
import com.microsoft.graph.requests.MessageCollectionRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Qualifier("outlookService")
public class OutlookService extends AbstractEmailService<Message> implements EmailService<Message> {

    @Autowired
    private GraphServiceClient graphClient;

    @Override
    protected List<Email> processEmails(List<List<Message>> messageList) {
        return messageList.stream()
                .flatMap(List::stream)
                .map(this::constructEmail)
                .collect(Collectors.toList());
    }

    @Override
    protected Email constructEmail(Message message) {
        Email email = new Email();
        String subject = message.subject;
        String from = message.from.emailAddress.address;

        String body = message.body.content;
        if (message.attachments != null) {
            List<Attachment> attachments = new ArrayList<>();
            for (com.microsoft.graph.models.Attachment attachmentItem : message.attachments.getCurrentPage()) {
                Attachment attachment = new Attachment();
                attachment.setFilename(attachmentItem.name);
                attachments.add(attachment);
            }
            email.setAttachments(attachments);
        }

        email.setSubject(subject);
        email.setFrom(from);
        email.setBody(body);

        logger.info("Email received: {}", email.getFrom());

        return email;
    }

    protected List<Message> fetchMessagesForToken(String pageToken) throws IOException {
        MessageCollectionRequest request = graphClient.me().mailFolders("inbox").messages().buildRequest();
        if (pageToken != null) {
            request = request.skipToken(pageToken);
        }
        MessageCollectionPage response = request.get();
        return response.getCurrentPage();
    }

    protected List<String> fetchPageTokens() throws IOException {
        List<String> pageTokens = new ArrayList<>();
        String nextPageToken = null;
        do {
            MessageCollectionRequest request = graphClient.me().mailFolders("inbox").messages().buildRequest();
            if (nextPageToken != null) {
                request = request.skipToken(nextPageToken);
            }

            request.select("nextPageToken");

            MessageCollectionPage response = request.get();
            nextPageToken = response.getNextPage().toString();
            pageTokens.add(nextPageToken);
        } while(nextPageToken != null);
        return pageTokens;
    }
}
