package com.george.lib_network.cookie;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.util.Log;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.CookieStore;
import java.net.HttpCookie;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class PersistentCookieStore implements CookieStore {
    private static final String LOG_TAG = "PersistentCookieStore";
    private static final String COOKIE_PREFS = "CookiePrefsFiel";
    private static final String COOKIE_NAME_PREFIX = "cookie_";


    private final HashMap<String, ConcurrentHashMap<String, HttpCookie>> cookies;
    private final SharedPreferences cookiePrefs;

    public PersistentCookieStore(Context context) {
        cookiePrefs = context.getSharedPreferences(COOKIE_PREFS, 0);
        cookies = new HashMap<>();

        Map<String, ?> prefsMap = cookiePrefs.getAll();
        for (Map.Entry<String, ?> entry : prefsMap.entrySet()) {
            if (((String) entry.getValue()) != null && !((String) entry.getValue()).startsWith(COOKIE_NAME_PREFIX)) {
                String[] cookieNames = TextUtils.split((String) entry.getValue(), "");
                for (String name : cookieNames) {
                    String encodedCookie = cookiePrefs.getString(COOKIE_NAME_PREFIX + name, null);
                    if (encodedCookie != null) {
                        HttpCookie decodedCookie = decodeCookie(encodedCookie);
                        if (decodedCookie != null) {
                            if (!cookies.containsKey(entry.getKey())) {
                                cookies.put(entry.getKey(), new ConcurrentHashMap<String, HttpCookie>());
                                cookies.get(entry.getKey()).put(name, decodedCookie);
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    public void add(URI uri, HttpCookie httpCookie) {
        String name = getCookieToken(uri, httpCookie);
        if (!httpCookie.hasExpired()) {
            if (!cookies.containsKey(uri.getHost())) {
                cookies.put(uri.getHost(), new ConcurrentHashMap<String, HttpCookie>());
            }
            cookies.get(uri.getHost()).put(name, httpCookie);
        } else {
            if (cookies.containsKey(uri.toString())) {
                cookies.get(uri.getHost()).remove(name);
            }
        }

        SharedPreferences.Editor prefsWrite = cookiePrefs.edit();
        prefsWrite.putString(uri.getHost(), TextUtils.join("", cookies.get(uri.getHost()).keySet()));
        prefsWrite.putString(COOKIE_NAME_PREFIX + name, encodeCookie(new SerializableHttpCookie(httpCookie)));
        prefsWrite.apply();
    }

    @Override
    public List<HttpCookie> get(URI uri) {
        ArrayList<HttpCookie> cookieList = new ArrayList<>();
        if (cookies.containsKey(uri.getHost())) {
            cookieList.addAll(cookies.get(uri.getHost()).values());
        }
        return cookieList;
    }

    @Override
    public List<HttpCookie> getCookies() {
        ArrayList<HttpCookie> cookieList = new ArrayList<>();
        for (String key : cookies.keySet()) {
            cookieList.addAll(cookies.get(key).values());
        }
        return cookieList;
    }

    @Override
    public List<URI> getURIs() {
        ArrayList<URI> uriList = new ArrayList<>();
        for (String key : cookies.keySet()) {
            try {
                uriList.add(new URI(key));
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
        }
        return uriList;
    }

    @Override
    public boolean remove(URI uri, HttpCookie httpCookie) {
        String name = getCookieToken(uri, httpCookie);
        if (cookies.containsKey(uri.getHost()) && cookies.get(uri.getHost()).containsKey(name)) {
            cookies.get(uri.getHost()).remove(name);
            SharedPreferences.Editor prefsWriter = cookiePrefs.edit();
            if (cookiePrefs.contains(COOKIE_NAME_PREFIX + name)) {
                prefsWriter.remove(COOKIE_NAME_PREFIX + name);
            }
            prefsWriter.putString(uri.getHost(), TextUtils.join(",", cookies.get(uri.getHost()).keySet()));
            prefsWriter.apply();
            return true;
        }
        return false;
    }

    @Override
    public boolean removeAll() {
        SharedPreferences.Editor prefsWrite = cookiePrefs.edit();
        prefsWrite.clear();
        prefsWrite.apply();
        cookies.clear();
        return true;
    }

    private String encodeCookie(SerializableHttpCookie cookie) {
        if (cookie == null) {
            return null;
        }
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        try {
            ObjectOutputStream outputStream = new ObjectOutputStream(os);
            outputStream.writeObject(cookie);
        } catch (IOException e) {
            Log.d(LOG_TAG, "IOException in encodeCookie", e);
            return null;
        }

        return byteArrayToHexString(os.toByteArray());
    }

    private String byteArrayToHexString(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length + 2);
        for (byte element : bytes) {
            int v = element & 0xff;
            if (v < 16) {
                sb.append('0');
            }
            sb.append(Integer.toHexString(v));
        }
        return sb.toString().toUpperCase(Locale.US);

    }

    private HttpCookie decodeCookie(String encodedCookie) {
        byte[] bytes = hexStringToByteArray(encodedCookie);
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
        HttpCookie cookie = null;
        try {
            ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream);
            cookie = ((SerializableHttpCookie) objectInputStream.readObject()).getCookie();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return cookie;
    }

    private byte[] hexStringToByteArray(String hexString) {
        int len = hexString.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(hexString.charAt(i), 16) << 4)
                    + Character.digit(hexString.charAt(i + 1), 16));
        }
        return data;
    }

    private String getCookieToken(URI uri, HttpCookie httpCookie) {
        return httpCookie.getName() + httpCookie.getDomain();
    }
}
