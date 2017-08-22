package zeusro.specialalarmclock.application;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

/**
 *
 * Created by Administrator on 2017/8/18 0018.
 */

public class BaseApplication extends Application {
    private static BaseApplication self;
    @Override
    public void onCreate(){
        super.onCreate();
        self = this;
        this.registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks(){

            @Override
            public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
//                BaseFrameCallback.getInstance().start();
            }

            @Override
            public void onActivityStarted(Activity activity) {
//                BaseFrameCallback.getInstance().start();
            }

            @Override
            public void onActivityResumed(Activity activity) {
//                BaseFrameCallback.getInstance().start();
            }

            @Override
            public void onActivityPaused(Activity activity) {
//                BaseFrameCallback.getInstance().start();
            }

            @Override
            public void onActivityStopped(Activity activity) {
//                BaseFrameCallback.getInstance().start();
            }

            @Override
            public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
//                BaseFrameCallback.getInstance().start();
            }

            @Override
            public void onActivityDestroyed(Activity activity) {
//                BaseFrameCallback.getInstance().start();
            }
        });
    }
    public static BaseApplication getInstance(){
        return self;
    }

}
