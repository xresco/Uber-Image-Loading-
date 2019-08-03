package com.abed.app.ubertest.network;

import android.support.annotation.NonNull;

import java.util.concurrent.TimeUnit;

public interface RetryStrategy {

    long delayUntilNextRetry(int numberOfRetries, long lastRetryDelay);

    @NonNull
    RetryStrategy FIBBONACCI_RETRY_STRATEGY = new RetryStrategy() {

        private final int maxNumberOfRetries = 10;
        private final long logCallDelayMillis = TimeUnit.MILLISECONDS.toMillis(500);

        /**
         * Delay in case of a major error. Note that this delay is the time to wait for a retrial
         * while we have no new data. If the app gets new data then the
         * delay will be short circuited as soon as a new event is available.
         */
        private final long maxLogDelayMillis = TimeUnit.MINUTES.toMillis(10);

        @Override
        public long delayUntilNextRetry(int numberOfRetries, long lastRetryDelay) {
            if (numberOfRetries >= maxNumberOfRetries) {
                return -1;
            }

            return Math.min(Math.max(logCallDelayMillis, (int) (lastRetryDelay * 1.61803)), maxLogDelayMillis);
        }
    };

    @NonNull
    RetryStrategy NEVER_RETRY_STRATEGY = (numberOfRetries, lastRetryDelay) -> -1;
}
