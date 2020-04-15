package co.chatsdk.firebase.module;

import org.pmw.tinylog.Logger;

import java.util.ArrayList;
import java.util.List;

import sdk.chat.core.dao.Keys;
import sdk.guru.common.BaseConfig;

public class FirebaseConfig<T> extends BaseConfig<T> {

    // Firebase
    public String firebaseRootPath = "default";

    public String firebaseDatabaseUrl;
    public String firebaseApp;

    public boolean disableClientProfileUpdate = false;
    public boolean developmentModeEnabled = false;
    public boolean disablePublicThreads = false;

    public boolean enableCompatibilityWithV4 = true;

    public boolean enableWebCompatibility = false;

    final public List<String> searchIndexes = new ArrayList<>();

    public FirebaseConfig(T onBuild) {
        super(onBuild);

        searchIndexes.add(Keys.Name);
        searchIndexes.add(Keys.Email);
        searchIndexes.add(Keys.Phone);
        searchIndexes.add(Keys.NameLowercase);
    }

    public FirebaseConfig<T> firebase(String rootPath) {

        if (rootPath != null && rootPath.length() > 0 && !rootPath.substring(rootPath.length() - 1).equals('/')) {
            rootPath += "/";
        }

        setFirebaseRootPath(rootPath);

        return this;
    }

    public FirebaseConfig<T> setFirebaseDatabaseURL(String firebaseDatabaseUrl) {
        this.firebaseDatabaseUrl = firebaseDatabaseUrl;
        return this;
    }

    public FirebaseConfig<T> setFirebaseRootPath(String rootPath) {
        String path = validatePath(rootPath);
        if (path != null) {
            this.firebaseRootPath = path;
        }
        return this;
    }

    protected String validatePath(String path) {
        if (path != null) {
            String validPath = path.replaceAll("[^a-zA-Z0-9_]", "");
            if (!validPath.isEmpty()) {
                if (!validPath.equals(path)) {
                    Logger.warn("The root path cannot contain special characters, they were removed so your new root path is: " + validPath);
                }
                return validPath;
            } else {
                Logger.warn("The root path cannot contain special characters, when removed your root path was empty so the default was used instead");
            }
        } else {
            Logger.warn("The root path provided cannot be null, the default was used instead");
        }
        return null;
    }

    public FirebaseConfig<T> setFirebaseApp(String firebaseApp) {
        this.firebaseApp = firebaseApp;
        return this;
    }

    public FirebaseConfig<T> setSearchIndexes(List<String> indexes) {
        this.searchIndexes.clear();
        this.searchIndexes.addAll(indexes);
        return this;
    }

    public FirebaseConfig<T> setDisableClientProfileUpdate(boolean disableClientProfileUpdate) {
        this.disableClientProfileUpdate = disableClientProfileUpdate;
        return this;
    }

    public FirebaseConfig<T> setDevelopmentModeEnabled(boolean developmentModeEnabled) {
        this.developmentModeEnabled = developmentModeEnabled;
        return this;
    }

    public FirebaseConfig<T> setDisablePublicThreads(boolean disablePublicThreads) {
        this.disablePublicThreads = disablePublicThreads;
        return this;
    }

    public FirebaseConfig<T> setEnableCompatibilityWithV4(boolean enabled) {
        this.enableCompatibilityWithV4 = enabled;
        return this;
    }

    public FirebaseConfig<T> setEnableWebCompatibility(boolean enableWebCompatibility) {
        this.enableWebCompatibility = enableWebCompatibility;
        return this;
    }

}
