package com.coillighting.udder.infrastructure;

import com.coillighting.udder.mix.Frame;

import java.util.concurrent.BlockingQueue;

public interface Transmitter extends Runnable {

    public void setFrameQueue(BlockingQueue<Frame> frameQueue);

}