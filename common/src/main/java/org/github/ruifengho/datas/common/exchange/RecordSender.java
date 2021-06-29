package org.github.ruifengho.datas.common.exchange;

public interface RecordSender {

    Record createRecord();

    void sendToWriter(Record record);

    void flush();

    void terminate();

    void shutdown();
}
