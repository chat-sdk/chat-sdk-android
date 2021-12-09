# Chat SDK for Android v5
### Open Source Messaging framework for Android

![Main Image](https://media.giphy.com/media/L05Bq9WHSP3PUH6uTF/giphy.gif)

Chat SDK is a fully featured open source instant messaging framework for Android. Chat SDK is fully featured, scalable and flexible and follows the following key principles:

- **Free.** Chat SDK uses the [Apache 2.0 license](https://www.apache.org/licenses/LICENSE-2.0)
- **Open Source.** Chat SDK is open source
- **Full control of the data.** You have full and exclusive access to the user's chat data
- **Quick integration.** Chat SDK is fully featured out of the box
- **Scalable.** Supports millons of daily users [[1](https://firebase.google.com/docs/database/usage/limits), [2](https://blog.process-one.net/ejabberd-massive-scalability-1node-2-million-concurrent-users/)]
- **Backend agnostic.** Chat SDK can be customized to [support any backend](https://hackmd.io/@dyR2Vn0UTFaO8tZjyiJyHw/SJUgMoJTU) 

[![](https://raw.githubusercontent.com/chat-sdk/chat-sdk-android/master/graphics/chat-sdk-play.png)](https://play.google.com/store/apps/details?id=sdk.chat.live)

## Technical details

- **Multiple Backends Support** [Firestore](https://firebase.google.com/), [Firebase](https://firebase.google.com/), [ejabberd](https://www.ejabberd.im/), [OpenFire](https://www.igniterealtime.org/projects/openfire/), [Prosody](https://prosody.im/), [Tigase](https://tigase.net/), [MongooseIM](https://mongooseim.readthedocs.io/en/latest/)
- **Persistence -** [GreenDao](http://greenrobot.org/greendao/)
- **Reactive -** [RXAndroid](https://github.com/ReactiveX/RxAndroid)
- **Java 8** supports Java 8 and lamda expressions
- **Multi-threaded** agressively multi-threaded
- **API Level 16+** Compatible with 99.3% of Android devices
- **Demo** [Download v5.0.1](https://play.google.com/store/apps/details?id=sdk.chat.live)

> Please bear in mind that this version is a major update. As a result we are making new releases every few days to fix bugs and crashes. If you see an issue, please report it on the Github bug tracker and we will fix it. 

## Features

- Powered by Firebase Firestore, Realtime database or XMPP
- Private and group messages [⇘GIF](https://giphy.com/gifs/chat-sdk-group-chat-l10OaBC7ce7zaJKvDe)
- Public chat rooms
- Username / password, Facebook, Twitter, Anonymous and custom login
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
- [Web Version](https://github.com/chat-sdk/chat-sdk-web)

## Extras

Sponsor us on either [Github sponsors](https://github.com/sponsors/chat-sdk) or [Paetron](https://www.patreon.com/chatsdk) and get these features. For full details visit our [Modules](https://hackmd.io/@dyR2Vn0UTFaO8tZjyiJyHw/ryODENucU) page.

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
- Nearby Users
- Contact book integration [⇘GIF](https://giphy.com/gifs/TgbLHgDIwcuGX9SDuV)
- Location based chat [⇘GIF](https://giphy.com/gifs/chat-sdk-nearby-users-J5qXSwAhkjLx0Aqk4O)
- XMPP Server Support
	- ejabberd
	- Prosody
	- OpenFire
	- Tigase
	- MongooseIM

Visit our [Animated GIF Gallery](https://giphy.com/channel/chat-sdk) to see all the features.

## About Us

Learn about the history of Chat SDK and our future plans in [this post](https://hackmd.io/@dyR2Vn0UTFaO8tZjyiJyHw/BkBKhRO0I).

## Scalability and Cost

People always ask about how much Chat SDK costs to run. And will it scale to millions of users? So I wrote an article talking about [just that](https://hackmd.io/@dyR2Vn0UTFaO8tZjyiJyHw/r1LJ26d0L). 

### Library Size

The Chat SDK library with **ALL** modules is around 


## Community

+ **Discord:** If you need support, join our [Server](https://discord.gg/abT5BM4)
+ **Support the project:** [Patreon](https://www.patreon.com/chatsdk) or [Github Sponsors](https://github.com/sponsors/chat-sdk) 🙏 and get access to premium modules
+ **Upvote:** our advert on [StackOverflow](https://meta.stackoverflow.com/questions/394409/open-source-advertising-1h-2020/396154#396154)
+ **Contribute by writing code:** Email the [Contributing
Document](https://github.com/chat-sdk/chat-sdk-ios/blob/master/CONTRIBUTING.md) to [**team@sdk.chat**](mailto:team@sdk.chat)
+ **Give us a star** on Github ⭐
+ **Upvoting us:** [Product Hunt](https://www.producthunt.com/posts/chat-sdk)
+ **Tweet:** about your Chat SDK project using [@chat_sdk](https://mobile.twitter.com/chat_sdk) 
+ **Live Stream** Join us every Saturday 18:00 CEST for a live stream where I answer questions about Chat SDK. For more details please join the Discord Server 

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

#### Migrating from v4

- [Migration guide](https://hackmd.io/@dyR2Vn0UTFaO8tZjyiJyHw/SJLWjxdcL)
- If you want to see the v4 docs, they are available on the [v4 branch](https://github.com/chat-sdk/chat-sdk-android/tree/v4)

#### Quick Start

[![Video Tutorial](https://img.youtube.com/vi/ZzfSd3hc3xw/0.jpg)](https://www.youtube.com/watch?v=ZzfSd3hc3xw)

> Bear in mind that the video is not updated frequently. Please cross reference with with the [text based instructions](https://chat-sdk.gitbook.io/android/setup/add-chat-sdk-to-your-project) for the latest gradle dependencies. 

- [Full video tutorial](https://www.youtube.com/watch?v=ZzfSd3hc3xw)
- [Building a messaging app using Chat SDK](https://hackmd.io/iBIxiQ24RDiMY-W76DomfA#Building-a-messaging-app-using-Chat-SDK)
- [Adding Chat SDK to a Firebase app](https://hackmd.io/iBIxiQ24RDiMY-W76DomfA#Add-the-Chat-SDK-to-a-Firebase-app)
- [Adding Chat SDK to a non-Firebase app](https://hackmd.io/iBIxiQ24RDiMY-W76DomfA#Add-the-Chat-SDK-to-a-non-Firebase-app)

#### Integration

1. [Add the Chat SDK libraries to your project](https://hackmd.io/@dyR2Vn0UTFaO8tZjyiJyHw/B1S2tXdqL)
2. [Configure Firebase](https://hackmd.io/@dyR2Vn0UTFaO8tZjyiJyHw/BkvpPKFqI)
3. [Configure the Chat SDK](https://hackmd.io/@dyR2Vn0UTFaO8tZjyiJyHw/Hke7KN_qI)
4. [Enable Location Messages](https://hackmd.io/@dyR2Vn0UTFaO8tZjyiJyHw/rkyHX76hU)
5. [Display the login screen](https://hackmd.io/@dyR2Vn0UTFaO8tZjyiJyHw/HJzwrrO5L)
6. [Add additional module dependencies](https://hackmd.io/@dyR2Vn0UTFaO8tZjyiJyHw/Bkpy076hL)
7. [Enable and configure the modules](https://hackmd.io/@dyR2Vn0UTFaO8tZjyiJyHw/BJSBZ5t5U)
8. [Synchronize user profiles with your app](https://hackmd.io/@dyR2Vn0UTFaO8tZjyiJyHw/ByPlWV6h8)
9. [Enable token authentication](https://hackmd.io/@dyR2Vn0UTFaO8tZjyiJyHw/H18dFBRhL)

#### Customization

1. [Override Activity or Fragment](https://hackmd.io/@dyR2Vn0UTFaO8tZjyiJyHw/ByL0mWG0L)
2. [Theme Chat SDK](https://hackmd.io/@dyR2Vn0UTFaO8tZjyiJyHw/r1xStjFcU)
3. [Customize the Icons](https://hackmd.io/@dyR2Vn0UTFaO8tZjyiJyHw/r1ZeZWGCI)
4. [Customize the Tabs](https://hackmd.io/@dyR2Vn0UTFaO8tZjyiJyHw/BkClVbzAU)
5. [Add a Chat Option](https://hackmd.io/@dyR2Vn0UTFaO8tZjyiJyHw/HymyNWfA8)
6. [Custom Message Types](https://hackmd.io/@dyR2Vn0UTFaO8tZjyiJyHw/ryxEXfA08)
7. [Handling Events](https://hackmd.io/@dyR2Vn0UTFaO8tZjyiJyHw/rJFli-GRL)
8. [Custom Push Handling](https://hackmd.io/@dyR2Vn0UTFaO8tZjyiJyHw/rywUHXikv)

#### Extras

1. [Example Firebase Schema](https://hackmd.io/@dyR2Vn0UTFaO8tZjyiJyHw/Bk9i0lUJv)

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
- [Firebase vs. Firestream vs. XMPP](https://hackmd.io/@dyR2Vn0UTFaO8tZjyiJyHw/Sk8-jryC8)

#### Setup Service

We provide extensive documentation on Github but if you’re a non-technical user or want to save yourself some work you can take advantage of our [setup and integration service](http://chatsdk.co/downloads/chat-sdk-setup-service/).
