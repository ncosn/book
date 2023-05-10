package cn.njupt.iot.b19060226.book;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import java.util.List;

public class TestAdapter extends RecyclerView.Adapter<TestAdapter.BaseViewHolder> {

    private Activity context;
    private List<TestData> dataList;
    private RoomDB database;

    public TestAdapter(Activity context, List<TestData> dataList) {
        this.context = context;
        this.dataList = dataList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public TestAdapter.BaseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        BaseViewHolder viewHolder;
        view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_row_main, parent, false);
        viewHolder = new BaseViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull TestAdapter.BaseViewHolder holder, int position) {
        BaseViewHolder baseViewHolder = holder;
        TestData data = dataList.get(position);
        database = RoomDB.getInstance(context);
        baseViewHolder.tvTime.setText(data.getTime());
        baseViewHolder.tvContent.setText(data.getQuestion());
    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }

    public class BaseViewHolder extends RecyclerView.ViewHolder {
        TextView tvTime,tvSearch, tvContent;

        public BaseViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTime = itemView.findViewById(R.id.tv_time);
            tvContent = itemView.findViewById(R.id.tv_content);
            tvSearch = itemView.findViewById(R.id.tv_search);
        }
    }
}
