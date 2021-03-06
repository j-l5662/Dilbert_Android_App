package com.johannlau.dilbert_app.ui;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.johannlau.test_app.R;
import com.ortiz.touchview.TouchImageView;

import butterknife.BindView;
import butterknife.ButterKnife;

public class FullScreenImageActivity extends AppCompatActivity {

    @BindView(R.id.full_screen_image)
    TouchImageView fullScreenImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_screen_image);

        ButterKnife.bind(this);

        byte[] byteArray = getIntent().getByteArrayExtra("bmp");
        Bitmap bmp = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
        fullScreenImageView.setImageBitmap(bmp);

    }
}
