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
package lucee.runtime

import java.io.IOException

/**
 * abstract Method for all generated Page Object
 */
abstract class Page : Serializable {
    private var pageSource: PageSource? = null
    private var loadType: Byte = 0
    private var staticTextLocation: Resource? = null

    /**
     * return version definition of the page
     *
     * @return version
     */
    fun getVersion(): Long {
        return -1
    }

    /**
     * method to invoke a page
     *
     * @param pc PageContext
     * @throws Throwable throwable
     * @return null
     */
    @Throws(Throwable::class)
    fun call(pc: PageContext?): Object? {
        return null
    }

    /**
     * return when the source file last time was modified
     *
     * @return last modification of source file
     */
    fun getSourceLastModified(): Long {
        return 0
    }

    /**
     * return the time when the file was compiled
     *
     * @return compile time
     */
    fun getCompileTime(): Long {
        return 0
    }

    @Throws(IOException::class, PageException::class)
    fun str(pc: PageContext?, off: Int, len: Int): String {
        if (staticTextLocation == null) {
            val ps: PageSource? = getPageSource()
            val m: Mapping = ps!!.getMapping()
            staticTextLocation = m.getClassRootDirectory()
            staticTextLocation = staticTextLocation.getRealResource(ps!!.getJavaName().toString() + ".txt")
        }
        val e: CFMLEngine = CFMLEngineFactory.getInstance()
        val io: IO = e.getIOUtil()
        val reader: Reader = io.getReader(staticTextLocation, e.getCastUtil().toCharset("UTF-8"))
        val carr = CharArray(len)
        try {
            if (off > 0) reader.skip(off)
            reader.read(carr)
        } finally {
            io.closeSilent(reader)
        }

        // print.e(carr);
        return String(carr)
    }

    /**
     * @param pageSource page source
     */
    fun setPageSource(pageSource: PageSource?) {
        this.pageSource = pageSource
    }

    /**
     * @return Returns the pageResource.
     */
    fun getPageSource(): PageSource? {
        return pageSource
    }

    /**
     * @return gets the load type
     */
    fun getLoadType(): Byte {
        return loadType
    }

    /**
     * @param loadType sets the load type
     */
    fun setLoadType(loadType: Byte) {
        this.loadType = loadType
    }

    @Throws(Throwable::class)
    fun udfCall(pageContext: PageContext?, udf: UDF?, functionIndex: Int): Object? {
        return null
    }

    @Throws(Throwable::class)
    fun threadCall(pageContext: PageContext?, threadIndex: Int) {
    }

    fun udfDefaultValue(pc: PageContext?, functionIndex: Int, argumentIndex: Int, defaultValue: Object?): Object? {
        return null
    }

    fun getImportDefintions(): Array<ImportDefintion?> {
        return NO_IMPORTS
    }

    fun getSubPages(): Array<CIPage?> {
        return NO_SUB_PAGES
    }

    @Transient
    var metaData: SoftReference<Struct>? = null
    var udfs: Array<UDFProperties>

    companion object {
        private const val serialVersionUID = 7844636300784565040L
        private val NO_IMPORTS: Array<ImportDefintion?> = arrayOfNulls<ImportDefintion>(0)
        private val NO_SUB_PAGES: Array<CIPage?> = arrayOfNulls<CIPage>(0)
        var FALSE = false
        var TRUE = true
    }
}