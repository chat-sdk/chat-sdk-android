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
    // if (messageType === 7) {
    //     body = "File";
    // }

    let data = {
        chat_sdk_thread_entity_id: threadId,
        chat_sdk_user_entity_id: senderId,
        chat_sdk_push_title: title,
        chat_sdk_push_body: body,
    };

    // Make the user ID safe
    recipientId = recipientId.split(".").join("1");
    recipientId = recipientId.split("%2E").join("1");
    recipientId = recipientId.split("@").join("2");
    recipientId = recipientId.split("%40").join("2");
    recipientId = recipientId.split(":").join("3");
    recipientId = recipientId.split("%3A").join("3");

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
            let name = metaValue["name"];
            if(!name || name === "undefined" || name === null) {
                name = "No Name";
            }
            return name;
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


exports.pushToChannels = functions.https.onCall((data, context) => {
//  exports.pushToChannels = functions.https.onRequest((req, res) => {

    let body = data.body;

    let action = data.action;
    if(!action) {
        action = iOSAction;
    }

    let sound = data.sound;
    if(!sound) {
        sound = Sound;
    }

    let type = data.type;
    let senderId = String(data.senderId);
    let threadId = String(data.threadId);

    let userIds = data.userIds;

    if(senderId === "undefined" || !senderId || senderId === null) {
        throw new functions.https.HttpsError("invalid-argument", "Sender ID not valid");
    }

    if(threadId === "undefined" || !threadId || threadId === null) {
        throw new functions.https.HttpsError("invalid-argument", "Sender ID not valid");
    }

    if(body === "undefined" || !body || body === null) {
        throw new functions.https.HttpsError("invalid-argument", "Sender ID not valid");
    }

    var status = {};
    for(let uid in userIds) {
        if(userIds.hasOwnProperty(uid)) {
            let userName = userIds[uid];
            let message = buildMessage(userName, body, action, sound, type, senderId, threadId, uid);
            status[uid] = message;
            admin.messaging().send(message);
        }
    }
    // return Promise.all(promises);
    // return res.send(status);
    return status;

});

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