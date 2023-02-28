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
package tachyon.runtime.tag

import java.io.IOException

/**
 * Writes the text specified in the text attribute to the 'head' section of a generated HTML page.
 * The cfhtmlhead tag can be useful for embedding CSS code, or placing other HTML tags such, as
 * META, LINK, TITLE, or BASE in an HTML page header.
 */
class HtmlHead : HtmlHeadBodyBase() {
    @get:Override
    override val tagName: String?
        get() = "htmlhead"

    @Override
    @Throws(IOException::class, ApplicationException::class)
    override fun actionAppend() {
        (pageContext as PageContextImpl?).getRootOut().appendHTMLHead(text)
    }

    @Override
    @Throws(IOException::class, ApplicationException::class)
    override fun actionWrite() {
        (pageContext as PageContextImpl?).getRootOut().writeHTMLHead(text)
    }

    @Override
    @Throws(IOException::class)
    override fun actionReset() {
        (pageContext as PageContextImpl?).getRootOut().resetHTMLHead()
    }

    @Override
    @Throws(PageException::class, IOException::class)
    override fun actionRead() {
        val str: String = (pageContext as PageContextImpl?).getRootOut().getHTMLHead()
        pageContext.setVariable(if (!StringUtil.isEmpty(variable)) variable else "cfhtmlhead", str)
    }

    @Override
    @Throws(IOException::class)
    override fun actionFlush() {
        val pci: PageContextImpl? = pageContext as PageContextImpl?
        pci.write(pci.getRootOut().getHTMLHead())
        pci.getRootOut().resetHTMLHead()
    }

    @get:Override
    override val defaultForce: Boolean
        get() = true
}