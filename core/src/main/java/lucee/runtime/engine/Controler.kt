/**
 *
 * Copyright (c) 2014, the Railo Company Ltd. All rights reserved.
 * Copyright (c) 2015, Lucee Assosication Switzerland
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
package lucee.runtime.engine

import java.io.IOException

/**
 * own thread how check the main thread and his data
 */
class Controler(configServer: ConfigServer?, contextes: Map?, interval: Int, state: ControllerState?) : ParentThreasRefThread() {
    private val interval: Int
    private var lastMinuteInterval: Long = System.currentTimeMillis() - 1000 * 59 // first after a second
    private var last10SecondsInterval: Long = System.currentTimeMillis() - 1000 * 9 // first after a second
    private var lastHourInterval: Long = System.currentTimeMillis()
    private val contextes: Map?

    // private ScheduleThread scheduleThread;
    private val configServer: ConfigServer?

    // private final ShutdownHook shutdownHook;
    private var state: ControllerState?
    private val poolValidate: Boolean

    private class ControlerThread(controler: Controler?, factories: Array<CFMLFactoryImpl?>?, firstRun: Boolean, log: Log?) : ParentThreasRefThread() {
        private val controler: Controler?
        private val factories: Array<CFMLFactoryImpl?>?
        private val firstRun: Boolean
        var done: Long = -1
        var t: Throwable? = null
        private val log: Log?
        val start: Long
        @Override
        fun run() {
            val start: Long = System.currentTimeMillis()
            try {
                controler!!.control(factories, firstRun, log)
                done = System.currentTimeMillis() - start
            } catch (t: Throwable) {
                ExceptionUtil.rethrowIfNecessary(t)
                this.t = t
            }
            // long time=System.currentTimeMillis()-start;
            // if(time>10000) {
            // log.info("controller", "["+hashCode()+"] controller was running for "+time+"ms");
            // }
        }

        init {
            start = System.currentTimeMillis()
            this.controler = controler
            this.factories = factories
            this.firstRun = firstRun
            this.log = log
        }
    }

    @Override
    fun run() {
        // scheduleThread.start();
        var firstRun = true
        val threads: List<ControlerThread?> = ArrayList<ControlerThread?>()
        var factories: Array<CFMLFactoryImpl?>? = null
        while (state!!.active()) {

            // sleep
            SystemUtil.wait(this, interval)
            if (!state!!.active()) break
            factories = toFactories(factories, contextes)
            // start the thread that calls control
            var ct = ControlerThread(this, factories, firstRun, configServer.getLog("application"))
            ct.start()
            threads.add(ct)
            if (threads.size() > 10 && lastMinuteInterval + 60000 < System.currentTimeMillis()) configServer.getLog("application").info("controller", threads.size().toString() + " active controller threads")

            // now we check all threads we have
            val it = threads.iterator()
            var time: Long
            while (it.hasNext()) {
                ct = it.next()
                // print.e(ct.hashCode());
                time = System.currentTimeMillis() - ct.start
                // done
                if (ct.done >= 0) {
                    if (time > 10000) configServer.getLog("application").info("controller", "controller took " + ct.done + "ms to execute successfully.")
                    it.remove()
                } else if (ct.t != null) {
                    addParentStacktrace(ct.t)
                    configServer.getLog("application").log(Log.LEVEL_ERROR, "controler", ct.t)
                    it.remove()
                } else if (time > TIMEOUT) {
                    SystemUtil.stop(ct)
                    // print.e(ct.getStackTrace());
                    if (!ct.isAlive()) {
                        configServer.getLog("application").error("controller", "controller thread [" + ct.hashCode().toString() + "] forced to stop after " + time.toString() + "ms")
                        it.remove()
                    } else {
                        val t = Throwable()
                        t.setStackTrace(ct.getStackTrace())
                        configServer.getLog("application").log(Log.LEVEL_ERROR, "controler", "was not able to stop controller thread running for " + time + "ms", t)
                    }
                }
            }
            if (factories!!.size > 0) firstRun = false
        }
    }

