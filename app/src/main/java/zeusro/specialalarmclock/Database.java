package zeusro.specialalarmclock;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.NonNull;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StreamCorruptedException;
import java.util.ArrayList;
import java.util.List;

import zeusro.specialalarmclock.bean.Alarm;

/**
 * 数据库
 *
 * version2时添加了 repeat_type
 * Created by Z on 2015/11/16.
 */
public class Database extends SQLiteOpenHelper {
    private static Database instance = null;
    private static SQLiteDatabase database = null;

    private static final String DATABASE_NAME = "DB";
    private static final int DATABASE_VERSION = 3;

    public static final String ALARM_TABLE = "alarm";
    public static final String COLUMN_ALARM_ID = "_id";
    public static final String COLUMN_ALARM_ACTIVE = "alarm_active";
    public static final String COLUMN_ALARM_TIME = "alarm_time";
    public static final String COLUMN_ALARM_DAYS = "alarm_days";
    public static final String COLUMN_ALARM_TONE_NAME = "alarm_tone_name";
    public static final String COLUMN_ALARM_VIBRATE = "alarm_vibrate";
    public static final String COLUMN_ALARM_NAME = "alarm_name";
    public static final String COLUMN_ALARM_REPEAT_TYPE = "alarm_repeat_type";
    public static final String COLUMN_ALARM_TONE_PATH = "alarm_tone_path";

    private Database(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS " + ALARM_TABLE + " ( "
                + COLUMN_ALARM_ID + " INTEGER primary key autoincrement, "
                + COLUMN_ALARM_ACTIVE + " INTEGER NOT NULL, "
                + COLUMN_ALARM_TIME + " TEXT NOT NULL, "
                + COLUMN_ALARM_DAYS + " BLOB NOT NULL, "
                + COLUMN_ALARM_TONE_NAME + " TEXT NOT NULL, "
                + COLUMN_ALARM_TONE_PATH + " TEXT NOT NULL, "
                + COLUMN_ALARM_VIBRATE + " INTEGER NOT NULL, "
                + COLUMN_ALARM_NAME + " TEXT NOT NULL, "
                + COLUMN_ALARM_REPEAT_TYPE + " INTEGER DEFAULT 0)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if(oldVersion<2){
            String sql = String.format("ALTER TABLE %s ADD COLUMN %s INTEGER DEFAULT 0",ALARM_TABLE,COLUMN_ALARM_REPEAT_TYPE);
            db.execSQL(sql);
        }if(oldVersion<3){
                String sql = String.format("ALTER TABLE %s ADD COLUMN %s %s DEFAULT 0",ALARM_TABLE,COLUMN_ALARM_TONE_NAME,"TEXT");
            db.execSQL(sql);
        }
    }

    public static void init(Context context) {
        if (null == instance) {
            instance = new Database(context);
        }
    }

    public static SQLiteDatabase getDatabase() {
        if (null == database) {
            database = instance.getWritableDatabase();
        }
        return database;
    }

    public static void deactivate() {
        if (null != database && database.isOpen()) {
            database.close();
        }
        database = null;
        instance = null;
    }

