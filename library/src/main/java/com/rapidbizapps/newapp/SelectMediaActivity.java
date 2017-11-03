package com.rapidbizapps.newapp;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

public class SelectMediaActivity extends AppCompatActivity {
    private boolean openGallery = true;
    private int MEDIA_INTENT = 123;
    public static String INTENTFILES = "filePaths";
    private List<String> mFilePathsList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_media);
    }

    public void openMainActivity(View view) {
        openGallery = false;
        if (!checkPermission("CAMERA")) {
            requestPermission("CAMERA");
        } else if (!checkPermission("READ_EXTERNAL")) {
            requestPermission("READ_EXTERNAL");
        } else {
            startActivityForResult(new Intent(SelectMediaActivity.this, MainActivity.class), MEDIA_INTENT);
        }
    }

    public void openGalley(View view) {
        openGallery = true;
        if (!checkPermission("READ_EXTERNAL")) {
            requestPermission("READ_EXTERNAL");
        } else {
            startActivityForResult(new Intent(SelectMediaActivity.this, GalleryActivity.class), MEDIA_INTENT);
        }
    }

    private boolean checkPermission(String permission) {
        boolean bool = false;
        int result;
        switch (permission) {
            case "READ_EXTERNAL":
                result = ContextCompat.checkSelfPermission(SelectMediaActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE);
                if (result == PackageManager.PERMISSION_GRANTED) {
                    bool = true;
                } else {
                    bool = false;
                }
                break;
            case "CAMERA":
                result = ContextCompat.checkSelfPermission(SelectMediaActivity.this, Manifest.permission.CAMERA);
                if (result == PackageManager.PERMISSION_GRANTED) {
                    bool = true;
                } else {
                    bool = false;
                }
                break;
        }
        return bool;
    }

    private void requestPermission(String permission) {
        switch (permission) {
            case "READ_EXTERNAL":
                if (ActivityCompat.shouldShowRequestPermissionRationale(SelectMediaActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    //   Toast.makeText(LoginActivity.this, "GPS permission allows us to access location data. Please allow in App Settings for additional functionality.", Toast.LENGTH_LONG).show();
                    ActivityCompat.requestPermissions(SelectMediaActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 102);
                } else {
                    ActivityCompat.requestPermissions(SelectMediaActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 102);
                }
                break;
            case "CAMERA":
                if (ActivityCompat.shouldShowRequestPermissionRationale(SelectMediaActivity.this, Manifest.permission.CAMERA)) {
                    //   Toast.makeText(LoginActivity.this, "GPS permission allows us to access location data. Please allow in App Settings for additional functionality.", Toast.LENGTH_LONG).show();
                    ActivityCompat.requestPermissions(SelectMediaActivity.this, new String[]{Manifest.permission.CAMERA}, 101);
                } else {
                    ActivityCompat.requestPermissions(SelectMediaActivity.this, new String[]{Manifest.permission.CAMERA}, 101);
                }
                break;

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 101:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (!checkPermission("READ_EXTERNAL")) {
                        requestPermission("READ_EXTERNAL");
                    } else if (openGallery) {
                        startActivity(new Intent(SelectMediaActivity.this, GalleryActivity.class));
                    } else {
                        startActivity(new Intent(SelectMediaActivity.this, MainActivity.class));
                    }
                } else {

                }
                break;
            case 102:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (openGallery) {
                        startActivity(new Intent(SelectMediaActivity.this, GalleryActivity.class));
                    } else {
                        startActivity(new Intent(SelectMediaActivity.this, MainActivity.class));
                    }
                } else {
                }
                break;
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_CANCELED) {
            if (requestCode == MEDIA_INTENT) {
                mFilePathsList.clear();
                mFilePathsList = data.getStringArrayListExtra(INTENTFILES);
                for (String str : mFilePathsList) {
                    Log.e("Files that are selected",str);
                }
            }
        }
    }
}
