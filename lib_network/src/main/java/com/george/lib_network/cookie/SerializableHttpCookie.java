package com.george.lib_network.cookie;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.HttpCookie;

class SerializableHttpCookie implements Serializable {

    private transient final HttpCookie cookie;
    private transient HttpCookie httpCookie;
    public SerializableHttpCookie(HttpCookie httpCookie) {
        this.cookie = httpCookie;
    }

    public HttpCookie getCookie() {
        HttpCookie bestCookie = cookie;
        if(httpCookie!=null){
            bestCookie = httpCookie;
        }
        return bestCookie;
    }

    private void writeObject(ObjectOutputStream out) throws IOException
    {
        out.writeObject(cookie.getName());
        out.writeObject(cookie.getValue());
        out.writeObject(cookie.getComment());
        out.writeObject(cookie.getCommentURL());
        out.writeObject(cookie.getDomain());
        out.writeLong(cookie.getMaxAge());
        out.writeObject(cookie.getPath());
        out.writeObject(cookie.getPortlist());
        out.writeInt(cookie.getVersion());
        out.writeBoolean(cookie.getSecure());
        out.writeBoolean(cookie.getDiscard());
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException
    {
        String name = (String) in.readObject();
        String value = (String) in.readObject();
        httpCookie = new HttpCookie(name, value);
        httpCookie.setComment((String) in.readObject());
        httpCookie.setCommentURL((String) in.readObject());
        httpCookie.setDomain((String) in.readObject());
        httpCookie.setMaxAge(in.readLong());
        httpCookie.setPath((String) in.readObject());
        httpCookie.setPortlist((String) in.readObject());
        httpCookie.setVersion(in.readInt());
        httpCookie.setSecure(in.readBoolean());
        httpCookie.setDiscard(in.readBoolean());
    }
}
