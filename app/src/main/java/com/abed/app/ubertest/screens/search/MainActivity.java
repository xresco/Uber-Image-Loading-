package com.abed.app.ubertest.screens.search;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.Button;
import android.widget.EditText;

import com.abed.app.ubertest.R;
import com.abed.app.ubertest.model.response.FlickrPhoto;
import com.abed.app.ubertest.network.ImageDownloader;
import com.abed.app.ubertest.network.chache.FileCache;

import java.util.List;

public class MainActivity extends AppCompatActivity implements SearchPhotosView {

    @Nullable
    private SearchPhotosPresenter presenter;

    @Nullable
    private SearchPhotosAdapter photosAdapter;

    private int visibleItemCount;

    private int totalItemCount;

    private int pastVisibleItems;

    private static final int PERMISSIONS_REQUEST_STORAGE = 122;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        requestStoragePermission();
        presenter = new SearchPhotosPresenter();
        presenter.attach(this);
        init();
        presenter.loadMorePhoto();
    }

    private void requestStoragePermission() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {


            // No explanation needed; request the permission
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    PERMISSIONS_REQUEST_STORAGE);
        } else {
            ImageDownloader.init(new FileCache(this));
        }

    }


    private void init() {
        RecyclerView recyclerView = findViewById(R.id.recycler_search_result);
        photosAdapter = new SearchPhotosAdapter();
        recyclerView.setAdapter(photosAdapter);
        GridLayoutManager layoutManager = new GridLayoutManager(this, 3);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                if (dy > 0) {
                    visibleItemCount = layoutManager.getChildCount();
                    totalItemCount = layoutManager.getItemCount();
                    super.onScrolled(recyclerView, dx, dy);
                    visibleItemCount = layoutManager.getChildCount();
                    totalItemCount = layoutManager.getItemCount();
                    pastVisibleItems = layoutManager.findFirstVisibleItemPosition();
                    if ((visibleItemCount + pastVisibleItems) >= totalItemCount &&
                            presenter != null) {
                        presenter.loadMorePhoto();
                    }
                }
            }
        });

        Button btnSearch = findViewById(R.id.btn_search);
        btnSearch.setOnClickListener(v -> {
            EditText txtSearch = findViewById(R.id.txt_search_query);
            if (presenter != null && txtSearch != null) {
                presenter.searchPhotos(txtSearch.getText().toString());
            }
        });
    }

    @Override
    public void showPhotos(@NonNull List<FlickrPhoto> photos) {
        if (photosAdapter == null) {
            return;
        }

        photosAdapter.addItems(photos);
    }

    @Override
    public void showErrorState() {
        //Display error message, to be discussed with the designer
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (presenter != null) {
            presenter.detach();
        }
    }

    @Override
    public void clearPhotos() {
        if (photosAdapter == null) {
            return;
        }

        photosAdapter.clearItems();
    }

    @Override
    public void showLoadingState() {
        //Show loading message, to be discussed with the designer
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSIONS_REQUEST_STORAGE) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                ImageDownloader.init(new FileCache(this));
            }
        }
    }
}
