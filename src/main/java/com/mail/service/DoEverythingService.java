package com.mail.service;

import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutionException;

import javax.mail.BodyPart;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Store;

import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.update.UpdateRequest;
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
public class DoEverythingService {
    private final Client client;
    private final DateTimeFormatter dateTimeFormatter;
    private final BulkRequestBuilder bulkRequest;

    private Folder inbox;
    private Store store;
    private final String user = "testmailserver0000@gmail.com";
    private final String pass = "qwerty11!";
    private final String host = "imap.gmail.com";
    private final String mailStoreType = "imaps";

    private static final String INDEX = "mails";
    private static final String TYPE = "bookmark";
    private static final String ID = "0";
    private static final String START_DATETIME = "start_datetime";
    private static final Logger LOGGER = LoggerFactory.getLogger(DoEverythingService.class);

    @Autowired
    public DoEverythingService(Client client, DateTimeFormatter dateTimeFormatter) {
        this.client = client;
        bulkRequest = client.prepareBulk();
        this.dateTimeFormatter = dateTimeFormatter;
    }

    public void sync() {
        DateTime startBookmark = getStartDate();
        DateTime endBookmark = DateTime.now();
        List<MyMessage> mails = getMessagesForPeriod(startBookmark, endBookmark);

        LOGGER.info("Started mail messages prosessing for period from {} to {}", startBookmark.toString(dateTimeFormatter), endBookmark.toString(dateTimeFormatter));

        try {
            bulkInsert(mails);
            setStartDate(endBookmark);
        } catch (IOException | InterruptedException | ExecutionException e) {
            LOGGER.error(e.getMessage());
        }
    }

    public DateTime getStartDate() {
        GetResponse response = client.prepareGet(INDEX, TYPE, ID).get();
        DateTime dt = dateTimeFormatter.parseDateTime(response.getSource().get(START_DATETIME).toString());
        LOGGER.info("get bookmark: {}", dt.toString(dateTimeFormatter));
        return dt;
    }

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
            LOGGER.error("Failed to set new bookmark: {}", bookmark.toString(dateTimeFormatter));
            LOGGER.error(e.getMessage());
        }
        client.update(updateRequest).get();

        LOGGER.info("set bookmark: {}", bookmark.toString(dateTimeFormatter));
    }

    public List<MyMessage> getMessagesForPeriod(DateTime fromDate, DateTime toDate) {
        List<MyMessage> resultMessageList = new ArrayList<>();
        try {
            connect();
            Message[] messages = inbox.getMessages();
            for (int i = 0; i < messages.length; i++) {
                Message message = messages[i];
                DateTime receivedDate = new DateTime(message.getReceivedDate());
                if (receivedDate.isAfter(fromDate) && receivedDate.isBefore(toDate)) {
                    MyMessage shortMessage = new MyMessage();
                    shortMessage.setTitle(message.getSubject());
                    shortMessage.setFrom(message.getFrom()[0].toString());
                    shortMessage.setReceivedDate(receivedDate);
                    Multipart mp = (Multipart) message.getContent();
                    BodyPart bp = mp.getBodyPart(0);
                    shortMessage.setText(bp.getContent().toString());
                    shortMessage.setId(getMailId(message.getSubject(), bp.getContent().toString(), message.getReceivedDate(), message.getFrom()[0].toString()));

                    resultMessageList.add(shortMessage);
                }
            }
            disconnect();
        } catch (MessagingException | IOException e) {
            LOGGER.error(e.getMessage());
        }
        return resultMessageList;

    }

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

            LOGGER.debug("title: {}, body: {}, date: {}, from: {} ", title, body, date, from);
        }

        LOGGER.debug("{}", response);
        return emailMessageDtoList;
    }

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

    private int getMailId(String title, String text, Date receivedDate, String from) {
        return (receivedDate.getTime() + title + text + from).hashCode();
    }

    private void connect() throws MessagingException {
        Properties props = new Properties();
        props.put("mail.store.protocol", mailStoreType);
        Session session = Session.getInstance(props);

        store = session.getStore();
        store.connect(host, user, pass);

        inbox = store.getFolder("INBOX");
        inbox.open(Folder.READ_ONLY);

    }

    private void disconnect() throws MessagingException {
        inbox.close(false);
        store.close();

    }

}
