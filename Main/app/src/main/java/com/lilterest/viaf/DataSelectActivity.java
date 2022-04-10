package com.lilterest.viaf;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.lilterest.viaf.utils.GridSpacingItemDecoration;
import com.lilterest.viaf.utils.imageFolder;
import com.lilterest.viaf.utils.pictureFolderAdapter;
import com.google.android.material.tabs.TabLayout;

import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.app.AppCompatActivity;

import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class DataSelectActivity extends AppCompatActivity implements  com.lilterest.viaf.utils.itemClickListener{
    RecyclerView image_gal;
    RecyclerView video_gal;
    TextView image_empty;
    TextView video_empty;
    ArrayList<imageFolder> image_folds;
    ArrayList<imageFolder> video_folds;
    ArrayList<String> ImagedataFolders = new ArrayList<String>();
    ArrayList<String> VideodataFolders = new ArrayList<String>();
    Spinner image_spinner;
    Spinner video_spinner;
    spinnerAdapter imageAdapter;
    ArrayAdapter<String> videoAdapter;
    ArrayList<imageFolder> SpinnerImages;
    ArrayList<imageFolder> SpinnerVideos;
    private String funcFlag_mosaic = "mosaic";
    CardView cardView;
    int cardViewHeight;
    private String funcFlag;
    Button back_btn;
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        cardViewHeight = cardView.getHeight();
        image_spinner.setDropDownVerticalOffset(cardViewHeight);
    }

    private static final int REQUEST_PERMISSIONS = 1;
    private static final String[] MY_PERMISSIONS = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };


    public boolean hasAllPermissionsGranted() {
        for (String permission : MY_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, MY_PERMISSIONS, REQUEST_PERMISSIONS);
                finish();
                return false;
            }else{
                finish();
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
        setContentView(R.layout.activity_data_select);
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        Intent intent = getIntent();

        MobileAds.initialize(this);
        AdView adView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);

        funcFlag = intent.getExtras().getString("flag");
        image_spinner = findViewById(R.id.image_spinner);
        video_spinner = findViewById(R.id.video_spinner);
        image_empty = findViewById(R.id.image_empty);
        image_gal = findViewById(R.id.image_gal);
        video_empty = findViewById(R.id.video_empty);
        video_gal = findViewById(R.id.video_gal);
        cardView = findViewById(R.id.cardview);
        ImagedataFolders = makeImageFolders();
        VideodataFolders = makeVideoFolders();
        SpinnerImages = getPicturePathsFolderSpinnerImage();
        back_btn = findViewById(R.id.back_btn);

        back_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {

            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                // TODO : process tab selection event.
                int pos = tab.getPosition() ;
                if (pos ==1){
                    Log.d("aass","asssd");
                    Toast.makeText(DataSelectActivity.this,"Video Coming Soon...!",Toast.LENGTH_SHORT).show();

                }
                changeView(pos) ;
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                Toast.makeText(DataSelectActivity.this,"Coming Soon...!",Toast.LENGTH_SHORT);
                // do nothing
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                // do nothing
            }
        }) ;

        image_gal.addItemDecoration(new GridSpacingItemDecoration(4,0,true));
        image_gal.hasFixedSize();
        image_folds = getPicturePaths();

