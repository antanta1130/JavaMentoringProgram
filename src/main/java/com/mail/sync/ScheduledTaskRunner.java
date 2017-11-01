package com.mail.sync;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.mail.service.DoEverythingService;

@Component
public class ScheduledTaskRunner {
    private final DoEverythingService doEverythingService;
    private static final Logger LOGGER = LoggerFactory.getLogger(ScheduledTaskRunner.class);

    @Autowired
    public ScheduledTaskRunner(DoEverythingService doEverythingService) {
        this.doEverythingService = doEverythingService;
    }

    @Scheduled(fixedDelay = 1000000)
    public void sync() {
        LOGGER.info("sync started");
        doEverythingService.sync();
    }
}
