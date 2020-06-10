#! bin/bash

DEST="../chat-sdk-android-v5/"

chatsdk=( app-firebase app-demo core core-ui demo firebase-adapter firebase-push firebase-upload mod-auto mod-firebase-ui mod-profile-pictures mod-ui-extras )

for t in ${chatsdk[@]}
do
    rm -r chat-sdk-$t/build
    # rsync -a chat-sdk-$t $DEST
    cp -r chat-sdk-$t $DEST
done

guru=( common firestore realtime )


for t in ${guru[@]}
do
	echo $t
    rm -r sdk-guru-$t/build
    # rsync -a chat-sdk-$t $DEST
    cp -r sdk-guru-$t $DEST
done

cp README.md $DEST 
cp LICENSE.md $DEST 
