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
package lucee.runtime.tag

import java.net.URL

/**
 * Provides a programmatic interface to the Lucee scheduling engine. You can run a specified page at
 * scheduled intervals with the option to write out static HTML pages. This lets you offer users
 * access to pages that publish data, such as reports, without forcing users to wait while a
 * database transaction is performed in order to populate the data on the page.
 */
class Schedule : TagImpl() {
    /** Password if URL is protected.  */
    private var password: String? = ""

    /** Required when action ='update'. The date when scheduling of the task should start.  */
    private var startdate: Date? = null

    /** Specifies whether to resolve links in the result page to absolute references.  */
    private var resolveurl = false

    /**   */
    private var action: Short = 0

    /** Host name or IP address of a proxy server.  */
    private var proxyserver: String? = null

    /** user agent for the http request.  */
    private var userAgent: String? = null

    /** The date when the scheduled task ends.  */
    private var enddate: Date? = null

    /** Required with publish ='Yes' A valid filename for the published file.  */
    private var strFile: String? = null

    /**
     * Required when creating tasks with action = 'update'. Enter a value in seconds. The time when
     * scheduling of the task starts.
     */
    private var starttime: Time? = null

    /**
     * The port number on the proxy server from which the task is being requested. Default is 80. When
     * used with resolveURL, the URLs of retrieved documents that specify a port number are
     * automatically resolved to preserve links in the retrieved document.
     */
    private var proxyport = 80

    /**
     * The port number on the server from which the task is being scheduled. Default is 80. When used
     * with resolveURL, the URLs of retrieved documents that specify a port number are automatically
     * resolved to preserve links in the retrieved document.
     */
    private var port = -1

    /** The time when the scheduled task ends. Enter a value in seconds.  */
    private var endtime: Time? = null

    /**
     * Required when creating tasks with action = 'update'. Interval at which task should be scheduled.
     * Can be set in seconds or as Once, Daily, Weekly, and Monthly. The default interval is one hour.
     * The minimum interval is one minute.
     */
    private var interval: String? = null

    /** Specifies whether the result should be saved to a file.  */
    private var publish = false

    /**
     * Customizes the requestTimeOut for the task operation. Can be used to extend the default timeout
     * for operations that require more time to execute.
     */
    private var requesttimeout: Long = -1

    /** Username if URL is protected.  */
    private var username: String? = null

    /** Required when action = 'update'. The URL to be executed.  */
    private var url: String? = null

    /** Required with publish ='Yes' The path location for the published file.  */
    private var strPath: String? = null

    /** The name of the task to delete, update, or run.  */
    private var task: String? = null
    private var scheduler: Scheduler? = null
    private var proxyuser: String? = null
    private var proxypassword: String? = ""
    private var result: String? = "cfschedule"
    private var hidden = false
    private var readonly = false
    private var serverPassword: String? = null
    private var paused = false
    private var autoDelete = false
    private var unique = false
    fun setAutodelete(autoDelete: Boolean) {
        this.autoDelete = autoDelete
    }

    /**
     * @param readonly the readonly to set
     */
    fun setReadonly(readonly: Boolean) {
        this.readonly = readonly
    }

    /**
     * @param hidden the hidden to set
     */
    fun setHidden(hidden: Boolean) {
        this.hidden = hidden
    }

    /**
     * @param result The returnvariable to set.
     */
    fun setResult(result: String?) {
        this.result = result
    }

    /**
     * @param proxypassword The proxypassword to set.
     */
    fun setProxypassword(proxypassword: String?) {
        this.proxypassword = proxypassword
    }

    /**
     * @param proxyuser The proxyuser to set.
     */
    fun setProxyuser(proxyuser: String?) {
        this.proxyuser = proxyuser
    }

    fun setPaused(paused: Boolean) {
        this.paused = paused
    }

    fun setUnique(unique: Boolean) {
        this.unique = unique
    }

    /**
     * set the value password Password if URL is protected.
     *
     * @param password value to set
     */
    fun setPassword(password: String?) {
        this.password = password
    }

    /**
     * set the value startdate Required when action ='update'. The date when scheduling of the task
     * should start.
     *
     * @param objStartDate value to set
     * @throws PageException
     */
    @Throws(PageException::class)
    fun setStartdate(objStartDate: Object?) {
        if (StringUtil.isEmpty(objStartDate)) return
        startdate = DateImpl(DateCaster.toDateAdvanced(objStartDate, pageContext.getTimeZone()))
    }

    /**
     * set the value resolveurl Specifies whether to resolve links in the result page to absolute
     * references.
     *
     * @param resolveurl value to set
     */
    fun setResolveurl(resolveurl: Boolean) {
        this.resolveurl = resolveurl
    }

