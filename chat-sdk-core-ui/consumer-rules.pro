# Glide
-keep public class * implements com.bumptech.glide.module.GlideModule
-keep class * extends com.bumptech.glide.module.AppGlideModule {
 <init>(...);
}
-keep public enum com.bumptech.glide.load.ImageHeaderParser$** {
  **[] $VALUES;
  public *;
}

-keep class com.bumptech.glide.load.data.ParcelFileDescriptorRewinder$InternalRewinder {
  *** rewind();
}

# Image Cropper
-keep class androidx.appcompat.widget.** { *; }

# matisse
-dontwarn com.bumptech.glide.**

# Pretty time
-keep class org.ocpsoft.prettytime.i18n.**

# ChatKit

-keep class * extends com.stfalcon.chatkit.messages.MessageHolders$OutcomingTextMessageViewHolder {
     public <init>(android.view.View, java.lang.Object);
     public <init>(android.view.View);
 }
-keep class * extends com.stfalcon.chatkit.messages.MessageHolders$IncomingTextMessageViewHolder {
     public <init>(android.view.View, java.lang.Object);
     public <init>(android.view.View);
 }
-keep class * extends com.stfalcon.chatkit.messages.MessageHolders$IncomingImageMessageViewHolder {
     public <init>(android.view.View, java.lang.Object);
     public <init>(android.view.View);
 }
-keep class * extends com.stfalcon.chatkit.messages.MessageHolders$OutcomingImageMessageViewHolder {
     public <init>(android.view.View, java.lang.Object);
     public <init>(android.view.View);
 }

 # shameImageView

# -dontwarn android.support.v7.**
# -keep class android.support.v7.** { ; }
# -keep interface android.support.v7.* { ; }
 -keepattributes *Annotation,Signature
 -dontwarn com.github.siyamed.**
 -keep class com.github.siyamed.shapeimageview.**{ *; }

 # Iconics
 -keep class **.R$* {
     <fields>;
 }
