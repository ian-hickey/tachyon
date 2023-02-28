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
package lucee.commons.io.res.type.compress

import java.io.IOException

class Compress(file: Resource, format: Int, caseSensitive: Boolean) {
    // private final static Map files=new WeakHashMap();
    private val ffile: Resource

    // private ResourceProvider ramProvider;
    private var syn: Long = -1
    private var root: Resource? = null
    private var synchronizer: Synchronizer? = null
    private var lastMod: Long = -1
    private var lastCheck: Long = -1
    private val format: Int
    private var mode: Int
    private val caseSensitive = false
    private var temp: Resource? = null
    @Synchronized
    @Throws(IOException::class)
    private fun load(caseSensitive: Boolean) {
        val actLastMod: Long = ffile.lastModified()
        lastMod = actLastMod
        lastCheck = System.currentTimeMillis()
        val args: Map<String, Boolean> = HashMap<String, Boolean>()
        args.put("case-sensitive", Caster.toBoolean(caseSensitive))
        if (temp == null) {
            var cid = ""
            val config: Config = ThreadLocalPageContext.getConfig()
            if (config != null) {
                cid = config.getIdentification().getId()
                temp = config.getTempDirectory()
            }
            if (temp == null) temp = SystemUtil.getTempDirectory()
            temp = temp.getRealResource("compress")
            temp = temp.getRealResource(MD5.getDigestAsString(cid + "-" + ffile.getAbsolutePath()))
            if (!temp.exists()) temp.mkdirs()
        }
        if (temp != null) {
            var name: String = Caster.toString(actLastMod).toString() + ":" + Caster.toString(ffile.length())
            name = MD5.getDigestAsString(name, name)
            root = temp.getRealResource(name)
            if (actLastMod > 0 && root.exists()) return
            ResourceUtil.removeChildrenEL(temp)
            // if(root!=null)ResourceUtil.removeChildrenEL(root);
            // String name=CreateUUID.invoke();
            // root=temp.getRealResource(name);
            root.mkdirs()
        } else {
            val ramProvider: ResourceProvider = RamResourceProviderOld().init("ram", args)
            root = ramProvider.getResource("/")
        }
        _load()
    }

    private fun _load() {
        if (ffile.exists()) {
            try {
                CompressUtil.extract(format, ffile, root)
            } catch (e: IOException) {
            }
        } else {
            try {
                ffile.createFile(false)
            } catch (e: IOException) {
            }
            lastMod = ffile.lastModified()
        }
    }

    @Throws(IOException::class)
    fun getRamProviderResource(path: String?): Resource {
        var t: Long = System.currentTimeMillis()
        if (t > lastCheck + 2000) {
            lastCheck = t
            t = ffile.lastModified()
            if (lastMod - t > 10 || t - lastMod > 10 || root == null || !root.exists()) {
                lastMod = t
                load(caseSensitive)
            }
        }
        return root.getRealResource(path) // ramProvider.getResource(path);
    }

    /**
     * @return the zipFile
     */
    val compressFile: Resource
        get() = ffile

    @Synchronized
    fun synchronize(async: Boolean) {
        if (!async) {
            doSynchronize()
            return
        }
        syn = System.currentTimeMillis()
        if (synchronizer == null || !synchronizer!!.isRunning) {
            synchronizer = Synchronizer(this, 100)
            synchronizer.start()
        }
    }

    private fun doSynchronize() {
        try {
            CompressUtil.compress(format, root.listResources(), ffile, 777)
            // ramProvider=null;
        } catch (e: IOException) {
        }
    }

    internal inner class Synchronizer(private val zip: Compress, private val interval: Int) : Thread() {
        var isRunning = true
            private set

        @Override
        fun run() {
            if (FORMAT_TAR == format) runTar(ffile)
            if (FORMAT_TGZ == format) runTGZ(ffile) else runZip(ffile)
        }

        private fun runTGZ(res: Resource) {
            var gos: GZIPOutputStream? = null
            var tmpis: InputStream? = null
            val tmp: Resource = SystemUtil.getTempDirectory().getRealResource(System.currentTimeMillis().toString() + "_.tgz")
            try {
                gos = GZIPOutputStream(res.getOutputStream())
                // wait for sync
                while (true) {
                    sleepEL()
                    if (zip.syn + interval <= System.currentTimeMillis()) break
                }
                // sync
                tmpis = tmp.getInputStream()
                CompressUtil.compressTar(root.listResources(), tmp, -1)
                CompressUtil.compressGZip(tmpis, gos)
            } catch (e: IOException) {
            } finally {
                IOUtil.closeEL(gos)
                IOUtil.closeEL(tmpis)
                tmp.delete()
                isRunning = false
            }
        }

        private fun runTar(res: Resource) {
            var tos: TarArchiveOutputStream? = null
            try {
                tos = TarArchiveOutputStream(res.getOutputStream())
                tos.setLongFileMode(TarArchiveOutputStream.LONGFILE_GNU)
                // wait for sync
                while (true) {
                    sleepEL()
                    if (zip.syn + interval <= System.currentTimeMillis()) break
                }
                // sync
                CompressUtil.compressTar(root.listResources(), tos, -1)
            } catch (e: IOException) {
            } finally {
                IOUtil.closeEL(tos)
                isRunning = false
            }
        }

        private fun runZip(res: Resource) {
            var zos: ZipOutputStream? = null
            try {
                zos = ZipOutputStream(res.getOutputStream())
                // wait for sync
                while (true) {
                    sleepEL()
                    if (zip.syn + interval <= System.currentTimeMillis()) break
                }
                // sync
                CompressUtil.compressZip(root.listResources(), zos, null)
            } catch (e: IOException) {
            } finally {
                IOUtil.closeEL(zos)
                isRunning = false
            }
        }

        private fun sleepEL() {
            try {
                sleep(interval)
            } catch (e: InterruptedException) {
            }
        }
    }

    companion object {
        val FORMAT_ZIP: Int = CompressUtil.FORMAT_ZIP
        val FORMAT_TAR: Int = CompressUtil.FORMAT_TAR
        val FORMAT_TGZ: Int = CompressUtil.FORMAT_TGZ
        val FORMAT_TBZ2: Int = CompressUtil.FORMAT_TBZ2

        /**
         * return zip instance matching the zipfile, singelton instance only 1 zip for one file
         *
         * @param zipFile
         * @param format
         * @param caseSensitive
         * @return
         * @throws IOException
         */
        @Throws(IOException::class)
        fun getInstance(zipFile: Resource?, format: Int, caseSensitive: Boolean): Compress {
            val config: ConfigPro = ThreadLocalPageContext.getConfig() as ConfigPro
            return config.getCompressInstance(zipFile, format, caseSensitive)
        }
    }

    /**
     * private Constructor of the class, will be invoked be getInstance
     *
     * @param file
     * @param format
     * @param caseSensitive
     * @throws IOException
     */
    init {
        ffile = file
        this.format = format
        mode = ffile.getMode()
        if (mode == 0) mode = 511
        load(caseSensitive.also { this.caseSensitive = it })
    }
}