    fun setServerpassword(serverPassword: String?) {
        this.serverPassword = serverPassword
    }

    /**
     * set the value action
     *
     * @param action value to set
     * @throws ApplicationException
     */
    @Throws(ApplicationException::class)
    fun setAction(action: String?) {
        var action = action
        if (StringUtil.isEmpty(action)) return
        action = action.toLowerCase().trim()
        if (action.equals("run")) this.action = ACTION_RUN else if (action.equals("delete")) this.action = ACTION_DELETE else if (action.equals("update")) this.action = ACTION_UPDATE else if (action.equals("list")) this.action = ACTION_LIST else if (action.equals("lists")) this.action = ACTION_LIST else if (action.equals("pause")) this.action = ACTION_PAUSE else if (action.equals("resume")) this.action = ACTION_RESUME else throw ApplicationException("attribute action with value [$action] of tag schedule is invalid",
                "valid attributes are [delete,run,update,list,resume,pause]")
    }

    /**
     * set the value proxyserver Host name or IP address of a proxy server.
     *
     * @param proxyserver value to set
     */
    fun setProxyserver(proxyserver: String?) {
        this.proxyserver = proxyserver
    }

    /**
     * set the value of the userAgent for the http request.
     *
     * @param userAgent value to set
     */
    fun setUseragent(userAgent: String?) {
        this.userAgent = userAgent
    }

    /**
     * set the value enddate The date when the scheduled task ends.
     *
     * @param enddate value to set
     * @throws PageException
     */
    @Throws(PageException::class)
    fun setEnddate(enddate: Object?) {
        if (StringUtil.isEmpty(enddate)) return
        this.enddate = DateImpl(DateCaster.toDateAdvanced(enddate, pageContext.getTimeZone()))
    }

    /**
     * set the value file Required with publish ='Yes' A valid filename for the published file.
     *
     * @param file value to set
     */
    fun setFile(file: String?) {
        strFile = file
    }

    /**
     * set the value starttime Required when creating tasks with action = 'update'. Enter a value in
     * seconds. The time when scheduling of the task starts.
     *
     * @param starttime value to set
     * @throws PageException
     */
    @Throws(PageException::class)
    fun setStarttime(starttime: Object?) {
        if (StringUtil.isEmpty(starttime)) return
        this.starttime = DateCaster.toTime(pageContext.getTimeZone(), starttime)
    }

    /**
     * set the value proxyport The port number on the proxy server from which the task is being
     * requested. Default is 80. When used with resolveURL, the URLs of retrieved documents that specify
     * a port number are automatically resolved to preserve links in the retrieved document.
     *
     * @param proxyport value to set
     * @throws PageException
     */
    @Throws(PageException::class)
    fun setProxyport(oProxyport: Object?) {
        if (StringUtil.isEmpty(oProxyport)) return
        proxyport = Caster.toIntValue(oProxyport)
    }

    /**
     * set the value port The port number on the server from which the task is being scheduled. Default
     * is 80. When used with resolveURL, the URLs of retrieved documents that specify a port number are
     * automatically resolved to preserve links in the retrieved document.
     *
     * @param port value to set
     * @throws PageException
     */
    @Throws(PageException::class)
    fun setPort(oPort: Object?) {
        if (StringUtil.isEmpty(oPort)) return
        port = Caster.toIntValue(oPort)
    }

    /**
     * set the value endtime The time when the scheduled task ends. Enter a value in seconds.
     *
     * @param endtime value to set
     * @throws PageException
     */
    @Throws(PageException::class)
    fun setEndtime(endtime: Object?) {
        if (StringUtil.isEmpty(endtime)) return
        this.endtime = DateCaster.toTime(pageContext.getTimeZone(), endtime)
    }

    /**
     * set the value operation The type of operation the scheduler performs when executing this task.
     *
     * @param operation value to set
     * @throws ApplicationException
     */
    @Throws(ApplicationException::class)
    fun setOperation(operation: String?) {
        var operation = operation
        if (StringUtil.isEmpty(operation)) return
        operation = operation.toLowerCase().trim()
        if (!operation.equals("httprequest")) throw ApplicationException("attribute operation must have the value [HTTPRequest]")
    }

    /**
     * set the value interval Required when creating tasks with action = 'update'. Interval at which
     * task should be scheduled. Can be set in seconds or as Once, Daily, Weekly, and Monthly. The
     * default interval is one hour. The minimum interval is one minute.
     *
     * @param interval value to set
     */
    fun setInterval(interval: String?) {
        var interval = interval
        if (StringUtil.isEmpty(interval)) return
        interval = interval.trim().toLowerCase()
        if (interval.equals("week")) this.interval = "weekly" else if (interval.equals("day")) this.interval = "daily" else if (interval.equals("month")) this.interval = "monthly" else if (interval.equals("year")) this.interval = "yearly"
        this.interval = interval
    }

