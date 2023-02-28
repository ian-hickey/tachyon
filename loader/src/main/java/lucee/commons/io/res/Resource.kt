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
package lucee.commons.io.res

import java.io.IOException

/**
 * a Resource handle connection to different resources in an abstract form
 */
interface Resource : Serializable {
    /**
     * Tests whether the application can read the resource denoted by this abstract pathname.
     *
     * @return `true` if and only if the resource specified by this abstract pathname exists
     * *and* can be read by the application; `false` otherwise
     */
    val isReadable: Boolean

    /**
     * Tests whether the application can read the resource denoted by this abstract pathname.
     *
     * @return `true` if and only if the resource specified by this abstract pathname exists
     * *and* can be read by the application; `false` otherwise
     */
    @Deprecated
    @Deprecated("use instead <code>#isReadable()</code>")
    fun canRead(): Boolean

    /**
     * Tests whether the application can modify the resource denoted by this abstract pathname.
     *
     * @return `true` if and only if the resource system actually contains a resource denoted
     * by this abstract pathname *and* the application is allowed to write to the
     * resource; `false` otherwise.
     */
    val isWriteable: Boolean

    /**
     * Tests whether the application can modify the resource denoted by this abstract pathname.
     *
     * @return `true` if and only if the resource system actually contains a resource denoted
     * by this abstract pathname *and* the application is allowed to write to the
     * resource; `false` otherwise.
     */
    @Deprecated
    @Deprecated("use instead <code>#isWriteable()</code>")
    fun canWrite(): Boolean

    /**
     * Deletes the resource denoted by this abstract pathname. If this pathname denotes a directory,
     * then the directory must be empty, when argument "force" is set to false, when argument "force" is
     * set to true, also the children of the directory will be deleted.
     *
     * @param force force the removal
     *
     * @throws IOException if the file doesn't exists or can't delete
     */
    @Throws(IOException::class)
    fun remove(force: Boolean)

    /**
     * Deletes the resource denoted by this abstract pathname. If this pathname denotes a directory,
     * then the directory must be empty, when argument "force" is set to false, when argument "force" is
     * set to true, also the children oif the directory will be deleted.
     *
     * if the file doesn't exists or can't delete
     *
     * @return was delete sucessfull or not
     */
    @Deprecated
    @Deprecated("""replaced with method remove(boolean)
	  """)
    fun delete(): Boolean

    /**
     * Tests whether the resource denoted by this abstract pathname exists.
     *
     * @return true if and only if the resource denoted by this abstract pathname exists; false
     * otherwise
     */
    fun exists(): Boolean

    /**
     * Returns the absolute form of this abstract pathname.
     *
     * @return The absolute abstract pathname denoting the same resource as this abstract pathname
     */
    val absoluteResource: Resource?

    /**
     * Returns the absolute pathname string of this abstract pathname.
     *
     *
     *
     * If this abstract pathname is already absolute, then the pathname string is simply returned as if
     * by the `[.getPath]` method.
     *
     * @return The absolute pathname string denoting the same resource as this abstract pathname
     */
    val absolutePath: String?

    /**
     * Returns the canonical form of this abstract pathname.
     *
     * @return The canonical pathname string denoting the same resource as this abstract pathname
     *
     * @throws IOException If an I/O error occurs, which is possible because the construction of the
     * canonical pathname may require filesystem queries
     */
    @get:Throws(IOException::class)
    val canonicalResource: Resource?

    /**
     * Returns the canonical pathname string of this abstract pathname.
     *
     *
     *
     * A canonical pathname is both absolute and unique. The precise definition of canonical form is
     * system-dependent. This method first converts this pathname to absolute form if necessary, as if
     * by invoking the [.getAbsolutePath] method, and then maps it to its unique form in a
     * system-dependent way.
     *
     *
     *
     * Every pathname that denotes an existing file or directory has a unique canonical form. Every
     * pathname that denotes a nonexistent resource also has a unique canonical form. The canonical form
     * of the pathname of a nonexistent file or directory may be different from the canonical form of
     * the same pathname after the resource is created. Similarly, the canonical form of the pathname of
     * an existing resource may be different from the canonical form of the same pathname after the
     * resource is deleted.
     *
     * @return The canonical pathname string denoting the same file or directory as this abstract
     * pathname
     *
     * @throws IOException If an I/O error occurs, which is possible because the construction of the
     * canonical pathname may require filesystem queries
     */
    @get:Throws(IOException::class)
    val canonicalPath: String?

