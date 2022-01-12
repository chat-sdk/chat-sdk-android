package sdk.chat.dcom;

import sdk.chat.core.dao.Thread;
import sdk.chat.ui.fragments.ChatFragment;

public class DComChatFragment extends ChatFragment {
    public DComChatFragment(Thread thread, Delegate delegate) {
        super(thread, delegate);
    }

//    @Override
//    public void onCreateOptionsMenu(@androidx.annotation.NonNull Menu menu, @androidx.annotation.NonNull MenuInflater inflater) {
//
//        if (thread != null) {
//            if (!chatView.getSelectedMessages().isEmpty()) {
//
//                chatActionBar.hideSearchIcon();
//
//                inflater.inflate(R.menu.activity_chat_actions_menu, menu);
//
//                if (getActivity() != null) {
//                    menu.findItem(R.id.action_copy).setIcon(Icons.get(getActivity(), Icons.choose().copy, Icons.shared().actionBarIconColor));
//                    menu.findItem(R.id.action_delete).setIcon(Icons.get(getActivity(), Icons.choose().delete, Icons.shared().actionBarIconColor));
//                    menu.findItem(R.id.action_forward).setIcon(Icons.get(getActivity(), Icons.choose().forward, Icons.shared().actionBarIconColor));
//                    menu.findItem(R.id.action_reply).setIcon(Icons.get(getActivity(), Icons.choose().reply, Icons.shared().actionBarIconColor));
//                }
//
//                if (!UIModule.config().messageForwardingEnabled) {
//                    menu.removeItem(R.id.action_forward);
//                }
//
//                if (!UIModule.config().messageReplyEnabled) {
//                    menu.removeItem(R.id.action_reply);
//                }
//
//                if (chatView.getSelectedMessages().size() != 1) {
//                    menu.removeItem(R.id.action_reply);
//                }
//
//                if (!hasVoice(ChatSDK.currentUser())) {
//                    menu.removeItem(R.id.action_reply);
//                    menu.removeItem(R.id.action_delete);
//                    menu.removeItem(R.id.action_forward);
//                }
//
//                // Check that the messages could be deleted
//                boolean canBeDeleted = true;
//                for (MessageHolder holder: chatView.getSelectedMessages()) {
//                    if (!ChatSDK.thread().canDeleteMessage(holder.getMessage())) {
//                        canBeDeleted = false;
//                    }
//                }
//                if (!canBeDeleted) {
//                    menu.removeItem(R.id.action_delete);
//                }
//
//                chatActionBar.hideText();
//            } else {
//
//                chatActionBar.showSearchIcon();
//
//                if (ChatSDK.thread().canAddUsersToThread(thread) && getActivity() != null) {
//                    inflater.inflate(R.menu.add_menu, menu);
//                    menu.findItem(R.id.action_add).setIcon(Icons.get(getActivity(), Icons.choose().add, Icons.shared().actionBarIconColor));
//                }
//
//                chatActionBar.showText();
//            }
//        }
//
//        super.onCreateOptionsMenu(menu, inflater);
//    }

}
