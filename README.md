# Chat SDK for Android
### Open Source Messaging framework for Android

<img target="_blank" src="http://img.chatcatapp.com/chatsdk/template_1.png" />

Chat SDK is a fully featured open source instant messaging framework for Android. Chat SDK is fully featured, scalable and flexible and follows the following key principles:

- **Free.** The Chat SDK is free for unlimited commercial use
- **Open Source.** The Chat SDK is open source under the [MIT license](https://tldrlegal.com/license/mit-license)
- **Full data control.** You have full and exclusive access to the user's chat data
- **Quick integration.** Chat SDK is fully featured out of the box
- **Firebase** Powered by Google Firebase
- **[Download the APK](https://drive.google.com/open?id=0B5yzhtuipbsrYkl1Wkh2WjMwOEE)** to try out the Firebase Chat SDK for Android now! 

## Features

- Private and group messages
- Public chat rooms
- Username / password, Facebook, Twitter, Anonymous and custom login
- Push notifications
- Text, Image and Location messages
- User profiles
- User search
- Powered by Firebase
- [Cross Platform - see iOS Version](https://github.com/chat-sdk/chat-sdk-ios)

## Modules

The Chat SDK has a number of additional modules that can easily be installed including:

- [Typing indicator](http://chatsdk.co/downloads/typing-indicator/)
- [Read receipts](http://chatsdk.co/downloads/read-receipts/)
- [Location based chat](http://chatsdk.co/downloads/location-based-chat/)
- [Audio messages](http://chatsdk.co/downloads/audio-messages/)
- [Video messages](http://chatsdk.co/downloads/video-messages/)
- [Push notifications](http://chatsdk.co/downloads/backendless-push-notifications/)

## Get involved!
We're very excited about the project and we're looking for other people to get involved. Over time we would like to make the best messaging framework for mobile. Helping us could involve any of the following:

+ Providing feedback and feature requests
+ Reporting bugs
+ Fixing bugs
+ Writing documentation
+ Improving the user interface
+ Help us update the library to use Swift
+ Helping to write adapters for other services such as Layer, Pusher, Pubnub etc... 

If you're interested please email me at [**team@chatsdk.co**](mailto:team@chatsdk.co).

## The license
This project uses the MIT license which is a commercially friendly open source license. The license has the following features:

+ Commercial use is allowed
+ You can modify, distribute and sublicense the source code
+ The work is provided "as is". You may not hold the author liable.
+ You must include the copyright notice
+ You must include the license 

## Wiki

We have a lot more information on our [**Wiki**](https://github.com/chat-sdk/chat-sdk-android/wiki) so make sure to check it out! 

## Running the Chat SDK

You can download the Chat SDK and run it directly using our Firebase test account. The default version of the app has everything pre-configured - location messages, social login etc...

> **Note:**
>You should make sure that the correct SDK versions and build tools are installed in Android Studio. To do this open the Preferences panel and navigate to **Appearance & Behavour** -> **System Settings** -> **Android SDK** or click on the **SDK Manager** icon in the tool bar. Android SDK versions 4.4 and onwards should be installed. **Android SDK Build-Tools** version 21.1.0 should be installed. 

The next step is to setup the Chat SDK using your Firebase and Social Accounts. To do that you need to do the following.

## Configuring Chat SDK
Chat SDK relies on serveral different services to be fully operational. Firebase deals with storing the user data, notifying the app when the data has changed and storing the files for the audio, video and image messages. Backendless takes care of push notifications on the app. Facebook and Twitter can also be used if you want social media login for your app. Chat SDK comes preconfigured for all these types on our test accounts meaning you will need to create your own before you can get fully up and running.

### Firebase
1. Go to the Firebase website and create an account if you do not already have one
2. Create a new project on the Firebase dashboard
3. Click on this project and then click database in the left hand menu
4. Copy the url from near the top of the screen, it should look something like this: 'https://yourappname.firebaseio.com/'
5. In the Chat SDK Android open 'com.braunster.Chat SDK.network.BDefines.java' and copy your path in place of the 'ServerUrl' and also add it to your Firebase storage path. 

Finally set your Firebase root path - the root path allows you to run multiple chat instances on one Firebase account. During testing you could set the root path to `test` and then when you go live, you could change it to `live`. 

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
>The root path of the app allows you to set custom branches for a single project. The value of the string that is set, will determine where the data is stored. This means you can set a different root path for your production and testing phases meaning that the data will be completely seperatede from each other but stored in the same place to view.

Next you need to add your app to your Firebase project. Go to the Firebase dashboard. 

1. Add a new Android app in the Firebase Dashboard. 
2. Make sure that you set the package name to `com.braunster.androidchatsdk.firebaseplugin
`. We use this package name because it is the name associated with the Chat SDK module which will actually be connecting to Firebase. 
3. Download the **google-services.json** file to the **firebase_plugin** directory

Repeat the steps above but this time use the **applicationID** of the main project which can be found in **build.gradle(Module: App)**. Download the **google-services.json** file and add it to the **app** folder. This will also allow you to access the Firebase database from your main project. 

**Update the Firebase defines**

Open **sdk** -> **java** -> **com** -> **braunster.chatsdk** -> **network** -> **BDefines.java**

Make sure that `ServerURL` and the `FirebaseStoragePath` are both set to the values that are defined in the **google-services.json** file. 

#### Security Rules

Firebase secures your data by allowing you to write rules to govern who can access the database and what can be written. On the Firebase dashboard click **Database** then the **Rules** tab. 

Copy the contents of the [**rules.json**](https://github.com/chat-sdk/chat-sdk-ios/blob/master/rules.json) file into the rules and click publish. 

#### Authentication
Firebase allows users to login with many different account (Email, Anonymous, Facebook, Twitter, Google etc) but these need to be enabled from the app dashboard.

1. Click Auth in the left hand menu
2. Click SIGN-IN METHOD in the top menu
3. Click the sign in method you want and then enable. For social media logins you can come here for explanations of getting them set up.

### Facebook setup

1. Navigate to the Facebook developer account page

2. If you don't already have an account create one. Then click **Apps** -> **Create new app**. On the new app page click **Settings**. Click **Add Platform** and choose Android

3. Fill in a display name and a contact email. Then you need to set the app key hash. To get the app hash, you can use the `Utils.getSHA`. You will also need to set your app package name and class name

4. Then click **Status & Review** and set the switch to **Yes** this will allow other people to sign on to your app

5. Now go to the `sdk` string resource file and edit the `facebook_id` item setting it to your Facebook app id. You can also create a new item in your string resource file and it will override the original value

6. Navigate back to the Facebook developers page and in the left hand menu click the **+ Add Product button**

7. Choose Facebook login

8. Go back to your Firebase dashboard, click the auth button and then Facebook login, you can copy an OAuth redirect URL from here, copy it into the Valid OAuth redirect URLs area

9. Click save changes

10. Now the app is setup but you still need to link the Facebook account to Firebase and your app

11. Go back to the Firebase dashboard and click **Login & Auth**. Click the Facebook tab and tick the **Enabled** box. Then copy paste your Facebook **App Id** and **App Secret** into the boxes provided

### Backendless Push Notifictions

1. If you haven't already got a [Backendless account](https://backendless.com/) then go to the Backendless website and create one
2. Create a new app on the dashboard and click it
3. Click settings in the top menu to bring up your app keys
4. Navigate to your strings.xml file and add your AppID, App Secret and Version

```
<string name="backendless_app_id">1234567890ABCDEFGHIJKLMNOPQRSTUVWXYZ</string>
<string name="backendless_secret_key">1234567890ABCDEFGHIJKLMNOPQRSTUVWXYZ</string>
<string name="backendless_app_version">v1</string>
```

All the code for Push Notifications is already included in the Chat SDK. Getting them working only requires a small amount of configuration. For help regarding this, you can take a look at the [Backendless guide](https://backendless.com/documentation/messaging/android/messaging_push_notification_setup_androi.htm).

>**NOTE:**  
>Some of the steps in this tutorial include adding code to the app, these steps should be unnecessary as they have already been added to the project.

>**NOTE:**  
>If you want push notifications to work in Development mode make sure to configure them in your provisioning profile for both development and production.

###Google Maps

Copy the `permission.MAPS_RECEIVE` declared in the **app** manifest. Notice that you need to replace `com.braunster` with your package name.

After that, you will need to add your API key in the manifest. After that Google Play services are added as a dependency by the SDK. Once you have your api key, just paste it in the manifest. Notice that you will have to sign you app to get the key.

```
<!-- Google Maps Metadata-->
<meta-data
    android:name="com.google.android.maps.v2.API_KEY"
    android:value="YOUR API KEY"/>
```

There is more than one way to do it, this is how I do it but you can do it your own way.

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

If you are unable to see the map you may have an authentication problem, You need to add the package name and SHA to the credentials in your developer console.

## Adding Chat SDK to an existing project

In this example, we are going to create a new blank project using Android Studio and then we'll launch the Chat SDK on a button click. 

You can download the project that is used in this tutorial [here](http://img.chatcatapp.com/chatsdk/SetupTutorial.zip). 

#### Creating a new project
1. Open Android Studio and click **File** -> **New** -> **New Project**
2. Select API level 15 and click **Next**
3. Select the **Basic Activity** project template
4. Click **Finish**
5. Check that you can compile the project. It should show a blank screen with a button in the bottom right

#### Adding the Chat SDK Modules

Next we are going to add the Chat SDK modules to the project. 

1. Download the Chat SDK or clone it from Github
2. Unzip the Chat SDK library and put in a convenient location
2. Go back to your new project and click **File** -> **New** -> **Import Module**
3. In the dialog navigate to the Chat SDK folder and select the **firebase_plugin** directory. Three modules will automatically be added into the dialog - sdk, country_picker and facebook
4. Click **Finish**

The modules have now been successfully imported. However, there will be a Gradle build error because we are missing some configuration. 

#### Configuring the project

Now that the modules have been added, we need to configure the project. 

**SDK Version**

Now you will see that gradle cannot be sync because it missing some parameters. Open to **gradle.properties** file in the root of the project and add the following lines.

```
MIN_SDK = 15
ANDROID_BUILD_SDK_VERSION = 23
ANDROID_BUILD_TOOLS_VERSION = 21.1.0
ANDROID_BUILD_TARGET_SDK_VERSION = 23
ANDROID_COMPILE_SDK_VERSION = 23
```

> **Note:**
>You should make sure that the correct SDK versions and build tools are installed in Android Studio. To do this open the Preferences panel and navigate to **Appearance & Behavour** -> **System Settings** -> **Android SDK** or click on the **SDK Manager** icon in the tool bar. Android SDK versions 4.4 and onwards should be installed. **Android SDK Build-Tools** version 21.1.0 should be installed. 

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
        classpath 'com.android.tools.build:gradle:2.2.0'
        classpath 'com.google.gms:google-services:3.0.0'
    }
}

allprojects {
    repositories {
        jcenter()
    }
}

task clean(type: Delete) {
    delete rootProject.buildDir
}
```
Now run Gradle to get the project ready to be compiled. The project should now compile without error. 

At this point the Chat SDK has been added correctly but we still can't access the Chat SDK code from our app. To access the code, you need to open **build.gradle (Module: app)** and add the following line to the dependencies.

```

dependencies {
    compile fileTree(dir: 'libs', include: ['*.jar'])
    androidTestCompile('com.android.support.test.espresso:espresso-core:2.2.2', {
        exclude group: 'com.android.support', module: 'support-annotations'
    })
    compile 'com.android.support:appcompat-v7:25.1.0'
    compile 'com.android.support:design:25.1.0'
    
    //*** Add this line ***
    compile project(':firebase_plugin')

    testCompile 'junit:junit:4.12'
}
```

Sync Gradle again and we are ready to launch the Chat SDK activity. 

##Launching the Chat SDK login activity

Open up your app's main activity. It should be in **App** -> **java** -> **Your app namespace** -> **Main Activity**

Add the following imports.

```
import com.braunster.androidchatsdk.firebaseplugin.firebase.BChatcatNetworkAdapter;
import com.braunster.chatsdk.Utils.helper.ChatSDKUiHelper;
import com.braunster.chatsdk.network.BDefines;
import com.braunster.chatsdk.network.BNetworkManager;
import com.braunster.chatsdk.activities.ChatSDKLoginActivity;
import android.content.Intent;
```

Next add the Chat SDK setup code to your `onCreate` method. 

```
// This is used for the app custom toast and activity transition 
ChatSDKUiHelper.initDefault(); 

// Init the network manager
BNetworkManager.init(getApplicationContext()); 

// Create a new adapter
BChatcatNetworkAdapter adapter = new BChatcatNetworkAdapter(getApplicationContext());

// Set the adapter
BNetworkManager.sharedManager().setNetworkAdapter(adapter); 
```

This code gets the Chat SDK ready to be launched. Now add the following code to launch the login activity.

```
Intent myIntent = new Intent(this, ChatSDKLoginActivity.class);
startActivity(myIntent);
```

If you want the Chat SDK to launch on a button click, you could add the following code to the button callback.

```
FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
fab.setOnClickListener(new View.OnClickListener() {
    @Override
    public void onClick(View view) {
        Intent myIntent = new Intent(MainActivity.this, ChatSDKLoginActivity.class);
        MainActivity.this.startActivity(myIntent);
    }
});

```

The final step is to make the login activity available by adding it to our app manifest. Open the file **App** -> **manifests** -> **Android Manifest.xml** and add the following code below any existing activities that may be setup. 

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

```

Now run the app and you will see the Chat SDK login screen open when the app launches. 

If you have any problems Firebase offer good [documentation](https://www.firebase.com/docs/android/quickstart.html) and Facebook has an [integration guide](https://developers.facebook.com/docs/android/getting-started).
