/**
 *
 * Copyright (c) 2014, the Railo Company Ltd. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library.  If not, see <http:></http:>//www.gnu.org/licenses/>.
 *
 */
package lucee.runtime.schedule

import java.util.ArrayList

class ScheduledTaskThread(engine: CFMLEngineImpl?, scheduler: Scheduler?, task: ScheduleTask?) : ParentThreasRefThread() {
    // private Calendar calendar;
    val start: Long
    private val startDate: Long
    private val startTime: Long
    private val endDate: Long
    private val endTime: Long
    private var intervall: Int
    private var amount = 0
    private var stop = false
    private val util: DateTimeUtil?

    // private int cIntervall;
    private val task: ScheduleTask?
    private val engine: CFMLEngineImpl?
    private val timeZone: TimeZone?
    private val scheduler: SchedulerImpl?
    private var exeThreads: List<ExecutionThread?>? = ArrayList<ExecutionThread?>()
    private var exeThread: ExecutionThread? = null
    private val unique: Boolean
    private val config: Config?
    fun setStop(stop: Boolean) {
        this.stop = stop
    }

    fun stopIt() {
        setStop(true)
        val log: Log = ThreadLocalPageContext.getLog(scheduler!!.getConfig(), "scheduler")
        log.info("scheduler", "stopping task thread [" + task.getTask().toString() + "]")
        if (unique) {
            stop(log, exeThread)
        } else {
            val it: Iterator<ExecutionThread?> = exeThreads!!.iterator()
            while (it.hasNext()) {
                stop(log, it.next())
            }
            cleanThreads()
        }

        // stop this thread itself
        SystemUtil.notify(this)
        if (this.isAlive()) SystemUtil.sleep(1)
        SystemUtil.stop(this)
        if (this.isAlive()) log.log(Log.LEVEL_WARN, "scheduler", "task [" + task.getTask().toString() + "] could not be stopped.", ExceptionUtil.toThrowable(this.getStackTrace())) else log.info("scheduler", "task [" + task.getTask().toString() + "] stopped")
    }

    private fun stop(log: Log?, et: ExecutionThread?) {
        if (exeThread != null) SystemUtil.stop(exeThread)
        if (et != null && et.isAlive()) log.log(Log.LEVEL_WARN, "scheduler", "task thread [" + task.getTask().toString() + "] could not be stopped.", ExceptionUtil.toThrowable(et.getStackTrace())) else log.info("scheduler", "task thread [" + task.getTask().toString() + "] stopped")
    }

    @Override
    fun run() {
        if (ThreadLocalPageContext.getConfig() == null && config != null) ThreadLocalConfig.register(config)
        try {
            _run()
        } catch (e: Exception) {
            addParentStacktrace(e)
            log(Log.LEVEL_ERROR, e)
            if (e is RuntimeException) throw e as RuntimeException
            throw RuntimeException(e)
        } finally {
            log(Log.LEVEL_INFO, "ending task")
            task.setValid(false)
            try {
                scheduler!!.removeIfNoLonerValid(task)
            } catch (e: Exception) {
            }
        }
    }

    fun _run() {

        // check values
        if (startDate > endDate) {
            log(Log.LEVEL_ERROR, "Invalid task definition: enddate is before startdate")
            return
        }
        if (intervall == ScheduleTaskImpl.INTERVAL_EVEREY && startTime > endTime) {
            log(Log.LEVEL_ERROR, "Invalid task definition: endtime is before starttime")
            return
        }
        var today: Long = System.currentTimeMillis()
        var execution: Long
        val isOnce = intervall == ScheduleTask.INTERVAL_ONCE
        execution = if (isOnce) {
            if (startDate + startTime < today) {
                log(Log.LEVEL_INFO, "not executing task because single execution was in the past")
                return
            }
            startDate + startTime
        } else calculateNextExecution(today, false)
        log(Log.LEVEL_INFO, "First execution")
        while (true) {
            sleepEL(execution, today)
            if (stop) break
            if (!engine.isRunning()) {
                log(Log.LEVEL_ERROR, "Engine is not running")
                break
            }
            today = System.currentTimeMillis()
            val todayTime: Long = util.getMilliSecondsInDay(null, today)
            val todayDate = today - todayTime
            if (!task.isValid()) {
                log(Log.LEVEL_ERROR, "Task is not valid")
                break
            }
            if (!task.isPaused()) {
                if (endDate < todayDate && endTime < todayTime) {
                    log(Log.LEVEL_ERROR, String.format("End date %s has passed; now: %s", DateTimeUtil.format(endDate + endTime, null, timeZone),
                            DateTimeUtil.format(todayDate + todayTime, null, timeZone)))
                    break
                }
                execute()
            }
            if (isOnce) {
                log(Log.LEVEL_INFO, "ending task after a single execution")
                break
            }
            today = System.currentTimeMillis()
            execution = calculateNextExecution(today, true)
            if (!task.isPaused()) log(Log.LEVEL_DEBUG, "next execution runs at " + DateTimeUtil.format(execution, null, timeZone))
            // sleep=execution-today;
        }
    }

