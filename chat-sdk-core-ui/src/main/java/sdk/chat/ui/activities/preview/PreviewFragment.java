package sdk.chat.ui.activities.preview;

import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.lassi.data.media.MiMedia;

import sdk.chat.core.audio.AudioPlayer;
import sdk.chat.core.session.ChatSDK;
import sdk.chat.core.ui.ThemeProvider;
import sdk.chat.ui.R;

public class PreviewFragment extends Fragment {

    public ImageView imageView;
    public ImageButton playButton;
    public TextView durationTextView;

    public MiMedia media;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        View rootView = inflater.inflate(R.layout.fragment_media, container);

        imageView = rootView.findViewById(R.id.imageView);
        playButton = rootView.findViewById(R.id.playButton);
        durationTextView = rootView.findViewById(R.id.durationTextView);

        if (media != null) {
            Glide.with(this).load(media.getPath()).into(imageView);
            if (media.getDuration() > 0) {
                durationTextView.setText(AudioPlayer.toSeconds(media.getDuration()));
                playButton.setVisibility(VISIBLE);
                durationTextView.setVisibility(VISIBLE);
                playButton.setOnClickListener(view -> {
                    if (getActivity() != null && ChatSDK.videoMessage() != null) {
                        ChatSDK.videoMessage().startPlayVideoActivity(getActivity(), media.getPath());
                    }
                });

                ThemeProvider provider = ChatSDK.feather().instance(ThemeProvider.class);
                if (provider != null) {
                    provider.applyTheme(playButton, "chat-preview-play-video-button");
                }

            } else {
                playButton.setVisibility(INVISIBLE);
                durationTextView.setVisibility(INVISIBLE);
                playButton.setOnClickListener(null);
            }
        }

        return rootView;
    }
}
