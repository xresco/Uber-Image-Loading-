package com.abed.app.ubertest.network;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.abed.app.ubertest.utils.Logger;

import java.util.Locale;
import java.util.concurrent.Executor;
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
class DispatcherThread {

    private static final String TAG = "Uber:Test:DispatcherThread";

    @NonNull
    private final Executor executor = Executors.newSingleThreadExecutor();

    /**
     * The logger to log events
     */
    @NonNull
    private final Logger logger;

    /**
     * Indicates if the internal dispatcher thread was created.
     */
    @NonNull
    private final AtomicBoolean taskRunning = new AtomicBoolean(false);

    /**
     * The name of the task.
     */
    @NonNull
    private final String name;

    DispatcherThread(@NonNull String name,
                     @NonNull Logger logger) {
        this.name = name;
        this.logger = logger;
    }

    /**
     * <p>Request that a new task is dispatched.</p>
     */
    <T> void requestDispatch(@NonNull DispatcherTask<T> task,
                             @NonNull final DispatcherCallback<T> callback,
                             @NonNull final RetryStrategy retryStrategy) {
        if (taskRunning.compareAndSet(false, true)) {
            executor.execute(() -> this.mainLoop(
                    task,
                    new DispatcherCallback<T>() {
                        @Override
                        public void onSuccess(@Nullable T result) {
                            callback.onSuccess(result);
                            taskRunning.set(false);
                        }

                        @Override
                        public void onFail(int completionStatus, @Nullable Throwable throwable) {
                            callback.onFail(completionStatus, throwable);
                            taskRunning.set(false);
                        }
                    },
                    retryStrategy)
            );
        } else {
            callback.onFail(DispatcherCallback.STATUS_IGNORED, null);
        }
    }

    /**
     * <p>Main loop for the dispatcher. It monitors if there are pending calls and executes them one
     * at a time. This call can't be executed simultaneously.</p>
     */
    private <T> void mainLoop(@NonNull DispatcherTask<T> task,
                              @NonNull DispatcherCallback<T> dispatcherCallback,
                              @NonNull RetryStrategy retryStrategy) {
        long delayTimeMillis = 0;
        int failedRetries = 0;

        logger.logEvent(TAG, String.format("Started %s dispatcher", this.name));

        try {
            T result = null;
            boolean succeeded;

            //noinspection InfiniteLoopStatement
            while (true) {
                // Make the actual dispatch call
                logger.logEvent(TAG, String.format("%s dispatcher executing call", this.name));

                try {
                    result = task.dispatch();
                    succeeded = true;
                } catch (Exception e) {
                    logger.logError(TAG, String.format("%s dispatcher executing call failure", this.name), e);
                    succeeded = false;
                }

                if (succeeded) {
                    break;
                } else {
                    // Something failed, wait longer before retrying.
                    delayTimeMillis = retryStrategy.delayUntilNextRetry(failedRetries, delayTimeMillis);

                    if (delayTimeMillis < 0) {
                        logger.logWarning(TAG, String.format("%s dispatcher task failed, and will not be retried.", this.name));
                        succeeded = false;
                        break;
                    }

                    Thread.sleep(delayTimeMillis);
                    ++failedRetries;

                    logger.logWarning(TAG,
                            String.format(Locale.US, "%s dispatcher failed %d times; delaying next request of %d ms",
                                    this.name,
                                    failedRetries,
                                    TimeUnit.MILLISECONDS.toSeconds(delayTimeMillis)));
                }
            }

            if (succeeded) {
                dispatcherCallback.onSuccess(result);
            } else {
                dispatcherCallback.onFail(DispatcherCallback.STATUS_TOO_MANY_RETRIES, null);
            }
        } catch (RuntimeException | InterruptedException e) {
            logger.logError(TAG, String.format("%s dispatcher failed!", this.name), e);
            dispatcherCallback.onFail(DispatcherCallback.STATUS_FAILED, e);
        }
    }

    /**
     * Interface that dispatch calls have to implement.
     */
    interface DispatcherTask<T> {
        /**
         * Performs the dispatch call.
         */
        T dispatch();
    }

    interface DispatcherCallback<T> {

        int STATUS_IGNORED = 1;
        int STATUS_FAILED = 2;
        int STATUS_TOO_MANY_RETRIES = 3;

        void onSuccess(@Nullable T result);

        void onFail(int completionStatus, @Nullable Throwable throwable);
    }

    @NonNull
    static final DispatcherCallback NOOP = new DispatcherCallback() {
        @Override
        public void onSuccess(@NonNull Object result) {
            //Do nothing
        }

        @Override
        public void onFail(int completionStatus, @Nullable Throwable throwable) {
            //Do nothing
        }
    };

}
