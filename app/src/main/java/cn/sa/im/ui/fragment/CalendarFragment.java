package cn.sa.im.ui.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import cn.sa.im.R;
import cn.sa.im.ui.User;
import cn.sa.im.ui.apadper.UserAdapter;
import cn.sa.im.ui.widget.HintSideBar;
import cn.sa.im.ui.widget.SideBar;
import io.rong.imkit.RongIM;
import io.rong.imlib.model.CSCustomServiceInfo;
import io.rong.imlib.model.Conversation;

public class CalendarFragment extends Fragment{

    private List<User> userList;

    private UserAdapter adapter;

    private RecyclerView rv_userList;

    private LinearLayoutManager manager;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_calendar, container,false);

        return view;
    }


}