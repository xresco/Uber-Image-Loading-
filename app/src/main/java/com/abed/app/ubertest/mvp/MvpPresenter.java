package com.abed.app.ubertest.mvp;

import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public abstract class MvpPresenter<T extends MvpView> {

    @Nullable
    private T attachedView;

    /**
     * View was created and ready to be controlled by presenter. This method should be called
     * in Fragment#onCreateView after all fields with views were initialized.
     *
     * @param viewToAttach view a view that this presenter will control
     */
    @CallSuper
    public void attach(T viewToAttach) {
        this.attachedView = viewToAttach;
    }

    /**
     * View is about to destroy. Stop background tasks if there is no one waiting for it. You
     * should assign null to all references of view in order to prevent memory leaks.
     * <p>
     * This method should be called in Fragment#onDestroy and in Fragment#onDestroyView
     */
    @CallSuper
    public void detach() {
        attachedView = null;
    }

    /**
     * This method can only be called when the presenter has an attached views.
     *
     * @return The attached view.
     * @throws IllegalArgumentException when this method is called and the view is not attached.
     */
    @NonNull
    protected T getAttachedView() {
        if (attachedView == null) {
            throw new IllegalStateException(
                    "Trying to use a view of a detached presenter.");
        }

        return attachedView;
    }

    protected boolean isAttached() {
        return attachedView != null;
    }
}
