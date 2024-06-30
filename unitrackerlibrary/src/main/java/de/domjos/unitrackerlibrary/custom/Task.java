package de.domjos.unitrackerlibrary.custom;

import android.app.Activity;
import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class Task<Parameter, Progress, Result> {
    private final Executor executor;
    private final Handler handler;
    private final Parameter[] parameters;

    private UpdateProgress<Progress> updateProgress;
    private Activity activity;
    private Callback<Result> callback;

    public Task(Activity activity, UpdateProgress<Progress> updateProgress, Parameter... parameters) {
        this.activity = activity;
        this.executor = Executors.newSingleThreadExecutor();
        this.handler = new Handler(Looper.getMainLooper());
        this.parameters = parameters;
        this.updateProgress = updateProgress;
    }

    public void setOnComplete(Callback<Result> callback) {
        this.callback = callback;
    }

    public void executeAsync(Action<Result, Parameter> action) {
        executor.execute(() -> {
            final Result result;
            try {
                result = action.onExecute(this.parameters);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            handler.post(() -> {
                if(this.callback != null) {
                    this.callback.onComplete(result);
                }
            });
        });
    }

    protected void onUpdate(Progress progress) {
        this.activity.runOnUiThread(()-> {
            if(this.updateProgress != null) {
                this.updateProgress.onProgress(progress);
            }
        });
    }

    public interface Action<Result, Parameter> {
        Result onExecute(Parameter... parameters);
    }

    public interface Callback<Result> {
        void onComplete(Result results);
    }

    public interface UpdateProgress<Progress> {
        void onProgress(Progress progress);
    }
}
