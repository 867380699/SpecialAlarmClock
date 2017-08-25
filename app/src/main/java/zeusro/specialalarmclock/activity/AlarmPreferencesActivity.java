package zeusro.specialalarmclock.activity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Vibrator;
import android.text.Editable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.bigkoo.alertview.AlertView;
import com.bigkoo.alertview.OnItemClickListener;
import com.kyleduo.switchbutton.SwitchButton;

import java.util.Calendar;

import zeusro.specialalarmclock.Database;
import zeusro.specialalarmclock.R;
import zeusro.specialalarmclock.application.BaseApplication;
import zeusro.specialalarmclock.bean.Alarm;
import zeusro.specialalarmclock.utils.TextWatcherAdapter;
import zeusro.specialalarmclock.utils.TimePickerUtils;
import zeusro.specialalarmclock.utils.ToastUtils;

public class AlarmPreferencesActivity extends BaseActivity  implements View.OnClickListener, OnItemClickListener{
    public static final String TAG = "AlarmPreferences";
    public static final String KEY_ALARM = "alarm";
    private Alarm alarm;
    private MediaPlayer mediaPlayer;
    private CountDownTimer alarmToneTimer;
    private TimePicker timePicker;
    private String[] alarmTones;
    private String[] alarmTonePaths;
    private Button cancelBtn;
    private Button saveAlarmBtn;
    private RelativeLayout editRemark;
    private AlertView mAlertViewExt;
    private EditText etName;
    private InputMethodManager imm;
    private TextView editRemarkText;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.preferences);
        Bundle bundle = getIntent().getExtras();
        if (bundle != null && bundle.containsKey(KEY_ALARM)) {
            alarm = ((Alarm) bundle.getSerializable(KEY_ALARM));
        } else {
            alarm = new Alarm();
            alarm.getAlarmTime().set(Calendar.MINUTE,Calendar.getInstance().get(Calendar.MINUTE) + 1);
        }
        getSupportActionBar().hide();
        queryRingtoneList();
        setMathAlarm(alarm);
        initTitleEditor();
        initTimePicker();
        initRepeatButton();
        initSwitchButtonView();
        initToolBarBtn();
        initEditRemark();
    }

    private void initEditRemark() {
        editRemark=(RelativeLayout)findViewById(R.id.remark_relative_layout);
        editRemarkText=(TextView)findViewById(R.id.remark_text_secondary);
        editRemark.setOnClickListener(this);
        imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        mAlertViewExt = new AlertView("备注", null, "取消", null, new String[]{"确定"}, this, AlertView.Style.Alert, this);
        ViewGroup extView = (ViewGroup) LayoutInflater.from(this).inflate(R.layout.alertext_form,null);
        etName = (EditText) extView.findViewById(R.id.etName);
        mAlertViewExt.addExtView(extView);
    }

    private void initToolBarBtn() {
        cancelBtn= (Button) findViewById(R.id.cancel_alarm);
        saveAlarmBtn=(Button)findViewById(R.id.save_alarm);
        cancelBtn.setOnClickListener(this);
        saveAlarmBtn.setOnClickListener(this);
    }


    private void queryRingtoneList() {
        Thread t=new Thread(new Runnable() {
            @Override
            public void run() {
                Bundle bundle=AlarmPreferencesActivity.this.getIntent().getExtras();
                if(bundle!=null){
                    alarmTones=(String[])bundle.getSerializable("alarmTones");
                    alarmTonePaths=(String[])bundle.getSerializable("alarmTonePaths");
                }
                if(alarmTones!=null && alarmTonePaths!=null){
                    return;
                }
                RingtoneManager ringtoneMgr = new RingtoneManager(AlarmPreferencesActivity.this);
                ringtoneMgr.setType(RingtoneManager.TYPE_ALARM);
                Cursor alarmsCursor = ringtoneMgr.getCursor();
                alarmTones = new String[alarmsCursor.getCount() + 1];
                alarmTonePaths = new String[alarmsCursor.getCount() + 1];
                alarmTones[0] = "无";
                alarmTonePaths[0] = "";
                if (alarmsCursor.moveToFirst()) {
                    do {
                        int position = alarmsCursor.getPosition();
                        //Log.d("ITEM", ringtoneMgr.getRingtone(position).getTitle(this));
                        //Log.d("ITEM", ringtoneMgr.getRingtoneUri(position).toString());
                        alarmTones[alarmsCursor.getPosition() + 1] = ringtoneMgr.getRingtone(position).getTitle(AlarmPreferencesActivity.this);
                        alarmTonePaths[alarmsCursor.getPosition() + 1] = ringtoneMgr.getRingtoneUri(position).toString();
                    } while (alarmsCursor.moveToNext());
                }
                AlarmPreferencesActivity.this.getIntent().putExtra("alarmTones",alarmTones);
                AlarmPreferencesActivity.this.getIntent().putExtra("alarmTonePaths",alarmTonePaths);
                Log.d(TAG, "Finished Loading " + alarmTones.length + " Ringtones.");
                alarmsCursor.close();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        initRingtoneSelector();
                    }
                });
            }
        });
        t.start();


    }

    private void initRingtoneSelector() {
        final TextView text2 = (TextView) findViewById(R.id.tv_ring_subtitle);

        Uri alarmToneUri = Uri.parse(alarm.getAlarmTonePath());
        Ringtone alarmTone = RingtoneManager.getRingtone(this, alarmToneUri);

        text2.setText(alarmTones[0]);

        findViewById(R.id.ll_ring).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder alert;
                alert = new AlertDialog.Builder(AlarmPreferencesActivity.this);
                alert.setTitle("铃声");
                CharSequence[] items = new CharSequence[alarmTones.length];
                for (int i = 0; i < items.length; i++){
                    items[i] = alarmTones[i];

                }
                alert.setItems(items, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        alarm.setAlarmTonePath(alarmTonePaths[which]);
                        text2.setText(alarmTones[which]);
                        if (alarm.getAlarmTonePath() != null) {
                            if (mediaPlayer == null) {
                                mediaPlayer = new MediaPlayer();
                            } else {
                                if (mediaPlayer.isPlaying())
                                    mediaPlayer.stop();
                                mediaPlayer.reset();
                            }
                            try {
                                // mediaPlayer.setVolume(1.0f, 1.0f);
                                mediaPlayer.setVolume(0.2f, 0.2f);
                                mediaPlayer.setDataSource(AlarmPreferencesActivity.this, Uri.parse(alarm.getAlarmTonePath()));
                                mediaPlayer.setAudioStreamType(AudioManager.STREAM_ALARM);
                                mediaPlayer.setLooping(false);
                                mediaPlayer.prepare();
                                mediaPlayer.start();

                                // Force the mediaPlayer to stop after 3
                                // seconds...
                                if (alarmToneTimer != null)
                                    alarmToneTimer.cancel();
                                alarmToneTimer = new CountDownTimer(3000, 3000) {
                                    @Override
                                    public void onTick(long millisUntilFinished) {

                                    }

                                    @Override
                                    public void onFinish() {
                                        try {
                                            if (mediaPlayer.isPlaying())
                                                mediaPlayer.stop();
                                        } catch (Exception e) {

                                        }
                                    }
                                };
                                alarmToneTimer.start();
                            } catch (Exception e) {
                                try {
                                    if (mediaPlayer.isPlaying())
                                        mediaPlayer.stop();
                                } catch (Exception e2) {

                                }
                            }
                        }
                        setMathAlarm(alarm);
                    }

                });
                alert.show();
            }
        });
    }

    private void initSwitchButtonView() {
        final SwitchButton switchButton = (SwitchButton) findViewById(R.id.shake_switch_btn);
        switchButton.setChecked(alarm.IsVibrate());
        switchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean checked = switchButton.isChecked();
//                switchButton.setChecked(checked);
                alarm.setVibrate(checked);
                if (checked) {
                    Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
                    vibrator.vibrate(1000);
                }
            }
        });
    }

    private void initRepeatButton() {
        final TextView tvRepeat = (TextView) findViewById(R.id.tv_repeat);
        tvRepeat.setText(Alarm.getRepeatTypeString(alarm.getRepeatType()));
        RelativeLayout rlRepeat = (RelativeLayout) findViewById(R.id.week_relative_layout);
        rlRepeat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(AlarmPreferencesActivity.this);
                builder.setItems(Alarm.getRepeatTypeArray(), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        alarm.setRepeatType(which);
                        tvRepeat.setText(Alarm.getRepeatTypeString(alarm.getRepeatType()));
                        Log.d(TAG,Alarm.getRepeatTypeString(which));
                        if(which==Alarm.TYPE_ALARM_CUSTOM){
                            //TODO: 完成自定义功能
                            Log.d(TAG,"custom click");
                        }
                    }
                });
                builder.setView(null);
                builder.create().show();
            }
        });
    }


    private void initTimePicker() {
        timePicker = (TimePicker) findViewById(R.id.timePicker);
        int oldHour = alarm.getAlarmTime().get(Calendar.HOUR_OF_DAY);
        int oldMinute = alarm.getAlarmTime().get(Calendar.MINUTE);
        timePicker.setCurrentHour(oldHour);
        timePicker.setCurrentMinute(oldMinute);
        timePicker.setIs24HourView(true);
        TimePickerUtils.setTimerPickerStyle(timePicker);
        final Calendar newAlarmTime = Calendar.getInstance();
//        newAlarmTime.set(Calendar.HOUR_OF_DAY, Calendar.getInstance().get(Calendar.HOUR_OF_DAY));
//        newAlarmTime.set(Calendar.MINUTE, Calendar.getInstance().get(Calendar.MINUTE) + 1);
//        alarm.setAlarmTime(newAlarmTime);

        if (timePicker != null) {
            timePicker.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {
                @Override
                public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
                    newAlarmTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
                    newAlarmTime.set(Calendar.MINUTE, minute);
                    alarm.setAlarmTime(newAlarmTime);
                    setMathAlarm(alarm);
                    timePicker.setCurrentHour(hourOfDay);
                    timePicker.setCurrentMinute(minute);
                }
            });
        }
    }

    private void initTitleEditor() {
//        EditText etTitle = (EditText) findViewById(R.id.tagText);
//        etTitle.setText(alarm.getAlarmName());
//        etTitle.addTextChangedListener(new TextWatcherAdapter() {
//            @Override
//            public void afterTextChanged(Editable s) {
//                alarm.setAlarmName(s.toString());
//            }
//        });
    }

    public void setMathAlarm(Alarm alarm) {
        this.alarm = alarm;
    }



    @Override
    public void onBackPressed() {
        releaseMusicPlayer();
        super.onBackPressed();
        finish();
        //跨activity传值,用于测试
//        Intent resultIntent = new Intent();
//        Bundle bundle = new Bundle();
//        bundle.putSerializable("object", alarm);
//        resultIntent.putExtras(bundle);
//        setResult(RESULT_OK, resultIntent);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putSerializable("alarm", alarm);
    }

    @Override
    protected void onPause() {
        super.onPause();
        try {
            releaseMusicPlayer();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.save_alarm:{
                Database.init(getApplicationContext());
                if (alarm.getId() < 1) {
                    long id = Database.create(alarm);
                    alarm.setId(id);
                } else {
                    Database.update(alarm);
                }
                callAlarmServiceBroadcastReceiver(alarm);
                ToastUtils.show(alarm.getTimeUntilNextAlarmMessage());
                releaseMusicPlayer();
                finish();
                break;
            }
            case R.id.cancel_alarm:{
                releaseMusicPlayer();
                finish();
                break;
            }
            case R.id.remark_relative_layout:{
                mAlertViewExt.show();
                break;
            }
            default:{
                break;
            }
        }
    }



    private void releaseMusicPlayer() {
        if (mediaPlayer != null){
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    @Override
    public void onItemClick(Object o, int position) {
        closeKeyboard();
        //判断是否是拓展窗口View，而且点击的是非取消按钮
        if(o == mAlertViewExt && position != AlertView.CANCELPOSITION){
            String content = etName.getText().toString();
            if(content.isEmpty()){
            }
            else{
                editRemarkText.setText(content);
                alarm.setAlarmName(content);
            }
            return;
        }
    }

    private void closeKeyboard() {
        //关闭软键盘
        imm.hideSoftInputFromWindow(etName.getWindowToken(),0);
        //恢复位置
        mAlertViewExt.setMarginBottom(0);
    }
}


