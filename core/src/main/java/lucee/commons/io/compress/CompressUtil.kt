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
package lucee.commons.io.compress

import java.io.ByteArrayInputStream

/**
 * Util to manipulate zip files
 */
object CompressUtil {
    /**
     * Field `FORMAT_ZIP`
     */
    const val FORMAT_ZIP = 0

    /**
     * Field `FORMAT_TAR`
     */
    const val FORMAT_TAR = 1

    /**
     * Field `FORMAT_TGZ`
     */
    const val FORMAT_TGZ = 2

    /**
     * Field `FORMAT_GZIP`
     */
    const val FORMAT_GZIP = 3

    /**
     * Field `FORMAT_BZIP`
     */
    const val FORMAT_BZIP = 4

    /**
     * Field `FORMAT_BZIP`
     */
    const val FORMAT_BZIP2 = 4

    /**
     * Field `FORMAT_TBZ`
     */
    const val FORMAT_TBZ = 5

    /**
     * Field `FORMAT_TBZ2`
     */
    const val FORMAT_TBZ2 = 5

    /**
     * extract a zip file to a directory
     *
     * @param format
     * @param source
     * @param target
     * @throws IOException
     */
    @Throws(IOException::class)
    fun extract(format: Int, source: Resource, target: Resource) {
        if (format == FORMAT_ZIP) extractZip(source, target) else if (format == FORMAT_TAR) extractTar(source, target) else if (format == FORMAT_GZIP) extractGZip(source, target) else if (format == FORMAT_BZIP) extractBZip(source, target) else if (format == FORMAT_TGZ) extractTGZ(source, target) else if (format == FORMAT_TBZ) extractTBZ(source, target) else throw IOException("Can't extract in given format")
    }

    /*
	 * public static void listt(int format, Resource source) throws IOException { if (format ==
	 * FORMAT_ZIP) listZipp(source); // else if(format==FORMAT_TAR) listar(source); // else
	 * if(format==FORMAT_GZIP)listGZip(source); // else if(format==FORMAT_TGZ) listTGZ(source); else
	 * throw new IOException("can't list in given format, atm only zip files are supported"); }
	 */
    @Throws(IOException::class)
    private fun extractTGZ(source: Resource, target: Resource) {
        // File tmpTarget = File.createTempFile("_temp","tmp");
        val tmp: Resource = SystemUtil.getTempDirectory().getRealResource(System.currentTimeMillis().toString() + ".tmp")
        try {
            // read Gzip
            extractGZip(source, tmp)

            // read Tar
            extractTar(tmp, target)
        } finally {
            tmp.delete()
        }
    }

    @Throws(IOException::class)
    private fun extractTBZ(source: Resource, target: Resource) {
        // File tmpTarget = File.createTempFile("_temp","tmp");
        val tmp: Resource = SystemUtil.getTempDirectory().getRealResource(System.currentTimeMillis().toString() + ".tmp")
        try {
            // read bzip
            extractBZip(source, tmp)

            // read Tar
            extractTar(tmp, target)
        } finally {
            tmp.delete()
        }
    }

    @Throws(IOException::class)
    private fun extractBZip(source: Resource, target: Resource) {
        var `is`: InputStream? = null
        var os: OutputStream? = null
        try {
            `is` = BZip2CompressorInputStream(IOUtil.toBufferedInputStream(source.getInputStream()))
            os = IOUtil.toBufferedOutputStream(target.getOutputStream())
            IOUtil.copy(`is`, os, false, false)
        } finally {
            IOUtil.close(`is`, os)
        }
    }

    @Throws(IOException::class)
    private fun extractGZip(source: Resource, target: Resource) {
        var `is`: InputStream? = null
        var os: OutputStream? = null
        try {
            `is` = GZIPInputStream(IOUtil.toBufferedInputStream(source.getInputStream()))
            os = IOUtil.toBufferedOutputStream(target.getOutputStream())
            IOUtil.copy(`is`, os, false, false)
        } finally {
            IOUtil.close(`is`, os)
        }
    }

    /**
     * extract a zip file to a directory
     *
     * @param format
     * @param sources
     * @param target
     * @throws IOException
     */
    @Throws(IOException::class)
    fun extract(format: Int, sources: Array<Resource?>, target: Resource?) {
        for (i in sources.indices) {
            extract(format, sources[i], target)
        }
    }