    /**
     * Returns the name of the resource denoted by this abstract pathname. This is just the last name in
     * the pathname's name sequence. If the pathname's name sequence is empty, then the empty string is
     * returned.
     *
     * @return The name of the resource denoted by this abstract pathname, or the empty string if this
     * pathname's name sequence is empty
     */
    val name: String?

    /**
     * Returns the pathname string of this abstract pathname's parent, or `null` if this
     * pathname does not name a parent directory.
     *
     *
     *
     * The *parent* of an abstract pathname consists of the pathname's prefix, if any, and each
     * name in the pathname's name sequence except for the last. If the name sequence is empty then the
     * pathname does not name a parent directory.
     *
     * @return The pathname string of the parent directory named by this abstract pathname, or
     * `null` if this pathname does not name a parent
     */
    val parent: String?

    /**
     * Returns the abstract pathname of this abstract pathname's parent, or `null` if this
     * pathname does not name a parent directory.
     *
     *
     *
     * The *parent* of an abstract pathname consists of the pathname's prefix, if any, and each
     * name in the pathname's name sequence except for the last. If the name sequence is empty then the
     * pathname does not name a parent directory.
     *
     * @return The abstract pathname of the parent directory named by this abstract pathname, or
     * `null` if this pathname does not name a parent
     */
    val parentResource: Resource?

    /**
     * returns a resource path that is relative to the current resource
     *
     * @param realpath relative path to get resource from
     * @return relative resource path to the current
     */
    fun getReal(realpath: String?): String?

    /**
     * returns a resource that is relative to the current resource
     *
     * @param relpath relative path to get resource from
     * @return relative resource to the current
     */
    fun getRealResource(relpath: String?): Resource?

    /**
     * Converts this abstract pathname into a pathname string.
     *
     * @return The string form of this abstract pathname
     */
    val path: String?

    /**
     * Tests whether this abstract pathname is absolute.
     *
     * @return `true` if this abstract pathname is absolute, `false` otherwise
     */
    val isAbsolute: Boolean

    /**
     * Tests whether the resource denoted by this abstract pathname is a directory.
     *
     * @return `true` if and only if the file denoted by this abstract pathname exists
     * *and* is a directory; `false` otherwise
     */
    val isDirectory: Boolean

    /**
     * Tests whether the file denoted by this abstract pathname is a normal file. A file is
     * *normal* if it is not a directory and, in addition, satisfies other system-dependent
     * criteria. Any non-directory file created by a Java application is guaranteed to be a normal file.
     *
     * @return `true` if and only if the file denoted by this abstract pathname exists
     * *and* is a normal file; `false` otherwise
     */
    val isFile: Boolean
    /**
     * Tests whether the resource named by this abstract pathname is a hidden resource.
     *
     * @return `true` if and only if the file denoted by this abstract pathname is hidden
     */
    /**
     * sets hidden attribute of the resource
     *
     * @param value value to set
     * @throws IOException thrown when no access to change the value or the resource doesn't exist
     */
    @get:Deprecated("use instead <code>{@link #getAttribute(short)}</code>")
    @get:Deprecated
    @set:Throws(IOException::class)
    @set:Deprecated("use instead <code>{@link #setAttribute(short, boolean)}</code>")
    @set:Deprecated
    var isHidden: Boolean
    /**
     * Tests whether the resource named by this abstract pathname is an archive resource.
     *
     * @return `true` if and only if the file denoted by this abstract pathname is an archive
     */
    /**
     * sets archive attribute of the resource
     *
     * @param value value to set
     * @throws IOException thrown when no access to change the value or the resource doesn't exist
     */
    @get:Deprecated("use instead <code>{@link #getAttribute(short)}</code>")
    @get:Deprecated
    @set:Throws(IOException::class)
    @set:Deprecated("use instead <code>{@link #setAttribute(short, boolean)}</code>")
    @set:Deprecated
    var isArchive: Boolean
    /**
     * Tests whether the resource named by this abstract pathname is a system resource.
     *
     * @return `true` if and only if the file denoted by this abstract pathname is a system
     * resource
     */
    /**
     * sets system attribute of the resource
     *
     * @param value value to set
     * @throws IOException thrown when no access to change the value or the resource doesn't exist
     */
    @get:Deprecated("use instead <code>{@link #getAttribute(short)}</code>")
    @get:Deprecated
    @set:Throws(IOException::class)
    @set:Deprecated("use instead <code>{@link #setAttribute(short, boolean)}</code>")
    @set:Deprecated
    var isSystem: Boolean

