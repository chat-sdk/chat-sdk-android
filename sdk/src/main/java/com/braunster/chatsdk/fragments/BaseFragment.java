package com.braunster.chatsdk.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.braunster.chatsdk.R;

/**
 * Created by itzik on 6/17/2014.
 */
public class BaseFragment extends Fragment implements BaseFragmentInterface {

    View mainView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
//        mainView = inflater.inflate(resourceID, null);
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
        loadData();
        // TODO handle network call more efficiantly check time intervals and mabey listen to data coming.
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }


    @Override
    public void onRefresh() {
        loadData();
    }

    @Override
    public void loadData() {

    }

    @Override
    public void initViews() {

    }
}

interface BaseFragmentInterface{
    public void onRefresh();

    public void loadData();

    public void initViews();
}
