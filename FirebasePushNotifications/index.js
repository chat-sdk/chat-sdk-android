'use strict';

const functions = require('firebase-functions');
const admin = require('firebase-admin');

admin.initializeApp();

let Sound = "default";
let iOSAction = "co.chatsdk.QuickReply";

function buildMessage (title, theBody, clickAction, sound, type, senderId, threadId, recipientId) {
    // Make the token payload
    let body = theBody;

    let messageType = parseInt(type);
    if (messageType === 1) {
        body = "Location";
    }
    if (messageType === 2) {
        body = "Image";
    }
    if (messageType === 3) {
        body = "Audio";
    }
    if (messageType === 4) {
        body = "Video";
    }
    if (messageType === 5) {
        body = "System";
    }
    if (messageType === 6) {
        body = "Sticker";
    }

    let data = {
        chat_sdk_thread_entity_id: threadId,
        chat_sdk_user_entity_id: senderId,
        chat_sdk_push_title: title,
        chat_sdk_push_body: body,
    };

    return {
        data: data,
        apns: {
            headers: {
            },
            payload: {
                aps: {
                    alert: {
                        title: title,
                        body: body
                    },
                    badge: 1,
                    sound: sound,
                    priority: "high",
                    category: clickAction,
                }
            },
        },
        topic: recipientId,
    };
}

function getUserName(usersRef, userId) {
    return usersRef.child(userId).child('meta').once('value').then((meta) => {
        let metaValue = meta.val();
        if (metaValue !== null) {
            return metaValue["name"];
        }
        return null;
    });
}

 function getUserIds (threadRef, senderId) {
    return threadRef.child('users').once('value').then((users) => {

        let IDs = [];

        let usersVal = users.val();
        if (usersVal !== null) {
            for (let userID in usersVal) {
                if (usersVal.hasOwnProperty(userID)) {
                    if (userID !== senderId) {
                        IDs.push(userID);
                    }
                }
            }
        }

        return IDs;
    });
}

// exports.date = functions.https.onRequest((req, res) => {
//
//     let messageValue = {
//         date: 1535358858965,
//         json_v2: {
//             text: "Hi"
//         },
//         payload: "Hi",
//         type: 0,
//         "user-firebase-id": "NX6mGPCkR3NcNiJJxBzNrsR2Anz2"
//     };
//     let threadId = "-LMnYRXqnV5i07LygqqR";
//
//     let threadRef = admin.database().ref('18_08_test_5/threads/' + threadId);
//     let usersRef = admin.database().ref('18_08_test_5/users/');
//
//     let senderId = messageValue["user-firebase-id"];
//
//
//     return getUserName(usersRef, senderId).then((name) => {
//         let senderId = messageValue["user-firebase-id"];
//         return getUserIds(threadRef, senderId).then((IDs) => {
//             let promises = [];
//             for(let userId in IDs) {
//                 if (IDs.hasOwnProperty(userId)) {
//                     let message = buildMessage(
//                         name, // Name
//                         messageValue["payload"], // Body
//                         iOSAction,
//                         Sound,
//                         messageValue["type"],
//                         senderId,
//                         threadId,
//                         userId,
//                     );
//                     admin.messaging().send(message);
//                 }
//             }
//             // return res.status(200).send(IDs);
//             return res.status(200).send("Success");
//         });
//     });
// });

exports.pushListener = functions.database.ref('{rootPath}/threads/{threadId}/messages/{messageId}').onCreate((messageSnapshot, context) => {

    let messageValue = messageSnapshot.val();
    let senderId = messageValue["user-firebase-id"];

    let pushRef = admin.database().ref(context.params.rootPath).child("push-test");

    let threadId = context.params.threadId;
    let rootPath = context.params.rootPath;

    let threadRef = admin.database().ref(rootPath).child("threads").child(threadId);
    let usersRef = admin.database().ref(rootPath).child("users");

    return getUserName(usersRef, senderId).then((name) => {

        let senderId = messageValue["user-firebase-id"];
        return getUserIds(threadRef, senderId).then((IDs) => {

            for(let key in IDs) {
                if (IDs.hasOwnProperty(key)) {
                    let userId = IDs[key];
                    let message = buildMessage(name, messageValue["payload"], iOSAction, Sound, messageValue['type'], senderId, threadId, userId);
                    admin.messaging().send(message).then(success => {
                        return success;
                    }).catch(error => {
                        console.log(error.message);
                    });
                }
            }
            return 0;
        });
    });

});