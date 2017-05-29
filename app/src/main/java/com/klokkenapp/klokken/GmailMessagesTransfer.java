package com.klokkenapp.klokken;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class GmailMessagesTransfer implements Serializable {

    Map<String, GmailMessage> messageMap = new HashMap<>();

    public GmailMessagesTransfer(Map<String, GmailMessage> messageMap) {
        this.messageMap = messageMap;
    }

    public Map<String, GmailMessage> getMessageMap() {
        return messageMap;
    }

    public void setMessageMap(Map<String, GmailMessage> messageMap) {
        this.messageMap = messageMap;
    }

}