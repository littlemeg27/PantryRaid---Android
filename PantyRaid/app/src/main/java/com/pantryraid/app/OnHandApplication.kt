package com.onhand.app

import android.app.Application
import com.onhand.app.data.InventoryRepository

class OnHandApplication : Application() {
    lateinit var repository: InventoryRepository
        private set

    override fun onCreate() {
        super.onCreate()
        repository = InventoryRepository(this)
    }
}
