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
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

import com.mail.controller.MailController;
import com.mail.service.DoEverythingService;
import com.mail.sync.ScheduledTaskRunner;

@Configuration
@EnableScheduling
public class Config {

    @Value("${elasticsearch.home:C:/Users/Tetiana/Documents/elasticsearch-2.4.6}")
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
    public DoEverythingService doEverythingService(Client client, DateTimeFormatter dateTimeFormatter) {
        return new DoEverythingService(client, dateTimeFormatter);
    }

    @Bean
    public MailController mailController(DoEverythingService doEverythingService, DateTimeFormatter dateTimeFormatter) {
        return new MailController(doEverythingService, dateTimeFormatter);
    }

    @Bean
    public ScheduledTaskRunner scheduledTaskRunner(DoEverythingService doEverythingService) {
        return new ScheduledTaskRunner(doEverythingService);
    }

    @Bean
    public DateTimeFormatter dateTimeFormatter() {
        return DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
    }

}
