package com.tjmedicine.emergency.ui.mine.contact.view;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.constraintlayout.utils.widget.ImageFilterView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bumptech.glide.Glide;
import com.jude.easyrecyclerview.EasyRecyclerView;
import com.jude.easyrecyclerview.adapter.BaseViewHolder;
import com.jude.easyrecyclerview.adapter.RecyclerArrayAdapter;
import com.tjmedicine.emergency.R;
import com.tjmedicine.emergency.common.base.Adapter;
import com.tjmedicine.emergency.common.base.BaseActivity;
import com.tjmedicine.emergency.common.base.OnMultiClickListener;
import com.tjmedicine.emergency.common.base.ViewHolder;
import com.tjmedicine.emergency.common.bean.VolunteerBean;
import com.tjmedicine.emergency.ui.bean.ContactBean;
import com.tjmedicine.emergency.ui.bean.EmergencyContactData;
import com.tjmedicine.emergency.ui.bean.TeachData;
import com.tjmedicine.emergency.ui.mine.contact.presenter.ContactPresenter;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import de.hdodenhof.circleimageview.CircleImageView;

public class EmergencyContactActivity extends BaseActivity implements IContactView {

    private ContactPresenter contactPresenter = new ContactPresenter(this);

    @BindView(R.id.tv_title)
    TextView tv_title;
    @BindView(R.id.recycler_view)
    EasyRecyclerView mEasyRecyclerView;
    @BindView(R.id.ll)
    LinearLayout linearLayout;
    @BindView(R.id.tv_count)
    TextView mCount;
    @BindView(R.id.btn_add)
    Button btn_add;

    Adapter<ContactBean> mAdapter;

    @Override
    protected int setLayoutResourceID() {
        return R.layout.activity_emergency_contact;
    }

    @Override
    protected void initView() {
        tv_title.setText("紧急联系人");
        initRecyclerView();
        initListeners();
    }

    @Override
    protected void onResume() {
        super.onResume();
        contactPresenter.findContact();
    }

    private void initListeners() {
        btn_add.setOnClickListener(new OnMultiClickListener() {
            @Override
            public void onMultiClick(View v) {
                startActivity(EmergencyAddContactActivity.class);
            }
        });
    }

    private void initRecyclerView() {

        LinearLayoutManager myLinearLayoutManager = new LinearLayoutManager(this);
        mEasyRecyclerView.setLayoutManager(myLinearLayoutManager);

        mEasyRecyclerView.setAdapterWithProgress(mAdapter = new Adapter<ContactBean>(this) {
            @Override
            public BaseViewHolder OnCreateViewHolder(ViewGroup parent, int viewType) {

                return new ViewHolder<ContactBean>(parent, R.layout.contact_recycler_item) {

                    private TextView tv_name, tv_phone;
                    private CircleImageView riv_image;

                    @Override
                    public void initView() {
                        tv_name = $(R.id.tv_name);
                        tv_phone = $(R.id.tv_phone);
                        riv_image = $(R.id.show_img);
                    }

                    @Override
                    public void setData(ContactBean data) {
                        super.setData(data);
                        tv_name.setText(data.getUsername());
                        tv_phone.setText(data.getPhone());
                        riv_image.setBackgroundResource(R.mipmap.head);
                    }
                };
            }
        });

        mAdapter.setOnItemClickListener(position -> {
            Bundle bundle = new Bundle();
            bundle.putSerializable("contactData", mAdapter.getItem(position));
            startActivity(EmergencyAddContactActivity.class, bundle);
        });

        mAdapter.setOnItemLongClickListener(new RecyclerArrayAdapter.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(int position) {
                contactPresenter.delContact(String.valueOf(mAdapter.getItem(position).getId()));
                return false;
            }
        });
    }

    @Override
    public void addContactSuccess() {

    }

    @Override
    public void addContactFail(String info) {

    }

    @Override
    public void findContactSuccess(List<ContactBean> contactBean) {
        if (contactBean.size() == 0) {
            linearLayout.setVisibility(View.VISIBLE);
            mCount.setVisibility(View.GONE);
        } else {
            linearLayout.setVisibility(View.GONE);
            mCount.setVisibility(View.VISIBLE);
            int count = 5;
            int vi = count - contactBean.size();
            mCount.setText("已添加" + contactBean.size() + "位,还可以添加"+vi+"人");
            mAdapter.addAll(contactBean);
        }
    }

    @Override
    public void findContactFail(String info) {
        mApp.shortToast(info);
    }

    @Override
    public void delContactSuccess(String id) {

    }

    @Override
    public void delContactFail(String info) {

    }
}
