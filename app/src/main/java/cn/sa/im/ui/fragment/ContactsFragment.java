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
        adapter.setOnItemClickListener(new UserAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                if(userList.get(position).getTagid()!=null){
                    //首先需要构造使用客服者的用户信息
                    CSCustomServiceInfo.Builder csBuilder = new CSCustomServiceInfo.Builder();
                    CSCustomServiceInfo csInfo = csBuilder.nickName("融云").referrer("20001").build();
                    RongIM.getInstance().startCustomerServiceChat(getActivity(), "service", "在线客服",csInfo);

                }else {
                    RongIM.getInstance().startConversation(ContactsFragment.this.getActivity(), Conversation.ConversationType.PRIVATE, userList.get(position).getPhone(), userList.get(position).getUserName());
                }
            }
            @Override
            public void onItemLongClick(View view, int position) {

            }
        });
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
        User user1 = new User(getString(R.string.sa_one), "10003");
        User user2 = new User(getString(R.string.sa_two), "10002");
        User kean = new User("客服","10001");
        kean.setTagid("10001");
        userList.add(user1);
        userList.add(user2);
        userList.add(kean);
        adapter.notifyDataSetChanged();
    }
}
