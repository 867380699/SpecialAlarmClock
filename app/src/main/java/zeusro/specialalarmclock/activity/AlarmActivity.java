package zeusro.specialalarmclock.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Timer;
import java.util.TimerTask;

import zeusro.specialalarmclock.Database;
import zeusro.specialalarmclock.R;
import zeusro.specialalarmclock.adapter.AlarmAdapter;
import zeusro.specialalarmclock.bean.Alarm;
import zeusro.specialalarmclock.receiver.NotificationWakeUpReceiver;
import zeusro.specialalarmclock.repository.HolidayRepository;

/**
 * 主activity
 */
public class AlarmActivity extends BaseActivity{
    public static final String TAG = "AlarmActivity";
    private ImageButton btnAdd;
    private FloatingActionButton btnSetting;
    private boolean isExit;
    public final static int notificationId = 1;
    private HolidayRepository repository;
    AlarmAdapter adapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        Toast.makeText(this, R.string.Thank, Toast.LENGTH_SHORT).show();
        repository = new HolidayRepository(this);
        initView();
    }

    private void initView() {
        ActionBar actionBar = getSupportActionBar();
        if(actionBar!=null){
            actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
            actionBar.setCustomView(R.layout.middle_title_actionbar);
        }
        initAlarmList();
        initAddAlarmButton();
        initSettingButton();
    }

    @Override
    protected void onPause() {
        Database.deactivate();
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(repository==null){
            repository = new HolidayRepository(this);
        }
        repository.updateHolidayAndWorkday();
        if(adapter!=null){
            int count = adapter.updateData();
            TextView text = (TextView) findViewById(R.id.textView);
            if (count > 0) {
                text.setVisibility(View.GONE);
            } else {
                text.setText(R.string.NoClockAlert);
                text.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d("onActivityResult", String.valueOf(resultCode));
        switch (resultCode) {
            case RESULT_OK:
                Bundle b = data.getExtras();
                Alarm alarm = (Alarm) b.getSerializable("object");//回传的值
                if (alarm != null) {
                    Log.d("data", alarm.getAlarmName());
                }
                break;
            default:
                break;
        }
    }

    private void initAddAlarmButton() {
        btnAdd = (ImageButton) findViewById(R.id.btn_add_alarm);
        if (btnAdd != null) {
            btnAdd.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    Intent newAlarmIntent = new Intent(getApplicationContext(), AlarmPreferencesActivity.class);
                    startActivityForResult(newAlarmIntent, 0);
                }

            });
        }
    }


    private void initAlarmList() {
        RecyclerView rcvAlarm = (RecyclerView) findViewById(R.id.listView);
        rcvAlarm.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AlarmAdapter(this);
        adapter.setOnItemClickListener(new AlarmAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Alarm alarm) {
                AlarmActivity.this.callAlarmServiceBroadcastReceiver(alarm);
            }
        });
        rcvAlarm.setAdapter(adapter);
        rcvAlarm.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if(dy>0 && btnSetting.isShown()){
                    btnSetting.hide();
                }else if(dy<0 && !btnSetting.isShown()){
                    btnSetting.show();
                }
            }
        });
    }

    private void initSettingButton() {
        btnSetting = (FloatingActionButton) findViewById(R.id.fab_setting);
        if (btnSetting != null) {
            btnSetting.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    createNotification(null);
                }

            });
        }
    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(false);
    }

    private void createNotification(Alarm alarm) {
        Intent intent = new Intent();
        intent.setClass(this, NotificationWakeUpReceiver.class);
        sendBroadcast(intent);//发送广播事件
    }
}
