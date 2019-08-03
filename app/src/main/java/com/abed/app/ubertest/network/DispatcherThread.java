package com.abed.app.ubertest.network;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.abed.app.ubertest.utils.Logger;

import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * <p>Dispatcher used for trying a network operation. This dispatcher ensures that the given tasks
 * is executed at most once at a time (no concurrent executions are allowed).</p>
 *
 * <p>It also handles retries with an exponential backoff. The retries are soft retries and will be
 * interrupted if a new dispatch request is triggered via requestDispatch().</p>
 */
public class DispatcherThread<T, R> {

    private static final String TAG = "Uber:Test:DispatcherThread";

    /**
     * Indicates if the internal dispatcher thread was created.
     */
    @NonNull
    private final AtomicBoolean taskRunning = new AtomicBoolean(false);

    @NonNull
    private final String name;

    @NonNull
    private final DispatcherTask<T, R> task;

    @NonNull
    private final RetryStrategy retryStrategy;

    @NonNull
    private final DispatcherCallback<R> callback;

    @NonNull
    private final ExecutorService executor = Executors.newSingleThreadExecutor();


    public DispatcherThread(@NonNull final String name,
                                   @NonNull final DispatcherTask<T, R> task,
                                   @NonNull final DispatcherCallback<R> callback,
                                   @NonNull final RetryStrategy retryStrategy) {
        this.name = name;
        this.task = task;
        this.callback = callback;
        this.retryStrategy = retryStrategy;
    }

    /**
     * <p>Request that a new task is dispatched.</p>
     */
    public void requestDispatch(@Nullable T data) {
        if (taskRunning.compareAndSet(false, true)) {
            executor.execute(() -> mainLoop(data));
        } else {
            callback.onFail(DispatcherCallback.STATUS_IGNORED, null);
        }
    }

    public void shutdown() {
        if (taskRunning.compareAndSet(true, false)) {
            executor.shutdown();
        }
    }

    /**
     * <p>Main loop for the dispatcher. It monitors if there are pending calls and executes them one
     * at a time. This call can't be executed simultaneously.</p>
     */
    private void mainLoop(@Nullable T data) {
        long delayTimeMillis = 0;
        int failedRetries = 0;

        Logger.logEvent(TAG, String.format("Started %s dispatcher", this.name));

        try {
            R result = null;
            boolean succeeded;

            //noinspection InfiniteLoopStatement
            while (true) {
                // Make the actual dispatch call
                Logger.logEvent(TAG, String.format("%s dispatcher executing call", this.name));

                try {
                    result = task.dispatch(data);
                    succeeded = true;
                } catch (Exception e) {
                    Logger.logError(TAG, String.format("%s dispatcher executing call failure", this.name), e);
                    succeeded = false;
                }

                if (succeeded) {
                    break;
                } else {
                    // Something failed, wait longer before retrying.
                    delayTimeMillis = retryStrategy.delayUntilNextRetry(failedRetries, delayTimeMillis);

                    if (delayTimeMillis < 0) {
                        Logger.logWarning(TAG, String.format("%s dispatcher task failed, and will not be retried.", this.name));
                        succeeded = false;
                        break;
                    }

                    Thread.sleep(delayTimeMillis);
                    ++failedRetries;

                    Logger.logWarning(TAG,
                            String.format(Locale.US, "%s dispatcher failed %d times; delaying next request of %d ms",
                                    this.name,
                                    failedRetries,
                                    TimeUnit.MILLISECONDS.toSeconds(delayTimeMillis)));
                }
            }

            if (succeeded) {
                callback.onSuccess(result);
            } else {
                callback.onFail(DispatcherCallback.STATUS_TOO_MANY_RETRIES, null);
            }
        } catch (RuntimeException | InterruptedException e) {
            Logger.logError(TAG, String.format("%s dispatcher failed!", this.name), e);
            callback.onFail(DispatcherCallback.STATUS_FAILED, e);
        } finally {
            taskRunning.set(false);
        }
    }

    /**
     * Interface that dispatch calls have to implement.
     */
    public interface DispatcherTask<T, R> {
        /**
         * Performs the dispatch call.
         */
        R dispatch(T data) throws Exception;
    }

    public interface DispatcherCallback<T> {

        int STATUS_IGNORED = 1;
        int STATUS_FAILED = 2;
        int STATUS_TOO_MANY_RETRIES = 3;

        void onSuccess(@Nullable T result);

        void onFail(int completionStatus, @Nullable Throwable throwable);
    }

    @NonNull
    static final DispatcherCallback NOOP = new DispatcherCallback() {
        @Override
        public void onSuccess(@Nullable Object result) {
            //Do nothing
        }

        @Override
        public void onFail(int completionStatus, @Nullable Throwable throwable) {
            //Do nothing
        }
    };

}
