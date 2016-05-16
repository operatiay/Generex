package com.mifmif.common.regex.utils.yield;

public interface Yielderable<T> extends Iterable<T> {

    void execute(YieldDefinition<T> builder);

    default CloseableIterator<T> iterator() {
        YieldDefinition<T> yieldDefinition = new YieldDefinition<>();
        Thread collectorThread = new Thread(() -> {
            yieldDefinition.waitUntilFirstValueRequested();
            try {
                execute(yieldDefinition);
            } catch (BreakException e) {
                // do nothing
            }
            yieldDefinition.signalComplete();
        });
        yieldDefinition.onClose(collectorThread::interrupt);
        collectorThread.setDaemon(true);
        collectorThread.start();
        return yieldDefinition.iterator();
    }
}
