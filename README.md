# Chat SDK for Android v5
### Open Source Messaging framework for Android

![Main Image](https://media.giphy.com/media/L05Bq9WHSP3PUH6uTF/giphy.gif)

Chat SDK is a fully featured open source instant messaging framework for Android. Chat SDK is fully featured, scalable and flexible and follows the following key principles:

- **Free.** 
- **Open Source.** 
- **Full control of the data.** You have full and exclusive access to the user's chat data
- **Quick integration.** Chat SDK is fully featured out of the box
- **Scalable.** Supports millons of daily users [[1](https://firebase.google.com/docs/database/usage/limits), [2](https://blog.process-one.net/ejabberd-massive-scalability-1node-2-million-concurrent-users/)]
- **Backend agnostic.** Supports Firebase and XMPP (ejabberd, Prosody, MongooseIM, Tigase, OpenFire)

[![](https://raw.githubusercontent.com/chat-sdk/chat-sdk-android/master/graphics/chat-sdk-play.png)](https://play.google.com/store/apps/details?id=sdk.chat.live)

## Technical details

- **Multiple Backends Support** [Firestore](https://firebase.google.com/), [Firebase](https://firebase.google.com/), [ejabberd](https://www.ejabberd.im/), [OpenFire](https://www.igniterealtime.org/projects/openfire/), [Prosody](https://prosody.im/), [Tigase](https://tigase.net/), [MongooseIM](https://mongooseim.readthedocs.io/en/latest/)
- **Persistence -** [GreenDao](http://greenrobot.org/greendao/)
- **Reactive -** [RXAndroid](https://github.com/ReactiveX/RxAndroid)
- **Java 8** supports Java 8 and lamda expressions
- **Multi-threaded** agressively multi-threaded
- **API Level 23+** 
- **Demo** [Google Play Store](https://play.google.com/store/apps/details?id=sdk.chat.live)

> Please bear in mind that this version is a major update. As a result we are making new releases every few days to fix bugs and crashes. If you see an issue, please report it on the Github bug tracker and we will fix it. 

## Features

- Powered by Firebase Firestore, Realtime database or XMPP
- Private and group messages [⇘GIF](https://giphy.com/gifs/chat-sdk-group-chat-l10OaBC7ce7zaJKvDe)
- Public chat rooms
- Username / password, Social, Anonymous and custom login
- Phone number authentication
- Push notifications (using FCM)
- Text, Image [⇘GIF](https://giphy.com/gifs/chat-sdk-image-message-MXLfUgTh3LFjVzC1BV) and Location [⇘GIF](https://giphy.com/gifs/chat-sdk-location-message-gM0wVTbTnG0H8JQuBS) messages
- Forward, Reply [⇘GIF](https://giphy.com/gifs/hQpGyo24gxYFqLPj2E), Copy and Delete [⇘GIF](https://giphy.com/gifs/iD616avkpifElZ6IRl) messages
- Tabbar [⇘GIF](https://giphy.com/gifs/chat-sdk-tabbar-ln715cYWiX9yYVEkCm) or Drawer [⇘GIF](https://giphy.com/gifs/eNRDygZZ7q9n4Yqk3b) layout
- User Profiles [⇘GIF](https://giphy.com/gifs/profile-chat-sdk-UVZIcvzSjBy6ZrJq7E)
- User Search [⇘GIF](https://giphy.com/gifs/search-chat-sdk-ducLm14OeuX0pUzVEl)
- Contacts [⇘GIF](https://giphy.com/gifs/profile-chat-sdk-UVZIcvzSjBy6ZrJq7E)
- Add contact by QR code [⇘GIF](https://giphy.com/gifs/jOnq7hTrHMFEHJDbim)
- Firebase UI [⇘GIF](https://giphy.com/gifs/chat-sdk-firebase-ui-hrps78wBSz49QXbuv3)
- [iOS Version](https://github.com/chat-sdk/chat-sdk-ios)

## Pro Features

If you are an open source project using GPLv3 you can use all the features with that license. 

For commercial projects, sponsor us on either [Github sponsors](https://github.com/sponsors/chat-sdk) or [Paetron](https://www.patreon.com/chatsdk) and get these features. For full details visit our [Modules](https://chat-sdk.gitbook.io/chat-sdk/commercial/module-licensing) page.

When you support us on Patreon, you get: **extra modules**, **code updates**, **support** as well as special access to the Discord Server. 

- Typing indicator [⇘GIF](https://giphy.com/gifs/typing-chat-sdk-KxcLVS0IFrRtsM2OjR)
- Read receipts
- Last online indicator
- Audio messages [⇘GIF](https://giphy.com/gifs/hQPw2GZ7dXKlnW8gBb)
- Video messages [⇘GIF](https://giphy.com/gifs/chat-sdk-video-message-U72VXhWW9wIdMcRX4D)
- Sticker messages [⇘GIF](https://giphy.com/gifs/chat-sdk-LmlI3CJtrHhhTkVGAY)
- User blocking [⇘GIF](https://giphy.com/gifs/blocking-chat-sdk-SSiqIHMBddhbyt5US9)
- File Messages [⇘GIF](https://giphy.com/gifs/chat-sdk-file-message-ihAaHtT8POJElt47A7)
- End-to-end encryption
- Contact book integration [⇘GIF](https://giphy.com/gifs/TgbLHgDIwcuGX9SDuV)
- Location based chat [⇘GIF](https://giphy.com/gifs/chat-sdk-nearby-users-J5qXSwAhkjLx0Aqk4O)
- XMPP Server Support
	- ejabberd
	- Prosody
	- OpenFire
	- Tigase
	- MongooseIM

Visit our [Animated GIF Gallery](https://giphy.com/channel/chat-sdk) to see all the features.

## License

This project is covered by multiple different licenses. Use the flowchart to determine which license you can use. 

<img src="https://github.com/chat-sdk/files/blob/main/ios/license-flowchart.png?raw=true" width="400" />

### Standard modules:

- app
- app-demo
- app-firestream
- chat-sdk-app-firebase
- chat-sdk-app-firestream
- chat-sdk-core
- chat-sdk-core-ui
- chat-sdk-demo
- chat-sdk-firebase-adapter
- chat-sdk-firebase-push
- chat-sdk-firebase-upload
- chat-sdk-firestream-adapter
- chat-sdk-mod-auto
- chat-sdk-mod-firebase-ui
- chat-sdk-mod-image-editor
- chat-sdk-mod-message-location
- chat-sdk-mod-ui-extras
- firestream
- firestream-firestore
- firestream-realtime
- sdk-guru-common
- sdk-guru-firestore
- sdk-guru-realtime
- vendor-chatkit

#### License:  

| Monthly Active Users  |  |
|---|---|
| Less than 1 million  | [Apache 2.0](https://www.apache.org/licenses/LICENSE-2.0)  |
| More than 1 million  | [GPLv3](https://www.gnu.org/licenses/gpl-3.0.en.html) or [Commercial License](https://github.com/chat-sdk/chat-sdk-android#commercial-license) |

### Pro modules:

- app-xmpp
- chat-sdk-app-xmpp
- chat-sdk-pro-contact-book
- chat-sdk-pro-encryption
- chat-sdk-pro-firebase-blocking
- chat-sdk-pro-firebase-last-online
- chat-sdk-pro-firebase-nearby-users
- chat-sdk-pro-firebase-read-receipts
- chat-sdk-pro-firebase-typing-indicator
- chat-sdk-pro-firestream-blocking
- chat-sdk-pro-firestream-read-receipts
- chat-sdk-pro-firestream-typing-indicator
- chat-sdk-pro-message-audio
- chat-sdk-pro-message-file
- chat-sdk-pro-message-sticker
- chat-sdk-pro-message-video
- chat-sdk-pro-xmpp-adapter
- chat-sdk-pro-xmpp-omemo
- chat-sdk-pro-xmpp-read-receipts

#### License:  

| Monthly Active Users  |  |
|---|---|
| Any  | [GPLv3](https://www.gnu.org/licenses/gpl-3.0.en.html) or [Commercial License](https://github.com/chat-sdk/chat-sdk-android#commercial-license) |

## Commercial Licensing

For commercial licensing, you have several options: 

1. Pay monthly: [Patreon](https://www.patreon.com/chatsdk) or [Github Sponsors](https://github.com/sponsors/chat-sdk)
2. [Buy a one-off license](https://chatsdk.co/features)
2. If your project has over 1m MAU or XMPP contact [team@chatsdk.co](mailto: team@chatsdk.co)

## About Us

Learn about the history of Chat SDK and our future plans in [this post](https://chat-sdk.gitbook.io/chat-sdk/about-us/history-of-chat-sdk).

## Scalability and Cost

People always ask about how much Chat SDK costs to run. And will it scale to millions of users? So I wrote an article talking about [just that](https://chat-sdk.gitbook.io/chat-sdk/commercial/scalability-and-cost). 

### Library Size

The Chat SDK library with **ALL** modules is around 20mb

## Community

+ **Discord:** If you need support, join our [Server](https://discord.gg/abT5BM4)
+ **Support the project:** [Patreon](https://www.patreon.com/chatsdk) or [Github Sponsors](https://github.com/sponsors/chat-sdk) 🙏 and get access to premium modules
+ **Contribute by writing code:** Email the [Contributing
Document](https://github.com/chat-sdk/chat-sdk-ios/blob/master/CONTRIBUTING.md) to [**team@sdk.chat**](mailto:team@sdk.chat)
+ **Give us a star** on Github ⭐
+ **Upvoting us:** [Product Hunt](https://www.producthunt.com/posts/chat-sdk)
+ **Tweet:** about your Chat SDK project using [@chat_sdk](https://mobile.twitter.com/chat_sdk) 

You can also help us by:

+ Providing feedback and feature requests
+ Reporting bugs
+ Fixing bugs
+ Writing documentation

Email us at: [team@sdk.chat](mailto:team@sdk.chat)

We also offer development services we are a team of full stack developers who are Firebase experts.
For more information check out our [consulting site](https://chat-sdk.github.io/hire-us/). 

### Firestream - A light-weight messaging library for Firebase

If you are looking for something that is more-light weight than Chat SDK, we also have a library which only provides instant messaging functionality. 

1. 1-to-1 Messaging
2. Group chat, roles, moderation
3. Android, iOS, Web and Node.js
2. Fully customisable messages
3. Typing Indicator
4. Delivery receipts
5. User blocking
6. Presence
7. Message history (optional)
7. **Firestore** or **Realtime** database

You can check out the project: [Firestream on Github](https://github.com/chat-sdk/firestream-android). 

## Chat SDK Firebase Documentation

#### Quick Start

[![Video Tutorial](https://img.youtube.com/vi/ZzfSd3hc3xw/0.jpg)](https://www.youtube.com/watch?v=ZzfSd3hc3xw)

> Bear in mind that the video is not updated frequently. Please cross reference with with the [text based instructions](https://chat-sdk.gitbook.io/android/setup/add-chat-sdk-to-your-project) for the latest gradle dependencies. 

- [Full video tutorial](https://www.youtube.com/watch?v=ZzfSd3hc3xw)
- [API - Useful for Chat SDK customization](https://chat-sdk.gitbook.io/chat-sdk/guides/api-cheatsheet)
- [Documentation Homepage](https://chat-sdk.gitbook.io/chat-sdk/)
- [Building a messaging app using Chat SDK](https://chat-sdk.gitbook.io/android/getting-started/chat-sdk-quickstart)
- [Adding Chat SDK to a Firebase app](https://chat-sdk.gitbook.io/android/getting-started/getting-started)
- [Adding Chat SDK to a non-Firebase app](https://chat-sdk.gitbook.io/android/getting-started/add-the-chat-sdk-to-a-non-firebase-app)

#### Integration

1. [Add the Chat SDK to your project](https://chat-sdk.gitbook.io/android/setup/add-chat-sdk-to-your-project)
2. [Firebase Setup](https://chat-sdk.gitbook.io/android/setup/untitled)
3. [Chat SDK Initialization](https://chat-sdk.gitbook.io/android/setup/chat-sdk-initialization)
4. [Set the Chat SDK Theme](https://chat-sdk.gitbook.io/android/setup/set-the-chat-sdk-theme)
5. [Enable Location Messages](https://chat-sdk.gitbook.io/android/setup/enable-location-messages)
6. [Display the login screen](https://chat-sdk.gitbook.io/android/setup/authentication-screen)
7. [Add module dependencies](https://chat-sdk.gitbook.io/android/setup/add-additional-modules)
8. [Module Configuration](https://chat-sdk.gitbook.io/android/setup/module-configuration)
9. [Proguard](https://chat-sdk.gitbook.io/android/setup/proguard)
10. [Push Notifications, Security Rules and Storage](https://github.com/chat-sdk/chat-sdk-firebase)

#### Customization

1. [Override Activity or Fragment](https://chat-sdk.gitbook.io/android/api/overriding-activities-and-fragments)
2. [Theme Chat SDK](https://chat-sdk.gitbook.io/android/api/theming)
3. [Customize the Icons](https://chat-sdk.gitbook.io/android/api/customizing-the-icons)
4. [Customize the Tabs](https://chat-sdk.gitbook.io/android/api/tab-customization)
5. [Add a Chat Option](https://chat-sdk.gitbook.io/android/api/add-a-chat-option)
6. [Custom Message Types](https://chat-sdk.gitbook.io/android/api/message-customization)
7. [Handling Events](https://chat-sdk.gitbook.io/android/api/events)
8. [Custom Push Handling](https://chat-sdk.gitbook.io/android/api/overriding-the-push-notification-handler)
8. [Synchronize user profiles with your app](https://chat-sdk.gitbook.io/android/api/integrating-chat-sdk-user-profiles-with-your-app)
9. [Custom File Upload Handler](https://chat-sdk.gitbook.io/chat-sdk/guides/custom-file-upload-handler)
10. [Enable token authentication](https://chat-sdk.gitbook.io/chat-sdk/custom-token-authentication)

#### Extras

1. [Example Firebase Schema](https://chat-sdk.gitbook.io/chat-sdk/guides/firebase-schema)

#### Migrating from v4

- [Migration guide](https://chat-sdk.gitbook.io/android/chat-sdk-v4/updating-from-v4-to-v5)
- If you want to see the v4 docs, they are available on the [v4 branch](https://github.com/chat-sdk/chat-sdk-android/tree/v4)

<!--
#### Customization

- Chat SDK Configuration 
- UI Customization
- Using Chat SDK UI components
- Customizing Chat SDK UI Components
- Custom Authentication using token
- Chat SDK API

-->

#### Recommended background

- [RxJava for beginners](https://medium.com/@factoryhr/understanding-java-rxjava-for-beginners-5eacb8de12ca)
- [API Examples](https://github.com/chat-sdk/chat-sdk-android/tree/master/chat-sdk-demo/src/main/java/sdk/chat/demo/examples/api)
- [Firebase vs. Firestream vs. XMPP](https://chat-sdk.gitbook.io/chat-sdk/commercial/firebase-vs.-firestream-vs.-xmpp)

#### Setup Service

We provide extensive documentation on Github but if you’re a non-technical user or want to save yourself some work you can take advantage of our [setup and integration service](http://chatsdk.co/downloads/chat-sdk-setup-service/).
