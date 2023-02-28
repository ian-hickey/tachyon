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
package tachyon.commons.io.res.type.smb

import java.io.ByteArrayInputStream

class SMBResource private constructor(provider: SMBResourceProvider) : ResourceSupport(), Resource {
    private val provider: SMBResourceProvider
    private var path: String? = null
    private var auth: NtlmPasswordAuthentication? = null
    private var _smbFile: SmbFile? = null
    private var _smbDir: SmbFile? = null

    constructor(provider: SMBResourceProvider, path: String) : this(provider) {
        _init(_stripAuth(path), _extractAuth(path))
    }

    constructor(provider: SMBResourceProvider, path: String, auth: NtlmPasswordAuthentication) : this(provider) {
        _init(path, auth)
    }

    constructor(provider: SMBResourceProvider, parent: String, child: String?) : this(provider) {
        _init(ResourceUtil.merge(_stripAuth(parent), child), _extractAuth(parent))
    }

    constructor(provider: SMBResourceProvider, parent: String, child: String?, auth: NtlmPasswordAuthentication) : this(provider) {
        _init(ResourceUtil.merge(_stripAuth(parent), child), auth)
    }

    private fun _init(path: String, auth: NtlmPasswordAuthentication) {
        // String[] pathName=ResourceUtil.translatePathName(path);
        this.path = _stripScheme(path)
        this.auth = auth
    }

    private fun _stripScheme(path: String): String {
        return path.replace(_scheme(), "/")
    }

    private fun _userInfo(path: String): String {
        return try {
            // use http scheme just so we can parse the url and get the user info out
            var schemeless = _stripScheme(path)
            schemeless = schemeless.replaceFirst("^/", "")
            val result: String = URL("http://".concat(schemeless)).getUserInfo()
            SMBResourceProvider.unencryptUserInfo(result)
        } catch (e: MalformedURLException) {
            ""
        }
    }

    private fun _extractAuth(path: String): NtlmPasswordAuthentication {
        return NtlmPasswordAuthentication(_userInfo(path))
    }

    private fun _stripAuth(path: String): String {
        return _calculatePath(path).replaceFirst(_scheme().concat("[^/]*@"), "")
    }

    private fun _file(): SmbFile? {
        return _file(false)
    }

    private fun _file(expectDirectory: Boolean): SmbFile {
        var _path = _calculatePath(innerPath)
        val result: SmbFile?
        if (expectDirectory) {
            if (!_path.endsWith("/")) _path += "/"
            if (_smbDir == null) {
                _smbDir = provider.getFile(_path, auth)
            }
            result = _smbDir
        } else {
            if (_smbFile == null) {
                _smbFile = provider.getFile(_path, auth)
            }
            result = _smbFile
        }
        return result
    }

    private fun _calculatePath(path: String): String? {
        return _calculatePath(path, null)
    }

    private fun _calculatePath(path: String?, auth: NtlmPasswordAuthentication?): String? {
        var path = path
        if (!path.startsWith(_scheme())) {
            if (path.startsWith("/") || path.startsWith("\\")) {
                path = path.substring(1)
            }
            if (auth != null) {
                path = SMBResourceProvider.encryptUserInfo(_userInfo(auth, false)).concat("@").concat(path)
            }
            path = _scheme().concat(path)
        }
        return path
    }

    private fun _scheme(): String {
        return provider.getScheme().concat("://")
    }

    @get:Override
    val isReadable: Boolean
        get() {
            val file: SmbFile? = _file()
            return try {
                file != null && file.canRead()
            } catch (e: SmbException) {
                false
            }
        }

    // canWrite() doesn't work on shares. always returns false even if you can truly write, test this by
    // opening a file on the share
    @get:Override
    val isWriteable: Boolean
        get() {
            val file: SmbFile = _file() ?: return false
            try {
                if (file.canWrite()) return true
            } catch (e1: SmbException) {
                return false
            }
            return try {
                if (file.getType() === SmbFile.TYPE_SHARE) {
                    // canWrite() doesn't work on shares. always returns false even if you can truly write, test this by
                    // opening a file on the share
                    val testFile: SmbFile = _getTempFile(file, auth) ?: return false
                    if (testFile.canWrite()) return true
                    var os: OutputStream? = null
                    os = try {
                        testFile.getOutputStream()
                    } catch (e: IOException) {
                        return false
                    } finally {
                        if (os != null) IOUtils.closeQuietly(os)
                        testFile.delete()
                    }
                    return true
                }
                file.canWrite()
            } catch (e: SmbException) {
                false
            }
        }

    @Throws(SmbException::class)
    private fun _getTempFile(directory: SmbFile, auth: NtlmPasswordAuthentication): SmbFile? {
        if (!directory.isDirectory()) return null
        val r = Random()
        val result: SmbFile = provider.getFile(directory.getCanonicalPath().toString() + "/write-test-file.unknown." + r.nextInt(), auth)
        return if (result.exists()) _getTempFile(directory, auth) else result // try again
    }

