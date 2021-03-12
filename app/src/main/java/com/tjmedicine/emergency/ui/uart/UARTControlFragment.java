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
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.gson.Gson;
import com.jude.easyrecyclerview.EasyRecyclerView;
import com.jude.easyrecyclerview.adapter.BaseViewHolder;
import com.lxj.xpopup.XPopup;
import com.orhanobut.logger.Logger;
import com.tjmedicine.emergency.EmergencyApplication;
import com.tjmedicine.emergency.R;
import com.tjmedicine.emergency.common.base.Adapter;
import com.tjmedicine.emergency.common.base.OnMultiClickListener;
import com.tjmedicine.emergency.common.base.ViewHolder;
import com.tjmedicine.emergency.common.cache.SharedPreferences.UserInfo;
import com.tjmedicine.emergency.common.cache.db.SQLiteHelper;
import com.tjmedicine.emergency.common.dialog.CustomDjsFullScreenPopup;
import com.tjmedicine.emergency.common.dialog.DialogManage;
import com.tjmedicine.emergency.common.global.Constants;
import com.tjmedicine.emergency.common.global.GlobalConstants;
import com.tjmedicine.emergency.common.net.HttpProvider;
import com.tjmedicine.emergency.common.net.HttpsUtils;
import com.tjmedicine.emergency.ui.bean.PDBean;
import com.tjmedicine.emergency.ui.uart.data.presenter.IUARTControlView;
import com.tjmedicine.emergency.ui.uart.data.presenter.UARTControlPresenter;
import com.tjmedicine.emergency.ui.uart.domain.Command;
import com.tjmedicine.emergency.ui.uart.domain.UartConfiguration;
import com.tjmedicine.emergency.ui.uart.profile.BleProfileServiceReadyActivity;
import com.tjmedicine.emergency.ui.uart.profile.UARTInterface;
import com.tjmedicine.emergency.utils.GifLoadOneTimeGif;
import com.tjmedicine.emergency.utils.GsonUtils;
import com.tjmedicine.emergency.utils.LCIMAudioHelper;
import com.tjmedicine.emergency.utils.SoundPlayUtils;
import com.tjmedicine.emergency.utils.ToastUtils;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import lecho.lib.hellocharts.model.Axis;
import lecho.lib.hellocharts.model.AxisValue;
import lecho.lib.hellocharts.model.Line;
import lecho.lib.hellocharts.model.LineChartData;
import lecho.lib.hellocharts.model.PointValue;
import lecho.lib.hellocharts.model.ValueShape;
import lecho.lib.hellocharts.model.Viewport;
import lecho.lib.hellocharts.util.ChartUtils;
import lecho.lib.hellocharts.view.LineChartView;


public class UARTControlFragment extends Fragment implements UARTActivity.ConfigurationListener, IUARTControlView {
    private final static String TAG = "UARTControlFragment";
    private final static String SIS_EDIT_MODE = "sis_edit_mode";
    private final static String ACTION_RECEIVE = "no.nordicsemi.android.nrftoolbox.uart.ACTION_RECEIVE";
    private UARTInterface uartInterface;
    private Adapter<String> mAdapter;
    private UartConfiguration configuration;
    private boolean editMode;
    private MyReceiver myReceiver;
    EasyRecyclerView mEasyRecyclerView;
    TextView tv_connect;
    Button mStartRobot;
    Chronometer chronometer;
    ImageView mCountdownView;
    long startTime;
    boolean incline = true;//是否点击开始模拟人
    List<String> liscount;
    List<String> lisdata;
    List<String> serverDataList;
    List<String> blowList = new ArrayList<>();

    private LineChartView chart;        //显示线条的自定义View
    private LineChartData data;          // 折线图封装的数据类
    private int numberOfLines = 1;         //线条的数量
    private int maxNumberOfLines = 4;     //最大的线条数据
//    private int numberOfPoints = 12;     //点的数量

