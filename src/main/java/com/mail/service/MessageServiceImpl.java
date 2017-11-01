package com.mail.service;

import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mail.dto.output.EmailMessageDto;
import com.mail.model.MyMessage;

@Service
public class MessageServiceImpl implements MessageService {
    private final Client client;
    private final BulkRequestBuilder bulkRequest;
    private final DateTimeFormatter dateTimeFormatter;

    private static final Logger LOGGER = LoggerFactory.getLogger(MessageServiceImpl.class);

    @Autowired
    public MessageServiceImpl(Client client, DateTimeFormatter dateTimeFormatter) {
        this.client = client;
        bulkRequest = client.prepareBulk();
        this.dateTimeFormatter = dateTimeFormatter;
    }

    @Override
    public List<EmailMessageDto> mailFiltrationByPeriodAndText(DateTime fromDate, DateTime toDate, String textToSearch) {
        SearchResponse response = client.prepareSearch("mails")
                .setTypes("messages")
                .addFields("title", "body", "received_date", "from")
                .setPostFilter(QueryBuilders.rangeQuery("received_date")
                        .from(fromDate.getMillis())
                        .to(toDate.getMillis())
                        .includeLower(true)
                        .includeUpper(true))
                .execute()
                .actionGet();

        SearchHit[] searchHits = response.getHits().getHits();
        List<EmailMessageDto> emailMessageDtoList = new ArrayList<>();

        LOGGER.info("Search hits were found in {} messages", searchHits.length);

        for (SearchHit hit : searchHits) {

            String date = new DateTime((long) hit.field("received_date").getValue()).toString(dateTimeFormatter);
            String title = hit.field("title").<String>getValue();
            String body = hit.field("body").<String>getValue();
            String from = hit.field("from").<String>getValue();
            emailMessageDtoList.add(new EmailMessageDto(title, body, date, from));

            LOGGER.debug("title: {}, body: {}, date: {}, from: {} ", title, body, date, from );
        }

        LOGGER.debug("{}", response);
        return emailMessageDtoList;
    }

    @Override
    public int bulkInsert(List<MyMessage> messages) throws IOException {
        LOGGER.info("{} messages were received", messages.size());
        if (messages.size() == 0) {
            return 0;
        }

        for (MyMessage message : messages) {
            bulkRequest.add(client.prepareIndex("mails", "messages", message.getId().toString())
                    .setSource(
                            jsonBuilder()
                                    .startObject()
                                    .field("id", message.getId())
                                    .field("title", message.getTitle())
                                    .field("body", message.getText())
                                    .field("received_date", message.getReceivedDate().getMillis())
                                    .field("from", message.getFrom())
                                    .endObject()));
            LOGGER.info("message: {}", message.toString());
        }

        BulkResponse bulkResponse = bulkRequest.get();
        if (bulkResponse.hasFailures()) {
            LOGGER.error("bulk insert FAILURE: {}", bulkResponse.buildFailureMessage());
            return 1;
        }
        return 0;
    }
}
