package com.klokkenapp.klokken;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class GmailMessagesTransfer implements Serializable {

    Map<String, String> messageMap = new HashMap<>();

    public GmailMessagesTransfer(Map<String, String> messageMap) {
        this.messageMap = messageMap;
    }

    public Map<String, String> getMessageMap() {
        return messageMap;
    }

    public void setMessageMap(Map<String, String> messageMap) {
        this.messageMap = messageMap;
    }

}
