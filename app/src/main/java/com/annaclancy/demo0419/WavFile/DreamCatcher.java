package com.annaclancy.demo0419.WavFile;

import android.media.AudioFormat;
import android.media.MediaRecorder;

import com.annaclancy.demo0419.AnnaUtil;
import com.annaclancy.demo0419.api.audio.AudioCapturer;
import com.annaclancy.demo0419.api.wav.WavFileWriter;

import java.io.IOException;

/**
 * Created by Administrator
 * on 2019/5/3.
 */

public class DreamCatcher implements AudioCapturer.OnAudioFrameCapturedListener{
    private AudioCapturer mAudioCapturer;
    private WavFileWriter mWavFileWriter;

    public boolean startWork() {
        mAudioCapturer = new AudioCapturer();
        mWavFileWriter = new WavFileWriter();
        try {
            mWavFileWriter.openFile(AnnaUtil.DEFAULT_FILE, AnnaUtil.MODEL_FRAMERATE, 1, 16);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        mAudioCapturer.setOnAudioFrameCapturedListener(this);
        return mAudioCapturer.startCapture(MediaRecorder.AudioSource.MIC, AnnaUtil.MODEL_FRAMERATE, AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT);
    }

    public boolean takeRelax() {
        mAudioCapturer.stopCapture();
        try {
            mWavFileWriter.closeFile();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    @Override
    public void onAudioFrameCaptured(byte[] audioData) {
        mWavFileWriter.writeData(audioData, 0, audioData.length);
    }
}
