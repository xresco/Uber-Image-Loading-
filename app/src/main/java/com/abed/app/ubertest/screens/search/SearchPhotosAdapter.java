package com.abed.app.ubertest.screens.search;

import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.abed.app.ubertest.R;
import com.abed.app.ubertest.model.request.PhotoRequest;
import com.abed.app.ubertest.model.response.FlickrPhoto;
import com.abed.app.ubertest.network.ImageDownloader;
import com.abed.app.ubertest.network.dispatcher.Scheduler;
import com.abed.app.ubertest.network.dispatcher.ThreadPoolScheduler;

import java.util.LinkedList;
import java.util.List;


public class SearchPhotosAdapter extends RecyclerView.Adapter<SearchPhotosAdapter.ViewHolder> {

    private static final String TAG = "SEARCH_ADAPTER";

    @NonNull
    private List<FlickrPhoto> items;

    @NonNull
    private ThreadPoolScheduler<PhotoRequest, Bitmap> dispatcherThread;

    SearchPhotosAdapter() {
        this.items = new LinkedList<>();
        setHasStableIds(true);
        Scheduler.SchedulerTask<PhotoRequest, Bitmap> dispatcherTask = data -> ImageDownloader.getInstance().getBitmapFromUrl(
                data.getUrl(),
                data.getReqWidth(),
                data.getReqHeight());
        dispatcherThread = new ThreadPoolScheduler<>(TAG, dispatcherTask);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        View v = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.search_item_photo, viewGroup, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int position) {
        if (items.get(position) == null) {
            return;
        }

        FlickrPhoto flickrPhoto = items.get(position);

        if (flickrPhoto == null) {
            return;
        }

        viewHolder.setData(items.get(position), dispatcherThread);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    void setItems(@Nullable List<FlickrPhoto> list) {
        items.clear();
        if (list != null) {
            items.addAll(list);
        }
        notifyDataSetChanged();
    }

    void addItems(@NonNull List<FlickrPhoto> list) {
        int oldSize = items.size();
        items.addAll(list);
        notifyItemRangeInserted(oldSize, list.size());
    }

    void clearItems() {
        items.clear();
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        private ImageView img;

        private TextView txtTile;

        ViewHolder(View itemView) {
            super(itemView);
            img = itemView.findViewById(R.id.img_search_item_photo);
            txtTile = itemView.findViewById(R.id.txt_search_item_photo);

        }

        void setData(@NonNull FlickrPhoto photo, ThreadPoolScheduler<PhotoRequest, Bitmap> dispatcherThread) {
            PhotoRequest photoRequest = new PhotoRequest(photo.getImageURL(), 100, 100);
            dispatcherThread.requestDispatch(photoRequest,
                    new Scheduler.SchedulerCallback<Bitmap>() {
                        @Override
                        public void onSuccess(@Nullable Bitmap bitmap) {
                            img.setImageBitmap(bitmap);

                        }

                        @Override
                        public void onFail(int completionStatus, @Nullable Throwable throwable) {
                            img.setImageResource(R.drawable.ic_launcher_background);
                        }
                    });
            txtTile.setText(photo.getTitle());
        }
    }
}
