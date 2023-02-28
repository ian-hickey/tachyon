/**
 * Copyright (c) 2014, the Railo Company Ltd.
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
package lucee.commons.io.res.type.file

import java.io.BufferedInputStream

/**
 * Implementation og Resource for the local filesystem (java.io.File)
 */
class FileResource : File, Resource {
    private val provider: FileResourceProvider

    /**
     * Constructor for the factory
     *
     * @param pathname
     */
    internal constructor(provider: FileResourceProvider, pathname: String?) : super(pathname) {
        this.provider = provider
    }

    /**
     * Inner Constr constructor to create parent/child
     *
     * @param parent
     * @param child
     */
    private constructor(provider: FileResourceProvider, parent: File, child: String) : super(parent, child) {
        this.provider = provider
    }

    @Override
    @Throws(IOException::class)
    fun copyFrom(res: Resource, append: Boolean) {
        if (res is File && (!append || !isFile)) {
            try {
                Files.copy((res as File).toPath(), this.toPath(), COPY_OPTIONS)
                return
            } catch (exception: Exception) {
            }
        }
        IOUtil.copy(res, getOutputStream(append), true)

        // executable?
        val e = res is File && (res as File).canExecute()
        val w: Boolean = res.canWrite()
        val r: Boolean = res.canRead()
        if (e) this.setExecutable(true)
        if (w != canWrite()) setWritable(w)
        if (r != canRead()) setReadable(r)
    }

    @Override
    @Throws(IOException::class)
    fun copyTo(res: Resource, append: Boolean) {
        if (res is File && (!append || !res.isFile())) {
            try {
                Files.copy(this.toPath(), (res as File).toPath(), COPY_OPTIONS)
                return
            } catch (exception: Exception) {
            }
        }
        IOUtil.copy(this, res.getOutputStream(append), true)
        val e: Boolean = canExecute()
        val w = canWrite()
        val r = canRead()
        if (e && res is File) (res as File).setExecutable(true)
        if (w != res.canWrite()) res.setWritable(w)
        if (r != res.canRead()) res.setReadable(r)
    }

    @get:Override
    val absoluteResource: Resource
        get() = FileResource(provider, getAbsolutePath())

    @get:Throws(IOException::class)
    @get:Override
    val canonicalResource: Resource
        get() = FileResource(provider, getCanonicalPath())

    @get:Override
    val parentResource: Resource?
        get() {
            val p: String = getParent() ?: return null
            return FileResource(provider, p)
        }

    @Override
    fun listResources(): Array<Resource?>? {
        val files = list() ?: return null
        val resources: Array<Resource?> = arrayOfNulls<Resource>(files.size)
        for (i in files.indices) {
            resources[i] = getRealResource(files[i])
        }
        return resources
    }

    @Override
    fun list(filter: ResourceFilter): Array<String>? {
        val files = list() ?: return null
        val list: List<String> = ArrayList<String>()
        var res: FileResource
        for (i in files.indices) {
            res = FileResource(provider, this, files[i])
            if (filter.accept(res)) list.add(files[i])
        }
        return list.toArray(arrayOfNulls<String>(list.size()))
    }

    @Override
    fun listResources(filter: ResourceFilter): Array<Resource>? {
        val files = list() ?: return null
        val list: List<Resource> = ArrayList<Resource>()
        var res: Resource?
        for (i in files.indices) {
            res = getRealResource(files[i])
            if (filter.accept(res)) list.add(res)
        }
        return list.toArray(arrayOfNulls<FileResource>(list.size()))
    }

    @Override
    fun list(filter: ResourceNameFilter): Array<String>? {
        val files = list() ?: return null
        val list: List<String> = ArrayList<String>()
        for (i in files.indices) {
            if (filter.accept(this, files[i])) list.add(files[i])
        }
        return list.toArray(arrayOfNulls<String>(list.size()))
    }

    @Override
    fun listResources(filter: ResourceNameFilter): Array<Resource>? {
        val files = list() ?: return null
        val list: List<Resource> = ArrayList<Resource>()
        for (i in files.indices) {
            if (filter.accept(this, files[i])) list.add(getRealResource(files[i]))
        }
        return list.toArray(arrayOfNulls<Resource>(list.size()))
    }

