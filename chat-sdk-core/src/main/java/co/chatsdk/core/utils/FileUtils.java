package co.chatsdk.core.utils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * Created by ben on 9/29/17.
 */

public class FileUtils {

//    public static byte[] toByteArray(String filePath) {
//
//        ByteArrayOutputStream out = new ByteArrayOutputStream();
//        BufferedInputStream in = null;
//
//        try {
//            in = new BufferedInputStream(new FileInputStream(new File(filePath)));
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        }
//
//        int read;
//        byte[] buff = new byte[1024];
//
//        try {
//            while ((read = in.read(buff)) > 0)
//            {
//                out.write(buff, 0, read);
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        try {
//            out.flush();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//        return out.toByteArray();
//
//    }

    public static byte [] fileToBytes (File file) {
        int size = (int) file.length();
        byte[] bytes = new byte[size];
        try {
            BufferedInputStream buf = new BufferedInputStream(new FileInputStream(file));
            buf.read(bytes, 0, bytes.length);
            buf.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bytes;
    }

}
