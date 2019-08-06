package co.chatsdk.ui.chat.options;

public class MediaType {

    public static byte Choose = 0x1;
    public static int Take = 0x2;
    public static int Photo = 0x4;
    public static int Video = 0x8;

    public static int TakePhoto = Take | Photo;
    public static int ChoosePhoto = Choose | Photo;
    public static int TakeVideo = Take | Video;
    public static int ChooseVideo = Choose | Video;

    int type;

    public MediaType(int type) {
        this.type = type;
    }

    public boolean is (int type) {
        return (this.type & type) > 0;
    }

    public boolean isEqual (int type) {
        return this.type == type;
    }

    public static MediaType takePhoto () {
        return new MediaType(TakePhoto);
    }

    public static MediaType takeVideo () {
        return new MediaType(TakeVideo);
    }

    public static MediaType choosePhoto () {
        return new MediaType(ChoosePhoto);
    }

    public static MediaType chooseVideo () {
        return new MediaType(ChooseVideo);
    }

}
