package zeusro.specialalarmclock.adapter;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.kyleduo.switchbutton.SwitchButton;

import java.util.ArrayList;
import java.util.List;

import zeusro.specialalarmclock.Database;
import zeusro.specialalarmclock.R;
import zeusro.specialalarmclock.activity.AlarmPreferencesActivity;
import zeusro.specialalarmclock.bean.Alarm;
import zeusro.specialalarmclock.utils.ToastUtils;

/**
 * @author lls
 * @since 2017/9/5 下午3:59
 */

public class AlarmAdapter extends RecyclerView.Adapter<AlarmAdapter.ViewHolder> {
    private Context mContent;
    private List<Alarm> alarms = new ArrayList<Alarm>();
    private OnItemClickListener onItemClickListener;

    public AlarmAdapter(@NonNull Context context) {
        this.mContent = context;
        Database.init(mContent);
        this.alarms = Database.getAll();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(mContent).inflate(R.layout.alarm_list,parent,false);
        ViewHolder holder = new ViewHolder(v);
        holder.switchButton = (SwitchButton) v.findViewById(R.id.alarm_list_switch);
        holder.alarmTimeView = (TextView) v.findViewById(R.id.alarm_list_time);
        holder.alarmRepeatView = (TextView) v.findViewById(R.id.alarm_list_repeat);
        return holder;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        Alarm alarm = alarms.get(position);
        holder.switchButton.setChecked(alarm.isActive());
        holder.switchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Alarm alarm = alarms.get(position);
                alarm.setActive(holder.switchButton.isChecked());
                Database.update(alarm);
                if (holder.switchButton.isChecked()) {
                    ToastUtils.show(alarm.getTimeUntilNextAlarmMessage());
                }
                if (onItemClickListener != null) {
                    onItemClickListener.onItemClick(alarms.get(position));
                }
            }
        });
        holder.alarmTimeView.setText(alarm.getAlarmTimeString());
        holder.alarmRepeatView.setText(Alarm.getRepeatTypeString(alarm.getRepeatType()));
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                v.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
                final Alarm alarm = alarms.get(position);
                AlertDialog.Builder dialog = new AlertDialog.Builder(mContent);
//                    dialog.setTitle("删除这个闹钟?");
                dialog.setMessage("删除这个闹钟?");
                dialog.setPositiveButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                dialog.setNegativeButton("删除", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Database.init(mContent);
                        Database.deleteEntry(alarm);
                        alarms.remove(alarm);
                        //TODO: 添加取消闹钟的功能
                        notifyDataSetChanged();
                    }
                });
                dialog.show();
                return true;
            }
        });
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Alarm alarm = alarms.get(position);
                Intent intent = new Intent(mContent, AlarmPreferencesActivity.class);
                intent.putExtra("alarm", alarm);
                ((Activity)mContent).startActivityForResult(intent, 0);
            }
        });
    }

    @Override
    public int getItemCount() {
        return alarms.size();
    }

    public int updateData(){
        Database.init(mContent);
        this.alarms = Database.getAll();
        notifyDataSetChanged();
        return this.alarms.size();
    }

    public AlarmAdapter setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
        return this;
    }

    public static interface OnItemClickListener {
        void onItemClick(Alarm alarm);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        SwitchButton switchButton;
        TextView alarmTimeView;
        TextView alarmRepeatView;

        ViewHolder(View itemView) {
            super(itemView);
        }
    }
}
