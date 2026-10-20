package com.phan.bit240007_foodmanager.ui

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import com.phan.bit240007_foodmanager.util.LocaleHelper

abstract class BaseActivity : AppCompatActivity() {
    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LocaleHelper.onAttach(newBase))
    }
}
