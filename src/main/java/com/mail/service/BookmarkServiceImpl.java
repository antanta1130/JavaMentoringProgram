package com.mail.service;

import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.client.Client;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class BookmarkServiceImpl implements BookmarkService {
    private final Client client;
    private DateTimeFormatter dateTimeFormatter;

    private static final String INDEX = "mails";
    private static final String TYPE = "bookmark";
    private static final String ID = "0";
    private static final String START_DATETIME = "start_datetime";
    private static final Logger LOGGER = LoggerFactory.getLogger(BookmarkServiceImpl.class);

    @Autowired
    public BookmarkServiceImpl(Client client) {
        this.client = client;
    }

    @Override
    public DateTime getStartDate() {
        GetResponse response = client.prepareGet(INDEX, TYPE, ID).get();
        DateTime dt = dateTimeFormatter.parseDateTime(response.getSource().get(START_DATETIME).toString());
        LOGGER.info("get bookmark: {}", dt);
        return dt;
    }

    @Override
    public void setStartDate(DateTime bookmark) throws IOException, InterruptedException, ExecutionException {
        UpdateRequest updateRequest = new UpdateRequest();
        updateRequest.index(INDEX);
        updateRequest.type(TYPE);
        updateRequest.id(ID);
        try {
            updateRequest.doc(jsonBuilder()
                    .startObject()
                    .field(START_DATETIME, bookmark.toString(dateTimeFormatter))
                    .endObject());
        } catch (Exception e) {
            LOGGER.error("fails to set new bookmark: {}", bookmark.toString(dateTimeFormatter));
            LOGGER.error(e.getMessage());
        }
        client.update(updateRequest).get();

        LOGGER.info("set new bookmark: {}", bookmark.toString(dateTimeFormatter));
    }

    @Autowired
    public void setDateTimeFormatter(DateTimeFormatter dateTimeFormatter) {
        this.dateTimeFormatter = dateTimeFormatter;
    }

}