    @Throws(IOException::class)
    private fun extractTar(tarFile: Resource, targetDir: Resource) {
        if (!targetDir.exists() || !targetDir.isDirectory()) throw IOException("[$targetDir] is not an existing directory")
        if (!tarFile.exists()) throw IOException("[$tarFile] is not an existing file")
        if (tarFile.isDirectory()) {
            val files: Array<Resource> = tarFile.listResources(ExtensionResourceFilter("tar"))
                    ?: throw IOException("directory [$tarFile] is empty")
            extract(FORMAT_TAR, files, targetDir)
            return
        }

        // read the zip file and build a query from its contents
        var tis: TarArchiveInputStream? = null
        try {
            tis = TarArchiveInputStream(IOUtil.toBufferedInputStream(tarFile.getInputStream()))
            var entry: TarArchiveEntry
            var mode: Int
            while (tis.getNextTarEntry().also { entry = it } != null) {
                // print.ln(entry);
                val target: Resource = targetDir.getRealResource(entry.getName())
                if (entry.isDirectory()) {
                    target.mkdirs()
                } else {
                    val parent: Resource = target.getParentResource()
                    if (!parent.exists()) parent.mkdirs()
                    IOUtil.copy(tis, target, false)
                }
                target.setLastModified(entry.getModTime().getTime())
                mode = entry.getMode()
                if (mode > 0) target.setMode(mode)
                // tis.closeEntry() ;
            }
        } finally {
            IOUtil.close(tis)
        }
    }

    @Throws(IOException::class)
    private fun extractZip(zipFile: Resource, targetDir: Resource) {
        if (!targetDir.exists() || !targetDir.isDirectory()) throw IOException("[$targetDir] is not an existing directory")
        if (!zipFile.exists()) throw IOException("[$zipFile] is not an existing file")
        if (zipFile.isDirectory()) {
            val files: Array<Resource> = zipFile.listResources(OrResourceFilter(arrayOf<ResourceFilter>(ExtensionResourceFilter("zip"), ExtensionResourceFilter("jar"),
                    ExtensionResourceFilter("war"), ExtensionResourceFilter("tar"), ExtensionResourceFilter("ear"))))
                    ?: throw IOException("directory [$zipFile] is empty")
            extract(FORMAT_ZIP, files, targetDir)
            return
        }

        // read the zip file and build a query from its contents
        unzip(zipFile, targetDir)
        /*
		 * ZipInputStream zis=null; try { zis = new ZipInputStream(
		 * IOUtil.toBufferedInputStream(zipFile.getInputStream()) ) ; ZipEntry entry; while ( ( entry =
		 * zis.getNextEntry()) != null ) { Resource target=targetDir.getRealResource(entry.getName());
		 * if(entry.isDirectory()) { target.mkdirs(); } else { Resource parent=target.getParentResource();
		 * if(!parent.exists())parent.mkdirs();
		 * 
		 * IOUtil.copy(zis,target,false); } target.setLastModified(entry.getTime()); zis.closeEntry() ; } }
		 * finally { IOUtil.closeEL(zis); }
		 */
    }

    @Throws(IOException::class)
    private fun unzip(zipFile: Resource, targetDir: Resource) {
        /*
		 * if(zipFile instanceof File){ unzip((File)zipFile, targetDir); return; }
		 */
        var zis: ZipInputStream? = null
        try {
            zis = ZipInputStream(IOUtil.toBufferedInputStream(zipFile.getInputStream()))
            var entry: ZipEntry
            while (zis.getNextEntry().also { entry = it } != null) {
                val target: Resource = ZipUtil.toResource(targetDir, entry)
                if (entry.isDirectory()) {
                    target.mkdirs()
                } else {
                    val parent: Resource = target.getParentResource()
                    if (!parent.exists()) parent.mkdirs()
                    if (!target.exists()) IOUtil.copy(zis, target, false)
                }
                target.setLastModified(entry.getTime())
                zis.closeEntry()
            }
        } finally {
            IOUtil.close(zis)
        }
    }

