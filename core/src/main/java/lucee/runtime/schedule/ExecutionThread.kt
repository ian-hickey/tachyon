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

internal class ExecutionThread(config: Config?, task: ScheduleTask?, charset: String?) : ParentThreasRefThread() {
    private val config: Config?

    // private Log log;
    private val task: ScheduleTask?
    private val charset: String?
    @Override
    fun run() {
        if (ThreadLocalPageContext.getConfig() == null && config != null) ThreadLocalConfig.register(config)
        execute(this, config, task, charset)
    }

    companion object {
        fun execute(ptrt: ParentThreasRefThread?, config: Config?, task: ScheduleTask?, charset: String?) {
            val scheduler: Scheduler = (task as ScheduleTaskImpl?)!!.getScheduler()
            if (scheduler is SchedulerImpl && !(scheduler as SchedulerImpl)!!.active()) return
            val log: Log? = getLog(config)
            var hasError = false
            val logName = "schedule task:" + task.getTask()
            // init
            // HttpClient client = new HttpClient();
            // client.setStrictMode(false);
            // HttpState state = client.getState();
            val url: String
            url = if (task.getUrl().getQuery() == null) task.getUrl().toExternalForm().toString() + "?RequestTimeout=" + task.getTimeout() / 1000 else if (StringUtil.isEmpty(task.getUrl().getQuery())) task.getUrl().toExternalForm().toString() + "RequestTimeout=" + task.getTimeout() / 1000 else {
                if (StringUtil.indexOfIgnoreCase(task.getUrl().getQuery().toString() + "", "RequestTimeout") !== -1) task.getUrl().toExternalForm() else task.getUrl().toExternalForm().toString() + "&RequestTimeout=" + task.getTimeout() / 1000
            }

            // HttpMethod method = new GetMethod(url);
            // HostConfiguration hostConfiguration = client.getHostConfiguration();
            var userAgent: String = (task as ScheduleTaskPro?)!!.getUserAgent()
            if (StringUtil.isEmpty(userAgent)) userAgent = Constants.NAME.toString() + " Scheduler"
            // userAgent = "CFSCHEDULE"; this old userAgent string is on block listslists
            val headers: ArrayList<Header?> = ArrayList<Header?>()
            headers.add(HTTPEngine.header("User-Agent", userAgent))

            // method.setRequestHeader("User-Agent","CFSCHEDULE");

            // Userame / Password
            val credentials: Credentials = task.getCredentials()
            var user: String? = null
            var pass: String? = null
            if (credentials != null) {
                user = credentials.getUsername()
                pass = credentials.getPassword()
                // get.addRequestHeader("Authorization","Basic admin:spwwn1p");
                val plainCredentials = "$user:$pass"
                val base64Credentials: String = Base64Encoder.encode(plainCredentials.getBytes())
                val authorizationHeader = "Basic $base64Credentials"
                headers.add(HTTPEngine.header("Authorization", authorizationHeader))
            }

            // Proxy
            var proxy: ProxyData = ProxyDataImpl.validate(task.getProxyData(), task.getUrl().getHost())
            if (proxy == null) {
                proxy = ProxyDataImpl.validate(config.getProxyData(), task.getUrl().getHost())
            }
            var rsp: HTTPResponse? = null

            // execute
            log.info(logName, "calling URL [$url]")
            try {
                rsp = HTTPEngine.get(URL(url), user, pass, task.getTimeout(), true, charset, null, proxy, headers.toArray(arrayOfNulls<Header?>(headers.size())))
                if (rsp != null) {
                    val sc: Int = rsp.getStatusCode()
                    if (sc >= 200 && sc < 300) log.info(logName, "successfully called URL [$url], response code $sc") else log.warn(logName, "called URL [$url] returned response code $sc")
                }
            } catch (e: Exception) {
                try {
                    log.log(Log.LEVEL_ERROR, logName, e)
                } catch (ee: Exception) {
                    if (ptrt != null) {
                        ptrt.addParentStacktrace(e)
                        ptrt.addParentStacktrace(ee)
                    }
                    // TODO log parent stacktrace as well
                    LogUtil.logGlobal(config, "scheduler", e)
                    LogUtil.logGlobal(config, "scheduler", ee)
                }
                hasError = true
            }

            // write file
            var file: Resource = task.getResource()
            if (!hasError && file != null && task.isPublish()) {
                var n: String = file.getName()
                if (n.indexOf("{id}") !== -1) {
                    n = StringUtil.replace(n, "{id}", CreateUUID.invoke(), false)
                    file = file.getParentResource().getRealResource(n)
                }
                if (isText(rsp) && task.isResolveURL()) {
                    var str: String
                    try {
                        val stream: InputStream = rsp.getContentAsStream()
                        str = if (stream == null) "" else IOUtil.toString(stream, null as Charset?)
                        if (str == null) str = ""
                    } catch (e: IOException) {
                        str = e.getMessage()
                    }
                    try {
                        str = URLResolver().transform(str, task.getUrl(), false)
                    } catch (e: PageException) {
                        log.log(Log.LEVEL_ERROR, logName, e)
                        hasError = true
                    }
                    try {
                        IOUtil.write(file, str, charset, false)
                    } catch (e: IOException) {
                        log.log(Log.LEVEL_ERROR, logName, e)
                        hasError = true
                    }
                } else {
                    try {
                        IOUtil.copy(rsp.getContentAsStream(), file, true)
                    } catch (e: IOException) {
                        log.log(Log.LEVEL_ERROR, logName, e)
                        hasError = true
                    }
                }
                HTTPEngine.closeEL(rsp)
            }
        }

        private fun getLog(config: Config?): Log? {
            return ThreadLocalPageContext.getLog(config, "scheduler")
        }

        private fun isText(rsp: HTTPResponse?): Boolean {
            val ct: ContentType = rsp.getContentType() ?: return true
            val mimetype: String = ct.getMimeType()
            return mimetype == null || mimetype.startsWith("text") || mimetype.startsWith("application/octet-stream")
        }
    }

    init {
        this.config = config
        this.task = task
        this.charset = charset
    }
}