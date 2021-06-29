package org.github.ruifengho.datas.core;


import lombok.extern.slf4j.Slf4j;
import org.github.ruifengho.datas.common.ConfigConstants;
import org.github.ruifengho.datas.common.Configuration;
import org.github.ruifengho.datas.core.exchanger.RecordExchanger;

@Slf4j
public class TaskContainer {

    private Channel channel;

    private Reader reader;

    private Writer writer;

    private Thread readThread;

    private Thread writerThread;


    public TaskContainer(Configuration configuration) {
        String readerClass = configuration.getString(ConfigConstants.READER_CLASS);
        String writerClass = configuration.getString(ConfigConstants.WRITER_CLASS);
        RecordExchanger recordExchanger = new RecordExchanger(channel);

        try {
            ReaderRunner readerRunner = (ReaderRunner) Class.forName(readerClass).newInstance();
            readerRunner.setRecordSender(recordExchanger);
            this.reader = readerRunner;
            this.readThread = new Thread(readerRunner, "Reader-thread");

            WriterRunner writerRunner = (WriterRunner) Class.forName(writerClass).newInstance();
            writerRunner.setRecordReceiver(recordExchanger);
            this.writer = writerRunner;
            this.writerThread = new Thread(writerRunner, "Writer-thread");

        } catch (Exception e) {
            log.error("", e);
            throw new RuntimeException("初始化失败");
        }
    }

    public void doStart() {
        writerThread.start();

        if (!writerThread.isAlive()) {
            throw new RuntimeException("无法启动写任务");
        }

        readThread.start();

        if (readThread.isAlive()) {
            throw new RuntimeException("无法启动读任务");
        }


    }

}
