package zeusro.specialalarmclock.activity;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TimePicker;

import com.bigkoo.alertview.AlertView;
import com.bigkoo.alertview.OnItemClickListener;
import com.kevalpatel.ringtonepicker.RingtonePickerDialog;
import com.kevalpatel.ringtonepicker.RingtonePickerListener;
import com.kyleduo.switchbutton.SwitchButton;

import java.util.Calendar;

import zeusro.specialalarmclock.Database;
import zeusro.specialalarmclock.R;
import zeusro.specialalarmclock.bean.Alarm;
import zeusro.specialalarmclock.utils.DateTimeUtils;
import zeusro.specialalarmclock.utils.TimePickerUtils;
import zeusro.specialalarmclock.utils.ToastUtils;

/**
 * 设置闹钟的界面
 *
 * @author lls
 * @since 2017/9/6 上午10:59
 */
public class AlarmPreferencesActivity extends BaseActivity  implements View.OnClickListener, OnItemClickListener{
    public static final String TAG = "AlarmPreferences";
    public static final String KEY_ALARM = "alarm";
    public static final int REQUEST_CODE_PERMISSION_READ_EXTERNAL_STORAGE = 111;
    private Alarm alarm;
    private TimePicker timePicker;
    private AlertView mAlertViewExt;
    private EditText etName;
    private InputMethodManager imm;
    private TextView editRemarkText;
    private AlarmPreferencesActivity self;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.preferences);
        self = this;
        Bundle bundle = getIntent().getExtras();
        if (bundle != null && bundle.containsKey(KEY_ALARM)) {
            alarm = ((Alarm) bundle.getSerializable(KEY_ALARM));
        } else {
            alarm = new Alarm();
            alarm.setAlarmTime(System.currentTimeMillis()+ DateTimeUtils.MINUTE);
        }
        getSupportActionBar().hide();
        initRingtonePicker();
        setMathAlarm(alarm);
        initTimePicker();
        initRepeatButton();
        initSwitchButtonView();
        initToolBarBtn();
        initEditRemark();
    }

    private void initEditRemark() {
        RelativeLayout editRemark = (RelativeLayout) findViewById(R.id.remark_relative_layout);
        editRemarkText=(TextView)findViewById(R.id.remark_text_secondary);
        editRemarkText.setText(alarm.getAlarmName());
        editRemark.setOnClickListener(this);
        imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        mAlertViewExt = new AlertView("备注", null, "取消", null, new String[]{"确定"}, this, AlertView.Style.Alert, this);
        ViewGroup extView = (ViewGroup) LayoutInflater.from(this).inflate(R.layout.alertext_form,null);
        etName = (EditText) extView.findViewById(R.id.etName);
        mAlertViewExt.addExtView(extView);
    }

    private void initToolBarBtn() {
        Button cancelBtn = (Button) findViewById(R.id.cancel_alarm);
        Button saveAlarmBtn = (Button) findViewById(R.id.save_alarm);
        cancelBtn.setOnClickListener(this);
        saveAlarmBtn.setOnClickListener(this);
    }

    private void initRingtonePicker(){
        final TextView text2 = (TextView) findViewById(R.id.tv_ring_subtitle);
        text2.setText(alarm.getAlarmToneName());
         final Uri alarmToneUri = Uri.parse(alarm.getAlarmTonePath());


        findViewById(R.id.ll_ring).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RingtonePickerDialog.Builder ringtonePickerBuilder = new RingtonePickerDialog.Builder(self, getSupportFragmentManager());
                ringtonePickerBuilder.setTitle("铃声");

                //Add the desirable ringtone types.
                if(checkPermission()){
                    ringtonePickerBuilder.addRingtoneType(RingtonePickerDialog.Builder.TYPE_MUSIC);
                }
                ringtonePickerBuilder.addRingtoneType(RingtonePickerDialog.Builder.TYPE_NOTIFICATION);
                ringtonePickerBuilder.addRingtoneType(RingtonePickerDialog.Builder.TYPE_RINGTONE);
                ringtonePickerBuilder.addRingtoneType(RingtonePickerDialog.Builder.TYPE_ALARM);

                ringtonePickerBuilder.setPositiveButtonText("设置");

                ringtonePickerBuilder.setCancelButtonText("取消");

                //Set flag true if you want to play the com.ringtonepicker.sample of the clicked tone.
                ringtonePickerBuilder.setPlaySampleWhileSelection(true);

                ringtonePickerBuilder.setListener(new RingtonePickerListener() {
                    @Override
                    public void OnRingtoneSelected(String ringtoneName, Uri ringtoneUri) {
                        alarm.setAlarmTonePath(ringtoneUri.toString());
                        alarm.setAlarmToneName(ringtoneName);
                        text2.setText(ringtoneName);
                        setMathAlarm(alarm);
                    }
                });
                //set the currently selected uri, to mark that ringtone as checked by default. (Optional)
                ringtonePickerBuilder.setCurrentRingtoneUri(alarmToneUri);

                ringtonePickerBuilder.show();
            }
        });
    }

    private boolean checkPermission() {
        int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
        if(permissionCheck== PackageManager.PERMISSION_GRANTED){
            return true;
        }else{
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_CODE_PERMISSION_READ_EXTERNAL_STORAGE);
        }
        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case REQUEST_CODE_PERMISSION_READ_EXTERNAL_STORAGE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    ToastUtils.show("授权成功，加载更多铃声");
                }else{
                    ToastUtils.show("授权失败");
                }
        }
    }

    private void initSwitchButtonView() {
        final SwitchButton switchButton = (SwitchButton) findViewById(R.id.shake_switch_btn);
        switchButton.setChecked(alarm.getVibrate());
        final TextView shakeSecondaryText=(TextView)findViewById(R.id.shake_secondary_text);
        switchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean checked = switchButton.isChecked();
//                switchButton.setChecked(checked);
                alarm.setVibrate(checked);
                if (checked) {
                    Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
                    vibrator.vibrate(1000);
                    shakeSecondaryText.setText("响铃时振动");
                    alarm.setVibrate(true);
                }else{
                    shakeSecondaryText.setText("无");
                    alarm.setVibrate(false);
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
                AlertDialog.Builder builder = new AlertDialog.Builder(self);
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
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(alarm.getAlarmTime());
        timePicker.setIs24HourView(true);
        int oldHour = calendar.get(Calendar.HOUR_OF_DAY);
        int oldMinute = calendar.get(Calendar.MINUTE);
        timePicker.setCurrentHour(oldHour);
        timePicker.setCurrentMinute(oldMinute);
        TimePickerUtils.setTimerPickerStyle(timePicker);
        final Calendar newAlarmTime = Calendar.getInstance();
        newAlarmTime.setTimeInMillis(calendar.getTimeInMillis());

        if (timePicker != null) {
            timePicker.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {
                @Override
                public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
                    newAlarmTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
                    newAlarmTime.set(Calendar.MINUTE, minute);
                    newAlarmTime.set(Calendar.SECOND,0);
                    newAlarmTime.set(Calendar.MILLISECOND,0);
                    alarm.setAlarmTime(newAlarmTime.getTimeInMillis());
                    setMathAlarm(alarm);
                    timePicker.setCurrentHour(hourOfDay);
                    timePicker.setCurrentMinute(minute);
                }
            });
        }
    }

    public void setMathAlarm(Alarm alarm) {
        this.alarm = alarm;
    }



    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putSerializable("alarm", alarm);
    }

    @Override
    protected void onPause() {
        super.onPause();
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
                finish();
                break;
            }
            case R.id.cancel_alarm:{
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

    @Override
    public void onItemClick(Object o, int position) {
        closeKeyboard();
        //判断是否是拓展窗口View，而且点击的是非取消按钮
        if(o == mAlertViewExt && position != AlertView.CANCELPOSITION){
            String content = etName.getText().toString();
            if(content.isEmpty()){
                editRemarkText.setText("无");
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


