package com.mail.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import javax.mail.BodyPart;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Store;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.mail.model.MyMessage;

@Service
public class MailServiceImpl implements MailService {
    private Folder inbox;
    private Store store;
    private final String user = "testmailserver0000@gmail.com";
    private final String pass = "qwerty11!";
    private final String host = "imap.gmail.com";
    private final String mailStoreType = "imaps";

    private static final Logger LOGGER = LoggerFactory.getLogger(MailServiceImpl.class);

    @Override
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