//        imageAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item,ImagedataFolders);
        imageAdapter = new spinnerAdapter(DataSelectActivity.this,ImagedataFolders, SpinnerImages);
        image_spinner.setAdapter(imageAdapter);

        cardView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        Log.d("aaaa",Integer.toString(cardView.getMeasuredHeightAndState()));

        if (ActivityCompat.checkSelfPermission(DataSelectActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            boolean permission = hasAllPermissionsGranted();
            Log.e("test","permission : "+permission);
            if(!permission) {
                return;
            }
        }

        if (ActivityCompat.checkSelfPermission(DataSelectActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            boolean permission = hasAllPermissionsGranted();
            Log.e("test","permission : "+permission);
            if(!permission){
                return;
            }

        }

//        image_gal.addItemDecoration(new MarginDecoration(DataSelectActivity.this));
        image_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int pos, long l) {

                if(pos==0) {
                    image_gal.hasFixedSize();
                    image_folds = getPicturePaths();

                    if (image_folds.isEmpty()) {
                        image_empty.setVisibility(View.VISIBLE);
                    } else {
                        RecyclerView.Adapter folderAdapter = new pictureFolderAdapter(image_folds, DataSelectActivity.this, DataSelectActivity.this);
                        image_gal.setAdapter(folderAdapter);
                        image_empty.setVisibility(View.GONE);
                    }

                }
                else {

                    image_gal.hasFixedSize();
                    image_folds = getPicturePathsFolder(ImagedataFolders.get(pos));

                    if (image_folds.isEmpty()) {
                        image_empty.setVisibility(View.VISIBLE);
                    } else {
                        RecyclerView.Adapter folderAdapter = new pictureFolderAdapter(image_folds, DataSelectActivity.this, DataSelectActivity.this);
                        image_gal.setAdapter(folderAdapter);
                        image_empty.setVisibility(View.GONE);
                    }
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                System.out.println("a");
            }


        });
        videoAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item,VideodataFolders);
        video_spinner.setAdapter(videoAdapter);
        video_gal.addItemDecoration(new GridSpacingItemDecoration(4,1,false));
        video_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int pos, long l) {

                if(pos==0) {

                    video_gal.hasFixedSize();
                    video_folds = getVideoPaths();

                    if (video_folds.isEmpty()) {
                        video_empty.setVisibility(View.VISIBLE);
                        video_gal.setVisibility(View.GONE);
                    } else {
                        RecyclerView.Adapter folderAdapter = new pictureFolderAdapter(video_folds, DataSelectActivity.this, DataSelectActivity.this);

                        video_gal.setAdapter(folderAdapter);
                        video_empty.setVisibility(View.GONE);
                    }
                }
                else {

                    video_gal.hasFixedSize();
                    video_folds = getVideoPathsFolder(VideodataFolders.get(pos));

                    if (video_folds.isEmpty()) {
                        video_empty.setVisibility(View.VISIBLE);
                        video_gal.setVisibility(View.GONE);
                    } else {
                        RecyclerView.Adapter folderAdapter = new pictureFolderAdapter(video_folds, DataSelectActivity.this, DataSelectActivity.this);
                        video_gal.setAdapter(folderAdapter);
                        video_empty.setVisibility(View.GONE);
                    }
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                System.out.println("a");
            }

        });

    video_spinner.setVisibility(View.GONE);
    }

    private ArrayList<imageFolder> getPicturePaths(){
        ArrayList<imageFolder> picFolders = new ArrayList<>();
        ArrayList<String> picPaths = new ArrayList<>();
        Uri allImagesuri = android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        String[] projection = { MediaStore.Images.ImageColumns.DATA ,MediaStore.Images.Media.DISPLAY_NAME,
                MediaStore.Images.Media.BUCKET_DISPLAY_NAME,MediaStore.Images.Media.BUCKET_ID};
        Cursor cursor = this.getContentResolver().query(allImagesuri, projection, null, null, null);
        try {
            if (cursor != null) {
                cursor.moveToLast();
            }
            do {
                imageFolder folds = new imageFolder();
                String name = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME));
                String folder = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME));
                String datapath = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA));