    @Override
    @Throws(IOException::class)
    fun remove(alsoRemoveChildren: Boolean) {
        if (alsoRemoveChildren) ResourceUtil.removeChildren(this)
        _delete()
    }

    @Throws(IOException::class)
    private fun _delete() {
        provider.lock(this)
        try {
            var file: SmbFile = _file()
                    ?: throw IOException("Can't delete [" + getPath() + "], SMB path is invalid or inaccessible")
            if (file.isDirectory()) {
                file = _file(true)
            }
            file.delete()
        } catch (e: SmbException) {
            throw IOException(e) // for cfcatch type="java.io.IOException"
        } finally {
            provider.unlock(this)
        }
    }

    @Override
    fun exists(): Boolean {
        val file: SmbFile? = _file()
        return try {
            file != null && file.exists()
        } catch (e: SmbException) {
            false
        }
    }

    // remote trailing slash for directories
    @get:Override
    val name: String
        get() {
            val file: SmbFile = _file() ?: return ""
            return file.getName().replaceFirst("/$", "") // remote trailing slash for directories
        }

    // SmbFile's getParent function seems to return just smb:// no matter what, implement custom
    // getParent Function()
    @get:Override
    val parent: String
        get() {
            // SmbFile's getParent function seems to return just smb:// no matter what, implement custom
            // getParent Function()
            val path: String = getPath().replaceFirst("[\\\\/]+$", "")
            val location: Int = Math.max(path.lastIndexOf('/'), path.lastIndexOf('\\'))
            return if (location == -1 || location == 0) "" else path.substring(0, location)
        }

    @get:Override
    val parentResource: Resource?
        get() {
            val p = parent ?: return null
            return SMBResource(provider, _stripAuth(p), auth)
        }

    @Override
    fun getRealResource(realpath: String): Resource? {
        var realpath = realpath
        realpath = ResourceUtil.merge("$innerPath/", realpath)
        return if (realpath.startsWith("../")) null else SMBResource(provider, _calculatePath(realpath, auth), auth)
    }

    private val innerPath: String
        private get() = if (path == null) "/" else path

    @Override
    fun getPath(): String? {
        return _calculatePath(path, auth)
    }

    @get:Override
    val isAbsolute: Boolean
        get() = _file() != null

    @get:Override
    val isDirectory: Boolean
        get() {
            val file: SmbFile? = _file()
            return try {
                file != null && _file().isDirectory()
            } catch (e: SmbException) {
                false
            }
        }

    @get:Override
    val isFile: Boolean
        get() {
            val file: SmbFile? = _file()
            return try {
                file != null && file.isFile()
            } catch (e: SmbException) {
                false
            }
        }

    @get:Override
    @set:Throws(IOException::class)
    @set:Override
    var isHidden: Boolean
        get() = _isFlagSet(_file(), SmbFile.ATTR_HIDDEN)
        set(value) {
            setAttribute(SmbFile.ATTR_SYSTEM as Short, value)
        }

    @get:Override
    @set:Throws(IOException::class)
    @set:Override
    var isArchive: Boolean
        get() = _isFlagSet(_file(), SmbFile.ATTR_ARCHIVE)
        set(value) {
            setAttribute(SmbFile.ATTR_ARCHIVE as Short, value)
        }

    @get:Override
    @set:Throws(IOException::class)
    @set:Override
    var isSystem: Boolean
        get() = _isFlagSet(_file(), SmbFile.ATTR_SYSTEM)
        set(value) {
            setAttribute(SmbFile.ATTR_SYSTEM as Short, value)
        }

    private fun _isFlagSet(file: SmbFile?, flag: Int): Boolean {
        return if (file == null) false else try {
            file.getAttributes() and flag === flag
        } catch (e: SmbException) {
            false
        }
    }

    @Override
    fun lastModified(): Long {
        val file: SmbFile = _file() ?: return 0
        return try {
            file.lastModified()
        } catch (e: SmbException) {
            0
        }
    }

    @Override
    fun length(): Long {
        val file: SmbFile = _file() ?: return 0
        return try {
            file.length()
        } catch (e: SmbException) {
            0
        }
    }

    @Override
    fun listResources(): Array<Resource?>? {
        return if (isFile) null else try {
            val dir: SmbFile = _file(true)
            val files: Array<SmbFile> = dir.listFiles()
            val result: Array<Resource?> = arrayOfNulls<Resource>(files.size)
            for (i in files.indices) {
                val file: SmbFile = files[i]
                result[i] = SMBResource(provider, file.getCanonicalPath(), auth)
            }
            result
        } catch (e: SmbException) {
            arrayOfNulls<Resource>(0)
        }
    }

    @Override
    fun setLastModified(time: Long): Boolean {
        val file: SmbFile = _file() ?: return false
        try {
            provider.lock(this)
            file.setLastModified(time)
        } catch (e: SmbException) {
            return false
        } catch (e: IOException) {
            return false
        } finally {
            provider.unlock(this)
        }
        return true
    }

