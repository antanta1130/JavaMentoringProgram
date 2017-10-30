package com.mail.dto.output;

import lombok.Getter;

public class EmailMessageDto {
    @Getter
    final private String title;

    @Getter
    final private String body;

    @Getter
    final private String receivedDate;

    @Getter
    final private String from;

    public EmailMessageDto(String title, String body, String receivedDate, String from) {
        this.title = title;
        this.body = body;
        this.receivedDate = receivedDate;
        this.from = from;
    }
}
