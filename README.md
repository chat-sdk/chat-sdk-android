#Chat SDK for Android
###Open Source Messaging framework for Android

<img target="_blank" src="http://img.chatcatapp.com/chatsdk/template_1.png" />

Chat SDK is a fully featured open source instant messaging framework for Android. Chat SDK is fully featured, scalable and flexible and follows the following key principles:

- **Open Source.** The Chat SDK is open source under the MIT license.
- **Full data control.** You have full and exclusive access to the user's chat data
- **Quick integration.** Chat SDK is fully featured out of the box
- **Firebase** Powered by Google Firebase

##Features

- Private and group messages
- Public chat rooms
- Username / password, Facebook, Twitter, Anonymous and custom login
- Push notifications
- Text, Image and Location messages
- User profiles
- User search
- Powered by Firebase
- [Cross Platform - see iOS Version](https://github.com/chat-sdk/chat-sdk-ios)

##Get involved!
We're very excited about the project and we're looking for other people to get involved. Over time we would like to make the best messaging framework for mobile. Helping us could involve any of the following:

+ Providing feedback and feature requests
+ Reporting bugs
+ Fixing bugs
+ Writing documentation
+ Improving the user interface
+ Help us update the library to use Swift
+ Helping to write adapters for other services such as Layer, Pusher, Pubnub etc... 

If you're interested please email me at **ben@chatsdk.co**.

##Adding ChatSDK to an existing project

You can easily enable instant messaging for your app in only a few minutes by adding the ChatSDK to your existing project:

1. Open your existing project or create a new one
2. Download the Android ChatSDK and unzip it
3. Import the project modules from the ChatSDK to your project
  We need to import the following modules `country_picker`, `facebook`, `firebase_plugin`, generator and sdk. To do this open project and click **file** -> **new** -> **import module**
  
  <img target="_blank" src="http://img.chatcatapp.com/chatsdk/android_screen_1.png" />
  
  
  Next click on the far right button to browse your directories. Navigate to where you have saved the ChatSDK project and select the above dependancies. You should be able to import all of them at once, if you can't then repeat the process to add all the modules.

4. Add the SDK versions  
Now you will see that gradle cannot be sync because it missing some parameters. Go to `gradle.properties` in the root of your project and add this to allow us to set higher build versions for the entire SDK all at once:

  ```
  MIN_SDK = 15
  ANDROID_BUILD_SDK_VERSION = 21
  ANDROID_BUILD_TOOLS_VERSION = 21.1.0
  ANDROID_BUILD_TARGET_SDK_VERSION = 21
  ANDROID_COMPILE_SDK_VERSION = 21
  ```
5. Add `Google-Services` to your gradle file  
Finally you need to add the following code to your project's 'build.gradle' file. Ensure the repositories contains the following:

  ```
  repositories {
      mavenCentral()
      jcenter()
  }
  ```

  and the dependencies contain the following:
  
  ```
  classpath 'com.android.tools.build:gradle:2.1.3'
  classpath 'com.google.gms:google-services:3.0.0'
  ```
  
  >**Note:**  
  >It is worth checking the latest version for these. The ones displayed are the correct ones on time of writing and will depreciate when new versions are released.

##Configuring ChatSDK
ChatSDK relies on serveral different services to be fully operational. Firebase deals with storing the user data, notifying the app when the data has changed and storing the files for the audio, video and image messages. Backendless takes care of push notifications on the app. Facebook and Twitter can also be used if you want social media login for your app. ChatSDK comes preconfigured for all these types on our test accounts meaning you will need to create your own before you can get fully up and running.

###Firebase
1. Go to the Firebase website and create an account if you do not already have one
2. Create a new project on the Firebase dashboard
3. Click on this project and then click database in the left hand menu
4. Copy the url from near the top of the screen, it shuold look something like this: 'https://yourappname.firebaseio.com/'
5. In the ChatSDK Android open 'com.braunster.chatsdk.network.BDefines.java' and copy your path in place of the 'ServerUrl' and also add it to your Firebas storage path. Finally set your Firebase root path.

>**IMPORTANT**  
>The base URL path mush have a trailing slash. If you miss off the trailing slash the SDK won't be able to process the URLs properly and messages may not be updated.

```
public static final String BRootPath = "testRoot/";

public static String ServerUrl = "https://your-firebase-name.firebaseio.com/" + BRootPath;

public static String FirebaseStoragePath = "gs://your-firebase-name.appspot.com";
```

>**NOTE:**  
>For the Firebase storage path you need to only add the Firebase name you have set followed by appspot.com.

>**NOTE:**  
>The root path of the app allows you to set custom branches for a single project. Depending on the string set will determine where the data is stored. This means you can set a different root path for your production and testing phases meaning that the data will be completely seperatede from each other but stored in the same place to view.

Next you need to add your app to your Firebase project. Go to the Firebase dashboard. 

1. Click the cog icon and then click Project settings
2. Click Add app
3. Click the Android icon
4. Add your App package ID 
5. The rest of the instructions are all already covered, finishing should download a Google Services JSON file

You should now add this JSON file to your sdk file.

>**IMPORTANT:**  
>You need to go through this process twice. The first time you need to add the package name of your app (to the sdk file). The second time you need to add the following package name: **com.braunster.androidchatsdk.firebaseplugin**

This JSON needs to be added into your `firebase_plugin` folder. If you do not add this then the ChatSDK won't compile properly. 

####Security Rules

Firebase secures your data by allowing you to write rules to govern who can access the database and what can be written. On the Firebase dashboard click **Database** then the **Rules** tab. 

Copy the contents of the **rules.json** file into the rules and click publish. 

####Authentication
Firebase allows users to login with many different account (Email, Anonymous, Facebook, Twitter, Google etc) but these need to be enabled from the app dashboard.

1. Click Auth in the left hand menu
2. Click SIGN-IN METHOD in the top menu
3. Click the sign in method you want and then enable. For social media logins you can come here for explanations of getting them set up.

###Facebook setup

1. Navigate to the Facebook developer account page.

2. If you don't already have an account create one. Then click **Apps** -> **Create new app**. On the new app page click **Settings**. Click **Add Platform** and choose Android.

3. Fill in a display name and a contact email. Then you need to set the app key hash. To get the app hash, you can use the `Utils.getSHA`. You will also need to set your app package name and class name.

4. Then click **Status & Review** and set the switch to **Yes** this will allow other people to sign on to your app.

5. Now go to the `sdk` string resource file and edit the `facebook_id` item setting it to your Facebook app id. You can also create a new item in your string resource file and it will override the original value.

6. Navigate back to the Facebook developers page and in the left hand menu click the **+ Add Product button**.

7. Choose Facebook login

8. Go back to your Firebase dashboard, click the auth button and then Facebook login, you can copy an OAuth redirect URL from here, copy it into the Valid OAuth redirect URLs area

9. Click save changes

10. Now the app is setup but you still need to link the Facebook account to Firebase and your app.

11. Go back to the Firebase dashboard and click **Login & Auth**. Click the Facebook tab and tick the **Enabled** box. Then copy paste your Facebook **App Id** and **App Secret** into the boxes provided.

###Backendless Push Notifictions

1. If you haven't already got a [Backendless account](https://backendless.com/) then go to the Backendless website and create one.
2. Create a new app on the dashboard and click it
3. Click settings in the top menu to bring up your app keys
4. Navigate to your strings.xml file and add your AppID, App Secret and Version.

```
<string name="backendless_app_id">1234567890ABCDEFGHIJKLMNOPQRSTUVWXYZ</string>
<string name="backendless_secret_key">1234567890ABCDEFGHIJKLMNOPQRSTUVWXYZ</string>
<string name="backendless_app_version">v1</string>
```

All the code for Push Notifications is already included in the ChatSDK. Getting them working only requires a small amount of configuration. For help regarding this you can take a look at the [Backendless guide](https://backendless.com/documentation/messaging/android/messaging_push_notification_setup_androi.htm).

>**NOTE:**  
>Some of the steps in this tutorial include adding code to the app, these steps should be unnecessary as they have already been added to the project.

>**NOTE:**  
>If you want push notifications to work in Development mode make sure to configure them in your provisioning profile for both development and production.

###Google Maps

Copy the `permission.MAPS_RECEIVE` declared in the **app** manifest. Notice that you need to replace `com.braunster` with your package name.

After that, you will need to add you API key in the manifest. After that Google Play services are added as a dependency by the SDK. Once you have your api key, just past it in the manifest. Notice that you will have to sign you app to get the key.

```
<!-- Google Maps Metadata-->
<meta-data
    android:name="com.google.android.maps.v2.API_KEY"
    android:value="YOUR API KEY"/>
```

There is more then one way to do it, This is how i do it but you can do it your own way.

```
signingConfigs {

    debug {
        storeFile file('this is your keystore file path')
        keyAlias your alias'
        keyPassword ‘your password’
        storePassword 'your password'
    }

    release {
        storeFile file('this is your keystore file path')
        keyAlias your alias'
        keyPassword ‘your password’
        storePassword 'your password'
    }
}
```

If you are unable to see the map you may have authentication problem, You need to add the package name and SHA to the credentials in your developer console.

##Running the app

Add Dependency to the firebase module this goes below to the android brackets in your **app** `build.gradle` file.

```
@Override
public void onCreate() {
    super.onCreate();
    ChatSDKUiHelper.initDefault(); // This is used for the app custom toast and activity transition

    BNetworkManager.init(getApplicationContext()); // Init the network manager        

    BFirebaseNetworkAdapter adapter = new     BFirebaseNetworkAdapter(getApplicationContext()); // Create new network adapter
    BNetworkManager.sharedManager().setNetworkAdapter(adapter); // Set the adapter to the network manager.
}
```

If the project is fresh and you want to open the SDK default login activity and see the app flow you need to set the SDK login activity as your launcher activity in the application manifest. You can do it by replacing your main activity with this.

```
<activity
       android:name="com.braunster.chatsdk.activities.ChatSDKLoginActivity"
       android:label="@string/app_name"
       android:screenOrientation="portrait"
       android:theme="@style/ChatSDKTheme"
       android:windowSoftInputMode="stateHidden|adjustPan"
>
    <intent-filter>
        <action android:name="android.intent.action.MAIN" />
        <category android:name="android.intent.category.LAUNCHER" />
    </intent-filter>
</activity>
If you get Error:Execution failed for task ':app:packageDebug'.> Duplicate files copied in APK META-INF/LICENSE.txt while compiling try adding:

packagingOptions {
    exclude 'META-INF/DEPENDENCIES'
    exclude 'META-INF/NOTICE'
    exclude 'META-INF/LICENSE'
    exclude 'META-INF/LICENSE.txt'
    exclude 'META-INF/NOTICE.txt'
}
```

to your build.gradle file under your **app** module. It should be inside the **android** brackets.

To get the Firebase version of the app working several steps are necessary:

Now the app should be ready to go!

If you have any problems Firebase offer good [documentation](https://www.firebase.com/docs/android/quickstart.html) and Facebook have an [integration guide](https://developers.facebook.com/docs/android/getting-started).