    /**
     * 插入一条数据
     * @param alarm alarm
     * @return 新插入数据的pk
     */
    public static long create(Alarm alarm) {
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_ALARM_ACTIVE, alarm.isActive());
        cv.put(COLUMN_ALARM_TIME, alarm.getAlarmTimeString());
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(bos);
            oos.writeObject(alarm.getDays());
            byte[] buff = bos.toByteArray();
            cv.put(COLUMN_ALARM_DAYS, buff);
        } catch (Exception e) {
            e.printStackTrace();
        }
        cv.put(COLUMN_ALARM_TONE_NAME, alarm.getAlarmToneName());
        cv.put(COLUMN_ALARM_TONE_PATH, alarm.getAlarmTonePath());
        cv.put(COLUMN_ALARM_VIBRATE, alarm.IsVibrate());
        cv.put(COLUMN_ALARM_NAME, alarm.getAlarmName());
        cv.put(COLUMN_ALARM_REPEAT_TYPE, alarm.getRepeatType());
        return getDatabase().insert(ALARM_TABLE, null, cv);
    }

    public static int update(Alarm alarm) {
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_ALARM_ACTIVE, alarm.isActive());
        cv.put(COLUMN_ALARM_TIME, alarm.getAlarmTimeString());
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream oos = null;
            oos = new ObjectOutputStream(bos);
            oos.writeObject(alarm.getDays());
            byte[] buff = bos.toByteArray();
            cv.put(COLUMN_ALARM_DAYS, buff);
        } catch (Exception e) {
            e.printStackTrace();
        }
        cv.put(COLUMN_ALARM_TONE_NAME, alarm.getAlarmToneName());
        cv.put(COLUMN_ALARM_TONE_PATH, alarm.getAlarmTonePath());
        cv.put(COLUMN_ALARM_VIBRATE, alarm.IsVibrate());
        cv.put(COLUMN_ALARM_NAME, alarm.getAlarmName());
        cv.put(COLUMN_ALARM_REPEAT_TYPE, alarm.getRepeatType());
        return getDatabase().update(ALARM_TABLE, cv, "_id=" + alarm.getId(), null);
    }

    public static long deleteEntry(Alarm alarm) {
        return deleteEntry(alarm.getId());
    }

    public static long deleteEntry(long id) {
        return getDatabase().delete(ALARM_TABLE, COLUMN_ALARM_ID + "=" + id, null);
    }

    public static int deleteAll() {
        return getDatabase().delete(ALARM_TABLE, "1", null);
    }

    public static Alarm getAlarm(long id) {
        String[] columns = new String[]{
                COLUMN_ALARM_ID,
                COLUMN_ALARM_ACTIVE,
                COLUMN_ALARM_TIME,
                COLUMN_ALARM_DAYS,
                COLUMN_ALARM_TONE_NAME,
                COLUMN_ALARM_TONE_PATH,
                COLUMN_ALARM_VIBRATE,
                COLUMN_ALARM_NAME,
                COLUMN_ALARM_REPEAT_TYPE
        };
        Cursor c = getDatabase().query(ALARM_TABLE, columns, COLUMN_ALARM_ID + "=" + id, null, null, null,
                null);
        Alarm alarm = null;

        if (c.moveToFirst()) {
            alarm = readOneRow(c);
        }
        c.close();
        return alarm;
    }

    @NonNull
    private static Alarm readOneRow(Cursor c) {
        Alarm alarm = new Alarm();
        alarm.setId(c.getInt(c.getColumnIndexOrThrow(COLUMN_ALARM_ID)));
        alarm.setActive(c.getInt(c.getColumnIndexOrThrow(COLUMN_ALARM_ACTIVE)) == 1);
        alarm.setAlarmTime(c.getString(c.getColumnIndexOrThrow(COLUMN_ALARM_TIME)));
        byte[] repeatDaysBytes = c.getBlob(c.getColumnIndexOrThrow(COLUMN_ALARM_DAYS));

        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(repeatDaysBytes);
        try {
            ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream);
            int[] repeatDays;
            Object object = objectInputStream.readObject();
            if (object instanceof int[]) {
                repeatDays = (int[]) object;
                alarm.setDays(repeatDays);
            }
        } catch (StreamCorruptedException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        alarm.setAlarmToneName(c.getString(c.getColumnIndexOrThrow(COLUMN_ALARM_TONE_NAME)));
        alarm.setAlarmTonePath(c.getString(c.getColumnIndexOrThrow(COLUMN_ALARM_TONE_PATH)));
        alarm.setVibrate(c.getInt(c.getColumnIndexOrThrow(COLUMN_ALARM_VIBRATE)) == 1);
        alarm.setAlarmName(c.getString(c.getColumnIndexOrThrow(COLUMN_ALARM_NAME)));
        alarm.setRepeatType(c.getInt(c.getColumnIndexOrThrow(COLUMN_ALARM_REPEAT_TYPE)));
        return alarm;
    }

    public static Cursor getCursor() {
        String[] columns = new String[]{
                COLUMN_ALARM_ID,
                COLUMN_ALARM_ACTIVE,
                COLUMN_ALARM_TIME,
                COLUMN_ALARM_DAYS,
                COLUMN_ALARM_TONE_NAME,
                COLUMN_ALARM_TONE_PATH,
                COLUMN_ALARM_VIBRATE,
                COLUMN_ALARM_NAME,
                COLUMN_ALARM_REPEAT_TYPE
        };
        return getDatabase().query(ALARM_TABLE, columns, null, null, null, null,
                null);
    }

    public static List<Alarm> getAll() {
        List<Alarm> alarms = new ArrayList<Alarm>();
        Cursor cursor = Database.getCursor();
        if (cursor.moveToFirst()) {
            do {
                Alarm alarm = readOneRow(cursor);
                alarms.add(alarm);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return alarms;
    }
}
