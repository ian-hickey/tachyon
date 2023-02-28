/**
 * Copyright (c) 2014, the Railo Company Ltd.
 * Copyright (c) 2015, Tachyon Assosication Switzerland
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
package tachyon.runtime

import java.io.IOException

/**
 * extends the source file with class features
 */
interface PageSource : Serializable {
    /**
     * loads a page
     *
     * @param pc page context
     * @param forceReload force reload
     * @return page source
     * @throws PageException throws an exception when compilation fails or page does not exist
     */
    @Throws(PageException::class)
    fun loadPage(pc: PageContext?, forceReload: Boolean): Page?

    /**
     * loads a page
     *
     * @param pc page context
     * @param forceReload force reload
     * @param defaultValue default value
     * @return page source
     * @throws PageException throws an exception when compilation fails
     */
    @Throws(PageException::class)
    fun loadPageThrowTemplateException(pc: PageContext?, forceReload: Boolean, defaultValue: Page?): Page?

    /**
     * loads a page
     *
     * @param pc page context
     * @param forceReload force reload
     * @param defaultValue default value
     * @return page source
     */
    fun loadPage(pc: PageContext?, forceReload: Boolean, defaultValue: Page?): Page?

    /**
     * returns the realpath without the mapping
     *
     * @return Returns the realpath.
     */
    fun getRealpath(): String?

    /**
     * Returns the full name (mapping/realpath).
     *
     * @return mapping/realpath
     */
    fun getRealpathWithVirtual(): String?

    /**
     * @return return the file name of the source file (test.cfm)
     */
    fun getFileName(): String?

    /**
     * if the pageSource is based on an archive, Tachyon returns the ra:// path if the mapping physical
     * path and archive is invalid or not defined, it is possible this method returns null
     *
     * @return return the Resource matching this PageSource
     */
    fun getResource(): Resource?

    /**
     * if the pageSource is based on an archive, translate the source to a zip:// Resource
     *
     * @return return the Resource matching this PageSource
     * @param pc the Page Context Object
     * @throws PageException Page Exception
     */
    @Throws(PageException::class)
    fun getResourceTranslated(pc: PageContext?): Resource?

    /**
     * @return returns the full classname (package and name) matching to filename (Example:
     * my.package.test_cfm)
     */
    fun getClassName(): String?
    fun getJavaName(): String

    /**
     * @return returns the a package matching to file (Example: tachyon.web)
     */
    fun getComponentName(): String?

    /**
     * @return returns mapping where PageSource based on
     */
    fun getMapping(): Mapping

    /**
     * @return returns if page source exists or not
     */
    fun exists(): Boolean

    /**
     * @return returns if the physical part of the source file exists
     */
    fun physcalExists(): Boolean

    /**
     * @return return the source of the file as String array
     * @throws IOException IO Exception
     */
    @Throws(IOException::class)
    fun getSource(): Array<String?>?

    /**
     * get an new Pagesource from realpath
     *
     * @param realPath path
     * @return new Pagesource
     */
    fun getRealPage(realPath: String?): PageSource?

    /**
     * sets time last accessed page
     *
     * @param lastAccess time ast accessed
     */
    fun setLastAccessTime(lastAccess: Long)

    /**
     *
     * @return returns time last accessed page
     */
    fun getLastAccessTime(): Long

    /**
     * set time last accessed (now)
     */
    fun setLastAccessTime()

    /**
     * @return returns how many this page is accessed since server is in use.
     */
    fun getAccessCount(): Int

    /**
     * return file object, based on physical path and realpath
     *
     * @return file Object
     */
    fun getPhyscalFile(): Resource?

    /**
     * @return return source path as String
     */
    fun getDisplayPath(): String?
    fun getDialect(): Int

    /**
     * returns true if the page source can be executed, means the source exists or is trusted and loaded
     *
     * @return is the page source can be executed
     */
    fun executable(): Boolean
}