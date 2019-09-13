package com.carrerap.deliverynotesprocessor.DeliveryNote

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.carrerap.deliverynotesprocessor.R

class DeliveryNoteAdapter(var deliveryNoteList : ArrayList<DeliveryNote>, clickListener: (DeliveryNote)->Unit) : RecyclerView.Adapter<DeliveryNoteAdapter.ViewHolder>(){
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_delivery_note, parent, false))
    }

    override fun getItemCount(): Int {
        return deliveryNoteList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindItems(deliveryNoteList[position],clickListener)
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_delivery_note, parent, false))
        }

        override fun getItemCount(): Int {
            return deliveryNoteList.size
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.bindItems(deliveryNoteList[position],clickListener)
        }
    }
}