package com.abed.app.ubertest.network.dispatcher;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.abed.app.ubertest.utils.Logger;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * <p>Scheduler used for trying a network operation. This scheduler make it easy for concurrent executions.
 * It also allows to limit the number of concurrent executions using THREAD_COUNT, which later on can be
 * dynamic based on the specs of the device
 * </p>
 */
public class ThreadPoolScheduler<T, R> extends Scheduler<T, R> {

    private static final String TAG = "Uber:Test:ThreadPoolScheduler";
    private static final int THREAD_COUNT = 5;

    @NonNull
    private final ExecutorService executor;

    public ThreadPoolScheduler(@NonNull final String name,
                               @NonNull final SchedulerTask<T, R> task) {
        super(name, task);
        executor = Executors.newFixedThreadPool(THREAD_COUNT);
    }

    /**
     * <p>Request that a new task is dispatched.</p>
     */
    public Future requestDispatch(@Nullable T data, @NonNull SchedulerCallback<R> callback) {
        return executor.submit(() -> execute(data, callback));
    }

    public void shutdown() {
        executor.shutdown();
    }


    private void execute(@Nullable T data, @NonNull SchedulerCallback<R> callback) {
        Logger.logEvent(TAG, String.format("Started %s dispatcher", this.name));
        try {
            R result = null;
            boolean succeeded;

            try {
                result = task.dispatch(data);
                succeeded = true;
            } catch (Exception e) {
                Logger.logError(TAG, String.format("%s dispatcher executing call failure", this.name), e);
                succeeded = false;
            }

            final R finalResult = result;
            if (succeeded) {
                postOnUiThread(() -> callback.onSuccess(finalResult));
            } else {
                postOnUiThread(() -> callback.onFail(SchedulerCallback.STATUS_FAILED, null));
            }
        } catch (RuntimeException e) {
            Logger.logError(TAG, String.format("%s dispatcher failed!", this.name), e);
            postOnUiThread(() -> callback.onFail(SchedulerCallback.STATUS_FAILED, e));
        }
    }
}
