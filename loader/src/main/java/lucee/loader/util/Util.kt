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
package lucee.loader.util

import java.io.BufferedInputStream

/**
 * Util class for different little jobs
 */
object Util {
    private var tempFile: File? = null

    // private static File homeFile;
    private const val QUALIFIER_APPENDIX_SNAPSHOT = 1
    private const val QUALIFIER_APPENDIX_BETA = 2
    private const val QUALIFIER_APPENDIX_RC = 3
    private const val QUALIFIER_APPENDIX_OTHER = 4
    private const val QUALIFIER_APPENDIX_STABLE = 5
    private val HTTP_TIME_STRING_FORMAT: SimpleDateFormat? = null
    @Deprecated
    @Throws(IOException::class)
    fun copy(`in`: InputStream, out: OutputStream) {
        val buffer = ByteArray(0xffff)
        var len: Int
        while (`in`.read(buffer).also { len = it } != -1) out.write(buffer, 0, len)
        closeEL(`in`)
        closeEL(out)
    }

    @Throws(IOException::class)
    fun copy(`in`: InputStream, out: OutputStream, closeIS: Boolean, closeOS: Boolean) {
        val buffer = ByteArray(0xffff)
        var len: Int
        while (`in`.read(buffer).also { len = it } != -1) out.write(buffer, 0, len)
        if (closeIS) closeEL(`in`)
        if (closeOS) closeEL(out)
    }

    /**
     * @param is InputStream to read data from.
     * @return readed data from InputStream
     * @throws IOException in case it is not possible to convert to a string
     */
    @Deprecated
    @Deprecated("""use instead CFMLEngineFactory.getInstance.getIOUtil().toString (InputStream is,
	              Charset cs) read String data from an InputStream and returns it as String Object
	  """)
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

    @Deprecated
    @Throws(IOException::class)
    fun toBooleanValue(str: String?): Boolean {
        var str = str
        str = str?.trim()?.toLowerCase()
        if ("true".equals(str)) return true
        if ("false".equals(str)) return false
        if ("yes".equals(str)) return true
        if ("no".equals(str)) return false
        throw IOException("can't cast string to a boolean value")
    }

    @Deprecated
    fun closeEL(`is`: InputStream?, os: OutputStream?) {
        closeEL(`is`)
        closeEL(os)
    }

    @Deprecated
    fun closeEL(zf: ZipFile?) {
        try {
            if (zf != null) zf.close()
        } catch (e: Throwable) {
        }
    }

    @Deprecated
    fun closeEL(`is`: InputStream?) {
        try {
            if (`is` != null) `is`.close()
        } catch (e: Throwable) {
        }
    }

    @Deprecated
    fun closeEL(r: Reader?) {
        try {
            if (r != null) r.close()
        } catch (e: Throwable) {
        }
    }

    @Deprecated
    fun closeEL(w: Writer?) {
        try {
            if (w != null) w.close()
        } catch (e: Throwable) {
        }
    }

    @Deprecated
    fun closeEL(os: OutputStream?) {
        try {
            if (os != null) os.close()
        } catch (e: Throwable) {
        }
    }

    @Deprecated
    @Throws(IOException::class, PageException::class)
    fun getContentAsString(`is`: InputStream?, charset: String?): String {
        val br: BufferedReader = if (charset == null) BufferedReader(InputStreamReader(`is`)) else BufferedReader(InputStreamReader(`is`, charset))
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
     * check if string is empty (null or "")
     *
     * @param str input string
     * @return is empty or not
     */
    fun isEmpty(str: String?): Boolean {
        return str == null || str.length() === 0
    }

    /**
     * check if string is empty (null or "")
     *
     * @param str input string
     * @param trim trim it
     * @return is empty or not
     */
    fun isEmpty(str: String?, trim: Boolean): Boolean {
        return if (!trim) isEmpty(str) else str == null || str.trim().length() === 0
    }

    /**
     * @param str input string
     * @return length of  String
     */
    @Deprecated
    @Deprecated("""no replacement
	  """)
    fun length(str: String?): Int {
        return str?.length() ?: 0
    }

    /**
     * @param str String to work with
     * @param sub1 value to replace
     * @param sub2 replacement
     * @param onlyFirst replace only first or all
     * @return new String
     */
    @Deprecated
    @Deprecated("""use instead CFMLEngineFactory.getInstance().getStringUtil().replace(...)
	  """)
    fun replace(str: String, sub1: String, sub2: String, onlyFirst: Boolean): String {
        if (sub1.equals(sub2)) return str
        if (!onlyFirst && sub1.length() === 1 && sub2.length() === 1) return str.replace(sub1.charAt(0), sub2.charAt(0))
        val sb = StringBuffer()
        var start = 0
        var pos: Int
        val sub1Length: Int = sub1.length()
        while (str.indexOf(sub1, start).also { pos = it } != -1) {
            sb.append(str.substring(start, pos))
            sb.append(sub2)
            start = pos + sub1Length
            if (onlyFirst) break
        }
        sb.append(str.substring(start))
        return sb.toString()
    }

    /**
     * @param path path to parse
     * @return updated path
     */
    @Deprecated
    fun parsePlaceHolder(path: String?): String {
        return CFMLEngineFactory.getInstance().getResourceUtil().parsePlaceHolder(path)
    }

    /**
     * @return temp directory
     */
    @get:Deprecated("""use instead CFMLEngineFactory.getInstance().getResourceUtil(). getTempDirectory()
	              returns the Temp Directory of the System
	  """)
    @get:Deprecated
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
        }

