package com.mail.sync;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.mail.service.DBSyncService;

@Component
public class ScheduledTaskRunner {
    private final DBSyncService dbSyncService;
    private static final Logger LOGGER = LoggerFactory.getLogger(ScheduledTaskRunner.class);

    @Autowired
    public ScheduledTaskRunner(DBSyncService dbSyncService) {
        this.dbSyncService = dbSyncService;
    }

    @Scheduled(fixedDelay = 1000000)
    public void sync() {
        LOGGER.info("sync started");
        dbSyncService.sync();
    }
}
