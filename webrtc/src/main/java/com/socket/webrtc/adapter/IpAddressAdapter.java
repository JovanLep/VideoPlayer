package com.socket.webrtc.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.socket.webrtc.R;

import java.util.List;

public class IpAddressAdapter extends RecyclerView.Adapter<IpAddressAdapter.IpHolder> {

    private List<String> ipList;
    private Context context;

    public IpAddressAdapter(Context context, List<String> ipList) {
        this.context = context;
        this.ipList = ipList;
    }

    @NonNull
    @Override
    public IpHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_ip_address, parent, false);
        return new IpHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull IpHolder holder, int position) {
        holder.tvAddress.setText(ipList.get(position));
    }

    @Override
    public int getItemCount() {
        return ipList.size();
    }

    static class IpHolder extends RecyclerView.ViewHolder {
        private final TextView tvAddress;

        public IpHolder(@NonNull View itemView) {
            super(itemView);
            tvAddress = itemView.findViewById(R.id.tv_address);
        }
    }
}
