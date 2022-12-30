package tr.karacabey.dmrtd;

import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import tr.karacabey.dmrtd.model.DocumentReadException;
import tr.karacabey.dmrtd.model.ReadResult;

public class TaskRunner<R extends ReadResult> {
    private final Executor executor = Executors.newSingleThreadExecutor(); // change according to your requirements
    private final Handler handler = new Handler(Looper.getMainLooper());

    public interface Callback<R> {
        void onComplete(R result);
    }

    public <R extends ReadResult> void executeAsync(Callable<R> callable, Callback<ReadResult> callback){
        executor.execute(() -> {

            try {
               ReadResult result = callable.call();
                callback.onComplete(result);

            } catch (Exception e) {
                e.printStackTrace();

                String message;
                if(e instanceof DocumentReadException){
                    message= ((DocumentReadException) e).getCode();
                }else{
                    message=e.getMessage();
                }
                callback.onComplete(new ReadResult(false, message, null));



            }

        });
    }
}
