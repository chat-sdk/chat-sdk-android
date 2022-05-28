package sdk.chat.message.sticker.provider;

import android.content.ContentResolver;
import android.content.Context;
import android.content.res.Resources;
import android.net.Uri;

import androidx.annotation.RawRes;

import com.dd.plist.NSArray;
import com.dd.plist.NSDictionary;
import com.dd.plist.NSObject;
import com.dd.plist.NSString;
import com.dd.plist.PropertyListParser;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Single;
import sdk.chat.core.session.ChatSDK;
import sdk.chat.core.utils.StringChecker;
import sdk.chat.message.sticker.StickerPack;
import sdk.chat.message.sticker.module.StickerMessageModule;

public class PListStickerPackProvider implements StickerPackProvider {

    public static String FolderName = "drawable";

    protected Context context;
    protected @RawRes int plist;

    protected List<StickerPack> stickerPacks;

    public PListStickerPackProvider() {
        this.context = ChatSDK.ctx();
        this.plist = StickerMessageModule.config().plist;
    }

    public String resourceName(Context context, String name) {
        name = name.replace(".png", "").replace(".gif", "");
        return context.getPackageName() + ":" + FolderName + "/" + name;
    }

    public int resourceId (String name) {
        return context.getResources().getIdentifier(resourceName(context, name), null, null);
    }

    public String imageURI(Context context, String name) {
        Resources resources = context.getResources();
        int resID = resourceId(name);
        Uri uri = new Uri.Builder()
                .scheme(ContentResolver.SCHEME_ANDROID_RESOURCE)
                .authority(resources.getResourcePackageName(resID))
                .appendPath(resources.getResourceTypeName(resID))
                .appendPath(resources.getResourceEntryName(resID))
                .build();
        return uri.toString();
    }

    @Override
    public void preload() {
        getPacks().subscribe();
    }

    @Override
    public Single<List<StickerPack>> getPacks() {
        return Single.create(emitter -> {

            if (stickerPacks != null) {
                emitter.onSuccess(stickerPacks);
                return;
            }

            stickerPacks = new ArrayList<>();

            NSObject object = PropertyListParser.parse(context.getResources().openRawResource(plist));
            if(object instanceof NSArray) {
                NSArray packs = (NSArray) object;

                for(int i = 0; i < packs.count(); i++) {
                    if(packs.objectAtIndex(i) instanceof NSDictionary) {
                        NSDictionary data = (NSDictionary) packs.objectAtIndex(i);

                        String packImageName = null;
                        ArrayList<String> stickerImageNames = new ArrayList<>();

                        // Parse the pack
                        NSObject iconNSObject = data.get("icon");
                        if(iconNSObject instanceof NSString) {
                            NSString iconNSString = (NSString) iconNSObject;
                            packImageName = iconNSString.getContent();
                        }

                        NSObject stickersNSObject = data.get("stickers");

                        if(stickersNSObject instanceof NSArray) {
                            NSArray stickersArray = (NSArray) stickersNSObject;
                            for(int j = 0; j < stickersArray.count(); j++) {
                                NSObject imageNameNSObject = stickersArray.objectAtIndex(j);
                                if(imageNameNSObject instanceof NSString) {
                                    NSString imageNameNSString = (NSString) imageNameNSObject;
                                    if(!StringChecker.isNullOrEmpty(imageNameNSString.getContent())) {
                                        stickerImageNames.add(imageNameNSString.getContent());
                                    }
                                }
                            }
                        }

                        if(StringChecker.isNullOrEmpty(packImageName) || stickerImageNames.size() == 0) {
                            continue;
                        }

                        // Create the sticker pack
                        StickerPack pack = new StickerPack(imageURI(context, packImageName));

                        for(String name : stickerImageNames) {
                            pack.addSticker(imageURI(context, name), name);
                        }

                        if(pack.isValid()) {
                            stickerPacks.add(pack);
                        }
                    }
                }
            }

            emitter.onSuccess(stickerPacks);

        });
    }

    @Override
    public String imageURL(String name) {
        Resources resources = context.getResources();

        // Do this because otherwise we get a crash because the message
        // holder tries to update before it's ready
        if (!StringChecker.isNullOrEmpty(name)) {
            int resID = resourceId(name);
            Uri uri = new Uri.Builder()
                    .scheme(ContentResolver.SCHEME_ANDROID_RESOURCE)
                    .authority(resources.getResourcePackageName(resID))
                    .appendPath(resources.getResourceTypeName(resID))
                    .appendPath(resources.getResourceEntryName(resID))
                    .build();

            return uri.toString();
        }
        return null;
    }
}
