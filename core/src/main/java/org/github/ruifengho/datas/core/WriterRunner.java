package org.github.ruifengho.datas.core;


import org.github.ruifengho.datas.common.exchange.RecordReceiver;

public abstract class WriterRunner extends Writer implements Runnable {

    protected RecordReceiver recordReceiver;

    public void setRecordReceiver(RecordReceiver recordReceiver) {
        this.recordReceiver = recordReceiver;
    }

    @Override
    public void run() {
        this.prepare();
        this.preCheck();
        this.init();

        doRunAction(recordReceiver);


        this.post();
        this.destroy();
    }

    protected abstract void doRunAction(RecordReceiver recordReceiver);
}
