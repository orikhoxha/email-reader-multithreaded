package com.gmail.api.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Email {
    private String id;
    private String threadId;
    private String from;
    private String subject;
    private String body;
    private List<Attachment> attachments;
}
