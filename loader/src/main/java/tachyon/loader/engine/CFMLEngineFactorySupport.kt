/**
 * Copyright (c) 2015, Tachyon Assosication Switzerland. All rights reserved.
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
package tachyon.loader.engine

import java.io.BufferedReader

object CFMLEngineFactorySupport {
    private var tempFile: File? = null
    private var homeFile: File? = null

    /**
     * copy an inputstream to an outputstream
     *
     * @param in input stream
     * @param out output stream
     * @throws IOException in case the process fails
     */
    @Throws(IOException::class)
    fun copy(`in`: InputStream, out: OutputStream) {
        val buffer = ByteArray(0xffff)
        var len: Int
        try {
            while (`in`.read(buffer).also { len = it } != -1) out.write(buffer, 0, len)
        } finally {
            closeEL(`in`)
            closeEL(out)
        }
    }

    /**
     * close inputstream without an Exception
     *
     * @param is input stream
     */
    fun closeEL(`is`: InputStream?) {
        try {
            if (`is` != null) `is`.close()
        } catch (e: Throwable) {
        }
    }

    /**
     * close outputstream without an Exception
     *
     * @param os output stream
     */
    fun closeEL(os: OutputStream?) {
        try {
            if (os != null) os.close()
        } catch (e: Throwable) {
        }
    }

    /**
     * read String data from an InputStream and returns it as String Object
     *
     * @param is InputStream to read data from.
     * @return readed data from InputStream
     * @throws IOException io exception
     */
    @Throws(IOException::class)
    fun toString(`is`: InputStream?): String {
        val br = BufferedReader(InputStreamReader(`is`))
        val content = StringBuffer()
        var line: String? = br.readLine()
        if (line != null) {
            content.append(line)
            while (br.readLine().also { line = it } != null) content.append("""
    
    $line
    """.trimIndent())
        }
        br.close()
        return content.toString()
    }

    /**
     * cast a tachyon string version to an int version
     *
     * @param version input version
     * @param defaultValue default value
     * @return int version
     */
    fun toVersion(version: String, defaultValue: Version): Version {
        // remove extension if there is any
        var version = version
        val rIndex: Int = version.lastIndexOf(".lco")
        if (rIndex != -1) version = version.substring(0, rIndex)
        return try {
            Version.parseVersion(version)
        } catch (iae: IllegalArgumentException) {
            defaultValue
        }
    }

    fun removeQuotes(str: String?, trim: Boolean): String? {
        var str = str
        if (str == null) return str
        if (trim) str = str.trim()
        if (str.length() < 2) return str
        val first: Char = str.charAt(0)
        val last: Char = str.charAt(str.length() - 1)
        return if ((first == '"' || first == '\'') && first == last) str.substring(1, str.length() - 1) else str
    }

    /**
     * replace path placeholder with the real path, placeholders are
     * [{temp-directory},{system-directory},{home-directory}]
     *
     * @param path path
     * @return updated path
     */
    fun parsePlaceHolder(path: String?): String? {
        var path = path
        if (path == null) return path
        // Temp
        if (path.startsWith("{temp")) {
            if (path.startsWith("}", 5)) path = File(tempDirectory, path.substring(6)).toString() else if (path.startsWith("-dir}", 5)) path = File(tempDirectory, path.substring(10)).toString() else if (path.startsWith("-directory}", 5)) path = File(tempDirectory, path.substring(16)).toString()
        } else if (path.startsWith("{system")) {
            if (path.charAt(7) === ':') {
                // now we read the properties name
                val end: Int = path.indexOf('}', 8)
                if (end > 8) {
                    val name: String = path.substring(8, end)
                    val prop: String = System.getProperty(name)
                    if (prop != null) return File(File(prop), path.substring(end + 1)).getAbsolutePath()
                }
            } else if (path.startsWith("}", 7)) path = File(systemDirectory, path.substring(8)).toString() else if (path.startsWith("-dir}", 7)) path = File(systemDirectory, path.substring(12)).toString() else if (path.startsWith("-directory}", 7)) path = File(systemDirectory, path.substring(18)).toString()
        } else if (path.startsWith("{env:")) {
            // now we read the properties name
            val end: Int = path.indexOf('}', 5)
            if (end > 5) {
                val name: String = path.substring(5, end)
                val env: String = System.getenv(name)
                if (env != null) return File(File(env), path.substring(end + 1)).getAbsolutePath()
            }
        } else if (path.startsWith("{home")) {
            if (path.startsWith("}", 5)) path = File(homeDirectory, path.substring(6)).toString() else if (path.startsWith("-dir}", 5)) path = File(homeDirectory, path.substring(10)).toString() else if (path.startsWith("-directory}", 5)) path = File(homeDirectory, path.substring(16)).toString()
        }
        // ClassLoaderDir
        if (path.startsWith("{classloader")) {
            if (path.startsWith("}", 12)) path = File(classLoaderDirectory, path.substring(13)).toString() else if (path.startsWith("-dir}", 12)) path = File(classLoaderDirectory, path.substring(17)).toString() else if (path.startsWith("-directory}", 12)) path = File(classLoaderDirectory, path.substring(23)).toString()
        }
        return path
    }

    val homeDirectory: File?
        get() {
            if (homeFile != null) return homeFile
            val homeStr: String = System.getProperty("user.home")
            if (homeStr != null) {
                homeFile = File(homeStr)
                homeFile = getCanonicalFileEL(homeFile)
            }
            return homeFile
        }
    val classLoaderDirectory: File?
        get() = CFMLEngineFactory.getClassLoaderRoot(TP::class.java.getClassLoader())

    /**
     * returns the Temp Directory of the System
     *
     * @return temp directory
     */
    val tempDirectory: File?
        get() {
            if (tempFile != null) return tempFile
            val tmpStr: String = System.getProperty("java.io.tmpdir")
            if (tmpStr != null) {
                tempFile = File(tmpStr)
                if (tempFile.exists()) {
                    tempFile = getCanonicalFileEL(tempFile)
                    return tempFile
                }
            }
            try {
                val tmp: File = File.createTempFile("a", "a")
                tempFile = tmp.getParentFile()
                tempFile = getCanonicalFileEL(tempFile)
                tmp.delete()
            } catch (ioe: IOException) {
            }
            return tempFile
        }// String[] arr=List.toStringArrayEL(List.listToArray(pathes,File.pathSeparatorChar));

    /**
     * @return return System directory
     */
    val systemDirectory: File?
        get() {
            val pathes: String = System.getProperty("java.library.path")
            if (pathes != null) {
                val arr: Array<String> = pathes.split(File.pathSeparator)
                // String[] arr=List.toStringArrayEL(List.listToArray(pathes,File.pathSeparatorChar));
                for (element in arr) if (element.toLowerCase().indexOf("windows\\system") !== -1) {
                    val file = File(element)
                    if (file.exists() && file.isDirectory() && file.canWrite()) return getCanonicalFileEL(file)
                }
                for (element in arr) if (element.toLowerCase().indexOf("windows") !== -1) {
                    val file = File(element)
                    if (file.exists() && file.isDirectory() && file.canWrite()) return getCanonicalFileEL(file)
                }
                for (element in arr) if (element.toLowerCase().indexOf("winnt") !== -1) {
                    val file = File(element)
                    if (file.exists() && file.isDirectory() && file.canWrite()) return getCanonicalFileEL(file)
                }
                for (element in arr) if (element.toLowerCase().indexOf("win") !== -1) {
                    val file = File(element)
                    if (file.exists() && file.isDirectory() && file.canWrite()) return getCanonicalFileEL(file)
                }
                for (element in arr) {
                    val file = File(element)
                    if (file.exists() && file.isDirectory() && file.canWrite()) return getCanonicalFileEL(file)
                }
            }
            return null
        }

    private fun getCanonicalFileEL(file: File?): File? {
        return try {
            file.getCanonicalFile()
        } catch (e: IOException) {
            file
        }
    }
}