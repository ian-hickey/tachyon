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
package lucee.runtime.functions.system

import java.io.ByteArrayInputStream

object ManifestRead {
    @Throws(PageException::class)
    fun call(pc: PageContext?, str: String?): Struct? {
        var manifest: Manifest? = null
        // is it a file?
        var res: Resource? = null
        try {
            res = ResourceUtil.toResourceExisting(pc, str)
        } catch (t: Throwable) {
            ExceptionUtil.rethrowIfNecessary(t)
        }

        // is a file!
        if (res != null) {
            try {
                // is it a jar?
                var zip: ZipFile? = null
                try {
                    zip = ZipFile(FileWrapper.toFile(res))
                } catch (e: Exception) {
                    /* no jar or invalid jar */
                }

                // it is a jar
                if (zip != null) {
                    var `is`: InputStream? = null
                    try {
                        val ze: ZipEntry = zip.getEntry("META-INF/MANIFEST.MF")
                                ?: throw ApplicationException("zip file [$str] has no entry with name [META-INF/MANIFEST.MF]")
                        `is` = zip.getInputStream(ze)
                        manifest = Manifest(`is`)
                    } finally {
                        IOUtil.close(`is`)
                        IOUtil.closeEL(zip)
                    }
                } else {
                    var `is`: InputStream? = null
                    try {
                        manifest = Manifest(res.getInputStream().also { `is` = it })
                    } finally {
                        IOUtil.close(`is`)
                    }
                }
            } catch (t: Throwable) {
                ExceptionUtil.rethrowIfNecessary(t)
                throw Caster.toPageException(t)
            }
        }

        // was not a file
        if (manifest == null) {
            try {
                manifest = Manifest(ByteArrayInputStream(str.getBytes()))
            } catch (e: IOException) {
                throw Caster.toPageException(e)
            }
        }
        val sct: Struct = StructImpl()
        // set the main attributes
        ManifestRead[sct, "main"] = manifest.getMainAttributes()

        // all the others
        val set: Set<Entry<String?, Attributes?>?> = manifest.getEntries().entrySet()
        if (set.size() > 0) {
            val it: Iterator<Entry<String?, Attributes?>?> = set.iterator()
            val sec: Struct = StructImpl()
            sct.setEL("sections", sec)
            var e: Entry<String?, Attributes?>?
            while (it.hasNext()) {
                e = it.next()
                ManifestRead[sec, e.getKey()] = e.getValue()
            }
        }
        return sct
    }

    @Throws(PageException::class)
    private operator fun set(parent: Struct?, key: String?, attrs: Attributes?) {
        val sct: Struct = StructImpl()
        parent.set(key, sct)
        val it: Iterator<Entry<Object?, Object?>?> = attrs.entrySet().iterator()
        var e: Entry<Object?, Object?>?
        while (it.hasNext()) {
            e = it.next()
            sct.setEL(Caster.toString(e.getKey()), StringUtil.unwrap(Caster.toString(e.getValue())))
        }
    }
}