/**
 * Copyright (c) 2023, TachyonCFML.org
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
package tachyon.runtime.cache.legacy

import java.io.ByteArrayInputStream

internal class CacheItemFS(pc: PageContext?, req: HttpServletRequest?, id: String?, key: String?, useId: Boolean, dir: Resource?) : CacheItem(pc, req, id, key, useId) {
    private val res: Resource?
    private val directory: Resource?
    private val name: String?

    @Override
    override fun isValid(): Boolean {
        return res != null
    }

    @Override
    override fun isValid(timespan: TimeSpan?): Boolean {
        return res != null && res.exists() && res.lastModified() + timespan.getMillis() >= System.currentTimeMillis()
    }

    @Override
    @Throws(IOException::class)
    override fun writeTo(os: OutputStream?, charset: String?) {
        IOUtil.copy(res.getInputStream(), os, true, false)
    }

    @Override
    @Throws(IOException::class)
    override fun getValue(): String? {
        return IOUtil.toString(res, "UTF-8")
    }

    @Override
    @Throws(IOException::class)
    override fun store(result: String?) {
        IOUtil.write(res, result, "UTF-8", false)
        MetaData.getInstance(directory)!!.add(name, fileName)
    }

    @Override
    @Throws(IOException::class)
    override fun store(barr: ByteArray?, append: Boolean) {
        IOUtil.copy(ByteArrayInputStream(barr), res.getOutputStream(append), true, true)
        MetaData.getInstance(directory)!!.add(name, fileName)
    }

    companion object {
        @Throws(IOException::class)
        private fun getDirectory(pc: PageContext?): Resource? {
            val dir: Resource = pc.getConfig().getCacheDir()
            if (!dir.exists()) dir.createDirectory(true)
            return dir
        }

        @Throws(IOException::class)
        fun _flushAll(pc: PageContext?, dir: Resource?) {
            var dir: Resource? = dir
            if (dir == null) dir = getDirectory(pc)
            ResourceUtil.removeChildrenEL(dir)
        }

        @Throws(IOException::class)
        fun _flush(pc: PageContext?, dir: Resource?, expireurl: String?) {
            var dir: Resource? = dir
            if (dir == null) dir = getDirectory(pc)
            val names: List<String?>
            names = MetaData.getInstance(dir)!!.get(expireurl)
            val it = names.iterator()
            var name: String?
            while (it.hasNext()) {
                name = it.next()
                if (dir.getRealResource(name).delete()) {
                }
            }
        }
    }

    init {
        // directory
        directory = if (dir != null) dir else getDirectory(pc)

        // name
        name = Md5.getDigestAsString(fileName).toString() + ".cache"

        // res
        res = directory.getRealResource(name)
    }
}