package com.mail.service;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mail.model.MyMessage;

@Service
public class DBSyncServiceImpl implements DBSyncService {
    private final BookmarkService bookmarkService;
    private final MessageService messageService;
    private final MailService mailService;
    private DateTimeFormatter dateTimeFormatter;

    private static final Logger LOGGER = LoggerFactory.getLogger(DBSyncServiceImpl.class);

    @Autowired
    public DBSyncServiceImpl(BookmarkService bookmarkService, MessageService messageService, MailService mailService) {
        this.bookmarkService = bookmarkService;
        this.messageService = messageService;
        this.mailService = mailService;
    }

    @Override
    public void sync() {
        DateTime startBookmark = bookmarkService.getStartDate();
        DateTime endBookmark = DateTime.now();
        List<MyMessage> mails = mailService.getMessagesForPeriod(startBookmark, endBookmark);

        LOGGER.info("Started mail messages prosessing for period from {} to {}", startBookmark.toString(dateTimeFormatter), endBookmark.toString(dateTimeFormatter));

        try {
            messageService.bulkInsert(mails);
            bookmarkService.setStartDate(endBookmark);
        } catch (IOException | InterruptedException | ExecutionException e) {
            LOGGER.error(e.getMessage());
        }
    }

    @Autowired
    public void setDateTimeFormatter(DateTimeFormatter dateTimeFormatter) {
        this.dateTimeFormatter = dateTimeFormatter;
    }

}
