/*
 *  COPYRIGHT NOTICE  
 *  Copyright (C) 2016, Jhuster <lujun.hust@gmail.com>
 *  https://github.com/Jhuster/AudioDemo
 *   
 *  @license under the Apache License, Version 2.0 
 *
 *  @file    WavFileHeader.java
 *  
 *  @version 1.0     
 *  @author  Jhuster
 *  @date    2016/03/19
 */

/**
 * < Data Format 原模型数据格式要求 >
 * wav格式 diff文件头
 * 采样频率16 kHz, 采样位数16 bits, 256 samples, 2 bytes 长度
 * 是不是raw格式都可以，只要能够正确读取内容即可
 */

package com.annaclancy.demo0419.api.wav;

public class WavFileHeader {

    public static final int WAV_FILE_HEADER_SIZE = 44;          // header size
    public static final int WAV_CHUNKSIZE_EXCLUDE_DATA = 36;    // 记录Wav文件字节数

    public static final int WAV_CHUNKSIZE_OFFSET = 4;
    public static final int WAV_SUB_CHUNKSIZE1_OFFSET = 16;
    public static final int WAV_SUB_CHUNKSIZE2_OFFSET = 40;

    /***
     *  The "RIFF" chunk descriptor
     *  顶层信息块
     */
    public String mChunkID = "RIFF";    // 通过ChunkID来表示这是"RIFF"格式文件
    public int mChunkSize = 0;
    public String mFormat = "WAVE";     // Format标示这是Wav文件

    /***
     *  The "fmt" sub-chunk
     *  fmt 信息块
     */
    public String mSubChunk1ID = "fmt ";
    public int mSubChunk1Size = 16;
    public short mAudioFormat = 1;
    public short mNumChannel = 1;       // 通道数（单声道为1，双声道为2）
    public int mSampleRate = 8000;      // 采样率（每秒样本数，表示每个通道的播放速度），即帧速率
    public int mByteRate = 0;           // 字节率（波形音频数据传送速率）
    public short mBlockAlign = 0;       // 块对齐（数据块的调整数）
    public short mBitsPerSample = 8;    // 采样位数（每样本的数据位数）

    /***
     *  The "data" chunk descriptor
     *  data 信息块
     */
    public String mSubChunk2ID = "data";
    public int mSubChunk2Size = 0;      // 用于记录二进制原始音频数据的长度
    public int[][] mData = null;

    // 创建默认Wav文件头
    public WavFileHeader() {
    }

    public WavFileHeader(int sampleRateInHz, int channels, int bitsPerSample) {
        mSampleRate = sampleRateInHz;
        mBitsPerSample = (short) bitsPerSample;
        mNumChannel = (short) channels;
        // 字节率 = 采样率 * 通道数 * 采样位数 / 8
        mByteRate = mSampleRate * mNumChannel * mBitsPerSample / 8;
        // 块对齐 = 通道数 * 采样位数 / 8
        mBlockAlign = (short) (mNumChannel * mBitsPerSample / 8);
    }
}