    /**
     * Returns the time that the resource denoted by this abstract pathname was last modified.
     *
     * @return A `long` value representing the time the file was last modified, measured in
     * milliseconds since the epoch (00:00:00 GMT, January 1, 1970), or `0L` if the
     * file does not exist or if an I/O error occurs
     */
    fun lastModified(): Long

    /**
     * Returns the length of the resource denoted by this abstract pathname. The return value is
     * unspecified if this pathname denotes a directory.
     *
     * @return The length, in bytes, of the resource denoted by this abstract pathname, or
     * `0L` if the resource does not exist
     */
    fun length(): Long

    /**
     * Returns an array of strings naming the files and directories in the directory denoted by this
     * abstract pathname.
     *
     *
     *
     * If this abstract pathname does not denote a directory, then this method returns
     * `null`. Otherwise an array of strings is returned, one for each file or directory in
     * the directory. Names denoting the directory itself and the directory's parent directory are not
     * included in the result. Each string is a file name rather than a complete path.
     *
     *
     *
     * There is no guarantee that the name strings in the resulting array will appear in any specific
     * order; they are not, in particular, guaranteed to appear in alphabetical order.
     *
     * @return An array of strings naming the files and directories in the directory denoted by this
     * abstract pathname. The array will be empty if the directory is empty. Returns
     * `null` if this abstract pathname does not denote a directory, or if an I/O
     * error occurs.
     */
    fun list(): Array<String?>?

    /**
     * Returns an array of strings naming the files and directories in the directory denoted by this
     * abstract pathname that satisfy the specified filter. The behavior of this method is the same as
     * that of the `[.list]` method, except that the strings in the returned array
     * must satisfy the filter. If the given `filter` is `null` then all names are
     * accepted. Otherwise, a name satisfies the filter if and only if the value `true`
     * results when the `[ ][ResourceNameFilter.accept]` method of the filter is invoked on this abstract pathname and
     * the name of a file or directory in the directory that it denotes.
     *
     * @param filter A resourcename filter
     *
     * @return An array of strings naming the files and directories in the directory denoted by this
     * abstract pathname that were accepted by the given `filter`. The array will be
     * empty if the directory is empty or if no names were accepted by the filter. Returns
     * `null` if this abstract pathname does not denote a directory, or if an I/O
     * error occurs.
     */
    fun list(filter: ResourceNameFilter?): Array<String?>?
    fun list(filter: ResourceFilter?): Array<String?>?

    /**
     * Returns an array of abstract pathnames denoting the files in the directory denoted by this
     * abstract pathname.
     *
     *
     *
     * If this abstract pathname does not denote a directory, then this method returns
     * `null`. Otherwise an array of `File` objects is returned, one for each file
     * or directory in the directory. Therefore if this pathname is absolute then each resulting
     * pathname is absolute; if this pathname is relative then each resulting pathname will be relative
     * to the same directory.
     *
     *
     *
     * There is no guarantee that the name strings in the resulting array will appear in any specific
     * order; they are not, in particular, guaranteed to appear in alphabetical order.
     *
     * @return An array of abstract pathnames denoting the files and directories in the directory
     * denoted by this abstract pathname. The array will be empty if the directory is empty.
     * Returns `null` if this abstract pathname does not denote a directory, or if an
     * I/O error occurs.
     */
    fun listResources(): Array<Resource?>?

