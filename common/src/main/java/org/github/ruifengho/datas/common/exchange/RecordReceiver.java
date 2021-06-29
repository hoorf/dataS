package org.github.ruifengho.datas.common.exchange;

public interface RecordReceiver {

    Record getFromReader();

    void shutdown();
}
