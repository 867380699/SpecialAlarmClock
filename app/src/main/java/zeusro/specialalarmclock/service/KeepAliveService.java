package zeusro.specialalarmclock.service;

import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.xdandroid.hellodaemon.AbsWorkService;

import zeusro.specialalarmclock.application.BaseApplication;
import zeusro.specialalarmclock.receiver.AlarmServiceBroadcastReceiver;

/**
 * Created by Administrator on 2017/11/7 0007.
 */

public class KeepAliveService extends AbsWorkService {
    @Override
    public Boolean shouldStopService(Intent intent, int flags, int startId) {

        return false;
    }

    @Override
    public void startWork(Intent intent, int flags, int startId) {
        new AlarmServiceBroadcastReceiver().resetAllAlarm(BaseApplication.getContext());
    }

    @Override
    public void stopWork(Intent intent, int flags, int startId) {

    }

    @Override
    public Boolean isWorkRunning(Intent intent, int flags, int startId) {
        return false;

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent, Void alwaysNull) {
        return null;
    }

    @Override
    public void onServiceKilled(Intent rootIntent) {
        new AlarmServiceBroadcastReceiver().resetAllAlarm(BaseApplication.getContext());
    }
}