//                String folderpaths =  datapath.replace(name,"");
                String folderpaths = datapath.substring(0, datapath.lastIndexOf(folder + "/"));
                folderpaths = folderpaths + folder + "/" + name;
                if (!picPaths.contains(folderpaths)) {
                    picPaths.add(folderpaths);

                    folds.setPath(folderpaths);
                    folds.setFolderName(folder);
                    folds.setFirstPic(datapath);//if the folder has only one picture this line helps to set it as first so as to avoid blank image in itemview
                    folds.addpics();
                    picFolders.add(folds);
                } else {
                    for (int i = 0; i < picFolders.size(); i++) {
                        if (picFolders.get(i).getPath().equals(folderpaths)) {
                            picFolders.get(i).setFirstPic(datapath);
                            picFolders.get(i).addpics();
                        }
                    }
                }
            }while(cursor.moveToPrevious());
            cursor.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return picFolders;
    }

    private ArrayList<imageFolder> getVideoPaths(){
        ArrayList<imageFolder> picFolders = new ArrayList<>();
        ArrayList<String> picPaths = new ArrayList<>();
        Uri allImagesuri = android.provider.MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
        String[] projection = { MediaStore.Video.VideoColumns.DATA ,MediaStore.Video.Media.DISPLAY_NAME,
                MediaStore.Video.Media.BUCKET_DISPLAY_NAME,MediaStore.Video.Media.BUCKET_ID};
        Cursor cursor = this.getContentResolver().query(allImagesuri, projection, null, null, null);
        try {
            if (cursor != null) {
                cursor.moveToLast();
            }
            do {
                imageFolder folds = new imageFolder();
                String name = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME));
                String folder = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME));
                String datapath = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA));


//                String folderpaths =  datapath.replace(name,"");
                String folderpaths = datapath.substring(0, datapath.lastIndexOf(folder + "/"));
                folderpaths = folderpaths + folder + "/" + name;
                if (folderpaths.contains(".mp4") || folderpaths.contains(".avi")) {

                    if (!picPaths.contains(folderpaths)) {
                        picPaths.add(folderpaths);

                        folds.setPath(folderpaths);
                        folds.setFolderName(folder);
                        folds.setFirstPic(datapath);//if the folder has only one picture this line helps to set it as first so as to avoid blank image in itemview
                        folds.addpics();
                        picFolders.add(folds);
                    } else {
                        for (int i = 0; i < picFolders.size(); i++) {
                            if (picFolders.get(i).getPath().equals(folderpaths)) {
                                picFolders.get(i).setFirstPic(datapath);
                                picFolders.get(i).addpics();
                            }
                        }
                    }
                }
            }while(cursor.moveToPrevious());
            cursor.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return picFolders;
    }

    private ArrayList<imageFolder> getPicturePathsFolderSpinnerImage(){
        ArrayList<imageFolder> picFolders = new ArrayList<>();
        ArrayList<String> picPaths = new ArrayList<>();
        Uri allImagesuri = android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        String[] projection = { MediaStore.Images.ImageColumns.DATA ,MediaStore.Images.Media.DISPLAY_NAME,
                MediaStore.Images.Media.BUCKET_DISPLAY_NAME,MediaStore.Images.Media.BUCKET_ID};
        Cursor cursor = this.getContentResolver().query(allImagesuri, projection, null, null, null);
        try {
            if (cursor != null) {
                cursor.moveToLast();
            }
            do {
                imageFolder folds = new imageFolder();
                String folder = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME));
                String datapath = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA));

//                String folderpaths =  datapath.replace(name,"");
                String folderpaths = datapath.substring(0, datapath.lastIndexOf(folder + "/"));
                folderpaths = folderpaths + folder + "/";
                if (!picPaths.contains("all")){
                    picPaths.add("all");
                    folds.setFirstPic(datapath);
                    folds.addpics();
                    picFolders.add(folds);
                }
                if (!picPaths.contains(folderpaths)) {
                    picPaths.add(folderpaths);

                    folds.setPath(folderpaths);
                    folds.setFolderName(folder);
                    folds.setFirstPic(datapath);//if the folder has only one picture this line helps to set it as first so as to avoid blank image in itemview
                    folds.addpics();
                    picFolders.add(folds);
                }
            }while(cursor.moveToPrevious());
            cursor.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return picFolders;
    }

    private ArrayList<imageFolder> getPicturePathsFolder(String folder_name){
        ArrayList<imageFolder> picFolders = new ArrayList<>();
        ArrayList<String> picPaths = new ArrayList<>();
        Uri allImagesuri = android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        String[] projection = { MediaStore.Images.ImageColumns.DATA ,MediaStore.Images.Media.DISPLAY_NAME,
                MediaStore.Images.Media.BUCKET_DISPLAY_NAME,MediaStore.Images.Media.BUCKET_ID};
        Cursor cursor = this.getContentResolver().query(allImagesuri, projection, null, null, null);
        try {
            if (cursor != null) {
                cursor.moveToLast();
            }
            do {
                imageFolder folds = new imageFolder();
                String name = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME));
                String folder = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME));
                String datapath = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA));

