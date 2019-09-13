package com.carrerap.deliverynotesprocessor.DeliveryNote

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.carrerap.deliverynotesprocessor.R
import kotlinx.android.synthetic.main.fragment_list_delivery_note.*

class DeliveryNoteListFragment : Fragment(){
    var resultList : ArrayList<String> = ArrayList()
    var deliveryNoteList : ArrayList<DeliveryNote> = ArrayList()

        companion object {
        fun newInstance(resultList: ArrayList<String>): DeliveryNoteListFragment {
            val fragment = DeliveryNoteListFragment()
            val args = Bundle()
            args.putStringArrayList("resultList", resultList)
            fragment.arguments = args
            return fragment
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        resultList = arguments!!.getStringArrayList("resultList")
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_list_delivery_note, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadDeliveryNotes()
    }

    fun loadDeliveryNotes() {
        resultList.forEach { result ->
            organizeInfo(result)
            deliveryNoteList.add(DeliveryNote(1, result, 3, 4))
        }

        rvDeliveryNoteItems.layoutManager = LinearLayoutManager(this.context, RecyclerView.VERTICAL, false)
        rvDeliveryNoteItems.adapter = DeliveryNoteAdapter(deliveryNoteList) {}
    }

    fun organizeInfo(result: String){

    }

}
