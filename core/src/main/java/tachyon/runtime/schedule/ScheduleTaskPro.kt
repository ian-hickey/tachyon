package tachyon.runtime.schedule

import tachyon.runtime.schedule.ScheduleTask

// FUTURE add to ScheduleTask and delete
interface ScheduleTaskPro : ScheduleTask {
    /**
     * @return Returns the userAgent.
     */
    fun getUserAgent(): String?
}