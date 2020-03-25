package co.chatsdk.firebase.module;

import org.pmw.tinylog.Logger;

import java.util.ArrayList;
import java.util.List;

import co.chatsdk.core.dao.Keys;
import co.chatsdk.core.session.Config;

public class FirebaseConfig {

    // Firebase
    public String firebaseRootPath = "default";

    public String firebaseDatabaseUrl;
    public String firebaseApp;

    final public List<String> searchIndexes = new ArrayList<>();

    public FirebaseConfig() {
        searchIndexes.add(Keys.Name);
        searchIndexes.add(Keys.Email);
        searchIndexes.add(Keys.Phone);
        searchIndexes.add(Keys.NameLowercase);
    }

    public FirebaseConfig firebase(String rootPath) {

        if (rootPath != null && rootPath.length() > 0 && !rootPath.substring(rootPath.length() - 1).equals('/')) {
            rootPath += "/";
        }

        setFirebaseRootPath(rootPath);

        return this;
    }

    public FirebaseConfig setFirebaseDatabaseURL(String firebaseDatabaseUrl) {
        this.firebaseDatabaseUrl = firebaseDatabaseUrl;
        return this;
    }

    public FirebaseConfig setFirebaseRootPath(String rootPath) {
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

    public FirebaseConfig setFirebaseApp(String firebaseApp) {
        this.firebaseApp = firebaseApp;
        return this;
    }

    public FirebaseConfig setSearchIndexes(List<String> indexes) {
        this.searchIndexes.clear();
        this.searchIndexes.addAll(indexes);
        return this;
    }

}