    private fun control(factories: Array<CFMLFactoryImpl?>?, firstRun: Boolean, log: Log?) {
        val now: Long = System.currentTimeMillis()
        val do10Seconds = last10SecondsInterval + 10000 < now
        if (do10Seconds) last10SecondsInterval = now
        val doMinute = lastMinuteInterval + 60000 < now
        if (doMinute) lastMinuteInterval = now
        val doHour = lastHourInterval + 1000 * 60 * 60 < now
        if (doHour) lastHourInterval = now

        // broadcast cluster scope
        try {
            ScopeContext.getClusterScope(configServer, true).broadcast()
        } catch (t: Throwable) {
            ExceptionUtil.rethrowIfNecessary(t)
            if (log != null) log.error("controler", t)
        }
        if (firstRun) {
            try {
                RHExtension.correctExtensions(configServer)
            } catch (e: Exception) {
                if (log != null) log.error("controler", e)
            }
        }

        // every 10 seconds
        if (do10Seconds) {
            // deploy extensions, archives ...
            // try{DeployHandler.deploy(configServer);}catch(Throwable t){ExceptionUtil.rethrowIfNecessary(t);}
        }
        // every minute
        if (doMinute) {
            // deploy extensions, archives ...
            try {
                DeployHandler.deploy(configServer, configServer.getLog("deploy"), false)
            } catch (t: Throwable) {
                ExceptionUtil.rethrowIfNecessary(t)
                if (log != null) log.error("controler", t)
            }
            try {
                ConfigAdmin.checkForChangesInConfigFile(configServer)
            } catch (t: Throwable) {
                ExceptionUtil.rethrowIfNecessary(t)
                if (log != null) log.error("controler", t)
            }
        }
        // every hour
        if (doHour) {
            try {
                configServer.checkPermGenSpace(true)
            } catch (t: Throwable) {
                ExceptionUtil.rethrowIfNecessary(t)
                if (log != null) log.error("controler", t)
            }
        }
        for (i in factories.indices) {
            control(factories!![i], do10Seconds, doMinute, doHour, firstRun, log)
        }
    }

