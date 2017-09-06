package zeusro.specialalarmclock.receiver;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;

import java.util.Calendar;
import java.util.List;

import zeusro.specialalarmclock.Constants;
import zeusro.specialalarmclock.Database;
import zeusro.specialalarmclock.bean.Alarm;
import zeusro.specialalarmclock.repository.HolidayRepository;
import zeusro.specialalarmclock.service.SchedulingService;
import zeusro.specialalarmclock.utils.DateTimeUtils;

/**
 * 接收闹钟定时消息的 BroadcastReceiver
 *
 * @author zeusro
 * @since 2017/8/18 下午3:01
 */
public class AlarmServiceBroadcastReceiver extends WakefulBroadcastReceiver {
    public static final String TAG = "AlarmServiceReceiver";
    private PendingIntent alarmIntent;

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "onReceive: ");
        long alarmId = intent.getLongExtra("alarm", 0);
        if (alarmId > 0) {
            Database.init(context);
            Alarm alarm = Database.getAlarm(alarmId);
            if (alarm == null || !alarm.isActive()) {
                return;
            }
            HolidayRepository repository = new HolidayRepository();
            switch (alarm.getRepeatType()) {
                case Alarm.TYPE_ALARM_ONCE:
                case Alarm.TYPE_EVERYDAY:
                    showAlarm(context, alarm);
                    break;
                case Alarm.TYPE_ALARM_WORKDAY:
                    if (repository.isTodayWorkday()) {
                        showAlarm(context, alarm);
                    }
                    break;
                case Alarm.TYPE_ALARM_HOLIDAY:
                    if (repository.isTodayHoliday()) {
                        showAlarm(context, alarm);
                    }
                    break;
                case Alarm.TYPE_ALARM_MON_TO_FRI:
                    if (repository.isTodayCommonWorkday()) {
                        showAlarm(context, alarm);
                    }
                    break;
                case Alarm.TYPE_ALARM_CUSTOM:
                    break;
                default:
                    break;
            }
            // 如果是重复的闹钟，重新设置次日闹钟
            if (alarm.getRepeatType() != Alarm.TYPE_ALARM_ONCE) {
                alarm.setAlarmTime(alarm.getAlarmTime() + DateTimeUtils.DAY);
                setAlarm(context,alarm);
            }
        }
    }

    private void showAlarm(Context context, Alarm alarm) {
        Intent service = new Intent(context, SchedulingService.class);
        service.putExtra("alarm", alarm);
        startWakefulService(context, service);
        setResultCode(Activity.RESULT_OK);
    }

    public void setAlarm(Context context, Alarm alarm) {
        if (alarm == null) {
            return;
        }
        if (System.currentTimeMillis() > alarm.getAlarmTime()) {
            cancelAlarm(context);
            return;
        }
        Intent intent = new Intent(context, AlarmServiceBroadcastReceiver.class);
        intent.setAction(Constants.ACTION_ALARM);
        intent.putExtra("alarm", alarm.getId());
        intent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
        System.out.println("set id: " + alarm.getId());
        alarmIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmMgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmMgr.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, alarm.getAlarmTime(), alarmIntent);
        } else {
            alarmMgr.setExact(AlarmManager.RTC_WAKEUP, alarm.getAlarmTime(), alarmIntent);
        }
    }

    /**
     * 重启后添加所有的闹钟
     *
     * @param context context
     */
    public void resetAllAlarm(Context context) {
        Database.init(context);
        List<Alarm> alarms = Database.getAll();
        Calendar calendarAlarm = Calendar.getInstance();
        for (Alarm alarm : alarms) {
            calendarAlarm.setTimeInMillis(System.currentTimeMillis());
            calendarAlarm.set(Calendar.SECOND, 0);
            calendarAlarm.set(Calendar.MILLISECOND, 0);
            if (alarm.isActive()) {
                Calendar ca = Calendar.getInstance();
                ca.setTimeInMillis(alarm.getAlarmTime());
                calendarAlarm.set(Calendar.HOUR_OF_DAY, ca.get(Calendar.HOUR_OF_DAY));
                calendarAlarm.set(Calendar.MINUTE, ca.get(Calendar.MINUTE));
                if (calendarAlarm.getTimeInMillis() >= System.currentTimeMillis()) {
                    alarm.setAlarmTime(calendarAlarm.getTimeInMillis());
                    setAlarm(context, alarm);
                } else {
                    if (alarm.getRepeatType() == Alarm.TYPE_ALARM_ONCE) {
                        alarm.setActive(false);
                        Database.update(alarm);
                    } else {
                        calendarAlarm.add(Calendar.DAY_OF_YEAR, 1);
                        alarm.setAlarmTime(calendarAlarm.getTimeInMillis());
                        setAlarm(context, alarm);
                    }
                }
            }
        }
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
        AlarmManager alarmMgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmMgr.cancel(alarmIntent);
    }

    public void delayAlarm(Context context,Alarm alarm){
        if (alarmIntent == null) {
            Intent intent = new Intent(context, AlarmServiceBroadcastReceiver.class);
            intent.setAction(Constants.ACTION_ALARM);
            intent.putExtra("alarm", alarm.getId());
            intent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
            alarmIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        }
        AlarmManager alarmMgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmMgr.setExact(AlarmManager.RTC_WAKEUP, new java.util.Date().getTime()+10*1000*60, alarmIntent);
    }
}
