package zeusro.specialalarmclock.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.database.Cursor;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Vibrator;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import zeusro.specialalarmclock.Alarm;
import zeusro.specialalarmclock.AlarmPreference;
import zeusro.specialalarmclock.Database;
import zeusro.specialalarmclock.Key;
import zeusro.specialalarmclock.R;
import zeusro.specialalarmclock.Type;

public class AlarmPreferencesActivity extends BaseActivity {
    public static final String TAG = "AlarmPreferences";
    private Alarm alarm;
    private MediaPlayer mediaPlayer;
    private ListView listView;
    private CountDownTimer alarmToneTimer;
    private final String[] repeatDays = {"一", "二", "三", "四", "五", "六", "日"};
    private EditText etTitle;
    TimePicker timePicker1;
    private List<AlarmPreference> preferences = new ArrayList<>();
    private String[] alarmTones;
    private String[] alarmTonePaths;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.preferences);
        Bundle bundle = getIntent().getExtras();
        if (bundle != null && bundle.containsKey("alarm")) {
            //更新数据
            alarm = ((Alarm) bundle.getSerializable("alarm"));
        } else {
            alarm = (new Alarm());
        }

        RingtoneManager ringtoneMgr = new RingtoneManager(this);
        ringtoneMgr.setType(RingtoneManager.TYPE_ALARM);
        Cursor alarmsCursor = ringtoneMgr.getCursor();
        alarmTones = new String[alarmsCursor.getCount() + 1];
        alarmTones[0] = "静默模式";
        alarmTonePaths = new String[alarmsCursor.getCount() + 1];
        alarmTonePaths[0] = "";
        if (alarmsCursor.moveToFirst()) {
            do {
                Log.d("ITEM", ringtoneMgr.getRingtone(alarmsCursor.getPosition()).getTitle(this));
                Log.d("ITEM", ringtoneMgr.getRingtoneUri(alarmsCursor.getPosition()).toString());
                alarmTones[alarmsCursor.getPosition() + 1] = ringtoneMgr.getRingtone(alarmsCursor.getPosition()).getTitle(this);
                alarmTonePaths[alarmsCursor.getPosition() + 1] = ringtoneMgr.getRingtoneUri(alarmsCursor.getPosition()).toString();
            } while (alarmsCursor.moveToNext());
        }
        Log.d(TAG, "Finished Loading " + alarmTones.length + " Ringtones.");
        alarmsCursor.close();
        setMathAlarm(alarm);
//TEST

        etTitle = (EditText) findViewById(R.id.tagText);
        etTitle.setText(alarm.getAlarmName());
        etTitle.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                alarm.setAlarmName(s.toString());
            }
        });

        timePicker1 = (TimePicker) findViewById(R.id.timePicker);
        int oldHour = alarm.getAlarmTime().get(Calendar.HOUR_OF_DAY);
        int oldMinute = alarm.getAlarmTime().get(Calendar.MINUTE);
        Toast tt = new Toast(this);
        //// FIXME: 2015/11/25 正式环境改回去