//                String folderpaths =  datapath.replace(name,"");
                String folderpaths = datapath.substring(0, datapath.lastIndexOf(folder + "/"));
                folderpaths = folderpaths + folder + "/" + name;
                if (folder.equals(folder_name)) {
                    if (!picPaths.contains(folderpaths)) {
                        picPaths.add(folderpaths);

                        folds.setPath(folderpaths);
                        folds.setFolderName(folder);
                        folds.setFirstPic(datapath);//if the folder has only one picture this line helps to set it as first so as to avoid blank image in itemview
                        folds.addpics();
                        picFolders.add(folds);
                    } else {
                        for (int i = 0; i < picFolders.size(); i++) {
                            if (picFolders.get(i).getPath().equals(folderpaths)) {
                                picFolders.get(i).setFirstPic(datapath);
                                picFolders.get(i).addpics();
                            }
                        }
                    }
                }
            }while(cursor.moveToPrevious());
            cursor.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return picFolders;
    }

    private ArrayList<imageFolder> getVideoPathsFolder(String folder_name){
        ArrayList<imageFolder> picFolders = new ArrayList<>();
        ArrayList<String> picPaths = new ArrayList<>();
        Uri allImagesuri = android.provider.MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
        String[] projection = { MediaStore.Video.VideoColumns.DATA ,MediaStore.Video.Media.DISPLAY_NAME,
                MediaStore.Video.Media.BUCKET_DISPLAY_NAME,MediaStore.Video.Media.BUCKET_ID};
        Cursor cursor = this.getContentResolver().query(allImagesuri, projection, null, null, null);
        try {
            if (cursor != null) {
                cursor.moveToLast();
            }
            do {
                imageFolder folds = new imageFolder();
                String name = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME));
                String folder = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME));
                String datapath = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA));


//                String folderpaths =  datapath.replace(name,"");
                String folderpaths = datapath.substring(0, datapath.lastIndexOf(folder + "/"));
                folderpaths = folderpaths + folder + "/" + name;
                if (folderpaths.contains(".mp4") || folderpaths.contains(".avi")) {
                    if (folder.equals(folder_name)) {
                        if (!picPaths.contains(folderpaths)) {
                            picPaths.add(folderpaths);

                            folds.setPath(folderpaths);
                            folds.setFolderName(folder);
                            folds.setFirstPic(datapath);//if the folder has only one picture this line helps to set it as first so as to avoid blank image in itemview
                            folds.addpics();
                            picFolders.add(folds);
                        } else {
                            for (int i = 0; i < picFolders.size(); i++) {
                                if (picFolders.get(i).getPath().equals(folderpaths)) {
                                    picFolders.get(i).setFirstPic(datapath);
                                    picFolders.get(i).addpics();
                                }
                            }
                        }
                    }
                }
            }while(cursor.moveToPrevious());
            cursor.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return picFolders;
    }

    private ArrayList<String> makeImageFolders(){
        Uri allImagesuri = android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        String[] projection = new String[]{MediaStore.Images.ImageColumns.DATA, MediaStore.Images.Media.DISPLAY_NAME,
                MediaStore.Images.Media.BUCKET_DISPLAY_NAME, MediaStore.Images.Media.BUCKET_ID};
        Cursor cursor = this.getContentResolver().query(allImagesuri, projection, null, null, null);
        ArrayList<String> dataFolders = new ArrayList<String>();
        dataFolders.add("all");
        try {
            if (cursor != null) {
                cursor.moveToLast();
            }
            do {
                imageFolder folds = new imageFolder();
                String name = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME));
                String folder = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME));
                String datapath = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA));


