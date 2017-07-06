package com.example.hezhu.myapplication;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class CropImageActivity extends AppCompatActivity {

    @BindView(R.id.cropImageView) CropImageView cropImageView;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.crop_image_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
            case R.id.rotate:
                cropImageView.rotateImage(90);
                break;
            case R.id.done:
                CardCameraActivity.bitmap = cropImageView.getCroppedImage();
                setResult(RESULT_OK);
                finish();
                break;
        }
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crop_image);
        ButterKnife.bind(this);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("调整证件区域");
        actionBar.setDisplayHomeAsUpEnabled(true);
        cropImageView.setMultiTouchEnabled(true);
        cropImageView.setImageBitmap(CardCameraActivity.bitmap);
        cropImageView.setGuidelines(CropImageView.Guidelines.OFF);
    }
}