    /**
     * set the value publish Specifies whether the result should be saved to a file.
     *
     * @param publish value to set
     */
    fun setPublish(publish: Boolean) {
        this.publish = publish
    }

    /**
     * set the value requesttimeout Customizes the requestTimeOut for the task operation. Can be used to
     * extend the default timeout for operations that require more time to execute.
     *
     * @param requesttimeout value to set
     */
    @Throws(PageException::class)
    fun setRequesttimeout(oRequesttimeout: Object?) {
        if (StringUtil.isEmpty(oRequesttimeout)) return
        requesttimeout = Caster.toLongValue(oRequesttimeout) * 1000L
    }

    /**
     * set the value username Username if URL is protected.
     *
     * @param username value to set
     */
    fun setUsername(username: String?) {
        this.username = username
    }

    /**
     * set the value url Required when action = 'update'. The URL to be executed.
     *
     * @param url value to set
     */
    fun setUrl(url: String?) {
        this.url = url
    }

    /**
     * set the value path Required with publish ='Yes' The path location for the published file.
     *
     * @param path value to set
     */
    fun setPath(path: String?) {
        strPath = path
    }

    /**
     * set the value task The name of the task to delete, update, or run.
     *
     * @param task value to set
     */
    fun setTask(task: String?) {
        this.task = task
    }

    @Override
    @Throws(PageException::class)
    fun doStartTag(): Int {
        scheduler = pageContext.getConfig().getScheduler()
        if (action != ACTION_LIST && task == null) {
            throw ApplicationException("attribute [task] is required for tag [schedule] when action is not list")
        }
        when (action) {
            ACTION_DELETE -> doDelete()
            ACTION_RUN -> doRun()
            ACTION_UPDATE -> doUpdate()
            ACTION_LIST -> doList()
            ACTION_PAUSE -> doPause(true)
            ACTION_RESUME -> doPause(false)
        }
        return SKIP_BODY
    }

    /**
     * @throws PageException
     */
    @Throws(PageException::class)
    private fun doUpdate() {
        val message = "missing attribute for tag schedule with action update"
        val detail = "required attributes are [startDate, startTime, URL, interval, operation]"
        var file: Resource? = null
        // if(publish) {
        if (!StringUtil.isEmpty(strFile) && !StringUtil.isEmpty(strPath)) {
            file = ResourceUtil.toResourceNotExisting(pageContext, strPath)
            file = file.getRealResource(strFile)
        } else if (!StringUtil.isEmpty(strFile)) {
            file = ResourceUtil.toResourceNotExisting(pageContext, strFile)
        } else if (!StringUtil.isEmpty(strPath)) {
            file = ResourceUtil.toResourceNotExisting(pageContext, strPath)
        }
        if (file != null) pageContext.getConfig().getSecurityManager().checkFileLocation(pageContext.getConfig(), file, serverPassword)

        // missing attributes
        if (startdate == null || starttime == null || url == null || interval == null) throw ApplicationException(message, detail)

        // timeout
        if (requesttimeout < 0) requesttimeout = pageContext.getRequestTimeout()

        // username/password
        var cr: Credentials? = null
        if (username != null) cr = CredentialsImpl.toCredentials(username, password)
        try {
            val st: ScheduleTask = ScheduleTaskImpl(scheduler, task, file, startdate, starttime, enddate, endtime, url, port, interval, requesttimeout, cr,
                    ProxyDataImpl.getInstance(proxyserver, proxyport, proxyuser, proxypassword), resolveurl, publish, hidden, readonly, paused, autoDelete, unique, userAgent)
            scheduler.addScheduleTask(st, true)
        } catch (e: Exception) {
            throw Caster.toPageException(e)
        }

        //
    }

    /**
     * @throws PageException
     */
    @Throws(PageException::class)
    private fun doRun() {
        try {
            scheduler.runScheduleTask(task, true)
        } catch (e: Exception) {
            throw Caster.toPageException(e)
        }
    }

    /**
     * @throws PageException
     */
    @Throws(PageException::class)
    private fun doDelete() {
        try {
            scheduler.removeScheduleTask(task, true)
        } catch (e: Exception) {
            throw Caster.toPageException(e)
        }
    }

