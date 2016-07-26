package com.braunster.chatsdk.interfaces;

import org.jdeferred.Promise;

/**
 * Created by Erk on 26.07.2016.
 */
public interface BUploadHandler {
    public Promise uploadFile(byte[] data, String name, String mimeType);
}
