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
package tachyon.runtime.text.feed

import java.util.HashMap

class FeedDeclaration private constructor(declaration: Map<String?, El?>?, entryLevel: String?, type: String?) {
    companion object {
        private val declarations: Map<String?, FeedDeclaration?>? = HashMap<String?, FeedDeclaration?>()
        private var defaultDeclaration: FeedDeclaration? = null
        fun getInstance(decName: String?): FeedDeclaration? {
            var fd = declarations!![decName]
            if (fd != null) return fd
            if (StringUtil.startsWithIgnoreCase(decName, "rss")) fd = declarations["rss"]
            if (fd != null) return fd
            if (StringUtil.startsWithIgnoreCase(decName, "atom")) fd = declarations["atom"]
            return fd ?: defaultDeclaration
        }

        init {

            // RSS 2.0
            val decl: Map<String?, El?> = HashMap<String?, El?>()
            tachyon.runtime.text.feed.decl.put("rss", El(El.QUANTITY_1, Attr("version")))
            // rss.channel *
            tachyon.runtime.text.feed.decl.put("rss.channel.item", El(El.QUANTITY_0_N, true))
            tachyon.runtime.text.feed.decl.put("rss.channel.category", El(El.QUANTITY_0_N, Attr("domain")))
            tachyon.runtime.text.feed.decl.put("rss.channel.cloud",
                    El(El.QUANTITY_AUTO, arrayOf<Attr?>(Attr("domain"), Attr("port"), Attr("path"), Attr("registerProcedure"), Attr("protocol"))))
            tachyon.runtime.text.feed.decl.put("rss.channel.image", El(El.QUANTITY_AUTO))

            // rss.channel.item *
            tachyon.runtime.text.feed.decl.put("rss.channel.item.author", El(El.QUANTITY_0_1))
            tachyon.runtime.text.feed.decl.put("rss.channel.item.category", El(El.QUANTITY_0_N, Attr("domain")))
            tachyon.runtime.text.feed.decl.put("rss.channel.item.comments", El(El.QUANTITY_0_1))
            tachyon.runtime.text.feed.decl.put("rss.channel.item.enclosure", El(El.QUANTITY_0_N, arrayOf<Attr?>(Attr("url"), Attr("length"), Attr("type"))))
            tachyon.runtime.text.feed.decl.put("rss.channel.item.guid", El(El.QUANTITY_0_1, Attr("isPermaLink", "true")))
            tachyon.runtime.text.feed.decl.put("rss.channel.item.pubDate", El(El.QUANTITY_0_1))
            tachyon.runtime.text.feed.decl.put("rss.channel.item.source", El(El.QUANTITY_0_1, Attr("url")))

            // rss.channel.item +
            tachyon.runtime.text.feed.decl.put("rss.channel.item.title", El(El.QUANTITY_1))
            tachyon.runtime.text.feed.decl.put("rss.channel.item.description", El(El.QUANTITY_1, arrayOf<Attr?>())) // muss auch sein das er value anlegt

            // rss.channel.item ?
            tachyon.runtime.text.feed.decl.put("rss.channel.item.link", El(El.QUANTITY_0_1)) // muss auch sein das er value anlegt

            // rss.channel.item 1
            tachyon.runtime.text.feed.decl.put("rss.channel.image.url", El(El.QUANTITY_1))
            tachyon.runtime.text.feed.decl.put("rss.channel.image.title", El(El.QUANTITY_1))
            tachyon.runtime.text.feed.decl.put("rss.channel.image.link", El(El.QUANTITY_1))

            // rss.channel.item *
            tachyon.runtime.text.feed.decl.put("rss.channel.image.width", El(El.QUANTITY_0_N))
            tachyon.runtime.text.feed.decl.put("rss.channel.image.height", El(El.QUANTITY_0_N))
            tachyon.runtime.text.feed.decl.put("rss.channel.image.description", El(El.QUANTITY_0_N))
            tachyon.runtime.text.feed.decl.put("rss.channel.textInput.title", El(El.QUANTITY_1))
            tachyon.runtime.text.feed.decl.put("rss.channel.textInput.description", El(El.QUANTITY_1))
            tachyon.runtime.text.feed.decl.put("rss.channel.textInput.name", El(El.QUANTITY_1))
            tachyon.runtime.text.feed.decl.put("rss.channel.textInput.link", El(El.QUANTITY_1))
            val fd = FeedDeclaration(tachyon.runtime.text.feed.decl, "rss.channel", "rss_2.0")
            declarations.put("rss", tachyon.runtime.text.feed.fd)
            declarations.put("rss_2.0", tachyon.runtime.text.feed.fd)
            declarations.put("rss_2", tachyon.runtime.text.feed.fd)

            // RSS 0.92
            tachyon.runtime.text.feed.decl = HashMap<String?, El?>()
            tachyon.runtime.text.feed.decl.put("rss", El(El.QUANTITY_1, Attr("version")))
            // rss.channel *
            tachyon.runtime.text.feed.decl.put("rss.channel.item", El(El.QUANTITY_0_N))
            tachyon.runtime.text.feed.decl.put("rss.channel.category", El(El.QUANTITY_0_N, Attr("domain")))
            tachyon.runtime.text.feed.decl.put("rss.channel.cloud",
                    El(El.QUANTITY_AUTO, arrayOf<Attr?>(Attr("domain"), Attr("port"), Attr("path"), Attr("registerProcedure"), Attr("protocol"))))
            tachyon.runtime.text.feed.decl.put("rss.channel.image", El(El.QUANTITY_AUTO))

            // rss.channel.item *
            tachyon.runtime.text.feed.decl.put("rss.channel.item.author", El(El.QUANTITY_0_1))
            tachyon.runtime.text.feed.decl.put("rss.channel.item.category", El(El.QUANTITY_0_N, Attr("domain", "leer")))
            tachyon.runtime.text.feed.decl.put("rss.channel.item.comments", El(El.QUANTITY_0_1))
            tachyon.runtime.text.feed.decl.put("rss.channel.item.enclosure", El(El.QUANTITY_0_N, arrayOf<Attr?>(Attr("url"), Attr("length"), Attr("type"))))
            tachyon.runtime.text.feed.decl.put("rss.channel.item.guid", El(El.QUANTITY_0_1, Attr("isPermaLink", "true")))
            tachyon.runtime.text.feed.decl.put("rss.channel.item.pubDate", El(El.QUANTITY_0_1))
            tachyon.runtime.text.feed.decl.put("rss.channel.item.source", El(El.QUANTITY_0_1, Attr("url")))

            // rss.channel.item +
            tachyon.runtime.text.feed.decl.put("rss.channel.item.title", El(El.QUANTITY_1))

            // rss.channel.item ?
            tachyon.runtime.text.feed.decl.put("rss.channel.item.link", El(El.QUANTITY_0_1)) // muss auch sein das er value anlegt

            // rss.channel.item 1
            tachyon.runtime.text.feed.decl.put("rss.channel.image.url", El(El.QUANTITY_1))
            tachyon.runtime.text.feed.decl.put("rss.channel.image.title", El(El.QUANTITY_1))
            tachyon.runtime.text.feed.decl.put("rss.channel.image.link", El(El.QUANTITY_1))

            // rss.channel.item *
            tachyon.runtime.text.feed.decl.put("rss.channel.image.width", El(El.QUANTITY_0_N))
            tachyon.runtime.text.feed.decl.put("rss.channel.image.height", El(El.QUANTITY_0_N))
            tachyon.runtime.text.feed.decl.put("rss.channel.image.description", El(El.QUANTITY_0_N))
            tachyon.runtime.text.feed.decl.put("rss.channel.textInput.title", El(El.QUANTITY_1))
            tachyon.runtime.text.feed.decl.put("rss.channel.textInput.description", El(El.QUANTITY_1))
            tachyon.runtime.text.feed.decl.put("rss.channel.textInput.name", El(El.QUANTITY_1))
            tachyon.runtime.text.feed.decl.put("rss.channel.textInput.link", El(El.QUANTITY_1))
            tachyon.runtime.text.feed.fd = FeedDeclaration(tachyon.runtime.text.feed.decl, "rss.channel", "rss_0.92")
            declarations.put("rss_0.92", tachyon.runtime.text.feed.fd)

            // RSS 0.91
            tachyon.runtime.text.feed.decl = HashMap<String?, El?>()
            tachyon.runtime.text.feed.decl.put("rss", El(El.QUANTITY_1, Attr("version")))
            // rss.channel *
            tachyon.runtime.text.feed.decl.put("rss.channel.item", El(El.QUANTITY_0_N))
            tachyon.runtime.text.feed.decl.put("rss.channel.category", El(El.QUANTITY_0_N, Attr("domain")))
            tachyon.runtime.text.feed.decl.put("rss.channel.cloud",
                    El(El.QUANTITY_AUTO, arrayOf<Attr?>(Attr("domain"), Attr("port"), Attr("path"), Attr("registerProcedure"), Attr("protocol"))))
            tachyon.runtime.text.feed.decl.put("rss.channel.image", El(El.QUANTITY_AUTO))

            // rss.channel.item *
            tachyon.runtime.text.feed.decl.put("rss.channel.item.author", El(El.QUANTITY_0_1))
            tachyon.runtime.text.feed.decl.put("rss.channel.item.category", El(El.QUANTITY_0_N, Attr("domain", "leer")))
            tachyon.runtime.text.feed.decl.put("rss.channel.item.comments", El(El.QUANTITY_0_1))
            tachyon.runtime.text.feed.decl.put("rss.channel.item.enclosure", El(El.QUANTITY_0_N, arrayOf<Attr?>(Attr("url"), Attr("length"), Attr("type"))))
            tachyon.runtime.text.feed.decl.put("rss.channel.item.guid", El(El.QUANTITY_0_1, Attr("isPermaLink", "true")))
            tachyon.runtime.text.feed.decl.put("rss.channel.item.pubDate", El(El.QUANTITY_0_1))
            tachyon.runtime.text.feed.decl.put("rss.channel.item.source", El(El.QUANTITY_0_1, Attr("url")))

            // rss.channel.item 1
            tachyon.runtime.text.feed.decl.put("rss.channel.image.url", El(El.QUANTITY_1))
            tachyon.runtime.text.feed.decl.put("rss.channel.image.title", El(El.QUANTITY_1))
            tachyon.runtime.text.feed.decl.put("rss.channel.image.link", El(El.QUANTITY_1))

            // rss.channel.item *
            tachyon.runtime.text.feed.decl.put("rss.channel.image.width", El(El.QUANTITY_0_N))
            tachyon.runtime.text.feed.decl.put("rss.channel.image.height", El(El.QUANTITY_0_N))
            tachyon.runtime.text.feed.decl.put("rss.channel.image.description", El(El.QUANTITY_0_N))
            tachyon.runtime.text.feed.decl.put("rss.channel.textInput.title", El(El.QUANTITY_1))
            tachyon.runtime.text.feed.decl.put("rss.channel.textInput.description", El(El.QUANTITY_1))
            tachyon.runtime.text.feed.decl.put("rss.channel.textInput.name", El(El.QUANTITY_1))
            tachyon.runtime.text.feed.decl.put("rss.channel.textInput.link", El(El.QUANTITY_1))
            tachyon.runtime.text.feed.fd = FeedDeclaration(tachyon.runtime.text.feed.decl, "rss.channel", "rss_0.91")
            declarations.put("rss_0.91", tachyon.runtime.text.feed.fd)

            // ATOM
            tachyon.runtime.text.feed.decl = HashMap<String?, El?>()
            tachyon.runtime.text.feed.decl.put("feed", El(El.QUANTITY_1, Attr("version")))
            tachyon.runtime.text.feed.decl.put("feed.author.name", El(El.QUANTITY_1))
            tachyon.runtime.text.feed.decl.put("feed.title", El(El.QUANTITY_1, arrayOf<Attr?>()))
            tachyon.runtime.text.feed.decl.put("feed.link", El(El.QUANTITY_0_N))
            tachyon.runtime.text.feed.decl.put("feed.entry", El(El.QUANTITY_0_N))
            tachyon.runtime.text.feed.decl.put("feed.entry.author", El(El.QUANTITY_0_N))
            tachyon.runtime.text.feed.decl.put("feed.entry.contributor", El(El.QUANTITY_0_N))
            tachyon.runtime.text.feed.decl.put("feed.entry.content", El(El.QUANTITY_0_N))
            tachyon.runtime.text.feed.decl.put("feed.entry.link", El(El.QUANTITY_0_N))
            tachyon.runtime.text.feed.decl.put("feed.entry.title", El(El.QUANTITY_1, arrayOf<Attr?>()))
            tachyon.runtime.text.feed.decl.put("feed.entry.summary", El(El.QUANTITY_1, arrayOf<Attr?>(Attr("type", "text/plain"), Attr("mode", "xml"))))
            tachyon.runtime.text.feed.fd = FeedDeclaration(tachyon.runtime.text.feed.decl, "feed", "atom")
            declarations.put("atom", tachyon.runtime.text.feed.fd)
            defaultDeclaration = FeedDeclaration(HashMap<String?, El?>(), null, "custom")
        }
    }

    private val declaration: Map<String?, El?>?
    private val entryLevel: Array<Key?>?
    val type: String?

    internal inner class FeedDeclarationItem

    /**
     * @return the declaration
     */
    fun getDeclaration(): Map<String?, El?>? {
        return declaration
    }

    /**
     * @return the entryLevel
     */
    fun getEntryLevel(): Array<Key?>? {
        return entryLevel
    }

    init {
        this.declaration = declaration
        if (!StringUtil.isEmpty(entryLevel)) {
            val array: Array = ListUtil.listToArray(entryLevel, '.')
            this.entryLevel = arrayOfNulls<Collection.Key?>(array.size())
            for (i in this.entryLevel.indices) {
                this.entryLevel!![i] = KeyImpl.toKey(array.get(i + 1, null), null)
            }
        } else this.entryLevel = arrayOfNulls<Collection.Key?>(0)
        this.type = type
    }
}