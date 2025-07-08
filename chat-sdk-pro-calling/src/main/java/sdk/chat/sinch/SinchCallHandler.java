package sdk.chat.sinch;

import android.content.Context;
import android.content.Intent;

import androidx.fragment.app.Fragment;

import com.sinch.android.rtc.calling.Call;

import sdk.chat.core.dao.ThreadX;
import sdk.chat.core.handlers.CallHandler;
import sdk.chat.core.interfaces.ThreadType;
import sdk.chat.core.session.ChatSDK;
import sdk.chat.sinch.calling.CallActivity;

public class SinchCallHandler implements CallHandler {

    @Override
    public void startCall(Fragment fragment, String userEntityID) {
        Call call = SinchModule.shared().sinchService.callUserVideo(userEntityID);
        startCallActivity(fragment.getContext(), call.getCallId());
    }

    @Override
    public boolean callEnabled(Fragment fragment, String threadEntityID) {
        if (fragment.getActivity() == null) {
            return false;
        }
        ThreadX thread = ChatSDK.db().fetchThreadWithEntityID(threadEntityID);
        if (thread != null) {
            return thread.typeIs(ThreadType.Private1to1);
        }
        return false;
    }

    public void startCallActivity(Context context, String callId) {
        Intent callScreen = new Intent(context, CallActivity.class);
        callScreen.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        callScreen.putExtra(SinchService.CALL_ID, callId);
        context.startActivity(callScreen);
    }
}
