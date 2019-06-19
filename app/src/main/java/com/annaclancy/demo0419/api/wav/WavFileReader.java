/*
 *  COPYRIGHT NOTICE  
 *  Copyright (C) 2016, Jhuster <lujun.hust@gmail.com>
 *  https://github.com/Jhuster/AudioDemo
 *   
 *  @license under the Apache License, Version 2.0 
 *
 *  @file    WavFileReader.java
 *  
 *  @version 1.0     
 *  @author  Jhuster
 *  @date    2016/03/19
 */
package com.annaclancy.demo0419.api.wav;

/**

 * Api 使用：
 * https://github.com/Jhuster/AudioDemo
 * Data 读取参考：
 * https://www.jb51.net/article/68440.htm
 *
 * 对 Framerate 计算：
 * " 44100的每帧采样点数是1024 "，但实际上，模型要求为 16000
 */

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import android.util.Log;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class WavFileReader {

    private static final String TAG = WavFileReader.class.getSimpleName();
    // 数据读入流
    private DataInputStream mDataInputStream;
    // 自定义的类
    private WavFileHeader mWavFileHeader;
    // 4.22：添加Buffer流
    private BufferedInputStream bis = null;

    // 创建文件流
    public boolean openFile(String filepath) throws IOException {
        if (mDataInputStream != null) {
            closeFile();
        }
        Log.e(TAG,"DataInputStream openfile = " + filepath);
        mDataInputStream = new DataInputStream(new FileInputStream(filepath));
        bis = new BufferedInputStream(new FileInputStream(filepath));
        return readHeader();
    }

    // 关闭文件流
    public void closeFile() throws IOException {
        if (mDataInputStream != null) {
            mDataInputStream.close();
            mDataInputStream = null;
        }
        if(bis != null){
            bis.close();
            bis = null;
        }
    }

    public WavFileHeader getmWavFileHeader() {
        return mWavFileHeader;
    }

    // 反正是读文件bytes，看起来是用于确认文件是否为空的
    public int readData(byte[] buffer, int offset, int count) {
        if (mDataInputStream == null || mWavFileHeader == null) {
            return -1;
        }
        try {
            int nbytes = mDataInputStream.read(buffer, offset, count);
            if (nbytes == -1) {
                return 0;
            }
            return nbytes;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return -1;
    }

    // 读Wav头，详细请看WavFileHeader类
    private boolean readHeader() {
        if (mDataInputStream == null) {
            return false;
        }
        // 此处新建WavFileHeader对象
        WavFileHeader header = new WavFileHeader();
        byte[] intValue = new byte[4];
        byte[] shortValue = new byte[2];
        try {
            header.mChunkID = "" + (char) mDataInputStream.readByte() + (char) mDataInputStream.readByte() + (char) mDataInputStream.readByte() + (char) mDataInputStream.readByte();
            Log.d(TAG, "Read file chunkID:" + header.mChunkID);

            mDataInputStream.read(intValue);
            header.mChunkSize = byteArrayToInt(intValue);
            Log.d(TAG, "Read file chunkSize:" + header.mChunkSize);

            header.mFormat = "" + (char) mDataInputStream.readByte() + (char) mDataInputStream.readByte() + (char) mDataInputStream.readByte() + (char) mDataInputStream.readByte();
            Log.d(TAG, "Read file format:" + header.mFormat);

            header.mSubChunk1ID = "" + (char) mDataInputStream.readByte() + (char) mDataInputStream.readByte() + (char) mDataInputStream.readByte() + (char) mDataInputStream.readByte();
            Log.d(TAG, "Read fmt chunkID:" + header.mSubChunk1ID);

            mDataInputStream.read(intValue);
            header.mSubChunk1Size = byteArrayToInt(intValue);
            Log.d(TAG, "Read fmt chunkSize:" + header.mSubChunk1Size);

            mDataInputStream.read(shortValue);
            header.mAudioFormat = byteArrayToShort(shortValue);
            Log.d(TAG, "Read audioFormat:" + header.mAudioFormat);

            mDataInputStream.read(shortValue);
            header.mNumChannel = byteArrayToShort(shortValue);
            Log.d(TAG, "Read channel number:" + header.mNumChannel);

            mDataInputStream.read(intValue);
            header.mSampleRate = byteArrayToInt(intValue);
            Log.d(TAG, "Read samplerate:" + header.mSampleRate);

            mDataInputStream.read(intValue);
            header.mByteRate = byteArrayToInt(intValue);
            Log.d(TAG, "Read byterate:" + header.mByteRate);

            mDataInputStream.read(shortValue);
            header.mBlockAlign = byteArrayToShort(shortValue);
            Log.d(TAG, "Read blockalign:" + header.mBlockAlign);

            mDataInputStream.read(shortValue);
            header.mBitsPerSample = byteArrayToShort(shortValue);
            Log.d(TAG, "Read bitspersample:" + header.mBitsPerSample);

            header.mSubChunk2ID = "" + (char) mDataInputStream.readByte() + (char) mDataInputStream.readByte() + (char) mDataInputStream.readByte() + (char) mDataInputStream.readByte();
            Log.d(TAG, "Read data chunkID:" + header.mSubChunk2ID);

            mDataInputStream.read(intValue);
            header.mSubChunk2Size = byteArrayToInt(intValue);
            Log.d(TAG, "Read data chunkSize:" + header.mSubChunk2Size);

            // 4.22添加的对data读取，参考网址见最上
            int len = (int)(header.mSubChunk2Size/(header.mBitsPerSample/8)/header.mNumChannel);
            Log.e(TAG,"Org Data Len = " + len);
            header.mData = new int[header.mNumChannel][len];
            for(int i=0;i<len;i++){
                for(int n=0;n<header.mNumChannel;n++){
                    if(header.mBitsPerSample == 8)
                        header.mData[n][i] = bis.read();
                    else if(header.mBitsPerSample == 16)
                        header.mData[n][i] = readInt();
                }
            }
            // Log.d(TAG, "Read wav file success !");
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        mWavFileHeader = header;
        return true;
    }

    // ByteBuffer转Short类型
    private static short byteArrayToShort(byte[] b) {
        return ByteBuffer.wrap(b).order(ByteOrder.LITTLE_ENDIAN).getShort();
    }

    // ByteBuffer转Int类型
    private static int byteArrayToInt(byte[] b) {
        return ByteBuffer.wrap(b).order(ByteOrder.LITTLE_ENDIAN).getInt();
    }

    // 4.22
    private int readInt(){
        byte[] buf = new byte[2];
        int res = 0;
        try {
            if(bis.read(buf)!=2)
                throw new IOException("no more data!!!");
            res = (buf[0]&0x000000FF) | (((int)buf[1])<<8);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return res;
    }
}
