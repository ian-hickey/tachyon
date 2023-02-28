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
package tachyon.runtime.ext.tag

import tachyon.runtime.type.Collection

/**
 * Interface for Dynamic Attributes for tags (in j2ee at version 1.4.x)
 */
interface DynamicAttributes {
    /**
     * @param uri the namespace of the attribute, or null if in the default namespace.
     * @param localName the name of the attribute being set.
     * @param value the value of the attribute
     */
    @Deprecated
    @Deprecated("""use instead
	              <code>setDynamicAttribute(String uri, Collection.Key localName, Object value)</code>""")
    fun setDynamicAttribute(uri: String?, localName: String?, value: Object?)

    /**
     * @param uri the namespace of the attribute, or null if in the default namespace.
     * @param localName the name of the attribute being set.
     * @param value the value of the attribute
     */
    fun setDynamicAttribute(uri: String?, localName: Collection.Key?, value: Object?)
}