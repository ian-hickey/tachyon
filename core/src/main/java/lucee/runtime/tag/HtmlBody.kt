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
package lucee.runtime.tag

import java.io.IOException

class HtmlBody : HtmlHeadBodyBase() {
    @get:Override
    override val tagName: String?
        get() = "htmlbody"

    @get:Override
    override val defaultForce: Boolean
        get() = false

    @Override
    @Throws(IOException::class, ApplicationException::class)
    override fun actionAppend() {
        (pageContext as PageContextImpl?).getRootOut().appendHTMLBody(text)
    }

    @Override
    @Throws(IOException::class, ApplicationException::class)
    override fun actionWrite() {
        (pageContext as PageContextImpl?).getRootOut().writeHTMLBody(text)
    }

    @Override
    @Throws(IOException::class)
    override fun actionReset() {
        (pageContext as PageContextImpl?).getRootOut().resetHTMLBody()
    }

    @Override
    @Throws(PageException::class, IOException::class)
    override fun actionRead() {
        val str: String = (pageContext as PageContextImpl?).getRootOut().getHTMLBody()
        pageContext.setVariable(if (variable != null) variable else "cfhtmlbody", str)
    }

    @Override
    @Throws(IOException::class)
    override fun actionFlush() {
        val pci: PageContextImpl? = pageContext as PageContextImpl?
        pci.write(pci.getRootOut().getHTMLBody())
        pci.getRootOut().resetHTMLBody()
    }
}