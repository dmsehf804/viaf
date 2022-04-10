package com.lilterest.viaf;

import android.app.Activity;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Pair;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.SeekBar;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.face.Face;
import com.google.mlkit.vision.face.FaceDetection;
import com.google.mlkit.vision.face.FaceDetector;
import com.google.mlkit.vision.face.FaceDetectorOptions;
import com.lilterest.viaf.Mosaic.MosaicUtils;
import com.lilterest.viaf.Mosaic.SimilarityClassifier;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.VideoWriter;
import org.tensorflow.lite.Interpreter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static android.os.Environment.DIRECTORY_DCIM;

public class VideoMosaicActivity_backup extends AppCompatActivity {
    private VideoView ori_imageView;
    private VideoView mosaic_imageView;
    private ImageView imageView;
    private Bitmap input_bitmap;
    private Button infoBtn;
    private Button save_btn;
    private Button mosaic_btn;
    private Button saveBtn;
    private Bitmap boxedBitmap;
    private Bitmap blurredBitmap;
    VideoCapture vc;
    VideoWriter videoWriter;
    FaceDetector detector;
    ProcessingDialog progressDialog;
    MosaicUtils mosaicUtils;
    Interpreter tfLite;
    private Bitmap outputBitmap;
    String modelFile="mobile_face_net.tflite"; //model name
    int[] intValues;
    int inputSize=112;  //Input size for model
    boolean isModelQuantized=false;
    float[][] embeedings;
    float IMAGE_MEAN = 128.0f;
    float IMAGE_STD = 128.0f;
    int OUTPUT_SIZE=192; //Output size of model
    boolean start=true,flipX=false;
    private int i = 0;
    private int detect_idx=0;
    private LinearLayout face_list_layout;
    private List<Face> faceList;
    private ArrayList<Integer> face_list;
    private HashMap<String, SimilarityClassifier.Recognition> registered = new HashMap<>(); //saved Faces
    private String folderPath;
    private CardView postProc;
    private SimpleDateFormat formatter;
    private SeekBar blurSeek;
    private SeekBar pixelableSeek;

    private Button blurBtn;
    private Button pixelableBtn;

    private Button x_btn_denoise;
    private Button x_btn_contrast;

    private Button v_btn_denoise;
    private Button v_btn_contrast;

    private CardView blurSeekCard;
    private CardView pixelableSeekCard;

    private int pixelableParam = 60;
    private int blurParam = 3;
    private boolean pflag = false;
    private boolean bflag = false;
    private Button share_btn;
    private CardView postprocessCardview;
    private InterstitialAd mInterstitialAd;

    private int blurParam_tmp = 0;
    private int pixelableParam_tmp = 0;
    private Bitmap blurredBitmap_ori;
    Button back_btn;
    String btnad_id = "ca-app-pub-3202401163905235/8042204640";
    String openad_id = "ca-app-pub-3202401163905235/1327131793";
    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mosaic_video);
        share_btn = findViewById(R.id.share);
        share_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_SEND);
//                intent.putExtra("srimage", srImage);
                intent.setType("image/*");
                String bitmapPath = MediaStore.Images.Media.insertImage(getContentResolver(), blurredBitmap, "title", null); //이미지를 insert하고
                Uri bitmapUri = Uri.parse(bitmapPath);//경로를 통해서 Uri를 만들어서
                intent.putExtra(Intent.EXTRA_STREAM, bitmapUri);
                Intent chooser = Intent.createChooser(intent, "Share");
                startActivity(chooser);

            }
        });

        modelInit();
        Intent intent = getIntent();
        folderPath = intent.getExtras().getString("folderPath");
        ori_imageView = (VideoView)findViewById(R.id.original_img);
        mosaic_imageView = (VideoView)findViewById(R.id.sr_img);
        back_btn = findViewById(R.id.back_btn);
        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(@NonNull InitializationStatus initializationStatus) {
                loadAds();
            }
        });
        loadAds();
        showAds();
        AdView adView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);

        back_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        ori_imageView.setVideoPath(folderPath);

        MediaController mediaController = new MediaController(this);
        ori_imageView.setMediaController(mediaController);
        mediaController.setAnchorView(ori_imageView);
        ori_imageView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
