## Creating a new Project with a Firebase UI Login and with Chat SDK pre-integrated
###### Quick start guide - it takes about 15 minutes! This instruction manual assumes that you are a beginner at using Android Studio and assumes that you want to create a blank Android Studio project with a Firebase UI Login mechanism pre-integrated, and subsequently also integrate Chat SDK into it as well. If you are an advanced user or if you want to add the Chat SDK to an existing project, please use the manual here; NEED LINK!

1. If you do not already have a project at hand, then in Android Studio, Go to **File** -> **New** -> **Project**.

2. Click on the **Phone and Tablet** tab. Click on the **Add Empty Activity** tab and click **Next**. Enter a name for the **Application** as you see fit. Change the **Name** of the application as you see fit. Be sure to note the **Application name** and the **Package name**. You will need this information later. NEED MINIMUM API LEVEL. Check the box of the Android X option, then click **Finish**.

3. If you already have a project on hand, be sure to have an empty class an empty layout file bound to it. For the purposes of this tutorial these will be called `MainActivity.java` and `activity_main.xml` respectively.

4. Open the top level `build.gradle` file. You can do this by clicking on the vertical **Project** tab in the upper left hand corner, then clicking on the horizontal **Project** option in the drop down menu beside it. ![Project and Project](C:\Users\Alpha\AndroidStudioProjects\Chat SDK Manuals\Manual-3\chat-sdk-android\Manual-3\Project%20and%20Project.png)

5. Click on the folder with the **name of your App**, then click on the ``build.gradle`` file. When you open it, the tab should have the name of your App. That’s how you know it’s the **project level** `build.gradle` file. It should have the name of your App when you open it. ![Top Level Build Gradle File](C:\Users\Alpha\AndroidStudioProjects\Chat SDK Manuals\Manual-3\chat-sdk-android\Manual-3\Top%20Level%20Build%20Gradle%20File.png)

6. Find the section of `dependencies` and add this code inside of it:

   ```classpath 'com.google.gms:google-services:4.2.0'```

   Move your mouse over that line lines slowly, if android studio tells you that the version is outdated, enter the number of the latest version in place of the 4.2.0.

7. In the same file, find the section of `repositories` in `allprojects`, and add the following code inside of it:

   ```
    maven { url "http://dl.bintray.com/chat-sdk/chat-sdk-android" }
    maven { url "https://maven.google.com" }
    maven { url "https://jitpack.io" }
   ```

   The result should look like this:

   ```
    allprojects {
      repositories {
        google()
        jcenter()
        maven { url "http://dl.bintray.com/chat-sdk/chat-sdk-android" }
        maven { url "https://maven.google.com" }
        maven { url "https://jitpack.io" }
     }
    }
   ```

8. Now go to your app level `build.gradle` file. Click on the **app** folder above the `build.gradle` file on the right, and then open the `build.gradle` file in it. The file should have the title "app" when you open it. This is called the **App Level** `build.gradle` file.

9. Add this code in the `dependencies` section:

   ```implementation 'com.firebaseui:firebase-ui-auth:4.2.1'```

   Move your mouse over that line lines slowly, if android studio tells you that the version is outdated, enter the number of the latest version in place of the 4.2.1.

10. Find the following lines of code in the `dependencies` section, you can skip this step if your project does not implement Android X. 
   ```
    implementation 'androidx.appcompat:appcompat:1.0.0-beta01'
    androidTestImplementation 'androidx.test:runner:1.1.0-alpha4'
    androidTestImplementation 'androidx.test.espresso:espresso-core:3.1.0-alpha4'
   ```
   Move your mouse over them and update them to their most recent versions if needs be. These currently are:
   ```
    implementation 'androidx.appcompat:appcompat:1.1.0-alpha02'
    androidTestImplementation 'androidx.test:runner:1.1.2-alpha02'
    androidTestImplementation 'androidx.test.espresso:espresso-core:3.1.2-alpha01'
   ```

