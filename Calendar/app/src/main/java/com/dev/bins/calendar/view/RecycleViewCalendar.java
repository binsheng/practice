package com.dev.bins.calendar.view;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.IntDef;
import android.support.annotation.Nullable;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.dev.bins.calendar.R;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Created by bin on 10/10/2017.
 */

public class RecycleViewCalendar extends LinearLayout {


    public static final int STATE_OPEN = 1;
    public static final int STATE_COLLAPSE = 2;
    @STATE
    int mCurrentState = STATE_OPEN;
    private Calendar mCalendar;
    private int mCurrentSelectionPosition = -1;
    private RecyclerView mRecyclerView;
    private GestureDetectorCompat mGestureDetectorCompat;
    private Adapter mAdapter;
    private GridLayoutManager mGridLayoutManager;

    public RecycleViewCalendar(Context context) {
        this(context, null);
    }

    public RecycleViewCalendar(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RecycleViewCalendar(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        LayoutInflater.from(context).inflate(R.layout.recyclerview_calendar, this);
        mCalendar = Calendar.getInstance();
        mRecyclerView = findViewById(R.id.recyclerview);
        mGridLayoutManager = new GridLayoutManager(context, 7);
        mRecyclerView.setLayoutManager(mGridLayoutManager);
        mAdapter = new Adapter();
        mRecyclerView.setAdapter(mAdapter);
        mGestureDetectorCompat = new GestureDetectorCompat(context, new GestureDetector.SimpleOnGestureListener() {

            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {

                if (e2.getX() - e1.getX() > 10) {//右滑
                    if (mCurrentState == STATE_COLLAPSE) {
                        if (getTop() >= 0) {
                            mCalendar.add(Calendar.DAY_OF_MONTH, -7);
                            mAdapter.nextMonth();
                            mAdapter.notifyDataSetChanged();
                            //setTop(getMeasuredHeight()-getMinTop());
                            System.out.println(getTop()+":"+getMeasuredHeight());
                            offsetTopAndBottom(-getMeasuredHeight());
                            System.out.println(getTop()+":"+getMeasuredHeight());
                        } else {
                            mCalendar.add(Calendar.DAY_OF_MONTH, -7);
                            offsetTopAndBottom(getMinTop());
                        }
                    } else {
                        mCalendar.add(Calendar.MONTH, -1);
                        mAdapter.nextMonth();
                        mAdapter.notifyDataSetChanged();
                    }
                } else {//左滑
                    if (mCurrentState == STATE_COLLAPSE) {
                        if (-getTop() >= getMeasuredHeight() - getMinTop()) {
                            mCalendar.add(Calendar.MONTH, 1);
                            mAdapter.nextMonth();
                            mAdapter.notifyDataSetChanged();
                            offsetTopAndBottom(-getTop());
                        } else {
                            mCalendar.add(Calendar.DAY_OF_MONTH, 7);
                            offsetTopAndBottom(-getMinTop());
                        }
                    } else {
                        mCalendar.add(Calendar.MONTH, 1);
                        mAdapter.nextMonth();
                        mAdapter.notifyDataSetChanged();
                    }
                }
                mCurrentSelectionPosition = 0;

                return super.onFling(e1, e2, velocityX, velocityY);
            }

        });
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return true;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mGestureDetectorCompat.onTouchEvent(event);
        return true;
    }

    public int getState() {
        return mCurrentState;
    }

    public void onScroll(int dy) {
        View selctView = mRecyclerView.getChildAt(mCurrentSelectionPosition);
        int top = selctView.getTop();

        if (-getTop() < top) {
            offsetTopAndBottom(-dy);
        } else {
            offsetTopAndBottom(-top - getTop());
        }

    }

    public void collapse() {
        View selctView = mRecyclerView.getChildAt(mCurrentSelectionPosition);
        int top = selctView.getTop();
        offsetTopAndBottom(-top - getTop());
        mCurrentState = STATE_COLLAPSE;
    }

    public void expand() {
        mRecyclerView.setLayoutManager(mGridLayoutManager);
        int top = getTop();
        offsetTopAndBottom(-top);
        mCurrentState = STATE_OPEN;
    }

    public void onStateChange(boolean isOpen) {
        if (isOpen) {
            expand();
        } else {
            collapse();
        }
    }

    //当前选中 view 距离顶部的距离
    public int getSelectViewTop() {
        View selctView = mRecyclerView.getChildAt(mCurrentSelectionPosition);
        int top = selctView.getTop();
        return top;
    }


    public int getMinTop() {
        View view = mRecyclerView.getChildAt(0);
        return view.getMeasuredHeight();
    }

    public int getCurrentPosition() {
        return mCurrentSelectionPosition;
    }

    private boolean isToday(Calendar calendar, Calendar today) {
        return today.get(Calendar.MONTH) == calendar.get(Calendar.MONTH) && today.get(Calendar.YEAR) == calendar.get(Calendar.YEAR) && today.get(Calendar.DAY_OF_MONTH) == calendar.get(Calendar.DAY_OF_MONTH);
    }

    @IntDef({STATE_OPEN, STATE_COLLAPSE})
    @Retention(RetentionPolicy.SOURCE)
    public @interface STATE {
    }

    class Adapter extends RecyclerView.Adapter<Holder> {

        private List<Date> dates = new ArrayList<>();

        public Adapter() {
            nextMonth();
        }

        @Override
        public Holder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.calendar_text_day, parent, false);
            return new Holder(view);
        }

        @Override
        public void onBindViewHolder(Holder holder, int position) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(dates.get(position));
            Calendar current = mCalendar;
            holder.textView.setText(String.valueOf(calendar.get(Calendar.DAY_OF_MONTH)));

            if (current.get(Calendar.MONTH) == calendar.get(Calendar.MONTH)) {
                holder.textView.setTextColor(Color.BLACK);
            } else {
                holder.textView.setTextColor(Color.GRAY);
            }
            Calendar today = Calendar.getInstance();

            if (isToday(calendar, today)) {
                holder.textView.setTextColor(Color.RED);
                mCurrentSelectionPosition = position;
            }
        }

        @Override
        public int getItemCount() {
            return dates.size();
        }


        private void nextMonth() {
            dates.clear();
            Calendar temp = (Calendar) mCalendar.clone();
            temp.set(Calendar.DAY_OF_MONTH, 1);
            int preDays = temp.get(Calendar.DAY_OF_WEEK) - 1;
            int dayInMonth = temp.getActualMaximum(Calendar.DATE);
            int days = preDays + dayInMonth;
            if (days % 7 != 0) {
                int count = days / 7;
                days = (count + 1) * 7;
            }
            temp.add(Calendar.DAY_OF_MONTH, -preDays);
            for (int i = 0; i < days; i++) {
                dates.add(temp.getTime());
                temp.add(Calendar.DAY_OF_MONTH, 1);
            }
        }

    }

    class Holder extends RecyclerView.ViewHolder {

        TextView textView;

        public Holder(View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.tv_calendar_day);
        }
    }


}
