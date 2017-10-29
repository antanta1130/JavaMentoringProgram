package com.mail.service;

import java.io.IOException;
import java.util.List;

import org.joda.time.DateTime;

import com.mail.dto.output.EmailMessageDto;
import com.mail.model.MyMessage;

public interface MessageService {
    int bulkInsert(List<MyMessage> messages) throws IOException;

    List<EmailMessageDto> mailFiltrationByPeriodAndText(DateTime fromDate, DateTime toDate, String textToSearch);
}
