package sdk.chat.core.utils;

public class Size {

    public float width;
    public float height;

    public Size(float width, float height) {
        this.width = width;
        this.height = height;
    }

    public Size(float size) {
        this(size, size);
    }

    public int widthInt() {
        return Math.round(width);
    }

    public int heightInt() {
        return Math.round(height);
    }

}