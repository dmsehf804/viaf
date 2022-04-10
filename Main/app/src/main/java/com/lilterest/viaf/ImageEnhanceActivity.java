package com.lilterest.viaf;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.lilterest.viaf.utils.imageFolder;

import com.lilterest.viaf.SR.SRUtil;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import net.alhazmy13.imagefilter.ImageFilter;

import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.photo.Photo;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ImageEnhanceActivity extends AppCompatActivity {
    private Bitmap srImage;
    private Bitmap srImage_tmp;
    private Boolean srFlag = false;
    private Bitmap input_bitmap;

    private SeekBar denoiseSeek;
    private SeekBar contrastSeek;
    private SeekBar brightSeek;
    private SeekBar smoothSeek;

    private float contrast=1.0f;
    private float bright=1.0f;
    private float smoothValue=1.0f;

    private Button infoBtn;
    private Button saveBtn;

    private Button denoiseBtn;
    private Button contrastBtn;
    private Button brightBtn;
    private Button smoothBtn;
    private Button resizeBtn;

    private Button resize05;
    private Button resize2;
    private Button resize3;
    private Button resize4;

    private Button x_btn_denoise;
    private Button x_btn_contrast;
    private Button x_btn_bright;
    private Button x_btn_smooth;

    private Button v_btn_denoise;
    private Button v_btn_contrast;
    private Button v_btn_bright;
    private Button v_btn_smooth;

    private Bitmap bitmap;

    ProcessingDialog progressDialog;

    private ImageView ori_imageView;
    private ImageView sr_imageView;

    private CardView srBtnCardview;
    private CardView postprocessCardview;

    private CardView denoiseSeekCard;
    private CardView contrastSeekCard;
    private CardView brightSeekCard;
    private CardView smoothSeekCard;
    private CardView resizeSeekCard;

    private boolean dflag = false;
    private boolean cflag = false;
    private boolean bflag = false;
    private boolean sflag = false;
    private boolean rflag = false;

    private int dProgress = 10;
    private int cProgress = 100;
    private int bProgress = 100;
    private int sProgress = 100;

    private int dProgress_tmp = 10;
    private int cProgress_tmp = 100;
    private int bProgress_tmp = 100;
    private int sProgress_tmp = 100;

    private SimpleDateFormat formatter;
    private int dprogress;
    Button back_btn;
    SRUtil srUtil;
    private Button share_btn;
    private InterstitialAd mInterstitialAd;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enhance_image);
        imageFolder image = new imageFolder();
        Intent intent = getIntent();
        String folderPath = intent.getExtras().getString("folderPath");
        image.setFirstPic(folderPath);
        ori_imageView = (ImageView)findViewById(R.id.original_img);
        sr_imageView = (ImageView)findViewById(R.id.sr_img);
        sr_imageView.setVisibility(View.GONE);
        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(@NonNull InitializationStatus initializationStatus) {
                loadAds();
            }
        });
        loadAds();
        AdView adView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);

        share_btn = findViewById(R.id.share);
        share_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(android.content.Intent.ACTION_SEND);
