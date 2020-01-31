package co.chatsdk.core.avatar.gravatar;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

final class Utils {
  private static final StringBuilder sBuilder = new StringBuilder();
  private static final String SPECIAL_CHARS = " %$&+,/:;=?@<>#%";

  private Utils() {
  }

  private static String hex(byte[] array) {
    sBuilder.setLength(0);
    for (byte b : array) {
      sBuilder.append(Integer.toHexString((b & 0xFF) | 0x100).substring(1, 3));
    }
    return sBuilder.toString();
  }

  static String convertEmailToHash(String email) {
    final MessageDigest messageDigest;
    try {
      messageDigest = MessageDigest.getInstance("MD5");
      messageDigest.reset();
      return hex(messageDigest.digest(email.getBytes("UTF-8")));
    } catch (NoSuchAlgorithmException e) {
      return email;
    } catch (UnsupportedEncodingException e) {
      return email;
    }
  }

  static String encode(String input) {
    sBuilder.setLength(0);
    for (char ch : input.toCharArray()) {
      if (isUnsafe(ch)) {
        sBuilder.append('%');
        sBuilder.append(toHex(ch / 16));
        sBuilder.append(toHex(ch % 16));
      } else {
        sBuilder.append(ch);
      }
    }
    return sBuilder.toString();
  }

  private static char toHex(int ch) {
    return (char) (ch < 10 ? '0' + ch : 'A' + ch - 10);
  }

  private static boolean isUnsafe(char ch) {
    return ch > 128 || ch < 0 || SPECIAL_CHARS.indexOf(ch) >= 0;
  }
}