    private fun log(level: Int, msg: String?) {
        try {
            val logName = "schedule task:" + task.getTask()
            ThreadLocalPageContext.getLog(scheduler!!.getConfig(), "scheduler").log(level, logName, msg)
        } catch (e: Exception) {
            System.err.println(msg)
            System.err.println(e)
        }
    }

    private fun log(level: Int, e: Exception?) {
        try {
            val logName = "schedule task:" + task.getTask()
            ThreadLocalPageContext.getLog(scheduler!!.getConfig(), "scheduler").log(level, logName, e)
        } catch (ee: Exception) {
            LogUtil.logGlobal(config, "scheduler", e)
            LogUtil.logGlobal(config, "scheduler", ee)
        }
    }

    private fun sleepEL(`when`: Long, now: Long) {
        var millis = `when` - now
        try {
            if (millis > 0) {
                while (true) {
                    SystemUtil.wait(this, millis)
                    if (stop) break
                    millis = `when` - System.currentTimeMillis()
                    if (millis <= 0) break
                    millis = 10
                }
            }
        } catch (e: Exception) {
            log(Log.LEVEL_ERROR, e)
        }
    }

    private fun execute() {
        if (scheduler!!.getConfig() != null) {
            // unique
            if (unique && exeThread != null && exeThread.isAlive()) {
                return
            }
            val et = ExecutionThread(scheduler!!.getConfig(), task, scheduler!!.getCharset())
            et.start()
            if (unique) {
                exeThread = et
            } else {
                cleanThreads()
                exeThreads.add(et)
            }
        }
    }

    private fun cleanThreads() {
        val list: List<ExecutionThread?> = ArrayList<ExecutionThread?>()
        val it: Iterator<ExecutionThread?> = exeThreads!!.iterator()
        var et: ExecutionThread?
        while (it.hasNext()) {
            et = it.next()
            if (et.isAlive()) list.add(et)
        }
        exeThreads = list
    }

    private fun calculateNextExecution(now: Long, notNow: Boolean): Long {
        return if (intervall == ScheduleTaskImpl.INTERVAL_EVEREY) calculateNextExecutionEvery(util, now, notNow, timeZone, start, endTime, amount) else calculateNextExecutionNotEvery(util, now, notNow, timeZone, start, intervall)
    }

    fun getConfig(): Config? {
        return scheduler!!.getConfig()
    }

    fun getTask(): ScheduleTask? {
        return task
    }