    /**
     * Returns an array of abstract pathnames denoting the files and directories in the directory
     * denoted by this abstract pathname that satisfy the specified filter. The behavior of this method
     * is the same as that of the `[.listResources]` method, except that the
     * pathnames in the returned array must satisfy the filter. If the given `filter` is
     * `null` then all pathnames are accepted. Otherwise, a pathname satisfies the filter if
     * and only if the value `true` results when the
     * `[ResourceFilter.accept]` method of the filter is invoked on the
     * pathname.
     *
     * @param filter A resource filter
     *
     * @return An array of abstract pathnames denoting the files and directories in the directory
     * denoted by this abstract pathname. The array will be empty if the directory is empty.
     * Returns `null` if this abstract pathname does not denote a directory, or if an
     * I/O error occurs.
     */
    fun listResources(filter: ResourceFilter?): Array<Resource?>?

    /**
     * Returns an array of abstract pathnames denoting the files and directories in the directory
     * denoted by this abstract pathname that satisfy the specified filter. The behavior of this method
     * is the same as that of the `[.listResources]` method, except that the
     * pathnames in the returned array must satisfy the filter. If the given `filter` is
     * `null` then all pathnames are accepted. Otherwise, a pathname satisfies the filter if
     * and only if the value `true` results when the
     * `[ResourceNameFilter.accept]` method of the filter is invoked on this abstract
     * pathname and the name of a file or directory in the directory that it denotes.
     *
     * @param filter A resourcename filter
     *
     * @return An array of abstract pathnames denoting the files and directories in the directory
     * denoted by this abstract pathname. The array will be empty if the directory is empty.
     * Returns `null` if this abstract pathname does not denote a directory, or if an
     * I/O error occurs.
     */
    fun listResources(filter: ResourceNameFilter?): Array<Resource?>?

    /**
     * Move/renames the file denoted by this abstract pathname.
     *
     *
     *
     * Many aspects of the behavior of this method are inherently platform-dependent: The rename
     * operation might not be able to move a file from one filesystem to another, it might not be
     * atomic, and it might not succeed if a file with the destination abstract pathname already exists.
     *
     * @param dest The new abstract pathname for the named file
     * @return has successfull renamed or not
     *
     */
    @Deprecated
    @Deprecated("use instead <code>#moveTo(Resource)</code>")
    fun renameTo(dest: Resource?): Boolean

    /**
     * Move/renames the file denoted by this abstract pathname.
     *
     *
     *
     * Many aspects of the behavior of this method are inherently platform-dependent: The rename
     * operation might not be able to move a file from one filesystem to another, it might not be
     * atomic, and it might not succeed if a file with the destination abstract pathname already exists.
     *
     * @param dest The new abstract pathname for the named file
     * @throws IOException thrown when operation not done successfully
     */
    @Throws(IOException::class)
    fun moveTo(dest: Resource?)

    /**
     * Sets the last-modified time of the file or directory named by this abstract pathname.
     *
     *
     *
     * All platforms support file-modification times to the nearest second, but some provide more
     * precision. The argument will be truncated to fit the supported precision. If the operation
     * succeeds and no intervening operations on the file take place, then the next invocation of the
     * `[.lastModified]` method will return the (possibly truncated) `time`
     * argument that was passed to this method.
     *
     * @param time The new last-modified time, measured in milliseconds since the epoch (00:00:00 GMT,
     * January 1, 1970)
     *
     * @return `true` if and only if the operation succeeded; `false` otherwise
     */
    fun setLastModified(time: Long): Boolean

    /**
     * Marks the file or directory named by this abstract pathname so that only read operations are
     * allowed. After invoking this method the file or directory is guaranteed not to change until it is
     * either deleted or marked to allow write access. Whether or not a read-only file or directory may
     * be deleted depends upon the underlying system.
     *
     * @return `true` if and only if the operation succeeded; `false` otherwise
     */
    @Deprecated
    @Deprecated("use instead <code>{@link #setWritable(boolean)}</code>")
    fun setReadOnly(): Boolean

