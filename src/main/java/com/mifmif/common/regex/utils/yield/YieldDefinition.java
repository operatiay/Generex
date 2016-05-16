package com.mifmif.common.regex.utils.yield;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.atomic.AtomicReference;

public class YieldDefinition<T> implements Iterable<T>, CloseableIterator<T> {
    private final SynchronousQueue<Message<T>> dataChannel = new SynchronousQueue<>();
    private final SynchronousQueue<FlowControl> flowChannel = new SynchronousQueue<>();
    private final AtomicReference<Optional<T>> currentValue = new AtomicReference<>(Optional.empty());
    private List<Runnable> toRunOnClose = new CopyOnWriteArrayList<>();

    public void returning(T value) {
        publish(value);
        waitUntilNextValueRequested();
    }

    public void breaking() {
        throw new BreakException();
    }

    @Override
    public CloseableIterator<T> iterator() {
        return this;
    }

    @Override
    public boolean hasNext() {
        calculateNextValue();
        Message<T> message = Exceptions.unchecked(dataChannel::take);
        if (message instanceof Completed) {
            return false;
        }
        currentValue.set(message.value());
        return true;
    }

    @Override
    public T next() {
        try {
            IfAbsent.ifAbsent(currentValue.get()).then(this::hasNext);
            return currentValue.get().get();
        } finally {
            currentValue.set(Optional.empty());
        }
    }

    public void signalComplete() {
        Exceptions.unchecked( () -> this.dataChannel.put(Completed.completed()));
    }

    public void waitUntilFirstValueRequested() {
        waitUntilNextValueRequested();
    }

    private void waitUntilNextValueRequested() {
        Exceptions.unchecked(() -> flowChannel.take());
    }

    private void publish(T value) {
        Exceptions.unchecked(() -> dataChannel.put(Message.message(value)));
    }

    private void calculateNextValue() {
        Exceptions.unchecked(() -> flowChannel.put(FlowControl.youMayProceed));
    }

    @Override
    public void close() {
        toRunOnClose.forEach(Runnable::run);
    }

    public void onClose(Runnable onClose) {
        this.toRunOnClose.add(onClose);
    }

    @Override
    protected void finalize() throws Throwable {
        close();
        super.finalize();
    }
}
