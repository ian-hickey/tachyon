/**
 * Copyright (c) 2014, the Railo Company Ltd.
 * Copyright (c) 2015, Tachyon Assosication Switzerland
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
package tachyon.runtime.util

import java.io.File

interface ResourceUtil {
    /**
     * cast a String (argument destination) to a File Object, if destination is not an absolute, file
     * object will be relative to current position (get from PageContext) file must exist otherwise
     * throw exception
     *
     * @param pc Page Context to the current position in filesystem
     * @param path relative or absolute path for file object
     * @return file object from destination
     * @throws PageException Page Exception
     */
    @Throws(PageException::class)
    fun toResourceExisting(pc: PageContext?, path: String?): Resource?

    /**
     * cast a String (argument destination) to a File Object, if destination is not an absolute, file
     * object will be relative to current position (get from PageContext) at least parent must exist
     *
     * @param pc Page Context to the current position in filesystem
     * @param destination relative or absolute path for file object
     * @return file object from destination
     * @throws PageException Page Exception
     */
    @Throws(PageException::class)
    fun toResourceExistingParent(pc: PageContext?, destination: String?): Resource?

    /**
     * cast a String (argument destination) to a File Object, if destination is not an absolute, file
     * object will be relative to current position (get from PageContext) existing file is preferred but
     * dont must exist
     *
     * @param pc Page Context to the current position in filesystem
     * @param destination relative or absolute path for file object
     * @return file object from destination
     */
    fun toResourceNotExisting(pc: PageContext?, destination: String?): Resource?

    /**
     * create a file if possible, return file if ok, otherwise return null
     *
     * @param res file to touch
     * @param level touch also parent and grand parent
     * @param type is file or directory
     * @return file if exists, otherwise null
     */
    fun createResource(res: Resource?, level: Short, type: Short): Resource?

    /**
     * sets an attribute to the resource
     *
     * @param res Resource
     * @param attributes Attributes
     * @throws IOException IO Exception
     */
    @Throws(IOException::class)
    fun setAttribute(res: Resource?, attributes: String?)

    /**
     * return the mime type of a file, does not check the extension of the file, it checks the header
     *
     * @param res Resource
     * @param defaultValue default value
     * @return mime type of the file
     */
    @Deprecated
    @Deprecated("use instead <code>getContentType</code>")
    fun getMimeType(res: Resource?, defaultValue: String?): String?

    /**
     * return the mime type of a byte array
     *
     * @param barr Byte Array
     * @param defaultValue default value
     * @return mime type of the file
     */
    @Deprecated
    @Deprecated("use instead <code>getContentType</code>")
    fun getMimeType(barr: ByteArray?, defaultValue: String?): String?

    /**
     * check if file is a child of given directory
     *
     * @param file file to search
     * @param dir directory to search
     * @return is inside or not
     */
    fun isChildOf(file: Resource?, dir: Resource?): Boolean

    /**
     * return differnce of one file to another if first is child of second otherwise return null
     *
     * @param file file to search
     * @param dir directory to search
     * @return path to child
     */
    fun getPathToChild(file: Resource?, dir: Resource?): String?

    /**
     * get the Extension of a file resource
     *
     * @param res Resource
     * @return extension of file
     */
    @Deprecated
    @Deprecated("use instead <code>getExtension(Resource res, String defaultValue);</code>")
    fun getExtension(res: Resource?): String?

    /**
     * get the Extension of a file resource
     *
     * @param res Resource
     * @param defaultValue default value
     * @return extension of file
     */
    fun getExtension(res: Resource?, defaultValue: String?): String?

    /**
     * get the Extension of a file
     *
     * @param strFile path to file
     * @return extension of file
     */
    @Deprecated
    @Deprecated("use instead <code>getExtension(String strFile, String defaultValue);</code>")
    fun getExtension(strFile: String?): String?

    /**
     * get the Extension of a file resource
     *
     * @param strFile Path to resource
     * @param defaultValue default value
     * @return extension of file
     */
    fun getExtension(strFile: String?, defaultValue: String?): String?

    /**
     * copy a file or directory recursive (with his content)
     *
     * @param src Source Resource
     * @param trg Target Resource
     * @throws IOException IO Exception
     */
    @Throws(IOException::class)
    fun copyRecursive(src: Resource?, trg: Resource?)

    /**
     * copy a file or directory recursive (with his content)
     *
     * @param src Source Resource
     * @param trg Target Resource
     * @param filter filter Filter
     * @throws IOException IO Exception
     */
    @Throws(IOException::class)
    fun copyRecursive(src: Resource?, trg: Resource?, filter: ResourceFilter?)

    @Throws(IOException::class)
    fun removeChildren(res: Resource?)

    @Throws(IOException::class)
    fun removeChildren(res: Resource?, filter: ResourceNameFilter?)

    @Throws(IOException::class)
    fun removeChildren(res: Resource?, filter: ResourceFilter?)

    @Throws(IOException::class)
    fun moveTo(src: Resource?, dest: Resource?)

    /**
     * return if Resource is empty, means is directory and has no children or an empty file, if not exist
     * return false.
     *
     * @param res Resource
     * @return if the resource is empty
     */
    fun isEmpty(res: Resource?): Boolean
    fun isEmptyDirectory(res: Resource?): Boolean
    fun isEmptyFile(res: Resource?): Boolean
    fun translatePath(path: String?, slashAdBegin: Boolean, slashAddEnd: Boolean): String?
    fun translatePathName(path: String?): Array<String?>?
    fun merge(parent: String?, child: String?): String?
    fun removeScheme(scheme: String?, path: String?): String?

    /**
     * check if directory creation is ok with the rules for the Resource interface, to not change this
     * rules.
     *
     * @param resource Resource
     * @param createParentWhenNotExists create parent when not exists
     * @throws IOException IO Exception
     */
    @Throws(IOException::class)
    fun checkCreateDirectoryOK(resource: Resource?, createParentWhenNotExists: Boolean)

    /**
     * check if file creating is ok with the rules for the Resource interface, to not change this rules.
     *
     * @param resource Resource
     * @param createParentWhenNotExists create parent when not exists
     * @throws IOException IO Exception
     */
    @Throws(IOException::class)
    fun checkCreateFileOK(resource: Resource?, createParentWhenNotExists: Boolean)

    /**
     * check if copying a file is ok with the rules for the Resource interface, to not change this
     * rules.
     *
     * @param source Source Resource
     * @param target Target Resource
     * @throws IOException IO Exception
     */
    @Throws(IOException::class)
    fun checkCopyToOK(source: Resource?, target: Resource?)

    /**
     * check if moveing a file is ok with the rules for the Resource interface, to not change this
     * rules.
     *
     * @param source Source Resource
     * @param target Target Resource
     * @throws IOException IO Exception
     */
    @Throws(IOException::class)
    fun checkMoveToOK(source: Resource?, target: Resource?)

    /**
     * check if getting an inputstream of the file is ok with the rules for the Resource interface, to
     * not change this rules.
     *
     * @param resource Resource
     * @throws IOException IO Exception
     */
    @Throws(IOException::class)
    fun checkGetInputStreamOK(resource: Resource?)

    /**
     * check if getting an outputstream of the file is ok with the rules for the Resource interface, to
     * not change this rules.
     *
     * @param resource Resource
     * @throws IOException IO Exception
     */
    @Throws(IOException::class)
    fun checkGetOutputStreamOK(resource: Resource?)

    /**
     * check if removing the file is ok with the rules for the Resource interface, to not change this
     * rules.
     *
     * @param resource Resource
     * @throws IOException IO Exception
     */
    @Throws(IOException::class)
    fun checkRemoveOK(resource: Resource?)

    @Deprecated
    @Throws(IOException::class)
    fun toString(r: Resource?, charset: String?): String?

    @Throws(IOException::class)
    fun toString(r: Resource?, charset: Charset?): String?
    fun contractPath(pc: PageContext?, path: String?): String?
    val homeDirectory: Resource?
    val systemDirectory: Resource?
    val tempDirectory: Resource?
    fun parsePlaceHolder(path: String?): String?
    fun getExtensionResourceFilter(extension: String?, allowDir: Boolean): ResourceFilter?
    fun getExtensionResourceFilter(extensions: Array<String?>?, allowDir: Boolean): ResourceFilter?
    fun getContentType(file: Resource?): ContentType?

    /**
     * cast a String (argument destination) to a File Object, if destination is not an absolute, file
     * object will be relative to current position (get from PageContext) at least parent must exist
     *
     * @param pc Page Context to the current position in filesystem
     * @param destination relative or absolute path for file object
     * @param allowRealpath allow real path
     * @return file object from destination
     * @throws PageException Page Exception
     */
    @Throws(PageException::class)
    fun toResourceExistingParent(pc: PageContext?, destination: String?, allowRealpath: Boolean): Resource?
    fun toResourceNotExisting(pc: PageContext?, destination: String?, allowRealpath: Boolean, checkComponentMappings: Boolean): Resource?
    fun isUNCPath(path: String?): Boolean

    /**
     * translate the path of the file to an existing file path by changing case of letters Works only on
     * Linux, because
     *
     * Example Unix: we have an existing file with path "/usr/virtual/myFile.txt" now you call this
     * method with path "/Usr/Virtual/myfile.txt" the result of the method will be
     * "/usr/virtual/myFile.txt"
     *
     * if there are more file with rhe same name but different cases Example: /usr/virtual/myFile.txt
     * /usr/virtual/myfile.txt /Usr/Virtual/myFile.txt the nearest case wil returned
     *
     * @param res Resources
     * @return file
     */
    fun toExactResource(res: Resource?): Resource?
    fun prettifyPath(path: String?): String?

    /**
     * Returns the canonical form of this abstract pathname.
     *
     * @param res file to get canonical form from it
     *
     * @return The canonical pathname string denoting the same file or directory as this abstract
     * pathname
     *
     * @throws SecurityException If a required system property value cannot be accessed.
     */
    fun getCanonicalPathSilent(res: Resource?): String?

    /**
     * Returns the canonical form of this abstract pathname.
     *
     * @param res file to get canonical form from it
     *
     * @return The canonical pathname string denoting the same file or directory as this abstract
     * pathname
     *
     * @throws SecurityException If a required system property value cannot be accessed.
     */
    fun getCanonicalResourceSilent(res: Resource?): Resource?

    /**
     * creates a new File
     *
     * @param res Resource
     * @return was successfull
     */
    fun createNewResourceSilent(res: Resource?): Boolean

    /**
     * similar to linux bash function touch, create file if not exist otherwise change last modified
     * date
     *
     * @param res Resource
     * @throws IOException IO Exception
     */
    @Throws(IOException::class)
    fun touch(res: Resource?)

    @Throws(IOException::class)
    fun clear(res: Resource?)

    /**
     * change extension of file and return new file
     *
     * @param file Resource
     * @param newExtension New file extension
     * @return file with new Extension
     */
    fun changeExtension(file: Resource?, newExtension: String?): Resource?

    /**
     * delete the content of a directory
     *
     * @param src Resource
     * @param filter Filter
     */
    fun deleteContent(src: Resource?, filter: ResourceFilter?)

    @Throws(IOException::class)
    fun copy(src: Resource?, trg: Resource?)
    fun removeChildrenSilent(res: Resource?, filter: ResourceNameFilter?)
    fun removeChildrenSilent(res: Resource?, filter: ResourceFilter?)
    fun removeChildrenSilent(res: Resource?)
    fun removeSilent(res: Resource?, force: Boolean)
    fun createFileSilent(res: Resource?, force: Boolean)
    fun createDirectorySilent(res: Resource?, force: Boolean)

    /**
     * return the size of the Resource, other than method length of Resource this method return the size
     * of all files in a directory
     *
     * @param res Resource
     * @param filter Filter
     * @return the size of the directory
     */
    fun getRealSize(res: Resource?, filter: ResourceFilter?): Long
    fun getChildCount(res: Resource?, filter: ResourceFilter?): Int

    /**
     * return Boolean. True when directory is empty, Boolean. FALSE when directory is not empty and null if
     * directory does not exist
     *
     * @param res Resource
     * @param filter Filter
     * @return Returns if the Directory is empty.
     */
    fun isEmptyDirectory(res: Resource?, filter: ResourceFilter?): Boolean

    @Throws(IOException::class)
    fun deleteEmptyFolders(res: Resource?)
    fun getResource(pc: PageContext?, ps: PageSource?, defaultValue: Resource?): Resource?
    fun directrySize(dir: Resource?, filter: ResourceFilter?): Int
    fun directrySize(dir: Resource?, filter: ResourceNameFilter?): Int
    fun names(resources: Array<Resource?>?): Array<String?>?
    fun merge(srcs: Array<Resource?>?, vararg trgs: Resource?): Array<Resource?>?

    @Throws(IOException::class)
    fun removeEmptyFolders(dir: Resource?)
    fun listRecursive(res: Resource?, filter: ResourceFilter?): List<Resource?>?
    fun getSeparator(rp: ResourceProvider?): Char
    val fileResourceProvider: ResourceProvider?

    companion object {
        /**
         * Field `FILE_SEPERATOR`
         */
        val FILE_SEPERATOR: Char = File.separatorChar

        /**
         * Field `FILE_ANTI_SEPERATOR`
         */
        val FILE_ANTI_SEPERATOR = if (FILE_SEPERATOR == '/') '\\' else '/'

        /**
         * Field `TYPE_DIR`
         */
        const val TYPE_DIR: Short = 0

        /**
         * Field `TYPE_FILE`
         */
        const val TYPE_FILE: Short = 1

        /**
         * Field `LEVEL_FILE`
         */
        const val LEVEL_FILE: Short = 0

        /**
         * Field `LEVEL_PARENT_FILE`
         */
        const val LEVEL_PARENT_FILE: Short = 1

        /**
         * Field `LEVEL_GRAND_PARENT_FILE`
         */
        const val LEVEL_GRAND_PARENT_FILE: Short = 2
    }
}