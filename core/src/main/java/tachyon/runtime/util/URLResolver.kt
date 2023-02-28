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
package tachyon.runtime.util

import java.net.MalformedURLException

/**
 * Transform a HTML String, set all relative Pathes inside HTML File to absolute TODO Test this
 *
 */
class URLResolver {
    private val tags: Array<Tag?>? = arrayOf(Tag("a", "href"), Tag("link", "href"), Tag("form", "action"), Tag("applet", "code"), Tag("script", "src"),
            Tag("body", "background"), Tag("frame", "src"), Tag("bgsound", "src"), Tag("img", "src"),
            Tag("embed", arrayOf<String?>("src", "pluginspace")), Tag("object", arrayOf<String?>("data", "classid", "codebase", "usemap"))
    )

    @Throws(MalformedURLException::class)
    fun transform(node: Node?, url: URL?) {
        val el: Element?
        if (node.getNodeType() === Node.DOCUMENT_NODE) {
            transform(XMLUtil.getRootElement(node, true), url)
        } else if (node.getNodeType() === Node.ELEMENT_NODE) {
            el = node as Element?
            var attr: Array<String?>?
            var map: NamedNodeMap
            var attrName: String
            var value: String
            var value2: String?
            val nodeName: String = el.getNodeName()
            var len: Int
            // translate attribute
            for (i in tags.indices) {
                if (tags!![i]!!.tag.equalsIgnoreCase(nodeName)) {
                    attr = tags[i]!!.attributes
                    map = el.getAttributes()
                    len = map.getLength()
                    for (y in attr.indices) {
                        for (z in 0 until len) {
                            attrName = map.item(z).getNodeName()
                            if (attrName.equalsIgnoreCase(attr!![y])) {
                                value = el.getAttribute(attrName)
                                value2 = add(url, value)
                                if (value !== value2) {
                                    el.setAttribute(attrName, value2)
                                }
                                break
                            }
                        }
                    }
                }
            }

            // list children
            val nodes: NodeList = el.getChildNodes()
            len = nodes.getLength()
            for (i in 0 until len) {
                transform(nodes.item(i), url)
            }
        }
    }

    /**
     * transform the HTML String
     *
     * @param html HTML String to transform
     * @param url Absolute URL path to set
     * @return transformed HTMl String
     * @throws PageException
     */
    @Throws(PageException::class)
    fun transform(html: String?, url: URL?, setBaseTag: Boolean): String? {
        var html = html
        val target = StringBuffer()
        val cfml = SourceCode(null, html, false, CFMLEngine.DIALECT_CFML)
        while (!cfml.isAfterLast()) {
            if (cfml.forwardIfCurrent('<')) {
                target.append('<')
                try {
                    for (i in tags.indices) {
                        if (cfml.forwardIfCurrent(tags!![i]!!.tag.toString() + " ")) {
                            target.append(tags[i]!!.tag.toString() + " ")
                            transformTag(target, cfml, tags[i], url)
                        }
                    }
                } catch (me: MalformedURLException) {
                    throw Caster.toPageException(me)
                }
            } else {
                target.append(cfml.getCurrent())
                cfml.next()
            }
        }
        if (!setBaseTag) return target.toString()
        html = target.toString()
        var prefix = ""
        var postfix = ""
        var index: Int = StringUtil.indexOfIgnoreCase(html, "</head>")
        if (index == -1) {
            prefix = "<head>"
            postfix = "</head>"
            index = StringUtil.indexOfIgnoreCase(html, "</html>")
        }
        if (index != -1) {
            val sb = StringBuffer()
            sb.append(html.substring(0, index))
            val port = if (url.getPort() === -1) "" else ":" + url.getPort()
            sb.append(prefix + "<base href=\"" + (url.getProtocol().toString() + "://" + url.getHost() + port) + "\">" + postfix)
            sb.append(html.substring(index))
            html = sb.toString()
        }
        return html
    }

    /**
     * transform a single tag
     *
     * @param target target to write to
     * @param cfml CFMl String Object containing plain HTML
     * @param tag current tag totransform
     * @param url absolute URL to Set at tag attribute
     * @throws MalformedURLException
     */
    @Throws(MalformedURLException::class)
    private fun transformTag(target: StringBuffer?, cfml: SourceCode?, tag: Tag?, url: URL?) {
        // TODO attribute inside other attribute
        var quote = 0.toChar()
        var inside = false
        var value: StringBuffer? = StringBuffer()
        while (!cfml.isAfterLast()) {
            if (inside) {
                if (quote.toInt() != 0 && cfml.forwardIfCurrent(quote)) {
                    inside = false
                    target.append(add(url, value.toString()))
                    target.append(quote)
                } else if (quote.toInt() == 0 && (cfml.isCurrent(' ') || cfml.isCurrent("/>") || cfml.isCurrent('>') || cfml.isCurrent('\t') || cfml.isCurrent('\n'))) {
                    inside = false
                    target.append(URL(url, value.toString()))
                    target.append(cfml.getCurrent())
                    cfml.next()
                } else {
                    value.append(cfml.getCurrent())
                    cfml.next()
                }
            } else if (cfml.forwardIfCurrent('>')) {
                target.append('>')
                break
            } else {
                for (i in tag!!.attributes.indices) {
                    if (cfml.forwardIfCurrent(tag.attributes!![i])) {
                        target.append(tag.attributes!![i])
                        cfml.removeSpace()
                        // =
                        if (cfml.isCurrent('=')) {
                            inside = true
                            target.append('=')
                            cfml.next()
                            cfml.removeSpace()
                            quote = cfml.getCurrent()
                            value = StringBuffer()
                            if (quote != '"' && quote != '\'') quote = 0.toChar() else {
                                target.append(quote)
                                cfml.next()
                            }
                        }
                    }
                }
                if (!inside) {
                    target.append(cfml.getCurrent())
                    cfml.next()
                }
            }
        }
    }

    private fun add(url: URL?, value: String?): String? {
        var value = value
        value = value.trim()
        val lcValue: String = value.toLowerCase()
        return if (lcValue.startsWith("http://") || lcValue.startsWith("file://") || lcValue.startsWith("news://") || lcValue.startsWith("goopher://")
                || lcValue.startsWith("javascript:")) value else try {
            URL(url, value.toString()).toExternalForm()
        } catch (e: MalformedURLException) {
            value
        }
    }

    private inner class Tag {
        var tag: String?
        var attributes: Array<String?>?

        private constructor(tag: String?, attributes: Array<String?>?) {
            this.tag = tag.toLowerCase()
            this.attributes = arrayOfNulls<String?>(attributes!!.size)
            for (i in attributes.indices) {
                this.attributes!![i] = attributes[i].toLowerCase()
            }
        }

        private constructor(tag: String?, attribute1: String?) {
            this.tag = tag.toLowerCase()
            attributes = arrayOf(attribute1.toLowerCase())
        }
    }

    companion object {
        fun getInstance(): URLResolver? {
            return URLResolver()
        }
    }
}