package sdk.chat.core.rigs;

import android.util.Base64;

import androidx.annotation.Nullable;

import com.google.gson.Gson;

import java.io.IOException;
import java.security.MessageDigest;
import java.util.HashMap;

import sdk.chat.core.storage.FileManager;

public abstract class Uploadable {

    public static String mimeTypeKey = "mime";
    public static String nameKey = "name";
    public static String pathKey = "path";

    public String name;
    public String mimeType;
    public String messageKey;
    public Compressor compressor;
    public boolean reportProgress = true;

    public interface Compressor {
        Uploadable compress (Uploadable uploadable) throws IOException;
    }

    public Uploadable(String name, String mimeType, String messageKey) {
        this(name, mimeType, messageKey, null);
    }

    public Uploadable(String name, String mimeType, String messageKey, Compressor compressor) {
        this.name = name;
        this.mimeType = mimeType;
        this.messageKey = messageKey;
        this.compressor = compressor;
    }

    public Uploadable compress () throws IOException {
        if (compressor != null) {
            return compressor.compress(this);
        }
        else return this;
    }

    public abstract byte [] getBytes();

    public Uploadable setReportProgress(boolean reportProgress) {
        this.reportProgress = reportProgress;
        return this;
    }

    public String cache() {
        return FileManager.saveToFile(hash(), getBytes());
    }

    public String serialize() {
        Gson gson = new Gson();
        HashMap<String,String> values = new HashMap<String, String>() {{
            put(nameKey, name);
            put(mimeTypeKey, mimeType);
            put(pathKey, cache());
        }};
        return gson.toJson(values);
    }

    @Nullable
    public String hash() {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(getBytes());
            return Base64.encodeToString(hash, Base64.DEFAULT);
        } catch (Exception e) {
            return null;
        }
    }

}