    private fun control(cfmlFactory: CFMLFactoryImpl?, do10Seconds: Boolean, doMinute: Boolean, doHour: Boolean, firstRun: Boolean, log: Log?) {
        try {
            val isRunning: Boolean = cfmlFactory.getUsedPageContextLength() > 0
            if (isRunning) {
                cfmlFactory.checkTimeout()
            }
            var config: ConfigWeb? = null
            if (firstRun) {
                config = cfmlFactory.getConfig()
                ThreadLocalConfig.register(config)
                config.reloadTimeServerOffset()
                checkOldClientFile(config, log)
                try {
                    RHExtension.correctExtensions(config)
                } catch (e: Exception) {
                    if (log != null) log.error("controler", e)
                }

                // try{checkStorageScopeFile(config,Session.SCOPE_CLIENT);}catch(Throwable t)
                // {ExceptionUtil.rethrowIfNecessary(t);}
                // try{checkStorageScopeFile(config,Session.SCOPE_SESSION);}catch(Throwable t)
                // {ExceptionUtil.rethrowIfNecessary(t);}
                try {
                    config.reloadTimeServerOffset()
                } catch (t: Throwable) {
                    ExceptionUtil.rethrowIfNecessary(t)
                    if (log != null) log.error("controler", t)
                }
                try {
                    checkTempDirectorySize(config)
                } catch (t: Throwable) {
                    ExceptionUtil.rethrowIfNecessary(t)
                    if (log != null) log.error("controler", t)
                }
                try {
                    checkCacheFileSize(config)
                } catch (t: Throwable) {
                    ExceptionUtil.rethrowIfNecessary(t)
                    if (log != null) log.error("controler", t)
                }
                try {
                    cfmlFactory.getScopeContext().clearUnused()
                } catch (t: Throwable) {
                    ExceptionUtil.rethrowIfNecessary(t)
                    if (log != null) log.error("controler", t)
                }
            }
            if (config == null) {
                config = cfmlFactory.getConfig()
            }
            ThreadLocalConfig.register(config)
            if (do10Seconds) {
                // try{DeployHandler.deploy(config);}catch(Throwable t){ExceptionUtil.rethrowIfNecessary(t);}
            }

            // every Minute
            if (doMinute) {
                if (config == null) {
                    config = cfmlFactory.getConfig()
                }
                ThreadLocalConfig.register(config)
                LogUtil.log(ThreadLocalPageContext.getConfig(config), Log.LEVEL_TRACE, Controler::class.java.getName(), "Running background Controller maintenance (every minute).")
                try {
                    (config.getScheduler() as SchedulerImpl).startIfNecessary()
                } catch (e: Exception) {
                    if (log != null) log.error("controler", e)
                }

                // double check templates
                try {
                    (config as ConfigWebPro?).getCompiler().checkWatched()
                } catch (e: Exception) {
                    if (log != null) log.error("controler", e)
                }

                // deploy extensions, archives ...
                try {
                    DeployHandler.deploy(config, ThreadLocalPageContext.getLog(config, "deploy"), false)
                } catch (t: Throwable) {
                    ExceptionUtil.rethrowIfNecessary(t)
                    if (log != null) log.error("controler", t)
                }

                // clear unused DB Connections
                try {
                    for (pool in (config as ConfigPro?).getDatasourceConnectionPools()) {
                        try {
                            pool.evict()
                        } catch (ex: Exception) {
                            if (log != null) log.error("controler", ex)
                        }
                    }
                } catch (e: Exception) {
                    if (log != null) log.error("controler", e)
                }

                // Clear unused http connections
                try {
                    HTTPEngine4Impl.closeIdleConnections()
                } catch (e: Exception) {
                }

                // clear all unused scopes
                try {
                    cfmlFactory.getScopeContext().clearUnused()
                } catch (t: Throwable) {
                    ExceptionUtil.rethrowIfNecessary(t)
                    if (log != null) log.error("controler", t)
                }
                // Memory usage
                // clear Query Cache
                /*
				 * try{ ConfigWebUtil.getCacheHandlerFactories(config).query.clean(null);
				 * ConfigWebUtil.getCacheHandlerFactories(config).include.clean(null);
				 * ConfigWebUtil.getCacheHandlerFactories(config).function.clean(null);
				 * //cfmlFactory.getDefaultQueryCache().clearUnused(null); }catch(Throwable
				 * t){ExceptionUtil.rethrowIfNecessary(t);}
				 */
                // contract Page Pool
                try {
                    doClearPagePools(config)
                } catch (e: Exception) {
                    if (log != null) log.error("controler", e)
                }
                // try{checkPermGenSpace((ConfigWebPro) config);}catch(Throwable t)
                // {ExceptionUtil.rethrowIfNecessary(t);}
                try {
                    doCheckMappings(config)
                } catch (t: Throwable) {
                    ExceptionUtil.rethrowIfNecessary(t)
                    if (log != null) log.error("controler", t)
                }
                try {
                    doClearMailConnections()
                } catch (t: Throwable) {
                    ExceptionUtil.rethrowIfNecessary(t)
                    if (log != null) log.error("controler", t)
                }
                // clean LockManager
                if (cfmlFactory.getUsedPageContextLength() === 0) try {
                    (config.getLockManager() as LockManagerImpl).clean()
                } catch (t: Throwable) {
                    ExceptionUtil.rethrowIfNecessary(t)
                    if (log != null) log.error("controler", t)
                }
                try {
                    ConfigAdmin.checkForChangesInConfigFile(config)
                } catch (t: Throwable) {
                    ExceptionUtil.rethrowIfNecessary(t)
                    if (log != null) log.error("controler", t)
                }
            }
            // every hour
            if (doHour) {
                if (config == null) {
                    config = cfmlFactory.getConfig()
                }
                LogUtil.log(ThreadLocalPageContext.getConfig(config), Log.LEVEL_TRACE, Controler::class.java.getName(), "Running background Controller maintenance (every hour).")
                ThreadLocalConfig.register(config)

                // time server offset
                try {
                    config.reloadTimeServerOffset()
                } catch (t: Throwable) {
                    ExceptionUtil.rethrowIfNecessary(t)
                    if (log != null) log.error("controler", t)
                }
                // check file based client/session scope
                // try{checkStorageScopeFile(config,Session.SCOPE_CLIENT);}catch(Throwable t)
                // {ExceptionUtil.rethrowIfNecessary(t);}
                // try{checkStorageScopeFile(config,Session.SCOPE_SESSION);}catch(Throwable t)
                // {ExceptionUtil.rethrowIfNecessary(t);}
                // check temp directory
                try {
                    checkTempDirectorySize(config)
                } catch (t: Throwable) {
                    ExceptionUtil.rethrowIfNecessary(t)
                    if (log != null) log.error("controler", t)
                }
                // check cache directory
                try {
                    checkCacheFileSize(config)
                } catch (t: Throwable) {
                    ExceptionUtil.rethrowIfNecessary(t)
                    if (log != null) log.error("controler", t)
                }
            }
            try {
                configServer.checkPermGenSpace(true)
            } catch (t: Throwable) {
                ExceptionUtil.rethrowIfNecessary(t)
                if (log != null) log.error("controler", t)
            }
        } catch (t: Throwable) {
            ExceptionUtil.rethrowIfNecessary(t)
            if (log != null) log.error("controler", t)
        } finally {
            ThreadLocalConfig.release()
        }
    }

    private fun doClearPagePools(config: ConfigWeb?) {
        PagePoolClear.clear(null, config, true)
    }

    private fun toFactories(factories: Array<CFMLFactoryImpl?>?, contextes: Map?): Array<CFMLFactoryImpl?>? {
        var factories: Array<CFMLFactoryImpl?>? = factories
        if (factories == null || factories.size != contextes.size()) factories = contextes.values().toArray(arrayOfNulls<CFMLFactoryImpl?>(contextes.size()))
        return factories
    }

    private fun doClearMailConnections() {
        SMTPConnectionPool.closeSessions()
    }

