/*
 * Copyright (c) 2015, Nordic Semiconductor
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *
 * 3. Neither the name of the copyright holder nor the names of its contributors may be used to endorse or promote products derived from this
 * software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE
 * USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.tjmedicine.emergency.ui.uart;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.gson.Gson;
import com.jude.easyrecyclerview.EasyRecyclerView;
import com.jude.easyrecyclerview.adapter.BaseViewHolder;
import com.orhanobut.logger.Logger;
import com.tjmedicine.emergency.EmergencyApplication;
import com.tjmedicine.emergency.R;
import com.tjmedicine.emergency.common.base.Adapter;
import com.tjmedicine.emergency.common.base.OnMultiClickListener;
import com.tjmedicine.emergency.common.base.ViewHolder;
import com.tjmedicine.emergency.common.cache.db.SQLiteHelper;
import com.tjmedicine.emergency.common.dialog.DialogManage;
import com.tjmedicine.emergency.common.dialog.TaskSucDialog;
import com.tjmedicine.emergency.ui.uart.data.presenter.IUARTControlView;
import com.tjmedicine.emergency.ui.uart.data.presenter.UARTControlPresenter;
import com.tjmedicine.emergency.ui.uart.domain.UartConfiguration;
import com.tjmedicine.emergency.ui.uart.profile.UARTInterface;
import com.tjmedicine.emergency.utils.GifLoadOneTimeGif;
import com.tjmedicine.emergency.utils.GsonUtils;
import com.tjmedicine.emergency.utils.LCIMAudioHelper;
import com.tjmedicine.emergency.utils.SimpleCountDownTimer;
import com.tjmedicine.emergency.utils.SoundPlayUtils;
import com.tjmedicine.emergency.utils.ToastUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//, BleManagerCallbacks

public class UARTControlScoreFragment extends Fragment implements UARTActivity.ConfigurationListener, IUARTControlView {
    private final static String TAG = "UARTControlFragment";
    private final static String SIS_EDIT_MODE = "sis_edit_mode";
    private final static String ACTION_RECEIVE = "no.nordicsemi.android.nrftoolbox.uart.ACTION_RECEIVE";
    private UARTInterface uartInterface;
    private Adapter<String> mAdapter;
    private UartConfiguration configuration;
    private boolean editMode;
    private MyReceiver myReceiver;
    EasyRecyclerView mEasyRecyclerView;
    TextView tv_connect, mTime, tv_notice,mHistoricalData;
    Button mStartRobot;
    ImageView mCountdownView;
    List<String> liscount;
    List<String> lisdata;
    private Handler handler;
    private ImageButton ib_close;
    DialogManage mApp;
    private UARTService.UARTBinder bleService;
    private UARTControlPresenter uartControlPresenter = new UARTControlPresenter(this);
    List<String> serverDataList;
    @Override
    public void onAttach(@NonNull final Context context) {
        super.onAttach(context);

        try {
            ((UARTActivity) context).setConfigurationListener(this);
        } catch (final ClassCastException e) {
            Log.e(TAG, "The parent activity must implement EditModeListener");
        }
    }

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            editMode = savedInstanceState.getBoolean(SIS_EDIT_MODE);
        }

        //注册广播接收器
        myReceiver = new MyReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_RECEIVE);
        requireActivity().registerReceiver(myReceiver, intentFilter);
        mApp = new DialogManage(requireActivity());
    }


    public void onServiceStarted() {
        // The service has been started, bind to it
        final Intent service = new Intent(requireActivity(), UARTService.class);
        requireActivity().bindService(service, serviceConnection, 0);
    }

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(final ComponentName name, final IBinder service) {
            bleService = (UARTService.UARTBinder) service;
            uartInterface = bleService;
            tv_connect.setText("关闭连接");


        }


        @Override
        public void onServiceDisconnected(final ComponentName name) {
            uartInterface = null;
            tv_connect.setText("连接模拟人");
        }
    };


    @Override
    public void onSaveInstanceState(final Bundle outState) {
        outState.putBoolean(SIS_EDIT_MODE, editMode);
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {

        final View view = inflater.inflate(R.layout.fragment_feature_uart_control_score, container, false);
        requireActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        mEasyRecyclerView = view.findViewById(R.id.recycler_view);
        tv_connect = view.findViewById(R.id.action_connect);
        ib_close = view.findViewById(R.id.ib_close);
        tv_notice = view.findViewById(R.id.tv_notice);
        mStartRobot = view.findViewById(R.id.start_robot);
        mTime = view.findViewById(R.id.tv_time);
        mHistoricalData = view.findViewById(R.id.tv_historical_data);
        mCountdownView = view.findViewById(R.id.iv_countdown);
        handler = new Handler();
        new SoundPlayUtils(requireActivity());
        initRecyclerView();
        initListeners();
        return view;
    }

    private void initListeners() {
        startUARTRobot();

        mHistoricalData.setOnClickListener(new OnMultiClickListener() {
            @Override
            public void onMultiClick(View v) {
                Intent intent = new Intent(requireActivity(),UARTRobotActivity.class);
                intent.putExtra("","");
                startActivity(intent);
            }
        });
    }

    /**
     * 开启模拟人
     */
    private void startUARTRobot() {
        mStartRobot.setOnClickListener(new OnMultiClickListener() {
            @Override
            public void onMultiClick(View v) {
                if (null != uartInterface) {
                    mStartRobot.setVisibility(View.GONE);
                    tv_notice.setVisibility(View.GONE);
                    SoundPlayUtils.play(5);
                    GifLoadOneTimeGif.loadOneTimeGif(requireActivity(), R.drawable.countdown, mCountdownView, 1, new GifLoadOneTimeGif.GifListener() {
                        @Override
                        public void gifPlayComplete() {
                            //播放完成回调
                            if (null != uartInterface) {
                                uartInterface.send("<M>Start</M>");
                                serverDataList = new ArrayList<>();
                                startTimer();
                            }
                            liscount = new ArrayList<>();
                            lisdata = new ArrayList<>();
                            mAdapter.clear();
                        }
                    });

                } else {
                    ToastUtils.showTextToas(requireActivity(), "请连接模拟人后在点击开始测试哦~");
//                    startTimer();
                }
            }
        });
    }

    /**
     * 开始倒计时
     */
    private void startTimer() {
        long totalTime = 6000;
        // 初始化并启动倒计时
        new SimpleCountDownTimer(totalTime, mTime).setOnFinishListener(new SimpleCountDownTimer.OnFinishListener() {
            @Override
            public void onFinish() {
                // TODO: 2020-12-23 弹出评分
                //清除数据
                //断开模拟人
                Log.e(TAG, " --UARTControlFragment---传递服务器数据:-------> " + new Gson().toJson(serverDataList));
                uartControlPresenter.postUARTData(serverDataList, "1");
                if (null != uartInterface) {
                    uartInterface.send("<M>Stop</M>");
                }

            }
        }).start();
    }

    /**
     * 按压速度
     */
    private void PDSpeed() {
        String music = "android.resource://" + requireActivity().getPackageName() + "/" + R.raw.music;

        LCIMAudioHelper.getInstance().playAudio(music);
        Log.d("TAG", "handleMessage: timer  lisdata-->" + lisdata.size());
        // 20次/10S
        for (int i = 0; i < lisdata.size(); i++) {
            if (lisdata.get(i).contains("PD")) {
                liscount.add(lisdata.get(i));
            }
        }
        if (liscount.size() < 16) {
            liscount.clear();
            lisdata.clear();
            Log.d("TAG", "handleMessage: timer 按压太慢慢慢了" + liscount.size());
//                ToastUtils.showImageToas(requireActivity(), "按压太慢慢慢了");
            LCIMAudioHelper.getInstance().playAudio(music);
//                MediaPlayer mediaPlayer = MediaPlayer.create(getActivity(), R.raw.music);
            //用prepare方法，会报错误java.lang.IllegalStateExceptio
            //mediaPlayer.prepare();
//                mediaPlayer.start();
        } else if (liscount.size() > 20) {
//                MediaPlayer mediaPlayer = MediaPlayer.create(getActivity(), R.raw.music);
            //用prepare方法，会报错误java.lang.IllegalStateExceptio
            //mediaPlayer.prepare();
//                mediaPlayer.start();
            LCIMAudioHelper.getInstance().playAudio(music);
            liscount.clear();
            lisdata.clear();
            Log.d("TAG", "handleMessage: timer 按压太快快快了" + liscount.size());
//            ToastUtils.showImageToas(requireActivity(), "按压太快快快了");
        }
    }


    @Override
    public void onConfigurationModified() {

    }

    @Override
    public void onConfigurationChanged(@NonNull final UartConfiguration configuration) {
    }


    @Override
    public void postUARTDataSuccess(String score) {
        mApp.getLoadingDialog().hide();
        final TaskSucDialog taskSucDialog = new TaskSucDialog(EmergencyApplication.getContext());
        taskSucDialog.setTaskScore(Integer.parseInt(score));
        taskSucDialog.setTaskClose(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                taskSucDialog.mScaleSpring.setEndValue(1);
                SoundPlayUtils.play(2);
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        taskSucDialog.mScaleSpring.setEndValue(0);
                    }
                }, 100);
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        taskSucDialog.dismiss();
                        mStartRobot.setVisibility(View.VISIBLE);
                    }
                }, 300);
            }
        });
    }

    @Override
    public void postUARTDataFail(String info) {
        mApp.shortToast(info);
        mApp.getLoadingDialog().hide();
    }

    public class MyReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            //接收MainActivity传过来的数据
