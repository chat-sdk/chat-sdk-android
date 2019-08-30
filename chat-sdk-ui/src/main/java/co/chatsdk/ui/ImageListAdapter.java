package co.chatsdk.ui;

import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.recyclerview.widget.RecyclerView;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

public class ImageListAdapter extends RecyclerView.Adapter<ImageListAdapter.ImageGalleryViewHolder>  {

    public ArrayList<Uri> imageURIs = new ArrayList<>();
    private WeakReference<ImageGalleryActivity> parentActivity;

    public ImageListAdapter (ImageGalleryActivity activity) {
        this.parentActivity = new WeakReference(activity);
    }

    public class ImageGalleryViewHolder extends RecyclerView.ViewHolder {

        private WeakReference<ImageListAdapter> adapter;
        public ImageView imageView;
        public Uri currentImageUri;
        public int imagePosition;

        public ImageGalleryViewHolder(ImageListAdapter la, ImageView iv) {
            super(iv);
            this.adapter = new WeakReference(la);
            imageView = iv;

            imageView.setOnClickListener(v -> {
                setCurrentImageURI(currentImageUri);
            });

            imageView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    removeImageUri(currentImageUri);
                    if (getCurrentImageUri().equals(currentImageUri)) {
                        if (imageURIs.size() == imagePosition) {
                            setCurrentImageUri(imageURIs.get(imagePosition - 1));
                        }
                        else {
                            setCurrentImageUri(imageURIs.get(imagePosition));
                        }
                    }
                    return true;
                }
            });
        }
    }

    @Override
    public ImageGalleryViewHolder onCreateViewHolder (ViewGroup parent, int viewType) {

        ImageView v = (ImageView) LayoutInflater.from(parent.getContext()).inflate(R.layout.image_view, parent, false);
        ImageGalleryViewHolder vh = new ImageGalleryViewHolder(this, v);
        return vh;
    }

    @Override
    public void onBindViewHolder(ImageGalleryViewHolder holder, int position) {
        setHolderURI(holder, position);
    }

    @Override
    public int getItemCount() {
        return imageURIs.size();
    }

    public void addImageUri(Uri uri) {
        imageURIs.add(uri);
    }

    public void removeImageUri(Uri uri) {
        imageURIs.remove(uri);
        if (imageURIs.size() == 0) {
            parentActivity.get().finish();
        }
        notifyDataSetChanged();
    }

    public void setCurrentImageUri(Uri uri) {
        parentActivity.get().setTheMainImageDisplay(uri);
    }

    public Uri getCurrentImageUri() {
        return parentActivity.get().mainImageUri;
    }

    public void setCurrentImageURI (Uri uri) {
        setCurrentImageUri(uri);
    }

    public void setHolderURI(ImageGalleryViewHolder holder, int position) {
        holder.imageView.setImageURI(imageURIs.get(position));
        holder.currentImageUri = imageURIs.get(position);
        holder.imagePosition = position;
    }
}
