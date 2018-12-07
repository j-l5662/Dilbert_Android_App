package com.johannlau.dilbert_app.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;

import com.johannlau.test_app.R;

import butterknife.BindView;
import butterknife.ButterKnife;

public class FullScreenImageActivity extends AppCompatActivity {

    @BindView(R.id.full_screen_image)
    ImageView fullScreenImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_screen_image);

        ButterKnife.bind(this);

        Intent callActivityIntent = getIntent();
        if(callActivityIntent != null){
            Uri imageURI = callActivityIntent.getData();

            if(imageURI != null && fullScreenImageView != null) {
            }
        }
    }
}