    companion object {
        private const val DAY = (24 * 3600000).toLong()
        fun calculateNextExecutionNotEvery(util: DateTimeUtil?, now: Long, notNow: Boolean, timeZone: TimeZone?, start: Long, intervall: Int): Long {
            var intType = 0
            if (intervall == ScheduleTaskImpl.INTERVAL_DAY) intType = Calendar.DAY_OF_MONTH else if (intervall == ScheduleTaskImpl.INTERVAL_WEEK) intType = Calendar.WEEK_OF_YEAR else if (intervall == ScheduleTaskImpl.INTERVAL_MONTH) intType = Calendar.MONTH else if (intervall == ScheduleTaskImpl.INTERVAL_YEAR) intType = Calendar.YEAR
            val c: Calendar = JREDateTimeUtil.getThreadCalendar(timeZone)

            // get the current years, so we only have to search this year
            c.setTimeInMillis(now)
            val nowYear: Int = c.get(Calendar.YEAR)

            // extract the time in day info (we do not seconds in day to avoid DST issues)
            c.setTimeInMillis(start)
            val startDOW: Int = c.get(Calendar.DAY_OF_WEEK)
            val startDOM: Int = c.get(Calendar.DAY_OF_MONTH)
            val startMonth: Int = c.get(Calendar.MONTH)
            if (c.get(Calendar.YEAR) < nowYear) {
                c.set(Calendar.YEAR, nowYear)
                c.set(Calendar.MONTH, 0)
                c.set(Calendar.DAY_OF_MONTH, 1)
            }
            val startHour: Int = c.get(Calendar.HOUR_OF_DAY)
            val startMinute: Int = c.get(Calendar.MINUTE)
            val startSecond: Int = c.get(Calendar.SECOND)
            val startMilliSecond: Int = c.get(Calendar.MILLISECOND)
            var next: Long = c.getTimeInMillis()

            // weekly
            if (intervall == ScheduleTaskImpl.INTERVAL_WEEK) {
                var update = false
                while (c.get(Calendar.DAY_OF_WEEK) !== startDOW) {
                    c.add(Calendar.DAY_OF_YEAR, 1)
                    update = true
                }
                if (update) next = c.getTimeInMillis()
            } else if (intervall == ScheduleTaskImpl.INTERVAL_MONTH) {
                var update = false
                while (c.get(Calendar.DAY_OF_MONTH) !== startDOM) {
                    c.add(Calendar.DAY_OF_YEAR, 1)
                    update = true
                }
                if (update) next = c.getTimeInMillis()
            } else if (intervall == ScheduleTaskImpl.INTERVAL_YEAR) {
                var update = false
                while (c.get(Calendar.MONTH) !== startMonth) {
                    c.add(Calendar.MONTH, 1)
                    update = true
                }
                while (c.get(Calendar.DAY_OF_MONTH) !== startDOM) {
                    c.add(Calendar.DAY_OF_YEAR, 1)
                    update = true
                }
                if (update) next = c.getTimeInMillis()
            }

            // is it already in the future or we want not now
            while (next <= now) {
                // we allow now
                if (!notNow) {
                    val diff = now - next
                    if (diff >= 0 && diff < 1000) break
                }
                c.add(intType, 1)
                c.set(Calendar.HOUR_OF_DAY, startHour)
                c.set(Calendar.MINUTE, startMinute)
                c.set(Calendar.SECOND, startSecond)
                c.set(Calendar.MILLISECOND, startMilliSecond)

                // Daylight saving time
                if (c.get(Calendar.HOUR_OF_DAY) !== startHour) {
                    c.add(intType, 1)
                    c.set(Calendar.HOUR_OF_DAY, startHour)
                    c.set(Calendar.MINUTE, startMinute)
                    c.set(Calendar.SECOND, startSecond)
                    c.set(Calendar.MILLISECOND, startMilliSecond)
                }
                next = c.getTimeInMillis()
            }
            return next
        }

        // public static void main(String[] args) {
        // long start = 1604217661000L; // Sunday, November 1, 2020 9:01:01 AM CET
        // long now = 1610704861000L; // Friday, January 1, 2021 11:01:01 AM CET
        // long end = 4759891261000L; // Friday, January 1, 2021 11:01:01 AM CET
        // long next = calculateNextExecutionNotEvery(DateTimeUtil.getInstance(), now, true,
        // TimeZone.getDefault(), start, ScheduleTaskImpl.INTERVAL_HOUR);
        // long next = calculateNextExecutionEvery(DateTimeUtil.getInstance(), now, true,
        // TimeZone.getDefault(), start, end, 30);
        // print.e("start: " + java.text.DateFormat.getDateTimeInstance(java.text.DateFormat.FULL,
        // java.text.DateFormat.FULL, Locale.getDefault()).format(new Date(start)));
        // print.e("now: " + java.text.DateFormat.getDateTimeInstance(java.text.DateFormat.FULL,
        // java.text.DateFormat.FULL, Locale.getDefault()).format(new Date(now)));
        // print.e("next: " + java.text.DateFormat.getDateTimeInstance(java.text.DateFormat.FULL,
        // java.text.DateFormat.FULL, Locale.getDefault()).format(new Date(next)));
        // print.e(next);
        // }
        fun calculateNextExecutionEvery(util: DateTimeUtil?, now: Long, notNow: Boolean, timeZone: TimeZone?, start: Long, endTime: Long, amount: Int): Long {
            val c: Calendar = JREDateTimeUtil.getThreadCalendar(timeZone)
            // print.e("----------------------------------");
            // print.e("now:" + new Date(now));
            // print.e("start:" + new Date(start));
            // print.e(amount);
            // get the current years, so we only have to search this year

            // extract the time in day info (we do not seconds in day to avoid DST issues)
            c.setTimeInMillis(start)
            val startHour: Int = c.get(Calendar.HOUR_OF_DAY)
            val startMinute: Int = c.get(Calendar.MINUTE)
            val startSecond: Int = c.get(Calendar.SECOND)
            val startMilliSecond: Int = c.get(Calendar.MILLISECOND)

            // set to midnight
            c.setTimeInMillis(now)
            c.set(Calendar.HOUR_OF_DAY, 0)
            c.set(Calendar.MINUTE, 0)
            c.set(Calendar.SECOND, 0)
            c.set(Calendar.MILLISECOND, 0)
            c.setTimeInMillis(c.getTimeInMillis() + endTime)
            val end: Long = c.getTimeInMillis()
            // print.e("end:" + c.getTime());
            c.setTimeInMillis(now)
            c.set(Calendar.HOUR_OF_DAY, startHour)
            revertDST(c, startHour, Calendar.SECOND, amount)
            c.set(Calendar.MINUTE, startMinute)
            c.set(Calendar.SECOND, startSecond)
            c.set(Calendar.MILLISECOND, startMilliSecond)
            var next: Long = c.getTimeInMillis()
            // print.e("start:" + new Date(next));

            // is it already in the future or we want not now
            while (next <= now) {
                // we allow now
                if (!notNow) {
                    val diff = now - next
                    if (diff >= 0 && diff < 1000) break
                }
                c.add(Calendar.SECOND, amount)
                next = c.getTimeInMillis()
                // print.e("- " + c.getTime());
                // we reach end so we set it to start tomorrow
                if (next > end) {
                    c.setTimeInMillis(now)
                    c.set(Calendar.HOUR_OF_DAY, startHour)
                    c.set(Calendar.MINUTE, startMinute)
                    c.set(Calendar.SECOND, startSecond)
                    c.set(Calendar.MILLISECOND, startMilliSecond)
                    c.add(Calendar.DAY_OF_MONTH, 1)
                    // print.e("next0:" + c.getTime());
                    return c.getTimeInMillis()
                }
            }
            // print.e("next2:" + new Date(next));
            return next
        }

        private fun revertDST(c: Calendar?, hourExpected: Int, intervall: Int, amount: Int) {
            var hour: Int = c.get(Calendar.HOUR_OF_DAY)
            if (hour == hourExpected) return
            // go back until it shifts
            while (true) {
                // print.e("- " + c.getTime());
                c.add(intervall, -amount)
                hour = c.get(Calendar.HOUR_OF_DAY)
                if (hour <= hourExpected) {
                    c.add(intervall, amount)
                    break
                }
            }
        }

        fun getMilliSecondsInDay(c: Calendar?): Long {
            return c.get(Calendar.HOUR_OF_DAY) * 3600000 + c.get(Calendar.MINUTE) * 60000 + c.get(Calendar.SECOND) * 1000 + c.get(Calendar.MILLISECOND)
        }
    }

    init {
        util = DateTimeUtil.getInstance()
        this.engine = engine
        this.scheduler = scheduler
        this.task = task
        timeZone = ThreadLocalPageContext.getTimeZone(this.scheduler!!.getConfig())
        start = Caster.toTime(task.getStartDate(), task.getStartTime(), timeZone)
        startDate = util.getMilliSecondsAdMidnight(timeZone, start)
        startTime = util.getMilliSecondsInDay(timeZone, start)
        endDate = if (task.getEndDate() == null) Long.MAX_VALUE else util.getMilliSecondsAdMidnight(timeZone, task.getEndDate().getTime())
        endTime = if (task.getEndTime() == null) DAY else util.getMilliSecondsInDay(timeZone, task.getEndTime().getTime())
        unique = (task as ScheduleTaskImpl?)!!.unique()
        intervall = task.getInterval()
        if (intervall >= 10) {
            amount = intervall
            intervall = ScheduleTaskImpl.INTERVAL_EVEREY
        } else amount = 1

        // cIntervall = toCalndarIntervall(intervall);
        config = ThreadLocalPageContext.getConfig(this.scheduler!!.getConfig())
    }
}