#! bin/bash

DEST="../chat-sdk-android-v5"

chatsdk=( app-firebase core core-ui demo firebase-adapter firebase-push firebase-upload mod-auto mod-message-location mod-firebase-ui mod-profile-pictures mod-ui-extras )

cp README.md $DEST 
cp LICENSE.md $DEST 

chmod -R o+rwx $DEST

for t in ${chatsdk[@]}
do


	echo $t
    rm -r chat-sdk-$t/build
	echo "$DEST/chat-sdk-$t"
    rm -r "$DEST/chat-sdk-$t"

    # rsync -a chat-sdk-$t $DEST
    cp -r chat-sdk-$t $DEST
done

guru=( common firestore realtime )

for t in ${guru[@]}
do
	# echo $t
    rm -r sdk-guru-$t/build
	echo "$DEST/sdk-guru-$t"
    rm -r "$DEST/sdk-guru-$t"

    # rm -r $DEST
    # rsync -a sdk-guru-$t $DEST
    cp -r sdk-guru-$t $DEST
done

other=( app-demo )


for t in ${other[@]}
do
	# echo $t
    rm -r $t/build
	echo "$DEST/$t"
    rm -r "$DEST/$t"
 
    # rm -r $t/build
    # rsync -a $t $DEST
    cp -r $t $DEST
done

# DEST="../firestream-android"

# chmod -R o+rwx $DEST

# for t in ${guru[@]}
# do
# 	# echo $t
#     rm -r sdk-guru-$t/build
# 	echo "$DEST/sdk-guru-$t"
#     rm -r "$DEST/sdk-guru-$t"

#     # rsync -a sdk-guru-$t $DEST
#     cp -r sdk-guru-$t $DEST
# done

# other=( firestream firestream-firestore firestream-realtime )

# for t in ${other[@]}
# do
# 	# echo $t
#     rm -r $t/build
# 	echo "$DEST/$t"
#     rm -r "$DEST/$t"

#     # rm -r $t/build
#     # rsync -a $t $DEST
#     cp -r $t $DEST
# done

echo "Done"

