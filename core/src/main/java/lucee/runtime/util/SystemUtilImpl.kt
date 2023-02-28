/**
 * Copyright (c) 2015, Lucee Assosication Switzerland. All rights reserved.
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
package lucee.runtime.util

import java.io.IOException

class SystemUtilImpl : SystemUtil {
    @Override
    fun isFSCaseSensitive(): Boolean {
        return lucee.commons.io.SystemUtil.isFSCaseSensitive()
    }

    @Override
    fun isWindows(): Boolean {
        return lucee.commons.io.SystemUtil.isWindows()
    }

    @Override
    fun isLinux(): Boolean {
        return lucee.commons.io.SystemUtil.isLinux()
    }

    @Override
    fun isSolaris(): Boolean {
        return lucee.commons.io.SystemUtil.isSolaris()
    }

    @Override
    fun isMacOSX(): Boolean {
        return lucee.commons.io.SystemUtil.isMacOSX()
    }

    @Override
    fun isUnix(): Boolean {
        return lucee.commons.io.SystemUtil.isUnix()
    }

    @Override
    fun getSystemDirectory(): Resource? {
        return lucee.commons.io.SystemUtil.getSystemDirectory()
    }

    @Override
    fun getRuningContextRoot(): Resource? {
        return lucee.commons.io.SystemUtil.getRuningContextRoot()
    }

    @Override
    @Throws(IOException::class)
    fun getTempDirectory(): Resource? {
        return lucee.commons.io.SystemUtil.getTempDirectory()
    }

    @Override
    @Throws(IOException::class)
    fun getTempFile(extension: String?, touch: Boolean): Resource? {
        return lucee.commons.io.SystemUtil.getTempFile(extension!!, touch)
    }

    @Override
    fun getHomeDirectory(): Resource? {
        return lucee.commons.io.SystemUtil.getHomeDirectory()
    }

    @Override
    fun parsePlaceHolder(path: String?): String? {
        return lucee.commons.io.SystemUtil.parsePlaceHolder(path)
    }

    @Override
    fun hash64b(str: String?): String? {
        return HashUtil.create64BitHashAsString(str)
    }

    @Override
    @Throws(IOException::class)
    fun hashMd5(str: String?): String? {
        return try {
            Hash.md5(str)
        } catch (e: NoSuchAlgorithmException) {
            throw IOException(e)
        }
    }

    @Override
    fun hash(sc: ServletContext?): String? {
        return lucee.commons.io.SystemUtil.hash(sc)
    }

    @Override
    fun getCharset(): Charset? {
        return lucee.commons.io.SystemUtil.getCharset()
    }

    @Override
    fun setCharset(charset: Charset?) {
        lucee.commons.io.SystemUtil.setCharset(charset)
    }

    @Override
    fun getOSSpecificLineSeparator(): String? {
        return lucee.commons.io.SystemUtil.getOSSpecificLineSeparator()
    }

    @Override
    fun getOSArch(): Int {
        return lucee.commons.io.SystemUtil.getOSArch()
    }

    @Override
    fun getJREArch(): Int {
        return lucee.commons.io.SystemUtil.getJREArch()
    }

    @Override
    fun getAddressSize(): Int {
        return lucee.commons.io.SystemUtil.getAddressSize()
    }

    @Override
    fun getFreePermGenSpaceSize(): Long {
        return lucee.commons.io.SystemUtil.getFreePermGenSpaceSize()
    }

    @Override
    fun getPermGenFreeSpaceAsAPercentageOfAvailable(): Int {
        return lucee.commons.io.SystemUtil.getPermGenFreeSpaceAsAPercentageOfAvailable()
    }

    @Override
    fun getFreePermGenSpacePromille(): Int {
        return lucee.commons.io.SystemUtil.getFreePermGenSpacePromille()
    }

    @Override
    @Throws(PageException::class)
    fun getMemoryUsageAsQuery(type: Int): Query? {
        return lucee.commons.io.SystemUtil.getMemoryUsageAsQuery(type)
    }

    @Override
    fun getMemoryUsageAsStruct(type: Int): Struct? {
        return lucee.commons.io.SystemUtil.getMemoryUsageAsStruct(type)
    }

    @Override
    fun getMemoryUsageCompact(type: Int): Struct? {
        return lucee.commons.io.SystemUtil.getMemoryUsageCompact(type)
    }

    @Override
    @Throws(PageException::class)
    fun getFreeBytes(): Long {
        return lucee.commons.io.SystemUtil.getFreeBytes()
    }

    @Override
    @Throws(PageException::class)
    fun getTotalBytes(): Long {
        return lucee.commons.io.SystemUtil.getTotalBytes()
    }

    @Override
    @Throws(PageException::class)
    fun getCpuUsage(time: Long): Double {
        return lucee.commons.io.SystemUtil.getCpuUsage(time)
    }

    @Override
    fun setPrintWriter(type: Int, pw: PrintWriter?) {
        lucee.commons.io.SystemUtil.setPrintWriter(type, pw)
    }

    @Override
    fun getPrintWriter(type: Int): PrintWriter? {
        return lucee.commons.io.SystemUtil.getPrintWriter(type)
    }

    @Override
    fun getLoaderVersion(): Double {
        return lucee.commons.io.SystemUtil.getLoaderVersion()
    }

    @Override
    fun stop(thread: Thread?) {
        lucee.commons.io.SystemUtil.stop(thread)
    }

    @Override
    fun stop(pc: PageContext?, t: Throwable?, log: Log?) {
        // FUTURE remove argument Throwable t
        lucee.commons.io.SystemUtil.stop(pc, true)
    }

    @Override
    fun getMacAddress(): String? {
        return lucee.commons.io.SystemUtil.getMacAddress(null)
    }

    @Override
    fun getResource(bundle: Bundle?, path: String?): URL? {
        return lucee.commons.io.SystemUtil.getResource(bundle, path!!)
    }

    @Override
    fun addLibraryPath(res: Resource?) {
        lucee.commons.io.SystemUtil.addLibraryPathIfNoExist(res, null)
    }
}