    /**
     * @throws PageException
     */
    @Throws(PageException::class)
    private fun doList() {
        // if(tr ue) throw new PageRuntimeException("qqq");
        val tasks: Array<ScheduleTask?> = scheduler.getAllScheduleTasks()
        val v = "VARCHAR"
        val cols = arrayOf<String?>("task", "path", "file", "startdate", "starttime", "enddate", "endtime", "url", "port", "interval", "timeout", "username", "password",
                "proxyserver", "proxyport", "proxyuser", "proxypassword", "resolveurl", "publish", "valid", "paused", "autoDelete", "unique", "useragent")
        val types = arrayOf<String?>(v, v, v, "DATE", "OTHER", "DATE", "OTHER", v, v, v, v, v, v, v, v, v, v, v, "BOOLEAN", v, "BOOLEAN", "BOOLEAN", "BOOLEAN", v)
        val query: lucee.runtime.type.Query = QueryImpl(cols, types, tasks.size, "query")
        try {
            for (i in tasks.indices) {
                val row: Int = i + 1
                val task: ScheduleTask? = tasks[i]
                query.setAt(KeyConstants._task, row, task.getTask())
                if (task.getResource() != null) {
                    query.setAt(KeyConstants._path, row, task.getResource().getParent())
                    query.setAt(KeyConstants._file, row, task.getResource().getName())
                }
                query.setAt("publish", row, Caster.toBoolean(task.isPublish()))
                query.setAt("startdate", row, task.getStartDate())
                query.setAt("starttime", row, task.getStartTime())
                query.setAt("enddate", row, task.getEndDate())
                query.setAt("endtime", row, task.getEndTime())
                query.setAt(KeyConstants._url, row, printUrl(task.getUrl()))
                query.setAt(KeyConstants._port, row, Caster.toString(HTTPUtil.getPort(task.getUrl())))
                query.setAt("interval", row, task.getStringInterval())
                query.setAt("timeout", row, Caster.toString(task.getTimeout() / 1000))
                query.setAt("valid", row, Caster.toString(task.isValid()))
                if (task.hasCredentials()) {
                    query.setAt("username", row, task.getCredentials().getUsername())
                    query.setAt("password", row, task.getCredentials().getPassword())
                }
                val pd: ProxyData = task.getProxyData()
                if (ProxyDataImpl.isValid(pd)) {
                    query.setAt("proxyserver", row, pd.getServer())
                    if (pd.getPort() > 0) query.setAt("proxyport", row, Caster.toString(pd.getPort()))
                    if (ProxyDataImpl.hasCredentials(pd)) {
                        query.setAt("proxyuser", row, pd.getUsername())
                        query.setAt("proxypassword", row, pd.getPassword())
                    }
                }
                query.setAt("useragent", row, Caster.toString((task as ScheduleTaskPro?).getUserAgent()))
                query.setAt("resolveurl", row, Caster.toString(task.isResolveURL()))
                query.setAt("paused", row, Caster.toBoolean(task.isPaused()))
                query.setAt("autoDelete", row, Caster.toBoolean((task as ScheduleTaskImpl?).isAutoDelete()))
                query.setAt("unique", row, Caster.toBoolean((task as ScheduleTaskImpl?).unique()))
            }
            pageContext.setVariable(result, query)
        } catch (e: DatabaseException) {
        }
    }

    @Throws(PageException::class)
    private fun doPause(pause: Boolean) {
        try {
            (scheduler as SchedulerImpl?).pauseScheduleTask(task, pause, true)
        } catch (e: Exception) {
            throw Caster.toPageException(e)
        }
    }

    private fun printUrl(url: URL?): String? {
        var qs: String = url.getQuery()
        if (qs == null) qs = "" else if (qs.length() > 0) qs = "?$qs"
        val protocol: String = url.getProtocol()
        val port: Int = HTTPUtil.getPort(url)
        val isNonStandardPort = "https".equalsIgnoreCase(protocol) && port != 443 || "http".equalsIgnoreCase(protocol) && port != 80
        return protocol + "://" + url.getHost() + (if (isNonStandardPort) ":$port" else "") + url.getPath() + qs
    }

    @Override
    fun doEndTag(): Int {
        return EVAL_PAGE
    }

    @Override
    fun release() {
        super.release()
        readonly = false
        strPath = null
        strFile = null
        starttime = null
        startdate = null
        endtime = null
        enddate = null
        url = null
        port = -1
        interval = null
        requesttimeout = -1
        username = null
        password = ""
        proxyserver = null
        userAgent = null
        proxyport = 80
        proxyuser = null
        proxypassword = ""
        resolveurl = false
        publish = false
        result = "cfschedule"
        task = null
        hidden = false
        serverPassword = null
        paused = false
        unique = false
        autoDelete = false
    }

    companion object {
        private const val ACTION_RUN: Short = 1
        private const val ACTION_UPDATE: Short = 2
        private const val ACTION_DELETE: Short = 3
        private const val ACTION_LIST: Short = 4
        private const val ACTION_PAUSE: Short = 5
        private const val ACTION_RESUME: Short = 6
    }
}