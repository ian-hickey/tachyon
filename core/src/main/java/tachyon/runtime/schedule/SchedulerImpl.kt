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
package tachyon.runtime.schedule

import java.io.IOException

/**
 * scheduler class to execute the scheduled tasks
 */
class SchedulerImpl : Scheduler {
    private var tasks: Array<ScheduleTaskImpl?>?
    private val schedulerFile: Resource? = null
    private val su: StorageUtil? = StorageUtil()
    private val charset: String? = null
    private val config: Config?
    private val sync: Object? = SerializableObject()

    // private String md5;
    private var engine: CFMLEngineImpl?

    /**
     * constructor of the sheduler
     *
     * @param config
     * @param schedulerDir schedule file
     * @param log
     * @throws IOException
     * @throws SAXException
     * @throws PageException
     */
    constructor(engine: CFMLEngine?, config: Config?, tasks: Array?) {
        this.engine = engine as CFMLEngineImpl?
        this.config = config
        this.tasks = readInAllTasks(tasks)
        init()
    }

    /**
     * creates an empty Scheduler, used for event gateway context
     *
     * @param engine
     * @param config
     * @param log
     * @throws SAXException
     * @throws IOException
     * @throws PageException
     */
    constructor(engine: CFMLEngine?, xml: String?, config: Config?) {
        this.engine = engine as CFMLEngineImpl?
        this.config = config
        tasks = arrayOfNulls<ScheduleTaskImpl?>(0)
        init()
    }

    /**
     * initialize all tasks
     */
    private fun init() {
        for (i in tasks.indices) {
            init(tasks!![i])
        }
    }

    fun startIfNecessary() {
        for (i in tasks.indices) {
            init(tasks!![i])
        }
    }

    private fun init(task: ScheduleTask?) {
        (task as ScheduleTaskImpl?)!!.startIfNecessary(engine)
    }

    fun stop() {
        for (i in tasks.indices) {
            tasks!![i]!!.stop()
        }
    }

    /**
     * read in all schedule tasks
     *
     * @return
     *
     * @return all schedule tasks
     * @throws PageException
     */
    @Throws(PageException::class)
    private fun readInAllTasks(tasks: Array?): Array<ScheduleTaskImpl?>? {
        val list: ArrayList<ScheduleTaskImpl?> = ArrayList<ScheduleTaskImpl?>()
        val it: Iterator<*> = tasks.getIterator()
        while (it.hasNext()) {
            list.add(readInTask(it.next() as Struct?))
        }
        return list.toArray(arrayOfNulls<ScheduleTaskImpl?>(list.size()))
    }

    /**
     * read in a single task element
     *
     * @param el
     * @return matching task to Element
     * @throws PageException
     */
    @Throws(PageException::class)
    private fun readInTask(el: Struct?): ScheduleTaskImpl? {
        var timeout: Long = su!!.toLong(el, "timeout")
        if (timeout > 0 && timeout < 1000) timeout *= 1000
        if (timeout < 0) timeout = 600000
        return try {
            ScheduleTaskImpl(this, su!!.toString(el, "name").trim(), su!!.toResource(config, el, "file"), su.toDate(config, el, "startDate"),
                    su.toTime(config, el, "startTime"), su.toDate(config, el, "endDate"), su.toTime(config, el, "endTime"), su!!.toString(el, "url"), su!!.toInt(el, "port", -1),
                    su!!.toString(el, "interval"), timeout, su!!.toCredentials(el, "username", "password"),
                    ProxyDataImpl.getInstance(su!!.toString(el, "proxyHost"), su!!.toInt(el, "proxyPort", 80), su!!.toString(el, "proxyUser"), su!!.toString(el, "proxyPassword")),
                    su!!.toBoolean(el, "resolveUrl"), su!!.toBoolean(el, "publish"), su!!.toBoolean(el, "hidden", false), su!!.toBoolean(el, "readonly", false),
                    su!!.toBoolean(el, "paused", false), su!!.toBoolean(el, "autoDelete", false), su!!.toBoolean(el, "unique", false), su!!.toString(el, "userAgent").trim())
        } catch (e: Exception) {
            LogUtil.log(ThreadLocalPageContext.getConfig(config), SchedulerImpl::class.java.getName(), e)
            throw Caster.toPageException(e)
        }
    }

