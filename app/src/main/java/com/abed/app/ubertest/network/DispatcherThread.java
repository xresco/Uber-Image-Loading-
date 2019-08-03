package com.abed.app.ubertest.network;

import android.support.annotation.NonNull;

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
     * The runnable to run.
     */
    @NonNull
    private final DispatcherTask task;


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
                     @NonNull DispatcherTask task,
                     @NonNull Logger logger) {
        this.name = name;
        this.task = task;
        this.logger = logger;
    }

    /**
     * <p>Request that a new task is dispatched.</p>
     */
    void requestDispatch(@NonNull final DispatcherCallback callback,
                         @NonNull final RetryStrategy retryStrategy) {
        if (taskRunning.compareAndSet(false, true)) {
            executor.execute(() -> this.mainLoop(
                    completionStatus -> {
                        try {
                            Thread.sleep(200); // sleeps a little not to call another time too soon
                        } catch (InterruptedException e) {
                            // do Nothing
                        }

                        callback.onDispatchFinished(completionStatus);
                        taskRunning.set(false);
                    },
                    retryStrategy)
            );
        } else {
            callback.onDispatchFinished(DispatcherCallback.STATUS_IGNORED);
        }
    }

    /**
     * <p>Main loop for the dispatcher. It monitors if there are pending calls and executes them one
     * at a time. This call can't be executed simultaneously.</p>
     */
    private void mainLoop(@NonNull DispatcherCallback dispatcherCallback, @NonNull RetryStrategy retryStrategy) {
        long delayTimeMillis = 0;
        int failedRetries = 0;

        logger.logEvent(TAG, String.format("Started %s dispatcher", this.name));

        try {

            boolean succeeded;

            //noinspection InfiniteLoopStatement
            while (true) {
                // Make the actual dispatch call
                logger.logEvent(TAG, String.format("%s dispatcher executing call", this.name));

                try {
                    succeeded = this.task.dispatch();
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

            dispatcherCallback.onDispatchFinished(succeeded ? DispatcherCallback.STATUS_SUCCESS : DispatcherCallback.STATUS_FAILED);
        } catch (RuntimeException | InterruptedException e) {
            logger.logError(TAG, String.format("%s dispatcher failed!", this.name), e);
            dispatcherCallback.onDispatchFinished(DispatcherCallback.STATUS_FAILED);
        }
    }

    /**
     * Interface that dispatch calls have to implement.
     */
    interface DispatcherTask {

        /**
         * Performs the dispatch call.
         *
         * @return true if the call cass successful.
         * @throws Exception if an error occurs.
         */
        boolean dispatch() throws Exception;
    }

    interface DispatcherCallback {

        int STATUS_SUCCESS = 1;
        int STATUS_IGNORED = 2;
        int STATUS_FAILED = 3;

        void onDispatchFinished(int completionStatus);
    }

    @NonNull
    static final DispatcherCallback NOOP = status -> {
        // Do Nothing
    };

}
