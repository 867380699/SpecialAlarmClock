package zeusro.specialalarmclock.activity;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.ViewConfiguration;

import java.lang.reflect.Field;

import zeusro.specialalarmclock.R;
import zeusro.specialalarmclock.bean.Alarm;
import zeusro.specialalarmclock.receiver.AlarmServiceBroadcastReceiver;

/**
 *
 * Created by Z on 2015/11/16.
 */
public class BaseActivity extends AppCompatActivity{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            ViewConfiguration config = ViewConfiguration.get(this);
            Field menuKeyField = ViewConfiguration.class.getDeclaredField("sHasPermanentMenuKey");
            if (menuKeyField != null) {
                menuKeyField.setAccessible(true);
                menuKeyField.setBoolean(config, false);
            }
        } catch (Exception ex) {
            // Ignore
        }
    }

    protected void cancelAlarmServiceBroadcastReceiver(){
        AlarmServiceBroadcastReceiver receiver = new AlarmServiceBroadcastReceiver();
        receiver.cancelAlarm(this);
    }

    /**
     * 设置闹钟服务
     */
    protected void callAlarmServiceBroadcastReceiver(Alarm alarm) {
        AlarmServiceBroadcastReceiver receiver = new AlarmServiceBroadcastReceiver();
        receiver.setAlarm(this, alarm);
    }
}