    /*
	 * private static void listZipp(Resource zipFile) throws IOException { if (!zipFile.exists()) throw
	 * new IOException(zipFile + " is not an existing file");
	 * 
	 * if (zipFile.isDirectory()) { throw new IOException(zipFile + " is a directory"); }
	 * 
	 * ZipInputStream zis = null; try { zis = new
	 * ZipInputStream(IOUtil.toBufferedInputStream(zipFile.getInputStream())); ZipEntry entry; while
	 * ((entry = zis.getNextEntry()) != null) { if (!entry.isDirectory()) { ByteArrayOutputStream baos =
	 * new ByteArrayOutputStream(); IOUtil.copy(zis, baos, false, false); byte[] barr =
	 * baos.toByteArray(); ap rint.o(entry.getName() + ":" + barr.length); } } } finally {
	 * IOUtil.closeEL(zis); } }
	 */
    @Throws(IOException::class)
    private fun unzip2(zipFile: File, targetDir: Resource) {
        var zf: ZipFile? = null
        try {
            zf = ZipFile(zipFile)
            var entry: ZipEntry
            val en: Enumeration = zf.entries()
            while (en.hasMoreElements()) {
                entry = en.nextElement() as ZipEntry
                val target: Resource = ZipUtil.toResource(targetDir, entry)
                if (entry.isDirectory()) {
                    target.mkdirs()
                } else {
                    val parent: Resource = target.getParentResource()
                    if (!parent.exists()) parent.mkdirs()
                    val `is`: InputStream = zf.getInputStream(entry)
                    if (!target.exists()) IOUtil.copy(`is`, target, true)
                }
                target.setLastModified(entry.getTime())
            }
        } finally {
            IOUtil.closeEL(zf)
        }
    }

    /**
     * compress data to a zip file
     *
     * @param format format it that should by compressed usually is CompressUtil.FORMAT_XYZ
     * @param source
     * @param target
     * @param includeBaseFolder
     * @param mode
     * @throws IOException
     */
    @Throws(IOException::class)
    fun compress(format: Int, source: Resource, target: Resource, includeBaseFolder: Boolean, mode: Int) {
        if (format == FORMAT_GZIP) compressGZip(source, target) else if (format == FORMAT_BZIP2) compressBZip2(source, target) else {
            val sources: Array<Resource?> = if (!includeBaseFolder && source.isDirectory()) source.listResources() else arrayOf<Resource>(source)
            compress(format, sources, target, mode)
        }
    }

    /**
     * compress data to a zip file
     *
     * @param format format it that should by compressed usually is CompressUtil.FORMAT_XYZ
     * @param sources
     * @param target
     * @param mode
     * @throws IOException
     */
    @Throws(IOException::class)
    fun compress(format: Int, sources: Array<Resource?>?, target: Resource, mode: Int) {
        if (format == FORMAT_ZIP) compressZip(sources, target, null) else if (format == FORMAT_TAR) compressTar(sources, target, mode) else if (format == FORMAT_TGZ) compressTGZ(sources, target, mode) else if (format == FORMAT_TBZ2) compressTBZ2(sources, target, mode) else throw IOException("Can't compress in given format")
    }

    /**
     * compress a source file/directory to a tar/gzip file
     *
     * @param sources
     * @param target
     * @param mode
     * @throws IOException
     */
    @Throws(IOException::class)
    fun compressTGZ(sources: Array<Resource?>?, target: Resource, mode: Int) {
        val tmpTarget: File = File.createTempFile("_temp", "tmp")
        try {
            // write Tar
            val tmpOs: OutputStream = FileOutputStream(tmpTarget)
            try {
                compressTar(sources, tmpOs, mode)
            } finally {
                IOUtil.close(tmpOs)
            }

            // write Gzip
            var `is`: InputStream? = null
            var os: OutputStream? = null
            try {
                `is` = FileInputStream(tmpTarget)
                os = target.getOutputStream()
                compressGZip(`is`, os)
            } finally {
                IOUtil.close(`is`, os)
            }
        } finally {
            tmpTarget.delete()
        }
    }

    /**
     * compress a source file/directory to a tar/bzip2 file
     *
     * @param sources
     * @param target
     * @param mode
     * @throws IOException
     */
    @Throws(IOException::class)
    private fun compressTBZ2(sources: Array<Resource?>?, target: Resource, mode: Int) {
        // File tmpTarget = File.createTempFile("_temp","tmp");
        val baos = ByteArrayOutputStream()
        compressTar(sources, baos, mode)
        _compressBZip2(ByteArrayInputStream(baos.toByteArray()), target.getOutputStream())
        // tmpTarget.delete();
    }

    /**
     * compress a source file to a gzip file
     *
     * @param source
     * @param target
     * @throws IOException
     * @throws IOException
     */
    @Throws(IOException::class)
    private fun compressGZip(source: Resource, target: Resource) {
        if (source.isDirectory()) {
            throw IOException("You can only create a GZIP File from a single source file, use TGZ (TAR-GZIP) to first TAR multiple files")
        }
        var `is`: InputStream? = null
        var os: OutputStream? = null
        try {
            `is` = source.getInputStream()
            os = target.getOutputStream()
        } catch (ioe: IOException) {
            IOUtil.close(`is`, os)
            throw ioe
        }
        compressGZip(`is`, os)
    }

