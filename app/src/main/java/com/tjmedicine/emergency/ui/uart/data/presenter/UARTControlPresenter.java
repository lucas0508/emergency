package com.tjmedicine.emergency.ui.uart.data.presenter;

import com.tjmedicine.emergency.common.base.BasePresenter;
import com.tjmedicine.emergency.common.base.IBaseModel;
import com.tjmedicine.emergency.common.net.HttpProvider;
import com.tjmedicine.emergency.common.net.ResponseEntity;
import com.tjmedicine.emergency.ui.login.model.ILoginModel;
import com.tjmedicine.emergency.ui.uart.data.model.IUARTControlModel;
import com.tjmedicine.emergency.ui.uart.data.model.impl.UARTControlModel;

import java.util.ArrayList;
import java.util.List;

public class UARTControlPresenter extends BasePresenter {


    private IUARTControlView iuartControlView;


    private IUARTControlModel iuartControlModel = new UARTControlModel();


    public UARTControlPresenter(IUARTControlView iuartControlView) {
        this.iuartControlView = iuartControlView;
    }

    public void postUARTData(List<String> list, String type) {
        iuartControlModel.sendUARTData(list, type, new IBaseModel.OnCallbackListener() {
            @Override
            public void callback(ResponseEntity res) {
                if (HttpProvider.isSuccessful(res.getCode())) {
                    iuartControlView.postUARTDataSuccess((Double) res.getData());
                } else {
                    iuartControlView.postUARTDataFail(res.getMsg());
                }
            }
        });
    }
}
