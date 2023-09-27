## Adding the Chat SDK to your project
###### Quick start guide - it takes about 10 minutes!

Integration with an existing project is simple. You can add the core libraries using Gradle. 

### Gradle

Until the project is published in jCenter, you will need to include the repository manually:

```
repositories {
    maven { url "https://jitpack.io" }
    maven { url "http://dl.bintray.com/chat-sdk/chatkit" }
    maven { url "http://dl.bintray.com/chat-sdk/matisse"}
    maven { url "http://dl.bintray.com/chat-sdk/dexter"}
    maven { url "https://dl.bintray.com/mikepenz/maven" }
    maven { url "https://dl.bintray.com/chat-sdk/keyboardvisibilityevent" }
}
```

Then add this to your `dependencies` area:

```
implementation 'sdk.chat:chat-sdk-core:4.8.9'
implementation 'sdk.chat:chat-sdk-ui:4.8.9'
implementation 'sdk.chat:chat-sdk-firebase-adapter:4.8.9'
implementation 'sdk.chat:chat-sdk-firebase-ui:4.8.9'

// If you want the main draw activity
implementation 'sdk.chat:chat-sdk-ui-extras:4.8.9'

// Neede for push notifications
implementation 'sdk.chat:chat-sdk-firebase-push:4.8.9'

// Needed for file uploads
implementation 'sdk.chat:chat-sdk-firebase-file-storage:4.8.9'

// Allows users to upload multiple profile pictures
implementation 'sdk.chat:chat-sdk-profile-pictures:4.8.9'
```

You may also need to enable Java 8:

```
compileOptions {
    sourceCompatibility JavaVersion.VERSION_11
    targetCompatibility JavaVersion.VERSION_11
}
```

You can also add the [modules manually](https://github.com/chat-sdk/chat-sdk-android#adding-modules-manually) by downloading source code and importing the modules to Android Studio. 

Firebase depends on Google Services so we need to apply the google services plugin. This will read the **Google Services** file that we will add during Firebase setup. Add this to your top level `build.gradle` file.

```
buildscript {
    repositories {
        jcenter()
    }
    dependencies {
        classpath 'com.google.gms:google-services:4.3.3'
    }
}
```

Add this to the end of your app level `build.gradle` file:

```
apply plugin: 'com.google.gms.google-services'
```

#### Android X

Make sure you've added the following to your `gradle.properties` file:

```
android.useAndroidX=true
android.enableJetifier=true
```

### Initializing the Chat SDK

Now open your applications's main class and find the `onCreate` method. Add the following to setup the Chat SDK:

```
Context context = getApplicationContext();

try {

    // Create a new configuration
    Configuration config = new Configuration.Builder()
            .firebaseRootPath("dev")
            .googleMaps("Your Key")
            .build();

    // Initialize the Chat SDK
    ChatSDK.initialize(context, config, FirebaseNetworkAdapter.class, BaseInterfaceAdapter.class);
    
} catch (Exception e) {
    // Handle any exceptions
    e.printStackTrace();
} finally {
    // Enable FirebaseUI with email and phone auth
    FirebaseUIModule.activate(EmailAuthProvider.PROVIDER_ID, PhoneAuthProvider.PROVIDER_ID);
  
    // File storage is needed for profile image upload and image messages
    FirebaseFileStorageModule.activate();

    // Push notification module
    FirebasePushModule.activate();
    
    // Enable multiple profile pictures per user
    ProfilePicturesModule.activate();

    // Activate any other modules you need.
    // ...
}

``` 

Activate the core Chat SDK modules and any other modules you need. See the module setup guide for more information. 

Launch the Chat SDK login activity by adding this to your `AndroidManifest.xml`:

```
<activity android:name="sdk.chat.ui.activities.SplashScreenActivity">
    <intent-filter>
        <action android:name="android.intent.action.MAIN" />
        <category android:name="android.intent.category.LAUNCHER" />
    </intent-filter>
</activity>
```

Or if you want to launch the activity manually, you can do it using:

```
ChatSDK.ui().startSplashScreenActivity(context);
``` 

## Firebase Setup

1. Go to the [Firebase](http://firebase.com/) website and sign up or log in
2. Go to the [Firebase console](https://console.firebase.google.com/) and make a new project
3. Click **Add project**
4. Choose a name and a location
5. Click **Settings** (the gear icon). On the General tab, click **Add App -> Add Firebase to your Android app**
6. Enter your package name - found in your app's `build.gradle` file, app name and SHA-1 key (optional)
7. Download the **google-services.json** file and add it to your app project. It should be added inside the `app` folder.
8. In the Firebase dashboard click **Authentication -> Sign-in method** and enable all the appropriate methods 
9. Enable the security rules: [Enabling Security Rules](https://github.com/chat-sdk/chat-sdk-firebase).
10. Enable push notifications: [Push Notifications](https://github.com/chat-sdk/chat-sdk-firebase)

### Enabling location messages

The Chat SDK needs two google services to support location messages. The [Google Places API](https://developers.google.com/places/) to select the location and the [Google Static Maps API](https://developers.google.com/maps/documentation/static-maps/) to display the location.

Add the following during the configuration stage:

```
builder.googleMaps("YOUR GOOGLE MAPS STATIC API KEY");
```

You will also need to add your Google Places API key to the app manifest:

```
<meta-data android:name="com.google.android.geo.API_KEY" android:value="YOUR GOOGLE PLACES API KEY"/>
```
>**Note:**
> You need to enable billing in your google cloud console or else you will not be able to view map images in your messages, For more information visit the [Google usage and billing support page](https://developers.google.com/places/web-service/usage-and-billing#important-enable-billing)

You can disable location messages using the `builder.locationMessagesEnabled(false)` option. 

### Conclusion

Congratulations! 🎉🎉 You've just turned your app into a fully featured instant messenger! Keep reading below to learn how to further customize the Chat SDK.