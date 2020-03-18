# FireStream for Firebase
> A lightweight Firebase messaging library from Chat SDK

## Features

### FireStream Core

1. 1-to-1 Messaging
2. Group chat, roles, moderation
3. Android, iOS, Web and Node.js
2. Fully customisable messages
3. Typing Indicator
4. Delivery receipts
5. User blocking
6. Presence
7. Message history (optional)
7. Firestore or Realtime database

- Apache 2.0 License
- Commercial use allowed
- Support via Github bug tracker

FireStream supports both **Firestore** and the **Realtime** database. You pay the hosting cost directly to Firebase and have sole access to your data. 

There are many chat systems available, here are the benefits of FireStream:

1. Open Souce - you can review, fork, optimize and audit the code
2. Transparent - all the "server" code is included in the client
3. Full Control - you control the source, you have sole access to the data
4. Powered by Firebase - for a managed solution, it's the best

### Status

1. FireStream Android - beta
2. Chat SDK FireStream Android - beta
3. FireStream Web - beta
4. Chat SDK FireStream Web - In development
5. FireStream iOS - beta
6. Chat SDK FireStream iOS - In development

|Platform | FireStream | Chat SDK FireStream
|---|---|---|
| Android | Beta | Beta |
| iOS | Beta | In development |
| Web | Beta | In development |

Firebase benefits:

1. Low cost, excellent performance and reliability 
2. Reasonable data usage policy
3. Google isn't going out of business any time soon
4. You have full control over YOUR users' data

### Steps:

1. Chose your backend server: 
  1. Firebase Realtime
  2. Firebase Firestore
  3. XMPP 
2. Choose your platform
  1. iOS
  2. Android
  3. Web
  4. All
3. Choose your license type:
  1. Single app 	
  2. Multi app
  3. Enterprise


### XMPP

- Binary only: $19.99
- Source: $49.99

### FireStream+

To support the project, we offer optional extras for FireStream. 

1. Location, Audio, Video, Image, Sticker, File, Contact messages
2. Self-destructing messages
3. Message forwarding
4. Message replies
3. REST API

- 1 app: $2.49 per month
- 2 apps: $4.99 per month
- Unlimited apps: $9.99 per month

### FireStream Pro

With FireStream Pro you get everything from FireStream+ as well as the following:

1. End-to-end encryption
2. Admin panel / moderation
3. Bot integration
4. Email Support
5. Automatic translation

- 1 app: $9.99 per month
- 2 apps: $19.99 per month
- Unlimited apps: $39.99 per month

### FireStream Enterprise

1. Enhanced support via Slack or Skype
2. Custom developments 
3. Support with configuration and deployment

- Custom pricing availilable. Contact team@sdk.chat

## Performance and Hosting

FireStream can run on either Firestore or the Realtime Database. 

### Firestore 

*Performance and scalability:*

1. 1 million concurent users ~ 50 million MAU
2. Messages arrive in near realtime

For more details [Firestore usage and limits](https://firebase.google.com/docs/firestore/quotas).

*Hosting Cost:*

1. Free
  1. Up to 50 million monthly users
  2. 600k messages 	
2. $25 per month
  1. Up to 50 million monthly users
  2. 3 million messages 
3. Pay as you go
  1. Up to 50 million monthly users
  2. $1 per 400k messages 

For more details [Firestore pricing](https://firebase.google.com/pricing).

### Realtime database 

*Performance and scalability:*

1. 200k concurrent users ~ 10 million monthly users
2. Messages arrive in realtime

For more details [Realtime database limits](https://firebase.google.com/docs/database/usage/limits).

*Hosting Cost:*

1. Free
  1. Up to 5k monthly users
  2. 50 million messages
2. $25 per month
  1. Up to 10 million monthly users
  2. 100 million messages
3. Pay as you go
  1. Up to 10 million monthly users
  2. $1 per 5 million messages 

For more details [Firebase Pricing Page](https://firebase.google.com/pricing).

### Which is better? 

There is no definitive answer to this. Each comes with it own unique set of strenghts and weaknesses. But here is a general guide. 

Firestore is better for:

- Apps with more than 10 million monthly users
- Apps where realtime latency isn't critical
- Apps were a lot of users send few messages

Firebase is better for:

- Apps where realtime latency is critical
- Apps where a few users send a lot of messages

### Plan

- Simplify: 

1. Remove messaging view and use a standard library
2. Split messaging from user management
3. 
  
### Should I upgrade to FireStream

FireStream has a number of advantages over the old Firebase Network Adapter code. 

1. FireStream is an independent library and can be used with or without Chat SDK
2. FireStream has a more efficent architecture, it's quicker and uses less data
3. FireStream can work seamlessly with your own user management system
4. FireStream supports both Firestore and the Realtime database
5. FireStream has improved security
6. The old system will be deprecated so for new installations, FireStream is highly recommended
  
### Setup

Micro Chat SDK is simple and easy to setup:



Initialize the SDK

```
Fire.api().initialize();
```

The SDK will automatically add and remove listeners when your Firebase user logs in and out. 

When a user is authenticated you can send a message:

```
Fire.api().sendMessageWithText("toUid", "text").subscribe((messageId, throwable) -> {
            
});
```

>Note:
>We use the RxAndroid library. This means that any asynchronous tasks are activated lazily, you need to call `subscribe` for the action to be executed.

To receive a message add a listener:

```
Fire.api().messageStream.subscribe(message -> {
    String fromUid = message.getFromUid();
    HashMap<String, Object> body = message.getBody();
});
```

### Disposing of Disposables

Whenever you call subscribe, you get a disposable. This is used to remove the observer. Managing these disposables can be tricky because you want to keep the references around until the time when you want to dispose of them. 

FireStream has two helpful facilities to handles this:

1. Let the framework manage them for you:

```
Fire.api().manage(disposable);
``` 

```
chat.manage(disposable);
```

If you ask Firestream to manage a disposable, it will store the reference and then call dispose when the chat disconnects. This usually happens when the user logs out. The same goes for a group chat, if the user leaves the chat, the client will disconnect and the disposables will be disposed. 

2. Manage them yourself:

```
DisposableMap dm = new DisposableMap();

// Add
dm.add(disposable);

// Or add with a key
dm.put(chat.id, disposable);

// Then call to dispose of the default list
dm.dispose();

// Dispose of disposables associated with this key
dm.dispose(key);

// Dispose of all disposables
dm.disposeAll();
```

### Calling the library

There are multiple ways you can invoke FireStream. They are provided for convenience and all do the same thing:

```
// Standard
FireStream.shared().sendMessage(...)

// Shorter
Fire.stream().sendMessage(...)

// To access non-public methods
Fire.privateApi().getConfig()...
```

### FAQ

#### Is FireStream compatible with Chat SDK

Yes, if you enable the FireStreamNetworkAdapter your app will simply start to use FireStream for message delivery. FireStream doesn't manage users so it continues to rely on the FirebaseNetworkAdapter for user profiles and search. 

#### My app is already live, can I switch over without disruption?

#### I have purchased modules for Chat SDK, do they work with FireStream?

Yes, the modules will continue to work as before. 