    @Override
    @Throws(IOException::class)
    fun moveTo(dest: Resource) {
        if (this.equals(dest)) return
        var done = false
        if (dest is File) {
            provider.lock(this)
            try {
                if (dest.exists() && !dest.delete()) throw IOException("Can't move file [" + this.getAbsolutePath().toString() + "] cannot remove existing file [" + dest.getAbsolutePath().toString() + "]")
                done = super.renameTo(dest as File)
                /*
				 * if(!super.renameTo((File)dest)) { throw new
				 * IOException("can't move file "+this.getAbsolutePath()+" to destination resource "+dest.
				 * getAbsolutePath()); }
				 */
            } finally {
                provider.unlock(this)
            }
        }
        if (!done) {
            ResourceUtil.checkMoveToOK(this, dest)
            IOUtil.copy(inputStream, dest, true)
            if (!delete()) {
                throw IOException("Can't delete resource [" + this.getAbsolutePath().toString() + "]")
            }
        }
    }// provider.unlock(this);// return new BufferedInputStream(new FileInputStream(this));

    // provider.lock(this);
    @get:Throws(IOException::class)
    @get:Override
    val inputStream: InputStream
        get() {
            // provider.lock(this);
            provider.read(this)
            return try {
                BufferedInputStream(Files.newInputStream(toPath(), StandardOpenOption.READ))
                // return new BufferedInputStream(new FileInputStream(this));
            } catch (ioe: IOException) {
                // provider.unlock(this);
                throw ioe
            }
        }

    @get:Throws(IOException::class)
    @get:Override
    val outputStream: OutputStream
        get() = getOutputStream(false)

    @Override
    @Throws(IOException::class)
    fun getOutputStream(append: Boolean): OutputStream {
        provider.lock(this)
        return try {
            if (!super.exists() && !super.createNewFile()) {
                throw IOException("Can't create file [$this]")
            }
            BufferedOutputStream(ResourceOutputStream(this, FileOutputStream(this, append)))
        } catch (ioe: IOException) {
            provider.unlock(this)
            throw ioe
        }
    }

    @Override
    @Throws(IOException::class)
    fun createFile(createParentWhenNotExists: Boolean) {
        provider.lock(this)
        try {
            if (createParentWhenNotExists) {
                val p: File = super.getParentFile()
                if (!p.exists()) p.mkdirs()
            }
            if (!super.createNewFile()) {
                if (super.isFile()) throw IOException("Can't create file [$this], file already exists")
                throw IOException("Can't create file [$this]")
            }
        } finally {
            provider.unlock(this)
        }
    }

    @Override
    @Throws(IOException::class)
    fun remove(alsoRemoveChildren: Boolean) {
        if (alsoRemoveChildren && isDirectory) {
            val children: Array<Resource?>? = listResources()
            if (children != null) {
                for (i in children.indices) {
                    children[i].remove(alsoRemoveChildren)
                }
            }
        }
        provider.lock(this)
        try {
            if (!super.delete()) {
                if (!super.exists()) throw IOException("Can't delete file [$this], file does not exist")
                if (!super.canWrite()) throw IOException("Can't delete file [$this], no access")
                throw IOException("Can't delete file [$this]")
            }
        } finally {
            provider.unlock(this)
        }
    }

    @Override
    fun getReal(realpath: String): String {
        if (realpath.length() <= 2) {
            if (realpath.length() === 0) return getPath()
            if (realpath.equals(".")) return getPath()
            if (realpath.equals("..")) return getParent()
        }
        return FileResource(provider, this, realpath).getPath()
    }

    @Override
    fun getRealResource(realpath: String): Resource? {
        if (realpath.length() <= 2) {
            if (realpath.length() === 0) return this
            if (realpath.equals(".")) return this
            if (realpath.equals("..")) return parentResource
        }
        return FileResource(provider, this, realpath)
    }

    val contentType: ContentType
        get() = ResourceUtil.getContentType(this)

    @Override
    @Throws(IOException::class)
    fun createDirectory(createParentWhenNotExists: Boolean) {
        provider.lock(this)
        try {
            if (if (createParentWhenNotExists) !_mkdirs() else !super.mkdir()) {
                if (super.isDirectory()) throw IOException("Can't create directory [$this], directory already exists")
                throw IOException("Can't create directory [$this]")
            }
        } finally {
            provider.unlock(this)
        }
    }

    @get:Override
    val resourceProvider: ResourceProvider
        get() = provider

