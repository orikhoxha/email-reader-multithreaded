package com.gmail.api.util;

import com.gmail.api.domain.Attachment;
import com.google.api.services.gmail.model.MessagePart;
import com.google.api.services.gmail.model.MessagePartHeader;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

public final class EmailDecoderUtil {

    public static String decodeHeader(List<MessagePartHeader> headers, String name) {
        for (MessagePartHeader header : headers) {
            if (name.equals(header.getName())) return header.getValue();
        }
        return "";
    }

    public static String decodeBody(MessagePart payload) {
        if (payload.getBody().getData() != null) {
            byte[] decodeBytes = decodeBase64(payload.getBody().getData());
            return new String(decodeBytes, StandardCharsets.UTF_8);
        } else if (payload.getParts() != null) {
            StringBuilder body = new StringBuilder();
            for (MessagePart part : payload.getParts()) {
                if (part.getFilename() == null) { // Ignore parts with filename (treat as attachments)
                    body.append(decodeBody(part));
                }
            }
            return body.toString();
        }
        return "";
    }

    public static List<Attachment> extractAttachments(MessagePart payload) {
        List<Attachment> attachments = new ArrayList<>();
        if (payload.getParts() != null) {
            for (MessagePart part : payload.getParts()) {
                if (part.getFilename() != null && part.getBody() != null && part.getBody().getAttachmentId() != null) {
                    Attachment attachment = new Attachment();
                    attachment.setFilename(part.getFilename());
                    attachment.setMimeType(part.getMimeType());
                    attachment.setData(decodeBase64(part.getBody().getData()));
                    attachments.add(attachment);
                }
            }
        }
        return attachments;
    }

    private static byte[] decodeBase64(String base64Data) {
        return Base64.getUrlDecoder().decode(base64Data);
    }
}
