package org.github.ruifengho.datas.core;

public interface Task {

    void preCheck();

    void init();

    void prepare();

    void post();

    void destroy();
}
