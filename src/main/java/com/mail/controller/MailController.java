package com.mail.controller;

import java.util.List;

import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.mail.dto.output.EmailMessageDto;
import com.mail.service.MessageService;

@RestController
public class MailController {

    private final MessageService mailService;
    private DateTimeFormatter dateTimeFormatter;

    private static final Logger LOGGER = LoggerFactory.getLogger(MailController.class);

    @Autowired
    public MailController(MessageService mailService) {
        this.mailService = mailService;
    }

    @RequestMapping(value = "/messages/filter", method = RequestMethod.GET, produces = "application/json; charset=UTF-8")
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<List<EmailMessageDto>> handleMailFiltration(@RequestParam("fromDate") String fromDate,
            @RequestParam("toDate") String toDate, @RequestParam("searchInput") String searchInput) {
        LOGGER.info("from {}, till {}, string to search: {}", fromDate, toDate, searchInput);

        List<EmailMessageDto> emailList = mailService.mailFiltrationByPeriodAndText(dateTimeFormatter.parseDateTime(fromDate + " 00:00:00"),
                dateTimeFormatter.parseDateTime(toDate + " 23:59:59"), searchInput);
        return new ResponseEntity<>(emailList, HttpStatus.OK);
    }

    @Autowired
    public void setDateTimeFormatter(DateTimeFormatter dateTimeFormatter) {
        this.dateTimeFormatter = dateTimeFormatter;
    }

}
