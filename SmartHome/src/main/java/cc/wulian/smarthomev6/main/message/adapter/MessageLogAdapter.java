package cc.wulian.smarthomev6.main.message.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import cc.wulian.smarthomev6.R;
import cc.wulian.smarthomev6.entity.MessageNewInfo;
import cc.wulian.smarthomev6.support.core.apiunit.bean.MessageBean;
import cc.wulian.smarthomev6.support.utils.WLog;

/**
 * Created by Veev on 2017/7/7
 * Tel:         18365264930
 * QQ:          2355738466
 * Email:       wei.wang@wuliangroup.com
 * Function:    日志 adapter
 */

public class MessageLogAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public static final int TYPE_FIRST = 0;
    public static final int TYPE_NORMAL = 1;
    public static final int TYPE_NORMAL_ICON = 2;
    private List<MessageNewInfo> mData;

    public MessageLogAdapter() {
        mData = new ArrayList<>();
    }

    public void addNew(List<MessageBean.RecordListBean> data) {
        if (data.isEmpty()) {
            return;
        }
        if (mData.isEmpty()) {
            setData(data);
            return;
        }

        List<MessageNewInfo> list = new ArrayList<>();
        for (MessageBean.RecordListBean bean : data) {
            list.add(MessageNewInfo.getMessageBean(bean));
            WLog.i("MessageLogActivity", "Log: " + MessageNewInfo.getMessageBean(bean).getMsg());
        }

        list.get(0).setDayFirst(true);
        list.add(0, MessageNewInfo.getDateBean(list.get(0).getRecordListBean()));
        // 如果最新一条消息与 第一条消息是同一天
        if (list.get(list.size() - 1).isSameDay(mData.get(0))) {
            mData.remove(0);
            notifyItemRemoved(0);
            mData.get(0).setDayFirst(false);
            notifyItemChanged(0);
        } else {
            list.get(list.size() - 1).setDayLast(true);
        }
        for (int i = 1, count = list.size(); i < count; i++) {
            if (list.get(i).isDate()) {
                continue;
            }
            if (!list.get(i).isSameDay(list.get(i - 1))) {
                list.get(i).setDayFirst(true);
                list.add(i, MessageNewInfo.getDateBean(list.get(i).getRecordListBean()));
                count++;
                i++;
            }

            if (i < count - 1 && !list.get(i).isSameDay(list.get(i + 1))) {
                list.get(i).setDayLast(true);
            }
        }

        mData.addAll(0, list);
        WLog.i("MessageLogActivity", "addNew: " + list.size());
        notifyItemRangeInserted(0, list.size());
    }

    public void addOld(List<MessageBean.RecordListBean> data) {
        if (data.isEmpty()) {
            return;
        }
        List<MessageNewInfo> list = new ArrayList<>();
//        sort(data);
        for (MessageBean.RecordListBean bean : data) {
            list.add(MessageNewInfo.getMessageBean(bean));
            WLog.i("MessageLogActivity", "Log: " + MessageNewInfo.getMessageBean(bean).getFullDate());
        }

        int lastIndex = mData.size() - 1;
        if (!mData.isEmpty()) {
            if (mData.get(lastIndex).isSameDay(list.get(0))) {
                mData.get(lastIndex).setDayLast(false);
                notifyItemChanged(lastIndex);
            }
        }

        if (!list.get(0).isSameDay(mData.get(lastIndex))) {
            list.get(0).setDayFirst(true);
            list.add(0, MessageNewInfo.getDateBean(list.get(0).getRecordListBean()));
        }
        list.get(list.size() - 1).setDayLast(true);
        for (int i = 1, count = list.size() - 1; i < count; i++) {
            if (list.get(i).isDate()) {
                continue;
            }
            if (!list.get(i).isSameDay(list.get(i - 1))) {
                list.get(i).setDayFirst(true);
                list.add(i, MessageNewInfo.getDateBean(list.get(i).getRecordListBean()));
                count++;
                i++;
            }

            if (i < count - 1 && !list.get(i).isSameDay(list.get(i + 1))) {
                list.get(i).setDayLast(true);
            }
        }

        mData.addAll(list);
        notifyItemRangeInserted(lastIndex + 1, list.size());
    }

    public void setData(List<MessageBean.RecordListBean> data) {
        if (data.isEmpty()) {
            return;
        }
        List<MessageNewInfo> list = new ArrayList<>();
//        sort(data);
        for (MessageBean.RecordListBean bean : data) {
            list.add(MessageNewInfo.getMessageBean(bean));
        }

        list.get(0).setDayFirst(true);
        list.add(0, MessageNewInfo.getDateBean(list.get(0).getRecordListBean()));
        list.get(list.size() - 1).setDayLast(true);
        for (int i = 1, count = list.size(); i < count; i++) {
            if (list.get(i).isDate()) {
                continue;
            }
            if (!list.get(i).isSameDay(list.get(i - 1))) {
                list.get(i).setDayFirst(true);
                list.add(i, MessageNewInfo.getDateBean(list.get(i).getRecordListBean()));
                count++;
                i++;
            }

            if (i < count - 1 && !list.get(i).isSameDay(list.get(i + 1))) {
                list.get(i).setDayLast(true);
            }
        }

        mData = list;
        for (MessageNewInfo b : list) {
            WLog.i("MessageLogActivity", "Log: " + b.getFullDate());
        }
        notifyDataSetChanged();
    }

    public long getFirstTime() {
        if (isEmpty()) {
            return 0;
        }

        return mData.get(0).getRecordListBean().time;
    }

    public void clear() {
        if (mData != null) {
            mData.clear();
        }
    }

    public int lastPosition() {
        return mData.size() - 1;
    }

    public boolean isEmpty() {
        return mData.isEmpty();
    }

    public void sort(List<MessageBean.RecordListBean> data) {
        if (data == null) {
            return;
        }
        Collections.sort(data, new Comparator<MessageBean.RecordListBean>() {
            @Override
            public int compare(MessageBean.RecordListBean o1, MessageBean.RecordListBean o2) {
                if (o1.time < o2.time) {
                    return 1;
                } else if (o1.time > o2.time) {
                    return -1;
                }
                return 0;
            }
        });
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());

        if (viewType == TYPE_FIRST) {
            View itemView = layoutInflater.inflate(R.layout.item_message_date, parent, false);
            return new MessageDateHolder(itemView);
        }

        if (viewType == TYPE_NORMAL_ICON) {
            View itemView = layoutInflater.inflate(R.layout.item_message_alarm_icon, parent, false);
            return new MessageLogIconHolder(itemView);
        }

        View itemView = layoutInflater.inflate(R.layout.item_message_alarm, parent, false);
        return new MessageLogHolder(itemView);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
