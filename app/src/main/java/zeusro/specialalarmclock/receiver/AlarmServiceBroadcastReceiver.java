package zeusro.specialalarmclock.receiver;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;

import java.util.Calendar;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import zeusro.specialalarmclock.bean.Alarm;
import zeusro.specialalarmclock.Database;
import zeusro.specialalarmclock.service.SchedulingService;

/**
 * 接收闹钟定时消息的 BroadcastReceiver
 *
 * @author zeusro
 * @since 2017/8/18 下午3:01
 */
public class AlarmServiceBroadcastReceiver extends WakefulBroadcastReceiver {
    private Alarm alarm;
    private AlarmManager alarmMgr;
    private PendingIntent alarmIntent;

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(this.getClass().getSimpleName(), Thread.currentThread().getStackTrace()[2].getMethodName());
        try {
            long alarmId = intent.getLongExtra("alarm",0);
            if(alarmId > 0){
                Database.init(context);
                alarm = Database.getAlarm(alarmId);
                Intent service = new Intent(context, SchedulingService.class);
                service.putExtra("alarm", alarm);
                startWakefulService(context, service);
                setResultCode(Activity.RESULT_OK);
            }
            // Start the service, keeping the device awake while it is launching.
        } catch (Exception e) {
            Log.wtf("WTF", e);
        }
    }

    public void setAlarm(Context context, Alarm alarm) {
        if (alarm == null){
            return;
        }
        Calendar calendar = alarm.getAlarmTime();
        if (System.currentTimeMillis() > calendar.getTimeInMillis()) {
            cancelAlarm(context);
            return;
        }
        Intent intent = new Intent(context, AlarmServiceBroadcastReceiver.class);
        intent.setAction("zeusro.action.alert");
        intent.putExtra("alarm", alarm.getId());
        System.out.println("set id: " +alarm.getId());
        alarmIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        alarmMgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmMgr.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), alarmIntent);
        ComponentName receiver = new ComponentName(context, BootReceiver.class);
        PackageManager pm = context.getPackageManager();
        //可用状态
        pm.setComponentEnabledSetting(receiver, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
    }

    public void setAllAlarm(Context context) {
        Alarm alarm = getNext(context);
        if (alarm == null) {
            Log.d(context.getPackageName(), "没有闹钟");
            cancelAlarm(context);
            return;
        }
        Calendar calendar = alarm.getAlarmTime();

        if (System.currentTimeMillis() > calendar.getTimeInMillis()) {
            cancelAlarm(context);
            return;
        }
        Intent intent = new Intent(context, AlarmServiceBroadcastReceiver.class);
        intent.setAction("zeusro.action.alert");
        intent.putExtra("alarm", alarm);
        alarmIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
        alarmMgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmMgr.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), alarmIntent);
        ComponentName receiver = new ComponentName(context, BootReceiver.class);
        PackageManager pm = context.getPackageManager();
        //可用状态
        pm.setComponentEnabledSetting(receiver, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
    }
    /**
     * Cancels the alarm.
     *
     * @param context context
     */
    public void cancelAlarm(Context context) {
        if (alarmIntent == null) {
            Intent intent = new Intent(context, AlarmServiceBroadcastReceiver.class);
            alarmIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
        }
        if (alarmMgr != null) {
            alarmMgr.cancel(alarmIntent);
        }
        ComponentName receiver = new ComponentName(context, BootReceiver.class);
        PackageManager pm = context.getPackageManager();
        pm.setComponentEnabledSetting(receiver,
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP);
    }

    private Alarm getNext(Context context) {
        Set<Alarm> alarmQueue = new TreeSet<Alarm>(new Comparator<Alarm>() {
            @Override
            public int compare(Alarm lhs, Alarm rhs) {
                int result = 0;
                long diff = lhs.getAlarmTime().getTimeInMillis() - rhs.getAlarmTime().getTimeInMillis();
                if (diff > 0) {
                    return 1;
                } else if (diff < 0) {
                    return -1;
                }
                return result;
            }
        });
        Database.init(context);
        List<Alarm> alarms = Database.getAll();
        for (Alarm alarm : alarms) {
            if (alarm.IsAlarmActive())
                alarmQueue.add(alarm);
        }
        if (alarmQueue.iterator().hasNext()) {
            return alarmQueue.iterator().next();
        } else {
            return null;
        }
    }
}
