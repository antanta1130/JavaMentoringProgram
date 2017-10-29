package com.mail.service;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import org.joda.time.DateTime;

public interface BookmarkService {
    DateTime getStartDate();

    void setStartDate(DateTime bookmark) throws IOException, InterruptedException, ExecutionException;
}
