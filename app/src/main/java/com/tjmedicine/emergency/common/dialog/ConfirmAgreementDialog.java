package com.tjmedicine.emergency.common.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.text.Html;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.View;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.tjmedicine.emergency.R;
import com.tjmedicine.emergency.common.global.Constants;
import com.tjmedicine.emergency.common.global.GlobalConstants;
import com.tjmedicine.emergency.ui.other.WebActivity;

/**
 * @author QiZai
 * @desc
 * @date 2018/5/2 11:12
 */

public class ConfirmAgreementDialog extends Dialog {
    private Context mContext;
    private AlertDialog.Builder mBuilder;
    private AlertDialog mDialog;
    private View mView;
    private TextView mMessage, mWarnMessage, mOk, mCancel;
    private ConfirmCallback mConfirmCallback;
    private LinearLayout mDialogContent;

    public ConfirmAgreementDialog(@NonNull Context context) {
        super(context);
        this.mContext = context;
        mBuilder = new AlertDialog.Builder(mContext);
        mView = getLayoutInflater().inflate(R.layout.dialog_confirm_agreement, null);
        mDialogContent = mView.findViewById(R.id.ll_dialog_content);
        mMessage = mView.findViewById(R.id.tv_message);
        mWarnMessage = mView.findViewById(R.id.tv_warn_message);
        mOk = mView.findViewById(R.id.b_ok);
        mOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismissDialog(1);
            }
        });

        mCancel = mView.findViewById(R.id.b_cancel);
        mCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismissDialog(0);
            }
        });

        mBuilder.setView(mView);

        mDialog = mBuilder.create();
        mDialog.setCanceledOnTouchOutside(false);
        mDialog.setCancelable(false);
        Window w = mDialog.getWindow();
        w.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
    }


    //  Html.fromHtml("点击登录,即同意<font color=\"#0498f5\">《叫个工人用户协议》</font>");

    String ServiceAgreement = "<font color=\"#0498f5\">《服务协议》</font>";

    String privacyPolicy = "<font color=\"#0498f5\">《隐私政策》</font>";

    String notice = "<font color=\"#000000\">为了向你提供地理位置等服务,我们需要收集您的定位信息，你可以在设置中查看、变更并管理你的授权。</font>";

    public void show(@NonNull String message, ConfirmCallback callback) {
        mMessage.setText(message);
        mConfirmCallback = callback;
        showDialog();

        SpannableString spannableString = new SpannableString(Html.fromHtml("请你务必审慎阅读、充分理解" + ServiceAgreement + "和" + privacyPolicy + "各条款，包括但不限于:" +
                notice));


        spannableString.setSpan(new Clickable(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //点击代码
                Intent intent = new Intent(mContext, WebActivity.class);
                intent.putExtra(Constants.WEB_KEY_URL, GlobalConstants.AGREEMENT_URL);
                intent.putExtra(Constants.WEB_KEY_FLAG, 1);
                mContext.startActivity(intent);
            }
        }), 13, 19, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        spannableString.setSpan(new Clickable(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //点击代码
                Intent intent = new Intent(mContext, WebActivity.class);
                intent.putExtra(Constants.WEB_KEY_URL, GlobalConstants.PRIVACYPOLICY_URL);
                intent.putExtra(Constants.WEB_KEY_FLAG, 1);
                mContext.startActivity(intent);
            }
        }), 20, 26, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        mWarnMessage.setText(spannableString);
        mWarnMessage.setMovementMethod(LinkMovementMethod.getInstance());

    }

    private void showDialog() {
        mDialog.show();
        mView.startAnimation(AnimationUtils.loadAnimation(mContext, R.anim.alpha_in));
        mDialogContent.startAnimation(AnimationUtils.loadAnimation(mContext, R.anim.scale_in));
    }

    private void dismissDialog(final int type) {
        Animation animation = AnimationUtils.loadAnimation(mContext, R.anim.scale_out);
        mDialogContent.startAnimation(animation);
        mView.startAnimation(AnimationUtils.loadAnimation(mContext, R.anim.alpha_out));
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                if (type == 1 && mConfirmCallback != null)
                    mConfirmCallback.onOk();
                if (type == 0 && mConfirmCallback != null)
                    mConfirmCallback.onCancel();
                mDialog.dismiss();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
    }

    public interface ConfirmCallback {
        void onOk();
        void onCancel();
    }


    class Clickable extends ClickableSpan implements View.OnClickListener {

        private final View.OnClickListener mListener;

        public Clickable(View.OnClickListener l) {
            mListener = l;
        }

        @Override
        public void updateDrawState(TextPaint ds) {
            //设置是否有下划线
            ds.setUnderlineText(false);
        }

        @Override
        public void onClick(View v) {
            mListener.onClick(v);
        }
    }


}
