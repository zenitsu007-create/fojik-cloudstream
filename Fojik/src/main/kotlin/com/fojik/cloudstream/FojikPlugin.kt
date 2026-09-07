package com.fojik.cloudstream

import com.lagradost.cloudstream3.plugins.CloudstreamPlugin
import com.lagradost.cloudstream3.plugins.Plugin

@CloudstreamPlugin
class FojikPlugin : Plugin() {
    override fun load(context: android.content.Context) {
        registerMainAPI(FojikProvider())
    }
} 
