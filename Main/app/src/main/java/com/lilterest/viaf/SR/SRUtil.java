package com.lilterest.viaf.SR;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.util.Log;

import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.photo.Photo;

public class SRUtil {

    public static Bitmap changeBitmapContrastBrightness(Bitmap bmp, float contrast, float brightness)
    {
        Log.d("SR", String.valueOf(brightness));

        brightness = brightness *10.f;
        ColorMatrix cm = new ColorMatrix(new float[]
                {
                        contrast, 0, 0, 0, brightness,
                        0, contrast, 0, 0, brightness,
                        0, 0, contrast, 0, brightness,
                        0, 0, 0, 1, 0
                });

        Bitmap ret = Bitmap.createBitmap(bmp.getWidth(), bmp.getHeight(), bmp.getConfig());

        Canvas canvas = new Canvas(ret);

        Paint paint = new Paint();
        paint.setColorFilter(new ColorMatrixColorFilter(cm));
        canvas.drawBitmap(bmp, 0, 0, paint);

        return ret;
    }

    public static Bitmap denoise(Bitmap src, float v){
        Bitmap ret = Bitmap.createBitmap(src.getWidth(), src.getHeight(), src.getConfig());
        Mat imageMat = new Mat();
        Utils.bitmapToMat(src, imageMat);
        Photo.fastNlMeansDenoisingColored(imageMat,imageMat, v, v, 7, 21);
        Utils.matToBitmap(imageMat,ret);
        return ret;
    }
    public Bitmap applySmoothEffect(Bitmap src, float value) {
        //create convolution matrix instance
        ConvolutionMatrix convMatrix = new ConvolutionMatrix(3);
        convMatrix.setAll(1);
        convMatrix.Matrix[1][1] = value;
        // set weight of factor and offset
        convMatrix.Factor = value + 8;
        convMatrix.Offset = 1;
        return ConvolutionMatrix.computeConvolution3x3(src, convMatrix);
    }


    public Bitmap resizeBitmapImage(Bitmap source, int scale)
    {
        OpenCVLoader.initDebug();
        Bitmap bmp = Bitmap.createBitmap(source.getWidth()*scale, source.getHeight()*scale, Bitmap.Config.ARGB_8888);
//        Bitmap bmp = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        Mat imageMat = new Mat();
        Mat image_res = new Mat();
        //changes bitmap to Mat
        Utils.bitmapToMat(source, imageMat);

        //Resize Image
        Imgproc.resize(imageMat,image_res,new Size(source.getWidth()*scale, source.getHeight()*scale),0,0,Imgproc.INTER_AREA);
//        Imgproc.resize(imageMat,image_res,new Size(size, size),0,0,Imgproc.INTER_AREA);
        // For Testing purpose ! To display proccessed image to view the outpts
        Utils.matToBitmap(image_res,bmp);
        return bmp;
    }

}