    /**
     * @return home directory
     */
    @get:Deprecated("""use instead CFMLEngineFactory.getInstance().getResourceUtil(). getHomeDirectory()
	              returns the Home Directory of the System
	  """)
    @get:Deprecated
    val homeDirectory: File
        get() = CFMLEngineFactory.getInstance().getResourceUtil().getHomeDirectory() as File

    /**
     * @return return System directory
     */
    @get:Deprecated("""use instead CFMLEngineFactory.getInstance().getResourceUtil(). getSystemDirectory()
	  """)
    @get:Deprecated
    val systemDirectory: File
        get() = CFMLEngineFactory.getInstance().getResourceUtil().getSystemDirectory() as File

    /**
     *
     * @param file file to get canonical form from it
     *
     * @return The canonical pathname string denoting the same file or directory as this abstract
     * pathname
     */
    @Deprecated
    fun getCanonicalFileEL(file: File?): File? {
        return try {
            file.getCanonicalFile()
        } catch (e: IOException) {
            file
        }
    }

    @Deprecated
    fun toHTTPTimeString(date: Date?): String {
        return replace(HTTP_TIME_STRING_FORMAT.format(date), "+00:00", "", true)
    }

    @Deprecated
    fun toHTTPTimeString(): String {
        return replace(HTTP_TIME_STRING_FORMAT.format(Date()), "+00:00", "", true)
    }

    @Deprecated
    fun hasUpperCase(str: String): Boolean {
        return if (isEmpty(str)) false else !str.equals(str.toLowerCase())
    }

    /**
     * @param is input stream
     * @return buffered output stream
     */
    @Deprecated
    @Deprecated("""use instead CFMLEngineFactory.getInstance().getIOUtil(). toBufferedInputStream (...)
	  """)
    fun toBufferedInputStream(`is`: InputStream?): BufferedInputStream? {
        return if (`is` is BufferedInputStream) `is` as BufferedInputStream? else BufferedInputStream(`is`)
    }

    /**
     * @param os output steam to buffer
     * @return buffered output stream
     */
    @Deprecated
    @Deprecated("""use instead CFMLEngineFactory.getInstance().getIOUtil(). toBufferedOutputStream (...)
	  """)
    fun toBufferedOutputStream(os: OutputStream?): BufferedOutputStream? {
        return if (os is BufferedOutputStream) os as BufferedOutputStream? else BufferedOutputStream(os)
    }

    /**
     * @param in input resource
     * @param out output resource
     * @throws IOException exception thrown in case copy fails
     */
    @Deprecated
    @Deprecated("""use instead CFMLEngineFactory.getInstance.getIOUtil().copy(...)
	  """)
    @Throws(IOException::class)
    fun copy(`in`: Resource, out: Resource) {
        var `is`: InputStream? = null
        var os: OutputStream? = null
        try {
            `is` = toBufferedInputStream(`in`.getInputStream())
            os = toBufferedOutputStream(out.getOutputStream())
        } catch (ioe: IOException) {
            closeEL(os)
            closeEL(`is`)
            throw ioe
        }
        copy(`is`, os)
    }

    /**
     * @param str input string
     * @param addIdentityNumber add identity number or not
     * @return variable name
     */
    @Deprecated
    @Deprecated("""use instead CFMLEngineFactory.getInstance().getStringUtil(). toVariableName (...)
	  """)
    fun toVariableName(str: String?, addIdentityNumber: Boolean): String {
        return CFMLEngineFactory.getInstance().getStringUtil().toVariableName(str, addIdentityNumber, false)
    }

    /**
     * @param str input string
     * @param delimiter delimiter to split
     * @return first item in string
     */
    @Deprecated
    @Deprecated("""use instead CFMLEngineFactory.getInstance().getStringUtil().first(...);
	  """)
    fun first(str: String?, delimiter: String?): String {
        return CFMLEngineFactory.getInstance().getStringUtil().first(str, delimiter, true)
    }

    /**
     * @param str input string
     * @param delimiter delimiter to split
     * @return last item in string
     */
    @Deprecated
    @Deprecated("""use instead CFMLEngineFactory.getInstance().getStringUtil().last(...);
	  """)
    fun last(str: String?, delimiter: String?): String {
        return CFMLEngineFactory.getInstance().getStringUtil().last(str, delimiter, true)
    }

