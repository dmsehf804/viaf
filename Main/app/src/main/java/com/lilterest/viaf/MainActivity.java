package com.lilterest.viaf;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.RequestConfiguration;
import com.lilterest.viaf.utils.imageFolder;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;

import org.opencv.android.OpenCVLoader;

import java.io.File;
import java.util.ArrayList;

import static com.google.android.gms.common.util.CollectionUtils.listOf;

public class MainActivity extends AppCompatActivity {
    private final long FINISH_INTERVAL_TIME = 2000;
    private long backPressedTime = 0;

    private static final int REQUEST_PERMISSIONS = 1;
    private static final String[] MY_PERMISSIONS = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    public boolean hasAllPermissionsGranted() {
        for (String permission : MY_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, MY_PERMISSIONS, REQUEST_PERMISSIONS);
                return false;
            }
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == REQUEST_PERMISSIONS) {
            int index = 0;
            for (String permission : permissions) {
                if(permission.equalsIgnoreCase(Manifest.permission.READ_EXTERNAL_STORAGE) && grantResults[index] == PackageManager.PERMISSION_GRANTED) {

                    break;
                }
                index++;
            }
            return;
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (OpenCVLoader.initDebug()) {
            Log.d("myTag", "OpenCV loaded");
        }
        MobileAds.initialize(this);
        AdView adView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);

        ImageView enhanceBtn =  findViewById(R.id.enhance_btn);
        enhanceBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent move = new Intent(MainActivity.this, DataSelectActivity.class);
                move.putExtra("flag","enhance");
                startActivity(move);
            }
        });

        ImageView mosaicBtn =  findViewById(R.id.mosaic_btn);
        mosaicBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent move = new Intent(MainActivity.this,DataSelectActivity.class);
                move.putExtra("flag","mosaic");
                startActivity(move);
            }
        });

        if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            boolean permission = hasAllPermissionsGranted();
            Log.e("test","permission : "+permission);
            if(!permission)
                return;
        }

        if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            boolean permission = hasAllPermissionsGranted();
            Log.e("test","permission : "+permission);
            if(!permission)
                return;
        }

        Button info_btn = findViewById(R.id.info_btn);

        info_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InfoDialog infoDialog = new InfoDialog(MainActivity.this);
                infoDialog.callFunction("Email: lilterest1008@gmail.com\nVersion: 1.0");
            }
        });

    }

    @Override
    public void onBackPressed() {
        long tempTime = System.currentTimeMillis();
        long intervalTime = tempTime - backPressedTime;

        if (0 <= intervalTime && FINISH_INTERVAL_TIME >= intervalTime)
        {
            finish();
        }
        else
        {
            backPressedTime = tempTime;
            Toast.makeText(getApplicationContext(), "한번 더 누르면 종료됩니다.", Toast.LENGTH_SHORT).show();
        }
    }
}