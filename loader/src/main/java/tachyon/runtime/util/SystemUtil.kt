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
package tachyon.runtime.util

import java.io.IOException

interface SystemUtil {
    /**
     * returns if the file system case sensitive or not
     *
     * @return is the file system case sensitive or not
     */
    val isFSCaseSensitive: Boolean

    /**
     * @return is local machine a Windows Machine
     */
    val isWindows: Boolean

    /**
     * @return is local machine a Linux Machine
     */
    val isLinux: Boolean

    /**
     * @return is local machine a Solaris Machine
     */
    val isSolaris: Boolean

    /**
     * @return is local machine a Solaris Machine
     */
    val isMacOSX: Boolean

    /**
     * @return is local machine a Unix Machine
     */
    val isUnix: Boolean

    /**
     * @return return System directory
     */
    val systemDirectory: Resource?

    /**
     * @return return running context root
     */
    val runingContextRoot: Resource?

    /**
     * returns the Temp Directory of the System
     *
     * @return temp directory
     * @throws IOException IO Exception
     */
    @get:Throws(IOException::class)
    val tempDirectory: Resource?

    /**
     * returns a unique temp file (with no auto delete)
     *
     * @param extension File Extension
     * @param touch touch
     * @return temp directory
     * @throws IOException IO Exception
     */
    @Throws(IOException::class)
    fun getTempFile(extension: String?, touch: Boolean): Resource?

    /**
     * returns the Home Directory of the System
     *
     * @return home directory
     */
    val homeDirectory: Resource?

    /**
     * replace path placeholder with the real path, placeholders are
     * [{temp-directory},{system-directory},{home-directory}]
     *
     * @param path path
     * @return updated path
     */
    fun parsePlaceHolder(path: String?): String?
    fun hash64b(str: String?): String?

    @Throws(IOException::class)
    fun hashMd5(str: String?): String?
    fun hash(sc: ServletContext?): String?
    var charset: Charset?
    val oSSpecificLineSeparator: String?

    /**
     * return the operating system architecture
     *
     * @return one of the following SystemUtil.ARCH_UNKNOW, SystemUtil.ARCH_32, SystemUtil.ARCH_64
     */
    val oSArch: Int

    /**
     * return the JRE (Java Runtime Engine) architecture, this can be different from the operating
     * system architecture
     *
     * @return one of the following SystemUtil.ARCH_UNKNOW, SystemUtil.ARCH_32, SystemUtil.ARCH_64
     */
    val jREArch: Int
    val addressSize: Int
    val freePermGenSpaceSize: Long
    val permGenFreeSpaceAsAPercentageOfAvailable: Int
    val freePermGenSpacePromille: Int

    @Throws(PageException::class)
    fun getMemoryUsageAsQuery(type: Int): Query?
    fun getMemoryUsageAsStruct(type: Int): Struct?
    fun getMemoryUsageCompact(type: Int): Struct?

    @get:Throws(PageException::class)
    val freeBytes: Long

    @get:Throws(PageException::class)
    val totalBytes: Long

    @Throws(PageException::class)
    fun getCpuUsage(time: Long): Double

    /**
     * set the printer writer for System.out or System.err
     *
     * @param type Type
     * @param pw Print Writer
     */
    fun setPrintWriter(type: Int, pw: PrintWriter?)

    /**
     * get the printer writer for System.out or System.err
     *
     * @param type OUT or ERR
     * @return Returns a Print Writer.
     */
    fun getPrintWriter(type: Int): PrintWriter?
    val loaderVersion: Double
    fun stop(thread: Thread?)
    fun stop(pc: PageContext?, t: Throwable?, log: Log?) // FUTURE deprecated

    // public void stop(PageContext pc, Log log); // FUTURE add
    val macAddress: String?
    fun getResource(bundle: Bundle?, path: String?): URL?

    /**
     * add resource to "java.library.path"
     *
     * @param res Resource
     */
    fun addLibraryPath(res: Resource?)

    companion object {
        const val MEMORY_TYPE_ALL = 0
        const val MEMORY_TYPE_HEAP = 1
        const val MEMORY_TYPE_NON_HEAP = 2
        const val ARCH_UNKNOW = 0
        const val ARCH_32 = 32
        const val ARCH_64 = 64
        const val CHAR_DOLLAR = 36.toChar()
        const val CHAR_POUND = 163.toChar()
        const val CHAR_EURO = 8364.toChar()
        const val JAVA_VERSION_1_0 = 0
        const val JAVA_VERSION_1_1 = 1
        const val JAVA_VERSION_1_2 = 2
        const val JAVA_VERSION_1_3 = 3
        const val JAVA_VERSION_1_4 = 4
        const val JAVA_VERSION_1_5 = 5
        const val JAVA_VERSION_1_6 = 6
        const val JAVA_VERSION_1_7 = 7
        const val JAVA_VERSION_1_8 = 8
        const val JAVA_VERSION_1_9 = 9

        /*
	 * FUTURE public final int JAVA_VERSION_1_10 = 10; public final int JAVA_VERSION_1_11 = 11; public
	 * final int JAVA_VERSION_1_12 = 12; public final int JAVA_VERSION_1_13 = 13; public final int
	 * JAVA_VERSION_1_14 = 14;
	 * 
	 * public final int JAVA_VERSION_9 = JAVA_VERSION_1_9; public final int JAVA_VERSION_10 =
	 * JAVA_VERSION_1_10; public final int JAVA_VERSION_11 = JAVA_VERSION_1_11; public final int
	 * JAVA_VERSION_12 = JAVA_VERSION_1_12; public final int JAVA_VERSION_13 = JAVA_VERSION_1_13; public
	 * final int JAVA_VERSION_14 = JAVA_VERSION_1_14;
	 */
        const val OUT = 0
        const val ERR = 1
    }
}