package com.inovalou.seucofregerenciadordesenhas.core.coroutines

import kotlinx.coroutines.CoroutineDispatcher

data class AppDispatchers(
    val default: CoroutineDispatcher,
    val io: CoroutineDispatcher
)
