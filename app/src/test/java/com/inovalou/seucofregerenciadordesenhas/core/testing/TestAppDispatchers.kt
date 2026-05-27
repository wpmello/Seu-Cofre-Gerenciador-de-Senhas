package com.inovalou.seucofregerenciadordesenhas.core.testing

import com.inovalou.seucofregerenciadordesenhas.core.coroutines.AppDispatchers
import kotlin.coroutines.CoroutineContext
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

class RecordingDispatcher : CoroutineDispatcher() {
    var isRunning: Boolean = false
        private set
    var dispatchCount: Int = 0
        private set

    override fun dispatch(context: CoroutineContext, block: Runnable) {
        dispatchCount += 1
        isRunning = true
        try {
            block.run()
        } finally {
            isRunning = false
        }
    }
}
