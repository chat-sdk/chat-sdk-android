package sdk.chat.ui.utils;

public class ImageLoaderPayload {

    public int width = 0;
    public int height = 0;
    public float ar = 1;

    public int placeholder = 0;
    public int error = 0;
    public boolean isAnimated = false;

    public ImageLoaderPayload() {
    }

    public ImageLoaderPayload(int placeholder) {
        this(0, 0, placeholder, 0);
    }

    public ImageLoaderPayload(int width, int height, int placeholder, int error) {
        this(width, height, placeholder, error, false);
    }

    public ImageLoaderPayload(int width, int height, int placeholder, int error, boolean isAnimated) {
        this.width = width;
        this.height = height;
        this.placeholder = placeholder;
        this.error = error;
        this.isAnimated = isAnimated;
    }
}
