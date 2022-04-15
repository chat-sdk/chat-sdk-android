package sdk.chat.app.xmpp;

import static sdk.chat.ui.fragments.ChatFragment.getInputMethodManager;

import android.graphics.Rect;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsAnimationCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.List;

import sdk.chat.demo.xmpp.R;

public class KeyboardTestActivity2 extends AppCompatActivity {

    protected int getLayout() {
        return sdk.chat.demo.xmpp.R.layout.activity_keyboard_2;
    }

    protected CustomLayout root;
    protected EditText et;
    protected Button button;
    protected View options;

    protected int keyboardHeight = 0;
    protected boolean optionsShowing = false;
    protected boolean keyboardOpen = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(getLayout());

//        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);

        root = findViewById(R.id.root);

//        View decor = getWindow().getDecorView();
//        View rootView = decor.findViewById(android.R.id.content);

        button = findViewById(R.id.button);
        et = findViewById(R.id.edit);
        options = findViewById(R.id.options);

        root.keyboardShown = () -> {
            options.setVisibility(View.GONE);
        };

        root.keyboardHidden = () -> {
            options.setVisibility(View.VISIBLE);
        };

        root.heightUpdater = height -> {
            ViewGroup.LayoutParams params = options.getLayoutParams();
            params.height = height;
            options.setLayoutParams(params);
        };

    }

    protected void addListener() {

        Window mRootWindow = getWindow();
        View mRootView = mRootWindow.getDecorView().findViewById(android.R.id.content);
        mRootView.getViewTreeObserver().addOnGlobalLayoutListener(() -> {
            Rect r = new Rect();
            View view = mRootWindow.getDecorView();
            view.getWindowVisibleDisplayFrame(r);

            Insets in = ViewCompat.getRootWindowInsets(root).getInsets(WindowInsetsCompat.Type.ime());
            Insets sys = ViewCompat.getRootWindowInsets(root).getInsets(WindowInsetsCompat.Type.systemBars());



            // r.left, r.top, r.right, r.bottom
        });

        ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
            System.out.println("");



            return null;
        });


        ViewCompat.setOnApplyWindowInsetsListener(et, (v, insets) -> {
            System.out.println("");
            Insets in = ViewCompat.getRootWindowInsets(v).getInsets(WindowInsetsCompat.Type.ime());

            return null;
        });

        ViewCompat.setWindowInsetsAnimationCallback(root, new WindowInsetsAnimationCompat.Callback(WindowInsetsAnimationCompat.Callback.DISPATCH_MODE_CONTINUE_ON_SUBTREE) {
            @NonNull
            @Override
            public WindowInsetsCompat onProgress(@NonNull WindowInsetsCompat insets, @NonNull List<WindowInsetsAnimationCompat> runningAnimations) {
                System.out.println("");
                return null;
            }
        });

        button.setOnClickListener(v -> {
            if (keyboardOpen) {
                getInputMethodManager(et.getContext())
                        .hideSoftInputFromWindow(et.getWindowToken(), 0);
            } else {
                et.post(() -> {
                    et.requestFocus();
                    getInputMethodManager(et.getContext()).showSoftInput(et, 0);
                });
            }


            keyboardOpen = !keyboardOpen;

//            if (options.getVisibility() == View.GONE) {
//                options.setVisibility(View.VISIBLE);
//            } else {
//                options.setVisibility(View.GONE);
//            }

//            WindowInsetsControllerCompat controller = ViewCompat.getWindowInsetsController(root);
//            if (controller != null) {
//                if (keyboardOpen) {
//                    controller.hide(WindowInsetsCompat.Type.ime());
//                } else {
//                    controller.show(WindowInsetsCompat.Type.ime());
//                }
//            }
        });

        et.setOnFocusChangeListener((v, hasFocus) -> {
            optionsShowing = true;
//            WindowInsetsControllerCompat controller = ViewCompat.getWindowInsetsController(view);
//            if (controller != null) {
//                controller.show(WindowInsetsCompat.Type.ime());
//            }
        });
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
    }

    @Override
    protected void onResume() {
        super.onResume();
        addListener();

    }

    @Override
    protected void onStart() {
        super.onStart();


    }

}
