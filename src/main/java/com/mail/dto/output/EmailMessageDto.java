package com.mail.dto.output;

public class EmailMessageDto {
    final private String title;

    final private String text;

    final private String receivedDate;

    final private String from;

    public EmailMessageDto(String title, String text, String receivedDate, String from) {
        this.title = title;
        this.text = text;
        this.receivedDate = receivedDate;
        this.from = from;
    }

    public String getTitle() {
        return title;
    }

    public String getText() {
        return text;
    }

    public String getReceivedDate() {
        return receivedDate;
    }

    public String getFrom() {
        return from;
    }
}
