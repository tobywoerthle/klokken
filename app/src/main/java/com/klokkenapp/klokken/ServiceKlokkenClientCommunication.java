package com.klokkenapp.klokken;

import android.os.Binder;

public class ServiceKlokkenClientCommunication extends Binder {

    static ServiceKlokken serviceKlokken;

    public ServiceKlokkenClientCommunication(ServiceKlokken serviceKlokkenIn) {
     serviceKlokken = serviceKlokkenIn;
    }

    ServiceKlokken getService(){
        return serviceKlokken;
    }
}