    @Throws(IOException::class)
    fun compressGZip(source: InputStream?, target: OutputStream?) {
        var target: OutputStream? = target
        val `is`: InputStream = IOUtil.toBufferedInputStream(source)
        if (target !is GZIPOutputStream) target = GZIPOutputStream(IOUtil.toBufferedOutputStream(target))
        IOUtil.copy(`is`, target, true, true)
    }

    /**
     * compress a source file to a bzip2 file
     *
     * @param source
     * @param target
     * @throws IOException
     */
    @Throws(IOException::class)
    private fun compressBZip2(source: Resource, target: Resource) {
        if (source.isDirectory()) {
            throw IOException("You can only create a BZIP File from a single source file, use TBZ (TAR-BZIP2) to first TAR multiple files")
        }
        var `is`: InputStream? = null
        var os: OutputStream? = null
        try {
            `is` = source.getInputStream()
            os = target.getOutputStream()
        } catch (ioe: IOException) {
            IOUtil.close(`is`, os)
            throw ioe
        }
        _compressBZip2(`is`, os)
    }

    /**
     * compress a source file to a bzip2 file
     *
     * @param source
     * @param target
     * @throws IOException
     */
    @Throws(IOException::class)
    private fun _compressBZip2(source: InputStream?, target: OutputStream?) {
        val `is`: InputStream = IOUtil.toBufferedInputStream(source)
        val os: OutputStream = BZip2CompressorOutputStream(IOUtil.toBufferedOutputStream(target))
        IOUtil.copy(`is`, os, true, true)
    }

    /**
     * compress a source file/directory to a zip file
     *
     * @param sources
     * @param target
     * @param filter
     * @throws IOException
     */
    @Throws(IOException::class)
    fun compressZip(sources: Array<Resource?>?, target: Resource, filter: ResourceFilter?) {
        var zos: ZipOutputStream? = null
        try {
            zos = ZipOutputStream(IOUtil.toBufferedOutputStream(target.getOutputStream()))
            compressZip("", sources, zos, filter)
        } finally {
            IOUtil.close(zos)
        }
    }

    @Throws(IOException::class)
    fun compressZip(sources: Array<Resource?>?, zos: ZipOutputStream?, filter: ResourceFilter?) {
        compressZip("", sources, zos, filter)
    }

    @Throws(IOException::class)
    private fun compressZip(parent: String, sources: Array<Resource>?, zos: ZipOutputStream, filter: ResourceFilter) {
        var parent = parent
        if (parent.length() > 0) parent += "/"
        if (sources != null) {
            for (i in sources.indices) {
                compressZip(parent + sources[i].getName(), sources[i], zos, filter)
            }
        }
    }

    @Throws(IOException::class)
    private fun compressZip(parent: String, source: Resource, zos: ZipOutputStream, filter: ResourceFilter?) {
        if (source.isFile()) {
            // if(filter.accept(source)) {
            val ze = ZipEntry(parent)
            ze.setTime(source.lastModified())
            zos.putNextEntry(ze)
            try {
                IOUtil.copy(source, zos, false)
            } finally {
                zos.closeEntry()
            }
            // }
        } else if (source.isDirectory()) {
            if (!StringUtil.isEmpty(parent)) {
                val ze = ZipEntry("$parent/")
                ze.setTime(source.lastModified())
                try {
                    zos.putNextEntry(ze)
                } catch (ioe: IOException) {
                    if (Caster.toString(ioe.getMessage()).indexOf("duplicate") === -1) throw ioe
                }
                zos.closeEntry()
            }
            compressZip(parent, if (filter == null) source.listResources() else source.listResources(filter), zos, filter)
        }
    }

    /**
     * compress a source file/directory to a tar file
     *
     * @param sources
     * @param target
     * @param mode
     * @throws IOException
     */
    @Throws(IOException::class)
    fun compressTar(sources: Array<Resource?>?, target: Resource, mode: Int) {
        compressTar(sources, IOUtil.toBufferedOutputStream(target.getOutputStream()), mode)
    }

