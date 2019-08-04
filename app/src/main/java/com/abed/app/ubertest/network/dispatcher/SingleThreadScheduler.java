package com.abed.app.ubertest.network.dispatcher;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.abed.app.ubertest.network.RetryStrategy;
import com.abed.app.ubertest.utils.Logger;

import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * <p>Scheduler used for trying a network operation. This scheduler ensures that the given tasks
 * is executed at most once at a time (no concurrent executions are allowed).</p>
 *
 * <p>It also handles retries with an exponential backoff. The retries are soft retries and will be
 * interrupted if a new dispatch request is triggered via requestDispatch().</p>
 */
public class SingleThreadScheduler<T, R> extends Scheduler<T, R> {

    private static final String TAG = "Uber:Test:SchedulerThread";

    /**
     * Indicates if the internal scheduler thread was created.
     */
    @NonNull
    private final AtomicBoolean taskRunning = new AtomicBoolean(false);

    @NonNull
    private final RetryStrategy retryStrategy;

    @NonNull
    private final ExecutorService executor;

    public SingleThreadScheduler(@NonNull final String name,
                                 @NonNull final SchedulerTask<T, R> task,
                                 @NonNull final RetryStrategy retryStrategy) {
        super(name, task);
        this.retryStrategy = retryStrategy;
        this.executor = Executors.newSingleThreadExecutor();
    }

    /**
     * <p>Request that a new task is dispatched.</p>
     */
    public Future requestDispatch(@Nullable final T data, @NonNull SchedulerCallback<R> callback) {
        if (taskRunning.compareAndSet(false, true)) {
            return executor.submit(() -> mainLoop(data, callback));
        } else {
            postOnUiThread(() -> callback.onFail(SchedulerCallback.STATUS_IGNORED, null));
        }
        return null;
    }

    public void shutdown() {
        if (taskRunning.compareAndSet(true, false)) {
            executor.shutdown();
        }
    }

    /**
     * <p>Main loop for the scheduler. It monitors if there are pending calls and executes them one
     * at a time. This call can't be executed simultaneously.</p>
     */
    private void mainLoop(@Nullable T data, @NonNull SchedulerCallback<R> callback) {
        long delayTimeMillis = 0;
        int failedRetries = 0;

        Logger.logEvent(TAG, String.format("Started %s scheduler", this.name));

        try {
            R result = null;
            boolean succeeded;

            //noinspection InfiniteLoopStatement
            while (true) {
                // Make the actual dispatch call
                Logger.logEvent(TAG, String.format("%s scheduler executing call", this.name));

                try {
                    result = task.dispatch(data);
                    succeeded = true;
                } catch (Exception e) {
                    Logger.logError(TAG, String.format("%s scheduler executing call failure", this.name), e);
                    succeeded = false;
                }

                if (succeeded) {
                    break;
                } else {
                    // Something failed, wait longer before retrying.
                    delayTimeMillis = retryStrategy.delayUntilNextRetry(failedRetries, delayTimeMillis);

                    if (delayTimeMillis < 0) {
                        Logger.logWarning(TAG, String.format("%s scheduler task failed, and will not be retried.", this.name));
                        succeeded = false;
                        break;
                    }

                    Thread.sleep(delayTimeMillis);
                    ++failedRetries;

                    Logger.logWarning(TAG,
                            String.format(Locale.US, "%s scheduler failed %d times; delaying next request of %d ms",
                                    this.name,
                                    failedRetries,
                                    TimeUnit.MILLISECONDS.toSeconds(delayTimeMillis)));
                }
            }

            final R finalResult = result;
            if (succeeded) {
                postOnUiThread(() -> callback.onSuccess(finalResult));
            } else {
                postOnUiThread(() -> callback.onFail(SchedulerCallback.STATUS_TOO_MANY_RETRIES, null));
            }
        } catch (RuntimeException | InterruptedException e) {
            Logger.logError(TAG, String.format("%s scheduler failed!", this.name), e);
            postOnUiThread(() -> callback.onFail(SchedulerCallback.STATUS_FAILED, e));
        } finally {
            taskRunning.set(false);
        }
    }
}
