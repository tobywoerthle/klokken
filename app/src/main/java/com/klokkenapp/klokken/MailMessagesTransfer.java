package com.klokkenapp.klokken;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class MailMessagesTransfer implements Serializable {

    private Map<String, MailMessage> messageMap = new HashMap<>();

    public MailMessagesTransfer(Map<String, MailMessage> messageMap) {
        this.messageMap = messageMap;
    }

    public Map<String, MailMessage> getMessageMap() {
        return messageMap;
    }

    public void setMessageMap(Map<String, MailMessage> messageMap) {
        this.messageMap = messageMap;
    }
}
