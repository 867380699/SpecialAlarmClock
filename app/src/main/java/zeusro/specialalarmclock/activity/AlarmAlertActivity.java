package zeusro.specialalarmclock.activity;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.TextView;

import zeusro.specialalarmclock.R;
import zeusro.specialalarmclock.StaticWakeLock;
import zeusro.specialalarmclock.bean.Alarm;
import zeusro.specialalarmclock.receiver.AlarmServiceBroadcastReceiver;
import zeusro.specialalarmclock.view.SlideView;

/**
 * 闹钟唤醒时的页面，滑动关闭闹钟
 *
 * @author lls
 * @since 2017/8/18 下午3:47
 */
public class AlarmAlertActivity extends AppCompatActivity{
    private Alarm alarm;
    private MediaPlayer mediaPlayer;
    private Vibrator vibrator;
    private boolean alarmActive;
    private TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.alert);
        Bundle bundle = this.getIntent().getExtras();
        if (bundle != null) {
            alarm = (Alarm) bundle.getSerializable("alarm");
            if (null != alarm) {
                this.setTitle(alarm.getAlarmName());
                startAlarm();
            }
        }
        textView = (TextView) findViewById(R.id.textView2);
        textView.setText(alarm.toString());
        setSlideView();
        setTelephonyStateChangedListener();
    }

    private void setTelephonyStateChangedListener() {
        PhoneStateListener phoneStateListener = new PhoneStateListener() {
            @Override
            public void onCallStateChanged(int state, String incomingNumber) {
                switch (state) {
                    case TelephonyManager.CALL_STATE_RINGING:
                        Log.d(getClass().getSimpleName(), "Incoming call: " + incomingNumber);
                        try {
                            mediaPlayer.pause();
                        } catch (IllegalStateException e) {

                        }
                        break;
                    case TelephonyManager.CALL_STATE_IDLE:
                        Log.d(getClass().getSimpleName(), "Call State Idle");
                        if (mediaPlayer!=null){
                            try {
                                mediaPlayer.start();
                            } catch (IllegalStateException e) {
                                e.printStackTrace();
                            }
                        }
                        break;
                }
                super.onCallStateChanged(state, incomingNumber);
            }
        };
        TelephonyManager telephonyManager = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
        telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);
    }

    private void setSlideView() {
        SlideView slideView = (SlideView) findViewById(R.id.slider);
        slideView.setSlideListener(new SlideView.SlideListener() {
            @Override
            public void onDone() {
                AlarmServiceBroadcastReceiver receiver = new AlarmServiceBroadcastReceiver();
                receiver.cancelAlarm(AlarmAlertActivity.this);
                releaseRelease();
                finishAffinity();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        alarmActive = true;
    }

    private void startAlarm() {
        if (!"".equals(alarm.getAlarmTonePath())) {
            mediaPlayer = new MediaPlayer();
            if (alarm.getVibrate()) {
                vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
                long[] pattern = {1000, 200, 200, 200};
                vibrator.vibrate(pattern, 0);
            }
            try {
                mediaPlayer.setVolume(1.0f, 1.0f);
                mediaPlayer.setDataSource(this, Uri.parse(alarm.getAlarmTonePath()));
                mediaPlayer.setAudioStreamType(AudioManager.STREAM_ALARM);
                mediaPlayer.setLooping(true);
                mediaPlayer.prepare();
                mediaPlayer.start();

            } catch (Exception e) {
                mediaPlayer.release();
                mediaPlayer = null;
                alarmActive = false;
            }
        }

    }

    /**
     * 禁止返回取消闹钟
     */
    @Override
    public void onBackPressed() {
        if (!alarmActive){
            super.onBackPressed();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        StaticWakeLock.lockOff(this);
    }

    protected void releaseRelease() {
        if (mediaPlayer != null) {
            if(mediaPlayer.isPlaying()){
                mediaPlayer.stop();
            }
            mediaPlayer.release();
            mediaPlayer = null;
            finish();
        }
    }

    @Override
    protected void onDestroy() {
        try {
            if (vibrator != null)
                vibrator.cancel();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if(mediaPlayer!=null) {
            try {
                mediaPlayer.stop();
                mediaPlayer.release();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        super.onDestroy();
    }
}
