package lucee.runtime.schedule

import lucee.runtime.schedule.ScheduleTask

// FUTURE add to ScheduleTask and delete
interface ScheduleTaskPro : ScheduleTask {
    /**
     * @return Returns the userAgent.
     */
    fun getUserAgent(): String?
}