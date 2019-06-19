package com.annaclancy.demo0419;

/**
 * Created by AnnaClancy
 * on 2019/4/29.
 * 参考有：
 * https://blog.csdn.net/qq_39622065/article/details/83063438
 */

import android.util.Log;
import com.annaclancy.demo0419.WavFile.InitialDream;
import java.util.ArrayList;
import java.util.List;

public class Chain {

    private static final String TAG = "ClingClang";

    static {
        System.loadLibrary("tensorflow_inference");
    }

    private static int BATCH_SIZE = 1;
    private static int AUDIO_LENGTH = 1600;
    private static int AUDIO_FEATURE_LENGTH = 200;
    // 标签长：1421个拼音 + 1个空白块
    private static int DICT_LENGTH = 1422;

    private String INPUT_NAME = "the_input";
    private String OUTPUT_NAME = "output_1";
    /** 输入数据 **/
    private static int[] INPUT_SIZE = {BATCH_SIZE, AUDIO_LENGTH, AUDIO_FEATURE_LENGTH, 1};
    private float[][] data_input;
    /** 输出数据 **/
    private static int[] OUTPUT_SIZE = {1, AUDIO_FEATURE_LENGTH, DICT_LENGTH};
    private float[] predictions = new float[1 * AUDIO_FEATURE_LENGTH * DICT_LENGTH];

    private List<Integer> sym_index;
    private List<String> replay;
    public List<String> getReplay(){
        return replay;
    }

    public static String reMain;
    private InitialDream initialDream;

    // 默认测试路径
    Chain(){
        this.Predict();
    }

    // 接受文件路径
    Chain(String filepath){
        this.Predict(filepath);
    }

    public void Predict(){
        initialDream = new InitialDream();
        initialDream.head();
        data_input = initialDream.getDataIput();
        MainActivity.tf.feed(INPUT_NAME, createX_in(data_input), 1, AUDIO_LENGTH , AUDIO_FEATURE_LENGTH, 1);
        MainActivity.tf.run(new String[]{OUTPUT_NAME});
        MainActivity.tf.fetch(OUTPUT_NAME, predictions);
        sym_index = Argmax(predictions);
        replay = recoString();
        Log.e(TAG, String.valueOf(replay));
    }

    public void Predict(String filepath){
        initialDream = new InitialDream();
        initialDream.head(filepath);
        data_input = initialDream.getDataIput();
        if(data_input == null)
            return;
        MainActivity.tf.feed(INPUT_NAME, createX_in(data_input), 1, AUDIO_LENGTH , AUDIO_FEATURE_LENGTH, 1);
        MainActivity.tf.run(new String[]{OUTPUT_NAME});
        MainActivity.tf.fetch(OUTPUT_NAME, predictions);
        sym_index = Argmax(predictions);
        replay = recoString();
        // Log.e(TAG, String.valueOf(replay));
        reMain = String.valueOf(replay);
    }

    private float[] createX_in(float[][] data){
        float[] res = new float[1 * AUDIO_LENGTH * AUDIO_FEATURE_LENGTH * 1];
        int count = 0, col;
        // 不支持超出 1600 长度
        if(data.length < AUDIO_LENGTH)
            col = data.length;
        else col = AUDIO_LENGTH;
        for(int i=0;i<col;i++)
            for(int j=0;j<AUDIO_FEATURE_LENGTH;j++) {
                // Log.e(TAG, String.valueOf(i + " " + j));
                res[count] = data[i][j];
                count++;
            }
        return res;
    }

    private List<Integer> Argmax(float[] person){
        double[][] res = new double[200][1422];
        int n = 0;
        for(int i=0;i<200;i++)
            for(int j=0;j<1422;j++) {
                res[i][j] = person[n];
                n++;
            }
        int[] max = new int[200];
        List<Integer> list = new ArrayList<>();
        for(int i=0;i<200;i++) {
            max[i] = AnnaUtil.dim1Argmax(res[i]);
            if(max[i]!=1421)
                list.add(max[i]);
        }
        return list;
    }

    private List<String> recoString(){
        List<String> res = new ArrayList<>();
        // Log.e(TAG, String.valueOf(MainActivity.sym_list.size() + " ↔ " + sym_index.size()));
        for(int i=0;i<sym_index.size();i++)
            res.add(MainActivity.sym_list.get(sym_index.get(i)));
        return res;
    }

    float[] iDuntKnowWhatHappen_soIJustDoSomething(){
        // int input_len = data_input.length / 8;
        float[][][] DATA = AnnaUtil.reshape(data_input);
        return AnnaUtil.nomalizeData(DATA);
    }

}
