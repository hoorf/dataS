package org.github.ruifengho.datas.core.exchanger;

import lombok.extern.slf4j.Slf4j;
import org.github.ruifengho.datas.common.exchange.Record;
import org.github.ruifengho.datas.common.exchange.RecordReceiver;
import org.github.ruifengho.datas.common.exchange.RecordSender;
import org.github.ruifengho.datas.core.Channel;

@Slf4j
public class RecordExchanger implements RecordReceiver, RecordSender {

    private Channel channel;

    private static Class<? extends Record> RECORD_CLASS;

    private volatile boolean shutdown = false;

    public RecordExchanger(Channel channel) {
        this.channel = channel;
    }

    @Override
    public Record getFromReader() {

        check();

        Record record = channel.pull();

        return record;
    }

    @Override
    public Record createRecord() {
        Record record = null;
        try {
            record = RECORD_CLASS.newInstance();
        } catch (Exception e) {
            log.error("", e);
        }
        return record;
    }

    @Override
    public void sendToWriter(Record record) {

        check();

        channel.push(record);
    }

    @Override
    public void flush() {

    }

    @Override
    public void terminate() {

    }

    @Override
    public void shutdown() {
        shutdown = true;
    }


    private void check() {
        if (shutdown) {
            throw new RuntimeException("交换已关闭");
        }
    }
}
