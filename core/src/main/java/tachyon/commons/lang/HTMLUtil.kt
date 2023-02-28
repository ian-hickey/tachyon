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
package tachyon.commons.lang

import java.net.MalformedURLException

/**
 * HTML Util class
 *
 */
class HTMLUtil {
    private val tags = arrayOf<Tag>(Tag("a", "href"), Tag("link", "href"), Tag("form", "action"), Tag("applet", "code"), Tag("script", "src"),
            Tag("body", "background"), Tag("frame", "src"), Tag("bgsound", "src"), Tag("img", "src"),
            Tag("embed", arrayOf<String>("src", "pluginspace")), Tag("object", arrayOf<String>("data", "classid", "codebase", "usemap"))
    )

    /**
     * returns all urls in a html String
     *
     * @param html HTML String to search urls
     * @param url Absolute URL path to set
     * @return urls found in html String
     */
    fun getURLS(html: String?, url: URL): List<URL> {
        val urls: List<URL> = ArrayList<URL>()
        val cfml = SourceCode(null, html, false, CFMLEngine.DIALECT_CFML)
        while (!cfml.isAfterLast()) {
            if (cfml.forwardIfCurrent('<')) {
                for (i in tags.indices) {
                    if (cfml.forwardIfCurrent(tags[i].tag + " ")) {
                        getSingleUrl(urls, cfml, tags[i], url)
                    }
                }
            } else {
                cfml.next()
            }
        }
        return urls
    }

    /**
     * transform a single tag
     *
     * @param urls all urls founded
     * @param cfml CFMl String Object containing plain HTML
     * @param tag current tag totransform
     * @param url absolute URL to Set at tag attribute
     */
    private fun getSingleUrl(urls: List<URL>, cfml: SourceCode, tag: Tag, url: URL) {
        var quote = 0.toChar()
        var inside = false
        var value = StringBuilder()
        while (!cfml.isAfterLast()) {
            if (inside) {
                if (quote.toInt() != 0 && cfml.forwardIfCurrent(quote)) {
                    inside = false
                    add(urls, url, value.toString())
                } else if (quote.toInt() == 0 && (cfml.isCurrent(' ') || cfml.isCurrent("/>") || cfml.isCurrent('>') || cfml.isCurrent('\t') || cfml.isCurrent('\n'))) {
                    inside = false
                    try {
                        urls.add(URL(url, value.toString()))
                    } catch (e: MalformedURLException) {
                    }
                    cfml.next()
                } else {
                    value.append(cfml.getCurrent())
                    cfml.next()
                }
            } else if (cfml.forwardIfCurrent('>')) {
                break
            } else {
                for (i in tag.attributes.indices) {
                    if (cfml.forwardIfCurrent(tag.attributes[i])) {
                        cfml.removeSpace()
                        // =
                        if (cfml.isCurrent('=')) {
                            inside = true
                            cfml.next()
                            cfml.removeSpace()
                            quote = cfml.getCurrent()
                            value = StringBuilder()
                            if (quote != '"' && quote != '\'') quote = 0.toChar() else {
                                cfml.next()
                            }
                        }
                    }
                }
                if (!inside) {
                    cfml.next()
                }
            }
        }
    }

    private fun add(list: List<URL>, baseURL: URL, value: String) {
        var value = value
        value = value.trim()
        val lcValue: String = value.toLowerCase()
        try {
            if (lcValue.startsWith("http://") || lcValue.startsWith("news://") || lcValue.startsWith("goopher://") || lcValue.startsWith("javascript:")) list.add(HTTPUtil.toURL(value, HTTPUtil.ENCODED_AUTO)) else {
                list.add(URL(baseURL, value))
            }
        } catch (mue: MalformedURLException) {
        }
        // print.err(list.get(list.size()-1));
    }

    private inner class Tag {
        var tag: String
        var attributes: Array<String?>

        private constructor(tag: String, attributes: Array<String>) {
            this.tag = tag.toLowerCase()
            this.attributes = arrayOfNulls(attributes.size)
            for (i in attributes.indices) {
                this.attributes[i] = attributes[i].toLowerCase()
            }
        }

        private constructor(tag: String, attribute1: String) {
            this.tag = tag.toLowerCase()
            attributes = arrayOf(attribute1.toLowerCase())
        }
    }
}