    /**
     * @param str string to removes quotes from
     * @param trim trim it or not
     * @return string without quotes
     */
    @Deprecated
    @Deprecated("""use instead CFMLEngineFactory.getInstance().getStringUtil().removeQuotes (...);
	  """)
    fun removeQuotes(str: String?, trim: Boolean): String {
        return CFMLEngineFactory.getInstance().getStringUtil().removeQuotes(str, trim)
    }

    fun delete(f: File) {
        if (f.isDirectory()) for (c in f.listFiles()) delete(c)
        f.delete()
    }

    /**
     * check left value against right value
     *
     * @param left left operand
     * @param right right operand
     * @return returns if right is newer than left
     */
    fun isNewerThan(left: Version, right: Version): Boolean {

        // major
        if (left.getMajor() > right.getMajor()) return true
        if (left.getMajor() < right.getMajor()) return false

        // minor
        if (left.getMinor() > right.getMinor()) return true
        if (left.getMinor() < right.getMinor()) return false

        // micro
        if (left.getMicro() > right.getMicro()) return true
        if (left.getMicro() < right.getMicro()) return false

        // qualifier
        // left
        var q: String = left.getQualifier()
        var index: Int = q.indexOf('-')
        val qla = if (index == -1) "" else q.substring(index + 1).trim()
        val qln = if (index == -1) q else q.substring(0, index)
        val ql: Int = if (isEmpty(qln)) Integer.MIN_VALUE else Integer.parseInt(qln)

        // right
        q = right.getQualifier()
        index = q.indexOf('-')
        val qra = if (index == -1) "" else q.substring(index + 1).trim()
        val qrn = if (index == -1) q else q.substring(0, index)
        val qr: Int = if (isEmpty(qln)) Integer.MIN_VALUE else Integer.parseInt(qrn)
        if (ql > qr) return true
        if (ql < qr) return false
        val qlan = qualifierAppendix2Number(qla)
        val qran = qualifierAppendix2Number(qra)
        if (qlan > qran) return true
        if (qlan < qran) return false
        return if (qlan == QUALIFIER_APPENDIX_OTHER && qran == QUALIFIER_APPENDIX_OTHER) left.compareTo(right) > 0 else false
    }

    private fun qualifierAppendix2Number(str: String): Int {
        if (isEmpty(str, true)) return QUALIFIER_APPENDIX_STABLE
        if ("SNAPSHOT".equalsIgnoreCase(str)) return QUALIFIER_APPENDIX_SNAPSHOT
        if ("BETA".equalsIgnoreCase(str)) return QUALIFIER_APPENDIX_BETA
        return if ("RC".equalsIgnoreCase(str)) QUALIFIER_APPENDIX_RC else QUALIFIER_APPENDIX_OTHER
    }

    fun deleteContent(src: Resource?, filter: ResourceFilter?) {
        _deleteContent(src, filter, false)
    }

    fun _deleteContent(src: Resource, filter: ResourceFilter?, deleteDirectories: Boolean) {
        if (src.isDirectory()) {
            val files: Array<Resource> = if (filter == null) src.listResources() else src.listResources(filter)
            for (i in files.indices) {
                _deleteContent(files[i], filter, true)
                if (deleteDirectories) {
                    try {
                        src.remove(false)
                    } catch (e: IOException) {
                    }
                }
            }
        } else if (src.isFile()) {
            src.delete()
        }
    }

    fun deleteContent(src: File?, filter: FileFilter?) {
        _deleteContent(src, filter, false)
    }

    fun _deleteContent(src: File, filter: FileFilter?, deleteDirectories: Boolean) {
        if (src.isDirectory()) {
            val files: Array<File> = if (filter == null) src.listFiles() else src.listFiles(filter)
            for (i in files.indices) {
                _deleteContent(files[i], filter, true)
                if (deleteDirectories) {
                    src.delete()
                }
            }
        } else if (src.isFile()) {
            src.delete()
        }
    }

    /**
     * returns a system setting by either a Java property name or a System environment variable
     *
     * @param name - either a lowercased Java property name (e.g. lucee.controller.disabled) or an
     * UPPERCASED Environment variable name ((e.g. LUCEE_CONTROLLER_DISABLED))
     * @param defaultValue - value to return if the neither the property nor the environment setting was
     * found
     * @return - the value of the property referenced by propOrEnv or the defaultValue if not found
     */
    fun _getSystemPropOrEnvVar(name: String, defaultValue: String?): String? { // FUTURE remove _ in front of the name
        // env
        var name = name
        var value: String = System.getenv(name)
        if (!isEmpty(value)) return value

        // prop
        value = System.getProperty(name)
        if (!isEmpty(value)) return value

        // env 2
        name = name.replace('.', '_').toUpperCase()
        value = System.getenv(name)
        return if (!isEmpty(value)) value else defaultValue
    }

    init {
        HTTP_TIME_STRING_FORMAT = SimpleDateFormat("EE, dd MMM yyyy HH:mm:ss zz", Locale.ENGLISH)
        HTTP_TIME_STRING_FORMAT.setTimeZone(TimeZone.getTimeZone("GMT"))
    }
}