    //    private float[][] randomNumbersTab = new float[maxNumberOfLines][numberOfPoints];         //二维数组，线的数量和点的数量
    private List<PDBean> listBlood = new ArrayList<PDBean>();//数据

    private boolean hasAxes = true;       //是否有轴，x和y轴
    private boolean hasAxesNames = true;   //是否有轴的名字
    private boolean hasLines = true;       //是否有线（点和点连接的线）
    private boolean hasPoints = true;       //是否有点（每个值的点）
    private ValueShape shape = ValueShape.CIRCLE;    //点显示的形式，圆形，正方向，菱形
    private boolean isFilled = false;                //是否是填充
    private boolean hasLabels = false;               //每个点是否有名字
    private boolean isCubic = false;                 //是否是立方的，线条是直线还是弧线
    private boolean hasLabelForSelected = false;       //每个点是否可以选择（点击效果）
    private boolean pointsHaveDifferentColor;           //线条的颜色变换
    private boolean hasGradientToTransparent = false;      //是否有梯度的透明

    private List<AxisValue> mAxisXValues = new ArrayList<AxisValue>();   //x轴方向的坐标数据
    private List<AxisValue> mAxisYValues = new ArrayList<AxisValue>();


    Timer timer;
    private UARTService.UARTBinder bleService;
    private UARTControlPresenter uartControlPresenter = new UARTControlPresenter(this);
    DialogManage mApp;
    //是否吹气   每个15次按压检测是否有吹气
    int blowCount = 0;
    private CustomDjsFullScreenPopup customDjsFullScreenPopup;

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
        new SoundPlayUtils(requireActivity());


    }

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(final ComponentName name, final IBinder service) {
            bleService = (UARTService.UARTBinder) service;

            Log.e(TAG, "onServiceConnected: ------------------------------" + bleService.getBluetoothDevice());

            Log.d(TAG, "onServiceConnected: ------------------------------" + bleService.getDeviceAddress());

            Log.d(TAG, "onServiceConnected: ------------------------------" + bleService.getDeviceName());

            Log.e(TAG, "onServiceConnected: ------------------------------");
            uartInterface = bleService;

        }

        @Override
        public void onServiceDisconnected(final ComponentName name) {
            uartInterface = null;
        }
    };


    @Override
    public void onSaveInstanceState(final Bundle outState) {
        outState.putBoolean(SIS_EDIT_MODE, editMode);
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {

        final View view = inflater.inflate(R.layout.fragment_feature_uart_control, container, false);
        requireActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        mEasyRecyclerView = view.findViewById(R.id.recycler_view);
        tv_connect = view.findViewById(R.id.action_connect);
        mStartRobot = view.findViewById(R.id.start_robot);
        chronometer = view.findViewById(R.id.chronometer);
        chart = (LineChartView) view.findViewById(R.id.chart);

        chronometer.setFormat("练习时长：%s");
        mCountdownView = view.findViewById(R.id.iv_countdown);
        if (System.currentTimeMillis() - startTime == 5000) {
            Log.e(TAG, " --UARTControlFragment->" + String.valueOf(System.currentTimeMillis() - startTime));
            startTime = System.currentTimeMillis();
        }
        startUARTRobot();
        initRecyclerView();
        generateValues();
        generateData();
        resetViewport();
        return view;
    }

    private void generateValues() {
//        listBlood.add(new PDBean("1", 20));
//        listBlood.add(new PDBean("2", 60));
//        listBlood.add(new PDBean("3", 10));
//        listBlood.add(new PDBean("4", 90));
//        listBlood.add(new PDBean("5", 80));
//        listBlood.add(new PDBean("6", 40));
        mAxisXValues.clear();
        for (int i = 0; i < listBlood.size(); i++) {
            mAxisXValues.add(new AxisValue(i / 2).setLabel(""));
        }
        mAxisYValues.clear();
        //设置y轴坐标，显示的是数值0，30，60.。。。
        for (int i = 0; i < 8; i++) {
            int lengthY = 10 * i;
            mAxisYValues.add(new AxisValue(lengthY / 2).setLabel("" + lengthY));
        }
//        mAxisXValues.clear();
//        for (int i = 0; i < listBlood.size(); i++) {
//            mAxisXValues.add(new AxisValue(i / 2).setLabel(""));
//        }
//        mAxisYValues.clear();
//        //设置y轴坐标，显示的是数值0，30，60.。。。
//        for (int i = 0; i < 8; i++) {
//            int lengthY = 10 * i;
//            mAxisYValues.add(new AxisValue(lengthY / 2).setLabel("" + lengthY));
//        }
    }

    private void generateData() {
        //存放线条对象的集合
        List<Line> lines = new ArrayList<Line>();
        //把数据设置到线条上面去
        for (int i = 0; i < numberOfLines; ++i) {
            List<PointValue> values = new ArrayList<PointValue>();
            for (int j = 0; j < listBlood.size(); ++j) {
                //PointValue的两个参数值，一个是距离y轴的长度距离，另一个是距离x轴长度距离
                values.add(new PointValue(j, listBlood.get(j).getValue()));  //y的值除以2，因为默认在y上显示的是0到100，0到200的数值除以2，就相当于0到100.
            }

            Line line = new Line(values);
            //设置线条的基本属性
            line.setColor(ChartUtils.COLORS[i]);
            line.setShape(shape);
            line.setCubic(isCubic);
            line.setFilled(isFilled);
            line.setHasLabels(hasLabels);
            line.setHasLabelsOnlyForSelected(hasLabelForSelected);
            line.setHasLines(hasLines);
            line.setHasPoints(hasPoints);
            if (pointsHaveDifferentColor) {
                line.setPointColor(ChartUtils.COLORS[(i + 1) % ChartUtils.COLORS.length]);
            }
            lines.add(line);
        }

        data = new LineChartData(lines);

        if (hasAxes) {
            Axis axisX = new Axis();
            Axis axisY = new Axis().setHasLines(true);
            if (hasAxesNames) {
                axisX.setName("时间");
                axisY.setName("按压深度(mm)");
            }

            //对x轴，数据和属性的设置
            axisX.setTextSize(8);//设置字体的大小
            axisX.setHasTiltedLabels(false);//x坐标轴字体是斜的显示还是直的，true表示斜的
            axisX.setTextColor(Color.BLACK);//设置字体颜色
            axisX.setHasLines(true);//x轴的分割线
            axisX.setValues(mAxisXValues); //设置x轴各个坐标点名称

            //对Y轴 ，数据和属性的设置
            axisY.setTextSize(10);
            axisY.setHasTiltedLabels(false);//true表示斜的
            axisY.setTextColor(Color.BLACK);//设置字体颜色
            axisY.setValues(mAxisYValues); //设置x轴各个坐标点名称


            data.setAxisXBottom(axisX);//x轴坐标线的文字，显示在x轴下方
            //data.setAxisXTop();      //显示在x轴上方
            data.setAxisYLeft(axisY);   //显示在y轴的左边

        } else {
            data.setAxisXBottom(null);
            data.setAxisYLeft(null);
        }

        data.setBaseValue(Float.NEGATIVE_INFINITY);
        chart.setLineChartData(data);
    }

    private void resetViewport() {
        // Reset viewport height range to (0,100)
        final Viewport v = new Viewport(chart.getMaximumViewport());
        v.bottom = 0;
        v.top = 100;
        v.left = 0;
        v.right = 12; //  listBlood.size() - 1//如何解释
        chart.setMaximumViewport(v);
        chart.setCurrentViewport(v);
    }


    /**
     * 开启模拟人
     */
    private void startUARTRobot() {
        mStartRobot.setOnClickListener(new OnMultiClickListener() {
            @Override
            public void onMultiClick(View v) {
                if (null != uartInterface) {
                    if (incline) {
                        customDjsFullScreenPopup = new CustomDjsFullScreenPopup(requireActivity(), new CustomDjsFullScreenPopup.OnMyCompletionListener() {
                            @Override
                            public void onClick() {
                                customDjsFullScreenPopup.dismiss();
                                //播放完成回调
                                //final String text = field.getText().toString();
                                Log.e(TAG, "onMultiClick:111-- " + incline);
                                if (null != uartInterface) {
                                    uartInterface.send("<TestStart>");
                                    HttpProvider.doGet(GlobalConstants.APP_BURIED_POINT + Constants.B_BLE_START + "," + bleService.getDeviceAddress(), null);
                                    startTimer();
                                }
                                liscount = new ArrayList<>();
                                lisdata = new ArrayList<>();
                                mAdapter.clear();
                                incline = false;
                                serverDataList = new ArrayList<>();
                            }
                        });
                        new XPopup.Builder(requireActivity()).asCustom(customDjsFullScreenPopup).show();
                    } else {
                        Log.e(TAG, "onMultiClick:2222-- " + incline);
                        mStartRobot.setText("开启模拟人");
                        uartInterface.send("<TestStop>");
                        chronometer.setFormat("计时结束：%s");
                        chronometer.stop();
                        if (timer != null) {
                            timer.cancel();
                        }
                        mApp.getLoadingDialog().show();
                        HttpProvider.doGet(GlobalConstants.APP_BURIED_POINT + Constants.B_BLE_END + "," + bleService.getDeviceAddress(), null);
                        Log.e(TAG, " --UARTControlFragment---传递服务器数据:-------> " + new Gson().toJson(serverDataList));
                        uartControlPresenter.postUARTData(serverDataList, "2");
                        incline = true;
                    }

                } else {
                    ToastUtils.showTextToas(requireActivity(), "请连接模拟人后在点击开始练习哦~");
//                    startTimer();
                }
            }
        });
    }

    private void startTimer() {

        startTime = System.currentTimeMillis();
        // 启动计时器，延迟 1s 执行，每 10s 执行一次
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                Message msg = handler.obtainMessage();
                msg.what = 1;
                msg.obj = "";
                msg.sendToTarget();

            }
        }, 10000, 10000);
        //ToastUtils.showImageToas(requireActivity(), "开始模拟人操作,请按压模拟人");
        mStartRobot.setText("结束");
        chronometer.setFormat("练习时长：%s");
        chronometer.setBase(SystemClock.elapsedRealtime());// 去掉可实现暂停功能
        chronometer.start();
    }

    private Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            PDSpeed();
        }
    };

    /**
     * 按压深度
     * 5cm -6cm
     */


    /**
     * 频率
     * 按压速度
     * 30次按压之后+2次呼吸
     */
    private void PDSpeed() {
        Log.d("TAG", "handleMessage: timer  lisdata-->" + lisdata.size());
        // 20次/10S
        for (int i = 0; i < lisdata.size(); i++) {
            if (lisdata.get(i).contains("PD")) {
                liscount.add(lisdata.get(i));  // 10s内 blow的次数
            }
        }
        //x < 16
        // 10S 按压16-20次
        //x> 20



        if (liscount.size() < 16) {
            liscount.clear();
            lisdata.clear();
            MediaPlayer mediaPlayer = MediaPlayer.create(getActivity(), R.raw.pd_slow);
            mediaPlayer.start();
            Log.d("TAG", "handleMessage: timer 按压太慢慢慢了");
        } else if (liscount.size() > 20) {
            MediaPlayer mediaPlayer = MediaPlayer.create(getActivity(), R.raw.pd_fast);
            mediaPlayer.start();
            liscount.clear();
            lisdata.clear();
            Log.d("TAG", "handleMessage: timer 按压太快快快了");
        }
    }


    @Override
    public void onConfigurationModified() {
    }

    @Override
    public void onConfigurationChanged(@NonNull final UartConfiguration configuration) {

    }

    @Override
    public void postUARTDataSuccess(Double score) {
        mApp.getLoadingDialog().hide();
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
            GsonUtils.returnFormatText2(data);
//            Map<String, Object> stringObjectMap = new HashMap<>();
//            stringObjectMap.put("id", System.currentTimeMillis());
//            stringObjectMap.put("data", data);
            //SQLiteHelper.getInstance().mUartDB.insertData(SQLiteHelper.getInstance().getDBInstance(), stringObjectMap);
//            String s = GsonUtils.returnFormatText(data);
            // String s1 = GsonUtils.returnStatus(s);//最终要展示的数据
//            Message message = Message.obtain();
//            message.obj = s;
//            mHandler.sendMessage(message);
        }
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
            //PD=18   PD=18,0
            if (msg.obj.toString().contains("PD")) {
                String[] split = msg.obj.toString().split("=");
                listBlood.add(new PDBean(count+"",Integer.valueOf(split[1])));
                generateData();
            }
