package tachyon.commons.io.log.log4j2.appender

import java.io.IOException

class ResourceAppender(name: String?, filter: Filter?, layout: Layout?, res: Resource, charset: Charset, append: Boolean, timeout: Int, maxFileSize: Long, maxfiles: Int,
                       listener: RetireListener) : AbstractAppender(name, filter, layout) {
    private val maxFileSize: Long
    private val maxfiles: Int
    private val sync: Object = SerializableObject()
    private val res: Resource
    private val charset: Charset
    private val append: Boolean
    private val timeout: Int
    private val listener: RetireListener
    private var writer: OutputStreamWriter? = null
    private val token: String
    private var size: Long = 0
    @Override
    fun append(event: LogEvent?) {
        start()
        // check file length
        if (size > maxFileSize) {
            synchronized(token) {
                if (res.length() > maxFileSize) { // we do not trust size to much because of multi threading issues we do not avoid setting this var
                    try {
                        rollOver()
                    } catch (e: IOException) {
                        LogUtil.logGlobal(ThreadLocalPageContext.getConfig(), "log-loading", "Log rollover failed for [$res]", e)
                    }
                }
            }
        }
        try {
            val str: String = Caster.toString(getLayout().toSerializable(event))
            if (!StringUtil.isEmpty(str)) {
                try {
                    if (writer == null) setFile(append)
                    writer.write(str)
                    size += str.length()
                    writer.flush()
                } catch (ioe: IOException) {
                    LogUtil.logGlobal(ThreadLocalPageContext.getConfig(), "log-loading", "Unable to write to$res", ioe)
                    closeFile()
                    setFile(append)
                    writer.write(str)
                    size += str.length()
                    writer.flush()
                }
            }
        } catch (e: Exception) {
            LogUtil.logGlobal(ThreadLocalPageContext.getConfig(), "log-loading", "Unable to write to log [$res]", e)
            closeFile()
        } finally {
        }
    }

    val resource: Resource
        get() = res

    /**
     *
     *
     * Sets and *opens* the file where the log output will go. The specified file must be writable.
     *
     *
     *
     * If there was already an opened file, then the previous file is closed first.
     *
     *
     *
     * **Do not use this method directly. To configure a FileAppender or one of its subclasses, set its
     * properties one by one and then call activateOptions.**
     *
     * @param event
     *
     * @param fileName The path to the log file.
     * @param append If true will append to fileName. Otherwise will truncate fileName.
     */
    @Throws(IOException::class)
    protected fun setFile(append: Boolean) {
        synchronized(sync) {
            StatusLogger.getLogger().debug("setFile called: [$res], $append")
            reset()
            val parent: Resource = res.getParentResource()
            if (!parent.exists()) parent.createDirectory(true)
            val writeHeader = !append || res.length() === 0 // this must happen before we open the stream
            size = res.length()
            writer = OutputStreamWriter(RetireOutputStream(res, append, timeout, listener), charset)
            if (writeHeader) {
                val header = String(getLayout().getHeader(), charset)
                size += header.length()
                writer.write(header)
                writer.flush()
                // TODO new line?
            }
            StatusLogger.getLogger().debug("setFile ended")
        }
    }

    @Throws(IOException::class)
    private fun rollOver() {
        setFile(append)
        val footer = String(getLayout().getFooter(), charset)
        size += footer.length()
        writer.write(footer)
        closeFile()
        var target: Resource
        var file: Resource
        var renameSucceeded = true
        val parent: Resource = res.getParentResource()

        // If maxBackups <= 0, then there is no file renaming to be done.
        if (maxfiles > 0) {
            // Delete the oldest file, to keep Windows happy.
            file = parent.getRealResource(res.getName().toString() + "." + maxfiles + ".bak")
            if (file.exists()) renameSucceeded = file.delete()

            // Map {(maxBackupIndex - 1), ..., 2, 1} to {maxBackupIndex, ..., 3, 2}
            var i = maxfiles - 1
            while (i >= 1 && renameSucceeded) {
                file = parent.getRealResource(res.getName().toString() + "." + i + ".bak")
                if (file.exists()) {
                    target = parent.getRealResource(res.getName().toString() + "." + (i + 1) + ".bak")
                    StatusLogger.getLogger().debug("Renaming log file [$file] to [$target]")
                    renameSucceeded = file.renameTo(target)
                }
                i--
            }
            if (renameSucceeded) {
                // Rename fileName to fileName.1
                target = parent.getRealResource(res.getName().toString() + ".1.bak")
                file = res
                StatusLogger.getLogger().debug("Renaming log file [$file] to [$target]")
                renameSucceeded = file.renameTo(target)

                // if file rename failed, reopen file with append = true
                if (!renameSucceeded) {
                    try {
                        setFile(true)
                    } catch (e: IOException) {
                        LogUtil.logGlobal(ThreadLocalPageContext.getConfig(), "log-loading", "setFile([$res], true) call failed.", e)
                    }
                }
            }
        }

        // if all renames were successful, then
        if (renameSucceeded) {
            try {
                // This will also close the file. This is OK since multiple
                // close operations are safe.
                setFile(false)
            } catch (e: IOException) {
                LogUtil.logGlobal(ThreadLocalPageContext.getConfig(), "log-loading", "setFile([$res], false) call failed.", e)
            }
        }
    }

    protected fun reset() {
        closeFile()
    }

    protected fun closeFile() {
        size = 0
        if (writer != null) {
            try {
                writer.flush()
                writer.close()
                writer = null
            } catch (e: java.io.IOException) {
                LogUtil.logGlobal(ThreadLocalPageContext.getConfig(), "log-loading", "Could not close [$res]", e)
            }
        }
    }

    companion object {
        const val DEFAULT_MAX_FILE_SIZE = (10 * 1024 * 1024).toLong()
        const val DEFAULT_MAX_BACKUP_INDEX = 10
    }

    init {
        this.res = res
        this.charset = charset
        this.append = append
        this.timeout = timeout
        this.listener = listener
        this.maxFileSize = maxFileSize
        this.maxfiles = maxfiles
        setFile(append)
        token = SystemUtil.createToken("ResourceAppender", res.getAbsolutePath())
    }
}