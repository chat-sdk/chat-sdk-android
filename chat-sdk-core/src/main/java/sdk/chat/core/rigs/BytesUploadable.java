package sdk.chat.core.rigs;

public class BytesUploadable extends Uploadable {

    byte [] bytes;

    public BytesUploadable(byte [] bytes, String name, String mimeType, String messageKey) {
        this(bytes, name, mimeType, messageKey, null);
    }

    public BytesUploadable(byte [] bytes, String name, String mimeType, String messageKey, Compressor compressor) {
        super(name, mimeType, messageKey, compressor);
        this.bytes = bytes;
    }

    @Override
    public byte[] getBytes() {
        return bytes;
    }
}