//            Toast.makeText(context, intent.getStringExtra(Intent.EXTRA_TEXT), Toast.LENGTH_SHORT).show();
            String data = intent.getStringExtra(Intent.EXTRA_TEXT);
            Log.e(TAG, " --UARTControlFragment---onReceive:-------> " + data);

            Map<String, Object> stringObjectMap = new HashMap<>();
            stringObjectMap.put("id", System.currentTimeMillis());
            stringObjectMap.put("data", data);
            SQLiteHelper.getInstance().mUartDB.insertData(SQLiteHelper.getInstance().getDBInstance(), stringObjectMap);
            String s = GsonUtils.returnFormatText(data);
            // String s1 = GsonUtils.returnStatus(s);//最终要展示的数据
            Message message = Message.obtain();
            message.obj = s;
            mHandler.sendMessage(message);
        }
    }

    private void initRecyclerView() {
        LinearLayoutManager myLinearLayoutManager = new LinearLayoutManager(requireActivity());
        mEasyRecyclerView.setLayoutManager(myLinearLayoutManager);
        mEasyRecyclerView.setAdapterWithProgress(mAdapter = new Adapter<String>(requireActivity()) {
            @Override
            public BaseViewHolder OnCreateViewHolder(ViewGroup parent, int viewType) {

                return new ViewHolder<String>(parent, R.layout.robot_recrcler_item) {

                    private TextView tv_name, tv_msg, tv_count;

                    @Override
                    public void initView() {
                        tv_name = $(R.id.tv_name);
                        tv_msg = $(R.id.tv_msg);
                        tv_count = $(R.id.tv_count);
                    }

                    @Override
                    public void setData(String data) {
                        super.setData(data);

                        String[] splitdata1 = data.split(",");
                        //手机->设备	Start
                        Log.e(TAG, "setData: " + data);
//                        Log.e(TAG, "setData:count--> " + count);
                        tv_count.setText(splitdata1[1] + "");

                        if ("Blow".equals(splitdata1[0])) {
                            tv_name.setText("吹气：");
                            tv_msg.setText("YES");
                        }
                        //PD=18   PD=18,0
                        if (splitdata1[0].contains("PD")) {
                            String[] split = splitdata1[0].split("=");
                            tv_name.setText("按压深度：");
                            tv_msg.setText(split[1] + "mm");
                        }
                    }
                };
            }
        });
    }


    @SuppressLint("HandlerLeak")
    public Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {

            Log.e(TAG, "数据回调: " + msg.obj.toString());
            if (msg.obj.toString().equals("OK")) {
                return;
            }
            if (msg.obj.toString().equals("error")) {
                return;
            }
//            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//            for (int i = 0; i < 3; i++) {
//                try {
//                    Thread.sleep(5 * 1000); //设置暂停的时间 5 秒
//                    Log.e(TAG, "时间: "+sdf.format(new Date()) + "--循环执行第" + (i+1) + "次");
//                    System.out.println(sdf.format(new Date()) + "--循环执行第" + (i+1) + "次");
//
//                    List<String> allData = mAdapter.getAllData();
//
//                    for (int i1 = 0; i1 < allData.size(); i1++) {
//
//                        if (allData.get(i).contains("PD")) {
//                            System.out.println(sdf.format(new Date()) +allData.get(i) + "次");
//                            Log.e(TAG, "数据收集" + sdf.format(new Date()) +allData.get(i) + "次");
//                        }
//                    }
//
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//            }
            int count = (mAdapter.getCount() + 1);
            lisdata.add(msg.obj.toString());
            serverDataList.add(msg.obj.toString());
            mAdapter.add(msg.obj.toString() + "," + count);
            mEasyRecyclerView.scrollToPosition(mAdapter.getCount());
            mAdapter.notifyDataSetChanged();

            List<Map<String, Object>> maps = SQLiteHelper.getInstance().mUartDB.queryUserInfo(SQLiteHelper.getInstance().getDBInstance(), null);
            Logger.d("数据库中数据：" + new Gson().toJson(maps));
        }
    };


    @Override
    public void onStart() {
        super.onStart();

        /*
         * If the service has not been started before the following lines will not start it. However, if it's running, the Activity will be bound to it
         * and notified via serviceConnection.
         */
        final Intent service = new Intent(getActivity(), UARTService.class);
        requireActivity().bindService(service, serviceConnection, 0); // we pass 0 as a flag so the service will not be created if not exists

    }

    @Override
    public void onStop() {
        super.onStop();

        try {
            requireActivity().unbindService(serviceConnection);
            uartInterface = null;
        } catch (final IllegalArgumentException e) {
            // do nothing, we were not connected to the sensor
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        ((UARTActivity) requireActivity()).setConfigurationListener(null);

        if (uartInterface != null) {
            uartInterface.send("<M>Stop</M>");
            uartInterface = null;
        }
        if (bleService != null)
            bleService.disconnect();
    }
}
