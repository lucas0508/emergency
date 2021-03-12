package com.tjmedicine.emergency.common.dialog;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Typeface;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.facebook.rebound.BaseSpringSystem;
import com.facebook.rebound.SimpleSpringListener;
import com.facebook.rebound.Spring;
import com.facebook.rebound.SpringSystem;
import com.facebook.rebound.SpringUtil;
import com.tjmedicine.emergency.R;
import com.tjmedicine.emergency.utils.AnimUtil;
import com.tjmedicine.emergency.utils.SoundPlayUtils;


/**
 * Created by SXJ on 2017/2/28 0028.
 */

public class TaskSucDialog extends Dialog {

    private TextView taskGrade;
    private ImageView taskStar1;
    private ImageView taskStar2;
    private ImageView taskStar3;
    private TextView taskScore;
    private ImageView taskRestart;
    private ImageView taskClose;

    /*缩放动画*/
    private final BaseSpringSystem mSpringSystem = SpringSystem.create();
    private final ExampleSpringListener mSpringListener = new ExampleSpringListener();
    public Spring mScaleSpring;
    private Handler handler;

    public TaskSucDialog(Context context) {
        super(context, R.style.access_dialog);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.task_access_dialog, null);
        taskGrade = (TextView) view.findViewById(R.id.task_grade);
        taskScore = (TextView) view.findViewById(R.id.task_score);
        taskClose = (ImageView) view.findViewById(R.id.task_close);
        taskStar1 = (ImageView) view.findViewById(R.id.task_star1);
        taskStar2 = (ImageView) view.findViewById(R.id.task_star2);
        taskStar3 = (ImageView) view.findViewById(R.id.task_star3);
        taskRestart = (ImageView) view.findViewById(R.id.task_restart);
        Typeface typeface = Typeface.createFromAsset(context.getAssets(), "fonts/FolksBlack.ttf");
        taskGrade.setTypeface(typeface);
        taskScore.setTypeface(typeface);
        mScaleSpring = mSpringSystem.createSpring();
        handler = new Handler();
        setContentView(view);
        setCancelable(false);
        show();
    }

    public void setTaskClose(View.OnClickListener clickListener) {
        taskClose.setOnClickListener(clickListener);
    }

    public void setTaskScore(Double score) {
        taskScore.setText(score + "");
        if (score < 90) {
            taskGrade.setText("Good !");
            taskStar2.setImageResource(R.mipmap.task_star_dark);
            taskStar3.setImageResource(R.mipmap.task_star_dark);
            setSound(1);
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    taskStar1.setImageResource(R.mipmap.task_star_light);
                    AnimUtil.starAnim(taskStar1);
                }
            }, 500);
        } else if (score >= 90 && score <= 95) {
            taskGrade.setText("Great !");
            taskStar3.setImageResource(R.mipmap.task_star_dark);
            setSound(2);
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    taskStar1.setImageResource(R.mipmap.task_star_light);
                    AnimUtil.starAnim(taskStar1);
                }
            }, 500);
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    taskStar2.setImageResource(R.mipmap.task_star_light);
                    AnimUtil.starAnim(taskStar2);
                }
            }, 1000);
        } else {
            taskGrade.setText("Perfect !!");
            setSound(3);
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    taskStar1.setImageResource(R.mipmap.task_star_light);
                    AnimUtil.starAnim(taskStar1);
                }
            }, 500);
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    taskStar2.setImageResource(R.mipmap.task_star_light);
                    AnimUtil.starAnim(taskStar2);
                }
            }, 1000);
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    taskStar3.setImageResource(R.mipmap.task_star_light);
                    AnimUtil.starAnim(taskStar3);
                }
            }, 1500);
        }
    }

    private void setSound(int num) {
        for (int i = 0; i < num; i++) {
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    SoundPlayUtils.play(4);
                }
            }, 500 * (i + 1));
        }
    }

    public void setTaskRestart(View.OnClickListener clickListener) {
        taskRestart.setOnClickListener(clickListener);
    }



    private class ExampleSpringListener extends SimpleSpringListener {
        @Override
        public void onSpringUpdate(Spring spring) {
            float mappedValue = (float) SpringUtil.mapValueFromRangeToRange(spring.getCurrentValue(), 0, 1, 1, 0.5);
            taskClose.setScaleX(mappedValue);
            taskClose.setScaleY(mappedValue);
        }
    }

    @Override
    public void show() {
        super.show();
        mScaleSpring.addListener(mSpringListener);
    }

    @Override
    public void dismiss() {
        super.dismiss();
        mScaleSpring.removeListener(mSpringListener);
    }
}
