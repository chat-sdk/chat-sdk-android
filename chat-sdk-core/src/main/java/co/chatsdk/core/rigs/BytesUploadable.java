package co.chatsdk.core.rigs;

public class BytesUploadable extends Uploadable {

    byte [] bytes;

    public BytesUploadable(byte [] bytes, String name, String mimeType) {
        this(bytes, name, mimeType, null);
    }

    public BytesUploadable(byte [] bytes, String name, String mimeType, Compressor compressor) {
        super(name, mimeType, compressor);
        this.bytes = bytes;
    }

    @Override
    public byte[] getBytes() {
        return bytes;
    }
}
