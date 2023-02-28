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
package tachyon.runtime.cfx.customtag

import com.allaire.cfx.CustomTag

/**
 * interface for a CustomTag Class, a CustomTag Class is Base to generate a Custom Tag
 */
interface CFXTagClass {
    /**
     * @return return a New Instance
     * @throws CFXTagException CFX Tag Exception
     */
    @Throws(CFXTagException::class)
    fun newInstance(): CustomTag?

    /**
     * @return returns if Tag is readOnly (for Admin)
     */
    val isReadOnly: Boolean

    /**
     * @return returns a readonly copy of the tag
     */
    fun cloneReadOnly(): CFXTagClass?

    /**
     * @return returns Type of the CFX Tag as String
     */
    val displayType: String?

    /**
     * @return returns the Source Name as String
     */
    val sourceName: String?

    /**
     * @return returns if tag is ok
     */
    val isValid: Boolean
}