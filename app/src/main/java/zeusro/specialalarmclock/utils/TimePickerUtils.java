package zeusro.specialalarmclock.utils;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.TimePicker;

import java.lang.reflect.Field;

import zeusro.specialalarmclock.application.BaseApplication;

/**
 * Created by Administrator on 2017/8/22 0022.
 */

public class TimePickerUtils {
    public static void setTimerPickerStyle(TimePicker time_picker){
        Resources system = Resources.getSystem();
        int hourNumberPickerId = system.getIdentifier("hour", "id", "android");
        int minuteNumberPickerId = system.getIdentifier("minute", "id", "android");
        int amPmNumberPickerId = system.getIdentifier("amPm", "id", "android");

        NumberPicker hourNumberPicker = (NumberPicker) time_picker.findViewById(hourNumberPickerId);
        NumberPicker minuteNumberPicker = (NumberPicker) time_picker.findViewById(minuteNumberPickerId);
        NumberPicker amPmNumberPicker = (NumberPicker) time_picker.findViewById(amPmNumberPickerId);

        setNumberPickerStyle(hourNumberPicker);
        setNumberPickerStyle(minuteNumberPicker);
        setNumberPickerStyle(amPmNumberPicker);
    }

    public static void setNumberPickerStyle(NumberPicker numberPicker){
        final int count = numberPicker.getChildCount();
        //这里就是要设置的颜色，修改一下作为参数传入会更好
        int color= Color.argb(255,205,85,85);
//        final int color = BaseApplication.getContext().getResources().getColor(myColor);

        for(int i = 0; i < count; i++){
            View child = numberPicker.getChildAt(i);

            try{
                Field wheelpaintField = numberPicker.getClass().getDeclaredField("mSelectorWheelPaint");
                wheelpaintField.setAccessible(true);

                ((Paint)wheelpaintField.get(numberPicker)).setColor(color);
                ((EditText)child).setTextColor(color);
                numberPicker.invalidate();

                int widthPxValue = dp2px(BaseApplication.getContext(), 180);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(widthPxValue, LinearLayout.LayoutParams.WRAP_CONTENT);
                params.setMargins(0, 0, 0, 0);//这儿参数可根据需要进行更改
                numberPicker.setLayoutParams(params);
            }
            catch(NoSuchFieldException e){
                Log.w("setColor", e);
            }
            catch(IllegalAccessException e){
                Log.w("setColor", e);
            }
            catch(IllegalArgumentException e){
                Log.w("setColor", e);
            }
        }
    }

    public static int dp2px(Context context, float dpVal)
    {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                dpVal, context.getResources().getDisplayMetrics());
    }
}
