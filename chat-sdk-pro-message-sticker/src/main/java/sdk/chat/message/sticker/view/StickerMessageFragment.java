package sdk.chat.message.sticker.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import sdk.chat.message.sticker.Configuration;
import sdk.chat.message.sticker.R;
import sdk.chat.message.sticker.Sticker;
import sdk.chat.message.sticker.StickerPack;
import sdk.chat.ui.fragments.BaseFragment;

/**
 * Created by ben on 10/11/17.
 */

public class StickerMessageFragment extends BaseFragment {

    private RecyclerView selectPackRecyclerView;
    private RecyclerView selectStickerRecyclerView;

    public static int NumberOfColumns = 3;
    public int numberOfColumns = NumberOfColumns;

    private StickerListAdapter selectPackListAdapter;
    private StickerListAdapter selectStickerListAdapter;

    private ArrayList<StickerPack> stickerPacks = null;

    private Result stickerResultListener;

    public interface Result {
        void result (String stickerName);
    }

    @Override
    protected int getLayout() {
        return R.layout.fragment_sticker;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, final Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);

        try {
            stickerPacks = Configuration.getStickerPacks(getContext());
        }
        catch (Exception e) {
            finish();
        }

        selectPackRecyclerView = rootView.findViewById(R.id.select_pack_recycler);
        selectPackRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false));

        selectPackListAdapter = new StickerListAdapter();
        selectPackRecyclerView.setAdapter(selectPackListAdapter);

        selectStickerRecyclerView = rootView.findViewById(R.id.select_sticker_recycler);

        GridLayoutManager gridLayout = new GridLayoutManager(getContext(), numberOfColumns);
        selectStickerRecyclerView.setLayoutManager(gridLayout);

        selectStickerListAdapter = new StickerListAdapter();
        selectStickerRecyclerView.setAdapter(selectStickerListAdapter);

        if(stickerPacks != null && stickerPacks.size() > 0) {

            selectPackListAdapter.setItems(stickerPackListItems());
            for(StickerPack pack : stickerPacks) {
                pack.onClickListener = new StickerPack.StickerPackOnClickListener() {
                    @Override
                    public void onClick(StickerPack pack) {
                        selectStickerListAdapter.setItems(pack.getStickerItems());
                    }
                };
                pack.stickerOnClickListener = new StickerPack.StickerOnClickListener() {
                    @Override
                    public void onClick(Sticker sticker) {
                        if(stickerResultListener != null) {
                            stickerResultListener.result(sticker.imageName);
                        }
                    }
                };
            }
            selectStickerListAdapter.setItems(stickerPacks.get(0).getStickerItems());
        }
        else {
            // TODO: Quit context
            finish();
        }

        return view;
    }

    @Override
    protected void initViews() {

    }

    public void finish () {
        Toast.makeText(getContext(), getActivity().getString(R.string.unable_to_load_sticker_pack), Toast.LENGTH_LONG).show();
    }

    @Override
    public void clearData() {}

    @Override
    public void reloadData() {}

    private ArrayList<StickerListItem> stickerPackListItems () {
        ArrayList<StickerListItem> items = new ArrayList<>();
        for(StickerPack pack : stickerPacks) {
            items.add(pack);
        }
        return items;
    }

    public void setStickerResultListener(Result stickerResult) {
        this.stickerResultListener = stickerResult;
    }

}
