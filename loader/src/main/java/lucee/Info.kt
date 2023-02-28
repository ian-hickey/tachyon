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
package lucee

import kotlin.Throws
import lucee.loader.util.Util
import kotlin.jvm.Synchronized
import kotlin.jvm.Transient
import org.osgi.framework.Version

/**
 * Info to this Version
 */
interface Info {
    /**
     * @return the level
     */
    fun getLevel(): String?

    /**
     * @return Returns the releaseTime.
     */
    fun getRealeaseTime(): Long

    /**
     * @return Returns the version.
     */
    fun getVersion(): Version?
    /**
     * @return returns the state
     */
    // public int getStateAsInt();
    /**
     * @return returns the state
     */
    // public String getStateAsString();
    fun getFullVersionInfo(): Long
    fun getVersionName(): String?
    fun getVersionNameExplanation(): String?
    fun getCFMLTemplateExtensions(): Array<String?>?
    fun getLuceeTemplateExtensions(): Array<String?>?

    @Deprecated
    fun getCFMLComponentExtensions(): Array<String?>?

    @Deprecated
    fun getLuceeComponentExtensions(): Array<String?>?
    fun getCFMLComponentExtension(): String?
    fun getLuceeComponentExtension(): String?

    companion object {
        const val STATE_ALPHA = 2 * 100000000
        const val STATE_BETA = 1 * 100000000
        const val STATE_RC = 3 * 100000000
        const val STATE_FINAL = 0
    }
}