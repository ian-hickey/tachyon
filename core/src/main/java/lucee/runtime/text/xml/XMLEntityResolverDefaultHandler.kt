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
package lucee.runtime.text.xml

import org.xml.sax.InputSource

class XMLEntityResolverDefaultHandler(entity: InputSource?) : DefaultHandler() {
    private val entity: InputSource?
    @Override
    @Throws(SAXException::class)
    fun resolveEntity(publicID: String?, systemID: String?): InputSource? {
        return if (entity != null) entity else try {
            // TODO user resources
            InputSource(IOUtil.toBufferedInputStream(HTTPUtil.toURL(systemID, HTTPUtil.ENCODED_AUTO).openStream()))
        } catch (t: Throwable) {
            ExceptionUtil.rethrowIfNecessary(t)
            null
        }
    }

    init {
        this.entity = entity
    }
}