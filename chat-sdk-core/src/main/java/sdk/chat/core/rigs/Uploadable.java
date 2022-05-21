package sdk.chat.core.rigs;

import androidx.annotation.Nullable;

import java.io.IOException;

import sdk.chat.core.storage.FileManager;
import sdk.chat.core.storage.TransferManager;

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

    @Nullable
    public String hash() {
        return TransferManager.hash(getBytes());
    }



}
