# Chat SDK for Android
### Open Source Messaging framework for Android

<img target="_blank" src="http://img.chatcatapp.com/chatsdk/template_1_new.png" />

Chat SDK is a fully featured open source instant messaging framework for Android. Chat SDK is fully featured, scalable and flexible and follows the following key principles:

- **Free.** The Chat SDK is free for [commercial use](https://github.com/chat-sdk/chat-sdk-android/blob/master/LICENSE.md)
- **Open Source.** The Chat SDK is open source
- **Full control of the data.** You have full and exclusive access to the user's chat data
- **Quick integration.** Chat SDK is fully featured out of the box
- **Firebase** Powered by Google Firebase
- **[Download the APK](https://drive.google.com/open?id=0B5yzhtuipbsrYkl1Wkh2WjMwOEE)** to try out the Firebase Chat SDK for Android now! 

## v4.0 Released

We have just released the latest version of the Android Chat SDK. In this update we have gone over every class and rewritten large portions of the SDK to bring it fully up to date. The changes have focused around four key areas:

### Architecture

The project has been completely refactored to make it's architecture fully modular. The project has been split into 3 main modules: core, ui and firebase network adapter. The architecture is now almost identical to the iOS version of the project. 

### Technology Stack

The technology stack has been fully updated. Promises have been replaced by the more flexible RXJava library. All images are loaded using Fresco. GreenDao and all the other support libraries have been updated to the latest version. 

### Performance

Performance has been improved in a number of areas. All the list views have been updated to recycler views. Now all network processing happens on background threads. We have also stress tested the code with up to 1000 threads in a view and 10,000 messages in a thread. 

### UX

The user interface has seen a major update. We have improved the chat view as well as making the entire app more consistent. We have also added an advanced user profile. 


## Features

- Private and group messages
- Public chat rooms
- Username / password, Facebook, Twitter, Anonymous and custom login
- Push notifications
- Text, Image and Location messages
- User profiles
- User search
- Powered by Firebase
- Cross Platform - [iOS](https://github.com/chat-sdk/chat-sdk-ios), [Web](https://github.com/chat-sdk/chat-sdk-web)

## Modules

The Chat SDK has a number of additional modules that can easily be installed including:

- [Typing indicator](http://chatsdk.co/downloads/typing-indicator/)
- [Read receipts](http://chatsdk.co/downloads/read-receipts/)
- [Location based chat](http://chatsdk.co/downloads/location-based-chat/)
- [Audio messages](http://chatsdk.co/downloads/audio-messages/)
- [Video messages](http://chatsdk.co/downloads/video-messages/)
- [Sticker messages](https://chatsdk.co/downloads/sticker-messages/)
- [Contact book integration](https://chatsdk.co/downloads/contact-book-integration/)
- [Social Login](https://github.com/chat-sdk/chat-sdk-android#social-login)
- [Push Notifications](https://github.com/chat-sdk/chat-sdk-android#push-notifications)
- [File Storage](https://github.com/chat-sdk/chat-sdk-android/tree/master/chat_sdk_firebase_file_storage) (Included in basic setup instructions)
 
## Get involved!
We're very excited about the project and we're looking for other people to get involved. Over time we would like to make the best messaging framework for mobile. Helping us could involve any of the following:

+ Providing feedback and feature requests
+ Reporting bugs
+ Fixing bugs
+ Writing documentation
+ Improving the user interface
+ Help us update the library to use Swift
+ Helping to write adapters for other services such as Layer, Pusher, Pubnub etc... 
+ Write a tutorial - **we pay $100** for quality tutorials

If you're interested please review the [Contributing
Document](https://github.com/chat-sdk/chat-sdk-ios/blob/master/CONTRIBUTING.md) for details of our development flow and the CLA then email me at [**team@chatsdk.co**](mailto:team@chatsdk.co).

## Apps that use Chat SDK

+ [Parlor](http://parlor.me/)
+ [Voice - Instant Messaging App](https://play.google.com/store/apps/details?id=com.skintmedia.voice&hl=en_GB)
+ [Nex Tv Latino](https://play.google.com/store/apps/details?id=com.helpdevs.nexttv)

If you have an app that uses the Chat SDK let us know and we'll add a link. 

## Running the demo project

This repository contains a fully functional version of the Chat SDK which is configured using our Firebase account and social media logins. This is great way to test the features of the Chat SDK before you start integrating it with your app. 

> **Note:**
>You should make sure that the correct SDK versions and build tools are installed in Android Studio. To do this open the Preferences panel and navigate to **Appearance & Behavour** -> **System Settings** -> **Android SDK** or click on the **SDK Manager** icon in the tool bar. Android SDK versions 4.4 and onwards should be installed. **Android SDK Build-Tools** version 26.0.02 should be installed. 

The next step is to setup the Chat SDK using your Firebase and Social Accounts. To do that you need to do the following.

## Setup Service

We provide extensive documentation on Github but if you’re a non-technical user or want to save yourself some work you can take advantage of our [setup and integration service](http://chatsdk.co/downloads/chat-sdk-setup-service/).

## Integration with an existing project

The Chat SDK is distributed as a series of modules that can be imported into Android Studio. 

## Adding the Chat SDK to your project

Integration with an existing project is simple. The first step is to download the Chat SDK library and import the modules you need. 

There are two ways to add the Chat SDK. You can either import the modules manually or add they using Gradle.

### Gradle

<HERE> Gradle 

### Adding Modules Manually

In Android Studio:

**File** -> **New** -> **Import Module**

You must import the following core modules:

- `chat_sdk_core`
- `chat_sdk_ui`

And at least **one** network adapter:

- `chat_sdk_firebase_adapter`
- `chat_sdk_xmpp_adapter` - [Available here](https://chatsdk.co/downloads/xmpp-chat-sdk-for-android/)

And any of the following optional modules:

- `chat_sdk_firebase_file_storage`
- `chat_sdk_firebase_push`
- `chat_sdk_firebase_social_login`
- `chat_sdk_audio_message` - [Available here](http://chatsdk.co/downloads/audio-messages/)
- `chat_sdk_video_message` - [Available here](http://chatsdk.co/downloads/android-video-messages/)
- `chat_sdk_sticker_message` - [Available here](http://chatsdk.co/downloads/sticker-messages/)
- `chat_sdk_read_receipts` - [Available here](http://chatsdk.co/downloads/read-receipts/)
- `chat_sdk_typing_indicator` - [Available here](http://chatsdk.co/downloads/typing-indicator/)
- `chat_sdk_contact_book` - [Available here](http://chatsdk.co/downloads/contact-book-integration/)

Now import the modules in your `build.gradle` file. 

```
compile project(path: ':chat_sdk_ui')
compile project(path: ':chat_sdk_firebase_push')
```

At the end of this file, add the following:

```
apply plugin: 'com.google.gms.google-services'
```

#### Configuring the project

Now that the modules have been added, we need to configure the project. 

**SDK Version**

Now you will see that gradle cannot be sync because it missing some parameters. Open to **gradle.properties** file in the root of the project and add the following lines.

```
MIN_SDK = 16
ANDROID_BUILD_SDK_VERSION = 25
ANDROID_BUILD_TOOLS_VERSION = 25.0.2
ANDROID_BUILD_TARGET_SDK_VERSION = 25
ANDROID_COMPILE_SDK_VERSION = 25
```

> **Note:**
>You should make sure that the correct SDK versions and build tools are installed in Android Studio. To do this open the Preferences panel and navigate to **Appearance & Behavior** -> **System Settings** -> **Android SDK** or click on the **SDK Manager** icon in the tool bar. Android SDK versions 4.4 and onwards should be installed. **Android SDK Build-Tools** version 21.1.0 should be installed. 

**Update Gradle**

Next open the **build.gradle** file for your project. It should be called **build.gradle (Project: Your-project-name)**

Update this file by adding the following.

```
buildscript {
    repositories {
        //*** Make sure these lines are present ***
        jcenter()
        mavenCentral()
    }
    dependencies {
        //*** Make sure these lines are present ***
        classpath 'com.android.tools.build:gradle:2.3.3'
        classpath 'com.google.gms:google-services:3.0.0'
    }
}

allprojects {
    if (OperatingSystem.current().isWindows()) {
        buildDir = "C:/tmp/${rootProject.name}/${project.name}"
    }
    repositories {
        mavenCentral()
        maven { url "https://jitpack.io" }
        jcenter()
        maven { url "https://maven.google.com" }
    }
}

task clean(type: Delete) {
    delete rootProject.buildDir
}
```

Add any other modules that you need. Then sync the project with gradle. 

### Initializing the Chat SDK

Now open your applications's main class and find the `onCreate` method. Add the following to setup the Chat SDK:

```
// Enable multi-dexing
MultiDex.install(this);

Context context = getApplicationContext();

// Create a new configuration
Configuration.Builder builder = new Configuration.Builder(context);

// Perform any configuration steps

// Initialize the Chat SDK
ChatSDK.initialize(builder.build());


// Activate the Firebase module
FirebaseModule.activate(context);

// File storage is needed for profile image upload and image messages
FirebaseFileStorageModule.activate();

// Activate any other modules you need.
// ...

``` 

>**Note**  
>You may need to a the multi-dexing dependency

```
compile 'com.android.support:multidex:1.0.1'
```

Activate the core Chat SDK modules and any other modules you need. See the module setup guide for more information. 

Launch the Chat SDK login activity by adding this to your `AndroidManifest.xml`:

```
<activity android:name="co.chatsdk.ui.login.LoginActivity">
    <intent-filter>
        <action android:name="android.intent.action.MAIN" />
        <category android:name="android.intent.category.LAUNCHER" />
    </intent-filter>
</activity>
```

Or if you want to launch the activity manually, you can do it using:

```
InterfaceManager.shared().a.startLoginActivity(context, true);
```

## Configuration

There are two ways to configure the Chat SDK. By using the configuration object that is passed into the ChatSDK instance on initialization or by adding certain to the `AndroidManifest.xml` file. 

#### Configuration using AndroidManifest

In the instructions we tell you how to add values to your Android Manifest. This will always require you to add a user defined piece of meta-data to the `AndroidManifest.xml` file. It's also best practice to store the values in a separate file in the `res/values` folder of your app. You can use the Android Manifest to set a range of API keys but more settings are available if you modify the `Configuration` object directly.

#### Configuration using the Configuration Builder

In your main `onCreate` method you create a new instance of the `Configuration.Builder` and pass it to the `ChatSDK` singleton on initialization.

Here you have the option to set far more properties. For example:

##### Configure Firebase

```
builder.firebase("FirebaseURL", "rootPath", "storageUrl", "CloudMessaging Api Key");
``` 

##### Disable Facebook and Twitter login

```
builder.facebookLoginEnabled(false);
builder.twitterLoginEnabled(false);
```

##### Set a custom user name and avatar

```
builder.defaultUserName("TestUser");
builder.defaultUserAvatarUrl("http://your-site/image.png");
```

Remember that in the instructions we will advise you to configure using the Android Manifest but you are always free to use the direct configuration method instead. 

## Firebase Setup

1. Go to the [Firebase](http://firebase.com/) website and sign up or log in
2. Go to the [Firebase console](https://console.firebase.google.com/) and make a new project
3. Click **Add project**
4. Choose a name and a location
5. Click **Settings** (the gear icon). On the General tab, click **Add App -> Add Firebase to your Android app**
6. Enter your package name, app name and SHA-1 key
7. Download the **GoogleServices** file and add it to your app project. It should be added inside the `app` folder.

  >**Note:**  
  >It is worth opening your downloaded ```GoogleService-Info.plist``` and checking there is an ```API_KEY``` field included. Sometimes Firebase's automatic download doesn’t include this in the plist. To rectify, just re-download the plist from the project settings menu.  
  
8. Next copy the following lines to your **AndroidManifest.xml** file. 

  ```
  <meta-data android:name="firebase_url" android:value="@string/firebase_url" /> 
  <meta-data android:name="firebase_root_path" android:value="@string/firebase_root_path" />
  <meta-data android:name="firebase_storage_url" android:value="@string/firebase_storage_url" />

  ```
  
  Create a resource file called `chat_sdk_firebase.xml` in the `res/values` folder of your project and set the following keys:
  
  ```
  <string name="firebase_url">https://[YOUR APP].firebaseio.com</string>
  <string name="firebase_root_path">ROOT PATH</string>
  <string name="firebase_storage_url">gs://[YOUR APP].appspot.com</string>
  ```  
  >**Note**  
  >The root path variable allows you to run multiple Chat SDK instances on one Firebase account. Each different root path will represent a completely separate set of Firebase data. This can be useful for testing because you could have separate path for testing and production.
  
  >**Note**   
  > The file storage URL can be found in the Storage tab of the Firebase dashboard.

9. In the Firebase dashboard click **Authentication -> Sign-in method** and enable all the appropriate methods 

### Enabling location messages

The Chat SDK needs two google services to support location messages. The [Google Places API](https://developers.google.com/places/) to select the location and the [Google Maps API](https://developers.google.com/maps/documentation/android-api/) to display the location. 

Then add the following to your `AndroidManifest.xml` file:

```
<meta-data android:name="com.google.android.geo.API_KEY" android:value="@string/google_maps_api_key"/> 
```

Add this to your `chat_sdk_firebase.xml` file:

```
<string name="google_maps_api_key">YOUR KEY</string>
```

## Module Setup

There are a number of free and premium extensions that can be added to the Chat SDK. 

### Firebase Modules

For the following modules:

- Firebase File Storage (free)
- Firebase Push Notifications (free)
- Firebase Social Login (free)
- [Typing indicator](http://chatsdk.co/downloads/typing-indicator/)
- [Read receipts](http://chatsdk.co/downloads/read-receipts/)
- [Location based chat](http://chatsdk.co/downloads/location-based-chat/)
- [Audio messages](http://chatsdk.co/downloads/audio-messages/)
- [Video messages](http://chatsdk.co/downloads/video-messages/)
- [Contact book integration](http://chatsdk.co/downloads/contact-book-integration/)

The free modules are located in the main [Github repository](https://github.com/chat-sdk/chat-sdk-android). The premium modules can be purchased and downloaded from the links provided above. 

### Social Login

Add the following to your `build.gradle`

##### Gradle

```
<HERE>
```

##### Module Import

```
compile project(path: ':chat_sdk_firebase_social_login')
```

In your main class `onCreate` method add:

```
FirebaseSocialLoginModule.activate(getApplicationContext());
```

#### Facebook

1. On the [Facebook developer](https://developers.facebook.com/) site get the **App ID** and **App Secret**
2. Go to the [Firebase Console](https://console.firebase.google.com/) and open the **Auth** section
3. On the **Sign in method** tab, enable the **Facebook** sign-in method and specify the **App ID** and **App Secret** you got from Facebook.
4. Then, make sure your **OAuth redirect URI** (e.g. `my-app-12345.firebaseapp.com/__/auth/handler`) is listed as one of your **OAuth redirect URIs** in your Facebook app's settings page on the Facebook for Developers site in the **Product Settings > Facebook Login** config
5. Add the following to your `AndroidManifest.xml`:

  ```
  <meta-data android:name="com.facebook.sdk.ApplicationId" android:value="@string/facebook_app_id"/>
  ``` 
  
  Add the following to your `chat_sdk_firebase.xml` file:
  
  ```
  <string name="facebook_app_id">[FACEBOOK APP KEY]</string>
  ```

#### Twitter

1. [Register your app](https://apps.twitter.com/) as a developer application on Twitter and get your app's **API Key** and **API Secret**.
2. In the [Firebase console](https://console.firebase.google.com/), open the **Auth** section.
3. On the **Sign in method** tab, enable the **Twitter** sign-in method and specify the **API Key** and **API Secret** you got from Twitter.
4. Then, make sure your Firebase **OAuth redirect URI** (e.g. `my-app-12345.firebaseapp.com/__/auth/handler`) is set as your **Callback URL** in your app's settings page on your [Twitter app's config](https://apps.twitter.com/).
5. Add the following to your `AndroidManifest.xml`:

  ```
  <meta-data android:name="twitter_key" android:value="@string/twitter_key" />
  <meta-data android:name="twitter_secret" android:value="@string/twitter_secret" />
  ``` 
  
  Add the following to your `chat_sdk_firebase.xml` file:
  
  ```
  <string name="twitter_key">[TWITTER KEY]</string>
  <string name="twitter_secret">[TWITTER SECRET]</string>
  ```

#### Google
  
1. If you haven't yet specified your app's SHA-1 fingerprint, do so from the [Settings page](https://console.firebase.google.com/project/_/settings/general/) of the Firebase console. See [Authenticating Your Client](https://developers.google.com/android/guides/client-auth) for details on how to get your app's SHA-1 fingerprint.
2. In the [Firebase console](https://console.firebase.google.com/), open the **Auth** section.
3. On the **Sign in method** tab, enable the **Google** sign-in method and click **Save**.
4. You must pass your [server's client ID](https://developers.google.com/identity/sign-in/android/start-integrating#get_your_backend_servers_oauth_20_client_id) to the requestIdToken method. To find the OAuth 2.0 client ID.
5. Open the [Credentials page](https://console.developers.google.com/apis/credentials) in the API Console.
6. The **Web application type** client ID is your backend server's OAuth 2.0 client ID.
7. Add the following to your `AndroidManifest.xml`:

  ```
  <meta-data android:name="google_web_client_id" android:value="@string/google_web_client_id" />
  ``` 
  
  Add the following to your `chat_sdk_firebase.xml` file:
  
  ```
  <string name="google_web_client_id">[CLIENT ID]</string>
  ```
  
Social login can also be enabled or disabled by changing the Chat SDK [configuration](https://github.com/chat-sdk/chat-sdk-android#configuration).   

### Push Notifications

Add the following to your `build.gradle`

##### Gradle

```
<HERE>
```

##### Module Import

```
compile project(path: ':chat_sdk_firebase_push')
```

In your main class `onCreate` method add:

```
FirebasePushModule.activateForFirebase();
```


3. Get the push token. Go to the [Firebase Console](https://console.firebase.google.com) click **your project** and then the **Settings** button. Click the **Cloud Messaging** tab. Copy the **Server Key**.
4. Add the following to your `AndroidManifest.xml`:

  ```
  <meta-data android:name="cloud_messaging_server_key" android:value="@string/firebase_cloud_messaging_server_key" />
  ``` 
  
  Add the following to your `chat_sdk_firebase.xml` file:
  
  ```
  <string name="firebase_cloud_messaging_server_key">[SERVER KEY]</string>
  ```
  
### Other Modules

For the following modules:

- [Keyboard overlay](http://chatsdk.co/downloads/keyboard-overlay/)
- [Sticker messages](http://chatsdk.co/downloads/sticker-messages/)
- [Contact book integration](http://chatsdk.co/downloads/contact-book-integration/)
- [Typing indicator](http://chatsdk.co/downloads/typing-indicator/)
- [Read receipts](http://chatsdk.co/downloads/read-receipts/)
- [Location based chat](http://chatsdk.co/downloads/location-based-chat/)
- [Audio messages](http://chatsdk.co/downloads/audio-messages/)
- [Video messages](http://chatsdk.co/downloads/video-messages/)
- [Contact book integration](http://chatsdk.co/downloads/contact-book-integration/)

After you have purchased the module you will be provided with a link to the module source code. Unzip this file and import it into Android Studio.

1. Click **File** -> **New** -> **Import Module**
2. Add the module to your `build.gradle`

  ```
  compile project(path: ':chat_sdk_[module name]')
  ```
  
3. Sync Gradle
4. In your main class `onCreate` activate the module:

  ```
  ContactBookModule.activateForFirebase();
  ```
  
  or 

  ```
  ContactBookModule.activateForXMPP();
  ```

### Security Rules

Firebase secures your data by allowing you to write rules to govern who can access the database and what can be written. On the Firebase dashboard click **Database** then the **Rules** tab. 

Copy the contents of the [**rules.json**](https://github.com/chat-sdk/chat-sdk-ios/blob/master/rules.json) file into the rules and click publish. 
  
## The license

We offer a choice of two license for this app. You can either use the [Chat SDK](https://chatsdk.co/chat-sdk-license/) license or the [GPLv3](https://www.gnu.org/licenses/gpl-3.0.en.html) license. 

Most Chat SDK users either want to add the Chat SDK to an app that will be released to the App Store or they want to use the Chat SDK in a project for their client. The **Chat SDK** license gives you complete flexibility to do this for free.

**Chat SDK License Summary**

+ License does not expire.
+ Can be used for creating unlimited applications
+ Can be distributed in binary or object form only
+ Commercial use allowed
+ Can modify source-code but cannot distribute modifications (derivative works)

If a user wants to distribute the Chat SDK source code, we feel that any additions or modifications they make to the code should be contributed back to the project. The GPLv3 license ensures that if source code is distributed, it must remain open source and available to the community.

**GPLv3 License Summary**

+ Can modify and distribute source code
+ Commerical use allowed
+ Cannot sublicense or hold liable
+ Must include original license
+ Must disclose source 

**What does this mean?**

Please check out the [Licensing FAQ](https://github.com/chat-sdk/chat-sdk-ios/blob/master/LICENSE.md) for more information.
