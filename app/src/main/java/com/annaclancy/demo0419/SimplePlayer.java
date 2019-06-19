package com.annaclancy.demo0419;

import android.os.Environment;
import android.util.Log;

import com.annaclancy.demo0419.api.audio.AudioPlayer;
import com.annaclancy.demo0419.api.wav.WavFileReader;

import java.io.IOException;

/**
 * Created by Administrator
 * on 2019/6/1.
 */

public class SimplePlayer {
    private static final String TAG = SimplePlayer.class.getSimpleName();
    // 默认读取路径
    private static final String DEFAULT_TEST_FILE = Environment.getExternalStorageDirectory() + "/test.wav";

    private static final int SAMPLES_PER_FRAME = 1024;

    private AudioPlayer mAudioPlayer;
    private WavFileReader mWavFileReader;
    private volatile boolean mIsTestingExit = false;

    public boolean start(String filepath) {
        // Log.e(TAG, "AudioPlayer turn on!");
        mAudioPlayer = new AudioPlayer();

        Log.e(TAG, "WavFileReader turn on!");
        mWavFileReader = new WavFileReader();
        try {
            mWavFileReader.openFile(filepath);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        mAudioPlayer.startPlayer(16000);
        new Thread(AudioPlayRunnable).start();
        return true;
    }

    public boolean stop() {
        // Log.e(TAG, "AudioPlayer turn off!");
        mIsTestingExit = true;
        return true;
    }

    private Runnable AudioPlayRunnable = new Runnable() {
        @Override
        public void run() {
            byte[] buffer = new byte[SAMPLES_PER_FRAME * 2];
            while ( !mIsTestingExit && mWavFileReader.readData(buffer, 0, buffer.length) > 0 ) {
                mAudioPlayer.play(buffer, 0, buffer.length);
            }
            mAudioPlayer.stopPlayer();
            try {
                mWavFileReader.closeFile();
                Log.e(TAG,"WavFileReader turn off.");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    };
}
