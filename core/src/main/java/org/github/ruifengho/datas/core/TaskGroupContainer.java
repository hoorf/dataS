package org.github.ruifengho.datas.core;


import org.github.ruifengho.datas.common.Configuration;

import java.util.List;

public class TaskGroupContainer {

    public static final String JOB_READER = "job.reader";
    private List<TaskContainer> taskContainerList;


    public void start() {

    }

    private void split(Configuration configuration){
        configuration.set("job.reader.task",new String[]{"select * from table"});

    }

    private void shutdown(){

    }

}
