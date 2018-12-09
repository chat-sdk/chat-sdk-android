package co.chatsdk.ui.chat;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.File;

public class PhotoProvider extends ContentProvider {

    private static final String CONTENT_PROVIDER_AUTHORITY_SUFFIX = ".provider";

    @Override
    public boolean onCreate() {
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        return null;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        return null;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        return 0;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        return 0;
    }

    public static Uri getPhotoUri(File file, Context context) {
        Uri fileUri = Uri.fromFile(file);
        Uri.Builder builder = new Uri.Builder()
                .authority(getContentProviderAuthority(context))
                .scheme("file")
                .path(fileUri.getPath())
                .query(fileUri.getQuery())
                .fragment(fileUri.getFragment());

        return builder.build();
    }

    private static String getContentProviderAuthority(Context context){
        return context.getPackageName() + CONTENT_PROVIDER_AUTHORITY_SUFFIX;
    }
}
