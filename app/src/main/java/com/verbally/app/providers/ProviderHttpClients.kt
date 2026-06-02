package com.verbally.app.providers

import okhttp3.OkHttpClient

internal object ProviderHttpClients {
    val shared: OkHttpClient = OkHttpClient()
}
