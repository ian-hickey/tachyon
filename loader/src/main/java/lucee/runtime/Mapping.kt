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
package lucee.runtime

import java.io.IOException

/**
 * interface of the mapping definition
 */
interface Mapping : Serializable {
    @Throws(ClassNotFoundException::class)
    fun getArchiveClass(className: String?): Class<*>?
    fun getArchiveClass(className: String?, defaultValue: Class<*>?): Class<*>?
    fun getArchiveResourceAsStream(string: String?): InputStream?

    @Throws(ClassNotFoundException::class, IOException::class)
    fun getPhysicalClass(className: String?): Class<*>?

    @Throws(IOException::class)
    fun getPhysicalClass(className: String?, code: ByteArray?): Class<*>?

    /**
     * @return Returns the physical.
     */
    fun getPhysical(): Resource?

    /**
     * @return Returns the virtual lower case.
     */
    fun getVirtualLowerCase(): String?

    /**
     * @return Returns the virtual lower case with slash at the end.
     */
    fun getVirtualLowerCaseWithSlash(): String?

    /**
     * @return return the archive file
     */
    fun getArchive(): Resource?

    /**
     * @return returns if mapping has an archive
     */
    fun hasArchive(): Boolean

    /**
     * @return return if mapping has a physical path
     */
    fun hasPhysical(): Boolean

    /**
     * @return class root directory
     */
    fun getClassRootDirectory(): Resource?

    /**
     * pagesource matching given realpath
     *
     * @param realPath path
     * @return matching pagesource
     */
    fun getPageSource(realPath: String?): PageSource?

    /**
     * @param path path
     * @param isOut is out
     * @return matching pagesource
     */
    fun getPageSource(path: String?, isOut: Boolean): PageSource?

    /**
     * checks the mapping
     */
    fun check()

    /**
     * @return Returns the hidden.
     */
    fun isHidden(): Boolean

    /**
     * @return Returns the physicalFirst.
     */
    fun isPhysicalFirst(): Boolean

    /**
     * @return Returns the readonly.
     */
    fun isReadonly(): Boolean

    /**
     * @return Returns the strArchive.
     */
    fun getStrArchive(): String?

    /**
     * @return Returns the strPhysical.
     */
    fun getStrPhysical(): String?

    /**
     * @return Returns the trusted.
     */
    @Deprecated
    @Deprecated("use instead <code>public short getInspectTemplate();</code>")
    fun isTrusted(): Boolean
    fun getInspectTemplate(): Short
    fun isTopLevel(): Boolean

    /**
     * @return Returns the virtual.
     */
    fun getVirtual(): String?

    /**
     * returns config of the mapping
     *
     * @return config
     */
    fun getConfig(): Config?

    /**
     * mapping can have a specific listener mode to overwrite the listener mode coming from the
     * Application Context
     *
     * @return Listener mode
     */
    fun getListenerMode(): Int

    /**
     * mapping can have a specific listener type to overwrite the listener mode coming from the
     * Application Context
     *
     * @return Listener type
     */
    fun getListenerType(): Int // public void flush(); FUTURE
}