//        holder.itemView.setTag(getItemViewType(position));
//        holder.itemView.setContentDescription(mData.get(position).getFullDate());

        if (getItemViewType(position) == TYPE_NORMAL) {
            ((MessageLogHolder) holder).mTop.setVisibility(mData.get(position).isDayFirst() ? View.INVISIBLE : View.VISIBLE);
            ((MessageLogHolder) holder).mBottom.setVisibility(mData.get(position).isDayLast() ? View.INVISIBLE : View.VISIBLE);
            ((MessageLogHolder) holder).mTextTime.setText(mData.get(position).getDate());
            ((MessageLogHolder) holder).mTextMsg.setText(mData.get(position).getMsg());
        }

        if (getItemViewType(position) == TYPE_NORMAL_ICON) {
            ((MessageLogIconHolder) holder).mTop.setVisibility(mData.get(position).isDayFirst() ? View.INVISIBLE : View.VISIBLE);
            ((MessageLogIconHolder) holder).mBottom.setVisibility(mData.get(position).isDayLast() ? View.INVISIBLE : View.VISIBLE);
            ((MessageLogIconHolder) holder).mTextTime.setText(mData.get(position).getDate());
            ((MessageLogIconHolder) holder).mTextMsg.setText(mData.get(position).getMsg());
            try {
                ((MessageLogIconHolder) holder).mImageIcon.setBackgroundResource(getIconByAlarmCode(mData.get(position).getRecordListBean().messageCode));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (getItemViewType(position) == TYPE_FIRST) {
            ((MessageDateHolder) holder).mTextDate.setText(mData.get(position).getFullDate());
        }
    }

    private int getIconByAlarmCode(String alarmCode) {
        switch (alarmCode) {
            case "0103102":
            case "0103142":
                return R.drawable.icon_alarm_fingerprint;
            case "0103092":
            case "0103132":
                return R.drawable.icon_alarm_pwd;
            case "0103082":
            case "0103122":
                return R.drawable.icon_alarm_card;
            case "0103072":
            case "0103112":
                return R.drawable.icon_alarm_app;
            case "0103032":
                return R.drawable.icon_alarm_back_lock;
            case "0103052":
                return R.drawable.icon_alarm_insurance;
            case "0103152":
                return R.drawable.icon_alarm_door_close;
        }
        return 0;
    }

    @Override
    public int getItemCount() {
        return mData == null ? 0 : mData.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (mData.get(position).isDate()) {
            return TYPE_FIRST;
        }

        switch (mData.get(position).getRecordListBean().messageCode) {
            case "0103102":
            case "0103142":
            case "0103092":
            case "0103132":
            case "0103082":
            case "0103122":
            case "0103072":
            case "0103112":
            case "0103032":
            case "0103052":
            case "0103152":
                return TYPE_NORMAL_ICON;
        }

        return TYPE_NORMAL;
    }

    private class MessageLogHolder extends RecyclerView.ViewHolder {

        private View mTop, mBottom;
        private TextView mTextTime, mTextMsg;

        public MessageLogHolder(View itemView) {
            super(itemView);

            mTop = itemView.findViewById(R.id.item_alarm_detail_line_top);
            mBottom = itemView.findViewById(R.id.item_alarm_detail_line_bottom);
            mTextMsg = (TextView) itemView.findViewById(R.id.item_alarm_detail_text_msg);
            mTextTime = (TextView) itemView.findViewById(R.id.item_alarm_detail_text_date);
        }
    }

    private class MessageLogIconHolder extends RecyclerView.ViewHolder {

        private View mTop, mBottom;
        private TextView mTextTime, mTextMsg;
        private ImageView mImageIcon;

        public MessageLogIconHolder(View itemView) {
            super(itemView);

            mTop = itemView.findViewById(R.id.item_alarm_detail_line_top);
            mBottom = itemView.findViewById(R.id.item_alarm_detail_line_bottom);
            mTextMsg = (TextView) itemView.findViewById(R.id.item_alarm_detail_text_msg);
            mTextTime = (TextView) itemView.findViewById(R.id.item_alarm_detail_text_date);
            mImageIcon = (ImageView) itemView.findViewById(R.id.item_alarm_detail_image_icon);
        }
    }

    private class MessageDateHolder extends RecyclerView.ViewHolder {

        private TextView mTextDate;

        public MessageDateHolder(View itemView) {
            super(itemView);

            mTextDate = (TextView) itemView.findViewById(R.id.item_message_text_date);
        }
    }
}
