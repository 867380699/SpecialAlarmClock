package zeusro.specialalarmclock.activity;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;

import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;

import android.os.Bundle;
import android.util.Log;
import android.view.HapticFeedbackConstants;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;


import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;


import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import zeusro.specialalarmclock.Alarm;
import zeusro.specialalarmclock.Database;
import zeusro.specialalarmclock.R;
import zeusro.specialalarmclock.adapter.AlarmListAdapter;
import zeusro.specialalarmclock.receiver.NotificationWakeUpReceiver;

/**
 * 主activity
 */
public class AlarmActivity extends BaseActivity {

    AlarmListAdapter alarmListAdapter;
    ListView mathAlarmListView;
    ImageButton add, setting;
    private boolean isExit;
    public final static int notificationId = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

//        Log.d("activity","onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        Toast toast = Toast.makeText(this, R.string.Thank, Toast.LENGTH_SHORT);
        //显示toast信息
        toast.show();
        SetlistView();
        SetAddButton();
        SetSettingButton();
    }


    @Override
    protected void onStop() {
        Log.d("activity","onStop");
        super.onStop();
    }

    @Override
    protected void onDestroy() {
//        Log.d("activity", "onDestroy");
        super.onDestroy();
    }

    @Override
    protected void onPause() {
//        Log.d("activity","onPause");
        Database.deactivate();
        super.onPause();
    }

    @Override
    protected void onRestart() {
//        Log.d("activity", "onRestart");
        super.onRestart();
    }

    @Override
    protected void onResume() {
//        Log.d("activity","onResume");
        super.onResume();
        updateAlarmList();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        Log.d("activity","onActivityResult");
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


    /**
     * Called when a view has been clicked.
     *
     * @param v The view that was clicked.
     */
    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.checkBox_alarm_active) {
            CheckBox checkBox = (CheckBox) v;
            Alarm alarm = (Alarm) alarmListAdapter.getItem((Integer) checkBox.getTag());
            alarm.setAlarmActive(checkBox.isChecked());
            Database.update(alarm);
            AlarmActivity.this.CallAlarmServiceBroadcastReciever(alarm);
            if (checkBox.isChecked()) {
                Toast.makeText(AlarmActivity.this, alarm.getTimeUntilNextAlarmMessage(), Toast.LENGTH_LONG).show();
            }
        }
    }


    private void SetAddButton() {
        add = (ImageButton) findViewById(R.id.Add);
        if (add != null) {
            add.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    Intent newAlarmIntent = new Intent(getApplicationContext(), AlarmPreferencesActivity.class);
                    startActivityForResult(newAlarmIntent, 0);
//                    startActivity(newAlarmIntent);
                }

            });
        }
    }


    private void SetlistView() {
        mathAlarmListView = (ListView) findViewById(R.id.listView);
        if (mathAlarmListView != null) {
            mathAlarmListView.setLongClickable(true);
            mathAlarmListView.setOnItemLongClickListener(new OnItemLongClickListener() {
                @Override
                public boolean onItemLongClick(AdapterView<?> adapterView, View view, int position, long id) {
                    view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
                    final Alarm alarm = (Alarm) alarmListAdapter.getItem(position);
                    Builder dialog = new AlertDialog.Builder(AlarmActivity.this);
                    dialog.setTitle("删除");
                    dialog.setMessage("删除这个闹钟?");
                    dialog.setPositiveButton("取消", new OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            dialog.dismiss();
                        }
                    });
                    dialog.setNegativeButton("好", new OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Database.init(AlarmActivity.this);
                            Database.deleteEntry(alarm);
                            //取消
                            AlarmActivity.this.CancelAlarmServiceBroadcastReciever();
                            updateAlarmList();
                        }
                    });
                    dialog.show();
                    return true;
                }
            });
            CallAlarmServiceBroadcastReciever(null);
            alarmListAdapter = new AlarmListAdapter(this);
            this.mathAlarmListView.setAdapter(alarmListAdapter);
            mathAlarmListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                @Override
                public void onItemClick(AdapterView<?> arg0, View v, int position, long id) {
                    v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
                    Alarm alarm = (Alarm) alarmListAdapter.getItem(position);
                    Intent intent = new Intent(AlarmActivity.this, AlarmPreferencesActivity.class);
                    intent.putExtra("alarm", alarm);
                    startActivityForResult(intent, 0);
                }

            });
        }
    }


    public void updateAlarmList() {
        Database.init(AlarmActivity.this);
        final List<Alarm> alarms = Database.getAll();
        alarmListAdapter.setMathAlarms(alarms);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // reload content
                AlarmActivity.this.alarmListAdapter.notifyDataSetChanged();
                TextView text = (TextView) findViewById(R.id.textView);
                if (alarms != null && alarms.size() > 0) {
                    text.setVisibility(View.GONE);
                } else {
                    text.setText(R.string.NoClockAlert);
                    text.setVisibility(View.VISIBLE);
                }
            }
        });
    }


    @Override
    public void onBackPressed() {

        if (!isExit) {
            isExit = true; // 准备退出
            Toast.makeText(this, "再按一次退出", Toast.LENGTH_SHORT).show();
            Timer tExit = new Timer();

            tExit.schedule(new TimerTask() {
                @Override
                public void run() {
                    isExit = false; // 取消退出
                }
            }, 2000); // 如果2秒钟内没有按下返回键，则启动定时器取消掉刚才执行的任务
        } else {
            //退出
            finish();
        }
    }

    private void SetSettingButton() {
        setting = (ImageButton) findViewById(R.id.Setting);
        if (setting != null) {
            setting.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    CreateNotification(null);

                    getHoliday();

//                    Toast.makeText(AlarmActivity.this, "该功能见鬼中", Toast.LENGTH_SHORT).show();
//                    finish();
                }

            });
        }
    }

    private void CreateNotification(Alarm alarm) {
        Intent intent = new Intent();
        intent.setClass(this, NotificationWakeUpReceiver.class);
        sendBroadcast(intent);//发送广播事件
    }


    private void getHoliday(){
        new GetHolidayTask(new DownloadCallback<String>() {
            @Override
            public NetworkInfo getActiveNetworkInfo() {
                ConnectivityManager manager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                return manager.getActiveNetworkInfo();
            }

            @Override
            public void showResult(String result) {
                Toast.makeText(AlarmActivity.this,result,Toast.LENGTH_SHORT).show();
            }
        }).execute();
    }

    public interface DownloadCallback<T>{
        NetworkInfo getActiveNetworkInfo();

        void showResult(T result);
    }

    private class GetHolidayTask extends AsyncTask<Void, Void, String>{
        private DownloadCallback<String> callback;

        public GetHolidayTask(DownloadCallback<String> callback) {
            this.callback = callback;
        }

        @Override
        protected void onPreExecute() {
            if(this.callback!=null){
                NetworkInfo networkInfo = this.callback.getActiveNetworkInfo();
                if(networkInfo==null || !networkInfo.isConnected()){
                    cancel(true);
                }
            }
        }

        @Override
        protected String doInBackground(Void... voids) {
            if (!isCancelled()){
                try {
                    URL url = new URL("http://tool.bitefu.net/jiari?d=2017");
                    String resultString = downloadUrl(url);
                    if(resultString!=null){
                        return resultString;
                    }else{
                        throw new IOException("No response received");
                    }
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            if(result!=null && this.callback!=null){
                this.callback.showResult(result);
            }
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
        }
    }

    private String downloadUrl(URL url) throws IOException{
        InputStream stream = null;
        HttpURLConnection connection = null;
        String result = null;
        try{
            connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(3000);
            connection.setReadTimeout(3000);
            connection.setRequestMethod("GET");
            connection.setDoInput(true);
            connection.connect();
            int responseCode = connection.getResponseCode();
            if(responseCode==HttpURLConnection.HTTP_OK){
                stream = connection.getInputStream();
                if(stream!=null){
                    result = readStream(stream,500);
                }
            }else{
                throw new IOException("HTTP error code: " + responseCode);
            }
        }finally {
            if(connection!=null){
                connection.disconnect();
            }
            if(stream!=null){
                stream.close();
            }
        }
        return result;
    }

    private String readStream(InputStream stream, int maxReadSize) throws IOException, UnsupportedEncodingException{
        Reader reader = new InputStreamReader(stream,"UTF-8");
        char[] rawBuffer = new char[maxReadSize];
        int readSize;
        StringBuilder buffer = new StringBuilder();
        while((readSize = reader.read(rawBuffer)) != -1 && maxReadSize>0){
            if (readSize > maxReadSize){
                readSize = maxReadSize;
            }
            buffer.append(rawBuffer, 0, readSize);
            maxReadSize -= readSize;
        }
        return buffer.toString();
    }

}
