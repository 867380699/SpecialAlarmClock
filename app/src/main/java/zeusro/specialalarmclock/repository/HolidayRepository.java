package zeusro.specialalarmclock.repository;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.NonNull;
import android.support.v4.util.ArraySet;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Set;

import zeusro.specialalarmclock.application.BaseApplication;
import zeusro.specialalarmclock.net.DownloadCallback;
import zeusro.specialalarmclock.net.HttpGetAsyncTask;
import zeusro.specialalarmclock.utils.DateTimeUtils;

/**
 * 假期和工作日数据的DAO
 * TODO: 回调很多，显得过于复杂了，待改进
 * @author lls
 * @since 2017/8/18 上午9:05
 */

public class HolidayRepository {
    private static final String TAG = "HolidayRepository";
    private static final String SP_NAME = "HolidayRepository";
    private static final String GET_HOLIDAY = "http://tool.bitefu.net/jiari/data/%s.txt";
    private static final String GET_SPECIAL_WORKING_DAY = "http://tool.bitefu.net/jiari/data/%s_w.txt";
    private static final String SP_KEY_HOLIDAY = "holiday_list";
    private static final String SP_KEY_HOLIDAY_UPDATE_TIME = "holiday_list_update_time";
    private static final String SP_KEY_WORKING_DAY = "working_day_list";
    private static final String SP_KEY_WORKING_DAY_UPDATE_TIME = "holiday_list_update_time";
    private Context context;

    public interface Callback {
        void onDataLoaded(ArrayList<String> result);
        void onDataNotAvailable(String result);
    }

    /**
     *
     * @param context context
     */
    public HolidayRepository(@NonNull Context context) {
        this.context = context;
    }
    public HolidayRepository(){
        this.context = BaseApplication.getInstance().getApplicationContext();
    }

    /**
     * 从服务器获取某年的法定假日, 并缓存到本地
     *
     * @param year 年份
     */
    public void getHolidayFromRemote(String year, final Callback callback) {
        new HttpGetAsyncTask(new DownloadCallback<String>() {
            @Override
            public NetworkInfo getActiveNetworkInfo() {
                ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
                return manager.getActiveNetworkInfo();
            }

            @Override
            public void showResult(String result) {
                if (result != null) {
                    saveHolidayToLocal(result);
                }
                if(callback!=null){
                    callback.onDataLoaded(loadHolidayFromLocal());
                }
            }
        }).execute(String.format(GET_HOLIDAY, year));
    }

    /**
     * 从服务器获取某年的特殊工作日, 并缓存到本地
     *
     * @param year 年份
     */
    public void getSpecialWorkingDayFromRemote(String year, final Callback callback) {
        new HttpGetAsyncTask(new DownloadCallback<String>() {
            @Override
            public NetworkInfo getActiveNetworkInfo() {
                ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
                return manager.getActiveNetworkInfo();
            }

            @Override
            public void showResult(String result) {
                if (result != null) {
                    saveSpecialWorkingDayToLocal(result);
                    callback.onDataLoaded(loadSpecialWorkingDayFromLocal());
                }else{
                    callback.onDataNotAvailable("");
                }
            }
        }).execute(String.format(GET_SPECIAL_WORKING_DAY, year));
    }

    /**
     * 将从服务器获取来的数据处理后存储至本地
     * @param holidays 服务器返回的txt数据
     */
    private void saveHolidayToLocal(String holidays){
        String[] lines = holidays.split("\n");
        ArraySet<String> holidayList = new ArraySet<>();
        for (String line : lines) {
            String line_trim = line.trim();
            if (line_trim.length() == 4) {
                holidayList.add(line_trim);
            }
        }
        SharedPreferences pref = context.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putStringSet(SP_KEY_HOLIDAY, new ArraySet<>(holidayList));
        editor.putLong(SP_KEY_HOLIDAY_UPDATE_TIME, System.currentTimeMillis());
        editor.apply();
    }


    public ArrayList<String> loadHolidayFromLocal(){
        SharedPreferences pref = context.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE);
        Set<String> holidaySet = pref.getStringSet(SP_KEY_HOLIDAY,null);
        if(holidaySet!=null){
            return new ArrayList<>(holidaySet);
        }
        return null;
    }

    /**
     * 将从服务器获取来的数据处理后存储至本地
     * @param workdays 服务器返回的txt数据
     */
    private void saveSpecialWorkingDayToLocal(String workdays){
        String[] lines = workdays.split("\n");
        System.out.println(Arrays.asList(lines));
        ArraySet<String> holidayList = new ArraySet<>();
        for (String line : lines) {
            String line_trim = line.trim();
            if (line_trim.length() == 4) {
                holidayList.add(line_trim);
            }
        }
        System.out.println(holidayList);
        SharedPreferences pref = context.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putStringSet(SP_KEY_WORKING_DAY, new ArraySet<>(holidayList));
        editor.putLong(SP_KEY_WORKING_DAY_UPDATE_TIME, System.currentTimeMillis());
        editor.apply();
    }

    public ArrayList<String> loadSpecialWorkingDayFromLocal(){
        SharedPreferences pref = context.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE);
        Set<String> holidaySet = pref.getStringSet(SP_KEY_WORKING_DAY,null);
        if(holidaySet!=null){
            return new ArrayList<>(holidaySet);
        }
        return null;
    }

    /**
     * 先从本地获取数据，失败则从服务器获取
     * @param callback callback
     */
    public void getSpecialWorkday(final Callback callback){
        SharedPreferences pref = context.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE);
        long saveTime = pref.getLong(SP_KEY_WORKING_DAY_UPDATE_TIME,0);
        if(System.currentTimeMillis()-saveTime< DateTimeUtils.DAY_IN_MILISECOND){
            Set<String> workdaySet =  pref.getStringSet(SP_KEY_WORKING_DAY,null);
            if(workdaySet!=null){
                callback.onDataLoaded(new ArrayList<>(workdaySet));
            }else{
                String year = Integer.toString(Calendar.getInstance().get(Calendar.YEAR));
                getSpecialWorkingDayFromRemote(year, new Callback() {
                    @Override
                    public void onDataLoaded(ArrayList<String> result) {
                        callback.onDataLoaded(result);
                    }

                    @Override
                    public void onDataNotAvailable(String result) {
                        callback.onDataNotAvailable(result);
                    }
                });
            }
        }else{
            String year = Integer.toString(Calendar.getInstance().get(Calendar.YEAR));
            getSpecialWorkingDayFromRemote(year, new Callback() {
                @Override
                public void onDataLoaded(ArrayList<String> result) {
                    callback.onDataLoaded(result);
                }

                @Override
                public void onDataNotAvailable(String result) {
                    callback.onDataNotAvailable(result);
                }
            });
        }
    }


    public ArrayList<String> getHoliday(){
        return null;
    }


}