    @Override
    fun setWritable(writable: Boolean): Boolean {
        val file: SmbFile = _file() ?: return false
        try {
            setAttribute(SmbFile.ATTR_READONLY as Short, !writable)
        } catch (e1: IOException) {
            return false
        }
        return true
    }

    @Override
    fun setReadable(readable: Boolean): Boolean {
        return setWritable(!readable)
    }

    @Override
    @Throws(IOException::class)
    fun createFile(createParentWhenNotExists: Boolean) {
        try {
            ResourceUtil.checkCreateFileOK(this, createParentWhenNotExists)
            // client.unregisterFTPFile(this);
            IOUtil.copy(ByteArrayInputStream(ByteArray(0)), getOutputStream(), true, true)
        } catch (e: SmbException) {
            throw IOException(e) // for cfcatch type="java.io.IOException"
        }
    }

    @Override
    @Throws(IOException::class)
    fun createDirectory(createParentWhenNotExists: Boolean) {
        val file: SmbFile = _file(true) ?: throw IOException("SMBFile is inaccessible")
        ResourceUtil.checkCreateDirectoryOK(this, createParentWhenNotExists)
        try {
            provider.lock(this)
            file.mkdir()
        } catch (e: SmbException) {
            throw IOException(e) // for cfcatch type="java.io.IOException"
        } finally {
            provider.unlock(this)
        }
    }

    // for cfcatch type="java.io.IOException"
    @get:Throws(IOException::class)
    @get:Override
    val inputStream: InputStream
        get() = try {
            _file().getInputStream()
        } catch (e: SmbException) {
            throw IOException(e) // for cfcatch type="java.io.IOException"
        }

    @Override
    @Throws(IOException::class)
    fun getOutputStream(append: Boolean): OutputStream {
        ResourceUtil.checkGetOutputStreamOK(this)
        return try {
            provider.lock(this)
            val file: SmbFile? = _file()
            val os: OutputStream = SmbFileOutputStream(file, append)
            IOUtil.toBufferedOutputStream(ResourceOutputStream(this, os))
        } catch (e: IOException) {
            provider.unlock(this)
            throw IOException(e) // just in case it is an SmbException too... for cfcatch type="java.io.IOException"
        }
    }

    @get:Override
    val resourceProvider: ResourceProvider
        get() = provider

    // TODO
    @get:Override
    @set:Throws(IOException::class)
    @set:Override
    var mode: Int
        get() = 0
        set(mode) {
            // TODO
        }

    @Override
    @Throws(IOException::class)
    fun setAttribute(attribute: Short, value: Boolean) {
        val newAttribute = _lookupAttribute(attribute)
        val file: SmbFile = _file() ?: throw IOException("SMB File is not valid")
        try {
            provider.lock(this)
            var atts: Int = file.getAttributes()
            atts = if (value) {
                atts or newAttribute
            } else {
                atts and newAttribute.inv()
            }
            file.setAttributes(atts)
        } catch (e: SmbException) {
            throw IOException(e) // for cfcatch type="java.io.IOException"
        } finally {
            provider.unlock(this)
        }
    }

    @Override
    @Throws(IOException::class)
    fun moveTo(dest: Resource) {
        try {
            if (dest is SMBResource) {
                val destination = dest as SMBResource
                val file: SmbFile? = _file()
                file.renameTo(destination._file())
            } else {
                ResourceUtil.moveTo(this, dest, false)
            }
        } catch (e: SmbException) {
            throw IOException(e) // for cfcatch type="java.io.IOException"
        }
    }

    @Override
    fun getAttribute(attribute: Short): Boolean {
        return try {
            val newAttribute = _lookupAttribute(attribute)
            _file().getAttributes() and newAttribute !== 0
        } catch (e: SmbException) {
            false
        }
    }

    val smbFile: SmbFile?
        get() = _file()

    private fun _lookupAttribute(attribute: Short): Int {
        var result = attribute.toInt()
        when (attribute) {
            Resource.ATTRIBUTE_ARCHIVE -> result = SmbFile.ATTR_ARCHIVE
            Resource.ATTRIBUTE_SYSTEM -> result = SmbFile.ATTR_SYSTEM
            Resource.ATTRIBUTE_HIDDEN -> result = SmbFile.ATTR_HIDDEN
        }
        return result
    }

    companion object {
        private fun _userInfo(auth: NtlmPasswordAuthentication?, addAtSign: Boolean): String {
            var result = ""
            if (auth != null) {
                if (!StringUtils.isEmpty(auth.getDomain())) {
                    result += auth.getDomain().toString() + ";"
                }
                if (!StringUtils.isEmpty(auth.getUsername())) {
                    result += auth.getUsername().toString() + ":"
                }
                if (!StringUtils.isEmpty(auth.getPassword())) {
                    result += auth.getPassword()
                }
                if (addAtSign && !StringUtils.isEmpty(result)) {
                    result += "@"
                }
            }
            return result
        }
    }

    init {
        this.provider = provider
    }
}