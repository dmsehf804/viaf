package com.lilterest.viaf.SR;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.Tensor;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.HashMap;
import java.util.Map;

public class TfLiteIssue {

    private final static String TAG = "TfLiteIssue";
    private final Interpreter interpreter;

    public TfLiteIssue(Context context) throws IOException {
        this(context, "SR.tflite");
    }

    public TfLiteIssue(Context context, String modelPath) throws IOException {
        AssetFileDescriptor fileDescriptor = context.getAssets().openFd(modelPath);
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();
        FileChannel fileChannel = inputStream.getChannel();
        interpreter = new Interpreter(fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength),
                new Interpreter.Options());
    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    public Bitmap runModel(Bitmap input) {
        Log.d("SR","aaasasaassaassasaas11");
        interpreter.resizeInput(0, new int[]{1, input.getHeight(), input.getWidth(), 3});

        Map<Integer, Object> tfOutput = new HashMap<>();
        tfOutput.put(0, createOutputArray());
        Log.d("SR",String.valueOf(input));
        interpreter.runForMultipleInputsOutputs(new float[][][][][]{asFloatArray(input)}, tfOutput);
        Log.d("SR","aaasasaassaassasaas");
        return convertImageFloatToInt((float[][][][]) tfOutput.get(0), input.getWidth(),input.getHeight());
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private Bitmap convertImageFloatToInt(float[][][][] floatArray, int w, int h){
        float[][][] f;
        f = floatArray[0];
        int[] intArray = new int[floatArray.length / 3];
        for (int i = 0; i < w; ++i) {
            for (int j = 0; j < h; ++j) {
                float r = Math.min(1, Math.max(0, f[i][j][2]));
                float g = Math.min(1, Math.max(0, f[i][j][1]));
                float b = Math.min(1, Math.max(0, f[i][j][0]));
                int pixelValue = Color.rgb(r, g, b);
                intArray[i] = pixelValue;
            }
        }
        Bitmap bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        Log.d("SR", String.valueOf(intArray));
        bmp.setPixels(intArray,0,0,0,0,w,h);
        return bmp;
    }

    private float[][][][] createOutputArray() {
        Tensor tensor = interpreter.getOutputTensor(0);
        int[] shape = tensor.shape();
        return new float[1][shape[1]][shape[2]][shape[3]];
    }

    private float[][][][] asFloatArray(Bitmap bitmap) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int[] intPixels = new int[height * width];
        float[][][][] floatPixels = new float[1][height][width][3];
        bitmap.getPixels(intPixels, 0, width, 0, 0, width, height);
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                int pixel = intPixels[j + i * width];
                floatPixels[0][i][j][0] = (float) (pixel & 0x00FF0000 >> 16) / 255;
                floatPixels[0][i][j][1] = (float) (pixel & 0x0000FF00 >> 8) / 255;
                floatPixels[0][i][j][2] = (float) (pixel & 0x000000FF) / 255;
            }
        }
        return floatPixels;
    }
}
