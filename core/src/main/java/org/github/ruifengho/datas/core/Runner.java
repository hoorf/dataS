package org.github.ruifengho.datas.core;

import java.util.concurrent.atomic.AtomicInteger;

public abstract class Runner {

    public Integer STATE_INIT = 0;
    public Integer STATE_SUCCESS = 1;
    public Integer STATE_FAIL = -1;
    public Integer STATE_RUNNING = 2;

    protected AtomicInteger state = new AtomicInteger(0);

    public void markRun() {
        mark(STATE_INIT, STATE_RUNNING);
    }

    public void markSuccess() {
        mark(STATE_RUNNING, STATE_SUCCESS);
    }

    public void markFail() {
        mark(STATE_RUNNING, STATE_FAIL);
    }

    private Boolean mark(Integer current, Integer target) {
        return state.compareAndSet(current, target);
    }


}
