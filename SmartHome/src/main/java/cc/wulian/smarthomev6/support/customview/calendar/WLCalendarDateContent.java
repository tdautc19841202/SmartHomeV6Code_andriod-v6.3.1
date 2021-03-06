package cc.wulian.smarthomev6.support.customview.calendar;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.util.Calendar;

import cc.wulian.smarthomev6.R;
import cc.wulian.smarthomev6.support.customview.WLDatePicker;

/**
 * Created by Veev on 2017/5/5
 * Tel: 18365264930
 * QQ: 2355738466
 * Function:
 */

public class WLCalendarDateContent extends View {

    private Context mContext;

    private static final float TEXT_DEFAULT_SIZE = 16;
    private static final float LINE_DEFAULT_INTERVAL = 16;

    private static String weeks[];

    // color
    private int mPrimaryTextColor;        // 主要文字的颜色
    private int mPrimaryColor;            // 主要颜色
    private float mTextSize = TEXT_DEFAULT_SIZE;                          // 文字大小
    private float mLineInterval = LINE_DEFAULT_INTERVAL;

    // 显示的年月日， 当前的年月日
    private int showYear, showMonth, showDay, currentYear, currentMonth, currentDay;

    // 触点
    private PointF touchPoint = new PointF(0f, 0f);

    // 绘画的区域
    private Rect mAreaRect = new Rect();

    // 画笔
    private Paint mTextPaint, mPickDayPaint;

    public interface DatePickListener {
        void datePicked(int year, int month, int day);
    }

    private WLDatePicker.DatePickListener mListener;

    public void setDatePickListener(WLDatePicker.DatePickListener listener) {
        mListener = listener;
    }

    public WLCalendarDateContent(Context context) {
        super(context);

        init(context);
    }

    public WLCalendarDateContent(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        init(context);
    }

    public WLCalendarDateContent(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        init(context);
    }

    /**
     * 初始化
     */
    private void init(Context context) {
        mContext = context;

        weeks = new String[]{
                context.getResources().getString(R.string.MessageCenter_Calendar_Sun),
                context.getResources().getString(R.string.MessageCenter_Calendar_Mon),
                context.getResources().getString(R.string.MessageCenter_Calendar_Tue),
                context.getResources().getString(R.string.MessageCenter_Calendar_Wen),
                context.getResources().getString(R.string.MessageCenter_Calendar_Thu),
                context.getResources().getString(R.string.MessageCenter_Calendar_Fri),
                context.getResources().getString(R.string.MessageCenter_Calendar_Sta),
        };
        mTextSize = sp2px(context, mTextSize);
        mLineInterval = dp2px(context, mLineInterval);
        mPrimaryTextColor = context.getResources().getColor(R.color.v6_text_dark);
        mPrimaryColor = context.getResources().getColor(R.color.v6_text_green_light);

        Calendar mCalendar = Calendar.getInstance();
        showYear = mCalendar.get(Calendar.YEAR);
        showMonth = mCalendar.get(Calendar.MONTH);
        showDay = mCalendar.get(Calendar.DAY_OF_MONTH);
        currentYear = mCalendar.get(Calendar.YEAR);
        currentMonth = mCalendar.get(Calendar.MONTH);
        currentDay = mCalendar.get(Calendar.DAY_OF_MONTH);

        mTextPaint = new Paint();
        mTextPaint.setAntiAlias(true);
        mTextPaint.setColor(mPrimaryTextColor);
        mTextPaint.setTextSize(mTextSize);

        mPickDayPaint = new Paint();
        mPickDayPaint.setAntiAlias(true);
        mPickDayPaint.setColor(mPrimaryColor);
        mPickDayPaint.setStrokeWidth(4);
        mPickDayPaint.setStyle(Paint.Style.STROKE);
    }

    /**
     * 根据手机的分辨率从 px(像素) 的单位 转成为 dp
     */
    private float dp2px(Context context, float pxValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return pxValue * scale;
    }

    /**
     * 将sp值转换为px值，保证文字大小不变
     */
    public static float sp2px(Context context, float spValue) {
        final float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
        return spValue * fontScale;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        adjustSize(widthMeasureSpec, heightMeasureSpec);
    }

