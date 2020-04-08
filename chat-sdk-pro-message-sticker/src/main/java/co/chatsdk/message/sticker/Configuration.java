package co.chatsdk.message.sticker;

import android.content.Context;

import com.dd.plist.NSArray;
import com.dd.plist.NSDictionary;
import com.dd.plist.NSObject;
import com.dd.plist.NSString;
import com.dd.plist.PropertyListParser;

import java.util.ArrayList;

import co.chatsdk.core.utils.StringChecker;

import static co.chatsdk.message.sticker.R.raw.stickers;

/**
 * Created by ben on 10/11/17.
 */

public class Configuration {

    public static String FolderName = "drawable";

    public static ArrayList<StickerPack> getStickerPacks (Context context) throws Exception {
        ArrayList<StickerPack> stickerPacks = new ArrayList<>();

        NSObject object = PropertyListParser.parse(context.getResources().openRawResource(stickers));
        if(object instanceof NSArray) {
            NSArray packs = (NSArray) object;

            for(int i = 0; i < packs.count(); i++) {
                if(packs.objectAtIndex(i) instanceof NSDictionary) {
                    NSDictionary pack = (NSDictionary) packs.objectAtIndex(i);

                    String packImageName = null;
                    ArrayList<String> stickerImageNames = new ArrayList<>();

                    // Parse the pack
                    NSObject iconNSObject = pack.get("icon");
                    if(iconNSObject instanceof NSString) {
                        NSString iconNSString = (NSString) iconNSObject;
                        packImageName = iconNSString.getContent();
                    }

                    NSObject stickersNSObject = pack.get("stickers");

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
                    StickerPack stickerPack = new StickerPack();
                    stickerPack.imageResourceId = resourceId(context, packImageName);

                    for(String name : stickerImageNames) {
                        Sticker sticker = new Sticker();
                        sticker.imageResourceId = resourceId(context, name);
                        sticker.imageName = name;
                        stickerPack.addSticker(sticker);
                    }

                    if(stickerPack.isValid()) {
                        stickerPacks.add(stickerPack);
                    }
                }
            }
        }

        return stickerPacks;
    }

    public static String resourceName (Context context, String name) {
        return context.getPackageName() + ":" + FolderName + "/" + name.replace(".png", "");
    }

    public static int resourceId (Context context, String name) {
        return context.getResources().getIdentifier(resourceName(context, name), null, null);
    }

}
