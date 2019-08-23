package co.chatsdk.ui;

import android.app.Activity;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.recyclerview.widget.RecyclerView;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

public class ImageListAdapter extends RecyclerView.Adapter<ImageListAdapter.MyViewHolder>  {

    public ArrayList<Uri> uriArrayList = new ArrayList<>();
    private WeakReference<SendMultipleImagesAtOnceActivity> theMainActivity;

    public ImageListAdapter (SendMultipleImagesAtOnceActivity activity) {
        this.theMainActivity = new WeakReference<SendMultipleImagesAtOnceActivity>(activity);
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        private WeakReference<ImageListAdapter> adapter;
        public ImageView imageView;
        public Uri thisImageUri;
        public int thisImagePosition;

        public MyViewHolder(ImageListAdapter la, ImageView v) {
            super(v);
            this.adapter = new WeakReference<ImageListAdapter>(la);
            imageView = v;

            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    setMainImageDisplay(thisImageUri);
                    setMainImageUri(thisImageUri);
                }
            });

            imageView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    Uri toBeDeletedUri = thisImageUri;
                    removeImageFromAdapter(thisImageUri);
                    notifyDataSetChanged();
                    if (getMainImageUri() == toBeDeletedUri) {
                        if (uriArrayList.size() >= 1 && uriArrayList.size() == thisImagePosition) {
                            setMainImageDisplay(uriArrayList.get(thisImagePosition - 1));
                            setMainImageUri(uriArrayList.get(thisImagePosition - 1));
                        }
                        else if (uriArrayList.size() > 1) {
                            setMainImageDisplay(uriArrayList.get(thisImagePosition));
                            setMainImageUri(uriArrayList.get(thisImagePosition));
                        }
                    }
                    return true;
                }
            });
        }
    }

    @Override
    public ImageListAdapter.MyViewHolder onCreateViewHolder (ViewGroup parent, int viewType) {

        ImageView v = (ImageView) LayoutInflater.from(parent.getContext()).inflate(R.layout.image_view, parent, false);
        MyViewHolder vh = new MyViewHolder(this, v);
        return vh;
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        holder.imageView.setImageURI(uriArrayList.get(position));
        holder.thisImageUri = uriArrayList.get(position);
        holder.thisImagePosition = position;
    }

    @Override
    public int getItemCount() {
        return uriArrayList.size();
    }

    public void addImageToMyAdapter(Uri uri) {
        uriArrayList.add(uri);
    }

    public void removeImageFromAdapter(Uri uri) {
        uriArrayList.remove(uri);
        if (uriArrayList.size() == 0) {
            theMainActivity.get().finish();
        }
    }

    public void setMainImageDisplay(Uri uri) {
        theMainActivity.get().setTheMainImageDisplay(uri);
    }

    public Uri getMainImageUri() {
        return theMainActivity.get().mainImageUri;
    }

    public void setMainImageUri(Uri uri) {
        theMainActivity.get().setTheMainImageUri(uri);
    }
}
