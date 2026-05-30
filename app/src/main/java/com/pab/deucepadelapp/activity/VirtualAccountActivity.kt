package com.pab.deucepadelapp.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.pab.deucepadelapp.R

class VirtualAccountActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Hubungkan ke layout VA yang sudah kita buat sebelumnya
        setContentView(R.layout.activity_virtual_account)
    }
}