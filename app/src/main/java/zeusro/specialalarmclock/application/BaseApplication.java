package zeusro.specialalarmclock.application;

import android.app.Activity;
import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;

import zeusro.specialalarmclock.receiver.BootReceiver;

/**
 *
 * Created by Administrator on 2017/8/18 0018.
 */

public class BaseApplication extends Application {
    private static BaseApplication self;

    public static Context getContext() {
        return context;
    }

    public static void setContext(Context context) {
        BaseApplication.context = context;
    }

    private static Context context;
    @Override
    public void onCreate(){
        super.onCreate();
        this.context=getApplicationContext();
        self = this;
        enableBootReceiver();
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

    @Override
    public void onTerminate() {
        super.onTerminate();
        enableBootReceiver();
    }

    private void enableBootReceiver(){
        ComponentName receiver = new ComponentName(getApplicationContext(), BootReceiver.class);
        PackageManager pm = getApplicationContext().getPackageManager();
        pm.setComponentEnabledSetting(receiver, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
    }

    public static BaseApplication getInstance(){
        return self;
    }

}
