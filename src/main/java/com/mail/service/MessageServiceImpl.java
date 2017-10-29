package com.mail.service;

import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;

import java.io.IOException;
import java.util.List;

import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.index.query.QueryBuilders;
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
    private DateTimeFormatter dateTimeFormatter;

    private static final Logger LOGGER = LoggerFactory.getLogger(MessageServiceImpl.class);

    @Autowired
    public MessageServiceImpl(Client client) {
        this.client = client;
        bulkRequest = client.prepareBulk();
    }

    @Override
    public List<EmailMessageDto> mailFiltrationByPeriodAndText(DateTime fromDate, DateTime toDate, String textToSearch) {
        SearchResponse response = client.prepareSearch("mails")
                .setTypes("messages")
                .addFields("title", "body")
                .setPostFilter(QueryBuilders.rangeQuery("received_date")
                        .from(fromDate.toString(dateTimeFormatter))
                        .to(toDate.toString(dateTimeFormatter))
                        .includeLower(true)
                        .includeUpper(true))
                .execute()
                .actionGet();

        // SearchHit[] searchHits = response.getHits().getHits();
        LOGGER.info("{}", response);
        return null;
    }

    @Override
    public int bulkInsert(List<MyMessage> messages) throws IOException {
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
                                    .field("received_date", message.getReceivedDate().toString(dateTimeFormatter))
                                    .field("from", message.getFrom())
                                    .endObject()));
        }

        BulkResponse bulkResponse = bulkRequest.get();
        if (bulkResponse.hasFailures()) {
            LOGGER.error("bulk insert FAILURE");
            return 1;
        }
        return 0;
    }

    @Autowired
    public void setDateTimeFormatter(DateTimeFormatter dateTimeFormatter) {
        this.dateTimeFormatter = dateTimeFormatter;
    }
}