11. Add this section of code to the `dependencies` section as well:
   ```
   implementation "com.github.chat-sdk.chat-sdk-android:chat-sdk-ui:$project.CHAT_SDK_VERSION"
   implementation "com.github.chat-sdk.chat-sdk-android:chat-sdk-firebase-adapter:$project.CHAT_SDK_VERSION"
   implementation "com.github.chat-sdk.chat-sdk-android:chat-sdk-firebase-push:$project.CHAT_SDK_VERSION"
   implementation "com.github.chat-sdk.chat-sdk-android:chat-sdk-firebase-file-storage:$project.CHAT_SDK_VERSION"
   implementation "com.github.chat-sdk.chat-sdk-android:chat-sdk-firebase-ui:$project.CHAT_SDK_VERSION"
   implementation "com.github.chat-sdk.chat-sdk-android:chat-sdk-firebase-social-login:$project.CHAT_SDK_VERSION"```
   ```

11. Add the following code to the **App Level** `build.gradle file`, in the section  `dependencies`:

       ```
     implementation 'co.chatsdk.chatsdk:chat-sdk-firebase-adapter:4.6.0'
     implementation 'co.chatsdk.chatsdk:chat-sdk-firebase-file-storage:4.6.0'
     implementation 'co.chatsdk.chatsdk:chat-sdk-core:4.6.0'
     implementation 'co.chatsdk.chatsdk:chat-sdk-firebase-push:4.6.0'
     implementation 'co.chatsdk.chatsdk:chat-sdk-firebase-ui:4.6.0'
       ```

12. Move your mouse over these lines slowly, if android studio tells you that these versions are outdated, enter the number of the latest version in the appropriate line in place of the number of the latest version.

13. Find the `android {    }` section of the file. Add this code inside of it, but not inside any of the other items inside of it:

       ```
        compileOptions {
         sourceCompatibility JavaVersion.VERSION_1_8
         targetCompatibility JavaVersion.VERSION_1_8
        }
       ```

       It should then look like this :

       ```
        android {
         android {
          compileSdkVersion 27
          defaultConfig {
            applicationId "domain.testing.testapp2"
            minSdkVersion 21
            targetSdkVersion 27
            versionCode 1
            versionName "1.0"
            testInstrumentationRunner      "android.support.test.runner.AndroidJUnitRunner"
        }
        buildTypes {
            release {
                minifyEnabled false
                proguardFiles getDefaultProguardFile('proguard-    android.txt'), 'proguard-rules.pro'
            }
        }
        compileOptions {
            sourceCompatibility JavaVersion.VERSION_1_8
            targetCompatibility JavaVersion.VERSION_1_8
         }
        }
       ```

12. Add this at the very end of the file: ```apply plugin: 'com.google.gms.google-services'```

13. Open the `activity_main.xml` file, or whichever layout file is linked to your empty class. You do this by going into the folder `app` on the left, then `src`, then `main`, then `res`, then `layout`, and finally click on `activity_main.xml`.

14. Click on the button `Text` in the middle of the screen, then erase all the code you see, and copy the following code into it:

   ```
    <?xml version="1.0" encoding="utf-8"?>
    <android.support.constraint.ConstraintLayout
    xmlns:android="http://schemas.android.com/apk/res/android"
      xmlns:app="http://schemas.android.com/apk/res-auto"
      xmlns:tools="http://schemas.android.com/tools"
      android:layout_width="match_parent"
      android:layout_height="match_parent"
      tools:context=".MainActivity">
    
      <Button
          android:id="@+id/button"
          android:layout_width="wrap_content"
          android:layout_height="wrap_content"
          android:layout_marginStart="8dp"
          android:layout_marginTop="8dp"
          android:layout_marginEnd="8dp"
          android:layout_marginBottom="8dp"
          android:onClick="login_click"
          android:text="@string/login_button_text"
          app:layout_constraintBottom_toBottomOf="parent"
          app:layout_constraintEnd_toEndOf="parent"
          app:layout_constraintStart_toStartOf="parent"
          app:layout_constraintTop_toTopOf="parent" />
    </android.support.constraint.ConstraintLayout>
   ```

15. Open the folder that says ```values```, open the ```strings.xml``` file and add this to the resources:    ```<string name="login_button_text">Login With Firebase UI</string>```

16. Now you need to go to your empty java class. Under the **app** folder on the right, click on **src**, then on "main, and then on **java**. Under **java** there should  be a folder with the package name. Then click on `MainActivity`. Now add this code underneath the first line that starts with `package`: 

   ```
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.ErrorCodes;
import com.firebase.ui.auth.IdpResponse;
import com.google.firebase.FirebaseApp;
import java.util.ArrayList;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    Button signInButton;
    public static final int RC_SIGN_IN = 900;
    protected ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FirebaseApp.initializeApp(this);
        signInButton = (Button) findViewById(R.id.button);
    }

    public void startAuthenticationActivity () {
        ArrayList<AuthUI.IdpConfig> idps = new ArrayList<>();

        idps.add(new AuthUI.IdpConfig.EmailBuilder().build());

        startActivityForResult(
                AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setAvailableProviders(idps)
                        .build(),
                RC_SIGN_IN);
    }

    public void login_click (View v) {
        startAuthenticationActivity();
    }
    
    @Override
        protected void onResume() {
            super.onResume();
            authenticateWithCachedToken();
        }
    
        protected void authenticateWithCachedToken () {
                showProgressDialog(getString(chatsdk.co.chat_sdk_firebase_ui.R.string.authenticating));
            signInButton.setEnabled(false);
            ChatSDK.auth().authenticateWithCachedToken()
                    .observeOn(AndroidSchedulers.mainThread())
                    .doFinally(() -> {
                        signInButton.setEnabled(true);
                        //dismissProgressDialog();
                    })
                    .subscribe(() -> {
                        ChatSDK.ui().startMainActivity(MainActivity.this);
                    }, throwable -> {
    //                    startAuthenticationActivity();
                    });
        }
    
        protected void onActivityResult(int requestCode, int resultCode, Intent data) {
            super.onActivityResult(requestCode, resultCode, data);
            // RC_SIGN_IN is the request code you passed into  startActivityForResult(...) when starting the sign in flow.
            if (requestCode == RC_SIGN_IN) {
                IdpResponse response = IdpResponse.fromResultIntent(data);
    
                // Successfully signed in
                if (resultCode == RESULT_OK) {
                    return;
                }
                else {
    
                    // Sign in failed
                    if (response == null) {
                        // User pressed back button
                        ToastHelper.show(this, chatsdk.co.chat_sdk_firebase_ui.R.string.sign_in_cancelled);
                        return;
                    }
    
                    if (response.getError().getErrorCode() == ErrorCodes.NO_NETWORK) {
                        ToastHelper.show(this, chatsdk.co.chat_sdk_firebase_ui.R.string.no_internet_connection);
                        return;
                    }
    
                    if (response.getError().getErrorCode() == ErrorCodes.UNKNOWN_ERROR) {
                        ToastHelper.show(this, chatsdk.co.chat_sdk_firebase_ui.R.string.unknown_error);
                        return;
                    }
                }
                ToastHelper.show(this, chatsdk.co.chat_sdk_firebase_ui.R.string.unknown_sign_in_response);
    //            finish();
            }
        }
    
        /** Show a SuperToast with the given text. */
        protected void showToast(String text){
            if (StringUtils.isEmpty(text))
                return;
    
            ToastHelper.show(this, text);
        }
    
        protected void showProgressDialog(String message) {
            if (progressDialog == null) {
                progressDialog = new ProgressDialog(this);
            }
    
            if (!progressDialog.isShowing()) {
                progressDialog = new ProgressDialog(this);
                progressDialog.setMessage(message);
                progressDialog.show();
            }
        }
    
        protected void showOrUpdateProgressDialog(String message) {
            if (progressDialog == null) {
                progressDialog = new ProgressDialog(this);
            }
    
            if (!progressDialog.isShowing()) {
                progressDialog = new ProgressDialog(this);
                progressDialog.setMessage(message);
                progressDialog.show();
            } else progressDialog.setMessage(message);
        }
    
        protected void dismissProgressDialog() {
            // For handling orientation changed.
            try {
                if (progressDialog != null && progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }
            } catch (Exception e) {
                ChatSDK.logError(e);
            }
        }
}
   ```

17. Go to the very top right hand button (When you mouse over this button, it will say sign in to Google, and use it to sign in to Google with your Google account. If you do not have a Google account, you can use the button to create one.

    ![Buttons](C:\Users\Alpha\AndroidStudioProjects\Chat SDK Manuals\Manual-3\chat-sdk-android\Manual-3\Buttons2.png)

18. Now click on the button called **Sync Project with Gradle Files**. It should be at the top right hand corner, 5 buttons from the google account button. Ignore any messages telling you that the build failed,

19. Go to **Tools** -> **Firebase**. Go to the tab on the right, click on analytics, click on  **Log an Analytics event**, and then click **Connect to Firebase** then click on **Connect to firebase**. If there are errors, click **Connect to firebase** again and click **sync**, until the button turns into the word "Connected".

20. Click the **Gradle Sync** Button again.

21. Now you can go to to the [Firebase Console](https://console.firebase.google.com/)  in your web browser, and you should find your project. It should be a large white tile with the name of your app. Click on your project, then go to the firebase dashboard, and go to **Authentication** -> **Sign-In-Method**, and click on whichever sign in options you like. We recommend clicking only on the **Sign in with Email and Password** option, or further steps will become more complicated. Switch both Sign in switches to "On" and click **Save**.


27. Now you need to create a new class. Under the **app** folder on the right, click on **src**, then on "main, and then on **java**. Under **java** there should  be a folder with the package name. Right click on it, then go to **new** and click on **Java Class**. Call the class "AndroidApp" and under the label Superclass, write "Application". In the body of the class, erase all text **except for the first line.** This would normally be `package PACKAGE NAME;`and copy this code into it:


   ```
    import android.app.Application;
    import android.content.Context;
   
    import co.chatsdk.core.error.ChatSDKException;
    import co.chatsdk.core.session.ChatSDK;
    import co.chatsdk.core.session.Configuration;
    import co.chatsdk.firebase.FirebaseNetworkAdapter;
    import co.chatsdk.firebase.file_storage.FirebaseFileStorageModule;
    import co.chatsdk.firebase.push.FirebasePushModule;
    import co.chatsdk.ui.manager.BaseInterfaceAdapter;
   
    public class AndroidApp extends Application {
   
    @Override
    public void onCreate() {
        super.onCreate();
   
        Context context = getApplicationContext();
   
    // Create a new configuration
           Configuration.Builder config = new      Configuration.Builder(context);
   
    // Perform any configuration steps (optional)
           config.firebaseRootPath("prod");
   
    // Initialize the Chat SDK
           try {
               ChatSDK.initialize(config.build(), new     BaseInterfaceAdapter(context), new FirebaseNetworkAdapter());
           }
           catch (ChatSDKException e) {
           }
   
    // File storage is needed for profile image upload and image messages
           FirebaseFileStorageModule.activate();
           FirebasePushModule.activate();
   
    // Activate any other modules you need.
    // ...
   
     }
    }
   ```

28. If you have this class a different name than AndroidApp, you need to change the name of it in the line `public class AndroidApp extends Application` to whatever the name of the app is.

29. Open your `AndroidManifest.xml` file, it should be in the "main" folder. Add this code to the `<application` section: `android:name=".MainActivity"`. Then add this code to the end of your `application` section:
   ```
    <activity android:name=".AndroidApp">
                <intent-filter>
                    <action android:name="android.intent.action.MAIN" />
                    <category  android:name="android.intent.category.LAUNCHER" />
                </intent-filter>
            </activity>
   ```

30.  If you gave the AndroidApp class a different name, enter that name instead.
32. Currently, your `Android Manifest.xml` file should look something like this: 

   ```
    <?xml version="1.0" encoding="utf-8"?>
    <manifest xmlns:android="http://schemas.android.com/apk/res/android"
       package="domain.testing.totalintegrationtest">
   
    <application
        android:allowBackup="true"
        android:icon="@mipmap/ic_launcher"
        android:label="@string/app_name"
        android:roundIcon="@mipmap/ic_launcher_round"
        android:supportsRtl="true"
        android:theme="@style/AppTheme">
        <activity android:name="com.example.newfirebaseuitest.MainActivity"
            android:label="Login">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category  android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>
    </application>
   ```

31. Click the Gradle Sync button again.

32. Go back to your [Firebase Console](https://console.firebase.google.com/) , click on your app, click on **Database**. Scroll down to where it says **Realtime Database** and click on **Create database**. Start in locked mode and click **Enable**. Click the **Rules** tab. Delete everything in the box, then go to this [rules.json](https://github.com/chat-sdk/chat-sdk-android/blob/master/firebase-rules.json) file, copy everything in the box (approximately 355 lines), and paste it into the box in the firebase console. Click on **Publish**.

33. Run the app and click on the sign in button when the app launches. Select your preferred login account when the option **Continue with** comes up. When you click on **Save**. When the smart lock option comes up, make sure you click **SAVE PASSWORD, or else it will not work!**

34. At this point you are finished with setting up the basic app. If you would like to add the ability for the app to handle push notifications or location based messages, please continue with the steps in the following sections.

### Push Notifications

35. To handle push notifications, we use [Firebase Cloud Functions](https://firebase.google.com/docs/functions/).  This service allows you to upload a script to Firebase hosting. This  script monitors the Realtime database and whenever a new message is  detected, it sends a push notification to the recipient. Below is a summary of the steps that are required to setup push using  the Firebase Cloud Functions script. For further instructions you can  look at the [Firebase Documentation](https://firebase.google.com/docs/functions/get-started).

36. Install [Node.js](https://nodejs.org/). Then, in case you are using windows, run windows PowerShell, and in case you are using a mac, run the terminal app.

37. Run `firebase login` and login using the browser.

38. Make a new directory or choose an existing one to store your push functions in.

39. Navigate to that directory using the terminal.

48. Run `firebase init functions`.

49. Choose `y` to add the permissions.

50. Choose the correct project from the list, or select a new one if your current project is not present in the list.

51. Choose `y` for ESLint.

52. Choose `y` to install the node dependencies.

53. Run `firebase use --add` and pick the correct project from the list. It should be visible in the list now.

54. The alias can be any name that you like.

55. Find the `functions` directory you've just created and copy the `index.js` file from [Github](https://github.com/chat-sdk/chat-sdk-android/tree/master/FirebasePushNotifications) into the directory

56. Run `firebase deploy`. You are now done using windows power shell or the terminal app. The script is now active and push notifications will be set out automatically.

57. Now click on the button called **Sync Project with Gradle Files**. It should be at the top left hand corner, 5 buttons from the google account button. When the gradle sync completes, your App is ready to go!

### Enabling location based messages

50. If you would like for your app to be able to receive messages based on the location of the user's device, then you need to activate location based messages. The Chat SDK needs two google services to support location messages. The [Google Places API](https://developers.google.com/places/) to select the location and the [Google Static Maps API](https://developers.google.com/maps/documentation/static-maps/) to display the location.

58. Go to the [Google Places API](https://developers.google.com/places/) page, click **Get Started**, then click **Places**, and then click **Continue**.

59. After this you select your Project from the drop down lit and click **Next**. Then **QUICKLY** click on **Create a billing Account** when the dialog box pops up. If you miss it, simply repeat steps 30 and 31. In order to do this you will need a billing account. If you want to do that, then continue, otherwise disable location messages by placing this text into the AndroidApp's `Oncreate` method: ```config.locationMessagesEnabled(false)``` and skip to the conclusion, otherwise follow the next steps.

60. Select your country, and accept the Terms of Service, then click **Agree and Continue**. Click **Set up payments profile** and enter your billing information. Click **Start my free trial**, then click **Next** to enable to google maps platform. Copy your API key and click **Done**.

61. Although you need to setup billing, Google give you 200 USD per month for free. So you can load 10 million free location messages for free per month.

61. Go back to Android Studio, Add this line to the `oncreate` method of the AndroidApp: `config.googleMaps("YOUR GOOGLE PLACES API KEY");`

62. Now go the `AndroidManifest.xml` file and add this line directly above `</application>`line in the file: `<meta-data android:name="com.google.android.geo.API_KEY" android:value="YOUR COPIED GOOGLE PLACES API KEY"/>`

63. Now go to [Google Static Maps API](https://developers.google.com/maps/documentation/static-maps/) and click on **Get Started**. Click on **Maps** and click **Continue**. Select the appropriate project and click **Next**, copy the API Key then click **Done**. Save this API Key in some other file as you may need it later.

### Conclusion

Congratulations! 🎉🎉 You've just turned your app into a fully featured instant messenger! Keep reading below to learn how to further customize the Chat SDK  as well as add various other modules as needed.