    @get:Override
    val isReadable: Boolean
        get() = canRead()

    @get:Override
    val isWriteable: Boolean
        get() = canWrite()

    @Override
    fun renameTo(dest: Resource): Boolean {
        try {
            moveTo(dest)
            return true
        } catch (e: IOException) {
        }
        return false
    }

    @get:Override
    @set:Throws(IOException::class)
    @set:Override
    var isArchive: Boolean
        get() = getAttribute(ATTRIBUTE_ARCHIVE)
        set(value) {
            setAttribute(ATTRIBUTE_ARCHIVE, value)
        }

    @get:Override
    @set:Throws(IOException::class)
    @set:Override
    var isSystem: Boolean
        get() = getAttribute(ATTRIBUTE_SYSTEM)
        set(value) {
            setAttribute(ATTRIBUTE_SYSTEM, value)
        }// print.ln(ModeUtil.toStringMode(mode));

    // TODO unter windows mit setReadable usw.
    // TODO geht nur fuer file
    @get:Override
    @set:Throws(IOException::class)
    @set:Override
    var mode:
    // print.ln(getPath());
            Int
        get() {
            if (!exists()) return 0
            if (SystemUtil.isUnix()) {
                try {
                    // TODO geht nur fuer file
                    var line: String = Command.execute("ls -ld " + getPath(), false).getOutput()
                    line = line.trim()
                    line = line.substring(0, line.indexOf(' '))
                    // print.ln(getPath());
                    return ModeUtil.toOctalMode(line)
                } catch (e: Exception) {
                }
            }
            var mode = if (SystemUtil.isWindows() && exists()) 73 else 0
            if (super.canRead()) mode += 292
            if (super.canWrite()) mode += 146
            return mode
        }
        set(mode) {
            // TODO unter windows mit setReadable usw.
            if (!SystemUtil.isUnix()) return
            provider.lock(this)
            try {
                // print.ln(ModeUtil.toStringMode(mode));
                if (Runtime.getRuntime().exec(arrayOf("chmod", ModeUtil.toStringMode(mode), getPath())).waitFor() !== 0) throw IOException("chmod  [" + ModeUtil.toStringMode(mode).toString() + "] [" + toString() + "] failed")
            } catch (e: InterruptedException) {
                throw IOException("Interrupted waiting for chmod [" + toString() + "]")
            } finally {
                provider.unlock(this)
            }
        }

    @Override
    fun setReadable(value: Boolean): Boolean {
        return if (!SystemUtil.isUnix()) false else try {
            mode = ModeUtil.setReadable(mode, value)
            true
        } catch (e: IOException) {
            false
        }
    }

    @Override
    fun setWritable(value: Boolean): Boolean {
        // setReadonly
        if (!value) {
            try {
                provider.lock(this)
                if (!super.setReadOnly()) throw IOException("Can't set resource read-only")
            } catch (ioe: IOException) {
                return false
            } finally {
                provider.unlock(this)
            }
            return true
        }
        if (SystemUtil.isUnix()) {
            // need no lock because get/setmode has one
            mode = try {
                ModeUtil.setWritable(mode, value)
            } catch (e: IOException) {
                return false
            }
            return true
        }
        try {
            provider.lock(this)
            Runtime.getRuntime().exec("attrib -R " + getAbsolutePath())
        } catch (ioe: IOException) {
            return false
        } finally {
            provider.unlock(this)
        }
        return true
    }

    @Override
    fun createNewFile(): Boolean {
        return try {
            provider.lock(this)
            super.createNewFile()
        } catch (e: IOException) {
            false
        } finally {
            provider.unlock(this)
        }
    }

    @Override
    fun canRead(): Boolean {
        try {
            provider.read(this)
        } catch (e: IOException) {
            return false
        }
        return super.canRead()
    }

    @Override
    fun canWrite(): Boolean {
        try {
            provider.read(this)
        } catch (e: IOException) {
            return false
        }
        return super.canWrite()
    }

    @Override
    fun delete(): Boolean {
        return try {
            provider.lock(this)
            super.delete()
        } catch (e: IOException) {
            false
        } finally {
            provider.unlock(this)
        }
    }

    @Override
    fun exists(): Boolean {
        try {
            provider.read(this)
        } catch (e: IOException) {
        }
        return super.exists()
    }

