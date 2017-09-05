package zeusro.specialalarmclock.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * 关机后所有的定时会被取消，使用这个Receiver监听重启，重置闹钟
 *
 * @author zeusro
 * @since 2017/8/18 下午2:43
 */
public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(this.getClass().getSimpleName(), Thread.currentThread().getStackTrace()[2].getMethodName());
        if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
            AlarmServiceBroadcastReceiver alarm = new AlarmServiceBroadcastReceiver();
            alarm.resetAllAlarm(context);
        }
    }
}
