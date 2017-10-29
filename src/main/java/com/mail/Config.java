package com.mail;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

import com.mail.controller.MailController;
import com.mail.service.BookmarkService;
import com.mail.service.BookmarkServiceImpl;
import com.mail.service.DBSyncService;
import com.mail.service.DBSyncServiceImpl;
import com.mail.service.MailService;
import com.mail.service.MailServiceImpl;
import com.mail.service.MessageService;
import com.mail.service.MessageServiceImpl;
import com.mail.sync.ScheduledTaskRunner;

@Configuration
@ComponentScan(basePackages = { "com.mail" })
@EnableScheduling
public class Config {

    @Value("${elasticsearch.home:C:/Users/Tetiana/Documents/elasticsearch-5.6.3}")
    private String elasticsearchHome;

    private static final Logger LOGGER = LoggerFactory.getLogger(MailController.class);
    private static final int DEFAULT_PORT = 9300;

    @Bean
    public Client client() throws UnknownHostException {
        Settings settings = Settings.settingsBuilder()
                .put("cluster.name", "my-application")
                .put("client.transport.sniff", false)
                .build();
        TransportClient transportClient = TransportClient.builder().settings(settings).build();
        transportClient.addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName("localhost"), DEFAULT_PORT));
        return transportClient;
    }

    @Bean
    public DBSyncService dBSyncService(BookmarkService bookmarkService, MessageService messageService, MailService mailService) {
        return new DBSyncServiceImpl(bookmarkService, messageService, mailService);
    }

    @Bean
    public MailService mailService() {
        return new MailServiceImpl();
    }

    @Bean
    public BookmarkService bookmarkService(Client client) {
        return new BookmarkServiceImpl(client);
    }

    @Bean
    public MessageService messageService(Client client) throws UnknownHostException {
        return new MessageServiceImpl(client);
    }

    @Bean
    public MailController mailController(MessageService messageService) {
        return new MailController(messageService);
    }

    @Bean
    public ScheduledTaskRunner scheduledTaskRunner(BookmarkService bookmarkService, MessageService messageService, MailService mailService) {
        return new ScheduledTaskRunner(dBSyncService(bookmarkService,
                messageService, mailService));
    }

    @Bean
    public DateTimeFormatter dateTimeFormatter() {
        return DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
    }

}
