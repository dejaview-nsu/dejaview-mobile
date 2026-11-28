package ru.dejaview.app

import android.app.Application
import android.content.Context
import androidx.test.runner.AndroidJUnitRunner

class DejaViewTestRunner : AndroidJUnitRunner() {
    override fun callApplicationOnCreate(app: Application) {
        super.callApplicationOnCreate(app)
        app.getSharedPreferences(STARTUP_PREFERENCES, Context.MODE_PRIVATE)
            .edit().putBoolean(INTRODUCTION_COMPLETED, true).commit()
    }
}
