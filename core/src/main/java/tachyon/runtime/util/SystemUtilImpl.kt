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

class SystemUtilImpl : SystemUtil {
    @Override
    fun isFSCaseSensitive(): Boolean {
        return tachyon.commons.io.SystemUtil.isFSCaseSensitive()
    }

    @Override
    fun isWindows(): Boolean {
        return tachyon.commons.io.SystemUtil.isWindows()
    }

    @Override
    fun isLinux(): Boolean {
        return tachyon.commons.io.SystemUtil.isLinux()
    }

    @Override
    fun isSolaris(): Boolean {
        return tachyon.commons.io.SystemUtil.isSolaris()
    }

    @Override
    fun isMacOSX(): Boolean {
        return tachyon.commons.io.SystemUtil.isMacOSX()
    }

    @Override
    fun isUnix(): Boolean {
        return tachyon.commons.io.SystemUtil.isUnix()
    }

    @Override
    fun getSystemDirectory(): Resource? {
        return tachyon.commons.io.SystemUtil.getSystemDirectory()
    }

    @Override
    fun getRuningContextRoot(): Resource? {
        return tachyon.commons.io.SystemUtil.getRuningContextRoot()
    }

    @Override
    @Throws(IOException::class)
    fun getTempDirectory(): Resource? {
        return tachyon.commons.io.SystemUtil.getTempDirectory()
    }

    @Override
    @Throws(IOException::class)
    fun getTempFile(extension: String?, touch: Boolean): Resource? {
        return tachyon.commons.io.SystemUtil.getTempFile(extension!!, touch)
    }

    @Override
    fun getHomeDirectory(): Resource? {
        return tachyon.commons.io.SystemUtil.getHomeDirectory()
    }

    @Override
    fun parsePlaceHolder(path: String?): String? {
        return tachyon.commons.io.SystemUtil.parsePlaceHolder(path)
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
        return tachyon.commons.io.SystemUtil.hash(sc)
    }

    @Override
    fun getCharset(): Charset? {
        return tachyon.commons.io.SystemUtil.getCharset()
    }

    @Override
    fun setCharset(charset: Charset?) {
        tachyon.commons.io.SystemUtil.setCharset(charset)
    }

    @Override
    fun getOSSpecificLineSeparator(): String? {
        return tachyon.commons.io.SystemUtil.getOSSpecificLineSeparator()
    }

    @Override
    fun getOSArch(): Int {
        return tachyon.commons.io.SystemUtil.getOSArch()
    }

    @Override
    fun getJREArch(): Int {
        return tachyon.commons.io.SystemUtil.getJREArch()
    }

    @Override
    fun getAddressSize(): Int {
        return tachyon.commons.io.SystemUtil.getAddressSize()
    }

    @Override
    fun getFreePermGenSpaceSize(): Long {
        return tachyon.commons.io.SystemUtil.getFreePermGenSpaceSize()
    }

    @Override
    fun getPermGenFreeSpaceAsAPercentageOfAvailable(): Int {
        return tachyon.commons.io.SystemUtil.getPermGenFreeSpaceAsAPercentageOfAvailable()
    }

    @Override
    fun getFreePermGenSpacePromille(): Int {
        return tachyon.commons.io.SystemUtil.getFreePermGenSpacePromille()
    }

    @Override
    @Throws(PageException::class)
    fun getMemoryUsageAsQuery(type: Int): Query? {
        return tachyon.commons.io.SystemUtil.getMemoryUsageAsQuery(type)
    }

    @Override
    fun getMemoryUsageAsStruct(type: Int): Struct? {
        return tachyon.commons.io.SystemUtil.getMemoryUsageAsStruct(type)
    }

    @Override
    fun getMemoryUsageCompact(type: Int): Struct? {
        return tachyon.commons.io.SystemUtil.getMemoryUsageCompact(type)
    }

    @Override
    @Throws(PageException::class)
    fun getFreeBytes(): Long {
        return tachyon.commons.io.SystemUtil.getFreeBytes()
    }

    @Override
    @Throws(PageException::class)
    fun getTotalBytes(): Long {
        return tachyon.commons.io.SystemUtil.getTotalBytes()
    }

    @Override
    @Throws(PageException::class)
    fun getCpuUsage(time: Long): Double {
        return tachyon.commons.io.SystemUtil.getCpuUsage(time)
    }

    @Override
    fun setPrintWriter(type: Int, pw: PrintWriter?) {
        tachyon.commons.io.SystemUtil.setPrintWriter(type, pw)
    }

    @Override
    fun getPrintWriter(type: Int): PrintWriter? {
        return tachyon.commons.io.SystemUtil.getPrintWriter(type)
    }

    @Override
    fun getLoaderVersion(): Double {
        return tachyon.commons.io.SystemUtil.getLoaderVersion()
    }

    @Override
    fun stop(thread: Thread?) {
        tachyon.commons.io.SystemUtil.stop(thread)
    }

    @Override
    fun stop(pc: PageContext?, t: Throwable?, log: Log?) {
        // FUTURE remove argument Throwable t
        tachyon.commons.io.SystemUtil.stop(pc, true)
    }

    @Override
    fun getMacAddress(): String? {
        return tachyon.commons.io.SystemUtil.getMacAddress(null)
    }

    @Override
    fun getResource(bundle: Bundle?, path: String?): URL? {
        return tachyon.commons.io.SystemUtil.getResource(bundle, path!!)
    }

    @Override
    fun addLibraryPath(res: Resource?) {
        tachyon.commons.io.SystemUtil.addLibraryPathIfNoExist(res, null)
    }
}