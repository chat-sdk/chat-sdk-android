package co.chatsdk.core.rigs;

import android.graphics.Bitmap;

import java.io.File;
import java.io.IOException;

import co.chatsdk.core.utils.FileUtils;
import io.reactivex.Completable;

public abstract class Uploadable {

    public String name;
    public String mimeType;
    public Compressor compressor;

    public interface Compressor {
        Uploadable compress (Uploadable uploadable) throws IOException;
    }

    public Uploadable(String name, String mimeType) {
        this(name, mimeType, null);
    }

    public Uploadable(String name, String mimeType, Compressor compressor) {
        this.name = name;
        this.mimeType = mimeType;
        this.compressor = compressor;
    }

    public Uploadable compress () throws IOException {
        if (compressor != null) {
            return compressor.compress(this);
        }
        else return this;
    }

    public abstract byte [] getBytes();

}
