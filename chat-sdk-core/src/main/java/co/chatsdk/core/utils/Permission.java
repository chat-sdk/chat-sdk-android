package co.chatsdk.core.utils;

import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.PermissionInfo;

public class Permission {

    public String name;
    protected int title;
    protected int description;

    public Permission(String name) {
        this(name, 0, 0);
    }

    public Permission(String name, int title, int description) {
        this.name = name;
        this.title = title;
        this.description = description;
    }

    public String [] permissions () {
        String [] p = {name};
        return p;
    }

    public CharSequence getPermissionLabel(String permission, PackageManager packageManager) {
        try {
            PermissionInfo permissionInfo = packageManager.getPermissionInfo(permission, 0);
            return permissionInfo.loadLabel(packageManager);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }


    public CharSequence getPermissionDescription(String permission, PackageManager packageManager) {
        try {
            PermissionInfo permissionInfo = packageManager.getPermissionInfo(permission, 0);
            return permissionInfo.loadDescription(packageManager);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    public String title (Context context) {
        if (title != 0) {
            return context.getString(title);
        } else {
            return getPermissionLabel(name, context.getPackageManager()) + "";
        }
    }

    public String description (Context context) {
        if (description != 0) {
            return context.getString(description);
        } else {
            return getPermissionDescription(name, context.getPackageManager()) + "";
        }
    }

}
