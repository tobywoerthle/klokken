package com.klokkenapp.klokken;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.lang.reflect.Field;

public class AlertListFragment extends MainActivityFragments {

    private MailMessage mailMessage;

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

        if(mailMessage != null){
            TextView alertListTimeValueText = (TextView) mainView.findViewById(R.id.alertListTimeValue);
            TextView alertListTimeFromText = (TextView) mainView.findViewById(R.id.alertListFromValue);
            TextView alertListTimeSubjectText = (TextView) mainView.findViewById(R.id.alertListSubjectValue);

            alertListTimeValueText.setText(mailMessage.getMessageReceived());
            alertListTimeFromText.setText(mailMessage.getMessageFrom());
            alertListTimeSubjectText.setText(mailMessage.getMessageSubject());
        }
    }

    public void setMailMessage(MailMessage inMailMessage){

        mailMessage = inMailMessage;
    }

}