package co.chatsdk.ui;

import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder>  {

    private static ArrayList<Uri> mDataset;
    public Uri mainImageUri;

    public static class MyViewHolder extends RecyclerView.ViewHolder {

        public ImageView imageView;
        public Uri thisImageUri;
        public int thisImagePosition;

        public MyViewHolder(ImageView v) {
            super(v);
            imageView = v;

            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    SendMultipleImagesAtOnceActivity.mainImageDisplay.setImageURI(thisImageUri);
                    SendMultipleImagesAtOnceActivity.mainImageUri = thisImageUri;
                }
            });

            imageView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    Uri toBeDeletedUri = thisImageUri;
                    SendMultipleImagesAtOnceActivity.myImageArray.remove(thisImageUri);
                    SendMultipleImagesAtOnceActivity.mAdapter.notifyDataSetChanged();
                    if (SendMultipleImagesAtOnceActivity.mainImageUri == toBeDeletedUri) {
                        SendMultipleImagesAtOnceActivity.mainImageDisplay.setImageURI(mDataset.get(thisImagePosition));
                    }
                    return true;
                }
            });
        }
    }

    public MyAdapter (ArrayList<Uri> myDataset) {
        mDataset = myDataset;
    }

    @Override
    public MyAdapter.MyViewHolder onCreateViewHolder (ViewGroup parent, int viewType) {

        ImageView v = (ImageView) LayoutInflater.from(parent.getContext()).inflate(R.layout.image_view, parent, false);
        MyViewHolder vh = new MyViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        holder.imageView.setImageURI(mDataset.get(position));
        holder.thisImageUri = mDataset.get(position);
        holder.thisImagePosition = position;
    }

    @Override
    public int getItemCount() {
        return mDataset.size();
    }

    public void onDataSetChanged (ArrayList<Uri> myImageArray) {

    }

    public void setMainImageUri (Uri uri) {
        mainImageUri = uri;
    }

    public static void addImageToMyAdapter(Uri uri) {
        mDataset.add(uri);
    }

}
