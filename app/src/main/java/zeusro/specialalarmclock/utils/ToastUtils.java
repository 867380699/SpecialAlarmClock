package zeusro.specialalarmclock.utils;

import android.content.Context;
import android.widget.Toast;

import zeusro.specialalarmclock.application.BaseApplication;

/**
 *
 *
 * @author lls
 * @since 2017/8/21 上午11:29
 */
public class ToastUtils {
    private static Toast mToast;

    public static void show(String text){
        Context context = BaseApplication.getInstance().getApplicationContext();
        if(mToast ==null){
            mToast = Toast.makeText(context,"",Toast.LENGTH_SHORT);
        }
        mToast.setText(text);
        mToast.setDuration(Toast.LENGTH_SHORT);
        mToast.show();
    }
}
