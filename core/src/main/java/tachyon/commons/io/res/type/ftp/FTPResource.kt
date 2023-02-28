/**
 * Copyright (c) 2023, TachyonCFML.org
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
package tachyon.commons.io.res.type.ftp

import java.io.ByteArrayInputStream

class FTPResource : ResourceSupport {
    private val provider: FTPResourceProvider
    val innerParent: String

    @get:Override
    val name: String
    private val data: FTPConnectionData

    /**
     * Constructor of the class
     *
     * @param factory
     * @param data
     * @param path
     */
    internal constructor(provider: FTPResourceProvider, data: FTPConnectionData, path: String?) {
        this.provider = provider
        this.data = data
        val pathName: Array<String> = ResourceUtil.translatePathName(path)
        innerParent = pathName[0]
        name = pathName[1]
    }

    /**
     * Constructor of the class
     *
     * @param factory
     * @param data
     * @param path
     */
    private constructor(provider: FTPResourceProvider, data: FTPConnectionData, path: String, name: String) {
        this.provider = provider
        this.data = data
        innerParent = path
        this.name = name
    }

    @get:Override
    val isReadable: Boolean
        get() {
            val rtn = hasPermission(FTPFile.READ_PERMISSION) ?: return false
            return rtn.booleanValue()
        }

    @get:Override
    val isWriteable: Boolean
        get() {
            val rtn = hasPermission(FTPFile.WRITE_PERMISSION) ?: return false
            return rtn.booleanValue()
        }

    private fun hasPermission(permission: Int): Boolean? {
        var client: FTPResourceClient? = null
        return try {
            provider.read(this)
            client = provider.getClient(data)
            val file: FTPFile = client.getFTPFile(this) ?: return null
            Caster.toBoolean(file.hasPermission(FTPFile.USER_ACCESS, permission) || file.hasPermission(FTPFile.GROUP_ACCESS, permission)
                    || file.hasPermission(FTPFile.WORLD_ACCESS, permission))
        } catch (e: IOException) {
            Boolean.FALSE
        } finally {
            provider.returnClient(client)
        }
    }

    @Override
    @Throws(IOException::class)
    fun remove(alsoRemoveChildren: Boolean) {
        if (isRoot) throw FTPResoucreException("Can't delete root of ftp server")
        if (alsoRemoveChildren) ResourceUtil.removeChildren(this)
        var client: FTPResourceClient? = null
        try {
            provider.lock(this)
            client = provider.getClient(data)
            val result: Boolean = client.deleteFile(innerPath)
            if (!result) throw IOException("Can't delete file [" + getPath() + "]")
        } finally {
            provider.returnClient(client)
            provider.unlock(this)
        }
    }

    @Override
    fun delete(): Boolean {
        if (isRoot) return false
        var client: FTPResourceClient? = null
        return try {
            provider.lock(this)
            client = provider.getClient(data)
            client.deleteFile(innerPath)
        } catch (e: IOException) {
            false
        } finally {
            provider.returnClient(client)
            provider.unlock(this)
        }
    }

    @Override
    fun exists(): Boolean {
        try {
            provider.read(this)
        } catch (e: IOException) {
            return true
        }
        var client: FTPResourceClient? = null
        val `is`: InputStream? = null
        return try {
            // getClient has to be first to check connection
            client = provider.getClient(data)
            if (isRoot) return true
            val file: FTPFile = client.getFTPFile(this)
            if (file != null) {
                return !file.isUnknown()
            }

            // String pathname = getInnerPath();
            var p = innerPath
            if (!StringUtil.endsWith(p, '/')) p += "/"
            if (client.listNames(p) != null) true else false
        } catch (e: IOException) {
            false
        } finally {
            IOUtil.closeEL(`is`)
            provider.returnClient(client)
        }
    }

    @get:Override
    val parent: String?
        get() = if (isRoot) null else provider.getScheme().concat("://").concat(data.key()).concat(innerParent.substring(0, innerParent.length() - 1))

    @get:Override
    val parentResource: Resource?
        get() = if (isRoot) null else FTPResource(provider, data, innerParent)

    @Override
    fun getRealResource(realpath: String): Resource? {
        var realpath = realpath
        realpath = ResourceUtil.merge(innerPath, realpath)
        return if (realpath.startsWith("../")) null else FTPResource(provider, data, realpath)
    }

    @Override
    fun getPath(): String {
        return provider.getScheme().concat("://").concat(data.key()).concat(innerParent).concat(name)
    }

    /**
     * @return returns path starting from ftp root
     */
    val innerPath: String
        get() = innerParent.concat(name)

    @get:Override
    val isAbsolute: Boolean
        get() = true

    // getClient has to be first to check connection
    // if(file==null) return false;
    // return file.isDirectory();
    @get:Override
    val isDirectory: Boolean
        get() {
            try {
                provider.read(this)
            } catch (e1: IOException) {
                return false
            }
            var client: FTPResourceClient? = null
            return try {
                // getClient has to be first to check connection
                client = provider.getClient(data)
                if (isRoot) return true
                val file: FTPFile = client.getFTPFile(this)
                if (file != null) {
                    return file.isDirectory()
                }
                // if(file==null) return false;
                // return file.isDirectory();
                var p = innerPath
                if (!StringUtil.endsWith(p, '/')) p += "/"
                client.listNames(p) != null
            } catch (e: IOException) {
                false
            } finally {
                provider.returnClient(client)
            }
        }

    // String pathname = getInnerPath();
    // return (is=client.retrieveFileStream(pathname))!=null;
    @get:Override
    val isFile: Boolean
        get() {
            if (isRoot) return false
            try {
                provider.read(this)
            } catch (e1: IOException) {
                return false
            }
            var client: FTPResourceClient? = null
            val `is`: InputStream? = null
            return try {
                client = provider.getClient(data)
                val file: FTPFile = client.getFTPFile(this)
                if (file != null) {
                    file.isFile()
                } else false
                // String pathname = getInnerPath();
                // return (is=client.retrieveFileStream(pathname))!=null;
            } catch (e: IOException) {
                false
            } finally {
                IOUtil.closeEL(`is`)
                provider.returnClient(client)
            }
        }

    @Override
    fun lastModified(): Long {
        // if(isRoot()) return 0;
        var client: FTPResourceClient? = null
        return try {
            provider.read(this)
            client = provider.getClient(data)
            val file: FTPFile = client.getFTPFile(this) ?: return 0
            file.getTimestamp().getTimeInMillis()
        } catch (e: IOException) {
            0
        } finally {
            provider.returnClient(client)
        }
    }

    @Override
    fun length(): Long {
        if (isRoot) return 0
        var client: FTPResourceClient? = null
        return try {
            provider.read(this)
            client = provider.getClient(data)
            val file: FTPFile = client.getFTPFile(this) ?: return 0
            file.getSize()
        } catch (e: IOException) {
            0
        } finally {
            provider.returnClient(client)
        }
    }

    @Override
    fun listResources(): Array<Resource?>? {
        if (isFile) return null // new Resource[0];
        var client: FTPResourceClient? = null
        return try {
            client = provider.getClient(data)
            var files: Array<FTPFile>? = null
            var p = innerPath
            if (!StringUtil.endsWith(p, '/')) p += "/"
            files = client.listFiles(p)
            if (files == null) return arrayOfNulls<Resource>(0)
            val list: List<FTPResource> = ArrayList<FTPResource>()
            var parent: String = innerParent.concat(name)
            if (!StringUtil.endsWith(parent, '/')) parent += "/"
            var name: String
            var res: FTPResource
            for (i in files.indices) {
                name = files[i].getName()
                if (!".".equals(name) && !"..".equals(name)) {
                    res = FTPResource(provider, data, parent, name)
                    client.registerFTPFile(res, files[i])
                    list.add(res)
                }
            }
            list.toArray(arrayOfNulls<FTPResource>(list.size()))
        } catch (ioe: IOException) {
            null
        } finally {
            provider.returnClient(client)
        }
    }

    @Override
    fun setLastModified(time: Long): Boolean {
        // if(isRoot()) return false;
        var client: FTPResourceClient? = null
        try {
            provider.lock(this)
            client = provider.getClient(data)
            val pc: PageContext = ThreadLocalPageContext.get()
            val c: Calendar = JREDateTimeUtil.getThreadCalendar()
            if (pc != null) c.setTimeZone(pc.getTimeZone())
            c.setTimeInMillis(time)
            val file: FTPFile = client.getFTPFile(this) ?: return false
            file.setTimestamp(c)
            client.unregisterFTPFile(this)
            return true
        } catch (e: IOException) {
        } finally {
            provider.returnClient(client)
            provider.unlock(this)
        }
        return false
    }

    @Override
    fun setReadOnly(): Boolean {
        return try {
            mode = ModeUtil.setWritable(mode, false)
            true
        } catch (e: IOException) {
            false
        }
    }

    @Override
    @Throws(IOException::class)
    fun createFile(createParentWhenNotExists: Boolean) {
        ResourceUtil.checkCreateFileOK(this, createParentWhenNotExists)
        // client.unregisterFTPFile(this);
        IOUtil.copy(ByteArrayInputStream(ByteArray(0)), getOutputStream(), true, true)
    }

    @Override
    @Throws(IOException::class)
    fun moveTo(dest: Resource) {
        var client: FTPResourceClient? = null
        ResourceUtil.checkMoveToOK(this, dest)
        try {
            provider.lock(this)
            client = provider.getClient(data)
            client.unregisterFTPFile(this)
            if (dest is FTPResource) moveTo(client, dest as FTPResource) else super.moveTo(dest)
        } finally {
            provider.returnClient(client)
            provider.unlock(this)
        }
    }

    @Throws(IOException::class)
    private fun moveTo(client: FTPResourceClient?, dest: FTPResource) {
        if (!dest.data.equals(data)) {
            super.moveTo(dest)
            return
        }
        if (dest.exists()) dest.delete()
        client!!.unregisterFTPFile(dest)
        val ok: Boolean = client.rename(innerPath, dest.innerPath)
        if (!ok) throw IOException("can't create file $this")
    }

    @Override
    @Throws(IOException::class)
    fun createDirectory(createParentWhenNotExists: Boolean) {
        ResourceUtil.checkCreateDirectoryOK(this, createParentWhenNotExists)
        var client: FTPResourceClient? = null
        try {
            provider.lock(this)
            client = provider.getClient(data)
            client.unregisterFTPFile(this)
            val ok: Boolean = client.makeDirectory(innerPath)
            if (!ok) throw IOException("can't create file $this")
        } finally {
            provider.returnClient(client)
            provider.unlock(this)
        }
    }

    @get:Throws(IOException::class)
    @get:Override
    val inputStream: InputStream
        get() {
            ResourceUtil.checkGetInputStreamOK(this)
            provider.lock(this)
            val client: FTPResourceClient = provider.getClient(data)
            client.setFileType(FTP.BINARY_FILE_TYPE)
            return try {
                IOUtil.toBufferedInputStream(FTPResourceInputStream(client, this, client.retrieveFileStream(innerPath)))
            } catch (e: IOException) {
                provider.returnClient(client)
                provider.unlock(this)
                throw e
            }
        }

    @Override
    @Throws(IOException::class)
    fun getOutputStream(append: Boolean): OutputStream {
        ResourceUtil.checkGetOutputStreamOK(this)
        var client: FTPResourceClient? = null
        return try {
            provider.lock(this)
            client = provider.getClient(data)
            client.unregisterFTPFile(this)
            client.setFileType(FTP.BINARY_FILE_TYPE)
            val os: OutputStream = (if (append) client.appendFileStream(innerPath) else client.storeFileStream(innerPath))
                    ?: throw IOException("Can't open stream to file [$this]")
            IOUtil.toBufferedOutputStream(FTPResourceOutputStream(client, this, os))
        } catch (e: IOException) {
            provider.returnClient(client)
            provider.unlock(this)
            throw e
        }
    }

    @Override
    fun list(): Array<String?>? {
        if (isFile) return arrayOfNulls(0)
        var client: FTPResourceClient? = null
        return try {
            client = provider.getClient(data)
            var files: Array<String?>? = null
            var p = innerPath
            if (!StringUtil.endsWith(p, '/')) p += "/"
            files = client.listNames(p)
            if (files == null) return arrayOfNulls(0)
            for (i in files.indices) {
                files[i] = cutName(files[i])
            }
            files
        } catch (ioe: IOException) {
            null
        } finally {
            provider.returnClient(client)
        }
    }

    private fun cutName(path: String?): String? {
        val index: Int = path.lastIndexOf('/')
        return if (index == -1) path else path.substring(index + 1)
    }

    @get:Override
    val resourceProvider: ResourceProvider
        get() = provider
    val fTPResourceProvider: tachyon.commons.io.res.type.ftp.FTPResourceProvider
        get() = provider
    val isRoot: Boolean
        get() = StringUtil.isEmpty(name)// World

    // Group// World

    // Group

    // Owner

    // if(isRoot()) throw new IOException("can't change mode of root");
    // Owner
    // if(isRoot()) return 0;
    @get:Override
    @set:Throws(IOException::class)
    @set:Override
    var mode: Int
        get() {
            // if(isRoot()) return 0;
            var client: FTPResourceClient? = null
            try {
                provider.read(this)
                client = provider.getClient(data)
                val file: FTPFile = client.getFTPFile(this)
                var mode = 0
                if (file == null) return 0

                // World
                if (file.hasPermission(FTPFile.WORLD_ACCESS, FTPFile.EXECUTE_PERMISSION)) mode += 1
                if (file.hasPermission(FTPFile.WORLD_ACCESS, FTPFile.WRITE_PERMISSION)) mode += 2
                if (file.hasPermission(FTPFile.WORLD_ACCESS, FTPFile.READ_PERMISSION)) mode += 4

                // Group
                if (file.hasPermission(FTPFile.GROUP_ACCESS, FTPFile.EXECUTE_PERMISSION)) mode += 8
                if (file.hasPermission(FTPFile.GROUP_ACCESS, FTPFile.WRITE_PERMISSION)) mode += 16
                if (file.hasPermission(FTPFile.GROUP_ACCESS, FTPFile.READ_PERMISSION)) mode += 32

                // Owner
                if (file.hasPermission(FTPFile.USER_ACCESS, FTPFile.EXECUTE_PERMISSION)) mode += 64
                if (file.hasPermission(FTPFile.USER_ACCESS, FTPFile.WRITE_PERMISSION)) mode += 128
                if (file.hasPermission(FTPFile.USER_ACCESS, FTPFile.READ_PERMISSION)) mode += 256
                return mode
            } catch (e: IOException) {
            } finally {
                provider.returnClient(client)
            }
            return 0
        }
        set(mode) {
            // if(isRoot()) throw new IOException("can't change mode of root");
            var client: FTPResourceClient? = null
            try {
                provider.lock(this)
                client = provider.getClient(data)
                val file: FTPFile = client.getFTPFile(this)
                if (file != null) {
                    // World
                    file.setPermission(FTPFile.WORLD_ACCESS, FTPFile.EXECUTE_PERMISSION, mode and 1 > 0)
                    file.setPermission(FTPFile.WORLD_ACCESS, FTPFile.WRITE_PERMISSION, mode and 2 > 0)
                    file.setPermission(FTPFile.WORLD_ACCESS, FTPFile.READ_PERMISSION, mode and 4 > 0)

                    // Group
                    file.setPermission(FTPFile.GROUP_ACCESS, FTPFile.EXECUTE_PERMISSION, mode and 8 > 0)
                    file.setPermission(FTPFile.GROUP_ACCESS, FTPFile.WRITE_PERMISSION, mode and 16 > 0)
                    file.setPermission(FTPFile.GROUP_ACCESS, FTPFile.READ_PERMISSION, mode and 32 > 0)

                    // Owner
                    file.setPermission(FTPFile.USER_ACCESS, FTPFile.EXECUTE_PERMISSION, mode and 64 > 0)
                    file.setPermission(FTPFile.USER_ACCESS, FTPFile.WRITE_PERMISSION, mode and 128 > 0)
                    file.setPermission(FTPFile.USER_ACCESS, FTPFile.READ_PERMISSION, mode and 256 > 0)
                    client.unregisterFTPFile(this)
                }
            } catch (e: IOException) {
            } finally {
                provider.returnClient(client)
                provider.unlock(this)
            }
        }

    @Override
    fun setReadable(value: Boolean): Boolean {
        return try {
            mode = ModeUtil.setReadable(mode, value)
            true
        } catch (e: IOException) {
            false
        }
    }

    @Override
    fun setWritable(value: Boolean): Boolean {
        return try {
            mode = ModeUtil.setWritable(mode, value)
            true
        } catch (e: IOException) {
            false
        }
    }
}