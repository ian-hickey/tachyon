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

import java.io.IOException

/**
 * Define a single schedule Task
 */
class ScheduleTaskImpl(scheduler: Scheduler?, task: String?, file: Resource?, startDate: Date?, startTime: Time?, endDate: Date?, endTime: Time?, url: String?, port: Int, interval: String?,
                       timeout: Long, credentials: Credentials?, proxy: ProxyData?, resolveURL: Boolean, publish: Boolean, hidden: Boolean, readonly: Boolean, paused: Boolean, autoDelete: Boolean,
                       unique: Boolean, userAgent: String?) : ScheduleTaskPro {
    private val task: String?
    private val operation: Short = OPERATION_HTTP_REQUEST
    private val file: Resource?
    private val startDate: Date?
    private val startTime: Time?
    private val url: URL?
    private val endDate: Date?
    private val endTime: Time?
    private val interval: Int
    private val timeout: Long
    private val credentials: Credentials?
    private val proxy: ProxyData?
    private var userAgent: String?
    private val resolveURL: Boolean
    private var nextExecution: Long = 0
    private val strInterval: String?
    private val publish: Boolean
    private var valid = true
    private var hidden: Boolean
    private var readonly: Boolean
    private var paused: Boolean
    private var autoDelete: Boolean
    private val md5: String?
    private var thread: ScheduledTaskThread? = null
    private val scheduler: Scheduler?
    private var unique: Boolean
    @Override
    fun getCredentials(): Credentials? {
        return credentials
    }

    @Override
    fun hasCredentials(): Boolean {
        return credentials != null
    }

    @Override
    fun getResource(): Resource? {
        return file
    }

    @Override
    fun getInterval(): Int {
        return interval
    }

    @Override
    fun getOperation(): Short {
        return operation
    }

    @Override
    fun getProxyData(): ProxyData? {
        return proxy
    }

    @Override
    override fun getUserAgent(): String? {
        return userAgent
    }

    @Override
    fun isResolveURL(): Boolean {
        return resolveURL
    }

    @Override
    fun getTask(): String? {
        return task
    }

    @Override
    fun getTimeout(): Long {
        return timeout
    }

    @Override
    fun getUrl(): URL? {
        return url
    }

    @Override
    fun setNextExecution(nextExecution: Long) {
        this.nextExecution = nextExecution
    }

    @Override
    fun getNextExecution(): Long {
        return nextExecution
    }

    @Override
    fun getEndDate(): Date? {
        return endDate
    }

    @Override
    fun getStartDate(): Date? {
        return startDate
    }

    @Override
    fun getEndTime(): Time? {
        return endTime
    }

    @Override
    fun getStartTime(): Time? {
        return startTime
    }

    @Override
    fun getIntervalAsString(): String? {
        return strInterval
    }

    @Override
    fun getStringInterval(): String? {
        return strInterval
    }

    @Override
    fun isPublish(): Boolean {
        return publish
    }

    @Override
    fun isValid(): Boolean {
        return valid
    }

    @Override
    fun setValid(valid: Boolean) {
        this.valid = valid
    }

    /**
     * @return the hidden
     */
    @Override
    fun isHidden(): Boolean {
        return hidden
    }

    /**
     * @param hidden the hidden to set
     */
    @Override
    fun setHidden(hidden: Boolean) {
        this.hidden = hidden
    }

    /**
     * @return the readonly
     */
    fun isReadonly(): Boolean {
        return readonly
    }

    /**
     * @param readonly the readonly to set
     */
    fun setReadonly(readonly: Boolean) {
        this.readonly = readonly
    }

    @Override
    fun isPaused(): Boolean {
        return paused
    }

    fun setPaused(paused: Boolean) {
        this.paused = paused
    }

    fun isAutoDelete(): Boolean {
        return autoDelete
    }

    fun setAutoDelete(autoDelete: Boolean) {
        this.autoDelete = autoDelete
    }

    fun setUnique(unique: Boolean) {
        this.unique = unique
    }

    fun setUserAgent(userAgent: String?) {
        this.userAgent = userAgent
    }

    fun md5(): String? {
        return md5
    }

    fun startIfNecessary(engine: CFMLEngineImpl?) {
        if (thread != null) {
            if (thread.isAlive()) {
                if (thread.getState() === State.BLOCKED) {
                    ThreadLocalPageContext.getLog((scheduler as SchedulerImpl?)!!.getConfig(), "scheduler").info("scheduler", "thread is blocked")
                    SystemUtil.stop(thread)
                } else if (thread.getState() !== State.TERMINATED) {
                    return  // existing is still fine, so nothing to start
                }
            }
            ThreadLocalPageContext.getLog((scheduler as SchedulerImpl?)!!.getConfig(), "scheduler").info("scheduler", "Thread needs a restart (" + thread.getState().name().toString() + ")")
        }
        thread = ScheduledTaskThread(engine, scheduler, this)
        setValid(true)
        thread.start()
    }

    fun stop() {
        val log: Log = ThreadLocalPageContext.getLog((scheduler as SchedulerImpl?)!!.getConfig(), "scheduler")
        log.info("scheduler", "stopping task [" + getTask() + "]")
        if (thread == null || !thread.isAlive()) {
            log.info("scheduler", "task [" + getTask() + "] was not running")
            return
        }
        thread.stopIt()
    }

    fun unique(): Boolean {
        return unique
    }

    fun getScheduler(): Scheduler? {
        return scheduler
    }

    fun log(level: Int, msg: String?) {
        val logName = "schedule task:$task"
        ThreadLocalPageContext.getLog((scheduler as SchedulerImpl?)!!.getConfig(), "scheduler").log(level, logName, msg)
    }

    fun log(level: Int, msg: String?, t: Throwable?) {
        val logName = "schedule task:$task"
        ThreadLocalPageContext.getLog((scheduler as SchedulerImpl?)!!.getConfig(), "scheduler").log(level, logName, msg, t)
    }

    companion object {
        const val INTERVAL_EVEREY = -1
        const val INTERVAL_YEAR = 4

        /**
         * translate a String interval definition to an int definition
         *
         * @param interval
         * @return interval
         * @throws ScheduleException
         */
        @Throws(ScheduleException::class)
        private fun toInterval(interval: String?): Int {
            var interval = interval
            interval = interval.trim().toLowerCase()
            val i: Int = Caster.toIntValue(interval, 0)
            if (i == 0) {
                interval = interval.trim()
                if (interval.equals("once")) return INTERVAL_ONCE else if (interval.equals("daily")) return INTERVAL_DAY else if (interval.equals("day")) return INTERVAL_DAY else if (interval.equals("monthly")) return INTERVAL_MONTH else if (interval.equals("month")) return INTERVAL_MONTH else if (interval.equals("weekly")) return INTERVAL_WEEK else if (interval.equals("week")) return INTERVAL_WEEK
                throw ScheduleException("invalid interval definition [$interval], valid values are [once, daily, monthly, weekly or number]")
            }
            if (i < 1) {
                throw ScheduleException("interval must be at least 1")
            }
            return i
        }

        /**
         * translate a urlString and a port definition to a URL Object
         *
         * @param url URL String
         * @param port URL Port Definition
         * @return returns a URL Object
         * @throws MalformedURLException
         */
        @Throws(MalformedURLException::class)
        private fun toURL(url: String?, port: Int): URL? {
            val u: URL = HTTPUtil.toURL(url, HTTPUtil.ENCODED_AUTO)
            return if (port == -1) u else URL(u.getProtocol(), u.getHost(), port, u.getFile())
        }
    }

    /**
     * constructor of the class
     *
     * @param task Task name
     * @param file Output File
     * @param startDate Start Date
     * @param startTime Start Time
     * @param endDate
     * @param endTime
     * @param url URL to invoke
     * @param port Port of the URL to invoke
     * @param interval interval of the job
     * @param timeout request timeout in miilisconds
     * @param credentials username and password for the request
     * @param proxyHost
     * @param proxyPort
     * @param proxyCredentials proxy username and password
     * @param userAgent
     * @param resolveURL resolve links in the output page to absolute references or not
     * @param publish
     * @throws IOException
     * @throws ScheduleException
     */
    init {
        var file: Resource? = file
        this.scheduler = scheduler
        var md5: String = (task.toLowerCase() + file + startDate + startTime + endDate + endTime + url + port + interval + timeout + credentials + proxy + resolveURL + publish + hidden
                + readonly + paused + unique + userAgent)
        md5 = Md5.getDigestAsString(md5)
        this.md5 = md5
        if (file != null && file.toString().trim().length() > 0) {
            // is it a file?
            if (file.exists() && !file.isFile()) {
                ThreadLocalPageContext.getLog((scheduler as SchedulerImpl?)!!.getConfig(), "scheduler").error("scheduler", "Output file [$file] is not a file")
                file = null
            }
            if (file != null) {
                // cgeck parent directory
                var parent: Resource? = file.getParentResource()
                if (parent != null) {
                    if (!parent.exists()) {
                        val grandParent: Resource = parent.getParentResource()
                        if (grandParent != null && grandParent.exists()) parent.mkdir() else parent = null
                    }
                }
                // no parent directory
                if (parent == null) {
                    ThreadLocalPageContext.getLog((scheduler as SchedulerImpl?)!!.getConfig(), "scheduler").error("scheduler",
                            "Directory for output file [$file] doesn't exist")
                    file = null
                }
            }
        }
        if (timeout < 1) {
            throw ScheduleException("Value for [timeout] must be greater than 0")
        }
        if (startDate == null) throw ScheduleException("Start date is required")
        if (startTime == null) throw ScheduleException("Start time is required")
        // if(endTime==null)endTime=new Time(23,59,59,999);
        this.task = task.trim()
        this.file = file
        this.startDate = startDate
        this.startTime = startTime
        this.endDate = endDate
        this.endTime = endTime
        this.url = toURL(url, port)
        this.interval = toInterval(interval)
        strInterval = interval
        this.timeout = timeout
        this.credentials = credentials
        this.proxy = proxy
        this.userAgent = userAgent
        this.resolveURL = resolveURL
        this.publish = publish
        this.hidden = hidden
        this.readonly = readonly
        this.paused = paused
        this.autoDelete = autoDelete
        this.unique = unique
    }
}