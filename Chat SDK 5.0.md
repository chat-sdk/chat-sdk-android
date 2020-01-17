# Chat SDK 5.0

- Replaced ChatActivity implementation with ChatKit
- Removed social login module - you can use FirebaseUI if you need social login
- Add support for FireStream (Firestore and Realtime database)
- Added multi-message select
- Added message replies

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