//                intent.putExtra("srimage", srImage);
                intent.setType("image/*");
                String bitmapPath = MediaStore.Images.Media.insertImage(getContentResolver(), srImage, "title", null); //이미지를 insert하고
                Uri bitmapUri = Uri.parse(bitmapPath);//경로를 통해서 Uri를 만들어서
                intent.putExtra(Intent.EXTRA_STREAM, bitmapUri);
                Intent chooser = Intent.createChooser(intent, "Share");
                startActivity(chooser);


            }
        });



        back_btn = findViewById(R.id.back_btn);

        back_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        Glide.with(ImageEnhanceActivity.this)
                .load(folderPath)
                .into(ori_imageView);
        Glide.with(getApplicationContext()).asBitmap().load(folderPath)
                .into(new SimpleTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(Bitmap resource, Transition<? super Bitmap> transition) {
                        input_bitmap = resource;
                    }
                });

        infoBtn = findViewById(R.id.info_btn);
        infoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InfoDialog infoDialog = new InfoDialog(ImageEnhanceActivity.this);
                infoDialog.callFunction("Image enhance process \n1. Click Enhancing button\n2. Use post-processing\n3. Save!");
            }
        });

        saveBtn = findViewById(R.id.save_btn);
        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (srImage != null)
                    formatter = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss", Locale.US);
                    Date now = new Date();
                    String fileName = formatter.format(now) + ".png";
                    if (Build.VERSION.SDK_INT >=29) {
                        try {
                            MediaStore.Images.Media.insertImage(getContentResolver(), srImage, fileName, "output");
                            Toast.makeText(getApplicationContext(), "Save completed", Toast.LENGTH_SHORT).show();
                        }catch (Exception e){
                            Toast.makeText(getApplicationContext(), "Save failed", Toast.LENGTH_SHORT).show();
                        }
                    }
                    else {
                        File tempFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), fileName);    // 파일 경로와 이름 넣기
                        try {
                            tempFile.createNewFile();   // 자동으로 빈 파일을 생성하기
                            FileOutputStream out = new FileOutputStream(tempFile);  // 파일을 쓸 수 있는 스트림을 준비하기
                            srImage.compress(Bitmap.CompressFormat.PNG, 100, out);   // compress 함수를 사용해 스트림에 비트맵을 저장하기
                            out.close();    // 스트림 닫아주기
                            getApplicationContext().sendBroadcast(new Intent( Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(tempFile)) );

                            Toast.makeText(getApplicationContext(), "Save completed", Toast.LENGTH_SHORT).show();
                        } catch (Exception e) {
                            e.printStackTrace();
                            Toast.makeText(getApplicationContext(), "Save failed", Toast.LENGTH_SHORT).show();
                        }
                }
                SRDialog srDialog = new SRDialog(getApplicationContext(), ImageEnhanceActivity.this,"enhancing");
                srDialog.callFunction();
            }


        });

        srUtil = new SRUtil();
        ori_imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(srImage != null){
                    sr_imageView.setVisibility(View.VISIBLE);
                    ori_imageView.setVisibility(View.GONE);
                }
            }
        });

        sr_imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sr_imageView.setVisibility(View.GONE);
                ori_imageView.setVisibility(View.VISIBLE);
            }
        });

        Button sr_btn = findViewById(R.id.sr_btn);
        sr_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(input_bitmap != null) {
                    showAds();
                    predict(input_bitmap);
                    share_btn.setVisibility(View.VISIBLE);


                }
            }
        });

        if(input_bitmap != null) {
            srImage_tmp = Bitmap.createBitmap(input_bitmap.getWidth() * 2, input_bitmap.getHeight() * 2, input_bitmap.getConfig());
        }
        srBtnCardview = findViewById(R.id.sr_btn_cardview);
        postprocessCardview = findViewById(R.id.postprocess_cardview);

        denoiseSeekCard = findViewById(R.id.denoise_seek_card);
        denoiseBtn = findViewById(R.id.denoise_btn);
        denoiseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(dflag) {
                    denoiseSeekCard.setVisibility(View.GONE);
                    sr_imageView.setClickable(true);
                    dflag = false;
                    denoiseBtn.setBackground(ImageEnhanceActivity.this.getResources().getDrawable(R.drawable.deniose_icon));
                }
                else {
                    menuSwitcher("denoise");
                }
            }
        });

        x_btn_denoise = findViewById(R.id.x_btn_denoise);
        x_btn_denoise.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                denoiseSeekCard.setVisibility(View.GONE);
                denoiseBtn.setBackground(ImageEnhanceActivity.this.getResources().getDrawable(R.drawable.deniose_icon));
                dflag = false;
                denoiseSeek.setProgress(dProgress_tmp);
                sr_imageView.setImageBitmap(srImage);
            }
        });

        v_btn_denoise = findViewById(R.id.v_btn_denoise);
        v_btn_denoise.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (srImage_tmp != null) {
                    srImage = srImage_tmp;

                }
                denoiseSeekCard.setVisibility(View.GONE);
                denoiseBtn.setBackground(ImageEnhanceActivity.this.getResources().getDrawable(R.drawable.deniose_icon));
                dflag = false;
            }
        });
        denoiseSeek = (SeekBar) findViewById(R.id.denoise_seek);
        denoiseSeek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean b) {
                dProgress = progress;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                dProgress_tmp = dProgress;
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressDialog = new ProcessingDialog(ImageEnhanceActivity.this);
                        progressDialog.callFunction();
                    }
                });

                new denoiseTask().execute();

            }
        });
        denoiseSeek.setMax(20);
        denoiseSeek.setProgress(10);


        contrastSeekCard = findViewById(R.id.contrast_seek_card);
        contrastBtn = findViewById(R.id.contrast_btn);
        contrastBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(cflag) {
                    contrastSeekCard.setVisibility(View.GONE);
                    sr_imageView.setClickable(true);
                    cflag = false;
                    contrastBtn.setBackground(ImageEnhanceActivity.this.getResources().getDrawable(R.drawable.contrant_icon));
                }
                else {
                    menuSwitcher("contrast");
                }
            }
        });
        x_btn_contrast = findViewById(R.id.x_btn_contrast);
        x_btn_contrast.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                contrastSeekCard.setVisibility(View.GONE);
                contrastBtn.setBackground(ImageEnhanceActivity.this.getResources().getDrawable(R.drawable.contrant_icon));
                cflag = false;
                contrastSeek.setProgress(cProgress_tmp);
                sr_imageView.setImageBitmap(srImage);
            }
        });

        v_btn_contrast = findViewById(R.id.v_btn_contrast);
        v_btn_contrast.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (srImage_tmp != null) {
                    srImage = srImage_tmp;

                }
                contrastSeekCard.setVisibility(View.GONE);
                contrastBtn.setBackground(ImageEnhanceActivity.this.getResources().getDrawable(R.drawable.contrant_icon));
                cflag = false;
            }
        });
        contrastSeek = (SeekBar) findViewById(R.id.contrast_seek);
        contrastSeek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean b) {
                cProgress = progress;

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                cProgress_tmp = cProgress;
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                new contrastTask().execute();
                progressDialog = new ProcessingDialog(ImageEnhanceActivity.this);
                progressDialog.callFunction();
            }
        });

        contrastSeek.setMax(200);
        contrastSeek.setProgress(100);

        brightSeekCard = findViewById(R.id.bright_seek_card);
        brightBtn = findViewById(R.id.bright_btn);
        brightBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(bflag) {
                    brightSeekCard.setVisibility(View.GONE);
                    sr_imageView.setClickable(true);
                    bflag = false;
                    brightBtn.setBackground(ImageEnhanceActivity.this.getResources().getDrawable(R.drawable.brightness_icon));
                }
                else {
                    menuSwitcher("bright");
                }
            }
        });
        x_btn_bright = findViewById(R.id.x_btn_bright);
        x_btn_bright.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                brightSeekCard.setVisibility(View.GONE);
                brightBtn.setBackground(ImageEnhanceActivity.this.getResources().getDrawable(R.drawable.brightness_icon));
                bflag = false;
                brightSeek.setProgress(bProgress_tmp);
                sr_imageView.setImageBitmap(srImage);
            }
        });

        v_btn_bright = findViewById(R.id.v_btn_bright);
        v_btn_bright.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (srImage_tmp != null) {
                    srImage = srImage_tmp;

                }
                brightSeekCard.setVisibility(View.GONE);
                brightBtn.setBackground(ImageEnhanceActivity.this.getResources().getDrawable(R.drawable.brightness_icon));
                bflag = false;
            }
        });
        brightSeek = (SeekBar) findViewById(R.id.bright_seek);
        brightSeek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean b) {

                bProgress = progress;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                bProgress_tmp=bProgress;
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                new brightTask().execute();
                progressDialog = new ProcessingDialog(ImageEnhanceActivity.this);
                progressDialog.callFunction();
            }
        });

        brightSeek.setMax(200);
        brightSeek.setProgress(100);


        smoothSeekCard = findViewById(R.id.smooth_seek_card);
        smoothBtn = findViewById(R.id.smooth_btn);
        smoothBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(sflag) {
                    smoothSeekCard.setVisibility(View.GONE);
                    sr_imageView.setClickable(true);
                    sflag = false;
                    smoothBtn.setBackground(ImageEnhanceActivity.this.getResources().getDrawable(R.drawable.smoothing_icon));
                }
                else {
                    menuSwitcher("smooth");
                }
            }
        });
        x_btn_smooth = findViewById(R.id.x_btn_smooth);
        x_btn_smooth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                smoothSeekCard.setVisibility(View.GONE);
                smoothBtn.setBackground(ImageEnhanceActivity.this.getResources().getDrawable(R.drawable.smoothing_icon));
                sflag = false;
                smoothSeek.setProgress(sProgress_tmp);
                sr_imageView.setImageBitmap(srImage);
            }
        });

        v_btn_smooth = findViewById(R.id.v_btn_smooth);
        v_btn_smooth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (srImage_tmp != null) {
                    srImage = srImage_tmp;

                }
                smoothSeekCard.setVisibility(View.GONE);
                smoothBtn.setBackground(ImageEnhanceActivity.this.getResources().getDrawable(R.drawable.smoothing_icon));
                sflag = false;
            }
        });

        smoothSeek = (SeekBar) findViewById(R.id.smooth_seek);
        smoothSeek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean b) {

                sProgress = progress;

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                sProgress_tmp = sProgress;
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                progressDialog = new ProcessingDialog(ImageEnhanceActivity.this);
                progressDialog.callFunction();
                new smoothTask().execute();

            }
        });

        smoothSeek.setMax(200);
        smoothSeek.setProgress(100);

    }

    private void showAds(){
        if (mInterstitialAd != null){
            mInterstitialAd.show(this);

        }else{
            Log.d("TAG","no ad");
        }
    }

    private void loadAds(){
        AdRequest adRequest = new AdRequest.Builder().build();

        InterstitialAd.load(this, "ca-app-pub-3202401163905235/1077341049", adRequest, new InterstitialAdLoadCallback() {
            @Override
            public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                mInterstitialAd = interstitialAd;
                Log.i("TAG", "onAdLoaded");

                mInterstitialAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                    @Override
                    public void onAdDismissedFullScreenContent(){
                        Log.d("TAG"," ad dismissed");
                        loadAds();
                    }
                    @Override
                    public void onAdFailedToShowFullScreenContent(@NonNull AdError adError) {
                        Log.d("TAG","no ad show");
                    }
                    @Override
                    public void onAdShowedFullScreenContent(){
                        Log.d("TAG","ad showed");
                        mInterstitialAd = null;
                    }
                });
            }
            @Override
            public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                mInterstitialAd = null;
                Log.i("TAG",loadAdError.getMessage());
            }
        });
    }
    @Override
    public void onBackPressed() {
//        Intent move = new Intent(DataSelectActivity.this, MainActivity.class);
//        startActivity(move);
        finish();
    }
    public void menuSwitcher(String tag){
        if (tag == "denoise"){
//            sr_imageView.setClickable(false);
            denoiseSeekCard.setVisibility(View.VISIBLE);
            denoiseBtn.setBackground(ImageEnhanceActivity.this.getResources().getDrawable(R.drawable.deniose_icon_blue));
            dflag = true;

            contrastSeekCard.setVisibility(View.GONE);
            cflag = false;
            contrastBtn.setBackground(ImageEnhanceActivity.this.getResources().getDrawable(R.drawable.contrant_icon));
            contrastSeek.setProgress(cProgress_tmp);
            brightSeekCard.setVisibility(View.GONE);
            bflag = false;
            brightBtn.setBackground(ImageEnhanceActivity.this.getResources().getDrawable(R.drawable.brightness_icon));
            brightSeek.setProgress(bProgress_tmp);
            smoothSeekCard.setVisibility(View.GONE);
            sflag = false;
            smoothBtn.setBackground(ImageEnhanceActivity.this.getResources().getDrawable(R.drawable.smoothing_icon));
            smoothSeek.setProgress(sProgress_tmp);
            sr_imageView.setImageBitmap(srImage);
        }else if(tag == "contrast"){
//            sr_imageView.setClickable(false);
            sr_imageView.setImageBitmap(srImage);
            denoiseSeekCard.setVisibility(View.GONE);
            denoiseBtn.setBackground(ImageEnhanceActivity.this.getResources().getDrawable(R.drawable.deniose_icon));
            dflag = false;
            denoiseSeek.setProgress(dProgress_tmp);
            contrastSeekCard.setVisibility(View.VISIBLE);
            cflag = true;
            contrastBtn.setBackground(ImageEnhanceActivity.this.getResources().getDrawable(R.drawable.contrant_icon_blue));

            brightSeekCard.setVisibility(View.GONE);
            bflag = false;
            brightBtn.setBackground(ImageEnhanceActivity.this.getResources().getDrawable(R.drawable.brightness_icon));
            brightSeek.setProgress(bProgress_tmp);
            smoothSeekCard.setVisibility(View.GONE);
            sflag = false;
            smoothBtn.setBackground(ImageEnhanceActivity.this.getResources().getDrawable(R.drawable.smoothing_icon));
            smoothSeek.setProgress(sProgress_tmp);
        }else if(tag == "bright"){
//            sr_imageView.setClickable(false);
            sr_imageView.setImageBitmap(srImage);
            denoiseSeekCard.setVisibility(View.GONE);
            denoiseBtn.setBackground(ImageEnhanceActivity.this.getResources().getDrawable(R.drawable.deniose_icon));
            dflag = false;
            denoiseSeek.setProgress(dProgress_tmp);
            contrastSeekCard.setVisibility(View.GONE);
            cflag = false;
            contrastBtn.setBackground(ImageEnhanceActivity.this.getResources().getDrawable(R.drawable.contrant_icon));
            contrastSeek.setProgress(cProgress_tmp);
            brightSeekCard.setVisibility(View.VISIBLE);
            bflag = true;
            brightBtn.setBackground(ImageEnhanceActivity.this.getResources().getDrawable(R.drawable.brightness_icon_blue));

            smoothSeekCard.setVisibility(View.GONE);
            sflag = false;
            smoothBtn.setBackground(ImageEnhanceActivity.this.getResources().getDrawable(R.drawable.smoothing_icon));
            smoothSeek.setProgress(sProgress_tmp);
        }else if(tag == "smooth"){
//            sr_imageView.setClickable(false);
            sr_imageView.setImageBitmap(srImage);
            denoiseSeekCard.setVisibility(View.GONE);
            denoiseBtn.setBackground(ImageEnhanceActivity.this.getResources().getDrawable(R.drawable.deniose_icon));
            dflag = false;
            denoiseSeek.setProgress(dProgress_tmp);
            contrastSeekCard.setVisibility(View.GONE);
            cflag = false;
            contrastBtn.setBackground(ImageEnhanceActivity.this.getResources().getDrawable(R.drawable.contrant_icon));
            contrastSeek.setProgress(cProgress_tmp);
            brightSeekCard.setVisibility(View.GONE);
            bflag = false;
            brightBtn.setBackground(ImageEnhanceActivity.this.getResources().getDrawable(R.drawable.brightness_icon));
            brightSeek.setProgress(bProgress_tmp);
            smoothSeekCard.setVisibility(View.VISIBLE);
            sflag = true;
            smoothBtn.setBackground(ImageEnhanceActivity.this.getResources().getDrawable(R.drawable.smoothing_icon_blue));


        }

    }
    public void predict(Bitmap bitmap) {
        this.bitmap = bitmap;
//        new Thread(new threadDialog()).start();

        new proDig().execute();
        progressDialog = new ProcessingDialog(ImageEnhanceActivity.this);
        progressDialog.callFunction();
    }

    private class proDig extends AsyncTask<Bitmap, Void, Void> {

        @Override
        protected Void doInBackground(Bitmap... bitmaps) {
            if (bitmap != null) {
                Bitmap resultBitmap = bitmap;

                Mat imageMat = new Mat();
                Utils.bitmapToMat(resultBitmap, imageMat);
                Photo.detailEnhance(imageMat, imageMat, 5.0f, 0.15f);

//                Photo.fastNlMeansDenoisingColored(imageMat, imageMat, 10, 10, 3, 15);
                Utils.matToBitmap(imageMat, resultBitmap);
                ImageFilter.applyFilter(resultBitmap, ImageFilter.Filter.GAUSSIAN_BLUR);
                progressDialog.dismiss();
                if (resultBitmap != null) {
                    srImage = resultBitmap;
                    srFlag = true;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            postView();
                        }
                    });
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            sr_imageView.setImageBitmap(resultBitmap);
                        }
                    });

                }
            }
            return null;
        }
    }

    private class denoiseTask extends AsyncTask<Bitmap, Void, Void> {

        @Override
        protected Void doInBackground(Bitmap... bitmaps) {

                if(srImage != null) {
                    srImage_tmp = SRUtil.denoise(srImage, dprogress);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            sr_imageView.setImageBitmap(srImage_tmp);


                        }
                    });
                }
                progressDialog.dismiss();
            return null;
        }
    }

    private class contrastTask extends AsyncTask<Bitmap, Void, Void> {

        @Override
        protected Void doInBackground(Bitmap... bitmaps) {
            if(srImage != null) {
                contrast = (float) (cProgress) / 100.f;
                srImage_tmp = srUtil.changeBitmapContrastBrightness(srImage, contrast, bright);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        sr_imageView.setImageBitmap(srImage_tmp);

                    }
                });
            }
            progressDialog.dismiss();

            return null;
        }
    }
    private class brightTask extends AsyncTask<Bitmap, Void, Void> {

        @Override
        protected Void doInBackground(Bitmap... bitmaps) {
            if(srImage != null) {
                bright = (float) (bProgress - 100.f) / 10.f;
                srImage_tmp = srUtil.changeBitmapContrastBrightness(srImage, contrast, bright);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        sr_imageView.setImageBitmap(srImage_tmp);

                    }
                });
            }
            progressDialog.dismiss();
            return null;
        }
    }

    private class smoothTask extends AsyncTask<Bitmap, Void, Void> {

        @Override
        protected Void doInBackground(Bitmap... bitmaps) {
            if(srImage != null) {
                smoothValue = (float) (sProgress-100.f) / 10.f;
                srImage_tmp = srUtil.applySmoothEffect(srImage, smoothValue);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        sr_imageView.setImageBitmap(srImage_tmp);

                    }
                });

            }
            progressDialog.dismiss();
            return null;
        }
    }
    private void preView(){
        sr_imageView.setVisibility(View.GONE);
        ori_imageView.setVisibility(View.VISIBLE);


    }
    private void postView(){
        Log.d("post","0");
        if (srFlag){

            Log.d("post","1");
            sr_imageView.setVisibility(View.VISIBLE);
            postprocessCardview.setVisibility(View.VISIBLE);
            saveBtn.setVisibility(View.VISIBLE);

            infoBtn.setVisibility(View.GONE);
            ori_imageView.setVisibility(View.GONE);
            srBtnCardview.setVisibility(View.GONE);
            denoiseSeekCard.setVisibility(View.VISIBLE);
            denoiseBtn.setBackground(ImageEnhanceActivity.this.getResources().getDrawable(R.drawable.deniose_icon_blue));
            dflag = true;
        }
    }
}