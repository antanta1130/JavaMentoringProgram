package com.mail.service;

import java.util.List;

import org.joda.time.DateTime;

import com.mail.model.MyMessage;

public interface MailService {
    List<MyMessage> getMessagesForPeriod(DateTime fromDate, DateTime toDate);
}