//                timePicker1.setCurrentHour(oldHour);
//                timePicker1.setCurrentMinute(oldMinute);
        final Calendar newAlarmTime = Calendar.getInstance();
        //comment
        timePicker1.setCurrentHour(Calendar.getInstance().get(Calendar.HOUR_OF_DAY));
        timePicker1.setCurrentMinute(Calendar.getInstance().get(Calendar.MINUTE) + 1);
        newAlarmTime.set(Calendar.HOUR_OF_DAY, Calendar.getInstance().get(Calendar.HOUR_OF_DAY));
        newAlarmTime.set(Calendar.MINUTE, Calendar.getInstance().get(Calendar.MINUTE) + 1);
        alarm.setAlarmTime(newAlarmTime);
        //comment
        if (timePicker1 != null) {
            timePicker1.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {
                @Override
                public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
                    newAlarmTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
                    newAlarmTime.set(Calendar.MINUTE, minute);
                    alarm.setAlarmTime(newAlarmTime);
                    setMathAlarm(alarm);
                    timePicker1.setCurrentHour(hourOfDay);
                    timePicker1.setCurrentMinute(minute);
//
                }
            });
        }
        SetWeekButton((Button) findViewById(R.id.btn_Sunday), Calendar.SUNDAY);
        SetWeekButton((Button) findViewById(R.id.btn_Monday), Calendar.MONDAY);
        SetWeekButton((Button) findViewById(R.id.btn_Tuesday), Calendar.TUESDAY);
        SetWeekButton((Button) findViewById(R.id.btn_Webnesday), Calendar.WEDNESDAY);
        SetWeekButton((Button) findViewById(R.id.btn_Thursday), Calendar.THURSDAY);
        SetWeekButton((Button) findViewById(R.id.btn_Friday), Calendar.FRIDAY);
        SetWeekButton((Button) findViewById(R.id.btn_Saturday), Calendar.SATURDAY);

        CheckedTextView checkedTextView = (CheckedTextView) findViewById(android.R.id.text1);
        checkedTextView.setText(preferences.get(4).getTitle());
        checkedTextView.setChecked((Boolean) (preferences.get(4).getValue()));
        checkedTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean checked = !((CheckedTextView)v).isChecked();
                ((CheckedTextView) v).setChecked(checked);
                switch (preferences.get(4).getKey()) {
                    case ALARM_VIBRATE:
                        alarm.setVibrate(checked);
                        if (checked) {
                            Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
                            vibrator.vibrate(1000);
                        }
                        break;
                }
                preferences.get(4).setValue(checked);
            }
        });

        TextView text1 = (TextView) findViewById(R.id.tv_ring_title);
        text1.setTextSize(18);
        text1.setText(preferences.get(3).getTitle());

        TextView text2 = (TextView) findViewById(R.id.tv_ring_subtitle);
        text2.setText(preferences.get(3).getSummary());

        findViewById(R.id.ll_ring).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder alert;
                alert = new AlertDialog.Builder(AlarmPreferencesActivity.this);
                alert.setTitle(preferences.get(3).getTitle());
                CharSequence[] items = new CharSequence[preferences.get(3).getOptions().length];
                for (int i = 0; i < items.length; i++)
                    items[i] = preferences.get(3).getOptions()[i];
                alert.setItems(items, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        alarm.setAlarmTonePath(getAlarmTonePaths()[which]);
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

    public void setMathAlarm(Alarm alarm) {
        this.alarm = alarm;
        preferences.clear();
//        preferences.add(new AlarmPreference(Key.ALARM_ACTIVE, context.getString(R.string.AlarmStatus), null, null, alarm.getAlarmActive(), Type.BOOLEAN));
        preferences.add(new AlarmPreference(Key.ALARM_NAME, "标签", alarm.getAlarmName(), null, alarm.getAlarmName(), Type.EditText));
        preferences.add(new AlarmPreference(Key.ALARM_TIME, "时间", alarm.getAlarmTimeString(), null, alarm.getAlarmTime(), Type.TIME));
        preferences.add(new AlarmPreference(Key.ALARM_REPEAT, "重复", "重复", repeatDays, alarm.getDays(), Type.MULTIPLE_ImageButton));

        Uri alarmToneUri = Uri.parse(alarm.getAlarmTonePath());
        Ringtone alarmTone = RingtoneManager.getRingtone(this, alarmToneUri);

        if (alarmTone instanceof Ringtone && !alarm.getAlarmTonePath().equalsIgnoreCase("")) {
            preferences.add(new AlarmPreference(Key.ALARM_TONE, "铃声", alarmTone.getTitle(this), alarmTones, alarm.getAlarmTonePath(), Type.Ring));
        } else {
            preferences.add(new AlarmPreference(Key.ALARM_TONE, "铃声", getAlarmTones()[0], alarmTones, null, Type.Ring));
        }

        preferences.add(new AlarmPreference(Key.ALARM_VIBRATE, "振动", null, null, alarm.IsVibrate(), Type.BOOLEAN));
    }
    public String[] getAlarmTones() {
        return alarmTones;
    }

    void SetWeekButton(Button button, final int dayOfWeek) {
        final Button week = button;
        if (week != null) {
            Boolean isRepeat = alarm.IsRepeat(dayOfWeek);
            if (isRepeat) {
                week.setTextColor(Color.WHITE);
                week.setBackgroundColor(Color.GRAY);
            } else {
                week.setTextColor(Color.BLACK);
                week.setBackgroundColor(Color.WHITE);
            }
            week.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int oldButtonTextColor = week.getCurrentTextColor();
                    //0 白色
                    if (oldButtonTextColor != -1) {  // 当前文本颜色为黑/-1

                        week.setTextColor(Color.WHITE);
                        week.setBackgroundColor(Color.GRAY);
                        //选中
                        if (alarm != null)
                            alarm.addDay(dayOfWeek);
                        Log.d("data", String.valueOf(dayOfWeek));

                    } else {
                        int[] days = alarm.getDays();
                        //至少选择一项才允许取消
                        if (days != null && days.length > 0) {
                            week.setTextColor(Color.BLACK);
                            week.setBackgroundColor(Color.WHITE);
                            //为取消
                            if (alarm != null)
                                alarm.removeDay(dayOfWeek);
                        }

                    }
                }
            });
        }

    }

    @Override
    public void onBackPressed() {
//        String data = "data";
//        Log.d(data, alarm.getAlarmName());
//        Log.d(data, alarm.getAlarmTime().toString());
//        Log.d(data, String.valueOf(alarm.getDays().length));
//        Log.d(data, alarm.getAlarmTonePath());
//        Log.d(data, String.valueOf(alarm.getVibrate()));


//保存闹钟信息
//        int[] days = alarm.getDays();
//        if (days == null || days.length < 1) {
//            //todo: 当任何一天都不重复时,只提醒一次
//        }
        Database.init(getApplicationContext());
        if (alarm.getId() < 1) {
            Database.create(alarm);
        } else {
            Database.update(alarm);
        }
        CallAlarmServiceBroadcastReciever(alarm);
        Toast.makeText(AlarmPreferencesActivity.this, alarm.getTimeUntilNextAlarmMessage(), Toast.LENGTH_LONG).show();
        //跨activity传值,用于测试
//        Intent resultIntent = new Intent();
//        Bundle bundle = new Bundle();
//        bundle.putSerializable("object", alarm);
//        resultIntent.putExtras(bundle);
//        setResult(RESULT_OK, resultIntent);
        ReleaseMusicPlayer();
        super.onBackPressed();
        finish();
    }
    public String[] getAlarmTonePaths() {
        return alarmTonePaths;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putSerializable("alarm", alarm);
    }

    @Override
    protected void onPause() {
        super.onPause();
        try {
            ReleaseMusicPlayer();
        } catch (Exception e) {
        }
        // setListAdapter(null);
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);

    }

    private void ReleaseMusicPlayer() {
        if (mediaPlayer != null)
            mediaPlayer.release();
        mediaPlayer = null;
    }
}


