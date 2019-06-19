package com.annaclancy.demo0419;

import android.content.Context;
import android.os.Environment;
import android.util.Log;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator
 * on 2019/4/29.
 */

public class AnnaUtil {

    private static final String TAG = AnnaUtil.class.getSimpleName();

    public static int AUDIO_LENGTH = 1600;
    public static int AUDIO_FEATURE_LENGTH = 200;
    public static int MODEL_FRAMERATE = 16000;

    public static String[] fatewell_pinyin = new String[]
            {"ta1", "shi4", "ni3", "men5", "de5", "yang2", "guang1",
            "ni3", "men5", "de5", "lu4", "shui3",
            "shi4", "shang4", "de5", "feng5", "yu3", "yu3",
            "shi2", "jian1", "ben3", "shen1",
            "yi1", "qie4", "de5", "lai2", "long2", "qu4", "mai4",
            "yin1", "guo3", "luo2", "ji2"};

    // 默认读取路径，读SD卡根目录
    public static final String DEFAULT_FILE = Environment.getExternalStorageDirectory() + "/farewell.wav";
    // 默认测试文件路径
    public static final String TEST_FILE = Environment.getExternalStorageDirectory() + "/A11_11.wav";

    // thchs30 train 数据集文件
    public static final String TRAIN_SET_FILE = Environment.getExternalStorageDirectory() + "/A2_63.wav";
    // ST-CMDS test 数据集文件
    public static final String TEST_SET_FILE = Environment.getExternalStorageDirectory() + "/D4_787.wav";

    // 读复数数组与其长度，求其模
    public static float[] absComplex(float[] com){
        float[] res = new float[com.length/2];
        double z;
        for(int i=0;i<com.length;i=i+2){
            z = Math.sqrt(Math.pow(com[i], 2) + Math.pow(com[i+1], 2));
            res[i/2] = (float)z;
        }
        return res;
    }

    // 对传入数组log(n+1)，返回同等长度的数组
    public static float[] logPlus1(float[] data){
        float[] res = new float[data.length];
        for(int ct=0;ct<res.length;ct++)
            res[ct] = (float)Math.log(data[ct] + 1);
        return res;
    }

    public static float[][] intArrayToFloat(int[][] org){
        float[][] res = new float[org.length][org[0].length];
        for(int i=0;i<res.length;i++)
            for(int j=0;j<res[0].length;j++)
                res[i][j] = org[i][j];
        return res;
    }

    public static int dim1Argmax(double[] org){
        int best = 0;
        double best_conf = org[0];
        for(int i=1;i<org.length;i++)
            if(Double.compare(org[i], best_conf) == 1) {
                best = i;
                best_conf = org[i];
            }
        return best;
    }

    public static void getSymbolList(Context context) {
        InputStream inputStream = context.getClass().getClassLoader().getResourceAsStream("assets/dict.txt");
    }

    /** 仅在测试时使用↓ */

    // 实现np.reshape(np.shape[0], np.shape[1], 1)
    public static float[][][] reshape(float[][] org){
        float[][][] tagArr = new float[org.length][org[0].length][1];
        for(int i=0;i<tagArr.length;i++)
            for(int j=0;j<tagArr[i].length;j++)
                tagArr[i][j][0] = org[i][j];
        return tagArr;
    }

    public static float[] nomalizeData(float[][][] org){
        int arr_col = org[0].length;
        float[] res = new float[1 * AUDIO_LENGTH * AUDIO_FEATURE_LENGTH * 1];
        for(int i=0;i<org.length;i++)
            for(int j=0;j<arr_col;j++)
                res[i * arr_col + j] = org[i][j][0];
        return res;
    }

    public static List<Float> countNotNaN(float[] person){
        List<Float> list = new ArrayList<>();
        int ct = 0;
        Log.e(TAG, "Recog person Data");
        for(int i=0;i<person.length;i++)
            if(!Float.isNaN(person[i]))
                list.add(person[i]);
        return list;
    }

    public static int notNaN(float[] person){
        int num = 0;
        for(int i=0;i<person.length;i++)
                if(!Float.isNaN(person[i]))
                    num++;
        return num;
    }

    public static int notNaN(float[][] person){
        int num = 0;
        // Log.e(TAG, "Recog person Data");
        for(int i=0;i<person.length;i++)
            for(int j=0;j<person[i].length;j++)
                if(!Float.isNaN(person[i][j]))
                    num++;
        return num;
    }

}
