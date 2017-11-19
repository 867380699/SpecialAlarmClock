package zeusro.specialalarmclock.application;

import android.app.Activity;
import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.Nullable;

import com.xdandroid.hellodaemon.DaemonEnv;

import zeusro.specialalarmclock.activity.AlarmActivity;
import zeusro.specialalarmclock.activity.BaseActivity;
import zeusro.specialalarmclock.receiver.BootReceiver;
import zeusro.specialalarmclock.service.KeepAliveService;

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

        DaemonEnv.initialize(
                context,  //Application Context.
                KeepAliveService.class, //刚才创建的 Service 对应的 Class 对象.
                30*60*1000);  //定时唤醒的时间间隔(ms), 默认 6 分钟.
        context.startService(new Intent(context, KeepAliveService.class));

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
