package com.example.demo

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Message

class MainActivity : AppCompatActivity() {

    private val handler = @SuppressLint("HandlerLeak")
    object :Handler(){
        @SuppressLint("HandlerLeak")
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val msg = Message.obtain()
        msg.what = 0
        msg.obj = "msg1"
        handler.sendMessage(msg)
        val set =HashSet<Int>()
    }
}