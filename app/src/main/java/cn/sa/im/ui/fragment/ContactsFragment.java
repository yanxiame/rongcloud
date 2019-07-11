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

public class ContactsFragment extends Fragment implements SideBar.OnChooseLetterChangedListener{

    private List<User> userList;

    private UserAdapter adapter;

    private RecyclerView rv_userList;

    private LinearLayoutManager manager;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_contacts, container,false);
        HintSideBar hintSideBar = (HintSideBar) view.findViewById(R.id.hintSideBar);
        rv_userList = (RecyclerView) view.findViewById(R.id.rv_userList);
        hintSideBar.setOnChooseLetterChangedListener(this);
        manager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        rv_userList.setLayoutManager(manager);
        userList = new ArrayList<>();
        adapter = new UserAdapter(getActivity());
        initData();
        adapter.setData(userList);
        rv_userList.setAdapter(adapter);
        return view;
    }

    @Override
    public void onChooseLetter(String s) {
        int i = adapter.getFirstPositionByChar(s.charAt(0));
        if (i == -1) {
            return;
        }
        manager.scrollToPositionWithOffset(i, 0);
    }

    @Override
    public void onNoChooseLetter() {

    }
    public void initData() {
        User user1 = new User("陈", "12345678");
        User user2 = new User("赵", "12345678");
        userList.add(user1);
        userList.add(user2);

        adapter.notifyDataSetChanged();
    }
}
