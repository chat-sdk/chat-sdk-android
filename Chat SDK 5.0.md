# Chat SDK 5.0

- Add support for FireStream (Firestore or Realtime database)
- Replaced ChatActivity implementation with ChatKit
- Removed social login module - you can use FirebaseUI if you need social login
- Added multi-message select
- Add message replies
- Allow multiple images to be sent at once
- Streamline image selection using Matisse
- Make Firebase UI the default interface
- Replace Fresco with Picasso
- Removed Social Login

- Upgraded Audio message module to use ExoPlayer
- Audio message module supports file compression

## FireStream - Firestore support incoming

FireStream is a new message transmission system which will replace the Firebase Network Adapter. FireStream is a stand-alone cross platform messaging library for Firebase Firestore and Firebase Realtime Database. 

### API Changes

#### ChatActivity

The ChatActivity is being deprecated in favour of a ChatKit implementation. 

#### Hooks

Before:

```
ChatSDK.hook().addHook(new Hook(data -> Completable.create(emitter -> {
    // ...
    emitter.onComplete();
})), HookEvent.MessageReceived);
```

After:

If you are adding synchronous code:

```
ChatSDK.hook().addHook(Hook.sync(data -> {
    // ....
}), HookEvent.MessageReceived);
```
Or asynchronous code:

```
ChatSDK.hook().addHook(Hook.async(data -> Completable.create(emitter -> {
    // ... Async code here
    emitter.onComplete();
})), HookEvent.MessageReceived);
```

#### Thread Handler

Before:

```
Completable forwardMessage(Message message, Thread thread);
```
After:

```
Completable forwardMessage(Thread thread, Message message);
```

### Why did you remove social login?

Social login is complicated and error prone. We have had a lot of issues with third party login SDKs that change their API every few months and generate build problems with CocoaPods (Facebook and Twitter I'm looking at both of you). It also requires a lot of instructions to setup properly and the steps are always changing. For this reason, supporting social login takes a lot of time for relatively little benefit. Especially since this functionality is provided by Firebase UI. 

So the bottom line is that if you want Social Login, you can either use Firebase UI. If you need custom social login, I recomment that you look at version 4.x of the project and migrate it to version 5. 


#### Can I just use some of the views?

yes


#### Bundles

- Message Bundle - audio, video, sticker, contact, file, snapschat
- Encryption
- Basics - read receipts, typing indicator, last online 




