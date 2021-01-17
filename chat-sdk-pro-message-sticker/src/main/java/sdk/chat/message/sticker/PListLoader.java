package sdk.chat.message.sticker;

import android.content.Context;

import androidx.annotation.RawRes;

import com.dd.plist.NSArray;
import com.dd.plist.NSDictionary;
import com.dd.plist.NSObject;
import com.dd.plist.NSString;
import com.dd.plist.PropertyListParser;

import java.util.ArrayList;

import sdk.chat.core.utils.StringChecker;

/**
 * Created by ben on 10/11/17.
 */

public class PListLoader {

    public static String FolderName = "drawable";

    public static ArrayList<StickerPack> getStickerPacks(Context context, @RawRes int plist) throws Exception {
        ArrayList<StickerPack> stickerPacks = new ArrayList<>();

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
                    StickerPack pack = new StickerPack(resourceId(context, packImageName));

                    for(String name : stickerImageNames) {
                        pack.addSticker(resourceId(context, name), name);
                    }

                    if(pack.isValid()) {
                        stickerPacks.add(pack);
                    }
                }
            }
        }

        return stickerPacks;
    }

    public static String resourceName (Context context, String name) {
        name = name.replace(".png", "").replace(".gif", "");
        return context.getPackageName() + ":" + FolderName + "/" + name;
    }

    public static int resourceId (Context context, String name) {
        return context.getResources().getIdentifier(resourceName(context, name), null, null);
    }

}
