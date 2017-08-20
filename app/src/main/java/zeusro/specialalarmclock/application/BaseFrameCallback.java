package zeusro.specialalarmclock.application;

import android.util.Log;
import android.view.Choreographer;

/**
 * Created by Administrator on 2017/8/18 0018.
 */

public class BaseFrameCallback implements Choreographer.FrameCallback {
    public static BaseFrameCallback instance;
    private String TAG="BaseFrameCallback";
    public static final float deviceRefreshRateMs=16.6f;
    public static long lastFrameTimeNanos=0;
    public static long currentFrameTimeNanos=0;

    public void start() {
        Choreographer.getInstance().postFrameCallback(BaseFrameCallback.getInstance());
    }
    public static BaseFrameCallback getInstance(){
        if(instance==null){
            instance=new BaseFrameCallback();
        }
        return instance;
    }
    @Override
    public void doFrame(long frameTimeNanos) {
        if(lastFrameTimeNanos==0){
            lastFrameTimeNanos=frameTimeNanos;
            Choreographer.getInstance().postFrameCallback(this);
            return;
        }
        currentFrameTimeNanos=frameTimeNanos;
        float value=(currentFrameTimeNanos-lastFrameTimeNanos)/1000000.0f;

        final int skipFrameCount = skipFrameCount(lastFrameTimeNanos, currentFrameTimeNanos, deviceRefreshRateMs);
        Log.e(TAG,"两次绘制时间间隔value="+value+"  frameTimeNanos="+frameTimeNanos+"  currentFrameTimeNanos="+currentFrameTimeNanos+"  skipFrameCount="+skipFrameCount+"");
        lastFrameTimeNanos=currentFrameTimeNanos;

    }

    private  int skipFrameCount(long start,long end,float devicefreshRate){
        int count =0;
        long diffNs=end-start;
        long diffMs = Math.round(diffNs / 1000000.0f);
        long dev=Math.round(devicefreshRate);
        if(diffMs>dev){
            long skipCount=diffMs/dev;
            count=(int)skipCount;
        }
        return  count;
    }

}
