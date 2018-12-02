package com.johannlau.dilbert_app;

import android.content.Intent;
import android.media.Image;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;

import com.johannlau.test_app.R;

public class FullScreenImageActivity extends AppCompatActivity {

    private ImageView fullScreenImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_screen_image);
        fullScreenImageView = findViewById(R.id.full_screen_image);

        Intent callActivityIntent = getIntent();
        if(callActivityIntent != null){
            Uri imageURI = callActivityIntent.getData();

            if(imageURI != null && fullScreenImageView != null) {
            }
        }
    }
}