    private fun checkOldClientFile(config: ConfigWeb?, log: Log?) {
        val filter = ExtensionResourceFilter(".script", false)

        // move old structured file in new structure
        try {
            val dir: Resource = config.getClientScopeDir()
            var trgres: Resource
            val children: Array<Resource?> = dir.listResources(filter)
            var src: String
            var trg: String
            var index: Int
            for (i in children.indices) {
                src = children[i].getName()
                index = src.indexOf('-')
                trg = StorageScopeFile.getFolderName(src.substring(0, index), src.substring(index + 1), false)
                trgres = dir.getRealResource(trg)
                if (!trgres.exists()) {
                    trgres.createFile(true)
                    ResourceUtil.copy(children[i], trgres)
                }
                // children[i].moveTo(trgres);
                children[i].delete()
            }
        } catch (t: Throwable) {
            ExceptionUtil.rethrowIfNecessary(t)
            if (log != null) log.error("controler", t)
        }
    }

    private fun checkCacheFileSize(config: ConfigWeb?) {
        checkSize(config, config.getCacheDir(), config.getCacheDirSize(), ExtensionResourceFilter(".cache"))
    }

    private fun checkTempDirectorySize(config: ConfigWeb?) {
        checkSize(config, config.getTempDirectory(), (1024 * 1024 * 1024).toLong(), null)
    }

    private fun checkSize(config: ConfigWeb?, dir: Resource?, maxSize: Long, filter: ResourceFilter?) {
        if (!dir.exists()) return
        var res: Resource? = null
        var count: Int = ArrayUtil.size(if (filter == null) dir.list() else dir.list(filter))
        var size: Long = ResourceUtil.getRealSize(dir, filter)
        LogUtil.log(ThreadLocalPageContext.getConfig(config), Log.LEVEL_DEBUG, Controler::class.java.getName(),
                "Checking size of directory [$dir]. Current size [$size]. Max size [$maxSize].")
        var len = -1
        if (count > 100000 || size > maxSize) {
            LogUtil.log(ThreadLocalPageContext.getConfig(config), Log.LEVEL_WARN, Controler::class.java.getName(),
                    "Removing files from directory [$dir]. Current size [$size]. Max size [$maxSize]. Number of files [$count]")
        }
        while (count > 100000 || size > maxSize) {
            val files: Array<Resource?> = if (filter == null) dir.listResources() else dir.listResources(filter)
            if (len == files.size) break // protect from inifinti loop
            len = files.size
            for (i in files.indices) {
                if (res == null || res.lastModified() > files[i].lastModified()) {
                    res = files[i]
                }
            }
            if (res != null) {
                size -= res.length()
                try {
                    res.remove(true)
                    count--
                } catch (e: Exception) {
                    LogUtil.log(ThreadLocalPageContext.getConfig(config), Log.LEVEL_ERROR, Controler::class.java.getName(), "cannot remove resource [" + res.getAbsolutePath().toString() + "]")
                    break
                }
            }
            res = null
        }
    }

    private fun doCheckMappings(config: ConfigWeb?) {
        val mappings: Array<Mapping?> = config.getMappings()
        for (i in mappings.indices) {
            val mapping: Mapping? = mappings[i]
            mapping.check()
        }
    }

    fun close() {
        state = INACTIVE
        SystemUtil.notify(this)
    }

    internal class ExpiresFilter(private val time: Long, private val allowDir: Boolean) : ResourceFilter {
        @Override
        fun accept(res: Resource?): Boolean {
            if (res.isDirectory()) return allowDir

            // load content
            var str: String? = null
            str = try {
                IOUtil.toString(res, "UTF-8")
            } catch (e: IOException) {
                return false
            }
            val index: Int = str.indexOf(':')
            if (index != -1) {
                val expires: Long = Caster.toLongValue(str.substring(0, index), -1L)
                // check is for backward compatibility, old files have no expires date inside. they do ot expire
                if (expires != -1L) {
                    if (expires < System.currentTimeMillis()) {
                        return true
                    }
                    str = str.substring(index + 1)
                    return false
                }
            } else if (res.lastModified() <= time) {
                return true
            }
            return false
        }
    }

    companion object {
        private const val TIMEOUT = (50 * 1000).toLong()
        private val INACTIVE: ControllerState? = ControllerStateImpl(false)
    }

    /**
     * @param contextes
     * @param interval
     * @param run
     */
    init {
        this.contextes = contextes
        this.interval = interval
        this.state = state
        this.configServer = configServer
        poolValidate = Caster.toBooleanValue(SystemUtil.getSystemPropOrEnvVar("lucee.datasource.pool.validate", null), true)
        // shutdownHook=new ShutdownHook(configServer);
        // Runtime.getRuntime().addShutdownHook(shutdownHook);
    }
}