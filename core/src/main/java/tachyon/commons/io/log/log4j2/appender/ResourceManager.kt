package tachyon.commons.io.log.log4j2.appender

import java.io.FileOutputStream

/**
 * Manages actual File I/O for File Appenders.
 */
class ResourceManager protected constructor(loggerContext: LoggerContext?, fileName: String?, os: OutputStream?,
                                            /**
                                             * Returns the append status.
                                             *
                                             * @return true if the file will be appended to, false if it is overwritten.
                                             */
                                            val isAppend: Boolean,
                                            /**
                                             * Returns the lock status.
                                             *
                                             * @return true if the file will be locked when writing, false otherwise.
                                             */
                                            val isLocking: Boolean,
                                            /**
                                             * Returns the lazy-create.
                                             *
                                             * @return true if the file will be lazy-created.
                                             */
                                            val isCreateOnDemand: Boolean, private val advertiseURI: String, layout: Layout<out Serializable?>?, filePermissions: String?, fileOwner: String?,
                                            fileGroup: String?, writeHeader: Boolean, buffer: ByteBuffer) : OutputStreamManager(loggerContext, os, fileName, isCreateOnDemand, layout, writeHeader, buffer) {

    /**
     * Returns the buffer size to use if the appender was configured with BufferedIO=true, otherwise
     * returns a negative number.
     *
     * @return the buffer size, or a negative number if the output stream is not buffered
     */
    val bufferSize: Int
    private val filePermissions: Set<PosixFilePermission>? = null

    /**
     * Returns file owner if defined and the OS supports owner file attribute view, null otherwise.
     *
     * @return File owner
     * @see FileOwnerAttributeView
     */
    val fileOwner: String? = null

    /**
     * Returns file group if defined and the OS supports posix/group file attribute view, null
     * otherwise.
     *
     * @return File group
     * @see PosixFileAttributeView
     */
    val fileGroup: String? = null
    @Override
    @Throws(IOException::class)
    protected fun createOutputStream(): OutputStream {
        return createOutputStream(isAppend)
    }

    @Throws(IOException::class)
    protected fun createOutputStream(append: Boolean): OutputStream {
        val filename = fileName
        LOGGER.debug("Now writing to {} at {}", filename, Date())
        val res: Resource = createResource(filename)
        createParentDir(res)
        val os: OutputStream = res.getOutputStream(append)
        if (res.exists() && res.length() === 0) {
            try {
                res.setLastModified(System.currentTimeMillis())
            } catch (ex: Exception) {
                LogUtil.logGlobal(ThreadLocalPageContext.getConfig(), Log.LEVEL_WARN, "log-loading", "Unable to set current file time for $filename")
            }
            writeHeader(os)
        }
        return os
    }

    protected fun createParentDir(res: Resource) {
        res.getParentResource().mkdirs()
    }

    @Override
    @Synchronized
    protected fun write(bytes: ByteArray?, offset: Int, length: Int, immediateFlush: Boolean) {
        if (isLocking) {
            try {
                @SuppressWarnings("resource") val channel: FileChannel = (getOutputStream() as FileOutputStream).getChannel()
                channel.lock(0, Long.MAX_VALUE, false).use { lock -> super.write(bytes, offset, length, immediateFlush) }
            } catch (ex: IOException) {
                throw AppenderLoggingException("Unable to obtain lock on " + getName(), ex)
            }
        } else {
            super.write(bytes, offset, length, immediateFlush)
        }
    }

    /**
     * Overrides [OutputStreamManager.writeToDestination] to add support for
     * file locking.
     *
     * @param bytes the array containing data
     * @param offset from where to write
     * @param length how many bytes to write
     * @since 2.8
     */
    @Override
    @Synchronized
    protected fun writeToDestination(bytes: ByteArray?, offset: Int, length: Int) {
        if (isLocking) {
            try {
                @SuppressWarnings("resource") val channel: FileChannel = (getOutputStream() as FileOutputStream).getChannel()
                channel.lock(0, Long.MAX_VALUE, false).use { lock -> super.writeToDestination(bytes, offset, length) }
            } catch (ex: IOException) {
                throw AppenderLoggingException("Unable to obtain lock on " + getName(), ex)
            }
        } else {
            super.writeToDestination(bytes, offset, length)
        }
    }

    /**
     * Returns the name of the File being managed.
     *
     * @return The name of the File being managed.
     */
    val fileName: String
        get() = getName()

    /**
     * Returns posix file permissions if defined and the OS supports posix file attribute, null
     * otherwise.
     *
     * @return File posix permissions
     * @see PosixFileAttributeView
     */
    fun getFilePermissions(): Set<PosixFilePermission>? {
        return filePermissions
    }

    /**
     * FileManager's content format is specified by:
     * `Key: "fileURI" Value: provided "advertiseURI" param`.
     *
     * @return Map of content format keys supporting FileManager
     */
    @get:Override
    val contentFormat: Map<String, String>
        get() {
            val result: Map<String, String> = HashMap(super.getContentFormat())
            result.put("fileURI", advertiseURI)
            return result
        }

    /**
     * Factory Data.
     */
    private class FactoryData(val append: Boolean, val locking: Boolean, val bufferedIo: Boolean, val bufferSize: Int, val createOnDemand: Boolean, val advertiseURI: String,
                              layout: Layout<out Serializable?>, filePermissions: String, fileOwner: String, fileGroup: String, configuration: Configuration?) : ConfigurationFactoryData(configuration) {
        val layout: Layout<out Serializable?>
        val filePermissions: String
        val fileOwner: String
        val fileGroup: String

        /**
         * Constructor.
         *
         * @param append Append status.
         * @param locking Locking status.
         * @param bufferedIo Buffering flag.
         * @param bufferSize Buffer size.
         * @param createOnDemand if you want to lazy-create the file (a.k.a. on-demand.)
         * @param advertiseURI the URI to use when advertising the file
         * @param layout The layout
         * @param filePermissions File permissions
         * @param fileOwner File owner
         * @param fileGroup File group
         * @param configuration the configuration
         */
        init {
            this.layout = layout
            this.filePermissions = filePermissions
            this.fileOwner = fileOwner
            this.fileGroup = fileGroup
        }
    }

    /**
     * Factory to create a ResourceManager.
     */
    private class FileManagerFactory : ManagerFactory<ResourceManager?, FactoryData?> {
        /**
         * Creates a FileManager.
         *
         * @param name The name of the File.
         * @param data The FactoryData
         * @return The FileManager for the File.
         */
        @Override
        fun createManager(path: String, data: FactoryData): ResourceManager? {
            val res: Resource = createResource(path)
            try {
                res.getParentResource().mkdirs()
                val writeHeader = !data.append || !res.exists()
                val actualSize = if (data.bufferedIo) data.bufferSize else Constants.ENCODER_BYTE_BUFFER_SIZE
                val byteBuffer: ByteBuffer = ByteBuffer.wrap(ByteArray(actualSize))
                val os: OutputStream? = if (data.createOnDemand) null else res.getOutputStream(data.append)
                return ResourceManager(data.getLoggerContext(), path, os, data.append, data.locking, data.createOnDemand, data.advertiseURI, data.layout,
                        data.filePermissions, data.fileOwner, data.fileGroup, writeHeader, byteBuffer)
            } catch (ex: IOException) {
                LogUtil.logGlobal(ThreadLocalPageContext.getConfig(), Log.LEVEL_ERROR, "log-loading", "FileManager ($path) $ex")
            }
            return null
        }
    }

    companion object {
        private val FACTORY = FileManagerFactory()

        /**
         * Returns the FileManager.
         *
         * @param fileName The name of the file to manage.
         * @param append true if the file should be appended to, false if it should be overwritten.
         * @param locking true if the file should be locked while writing, false otherwise.
         * @param bufferedIo true if the contents should be buffered as they are written.
         * @param createOnDemand true if you want to lazy-create the file (a.k.a. on-demand.)
         * @param advertiseUri the URI to use when advertising the file
         * @param layout The layout
         * @param bufferSize buffer size for buffered IO
         * @param filePermissions File permissions
         * @param fileOwner File owner
         * @param fileGroup File group
         * @param configuration The configuration.
         * @return A FileManager for the File.
         */
        fun getFileManager(fileName: String?, append: Boolean, locking: Boolean, bufferedIo: Boolean, createOnDemand: Boolean,
                           advertiseUri: String, layout: Layout<out Serializable?>, bufferSize: Int, filePermissions: String, fileOwner: String,
                           fileGroup: String, configuration: Configuration?): ResourceManager {
            var locking = locking
            if (locking && bufferedIo) {
                locking = false
            }
            return narrow(ResourceManager::class.java, getManager(fileName,
                    FactoryData(append, locking, bufferedIo, bufferSize, createOnDemand, advertiseUri, layout, filePermissions, fileOwner, fileGroup, configuration), FACTORY))
        }

        protected fun createResource(path: String?): Resource {
            val config: Config = ThreadLocalPageContext.getConfig()
            return if (config != null) config.getResource(path) else ResourcesImpl.getFileResourceProvider().getResource(path)
        }
    }

    init {
        bufferSize = buffer.capacity()
        val views: Set<String> = FileSystems.getDefault().supportedFileAttributeViews()
        if (views.contains("posix")) {
            this.filePermissions = if (filePermissions != null) PosixFilePermissions.fromString(filePermissions) else null
            this.fileGroup = fileGroup
        } else {
            this.filePermissions = null
            this.fileGroup = null
            if (filePermissions != null) {
                LogUtil.logGlobal(ThreadLocalPageContext.getConfig(), Log.LEVEL_WARN, "log-loading",
                        "Posix file attribute permissions defined but it is not supported by this files system.")
            }
            if (fileGroup != null) {
                LogUtil.logGlobal(ThreadLocalPageContext.getConfig(), Log.LEVEL_WARN, "log-loading",
                        "Posix file attribute group defined but it is not supported by this files system.")
            }
        }
        if (views.contains("owner")) {
            this.fileOwner = fileOwner
        } else {
            this.fileOwner = null
            if (fileOwner != null) {
                LogUtil.logGlobal(ThreadLocalPageContext.getConfig(), Log.LEVEL_WARN, "log-loading", "Owner file attribute defined but it is not supported by this files system.")
            }
        }
    }
}