    // public void setWritable(boolean value) throws IOException;
    fun setWritable(writable: Boolean): Boolean

    // public void setReadable(boolean value) throws IOException;
    fun setReadable(readable: Boolean): Boolean

    /**
     * Creates a new, empty file named by this abstract pathname if and only if a file with this name
     * does not yet exist. The check for the existence of the file and the creation of the file if it
     * does not exist are a single operation that is atomic with respect to all other filesystem
     * activities that might affect the file.
     *
     * @return `true` if the named file does not exist and was successfully created;
     * `false` if the named file already exists
     *
     */
    @Deprecated
    @Deprecated("use instead <code>#createFile(boolean)</code>")
    fun createNewFile(): Boolean

    /**
     * Creates a new, empty file named by this abstract pathname if and only if a file with this name
     * does not yet exist. The check for the existence of the file and the creation of the file if it
     * does not exist are a single operation that is atomic with respect to all other filesystem
     * activities that might affect the file.
     *
     * @param createParentWhenNotExists create parent when not exist
     *
     *
     * @throws IOException If an I/O error occurred
     */
    @Throws(IOException::class)
    fun createFile(createParentWhenNotExists: Boolean)

    /**
     * Creates the directory named by this abstract pathname.
     *
     * @return `true` if and only if the directory was created; `false` otherwise
     */
    @Deprecated
    @Deprecated("use <code>#createDirectory(boolean)</code>")
    fun mkdir(): Boolean

    /**
     * Creates the directory named by this abstract pathname, including any necessary but nonexistent
     * parent directories. Note that if this operation fails it may have succeeded in creating some of
     * the necessary parent directories.
     *
     * @return `true` if and only if the directory was created, along with all necessary
     * parent directories; `false` otherwise
     */
    @Deprecated
    @Deprecated("use <code>#createDirectory(boolean)</code>")
    fun mkdirs(): Boolean

    /**
     * Creates the directory named by this abstract pathname, including any necessary but nonexistent
     * parent directories if flag "createParentWhenNotExists" is set to true. Note that if this
     * operation fails it may have succeeded in creating some of the necessary parent directories.
     *
     * @param createParentWhenNotExists throws Exception when can't create directory
     * @throws IOException in case copy fails
     */
    @Throws(IOException::class)
    fun createDirectory(createParentWhenNotExists: Boolean)

    @get:Throws(IOException::class)
    val inputStream: InputStream?

    @get:Throws(IOException::class)
    val outputStream: OutputStream?

    /**
     * copy current resource data to given resource
     *
     * @param res resource to copy to
     * @param append do append value to existing data or overwrite
     * @throws IOException in case copy fails
     */
    @Throws(IOException::class)
    fun copyTo(res: Resource?, append: Boolean)

    /**
     * copy data of given resource to current
     *
     * @param res resource to copy from
     * @param append do append value to existing data or overwrite
     * @throws IOException in case copy fails
     */
    @Throws(IOException::class)
    fun copyFrom(res: Resource?, append: Boolean)

    @Throws(IOException::class)
    fun getOutputStream(append: Boolean): OutputStream?
    val resourceProvider: lucee.commons.io.res.ResourceProvider?

    @set:Throws(IOException::class)
    var mode: Int

    /**
     * sets an attribute on the resource if supported otherwise it will ign
     *
     * @param attribute which attribute (Resource.ATTRIBUTE_*)
     * @param value value to set
     * @throws IOException thrown when no access to change the value, when attributes are not supported
     * or the resource doesn't exist
     */
    @Throws(IOException::class)
    fun setAttribute(attribute: Short, value: Boolean)

    /**
     * return value of a specific attribute
     *
     * @param attribute attribute to get the value for
     * @return value of the attribute
     */
    fun getAttribute(attribute: Short): Boolean

    companion object {
        const val ATTRIBUTE_HIDDEN: Short = 1
        const val ATTRIBUTE_SYSTEM: Short = 2
        const val ATTRIBUTE_ARCHIVE: Short = 4
    }
}