package com.cookandroid.myapplication;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class NoticePagerAdapter extends RecyclerView.Adapter<NoticePagerAdapter.NoticeViewHolder> {

    private List<function_main.Notice> notices;

    public NoticePagerAdapter(List<function_main.Notice> notices) {
        this.notices = notices;
    }

    @NonNull
    @Override
    public NoticeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.notice_item, parent, false);
        return new NoticeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NoticeViewHolder holder, int position) {
        function_main.Notice notice = notices.get(position);
        holder.noticeImage.setImageResource(notice.getImageResource());
        holder.noticeImage1.setImageResource(notice.getImageResource());
        holder.noticeImage2.setImageResource(notice.getImageResource());
    }

    @Override
    public int getItemCount() {
        return notices.size();
    }

    public static class NoticeViewHolder extends RecyclerView.ViewHolder {
        public ImageView noticeImage;
        public ImageView noticeImage1;
        public ImageView noticeImage2;

        public NoticeViewHolder(View itemView) {
            super(itemView);
            noticeImage = itemView.findViewById(R.id.noticeImage);
            noticeImage1 = itemView.findViewById(R.id.noticeImage1);
            noticeImage2 = itemView.findViewById(R.id.noticeImage2);
        }
    }
}