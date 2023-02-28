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
package lucee.commons.io

import java.io.File

/**
 * Helper methods for file objects
 */
object FileUtil {
    /**
     * Field `FILE_SEPERATOR`
     */
    val FILE_SEPERATOR: Char = File.separatorChar
    val FILE_SEPERATOR_STRING: String = String.valueOf(FILE_SEPERATOR)

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

    /**
     * create a file from path
     *
     * @param path
     * @return new File Object
     */
    fun toFile(path: String): File {
        return File(path.replace(FILE_ANTI_SEPERATOR, FILE_SEPERATOR))
    }

    /**
     * create a File from parent file and string
     *
     * @param parent
     * @param path
     * @return new File Object
     */
    fun toFile(parent: File?, path: String): File {
        return File(parent, path.replace(FILE_ANTI_SEPERATOR, FILE_SEPERATOR))
    }

    /**
     * create a File from parent file and string
     *
     * @param parent
     * @param path
     * @return new File Object
     */
    fun toFile(parent: String, path: String): File {
        return File(parent.replace(FILE_ANTI_SEPERATOR, FILE_SEPERATOR), path.replace(FILE_ANTI_SEPERATOR, FILE_SEPERATOR))
    }
    /*
	 * * create a file object from a file object (parent) and realpath, in difference to the same
	 * constructor of the File Object this method ignore the diffrent path seperators on the different
	 * plattforms
	 * 
	 * @param parent
	 * 
	 * @param realpath
	 * 
	 * @return new FIle Object matching on arguments / public static File toFile2(File parent, String
	 * realpath) { realpath=realpath.replace(FILE_ANTI_SEPERATOR,FILE_SEPERATOR);
	 * while(realpath.startsWith("../")) { parent=parent.getParentFile();
	 * realpath=realpath.substring(3); } if(realpath.startsWith("./")) realpath=realpath.substring(2);
	 * 
	 * return FileUtil.toFile(parent,realpath); }
	 */
    /**
     * translate a URL to a File Object
     *
     * @param url
     * @return matching file object
     * @throws MalformedURLException
     */
    @Throws(MalformedURLException::class)
    fun URLToFile(url: URL): File {
        if (!"file".equals(url.getProtocol())) throw MalformedURLException("URL protocol must be 'file'.")
        return File(URIToFilename(url.getFile()))
    }

    /**
     * Fixes a platform dependent filename to standard URI form.
     *
     * @param str The string to fix.
     * @return Returns the fixed URI string.
     */
    fun URIToFilename(str: String): String {
        // Windows fix
        var str = str
        if (str.length() >= 3) {
            if (str.charAt(0) === '/' && str.charAt(2) === ':') {
                val ch1: Char = Character.toUpperCase(str.charAt(1))
                if (ch1 >= 'A' && ch1 <= 'Z') str = str.substring(1)
            }
        }
        // handle platform dependent strings
        str = str.replace('/', java.io.File.separatorChar)
        return str
    }

    /**
     * check if there is a windows lock on the given file, on non windows this simply returns false
     *
     * @param res
     * @return
     */
    fun isLocked(res: Resource): Boolean {
        if (res !is File || !SystemUtil.isWindows() || !res.exists()) return false
        try {
            val `is`: InputStream = res.getInputStream()
            `is`.close()
        } catch (e: FileNotFoundException) {
            return true
        } catch (e: Exception) {
        }
        return false
    }

    /**
     * create a temp file from given file if the file is locked, if the file is not locked it returns
     * the original file unless the argument force is set
     */
    @Throws(IOException::class)
    fun createTempResourceFromLockedResource(res: Resource, force: Boolean): Resource {
        if (!res.isFile()) throw IOException("file [$res] is not a file")
        return if (!isLocked(res) && !force) res else _createTempResourceFromLockedResource(res, force)
    }

    /**
     * create a temp file from given file if the file is locked, if the file is not locked it returns
     * the original file unless the argument force is set
     */
    fun createTempResourceFromLockedResource(res: Resource, force: Boolean, defaultValue: Resource): Resource {
        if (!res.isFile()) return defaultValue
        return if (!isLocked(res) && !force) res else try {
            _createTempResourceFromLockedResource(res, force)
        } catch (e: Exception) {
            defaultValue
        }
    }

    @Throws(IOException::class)
    private fun _createTempResourceFromLockedResource(res: Resource, force: Boolean): Resource {
        val `is`: InputStream
        `is` = if (res is File) {
            Files.newInputStream((res as File).toPath(), StandardOpenOption.READ)
        } else {
            res.getInputStream()
        }
        val temp: Resource = SystemUtil.getTempFile("." + ResourceUtil.getExtension(res, "obj"), true)
        IOUtil.copy(`is`, temp, true)
        return temp
    }
}