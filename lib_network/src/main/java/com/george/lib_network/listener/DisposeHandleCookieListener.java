package com.george.lib_network.listener;

import java.util.ArrayList;

public interface DisposeHandleCookieListener extends DisposeDataListener {
    public void onCookie(ArrayList<String> cookieStrList);
}