//                ori_imageView.start();

            }
        });
        ori_imageView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                ori_imageView.start();
                new VideoMosaicActivity_backup.detectFaceTask().execute();
            }
        });
        progressDialog = new ProcessingDialog(VideoMosaicActivity_backup.this);
        progressDialog.callFunction();

        infoBtn = findViewById(R.id.info_btn);
        infoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InfoDialog infoDialog = new InfoDialog(VideoMosaicActivity_backup.this);
                infoDialog.callFunction("Auto mosaic process \n1. Click bottom face for exception\n2. Click Auto mosaic button\n3. Use post-processing\n4. Save!");
            }
        });
        mosaicUtils = new MosaicUtils(VideoMosaicActivity_backup.this);

        postProc = findViewById(R.id.postprocess_cardview);
        mosaic_btn = findViewById(R.id.mosaic_btn);
        save_btn = findViewById(R.id.save_btn);
        mosaic_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(input_bitmap != null) {
                    showAds();
                    share_btn.setVisibility(View.VISIBLE);
                    new mosaicFaceTask().execute();
                    face_list_layout.setVisibility(View.GONE);
                    postProc.setVisibility(View.VISIBLE);
                    save_btn.setVisibility(View.VISIBLE);
                    infoBtn.setVisibility(View.GONE);
                    mosaic_btn.setVisibility(View.GONE);
                    pixelableSeekCard.setVisibility(View.VISIBLE);
                    pixelableBtn.setBackground(VideoMosaicActivity_backup.this.getResources().getDrawable(R.drawable.mosaic_blue));
                    pflag = true;

                }
            }
        });

        postprocessCardview = findViewById(R.id.postprocess_cardview);

        pixelableSeekCard = findViewById(R.id.pixelable_seek_card);
        pixelableBtn = findViewById(R.id.pixel_btn);
        pixelableBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(pflag) {
                    pixelableSeekCard.setVisibility(View.GONE);
                    pflag = false;
                    pixelableBtn.setBackground(VideoMosaicActivity_backup.this.getResources().getDrawable(R.drawable.mosaic));
                }
                else {
                    menuSwitcher("pixelable");
                }
            }
        });

        x_btn_denoise = findViewById(R.id.x_btn_pixelable);
        x_btn_denoise.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pixelableSeekCard.setVisibility(View.GONE);
                pixelableBtn.setBackground(VideoMosaicActivity_backup.this.getResources().getDrawable(R.drawable.mosaic));
                pflag = false;
            }
        });

        v_btn_denoise = findViewById(R.id.v_btn_pixelable);
        v_btn_denoise.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pixelableSeekCard.setVisibility(View.GONE);
                pixelableBtn.setBackground(VideoMosaicActivity_backup.this.getResources().getDrawable(R.drawable.mosaic));
                pflag = false;
            }
        });
        blurSeek = (SeekBar) findViewById(R.id.blur_seek);
        blurSeek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean b) {
                blurParam = progress;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                blurParam_tmp = blurParam;
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                        new postTask().execute();
            }
        });
        blurSeek.setMax(10);
        blurSeek.setMin(1);
        blurSeek.setProgress(3);


        blurSeekCard = findViewById(R.id.blur_seek_card);
        blurBtn = findViewById(R.id.blur_btn);
        blurBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(bflag) {
                    blurSeekCard.setVisibility(View.GONE);
                    bflag = false;
                    blurBtn.setBackground(VideoMosaicActivity_backup.this.getResources().getDrawable(R.drawable.blur));
                }
                else {
                    menuSwitcher("blur");
                }
            }
        });
        x_btn_contrast = findViewById(R.id.x_btn_blur);
        x_btn_contrast.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                blurSeekCard.setVisibility(View.GONE);
                blurBtn.setBackground(VideoMosaicActivity_backup.this.getResources().getDrawable(R.drawable.blur));
                bflag = false;
                blurSeek.setProgress(blurParam_tmp);
            }
        });

        v_btn_contrast = findViewById(R.id.v_btn_blur);
        v_btn_contrast.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                blurSeekCard.setVisibility(View.GONE);
                blurBtn.setBackground(VideoMosaicActivity_backup.this.getResources().getDrawable(R.drawable.blur));
                bflag = false;
            }
        });
        pixelableSeek = (SeekBar) findViewById(R.id.pixelable_seek);
        pixelableSeek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean b) {
                pixelableParam = progress;

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                pixelableParam_tmp = pixelableParam;
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                new postTask().execute();
            }
        });

        pixelableSeek.setMax(100);
        pixelableSeek.setMin(1);
        pixelableSeek.setProgress(60);

        saveBtn = findViewById(R.id.save_btn);
        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAds();
                if (blurredBitmap != null)
                    formatter = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss", Locale.US);
                Date now = new Date();
                String fileName = formatter.format(now) + ".png";

                if (Build.VERSION.SDK_INT >= 29) {
                    try {
                        MediaStore.Images.Media.insertImage(getContentResolver(), blurredBitmap, fileName, "output");
                        Toast.makeText(getApplicationContext(), "Save completed", Toast.LENGTH_SHORT).show();
                    }catch (Exception e){
                        Toast.makeText(getApplicationContext(), "Save failed", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    File tempFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), fileName);
                    try {
                        tempFile.createNewFile();
                        FileOutputStream out = new FileOutputStream(tempFile);
                        blurredBitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
                        out.close();
                        getApplicationContext().sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(tempFile)));

                        Toast.makeText(getApplicationContext(), "Save completed", Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(getApplicationContext(), "Save failed", Toast.LENGTH_SHORT).show();
                    }
                }
                SRDialog srDialog = new SRDialog(getApplicationContext(), VideoMosaicActivity_backup.this,"mosaic");
                srDialog.callFunction();
            }


        });

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

        InterstitialAd.load(this, "ca-app-pub-3202401163905235/8042204640", adRequest, new InterstitialAdLoadCallback() {
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
    private class detectFaceTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            // 스레드가 시작하기 전에 수행할 작업(메인 스레드)
            super.onPreExecute();

        }
        @Override
        protected Void doInBackground(Void... voids) {
            vc = new VideoCapture(folderPath);
            Mat dummy_mat = new Mat();
            vc.read(dummy_mat);

            Log.d("hi", String.valueOf(dummy_mat.size()));
            String filename = Environment.getExternalStoragePublicDirectory(DIRECTORY_DCIM) +"/Camera/" + System.currentTimeMillis() + ".mp4";
            Log.d("hi9",filename);
            videoWriter = new VideoWriter(filename, VideoWriter.fourcc('M', 'J', 'P', 'G'), 25.0, dummy_mat.size());
            try{
                Log.d("videocapture",folderPath);
//                vc.open();
            } catch (Exception e) {
                /// handle error
            }

            if(!vc.isOpened()){
                // this code is always hit
                Log.v("VideoCapture", "failed");
            } else{
                Log.v("VideoCapture", "opened");
            }
            Mat input_mat = new Mat();
            Bitmap bitmap = Bitmap.createBitmap(dummy_mat.cols(), dummy_mat.rows(), Bitmap.Config.ARGB_8888);
            while(vc.isOpened()){
                boolean a = vc.read(input_mat);
                if (!a){
                    break;
                }
                if(input_mat!=null) {
                    Log.d("asssss", String.valueOf(a));
                }
                Core.flip(input_mat.t(), input_mat, 1);
                Utils.matToBitmap(input_mat,bitmap);

                if(bitmap != null) {
                    bitmap = faceDetect(bitmap);
                    Log.d("Mosaic","akakakakak");

//                    Utils.bitmapToMat(bitmap, input_mat);
                    Log.d("hi1", String.valueOf(input_mat.size()));

                    videoWriter.write(input_mat);
                    if(boxedBitmap != null) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Log.d("ssss", String.valueOf(boxedBitmap));
//                                imageView.setImageBitmap(boxedBitmap);
                            }
                        });
                    }

                }
            }

