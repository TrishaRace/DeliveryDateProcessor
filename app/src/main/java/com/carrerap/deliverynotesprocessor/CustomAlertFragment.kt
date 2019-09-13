package com.carrerap.deliverynotesprocessor

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

class CustomAlertFragment :Fragment(){
    private lateinit var text :String

    companion object {
        fun newInstance(text : String):CustomAlertFragment{
            val fragment = CustomAlertFragment()
            val args = Bundle()
            args.putString("text", text)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        text = arguments!!.getString("text")
    }
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_custom_alert, container, false)
    }
}