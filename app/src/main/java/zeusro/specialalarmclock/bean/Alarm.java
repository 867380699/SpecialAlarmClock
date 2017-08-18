package zeusro.specialalarmclock.bean;

import android.media.RingtoneManager;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * Created by Z on 2015/11/16.
 */
public class Alarm implements Serializable {

    private long id;
    private Boolean alarmActive = true;
    private Calendar alarmTime = Calendar.getInstance();
    private int[] days = {Calendar.SUNDAY, Calendar.MONDAY, Calendar.TUESDAY, Calendar.WEDNESDAY, Calendar.THURSDAY, Calendar.FRIDAY, Calendar.SATURDAY,};
    private String alarmTonePath = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM).toString();
    private Boolean vibrate = true;
    private String alarmName = "极简闹钟";

    public Alarm() {

    }


    public void addDay(int day) {
        boolean contains = false;
        int[] temp = getDays();
        for (int d : temp)
            if (d == day)
                contains = true;
        if (!contains) {
            int[] result = new int[temp.length + 1];
            for (int i = 0; i < temp.length; i++) {
                result[i] = temp[i];
            }
            result[temp.length] = day;
            setDays(result);
        }
    }

    public void removeDay(int day) {
        boolean contains = false;
        int[] temp = getDays();
        int[] result = new int[temp.length];
        int xiabiao = temp.length;
        for (int i = 0; i < temp.length; i++) {
            if (temp[i] == day) {
                contains = true;
                result = new int[temp.length - 1];
                xiabiao = i;
            }
        }
        if (contains) {
            for (int i = 0; i < xiabiao; i++) {
                result[i] = temp[i];
            }
            for (int i = xiabiao + 1; i < temp.length; i++) {
                result[i - 1] = temp[i];
            }
            setDays(result);
        }
    }

    /**
     * @return the alarmActive
     */
    public Boolean IsAlarmActive() {
        return alarmActive;
    }

    /**
     * 这一天是否重复
     *
     * @param dayOfWeek
     * @return
     */
    public boolean isRepeat(int dayOfWeek) {
        if (days == null || days.length < 1)
            return false;
        for (int i = 0; i < days.length; i++) {
            if (days[i] == dayOfWeek)
                return true;
        }
        return false;
    }

    /**
     * @param alarmActive the alarmActive to set
     */
    public void setAlarmActive(Boolean alarmActive) {
        this.alarmActive = alarmActive;
    }

    /**
     * @return the alarmTime
     */
    public Calendar getAlarmTime() {
//        if (alarmTime.before(Calendar.getInstance()))
//            alarmTime.add(Calendar.DAY_OF_MONTH, 1);
        return alarmTime;
    }

    /**
     * @return the alarmTime
     */
    public String getAlarmTimeString() {

        String time = "";
        if (alarmTime.get(Calendar.HOUR_OF_DAY) <= 9)
            time += "0";
        time += String.valueOf(alarmTime.get(Calendar.HOUR_OF_DAY));
        time += ":";

        if (alarmTime.get(Calendar.MINUTE) <= 9)
            time += "0";
        time += String.valueOf(alarmTime.get(Calendar.MINUTE));

        return time;
    }

    /**
     * @param alarmTime the alarmTime to set
     */
    public void setAlarmTime(Calendar alarmTime) {
        this.alarmTime = alarmTime;
    }

    /**
     * @param alarmTime the alarmTime to set
     */
    public void setAlarmTime(String alarmTime) {

        String[] timePieces = alarmTime.split(":");
        Calendar newAlarmTime = Calendar.getInstance();
        newAlarmTime.set(Calendar.HOUR_OF_DAY,
                Integer.parseInt(timePieces[0]));
        newAlarmTime.set(Calendar.MINUTE, Integer.parseInt(timePieces[1]));
        newAlarmTime.set(Calendar.SECOND, 0);
        setAlarmTime(newAlarmTime);
    }


    /**
     * @return the repeatDays
     */
    public int[] getDays() {
        return days;
    }

    /**
     * @param days the repeatDays to set
     */
    public void setDays(int[] days) {
        this.days = days;
    }

    /**
     * @return the alarmTonePath
     */
    public String getAlarmTonePath() {
        return alarmTonePath;
    }

    /**
     * @param alarmTonePath the alarmTonePath to set
     */
    public void setAlarmTonePath(String alarmTonePath) {
        this.alarmTonePath = alarmTonePath;
    }

    /**
     * @return the vibrate
     */
    public Boolean IsVibrate() {
        return vibrate;
    }

    /**
     * @param vibrate the vibrate to set
     */
    public void setVibrate(Boolean vibrate) {
        this.vibrate = vibrate;
    }

    /**
     * @return the alarmName
     */
    public String getAlarmName() {
        return alarmName;
    }

    /**
     * @param alarmName the alarmName to set
     */
    public void setAlarmName(String alarmName) {
        this.alarmName = alarmName;
    }


    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }


    public String getTimeUntilNextAlarmMessage() {
        long timeDifference = getAlarmTime().getTimeInMillis() - System.currentTimeMillis();
        long days = timeDifference / (1000 * 60 * 60 * 24);
        long hours = timeDifference / (1000 * 60 * 60) - (days * 24);
        long minutes = timeDifference / (1000 * 60) - (days * 24 * 60) - (hours * 60);
        long seconds = timeDifference / (1000) - (days * 24 * 60 * 60) - (hours * 60 * 60) - (minutes * 60);
        String alert = "闹钟将会在";
        if (days > 0) {
            alert += String.format("%d 天 %d 小时 %d 分钟 %d 秒", days, hours, minutes, seconds);
        } else {
            if (hours > 0) {
                alert += String.format("%d 小时, %d 分钟 %d 秒", hours, minutes, seconds);
            } else {
                if (minutes > 0) {
                    alert += String.format("%d 分钟 %d 秒", minutes, seconds);
                } else {
                    alert += String.format("%d 秒", seconds);
                }
            }
        }
        alert += "提醒";
        return alert;
    }


    public String getRepeatDaysString() {
        if (days == null || days.length < 1)
            return "只响一次";
        Map<Integer, String> map = new HashMap<>(7);
        map.put(Calendar.SUNDAY, "周日");
        map.put(Calendar.MONDAY, "周一");
        map.put(Calendar.TUESDAY, "周二");
        map.put(Calendar.WEDNESDAY, "周三");
        map.put(Calendar.THURSDAY, "周四");
        map.put(Calendar.FRIDAY, "周五");
        map.put(Calendar.SATURDAY, "周六");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < days.length; i++) {
            if (map.containsKey(days[i])) {
                sb.append(" " + map.get(days[i]));
            }
        }
        return sb.toString();
    }

    @Override
    public String toString() {
        SimpleDateFormat simpleDate =  new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return "Alarm{" +
                "id=" + id +
                ", alarmActive=" + alarmActive +
                ", alarmTime=" + simpleDate.format(new Date(alarmTime.getTimeInMillis()))  +
                ", days=" + Arrays.toString(days) +
                ", alarmTonePath='" + alarmTonePath + '\'' +
                ", vibrate=" + vibrate +
                ", alarmName='" + alarmName + '\'' +
                '}';
    }
}