//            vc.retrieve(input_mat,)
            videoWriter.release();
            return null;
        }
        @Override
        protected void onPostExecute(Void result) {
            // 스레드 작업이 모두 끝난 후에 수행할 작업(메인 스레드)
            super.onPostExecute(result);

        }
    }

    private class postTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            // 스레드가 시작하기 전에 수행할 작업(메인 스레드)
            super.onPreExecute();
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    progressDialog = new ProcessingDialog(VideoMosaicActivity_backup.this);
                    progressDialog.callFunction();
                }
            });

        }

        @Override
        protected Void doInBackground(Void... voids) {
            if(input_bitmap != null) {
                blurredBitmap_ori = blurredBitmap;
                blurredBitmap = mosaicRun(input_bitmap);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

//                        ori_imageView.setImageBitmap(blurredBitmap);
                    }
                });
            }
            return null;
        }
        @Override
        protected void onPostExecute(Void result) {
            // 스레드 작업이 모두 끝난 후에 수행할 작업(메인 스레드)
            super.onPostExecute(result);

        }

    }

    private class mosaicFaceTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            // 스레드가 시작하기 전에 수행할 작업(메인 스레드)
            super.onPreExecute();
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    progressDialog = new ProcessingDialog(VideoMosaicActivity_backup.this);
                    progressDialog.callFunction();
                }
            });

        }

        @Override
        protected Void doInBackground(Void... voids) {
            if(input_bitmap != null) {
                blurredBitmap = mosaicRun(input_bitmap);
                Log.d("Mosaic","akakakakak");

                Log.d("Mosaic", "set Imageview");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
//                        ori_imageView.setImageBitmap(blurredBitmap);

                    }
                });

            }

            return null;
        }
        @Override
        protected void onPostExecute(Void result) {
            // 스레드 작업이 모두 끝난 후에 수행할 작업(메인 스레드)
            super.onPostExecute(result);

        }

    }

    private MappedByteBuffer loadModelFile(Activity activity, String MODEL_FILE) throws IOException {
        AssetFileDescriptor fileDescriptor = activity.getAssets().openFd(MODEL_FILE);
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }

    private void modelInit(){
        try {
            tfLite=new Interpreter(loadModelFile(VideoMosaicActivity_backup.this,modelFile));
        } catch (IOException e) {
            e.printStackTrace();
        }
        //Initialize Face Detector
        FaceDetectorOptions highAccuracyOpts =
                new FaceDetectorOptions.Builder()
                        .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
                        .build();
        detector = FaceDetection.getClient(highAccuracyOpts);
    }

    public Rect rectFToRect(RectF rectF, Rect rect) {
        rect.left = Math.round(rectF.left);
        rect.top = Math.round(rectF.top);
        rect.right = Math.round(rectF.right);
        rect.bottom = Math.round(rectF.bottom);

//        org.opencv.core.Rect cv_rect = new org.opencv.core.Rect(rect.left, rect.bottom, rect.left - rect.right, rect.top - rect.bottom);
        return rect;
    }

    private static Bitmap getCropBitmapByCPU(Bitmap source, RectF cropRectF) {
        Bitmap resultBitmap = Bitmap.createBitmap((int) cropRectF.width(),
                (int) cropRectF.height(), Bitmap.Config.ARGB_8888);
        Canvas cavas = new Canvas(resultBitmap);

        // draw background
        Paint paint = new Paint(Paint.FILTER_BITMAP_FLAG);
        paint.setColor(Color.WHITE);
        cavas.drawRect(//from  w w  w. ja v  a  2s. c  om
                new RectF(0, 0, cropRectF.width(), cropRectF.height()),
                paint);

        Matrix matrix = new Matrix();
        matrix.postTranslate(-cropRectF.left, -cropRectF.top);

        cavas.drawBitmap(source, matrix, paint);
//
//        if (source != null && !source.isRecycled()) {
//            source.recycle();
//        }

        return resultBitmap;
    }

    public Bitmap getResizedBitmap(Bitmap bm, int newWidth, int newHeight) {
        int width = bm.getWidth();
        int height = bm.getHeight();
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        // CREATE A MATRIX FOR THE MANIPULATION
        Matrix matrix = new Matrix();
        // RESIZE THE BIT MAP
        matrix.postScale(scaleWidth, scaleHeight);

        // "RECREATE" THE NEW BITMAP
        Bitmap resizedBitmap = Bitmap.createBitmap(
                bm, 0, 0, width, height, matrix, false);
        bm.recycle();
        return resizedBitmap;
    }

    public void recognizeImage(final Bitmap bitmap) {
        ByteBuffer imgData = ByteBuffer.allocateDirect(1 * inputSize * inputSize * 3 * 4);

        imgData.order(ByteOrder.nativeOrder());

        intValues = new int[inputSize * inputSize];

        //get pixel values from Bitmap to normalize
        bitmap.getPixels(intValues, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());

        imgData.rewind();

        for (int i = 0; i < inputSize; ++i) {
            for (int j = 0; j < inputSize; ++j) {
                int pixelValue = intValues[i * inputSize + j];
                if (isModelQuantized) {
                    // Quantized model
                    imgData.put((byte) ((pixelValue >> 16) & 0xFF));
                    imgData.put((byte) ((pixelValue >> 8) & 0xFF));
                    imgData.put((byte) (pixelValue & 0xFF));
                } else { // Float model
                    imgData.putFloat((((pixelValue >> 16) & 0xFF) - IMAGE_MEAN) / IMAGE_STD);
                    imgData.putFloat((((pixelValue >> 8) & 0xFF) - IMAGE_MEAN) / IMAGE_STD);
                    imgData.putFloat(((pixelValue & 0xFF) - IMAGE_MEAN) / IMAGE_STD);

                }
            }
        }
        //imgData is input to our model
        Object[] inputArray = {imgData};

        Map<Integer, Object> outputMap = new HashMap<>();


        embeedings = new float[1][OUTPUT_SIZE]; //output of model will be stored in this variable

        outputMap.put(0, embeedings);

        tfLite.runForMultipleInputsOutputs(inputArray, outputMap); //Run model

        float distance = Float.MAX_VALUE;
        int id_int = (int)(Math.random() * 1000);
        String id_str = String.valueOf(id_int);
        String label = "?";

        //Compare new face with saved Faces.
        if (registered.size() == 0){
            addFace(id_str, distance,embeedings[0]);
        }
        if (registered.size() > 0) {

            final Pair<String, Float> nearest = findNearest(embeedings[0]);//Find closest matching face

            if (nearest != null) {

                final String name = nearest.first;
                label = name;
                distance = nearest.second;
                if(distance<1.000f) //If distance between Closest found face is more than 1.000 ,then output UNKNOWN face.
                    Log.d("Mosaic", "dist < 1.000f");
                    //TODO
                else{
                    final Button btn = new Button(this);
                    btn.setId(id_int);
                    // setId 버튼에 대한 키값
                    Drawable d = new BitmapDrawable(getResources(), bitmap);
                    btn.setBackground(d);

                    btn.setOnClickListener(new View.OnClickListener() {

                        public void onClick(View v) {

                        }
                    });
                }
                    Log.d("Mosaic", "dist > 1.000f");
                System.out.println("nearest: " + name + " - distance: " + distance);


            }
        }
    }

    private SimilarityClassifier.Recognition addFace(String id, float distance, float[] emb)
    {
        start=false;
        SimilarityClassifier.Recognition result = new SimilarityClassifier.Recognition(
                id, distance);
        result.setExtra(emb);
        // Set up the buttons
        return result;
    }

    private Pair<String, Float> findNearest(float[] emb) {

        Pair<String, Float> ret = null;
        for (Map.Entry<String, SimilarityClassifier.Recognition> entry : registered.entrySet()) {

            final String name = entry.getKey();
            final float[] knownEmb = ((float[][]) entry.getValue().getExtra())[0];

            float distance = 0;
            for (int i = 0; i < emb.length; i++) {
                float diff = emb[i] - knownEmb[i];
                distance += diff*diff;
            }
            distance = (float) Math.sqrt(distance);
            if (ret == null || distance < ret.second) {
                ret = new Pair<>(name, distance);
            }
        }

        return ret;

    }


    private Bitmap mosaicRun(Bitmap input_bitmap){

        InputImage impphoto=InputImage.fromBitmap(input_bitmap,0);

        outputBitmap = Bitmap.createBitmap(input_bitmap.getWidth(), input_bitmap.getHeight(), input_bitmap.getConfig());
        if (impphoto!=null) {

            Mat input_mat = new Mat();
            Utils.bitmapToMat(input_bitmap, input_mat);
            Mat input_mat_copy = input_mat.clone();

            for (int i = 0; i<faceList.size(); ++i){
                if(face_list.contains(i)){
                    continue;
                }

                Face face = faceList.get(i);

                RectF boundingBox = new RectF(face.getBoundingBox());
                Rect rect = new Rect();

                rect = rectFToRect(boundingBox, rect);
                Mat input_mat_blur = new Mat();

                Imgproc.resize(input_mat_copy, input_mat_blur, new Size(pixelableParam, pixelableParam), 0, 0, Imgproc.INTER_AREA);
                Imgproc.blur(input_mat_blur,input_mat_blur,new Size(blurParam,blurParam));
                Imgproc.resize(input_mat_blur, input_mat_blur, new Size(input_mat.width(), input_mat.height()), 0, 0, Imgproc.INTER_AREA);

                for (int x = rect.left; x <= rect.right; ++x) {
                    for (int y = rect.top; y <= rect.bottom; ++y) {
                        input_mat_copy.put(y, x, input_mat_blur.get(y, x));
                    }
                }

            }
            Utils.matToBitmap(input_mat_copy, outputBitmap);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ori_imageView.setVisibility(View.GONE);
//                    mosaic_imageView.setImageBitmap(outputBitmap);
                    mosaic_imageView.setVisibility(View.VISIBLE);
                }
            });
            if (progressDialog != null) {
                progressDialog.dismiss();
            }
            Log.d("Mosaic","fucking");
        }

        Log.d("Mosaic", String.valueOf(outputBitmap));
        return outputBitmap;
    }


    public void menuSwitcher(String tag){
        if (tag == "pixelable"){
//            sr_imageView.setClickable(false);
            pixelableSeekCard.setVisibility(View.VISIBLE);
            pixelableBtn.setBackground(VideoMosaicActivity_backup.this.getResources().getDrawable(R.drawable.mosaic_blue));
            pflag = true;

            blurSeekCard.setVisibility(View.GONE);
            bflag = false;
            blurBtn.setBackground(VideoMosaicActivity_backup.this.getResources().getDrawable(R.drawable.blur));
            blurSeek.setProgress(blurParam_tmp);

        }else if(tag == "blur"){
            pixelableSeekCard.setVisibility(View.GONE);
            pixelableBtn.setBackground(VideoMosaicActivity_backup.this.getResources().getDrawable(R.drawable.mosaic));
            pflag = false;
            pixelableSeek.setProgress(pixelableParam_tmp);
            blurSeekCard.setVisibility(View.VISIBLE);
            bflag = true;
            blurBtn.setBackground(VideoMosaicActivity_backup.this.getResources().getDrawable(R.drawable.blur_blue));

        }

    }

    private Bitmap faceDetect(Bitmap _input_bitmap){

        InputImage impphoto=InputImage.fromBitmap(_input_bitmap,0);

        outputBitmap = Bitmap.createBitmap(_input_bitmap.getWidth(), _input_bitmap.getHeight(), _input_bitmap.getConfig());
//        formatter = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss", Locale.US);
//        Date now = new Date();
//        String fileName = formatter.format(now) + ".png";
//        try {
//            MediaStore.Images.Media.insertImage(getContentResolver(), _input_bitmap, fileName, "output");
//            Log.d("hi3","aaa");
////                            Toast.makeText(getApplicationContext(), "Save completed", Toast.LENGTH_SHORT).show();
//        }catch (Exception e){
////                            Toast.makeText(getApplicationContext(), "Save failed", Toast.LENGTH_SHORT).show();
//            Log.d("hi2","aaa");
//        }
        if (impphoto!=null) {
            detector.process(impphoto).addOnSuccessListener(new OnSuccessListener<List<Face>>() {
                @Override
                public void onSuccess(List<Face> faces) {
                    Log.d("xxx", String.valueOf(impphoto.getHeight()));
                    if (faces.size() != 0) {
                        face_list = new ArrayList<Integer>();
                        faceList=faces;
                        Mat input_mat = new Mat();
                        Utils.bitmapToMat(_input_bitmap, input_mat);
                        Mat input_mat_copy = input_mat.clone();
                        DisplayMetrics dm = getResources().getDisplayMetrics();
                        int size = Math.round(50*dm.density);
                        int margin_size = Math.round(5*dm.density);
                        for (int i = 0; i<faces.size(); ++i){

                            Face face = faces.get(i);

                            RectF boundingBox = new RectF(face.getBoundingBox());
                            Rect rect = new Rect();

                            rect = rectFToRect(boundingBox, rect);
                            Imgproc.rectangle(input_mat_copy, new Point(rect.left,rect.bottom), new Point(rect.right,rect.top), new Scalar(238,87,96,255),5);
                            Bitmap cropped_face = getCropBitmapByCPU(_input_bitmap, boundingBox);
                            Bitmap scaled = getResizedBitmap(cropped_face, 112, 112);
                            Button face_btn = new Button(getApplicationContext());
                            CardView face_card = new CardView(getApplicationContext());
                            face_card.setCardBackgroundColor(getResources().getColor(R.color.liliy));
//                            face_card.setVisibility(View.INVISIBLE);
                            face_btn.setGravity(Gravity.CENTER);
//                            face_btn = findViewById(R.id.face_btn);
                            face_btn.setId(i);
//                            face_btn.setWidth(size);
//                            face_btn.setHeight(size);

                            face_btn.setOnClickListener(new View.OnClickListener() {
                                boolean flag = true;
                                int finalFace_idx = detect_idx;
                                @Override
                                public void onClick(View v) {
                                    if(flag){
                                        face_list.add(finalFace_idx);
                                        face_card.setCardBackgroundColor(getResources().getColor(R.color.black));
                                        removeBox();
                                        flag=false;
                                    }else{
                                        Log.d("aaa", String.valueOf(finalFace_idx));
                                        face_list.remove((Integer) finalFace_idx);
                                        face_card.setCardBackgroundColor(getResources().getColor(R.color.liliy));
                                        removeBox();
                                        flag=true;
                                    }
                                }
                            });
                            Drawable drawable = new BitmapDrawable(scaled);
                            face_btn.setBackground(drawable);
//                            scaled.recycle();

                            face_list_layout = (LinearLayout) findViewById(R.id.face_list_layout);
                            FrameLayout.LayoutParams face_btn_params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                            face_btn_params.setMargins(margin_size,margin_size,margin_size,margin_size);  // 왼쪽, 위, 오른쪽, 아래 순서입니다.
                            face_btn_params.width = size;
                            face_btn_params.height = size;
                            face_btn_params.gravity = Gravity.CENTER;
                            FrameLayout.LayoutParams face_card_params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                            face_card_params.width = size+5;
                            face_card_params.height = size+5;
//                            lp.rightMargin = rmargin_size;

                            face_card.addView(face_btn,face_btn_params);
                            face_list_layout.addView(face_card,face_card_params);

                            System.out.println(boundingBox);
                            detect_idx += 1;

                        }
                        Utils.matToBitmap(input_mat_copy, outputBitmap);
                        Log.d("hi5", "aasdasd");


//                        runOnUiThread(new Runnable() {
//                            @Override
//                            public void run() {
//                                ori_imageView.setVisibility(View.GONE);
////                                mosaic_imageView.setImageBitmap(outputBitmap);
//                                mosaic_imageView.setVisibility(View.VISIBLE);
//                            }
//                        });
                        if (progressDialog != null) {
                            progressDialog.dismiss();
                        }
                    }else{
                        Log.d("hi4", "no face");
                        outputBitmap = _input_bitmap;
//                        Toast toast = Toast.makeText(VideoMosaicActivity.this.getApplicationContext(),"No Face Detected.\nSelect Another Image ", Toast.LENGTH_LONG);
//                        toast.show();
//                        if (progressDialog != null) {
//                            progressDialog.dismiss();
//                        }
//                        Intent move = new Intent(VideoMosaicActivity.this,DataSelectActivity.class);
//                        move.putExtra("flag","mosaic");
//                        startActivity(move);
//                        finish();
                    }
//                    Mat m = new Mat();
//                    Utils.bitmapToMat(outputBitmap,m);
//                    videoWriter.write(m);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.d("Mosaic", "fail");
                }
            });
        }
        return outputBitmap;
    }
    private void removeBox(){
        Mat input_mat = new Mat();
        Utils.bitmapToMat(input_bitmap, input_mat);
        Mat input_mat_copy = input_mat.clone();
        for (int i = 0; i<faceList.size(); ++i){
            if (face_list.contains(i)) {
                continue;
            }
            Log.d("Mosaaaaaaic", String.valueOf(i));
            Face face = faceList.get(i);
            System.out.println(face);

            RectF boundingBox = new RectF(face.getBoundingBox());
            Rect rect = new Rect();

            Log.d("Mosaic", String.valueOf(input_mat));
            rect = rectFToRect(boundingBox, rect);
            Imgproc.rectangle(input_mat_copy, new Point(rect.left,rect.bottom), new Point(rect.right,rect.top), new Scalar(238,87,96,255),3);

        }
        Utils.matToBitmap(input_mat_copy, outputBitmap);
        Log.d("Mosaic", "fuck");
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ori_imageView.setVisibility(View.GONE);
//                mosaic_imageView.setImageBitmap(outputBitmap);
                mosaic_imageView.setVisibility(View.VISIBLE);
            }
        });
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
        Log.d("Mosaic","fucking");
    }


}