package sdk.chat.firebase.adapter.module;

import java.util.ArrayList;
import java.util.List;

import sdk.chat.core.base.BaseNetworkAdapter;
import sdk.chat.core.dao.Keys;
import sdk.chat.firebase.adapter.FirebaseNetworkAdapter;
import sdk.guru.common.BaseConfig;

public class FirebaseConfig<T> extends BaseConfig<T> {

    // Firebase
    public String firebaseRootPath = "pre_1";

    public String firebaseDatabaseUrl;
    public String firebaseApp;

    public boolean disableClientProfileUpdate = false;
    public boolean developmentModeEnabled = false;
    public boolean disablePublicThreads = false;

    public boolean enableCompatibilityWithV4 = true;

    public boolean enableWebCompatibility = false;

    final public List<String> searchIndexes = new ArrayList<>();

    public Class<? extends BaseNetworkAdapter> networkAdapter = FirebaseNetworkAdapter.class;

    public FirebaseConfig(T onBuild) {
        super(onBuild);

        searchIndexes.add(Keys.Name);
        searchIndexes.add(Keys.Email);
        searchIndexes.add(Keys.Phone);
        searchIndexes.add(Keys.NameLowercase);
    }

    public FirebaseConfig<T> firebase(String rootPath) throws Exception {

        if (rootPath != null && rootPath.length() > 0 && !rootPath.substring(rootPath.length() - 1).equals('/')) {
            rootPath += "/";
        }

        setFirebaseRootPath(rootPath);

        return this;
    }

    /**
     * Set a custom Firebase database URL
     * @param firebaseDatabaseUrl
     * @return
     */
    public FirebaseConfig<T> setFirebaseDatabaseURL(String firebaseDatabaseUrl) {
        this.firebaseDatabaseUrl = firebaseDatabaseUrl;
        return this;
    }

    /**
     * Set the Firebase sandbox. The Firebase database is like a big tree structure.
     * The root path is the root branch of that tree. So if this is set to the default "pre_1"
     * your data would be stored at:
     *
     * root/pre_1/chat-data
     *
     * This allows you to run multiple sand-boxed chat instances on one database.
     *
     * You could have "test", "pre", "prod" etc...
     *
     * @param rootPath
     * @return
     */
    public FirebaseConfig<T> setFirebaseRootPath(String rootPath) throws Exception {
        String path = validatePath(rootPath);
        if (path != null) {
            this.firebaseRootPath = path;
        }
        return this;
    }

    protected String validatePath(String path) throws Exception {
        if (path != null) {
            String validPath = path.replaceAll("[^a-zA-Z0-9_]", "");
            if (!validPath.isEmpty()) {
                if (!validPath.equals(path)) {
                    throw new Exception("The root path cannot contain special characters, they were removed so your new root path is: " + validPath);
                }
                return validPath;
            } else {
                throw new Exception("The root path cannot contain special characters, when removed your root path was empty so the default was used instead");
            }
        } else {
            throw new Exception("The root path provided cannot be null, the default was used instead");
        }
    }

    /**
     * Use a custom Firebase app name
     * @param firebaseApp
     * @return
     */
    public FirebaseConfig<T> setFirebaseApp(String firebaseApp) {
        this.firebaseApp = firebaseApp;
        return this;
    }

    /**
     * Set which search indexes are used. By default we can search for anything
     * within the user/meta path. Make sure to update security rules with indexes you use
     * @param indexes
     * @return
     */
    public FirebaseConfig<T> setSearchIndexes(List<String> indexes) {
        this.searchIndexes.clear();
        this.searchIndexes.addAll(indexes);
        return this;
    }

    /**
     * Prevent the client from setting the Firebase user profile. Useful when
     * the profile is maintained by a server
     * @param disableClientProfileUpdate
     * @return
     */
    public FirebaseConfig<T> setDisableClientProfileUpdate(boolean disableClientProfileUpdate) {
        this.disableClientProfileUpdate = disableClientProfileUpdate;
        return this;
    }

    /**
     * When developer mode is enabled, the app will be more robust if you need to delete data
     * directly from the Firebase console
     * @param developmentModeEnabled
     * @return
     */
    public FirebaseConfig<T> setDevelopmentModeEnabled(boolean developmentModeEnabled) {
        this.developmentModeEnabled = developmentModeEnabled;
        return this;
    }

    /**
     * Disable public chat rooms
     * @param disablePublicThreads
     * @return
     */
    public FirebaseConfig<T> setDisablePublicThreads(boolean disablePublicThreads) {
        this.disablePublicThreads = disablePublicThreads;
        return this;
    }

    /**
     * If you have users who are using Chat SDK v4 this should be set to true
     * @param enabled
     * @return
     */
    public FirebaseConfig<T> setEnableCompatibilityWithV4(boolean enabled) {
        this.enableCompatibilityWithV4 = enabled;
        return this;
    }

    /**
     * If you are using Chat SDK web, set this to true
     * @param enableWebCompatibility
     * @return
     */
    public FirebaseConfig<T> setEnableWebCompatibility(boolean enableWebCompatibility) {
        this.enableWebCompatibility = enableWebCompatibility;
        return this;
    }

    /**
     * Override the Firebase network adapter class
     * @param networkAdapter
     * @return
     */
    public FirebaseConfig<T> setNetworkAdapter(Class<? extends BaseNetworkAdapter> networkAdapter) {
        this.networkAdapter = networkAdapter;
        return this;
    }



}