    /**
     * 调整大小
     * 处理wrap_content情况
     */
    private void adjustSize(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        int width = widthSize;
        int height = heightSize;

        if (heightMode == MeasureSpec.AT_MOST) {
            // 高度为 文字高度 * 最大文字行数 * 2(一行文字 + 一行空行) + 上下边距
            height = (int) ((mTextSize + mLineInterval) * 6f + 60 + 40);
        }

        setMeasuredDimension(width, height);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        // 计算绘画区域
        measureArea();

        // 绘制天
        drawDay(canvas);
    }

    // 上一个事件
    private int lastEvent = -1;
    // 上一次点击的位置
    private PointF lastPoint = new PointF(-1, -1);

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        touchPoint.set(event.getX(), event.getY());

//        WLog.i("WL --->", "x: " + touchX + ", y: " + touchY);
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (isTouchValid()) {
                    lastEvent = getClickEvent();
                }

                lastPoint.set(touchPoint);
                break;
            case MotionEvent.ACTION_MOVE:
                break;
            case MotionEvent.ACTION_UP:
                if (!isTouchValid()) {
                    break;
                }

                dealClickEvent();
                break;
        }
        return true;
    }

    /**
     * 处理点击事件
     */
    private void dealClickEvent() {
        int touchEvent = getClickEvent();

        if (touchEvent != lastEvent) {
            return;
        }

        if (touchEvent < 0) {
//            WLog.i("WL --->", "无效区域");
        } else {
            // 点击日期
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.YEAR, showYear);
            calendar.set(Calendar.MONTH, showMonth);
            calendar.set(Calendar.DAY_OF_MONTH, touchEvent);
            setShowDay(calendar.get(Calendar.DAY_OF_MONTH));
            invalidate();
            if (mListener != null) {
                mListener.datePicked(showYear, showMonth, calendar.get(Calendar.DAY_OF_MONTH));
            }
        }
    }

    /**
     * 绘制日期
     */
    private void drawDay(Canvas canvas) {
        float unitWidth = mAreaRect.width() / 7;
        mTextPaint.setColor(mPrimaryTextColor);

        int startY = (int) (mAreaRect.top + mLineInterval);
        int startX = mAreaRect.left;
        int dayCount = getDayCount();

        for (int i = 1; i <= dayCount; i++) {
            String dayString = (i < 10 ? " " : "") + i;
            Point p = getPointByDay(i);
            float textWidth = mTextPaint.measureText(dayString);
            float x = startX + (unitWidth + (unitWidth - textWidth) / 7) * p.x;
            float y = startY + p.y * (mTextSize + mLineInterval);
            // 注意 “星期几” 和 “上个月”“下个月”的对齐方式
            // 注意此处的逻辑，有点绕
            if (isShowDay(i) || isToday(i)) {
                // Y的偏移量计算很麻烦 ↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓
                Rect r = new Rect();
                mTextPaint.getTextBounds(dayString, 0, dayString.length(), r);
                int height = r.bottom - r.top;
                Paint.FontMetrics fm = mTextPaint.getFontMetrics();
                float textHeight = fm.bottom - fm.top;
                float offsetY = height / 2 - (fm.bottom - (textHeight - height) / 2);
                // Y的偏移量计算很麻烦 ↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑
                if (!isToday(i)) {
                    mTextPaint.setColor(mPrimaryColor);
                    canvas.drawCircle(x + textWidth / 2f,
                            y - offsetY,
                            mTextSize,
                            mTextPaint);
                    mTextPaint.setColor(0xffffffff);
                    canvas.drawText(dayString, x, y, mTextPaint);
                    mTextPaint.setColor(mPrimaryTextColor);
                } else {
                    mTextPaint.setColor(mPrimaryColor);
                    canvas.drawCircle(x + textWidth / 2f,
                            y - offsetY,
                            mTextSize,
                            mPickDayPaint);
                    canvas.drawText(dayString, x, y, mTextPaint);
                    mTextPaint.setColor(mPrimaryTextColor);
                }
            } else {
                canvas.drawText(dayString, x, y, mTextPaint);
            }
        }
    }

    /**
     * 根据日期获取这一天的坐标
     * x = 0 为星期天, x = 6 为星期六
     * y = 0 为第一周
     */
    private Point getPointByDay(int day) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, showYear);
        calendar.set(Calendar.MONTH, showMonth);
        calendar.set(Calendar.DAY_OF_MONTH, day);
        // 星期几 - 横坐标
        int dayWeek = calendar.get(Calendar.DAY_OF_WEEK) - 1;
        // 第几个星期 - 纵坐标
        int dayWeekMonth = calendar.get(Calendar.WEEK_OF_MONTH) - 1;


        // WLog.i("WL --->", "week: " + dayWeek + ", month: " + dayWeekMonth);
        return new Point(dayWeek, dayWeekMonth);
    }

    /**
     * 根据坐标获取【天】
     *
     * @param x 0 为第一列，即星期天
     * @param y 0 为第一行，即第一周
     * @return -1 不是本月的天
     * 1 ~ 31 天
     */
    private int getDayByPoint(int x, int y) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, showYear);
        calendar.set(Calendar.MONTH, showMonth);
        calendar.set(Calendar.WEEK_OF_MONTH, y + 1);
        calendar.set(Calendar.DAY_OF_WEEK, x + 1);
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        // 注意
        // 第一天的左边是上个月的
        // 最后一天的右边是下个月的
        // 这种情况返回-1
        if (y == 0 && day > 7 || y >= 4 && day < 7) {
            return -1;
        }
        return calendar.get(Calendar.DAY_OF_MONTH);
    }


    /**
     * 计算绘画区域
     */
    private void measureArea() {
        int startX = 60;
        int startY = 60;
        int endX = getWidth() - 60;
        // 开始的高度 + （一共有多少周 + 月标题 + 周标题）* 每行高度 + 底部高度
        int endY = (int) (startY + (getWeekCount() + 2) * (mTextSize + mLineInterval) + 40);
        mAreaRect.set(startX, startY, endX, endY);
        // WLog.i("WL --->", "area: " + mAreaRect.toShortString());
    }

    /**
     * 设置显示的日期
     */
    public void setShowDate(int showYear, int showMonth, int showDay) {
        this.showYear = showYear;
        this.showMonth = showMonth;
        this.showDay = showDay;
        setShowDay(showDay);

        invalidate();
    }

    /**
     * 判断一个日期是否是今天
     */
    private boolean isToday(int day) {
        return showYear == currentYear
                && showMonth == currentMonth
                && currentDay == day;
    }

    /**
     * 判断一个日期是选中的天
     */
    private boolean isShowDay(int day) {
        return showDay == day + showMonth * 100 + showYear * 10000;
    }

    public void setShowDay(int day) {
        showDay = day + showMonth * 100 + showYear * 10000;
    }

    /**
     * 判断日期是否是这个月
     */
    private boolean isCurrentMonth() {
        return showYear == currentYear
                && showMonth == currentMonth;
    }

    /**
     * 获取一个月有多少周
     */
    private int getWeekCount() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, showYear);
        calendar.set(Calendar.MONTH, showMonth);
        return calendar.getActualMaximum(Calendar.WEEK_OF_MONTH);
    }

    /**
     * 获取一个月有多少天
     */
    private int getDayCount() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, showYear);
        calendar.set(Calendar.MONTH, showMonth);
        return calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
    }

    /**
     * 这个 touch 是否在有效区域
     */
    public boolean isTouchValid() {
        return mAreaRect.contains(((int) touchPoint.x), ((int) touchPoint.y));
    }

    /**
     * 获取点击的内容
     *
     * @return 1 ~ 31 为点击了日期
     * 32 为点击了【上个月】
     * 33 为点击了【下个月】
     * -1 为点击了其他区域
     */
    public int getClickEvent() {
        int dayTop = mAreaRect.top;
        int dayBottom = mAreaRect.bottom - 40;

        if (touchPoint.y >= dayTop && touchPoint.y <= dayBottom) {
            // 点击在第几个【文字高度行】 偶数为字，奇数为空行
            int y = (int) ((((int) touchPoint.y) - dayTop) / (mTextSize + mLineInterval));
            float yy = (touchPoint.y - dayTop) * 1f / (mTextSize + mLineInterval);
            int iyy = (int) yy;
//            WLog.i("WL --->", "float y: " + yy + ", int y: " + iyy);
            if ((yy - iyy) < (mTextSize * 1f / mLineInterval)) {
                // 单元格宽度
                float unitWidth = mAreaRect.width() / 7;
                // 得到这个日期是第几天，这里有待优化
                int x = (int) ((touchPoint.x - mAreaRect.left) / unitWidth);
                int day = getDayByPoint(x, iyy);
                if (day >= 0) {
//                    WLog.i("WL --->", "x: " + x + ", y: " + iyy + " - day: " + day);
                    return day;
                }
            }
        }
        return -1;
    }
}
