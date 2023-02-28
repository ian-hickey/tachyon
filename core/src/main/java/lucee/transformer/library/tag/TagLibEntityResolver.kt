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
package lucee.transformer.library.tag

import org.xml.sax.EntityResolver

/**
 * Hilfsklasse fuer die TagLibFactory, diese Klasse definiert den DTDHandler fuer den Sax Parser.
 * Die Klasse laedt wenn moeglich die DTD, anhand der Public-id vom lokalen System.
 *
 * @see org.xml.sax.EntityResolver
 */
class TagLibEntityResolver : EntityResolver {
    /**
     * Laedt die DTD vom lokalen System.
     *
     * @see org.xml.sax.EntityResolver.resolveEntity
     */
    @Override
    fun resolveEntity(publicId: String?, systemId: String?): InputSource? {
        for (i in 0 until Constants.DTDS_TLD.length) {
            if (publicId!!.equals(Constants.DTDS_TLD.get(i))) {
                return InputSource(getClass().getResourceAsStream(LUCEE_DTD_1_0))
            }
        }
        if (publicId!!.equals("-//Sun Microsystems, Inc.//DTD JSP Tag Library 1.1//EN")) {
            return InputSource(getClass().getResourceAsStream(SUN_DTD_1_1))
        } else if (publicId.equals("-//Sun Microsystems, Inc.//DTD JSP Tag Library 1.2//EN")) {
            return InputSource(getClass().getResourceAsStream(SUN_DTD_1_2))
        }
        return null
    }

    companion object {
        /**
         * Definert den DTD welche eine TLD validieren kann
         */
        val LUCEE_DTD_1_0: String? = "/resource/dtd/web-cfmtaglibrary_1_0.dtd"
        val SUN_DTD_1_0: String? = "/resource/dtd/web-jsptaglibrary_1_0.dtd"
        val SUN_DTD_1_1: String? = "/resource/dtd/web-jsptaglibrary_1_1.dtd"
        val SUN_DTD_1_2: String? = "/resource/dtd/web-jsptaglibrary_1_2.dtd"
    }
}