    private fun addTask(task: ScheduleTaskImpl?) {
        for (i in tasks.indices) {
            if (!tasks!![i]!!.getTask()!!.equals(task!!.getTask())) continue
            if (!tasks!![i]!!.md5()!!.equals(task!!.md5())) {
                tasks!![i]!!.log(Log.LEVEL_INFO, "invalidate task because the task is replaced with a new one")
                tasks!![i]!!.setValid(false)
                tasks!![i] = task
                init(task)
            }
            return
        }
        val tmp: Array<ScheduleTaskImpl?> = arrayOfNulls<ScheduleTaskImpl?>(tasks!!.size + 1)
        for (i in tasks.indices) {
            tmp[i] = tasks!![i]
        }
        tmp[tasks!!.size] = task
        tasks = tmp
        init(task)
    }

    @Override
    @Throws(ScheduleException::class)
    fun getScheduleTask(name: String?): ScheduleTask? {
        for (i in tasks.indices) {
            if (tasks!![i]!!.getTask().equalsIgnoreCase(name)) return tasks!![i]
        }
        throw ScheduleException("schedule task with name $name doesn't exist")
    }

    @Override
    fun getScheduleTask(name: String?, defaultValue: ScheduleTask?): ScheduleTask? {
        for (i in tasks.indices) {
            if (tasks!![i] != null && tasks!![i].getTask().equalsIgnoreCase(name)) return tasks!![i]
        }
        return defaultValue
    }

    @Override
    fun getAllScheduleTasks(): Array<ScheduleTask?>? {
        val list: ArrayList<ScheduleTask?> = ArrayList<ScheduleTask?>()
        for (i in tasks.indices) {
            if (!tasks!![i]!!.isHidden()) list.add(tasks!![i])
        }
        return list.toArray(arrayOfNulls<ScheduleTask?>(list.size()))
    }

    @Override
    @Throws(ScheduleException::class, IOException::class)
    fun addScheduleTask(task: ScheduleTask?, allowOverwrite: Boolean) {
        try {
            addTask(task as ScheduleTaskImpl?)
            ConfigAdmin.updateScheduledTask(config as ConfigPro?, task, true)
        } catch (e: Exception) {
            throw ExceptionUtil.toIOException(e)
        }
    }

    @Override
    @Throws(ScheduleException::class, IOException::class)
    fun pauseScheduleTask(name: String?, pause: Boolean, throwWhenNotExist: Boolean) {
        try {
            ConfigAdmin.pauseScheduledTask(config as ConfigPro?, name, pause, throwWhenNotExist, true)
        } catch (e: Exception) {
            throw ExceptionUtil.toIOException(e)
        }
        for (i in tasks.indices) {
            if (tasks!![i]!!.getTask().equalsIgnoreCase(name)) {
                tasks!![i]!!.setPaused(pause)
            }
        }
    }

    @Override
    @Throws(IOException::class, ScheduleException::class)
    fun removeScheduleTask(name: String?, throwWhenNotExist: Boolean) {
        synchronized(sync) {
            var pos = -1
            for (i in tasks.indices) {
                if (tasks!![i]!!.getTask().equalsIgnoreCase(name)) {
                    tasks!![i]!!.log(Log.LEVEL_INFO, "task gets removed")
                    tasks!![i]!!.setValid(false)
                    pos = i
                }
            }
            if (pos != -1) {
                val newTasks: Array<ScheduleTaskImpl?> = arrayOfNulls<ScheduleTaskImpl?>(tasks!!.size - 1)
                var count = 0
                for (i in tasks.indices) {
                    if (i != pos) newTasks[count++] = tasks!![i]
                }
                tasks = newTasks
            }
            try {
                ConfigAdmin.removeScheduledTask(config as ConfigPro?, name, true)
            } catch (e: Exception) {
                throw ExceptionUtil.toIOException(e)
            }
        }
    }

    @Throws(IOException::class)
    fun removeIfNoLonerValid(task: ScheduleTask?) {
        synchronized(sync) {
            val sti: ScheduleTaskImpl? = task
            if (sti!!.isValid() || !sti!!.isAutoDelete()) return
            try {
                removeScheduleTask(task.getTask(), false)
            } catch (e: ScheduleException) {
            }
        }
    }

    @Override
    @Throws(IOException::class, ScheduleException::class)
    fun runScheduleTask(name: String?, throwWhenNotExist: Boolean) {
        synchronized(sync) {
            val task: ScheduleTask? = getScheduleTask(name)
            if (task != null) {
                if (active()) execute(task)
            } else if (throwWhenNotExist) throw ScheduleException("can't run schedule task [$name], task doesn't exist")
        }
    }

    fun execute(task: ScheduleTask?) {
        ExecutionThread(config, task, charset).start()
    }

    fun getConfig(): Config? {
        return config
    }

    fun getCharset(): String? {
        return charset
    }

    fun active(): Boolean {
        return engine == null || engine.active()
    }
}