//            isBlow(msg);
            isBlow30(msg);
            serverDataList.add(msg.obj.toString());
            mAdapter.add(msg.obj.toString() + "," + count);
            mEasyRecyclerView.scrollToPosition(mAdapter.getCount());
            mAdapter.notifyDataSetChanged();


//            List<Map<String, Object>> maps = SQLiteHelper.getInstance().mUartDB.queryUserInfo(SQLiteHelper.getInstance().getDBInstance(), null);
//            Logger.d("数据库中数据：" + new Gson().toJson(maps));
        }
    };

    /**
     * 每到16次的时候 检查是否呼气 没有呼气则语音提示
     *
     * @param msg
     */
    private void isBlow(Message msg) {
        blowCount++;
        blowList.add(msg.obj.toString());
        if (blowCount == 16) {
            for (int i = 0; i < blowList.size(); i++) {
                if (!blowList.contains("Blow")) {
                    //吹气播放
                    MediaPlayer mediaPlayer = MediaPlayer.create(getActivity(), R.raw.blow);
                    mediaPlayer.start();
                    blowCount = 0;
                    blowList.clear();
                    Log.d("TAG", "handleMessage: timer 吹气吹气吹气吹气吹气");
                }
            }
        }
    }

    /**
     * 每到31次的时候 检查是否呼气 没有呼气则语音提示
     * 每到32次的时候 检查是否呼气 没有呼气则语音提示
     *
     * @param msg
     */
    private void isBlow30(Message msg) {
        blowCount++;
        blowList.add(msg.obj.toString());

        if (blowCount == 31) {
            if (!blowList.get(30).contains("Blow")) {
                Log.e("TAG", "onCreate: 第 " + blowCount + " 次吹气一次");
                //吹气播放
                MediaPlayer mediaPlayer = MediaPlayer.create(getActivity(), R.raw.blow);
                mediaPlayer.start();
            }
        }
        if (blowCount == 32) {
            if (!blowList.get(31).contains("Blow")) {
                Log.e("TAG", "onCreate: 第 " + blowCount + " 次吹气一次");
                MediaPlayer mediaPlayer = MediaPlayer.create(getActivity(), R.raw.blow);
                mediaPlayer.start();
            }
            blowCount = 0;
            blowList.clear();
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
        if (myReceiver != null) {
            requireActivity().unregisterReceiver(myReceiver);
        }
        ((UARTActivity) requireActivity()).setConfigurationListener(null);
        if (timer != null)
            timer.cancel();

        if (uartInterface != null) {
            HttpProvider.doGet(GlobalConstants.APP_BURIED_POINT + Constants.B_BLE_END + "," + bleService.getDeviceAddress(), null);

            uartInterface.send("<M>Stop</M>");
            uartInterface = null;

        }
        if (bleService != null) {
            try {
                bleService.disconnect();
            } catch (Exception e) {
            }
        }
    }


}