//                String folderpaths =  datapath.replace(name,"");
                String folderpaths = datapath.substring(0, datapath.lastIndexOf(folder + "/"));
                folderpaths = folderpaths + folder + "/";

                if(!dataFolders.contains(folder)){
                    dataFolders.add(folder);
                }
            }while(cursor.moveToPrevious());
            cursor.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return dataFolders;
    }

    private ArrayList<String> makeVideoFolders(){
        Uri allImagesuri = android.provider.MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
        String[] projection = new String[]{MediaStore.Video.VideoColumns.DATA, MediaStore.Video.Media.DISPLAY_NAME,
                MediaStore.Video.Media.BUCKET_DISPLAY_NAME, MediaStore.Video.Media.BUCKET_ID};
        Cursor cursor = this.getContentResolver().query(allImagesuri, projection, null, null, null);
        ArrayList<String> dataFolders = new ArrayList<String>();
        dataFolders.add("all");
        try {
            if (cursor != null) {
                cursor.moveToLast();
            }
            do {
                imageFolder folds = new imageFolder();
                String name = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME));
                String folder = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME));
                String datapath = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA));


//                String folderpaths =  datapath.replace(name,"");
                String folderpaths = datapath.substring(0, datapath.lastIndexOf(folder + "/"));
                folderpaths = folderpaths + folder + "/";

                if(!dataFolders.contains(folder)){
                    dataFolders.add(folder);
                }
            }while(cursor.moveToPrevious());
            cursor.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return dataFolders;
    }

    private void changeView(int index) {
        FrameLayout image_gal_frame = (FrameLayout) findViewById(R.id.image_gal_frame) ;
        FrameLayout video_gal_frame = (FrameLayout) findViewById(R.id.video_gal_frame) ;
//        imageAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item,ImagedataFolders);
        imageAdapter = new spinnerAdapter(DataSelectActivity.this, ImagedataFolders,SpinnerImages);
//        videoAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item,VideodataFolders);

        switch (index) {
            case 0 :

                image_gal_frame.setVisibility(View.VISIBLE) ;
                video_gal_frame.setVisibility(View.GONE) ;
                video_spinner.setVisibility(View.GONE);
                image_spinner.setVisibility(View.VISIBLE);
                Log.d("click", "tab 1");
                if (funcFlag.equals("mosaic")) {
                    funcFlag_mosaic = "mosaic";
                }
                break ;
            case 1 :


                video_spinner.setVisibility(View.VISIBLE);
                image_spinner.setVisibility(View.GONE);
                image_gal_frame.setVisibility(View.GONE) ;
                video_gal_frame.setVisibility(View.VISIBLE) ;
                Log.d("click", "tab 2");
                Log.d("click", funcFlag);
                if (funcFlag.equals("mosaic") ) {
                    funcFlag_mosaic = "mosaic_video";
                }
                break ;

        }
    }

    @Override
    public void onBackPressed() {
//        Intent move = new Intent(DataSelectActivity.this, MainActivity.class);
//        startActivity(move);
        finish();
    }
    @Override
    public void onPicClicked(String pictureFolderPath) {
        Log.d("click", funcFlag_mosaic);
        if (funcFlag.equals("enhance")) {
            Intent move = new Intent(DataSelectActivity.this, ImageEnhanceActivity.class);
            move.putExtra("folderPath", pictureFolderPath);
            Log.d("click", "11221221212112212112");
            //move.putExtra("recyclerItemSize",getCardsOptimalWidth(4));
            startActivity(move);
        }else if(funcFlag_mosaic.equals("mosaic")){
            Intent move = new Intent(DataSelectActivity.this, MosaicActivity.class);
            move.putExtra("folderPath", pictureFolderPath);
            Log.d("click", "mosaic");
            //move.putExtra("recyclerItemSize",getCardsOptimalWidth(4));
            startActivity(move);
        }else if(funcFlag_mosaic.equals("mosaic_video")){
            Intent move = new Intent(DataSelectActivity.this, VideoMosaicActivity.class);
            move.putExtra("folderPath", pictureFolderPath);
            Log.d("click", "mosaic video");
            //move.putExtra("recyclerItemSize",getCardsOptimalWidth(4));
            startActivity(move);
        }
    }

}