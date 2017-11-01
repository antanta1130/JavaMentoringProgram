package com.mail.dto.output;

public class EmailMessageDto {
    final private String title;

    final private String body;

    final private String receivedDate;

    final private String from;

    public EmailMessageDto(String title, String body, String receivedDate, String from) {
        this.title = title;
        this.body = body;
        this.receivedDate = receivedDate;
        this.from = from;
    }

    public String getTitle() {
        return title;
    }

    public String getBody() {
        return body;
    }

    public String getReceivedDate() {
        return receivedDate;
    }

    public String getFrom() {
        return from;
    }

}
