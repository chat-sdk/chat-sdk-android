package co.chatsdk.xmpp;

/**
 * Created by kykrueger on 2016-10-23.
 */

public class XmppThreadsManager {
//
//    XmppServiceConnection xmppServiceConnection;
//
//    public XmppThreadsManager(Context context){
//
//        xmppServiceConnection = new XmppServiceConnection(context);
//    }
//
//    public Promise<Void, BError, Void> deleteThread(BThread thread) {
//        return super.deleteThread(thread);
//    }
//
//    @Override
//    public Promise<Void, BError, Void> deleteThreadWithEntityID(String entityID) {
//        return null;
//    }
//
//    @Override
//    public Deferred<BMessage, BError, BMessage> sendMessage(BMessage message, Deferred<BMessage, BError, BMessage> deferred) {
//        return super.sendMessage(message, deferred);
//    }
//
//    @Override
//    public Promise<BMessage, BError, BMessage> sendMessage(final BMessage message) {
//        final Deferred<BMessage, BError, BMessage> deferred = new DeferredObject<>();
//
//        xmppServiceConnection.getConnection().observeOn(Schedulers.single()).doOnNext(new Consumer<AbstractXMPPConnection>() {
//            @Override
//            public void accept(AbstractXMPPConnection abstractXMPPConnection) throws Exception {
//                ChatManager chatManager = ChatManager.getInstanceFor(abstractXMPPConnection);
//                Chat chat = chatManager.getThreadChat(message.getThread().getEntityID());
//                chat.sendMessage(convertToXmppMessage(message));
//            }
//        }).doOnComplete(new Action() {
//            @Override
//            public void run() throws Exception {
//                deferred.resolve(message);
//            }
//        });
//
//        return deferred.promise();
//    }
//
//    @Override
//    public Promise<BThread, BError, Void> addUsersToThread(BThread thread, List<BUser> users) {
//        return null;
//    }
//
//    @Override
//    public Promise<BThread, BError, Void> addUsersToThread(BThread thread, BUser... users) {
//        return super.addUsersToThread(thread, users);
//    }
//
//    @Override
//    public Promise<BThread, BError, Void> createPublicThreadWithName(String name) {
//        return null;
//    }
//
//    @Override
//    public Promise<BThread, BError, Void> createThreadWithUsers(String name, List<BUser> users) {
//        return null;
//    }
//
//    @Override
//    public Promise<BThread, BError, Void> createThreadWithUsers(String name, BUser... users) {
//        return super.createThreadWithUsers(name, users);
//    }
//
//    @Override
//    public Promise<List<BMessage>, Void, Void> loadMoreMessagesForThread(BThread thread) {
//        return null;
//    }
//
//    @Override
//    public Promise<BThread, BError, Void> pushThread(BThread thread) {
//        return null;
//    }
//
//    @Override
//    public Promise<BThread, BError, Void> removeUsersFromThread(BThread thread, List<BUser> users) {
//        return null;
//    }
//
//    @Override
//    public Promise<BThread, BError, Void> removeUsersFromThread(BThread thread, BUser... users) {
//        return super.removeUsersFromThread(thread, users);
//    }
//
//
//    private Message convertToXmppMessage(BMessage message){
//        Message xmppMessage = new Message();
//        xmppMessage.setBody(message.getText());
//        return xmppMessage;
//    }


}