    @get:Override
    val isAbsolute: Boolean
        get() {
            try {
                provider.read(this)
            } catch (e: IOException) {
                return false
            }
            return super.isAbsolute()
        }

    @get:Override
    val isDirectory: Boolean
        get() {
            try {
                provider.read(this)
            } catch (e: IOException) {
                return false
            }
            return super.isDirectory()
        }

    @get:Override
    val isFile: Boolean
        get() {
            try {
                provider.read(this)
            } catch (e: IOException) {
                return false
            }
            return super.isFile()
        }

    @get:Override
    @set:Throws(IOException::class)
    @set:Override
    var isHidden: Boolean
        get() {
            try {
                provider.read(this)
            } catch (e: IOException) {
                return false
            }
            return super.isHidden()
        }
        set(value) {
            setAttribute(ATTRIBUTE_HIDDEN, value)
        }

    @Override
    fun lastModified(): Long {
        try {
            provider.read(this)
        } catch (e: IOException) {
            return 0
        }
        return super.lastModified()
    }

    @Override
    fun length(): Long {
        try {
            provider.read(this)
        } catch (e: IOException) {
            return 0
        }
        return super.length()
    }

    @Override
    fun list(): Array<String>? {
        try {
            provider.read(this)
        } catch (e: IOException) {
            return null
        }
        return super.list()
    }

    @Override
    fun mkdir(): Boolean {
        return try {
            provider.lock(this)
            super.mkdir()
        } catch (e: IOException) {
            false
        } finally {
            provider.unlock(this)
        }
    }

    @Override
    fun mkdirs(): Boolean {
        return try {
            provider.lock(this)
            _mkdirs()
        } catch (e: IOException) {
            false
        } finally {
            provider.unlock(this)
        }
    }

    private fun _mkdirs(): Boolean {
        if (super.exists()) return false
        if (super.mkdir()) return true
        val parent: File = super.getParentFile()
        return parent != null && parent.mkdirs() && super.mkdir()
    }

    @Override
    fun setLastModified(time: Long): Boolean {
        return try {
            provider.lock(this)
            super.setLastModified(time)
        } catch (t: Throwable) { // IllegalArgumentException or IOException
            ExceptionUtil.rethrowIfNecessary(t)
            false
        } finally {
            provider.unlock(this)
        }
    }

    @Override
    fun setReadOnly(): Boolean {
        return try {
            provider.lock(this)
            super.setReadOnly()
        } catch (e: IOException) {
            false
        } finally {
            provider.unlock(this)
        }
    }

    @Override
    fun getAttribute(attribute: Short): Boolean {
        return if (!SystemUtil.isWindows()) false else try {
            provider.lock(this)
            val attr: DosFileAttributes = Files.readAttributes(this.toPath(), DosFileAttributes::class.java)
            if (attribute == ATTRIBUTE_ARCHIVE) {
                attr.isArchive()
            } else if (attribute == ATTRIBUTE_HIDDEN) {
                attr.isHidden()
            } else if (attribute == ATTRIBUTE_SYSTEM) {
                attr.isSystem()
            } else {
                false
            }
        } catch (e: Exception) {
            false
        } finally {
            provider.unlock(this)
        }
    }

    @Override
    @Throws(IOException::class)
    fun setAttribute(attribute: Short, value: Boolean) {
        if (!SystemUtil.isWindows()) return
        provider.lock(this)
        try {
            if (attribute == ATTRIBUTE_ARCHIVE) {
                Files.setAttribute(this.toPath(), "dos:archive", value)
            } else if (attribute == ATTRIBUTE_HIDDEN) {
                Files.setAttribute(this.toPath(), "dos:hidden", value)
            } else if (attribute == ATTRIBUTE_SYSTEM) {
                Files.setAttribute(this.toPath(), "dos:system", value)
            }
        } catch (e: IOException) {
            return
        } finally {
            provider.unlock(this)
        }
    }

    @Override
    override fun equals(other: Object): Boolean {
        if (provider.isCaseSensitive()) return super.equals(other)
        return if (other !is File) false else getAbsolutePath().equalsIgnoreCase((other as File).getAbsolutePath())
    }

    companion object {
        private const val serialVersionUID = -6856656594615376447L
        private val COPY_OPTIONS: Array<CopyOption> = arrayOf<CopyOption>(StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.COPY_ATTRIBUTES)
    }
}