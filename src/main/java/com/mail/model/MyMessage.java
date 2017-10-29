package com.mail.model;

import org.joda.time.DateTime;

public class MyMessage {
    private Integer id;

    private String title;

    private String text;

    private DateTime receivedDate;

    private String from;

    public MyMessage() {
    }

    public Integer getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getText() {
        return text;
    }

    public DateTime getReceivedDate() {
        return receivedDate;
    }

    public String getFrom() {
        return from;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setText(String text) {
        this.text = text;
    }

    public void setReceivedDate(DateTime receivedDate) {
        this.receivedDate = receivedDate;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    @Override
    public String toString() {
        return "Message [title=" + title + ", subject=" + text + ", receivedDate=" + receivedDate + ", from=" + from + "]";
    }

}
