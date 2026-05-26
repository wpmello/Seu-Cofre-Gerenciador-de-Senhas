package com.inovalou.seucofregerenciadordesenhas.core.testing

import com.inovalou.seucofregerenciadordesenhas.core.coroutines.AppDispatchers
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher

@OptIn(ExperimentalCoroutinesApi::class)
fun testAppDispatchers(
    dispatcher: CoroutineDispatcher = UnconfinedTestDispatcher()
): AppDispatchers = AppDispatchers(
    default = dispatcher,
    io = dispatcher
)
