package com.abed.app.ubertest.network.dispatcher;

import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.concurrent.Future;

/**
 * <p>Scheduler used for trying a network operation.
 */
public abstract class Scheduler<T, R> {


    @NonNull
    final String name;

    @NonNull
    final SchedulerTask<T, R> task;


    Scheduler(@NonNull final String name,
              @NonNull final SchedulerTask<T, R> task) {
        this.name = name;
        this.task = task;
    }


    /**
     * <p>Request that a new task is dispatched.</p>
     */
    public abstract Future requestDispatch(@Nullable final T data, @NonNull final SchedulerCallback<R> callback);

    public abstract void shutdown();

    void postOnUiThread(@NonNull Runnable runnable) {
        new Handler(Looper.getMainLooper()).post(runnable);
    }


    /**
     * Interface that dispatch calls have to implement.
     */
    public interface SchedulerTask<T, R> {
        /**
         * Performs the dispatch call.
         */
        R dispatch(T data) throws Exception;
    }

    public interface SchedulerCallback<T> {

        int STATUS_IGNORED = 1;
        int STATUS_FAILED = 2;
        int STATUS_TOO_MANY_RETRIES = 3;

        void onSuccess(@Nullable T result);

        void onFail(int completionStatus, @Nullable Throwable throwable);
    }

    @NonNull
    static final SchedulerCallback NOOP = new SchedulerCallback() {
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
