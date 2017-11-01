package com.mail.controller;

import java.util.List;

import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.mail.dto.output.EmailMessageDto;
import com.mail.service.DoEverythingService;

@RestController
public class MailController {

    private final DoEverythingService doEverythingService;
    private final DateTimeFormatter dateTimeFormatter;

    private static final Logger LOGGER = LoggerFactory.getLogger(MailController.class);

    @Autowired
    public MailController(DoEverythingService doEverythingService, DateTimeFormatter dateTimeFormatter) {
        this.doEverythingService = doEverythingService;
        this.dateTimeFormatter = dateTimeFormatter;
    }

    @RequestMapping(value = "/messages/filter", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody ResponseEntity<List<EmailMessageDto>> handleMailFiltration(@RequestParam("fromDate") String fromDate,
            @RequestParam("toDate") String toDate, @RequestParam("searchInput") String searchInput) {
        LOGGER.info("from {} till {}, string to search: {}", fromDate, toDate, searchInput);

        List<EmailMessageDto> emailList = doEverythingService.mailFiltrationByPeriodAndText(dateTimeFormatter.parseDateTime(fromDate),
                dateTimeFormatter.parseDateTime(toDate), searchInput);
        LOGGER.info("response list size: {}", emailList.size());
        return new ResponseEntity<>(emailList, HttpStatus.OK);
    }

}
