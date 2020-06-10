package sdk.chat.demo;

import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.cardview.widget.CardView;

import butterknife.BindView;
import io.reactivex.Completable;
import io.reactivex.annotations.NonNull;

public class BackendFragment extends CardViewFragment {

    @BindView(R2.id.firebaseLegacyCardView)
    CardView firebaseLegacyCardView;
    @BindView(R2.id.firestreamCardView)
    CardView firestreamCardView;
    @BindView(R2.id.xmppCardView)
    CardView xmppCardView;
    @BindView(R2.id.firebaseTextView)
    TextView firebaseTextView;
    @BindView(R2.id.firestreamTextView)
    TextView firestreamTextView;
    @BindView(R2.id.xmppTextView)
    TextView xmppTextView;

    @Override
    protected int getLayout() {
        return R.layout.fragment_backend;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);

        initViews();

        return view;
    }

    @Override
    protected void initViews() {
        super.initViews();

        String firebaseText = "" +
                "&#8226; Firebase Realtime<br/>" +
                "&#8226; 200k concurrent users<br/>" +
                "&#8226; Android and iOS<br/>";
//                "&#8226; <a href=\"http://firestream.com\">More Details</a><br/>";

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            firebaseTextView.setText(Html.fromHtml(firebaseText, Html.FROM_HTML_MODE_COMPACT));
        } else {
            firebaseTextView.setText(Html.fromHtml(firebaseText));
        }

        String firestreamText = "" +
                "&#8226; Firebase Firestore or Realtime<br/>" +
                "&#8226; 1 million concurrent users<br/>" +
                "&#8226; Android, iOS, Web, Node.js<br/>";
//                "&#8226; <a href=\"http://firestream.com\">More Details</a><br/>";

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            firestreamTextView.setText(Html.fromHtml(firestreamText, Html.FROM_HTML_MODE_COMPACT));
        } else {
            firestreamTextView.setText(Html.fromHtml(firestreamText));
        }

        String xmppText = "" +
                "&#8226; eXtensible Messaging and Presence Protocol<br/>" +
                "&#8226; 2 million concurrent users per node<br/>" +
                "&#8226; ejabberd, OpenFire, Tigase, Prosody...<br/>" +
                "&#8226; Host on your own server<br/>" +
                "&#8226; Works on an intranet<br/>";
//                "&#8226; <a href=\"http://firestream.com\">More Details</a><br/>";

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            xmppTextView.setText(Html.fromHtml(xmppText, Html.FROM_HTML_MODE_COMPACT));
        } else {
            xmppTextView.setText(Html.fromHtml(xmppText));
        }

        setSelectionListener(firebaseLegacyCardView);
        setSelectionListener(firestreamCardView);
        setSelectionListener(xmppCardView);

        if (selected == null) {
            DemoConfigBuilder.Backend backend = DemoConfigBuilder.shared().getBackend();
            if (backend != null) {
                switch (backend) {
                    case Firebase:
                        super.selectView(firebaseLegacyCardView);
                        break;
                    case XMPP:
                        super.selectView(xmppCardView);
                        break;
                    case FireStream:
                        super.selectView(firestreamCardView);
                        break;
                }
            } else {
                selectView(firestreamCardView);
            }
        } else {
            selectView(selected, 0);
        }
    }

    @Override
    public void clearData() {

    }

    @Override
    public void reloadData() {

    }

//    @Override
//    public void setTabVisibility(boolean isVisible) {
//        if (isVisible) {
//
//        }
//    }

    @Override
    public Completable selectView(CardView view) {
        return selectView(view, 400);
    }

    @Override
    public Completable selectView(CardView view, long duration) {
        dm.add(super.selectView(view, duration).subscribe(() -> {
            if (view.equals(firebaseLegacyCardView)) {
                DemoConfigBuilder.shared().setBackend(DemoConfigBuilder.Backend.Firebase);
            }
            if (view.equals(firestreamCardView)) {
                DemoConfigBuilder.shared().setBackend(DemoConfigBuilder.Backend.FireStream);
            }
            if (view.equals(xmppCardView)) {
                DemoConfigBuilder.shared().setBackend(DemoConfigBuilder.Backend.XMPP);
            }
        }, throwable -> {

        }));
        return Completable.complete();
    }

}
