package com.shivora.puwifimanager.model.adapters;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.shivora.puwifimanager.model.database.UserEntry;
import com.shivora.puwifimanager.R;

import java.util.List;

public class UserListAdapter extends RecyclerView.Adapter {
    private Context context;
    private List<UserEntry> userList;
    private ListItemClickListener itemClickListener;

    public UserListAdapter(List<UserEntry> userList, ListItemClickListener itemClickListener){
        this.userList = userList;
        this.itemClickListener = itemClickListener;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_user_card,parent,false);
        return new UserListViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ((UserListViewHolder) holder).bind(position);
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public void setUserList(List<UserEntry> userList){
        this.userList = userList;
        notifyDataSetChanged();
    }

    class UserListViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        private TextView tvUserNickName;
        private TextView tvUserId;

        public UserListViewHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            tvUserNickName = itemView.findViewById(R.id.tv_user_nickname);
            tvUserId = itemView.findViewById(R.id.tv_user_id);
        }

        public void bind(int position){
            UserEntry user = userList.get(position);
            tvUserNickName.setText(user.getNickname());
            tvUserId.setText(user.getUserId());
        }

        @Override
        public void onClick(View v) {
            int position = getAdapterPosition();
            UserEntry user = userList.get(position);
            itemClickListener.onListItemClick(user);
        }
    }
}
