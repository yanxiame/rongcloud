package cn.sa.im.ui.apadper;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import cn.sa.im.R;
import cn.sa.im.ui.User;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserHolder> {

    private List<User> userList;

    private LayoutInflater inflater;

    public UserAdapter(Context context) {
        inflater = LayoutInflater.from(context);
        userList = new ArrayList<>();
    }

    @Override
    public UserHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.item_user, parent, false);
        return new UserHolder(view);
    }

    @Override
    public void onBindViewHolder(UserHolder holder, int position) {
        holder.tv_userName.setText(userList.get(position).getUserName());
        holder.tv_phone.setText(userList.get(position).getPhone());
    }

    public void setData(List<User> userList) {
        this.userList.clear();
        this.userList = userList;
    }

    public int getFirstPositionByChar(char sign) {
        if (sign == '#') {
            return 0;
        }
        for (int i = 0; i < userList.size(); i++) {
            if (userList.get(i).getHeadLetter() == sign) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    class UserHolder extends RecyclerView.ViewHolder {

        public TextView tv_userName;

        public TextView tv_phone;

        public UserHolder(View itemView) {
            super(itemView);
            tv_userName = (TextView) itemView.findViewById(R.id.tv_userName);
            tv_phone = (TextView) itemView.findViewById(R.id.tv_phone);
        }
    }

}