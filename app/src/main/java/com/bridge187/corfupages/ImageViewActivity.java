package com.bridge187.corfupages;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.bridge187.corfupages.utilities.ImageCache;


public class ImageViewActivity extends FragmentActivity
{

    public final static String IMAGE_URL = "IMAGE_URL";
    private String imageUrl;
    private ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.image_vew_activity);

        imageUrl = this.getIntent().getStringExtra(IMAGE_URL);
        imageView = (ImageView)findViewById(R.id.large_image_view);
        ImageButton cancelButton = (ImageButton)findViewById(R.id.large_image_view_close_button);
        cancelButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                ImageViewActivity.this.finish();
            }
        });
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        ImageCache imageCache = ((MainApplication)getApplication()).getImageCache();
        imageCache.loadBitmap(imageUrl, imageView, true, -1);
    }
}
