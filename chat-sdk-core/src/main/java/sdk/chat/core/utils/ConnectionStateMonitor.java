package sdk.chat.core.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.os.Build;

import androidx.annotation.RequiresApi;

import sdk.chat.core.events.NetworkEvent;
import sdk.chat.core.session.ChatSDK;

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class ConnectionStateMonitor extends ConnectivityManager.NetworkCallback {

    final NetworkRequest networkRequest;

    public ConnectionStateMonitor() {

        networkRequest = new NetworkRequest.Builder()
                .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
                .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                .build();
    }

    public void enable(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        connectivityManager.registerNetworkCallback(networkRequest, this);
    }

    // Likewise, you can have a disable method that simply calls ConnectivityManager.unregisterNetworkCallback(NetworkCallback) too.

    @Override
    public void onAvailable(Network network) {
        // Do what you need to do here
        if (ChatSDK.events() != null) {
            ChatSDK.events().source().accept(NetworkEvent.networkStateChanged(true));
        }
    }

    public void onLost(Network network) {
        if (ChatSDK.events() != null) {
            ChatSDK.events().source().accept(NetworkEvent.networkStateChanged(false));
        }
    }

}
