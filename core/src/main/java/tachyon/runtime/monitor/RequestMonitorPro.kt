package tachyon.runtime.monitor

import java.io.IOException

interface RequestMonitorPro : RequestMonitor {
    @Throws(IOException::class)
    fun init(pc: PageContext?)
}