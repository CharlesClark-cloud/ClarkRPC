package com.clarkrpc.compress;


import com.clarkrpc.extension.SPI;

/**
 * 压缩消息体
 */

@SPI
public interface Compress {

    byte[] compress(byte[] bytes);


    byte[] decompress(byte[] bytes);
}
