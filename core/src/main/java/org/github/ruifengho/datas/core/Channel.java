package org.github.ruifengho.datas.core;

import org.github.ruifengho.datas.common.exchange.Record;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public abstract class Channel {

    private static BlockingQueue<Record> queue = new LinkedBlockingQueue<>();


    public abstract void push(Record record);

    public abstract Record pull();

}
