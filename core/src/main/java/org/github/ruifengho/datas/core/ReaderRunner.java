package org.github.ruifengho.datas.core;


import org.github.ruifengho.datas.common.exchange.RecordSender;

public abstract class ReaderRunner extends Reader implements Runnable {

    protected RecordSender recordSender;


    public void setRecordSender(RecordSender recordSender) {
        this.recordSender = recordSender;
    }

    @Override
    public void run() {

        this.prepare();
        this.preCheck();
        this.init();

        doRunAction(recordSender);


        this.post();
        this.destroy();
    }

    protected abstract void doRunAction(RecordSender recordSender);
}
