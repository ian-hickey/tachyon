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
package tachyon.commons.io.res.type.ftp

import java.io.IOException

class FTPResourceClient(ftpConnectionData: FTPConnectionData, cacheTimeout: Int) : FTPClient() {
    private var workingDirectory: String? = null
    private val ftpConnectionData: FTPConnectionData

    /**
     * @return the lastAccess
     */
    var lastAccess: Long = 0
        private set
    val token: Object = SerializableObject()
    private val sync: Object = SerializableObject()
    private val files: Map<String, FTPFileWrap> = MapFactory.< String, FTPFileWrap>getConcurrentMap<String?, tachyon.commons.io.res.type.ftp.FTPResourceClient.FTPFileWrap?>()
    private val cacheTimeout: Int

    /**
     * @return the ftpConnectionData
     */
    fun getFtpConnectionData(): FTPConnectionData {
        return ftpConnectionData
    }

    fun touch() {
        lastAccess = System.currentTimeMillis()
    }

    @Override
    @Throws(IOException::class)
    fun changeWorkingDirectory(pathname: String): Boolean {
        var pathname = pathname
        if (StringUtil.endsWith(pathname, '/') && pathname.length() !== 1) pathname = pathname.substring(0, pathname.length() - 1)
        if (pathname.equals(workingDirectory)) return true
        workingDirectory = pathname
        return super.changeWorkingDirectory(pathname)
    }

    fun id(): String {
        return ftpConnectionData.toString()
    }

    @Override
    override fun equals(obj: Object): Boolean {
        return (obj as FTPResourceClient).id().equals(id())
    }

    @Throws(IOException::class)
    fun getFTPFile(res: FTPResource): FTPFile? {
        val path: String = res.getInnerPath()
        val fw = files[path] ?: return createFTPFile(res)
        if (fw.time + cacheTimeout < System.currentTimeMillis()) {
            files.remove(path)
            return createFTPFile(res)
        }
        return fw.file
    }

    fun registerFTPFile(res: FTPResource, file: FTPFile?) {
        files.put(res.getInnerPath(), FTPFileWrap(file))
    }

    fun unregisterFTPFile(res: FTPResource) {
        files.remove(res.getInnerPath())
    }

    @Throws(IOException::class)
    private fun createFTPFile(res: FTPResource): FTPFile? {
        var children: Array<FTPFile>? = null
        val isRoot: Boolean = res.isRoot()
        val path: String = if (isRoot) res.getInnerPath() else res.getInnerParent()
        synchronized(sync) {
            changeWorkingDirectory(path)
            children = listFiles()
        }
        if (children!!.size > 0) {
            for (i in children.indices) {
                if (isRoot) {
                    if (children!![i].getName().equals(".")) {
                        registerFTPFile(res, children!![i])
                        return children!![i]
                    }
                } else {
                    if (children!![i].getName().equals(res.getName())) {
                        registerFTPFile(res, children!![i])
                        return children!![i]
                    }
                }
            }
        }
        return null
    }

    @Override
    @Throws(IOException::class)
    fun deleteFile(pathname: String?): Boolean {
        files.remove(pathname)
        return super.deleteFile(pathname)
    }

    internal inner class FTPFileWrap(file: FTPFile) {
        val file: FTPFile
        val time: Long

        init {
            this.file = file
            time = System.currentTimeMillis()
        }
    }

    init {
        this.ftpConnectionData = ftpConnectionData
        this.cacheTimeout = cacheTimeout
    }
}