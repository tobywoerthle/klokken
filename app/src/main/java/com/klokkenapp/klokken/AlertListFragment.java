package com.klokkenapp.klokken;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.lang.reflect.Field;

public class AlertListFragment extends MainActivityFragments {

    private GmailMessage gmailMessage;

    private static View mainView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.alertlist_view, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mainView = getView();

        TextView alertListTimeValueText = (TextView) mainView.findViewById(R.id.alertListTimeValue);
        TextView alertListTimeFromText = (TextView) mainView.findViewById(R.id.alertListFromValue);
        TextView alertListTimeSubjectText = (TextView) mainView.findViewById(R.id.alertListSubjectValue);

        alertListTimeValueText.setText(gmailMessage.getMessageReceived());
        alertListTimeFromText.setText(gmailMessage.getMessageFrom());
        alertListTimeSubjectText.setText(gmailMessage.getMessageSubject());
    }

    public void setGmailMessage(GmailMessage inGmailMessage){
        gmailMessage = inGmailMessage;
    }

}