    @Throws(IOException::class)
    fun compressTar(sources: Array<Resource?>?, target: OutputStream?, mode: Int) {
        if (target is TarArchiveOutputStream) {
            compressTar("", sources, target as TarArchiveOutputStream?, mode)
            return
        }
        val tos = TarArchiveOutputStream(target)
        tos.setLongFileMode(TarArchiveOutputStream.LONGFILE_GNU)
        try {
            compressTar("", sources, tos, mode)
        } finally {
            IOUtil.close(tos)
        }
    }

    @Throws(IOException::class)
    fun compressTar(parent: String, sources: Array<Resource>?, tos: TarArchiveOutputStream?, mode: Int) {
        var parent = parent
        if (parent.length() > 0) parent += "/"
        if (sources != null) {
            for (i in sources.indices) {
                compressTar(parent + sources[i].getName(), sources[i], tos, mode)
            }
        }
    }

    @Throws(IOException::class)
    private fun compressTar(parent: String, source: Resource, tos: TarArchiveOutputStream, mode: Int) {
        var mode = mode
        if (source.isFile()) {
            // TarEntry entry = (source instanceof FileResource)?new TarEntry((FileResource)source):new
            // TarEntry(parent);
            val entry = TarArchiveEntry(parent)
            entry.setName(parent)

            // mode
            // 100777 TODO ist das so ok?
            if (mode > 0) entry.setMode(mode) else if (source.getMode().also { mode = it } > 0) entry.setMode(mode)
            entry.setSize(source.length())
            entry.setModTime(source.lastModified())
            tos.putArchiveEntry(entry)
            try {
                IOUtil.copy(source, tos, false)
            } finally {
                tos.closeArchiveEntry()
            }
        } else if (source.isDirectory()) {
            compressTar(parent, source.listResources(), tos, mode)
        }
    }

    @Throws(IOException::class)
    fun merge(sources: Array<Resource>, target: Resource) {
        var entry: ZipEntry
        var zis: ZipInputStream? = null
        var zos: ZipOutputStream? = null
        val done: Set<String> = HashSet()
        try {
            zos = ZipOutputStream(IOUtil.toBufferedOutputStream(target.getOutputStream()))
            for (r in sources) {
                try {
                    zis = ZipInputStream(IOUtil.toBufferedInputStream(r.getInputStream()))
                    while (zis.getNextEntry().also { entry = it } != null) {
                        if (!done.contains(entry.getName())) {
                            zos.putNextEntry(entry)
                            IOUtil.copy(zis, zos, false, false)
                            done.add(entry.getName())
                        }
                        zos.closeEntry()
                    }
                } finally {
                    IOUtil.close(zis)
                }
            }
        } finally {
            IOUtil.close(zos)
        }
    }

    @Throws(IOException::class)
    fun main(args: Array<String?>?) {
        val frp: ResourceProvider = ResourcesImpl.getFileResourceProvider()
        val sources: Array<Resource> = arrayOf<Resource>(frp.getResource("/Users/mic/Downloads/aws-java-sdk-core-1.12.153.jar"),
                frp.getResource("/Users/mic/Downloads/aws-java-sdk-kms-1.12.153.jar"), frp.getResource("/Users/mic/Downloads/aws-java-sdk-s3-1.12.153.jar"),
                frp.getResource("/Users/mic/Downloads/jmespath-java-1.12.153.jar"))
        merge(sources, frp.getResource("/Users/mic/Downloads/aws-java-sdk-s3-all-1.12.153.jar"))

        /*
		 * 
		 * Resource src = frp.getResource("/Users/mic/temp/a");
		 * 
		 * Resource tgz = frp.getResource("/Users/mic/temp/b/a.tgz"); tgz.getParentResource().mkdirs();
		 * Resource tar = frp.getResource("/Users/mic/temp/b/a.tar"); tar.getParentResource().mkdirs();
		 * Resource zip = frp.getResource("/Users/mic/temp/b/a.zip"); zip.getParentResource().mkdirs();
		 * 
		 * Resource tgz1 = frp.getResource("/Users/mic/temp/b/tgz"); tgz1.mkdirs(); Resource tar1 =
		 * frp.getResource("/Users/mic/temp/b/tar"); tar1.mkdirs(); Resource zip1 =
		 * frp.getResource("/Users/mic/temp/b/zip"); zip1.mkdirs();
		 * 
		 * compressTGZ(new Resource[] { src }, tgz, -1); compressTar(new Resource[] { src }, tar, -1);
		 * compressZip(new Resource[] { src }, zip, null);
		 * 
		 * extractTGZ(tgz, tgz1); extractTar(tar, tar1); extractZip(src, zip1);
		 */
    }
}