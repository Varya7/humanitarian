package com.example.hum1.maps

import android.content.Context
import com.yandex.mapkit.MapKitFactory

/**
 * Единая точка инициализации Yandex MapKit.
 * MapKit нельзя безопасно инициализировать отдельно в каждой активности,
 * поэтому все карты проходят через этот объект.
 */
object YandexMapKitInitializer {
    private const val API_KEY = "3c89017d-c56c-4694-b14e-3085f7402ed4"
    private var initialized = false

    @Synchronized
    fun init(context: Context) {
        if (initialized) return
        MapKitFactory.setApiKey(API_KEY)
        MapKitFactory.initialize(context.applicationContext)
        initialized = true
    }
}
