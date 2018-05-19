package co.chatsdk.core.utils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import co.chatsdk.core.session.ChatSDK;

/**
 * Created by ben on 5/17/18.
 */

public class FileUtils {

    public static byte [] fileToBytes (File file) {
        int size = (int) file.length();
        byte[] bytes = new byte[size];
        try {
            BufferedInputStream buf = new BufferedInputStream(new FileInputStream(file));
            buf.read(bytes, 0, bytes.length);
            buf.close();
        } catch (IOException e) {
            ChatSDK.logError(e);
        }
        return bytes;
    }

}
