package com.annaclancy.demo0419.WavFile;

/**
 * Created by AnnaClancy
 * on 2019/4/21.
 * 读入 wav 数据并处理
 *
 * FFT 库提供：
 * https://wendykierp.github.io/JTransforms/apidocs/
 */

import android.util.Log;
import com.annaclancy.demo0419.AnnaUtil;
import com.annaclancy.demo0419.api.wav.WavFileReader;
import java.io.IOException;
import org.jtransforms.fft.FloatFFT_1D;

public class InitialDream {
    private static final String TAG = InitialDream.class.getSimpleName();

    private WavFileReader mWavFileReader;

    /**
     * 全部的帧
     * str_data = wav.readframes(num_frame)
     * 帧速率
     * framerate = wav.getframerate()
     */
    private float[][] data; // 全部的帧
    private int framerate;  // 帧速率
    // 最终生成
    private float[][] data_iput;

    public int getDataIputDimX(){
        return data_iput.length;
    }
    public float[][] getDataIput(){
        return data_iput;
    }

    // 默认测试，不接收文件路径
    public boolean head() {
        // Log.e(TAG, "WavFileReader turn on!");
        mWavFileReader = new WavFileReader();
        try {
            mWavFileReader.openFile(AnnaUtil.TEST_FILE);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        data = AnnaUtil.intArrayToFloat(mWavFileReader.getmWavFileHeader().mData);
        framerate = mWavFileReader.getmWavFileHeader().mSampleRate;
        try {
            // 在数据读取完毕后，保证Wave流关闭
            mWavFileReader.closeFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        runHamming();  // 数据加窗
        // Log.e(TAG,"data_input.shape = " + data_iput.length + " & " + data_iput[0].length);
        return true;
    }

    public boolean head(String filepath) {
        // Log.e(TAG, "WavFileReader turn on!");
        mWavFileReader = new WavFileReader();
        try {
            mWavFileReader.openFile(filepath);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        data = AnnaUtil.intArrayToFloat(mWavFileReader.getmWavFileHeader().mData);
        framerate = mWavFileReader.getmWavFileHeader().mSampleRate;
        try {
            // 数据读取完毕后，自动关闭Wave流
            mWavFileReader.closeFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        runHamming();
        // Log.e(TAG,"data_input.shape = " + data_iput.length + " & " + data_iput[0].length);
        return true;
    }

    /**
     * Hanmming窗
     * */
    // int window_len = framerate/1000*time_window;
    private float[] weight = null;
    int window_len = 400;
    private void intial_weight(){
        int[] x = new int[window_len];
        weight = new float[window_len];
        int n = 0;
        for(int i=0;i<window_len;i++)
            x[i] = n++;
        for(int j=0;j<window_len;j++)
            weight[j] = (float) (0.54 - 0.46 * Math.cos(2*Math.PI*x[j]/(window_len-1)));
    }

    private void runHamming(){
        intial_weight();
        int time_window = 25;
        float wav_length = data[0].length, fs = framerate;
        int range0_end = (int)(wav_length/fs*1000 - time_window)/10;
        data_iput = new float[range0_end][200];
        float[] data_line = new float[window_len];

        float[] data_complex;
        FloatFFT_1D fft = new FloatFFT_1D(window_len);

        int p_start;
        for(int i=0;i<range0_end;i++) {
            // 截取 400 定长数据
            p_start = i * 160;
            System.arraycopy(data[0], p_start, data_line, 0, window_len);

            // 对单行数据加窗
            for (int cnt = 0; cnt < window_len; cnt++)
                data_line[cnt] = data_line[cnt] * weight[cnt];

            // 内部初始化复数数组*
            data_complex = new float[window_len*2];
            for (int plex = 0; plex < window_len; plex++)
                data_complex[plex*2] = data_line[plex];
            // 执行fft
            fft.complexForward(data_complex);

            // 求其模，返还给data_line
            System.arraycopy(AnnaUtil.absComplex(data_complex),0,
                    data_line, 0, window_len);
            for (int cnt = 0; cnt < window_len; cnt++)
                data_line[cnt] = data_line[cnt] / wav_length;
            System.arraycopy(data_line, 0, data_iput[i], 0, window_len/2);
        }
        // 执行 data_input = np.log(data_input + 1)
        for(int i=0;i<range0_end;i++)
            AnnaUtil.logPlus1(data_iput[i]);
    }

}
