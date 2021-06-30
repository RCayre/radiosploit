package laas.rcayre.radiosploit;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.radiosploit.R;

import java.util.ArrayList;

public abstract class PacketListAdapter extends RecyclerView.Adapter<PacketListAdapter.ViewHolder>{
    /* Adapter allowing to manage a list of PacketItemData */
    private ArrayList<PacketItemData> packetList;
    public PacketListAdapter(ArrayList<PacketItemData> list) {
        this.packetList = list;
    }

    public abstract void onItemClick(View view,PacketItemData itemData, int position);
    public abstract void onItemLongClick(View view,PacketItemData itemData, int position);

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View listItem= layoutInflater.inflate(R.layout.packet_view_item, parent, false);
        ViewHolder viewHolder = new ViewHolder(listItem);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final PacketItemData myListData = packetList.get(position);
        holder.descriptionTextView.setText("#"+String.valueOf(position)+"  "+packetList.get(position).getDescription());
        holder.statusTextView.setText(packetList.get(position).getStatus());
        holder.contentTextView.setText(packetList.get(position).getFormattedContent());
        holder.imageView.setImageResource(packetList.get(position).getImgId());

        holder.relativeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /* If there is a short click on a PacketItemData, propagate the event to the corresponding callback */
                PacketListAdapter.this.onItemClick(view,myListData, position);
            }
        });
        holder.relativeLayout.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick (View view){
                    /* If there is a long click on a PacketItemData, propagate the event to the corresponding callback */
                    PacketListAdapter.this.onItemLongClick(view,myListData, position);
                    return true;
                }
        }
        );
    }


    @Override
    public int getItemCount() {
        /* Returns the number of items in the list */
        return packetList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView imageView;
        public TextView descriptionTextView;
        public TextView statusTextView;
        public TextView contentTextView;
        public RelativeLayout relativeLayout;
        public ViewHolder(View itemView) {
            super(itemView);
            this.imageView = (ImageView) itemView.findViewById(R.id.packet_icon);
            this.descriptionTextView = (TextView) itemView.findViewById(R.id.packet_description);
            this.contentTextView = (TextView) itemView.findViewById(R.id.packet_content);
            this.statusTextView = (TextView) itemView.findViewById(R.id.packet_status);
            relativeLayout = (RelativeLayout)itemView.findViewById(R.id.packet_view_item_layout);
        }
    }

    /* Methods allowing to interact with the list items */
    public void addNewData(PacketItemData data){
        packetList.add(data);
        notifyDataSetChanged();
    }

    public void updateStatus(int packetId,String status) {
        packetList.get(packetId).setStatus(status);
        notifyItemChanged(packetId);
    }
    public void updateData(int packetId, PacketItemData data) {
        packetList.get(packetId).update(data);
        notifyItemChanged(packetId);
    }

    public void resetData() {
        packetList.clear();
        notifyDataSetChanged();
    }

    public PacketItemData getData(int position) {
        return packetList.get(position);
    }

}
