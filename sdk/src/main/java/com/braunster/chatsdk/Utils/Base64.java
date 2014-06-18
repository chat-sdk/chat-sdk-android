package com.braunster.chatsdk.Utils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;

import biz.source_code.base64Coder.Base64Coder;

public class Base64 {

    public static void main(String args[]) throws IOException {
  /*
   * if (args.length != 2) {System.out.println(
   * "Command line parameters: inputFileName outputFileName");
   * System.exit(9); } encodeFile(args[0], args[1]);
   */
        File sourceImage = new File("back3.png");
        File sourceImage64 = new File("back3.txt");
        File destImage = new File("back4.png");
        encodeFile(sourceImage, sourceImage64);
        decodeFile(sourceImage64, destImage);
    }

    public static void encodeFile(File inputFile, File outputFile) throws IOException {
        BufferedInputStream in = null;
        BufferedWriter out = null;
        try {
            in = new BufferedInputStream(new FileInputStream(inputFile));
            out = new BufferedWriter(new FileWriter(outputFile));
            encodeStream(in, out);
            out.flush();
        } finally {
            if (in != null)
                in.close();
            if (out != null)
                out.close();
        }
    }

    public static void encodeStream(InputStream in, BufferedWriter out) throws IOException {
        int lineLength = 72;
        byte[] buf = new byte[lineLength / 4 * 3];
        while (true) {
            int len = in.read(buf);
            if (len == 0)
            break;
            out.write(Base64Coder.encode(buf, 0, len));
            out.newLine();
        }
    }

    static String encodeArray(byte[] in) throws IOException {
        StringBuffer out = new StringBuffer();
        out.append(Base64Coder.encode(in, 0, in.length));
        return out.toString();
    }

    static byte[] decodeArray(String in) throws IOException {
        byte[] buf = Base64Coder.decodeLines(in);
        return buf;
    }

    public static void decodeFile(File inputFile, File outputFile) throws IOException {
        BufferedReader in = null;
        BufferedOutputStream out = null;
        try {
            in = new BufferedReader(new FileReader(inputFile));
            out = new BufferedOutputStream(new FileOutputStream(outputFile));
            decodeStream(in, out);
            out.flush();
        } finally {
            if (in != null)
                in.close();
            if (out != null)
                out.close();
        }
    }

    public static void decodeStream(BufferedReader in, OutputStream out) throws IOException {
        while (true) {
            String s = in.readLine();
            if (s == null)
                break;
            byte[] buf = Base64Coder.decodeLines(s);
            out.write(buf);
        }
    }
}