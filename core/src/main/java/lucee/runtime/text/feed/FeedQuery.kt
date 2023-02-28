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
package lucee.runtime.text.feed

import java.util.Iterator

object FeedQuery {
    val VERSION: Collection.Key? = KeyConstants._VERSION
    val ITEM: Collection.Key? = KeyConstants._ITEM
    val ENTRY: Collection.Key? = KeyConstants._ENTRY
    val AUTHOREMAIL: Collection.Key? = KeyImpl.getInstance("AUTHOREMAIL")
    val AUTHORNAME: Collection.Key? = KeyImpl.getInstance("AUTHORNAME")
    val AUTHORURI: Collection.Key? = KeyImpl.getInstance("AUTHORURI")
    val AUTHOR: Collection.Key? = KeyImpl.getInstance("AUTHOR")
    val CATEGORYLABEL: Collection.Key? = KeyImpl.getInstance("CATEGORYLABEL")
    val CATEGORYSCHEME: Collection.Key? = KeyImpl.getInstance("CATEGORYSCHEME")
    val CATEGORYTERM: Collection.Key? = KeyImpl.getInstance("CATEGORYTERM")
    val CATEGORY: Collection.Key? = KeyImpl.getInstance("CATEGORY")
    val COMMENTS: Collection.Key? = KeyConstants._COMMENTS
    val CONTENT: Collection.Key? = KeyConstants._CONTENT
    val CONTENTMODE: Collection.Key? = KeyImpl.getInstance("CONTENTMODE")
    val CONTENTSRC: Collection.Key? = KeyImpl.getInstance("CONTENTSRC")
    val CONTENTTYPE: Collection.Key? = KeyImpl.getInstance("CONTENTTYPE")
    val CONTRIBUTOREMAIL: Collection.Key? = KeyImpl.getInstance("CONTRIBUTOREMAIL")
    val CONTRIBUTORNAME: Collection.Key? = KeyImpl.getInstance("CONTRIBUTORNAME")
    val CONTRIBUTORURI: Collection.Key? = KeyImpl.getInstance("CONTRIBUTORURI")
    val CONTRIBUTOR: Collection.Key? = KeyImpl.getInstance("CONTRIBUTOR")
    val CREATEDDATE: Collection.Key? = KeyImpl.getInstance("CREATEDDATE")
    val CREATED: Collection.Key? = KeyImpl.getInstance("CREATED")
    val EXPIRATIONDATE: Collection.Key? = KeyImpl.getInstance("EXPIRATIONDATE")
    val ID: Collection.Key? = KeyConstants._ID
    val IDPERMALINK: Collection.Key? = KeyImpl.getInstance("IDPERMALINK")
    val LINKHREF: Collection.Key? = KeyImpl.getInstance("LINKHREF")
    val LINKHREFLANG: Collection.Key? = KeyImpl.getInstance("LINKHREFLANG")
    val LINKLENGTH: Collection.Key? = KeyImpl.getInstance("LINKLENGTH")
    val LINKREL: Collection.Key? = KeyImpl.getInstance("LINKREL")
    val LINKTITLE: Collection.Key? = KeyImpl.getInstance("LINKTITLE")
    val LINKTYPE: Collection.Key? = KeyImpl.getInstance("LINKTYPE")
    val PUBLISHEDDATE: Collection.Key? = KeyImpl.getInstance("PUBLISHEDDATE")
    val PUBLISHED: Collection.Key? = KeyImpl.getInstance("PUBLISHED")
    val PUBDATE: Collection.Key? = KeyImpl.getInstance("pubDate")
    val RDF_ABOUT: Collection.Key? = KeyImpl.getInstance("rdf:about")
    val RIGHTS: Collection.Key? = KeyImpl.getInstance("RIGHTS")
    val RSSLINK: Collection.Key? = KeyImpl.getInstance("RSSLINK")
    val SOURCE: Collection.Key? = KeyConstants._SOURCE
    val SOURCEURL: Collection.Key? = KeyImpl.getInstance("SOURCEURL")
    val SUMMARY: Collection.Key? = KeyImpl.getInstance("SUMMARY")
    val SUMMARYMODE: Collection.Key? = KeyImpl.getInstance("SUMMARYMODE")
    val SUMMARYSRC: Collection.Key? = KeyImpl.getInstance("SUMMARYSRC")
    val SUMMARYTYPE: Collection.Key? = KeyImpl.getInstance("SUMMARYTYPE")
    val TITLE: Collection.Key? = KeyImpl.getInstance("TITLE")
    val TITLETYPE: Collection.Key? = KeyImpl.getInstance("TITLETYPE")
    val UPDATEDDATE: Collection.Key? = KeyImpl.getInstance("UPDATEDDATE")
    val URI: Collection.Key? = KeyImpl.getInstance("URI")
    val XMLBASE: Collection.Key? = KeyImpl.getInstance("XMLBASE")
    val GUID: Collection.Key? = KeyConstants._guid
    val ENCLOSURE: Collection.Key? = KeyImpl.getInstance("enclosure")
    val LINK: Collection.Key? = KeyConstants._link
    val MODE: Collection.Key? = KeyConstants._mode
    val TEXT: Collection.Key? = KeyConstants._text
    val DOMAIN: Collection.Key? = KeyConstants._domain
    val ISSUED: Collection.Key? = KeyImpl.getInstance("issued")
    val COPYRIGHT: Collection.Key? = KeyImpl.getInstance("copyright")
    val SRC: Collection.Key? = KeyConstants._src
    val UPDATED: Collection.Key? = KeyConstants._updated
    val MODIFIED: Collection.Key? = KeyImpl.getInstance("modified")
    val URL: Collection.Key? = KeyConstants._url
    val LENGTH: Collection.Key? = KeyConstants._length
    val ISPERMALINK: Collection.Key? = KeyImpl.getInstance("isPermaLink")
    val DC_CONTRIBUTOR: Collection.Key? = KeyImpl.getInstance("DC_CONTRIBUTOR")
    val DC_COVERAGE: Collection.Key? = KeyImpl.getInstance("DC_COVERAGE")
    val DC_CREATOR: Collection.Key? = KeyImpl.getInstance("DC_CREATOR")
    val DC_DATE: Collection.Key? = KeyImpl.getInstance("DC_DATE")
    val DC_DESCRIPTION: Collection.Key? = KeyImpl.getInstance("DC_DESCRIPTION")
    val DC_FORMAT: Collection.Key? = KeyImpl.getInstance("DC_FORMAT")
    val DC_IDENTIFIER: Collection.Key? = KeyImpl.getInstance("DC_IDENTIFIER")
    val DC_LANGUAGE: Collection.Key? = KeyImpl.getInstance("DC_LANGUAGE")
    val DC_PUBLISHER: Collection.Key? = KeyImpl.getInstance("DC_PUBLISHER")
    val DC_RELATION: Collection.Key? = KeyImpl.getInstance("DC_RELATION")
    val DC_RIGHT: Collection.Key? = KeyImpl.getInstance("DC_RIGHTS")
    val DC_SOURCE: Collection.Key? = KeyImpl.getInstance("DC_SOURCE")
    val DC_TITLE: Collection.Key? = KeyImpl.getInstance("DC_TITLE")
    val DC_TYPE: Collection.Key? = KeyImpl.getInstance("DC_TYPE")
    val DC_SUBJECT_TAXONOMYURI: Collection.Key? = KeyImpl.getInstance("DC_SUBJECT_TAXONOMYURI")
    val DC_SUBJECT_VALUE: Collection.Key? = KeyImpl.getInstance("DC_SUBJECT_VALUE")
    val DC_SUBJECT: Collection.Key? = KeyImpl.getInstance("DC_SUBJECT")
    private val COLUMNS: Array<Collection.Key?>? = arrayOf<Collection.Key?>(AUTHOREMAIL, AUTHORNAME, AUTHORURI, CATEGORYLABEL, CATEGORYSCHEME, CATEGORYTERM, COMMENTS, CONTENT,
            CONTENTMODE, CONTENTSRC, CONTENTTYPE, CONTRIBUTOREMAIL, CONTRIBUTORNAME, CONTRIBUTORURI, CREATEDDATE, EXPIRATIONDATE, ID, IDPERMALINK, LINKHREF, LINKHREFLANG,
            LINKLENGTH, LINKREL, LINKTITLE, LINKTYPE, PUBLISHEDDATE, RIGHTS, RSSLINK, SOURCE, SOURCEURL, SUMMARY, SUMMARYMODE, SUMMARYSRC, SUMMARYTYPE, TITLE, TITLETYPE,
            UPDATEDDATE, URI, XMLBASE)
    private val COLUMNS_WITH_DC: Array<Collection.Key?>? = arrayOf<Collection.Key?>(AUTHOREMAIL, AUTHORNAME, AUTHORURI, CATEGORYLABEL, CATEGORYSCHEME, CATEGORYTERM, COMMENTS, CONTENT,
            CONTENTMODE, CONTENTSRC, CONTENTTYPE, CONTRIBUTOREMAIL, CONTRIBUTORNAME, CONTRIBUTORURI, CREATEDDATE,
            DC_CONTRIBUTOR, DC_COVERAGE, DC_CREATOR, DC_DATE, DC_DESCRIPTION, DC_FORMAT, DC_IDENTIFIER, DC_LANGUAGE, DC_PUBLISHER, DC_RELATION, DC_RIGHT, DC_SOURCE, DC_TITLE,
            DC_TYPE, DC_SUBJECT_TAXONOMYURI, DC_SUBJECT_VALUE,
            EXPIRATIONDATE, ID, IDPERMALINK, LINKHREF, LINKHREFLANG, LINKLENGTH, LINKREL, LINKTITLE, LINKTYPE, PUBLISHEDDATE, RIGHTS, RSSLINK, SOURCE, SOURCEURL, SUMMARY,
            SUMMARYMODE, SUMMARYSRC, SUMMARYTYPE, TITLE, TITLETYPE, UPDATEDDATE, URI, XMLBASE)

    @Throws(DatabaseException::class)
    fun toQuery(data: Struct?, hasDC: Boolean): Query? {
        val qry: Query = QueryImpl(if (hasDC) COLUMNS_WITH_DC else COLUMNS, 0, "")
        val version: String = Caster.toString(data.get(VERSION, ""), "")
        var items: Array? = null
        if (StringUtil.startsWithIgnoreCase(version, "rss") || StringUtil.startsWithIgnoreCase(version, "rdf")) {
            items = Caster.toArray(data.get(ITEM, null), null)
            if (items == null) {
                val sct: Struct = Caster.toStruct(data.get(version, null), null, false)
                if (sct != null) {
                    items = Caster.toArray(sct.get(ITEM, null), null)
                }
            }
            return toQuery(true, qry, items)
        } else if (StringUtil.startsWithIgnoreCase(version, "atom")) {
            items = Caster.toArray(data.get(ENTRY, null), null)
            return toQuery(false, qry, items)
        }
        return qry
    }

    private fun toQuery(isRss: Boolean, qry: Query?, items: Array?): Query? {
        if (items == null) return qry
        val len: Int = items.size()
        var item: Struct
        var row = 0
        var it: Iterator<Entry<Key?, Object?>?>
        var e: Entry<Key?, Object?>?
        for (i in 1..len) {
            item = Caster.toStruct(items.get(i, null), null, false)
            if (item == null) continue
            qry.addRow()
            row++
            it = item.entryIterator()
            while (it.hasNext()) {
                e = it.next()
                if (isRss) setQueryValueRSS(qry, e.getKey(), e.getValue(), row) else setQueryValueAtom(qry, e.getKey(), e.getValue(), row)
            }
        }
        return qry
    }

    private fun setQueryValueAtom(qry: Query?, key: Key?, value: Object?, row: Int) {
        if (key.equals(AUTHOR)) {
            val sct: Struct? = toStruct(value)
            if (sct != null) {
                qry.setAtEL(AUTHOREMAIL, row, sct.get("email", null))
                qry.setAtEL(AUTHORNAME, row, sct.get("name", null))
                qry.setAtEL(AUTHORURI, row, sct.get("uri", null))
            }
        }
        if (key.equals(CATEGORY)) {
            val sct: Struct? = toStruct(value)
            if (sct != null) {
                qry.setAtEL(CATEGORYLABEL, row, sct.get("label", null))
                qry.setAtEL(CATEGORYSCHEME, row, sct.get("scheme", null))
                qry.setAtEL(CATEGORYTERM, row, sct.get("term", null))
            }
            // else qry.setAtEL(CATEGORYLABEL, row, getValue(value));
        } else if (key.equals(COMMENTS)) {
            qry.setAtEL(COMMENTS, row, getValue(value))
        } else if (key.equals(CONTENT)) {
            val sct: Struct? = toStruct(value)
            if (sct != null) {
                qry.setAtEL(CONTENT, row, getValue(sct))
                qry.setAtEL(CONTENTMODE, row, sct.get(MODE, null))
                qry.setAtEL(CONTENTSRC, row, sct.get(SRC, null))
                qry.setAtEL(CONTENTTYPE, row, sct.get(KeyConstants._type, null))
                qry.setAtEL(XMLBASE, row, sct.get("xml:base", null))
            } else qry.setAtEL(CONTENT, row, getValue(value))
        } else if (key.equals(CONTRIBUTOR)) {
            val sct: Struct? = toStruct(value)
            if (sct != null) {
                qry.setAtEL(CONTRIBUTOREMAIL, row, sct.get("email", null))
                qry.setAtEL(CONTRIBUTORNAME, row, sct.get(KeyConstants._name, null))
                qry.setAtEL(CONTRIBUTORURI, row, sct.get("uri", null))
            }
        } else if (key.equals(CREATED)) {
            qry.setAtEL(CREATEDDATE, row, getValue(value))
        } else if (key.equals(ID)) {
            qry.setAtEL(ID, row, getValue(value))
        } else if (key.equals(LINK)) {
            val sct: Struct? = toStruct(value)
            if (sct != null) {
                qry.setAtEL(LINKHREF, row, sct.get("href", null))
                qry.setAtEL(LINKHREFLANG, row, sct.get("hreflang", null))
                qry.setAtEL(LINKLENGTH, row, sct.get(LENGTH, null))
                qry.setAtEL(LINKREL, row, sct.get("rel", null))
                qry.setAtEL(LINKTITLE, row, sct.get(TITLE, null))
                qry.setAtEL(LINKTYPE, row, sct.get(KeyConstants._type, null))
            }
        } else if (key.equals(PUBLISHED)) {
            qry.setAtEL(PUBLISHEDDATE, row, getValue(value))
        } else if (key.equals(ISSUED)) {
            qry.setAtEL(PUBLISHEDDATE, row, getValue(value))
        } else if (key.equals(RIGHTS)) {
            qry.setAtEL(RIGHTS, row, getValue(value))
        } else if (key.equals(COPYRIGHT)) {
            qry.setAtEL(RIGHTS, row, getValue(value))
        } else if (key.equals(SUMMARY)) {
            val sct: Struct? = toStruct(value)
            if (sct != null) {
                qry.setAtEL(SUMMARY, row, getValue(sct))
                qry.setAtEL(SUMMARYMODE, row, sct.get(MODE, null))
                qry.setAtEL(SUMMARYSRC, row, sct.get(SRC, null))
                qry.setAtEL(SUMMARYTYPE, row, sct.get(KeyConstants._type, null))
            } else qry.setAtEL(SUMMARY, row, getValue(value))
        } else if (key.equals(TITLE)) {
            val sct: Struct? = toStruct(value)
            if (sct != null) {
                qry.setAtEL(TITLE, row, getValue(sct))
                qry.setAtEL(TITLETYPE, row, sct.get(KeyConstants._type, null))
            } else qry.setAtEL(TITLE, row, getValue(value))
        } else if (key.equals(UPDATED)) {
            qry.setAtEL(UPDATEDDATE, row, getValue(value))
        } else if (key.equals(MODIFIED)) {
            qry.setAtEL(UPDATEDDATE, row, getValue(value))
        }
    }

    private fun setQueryValueRSS(qry: Query?, key: Key?, value: Object?, row: Int) {
        if (key.equals(AUTHOR)) {
            qry.setAtEL(AUTHOREMAIL, row, getValue(value))
        } else if (key.equals(CATEGORY)) {
            val sct: Struct? = toStruct(value)
            if (sct != null) {
                qry.setAtEL(CATEGORYLABEL, row, getValue(sct))
                qry.setAtEL(CATEGORYSCHEME, row, sct.get(DOMAIN, null))
            } else qry.setAtEL(CATEGORYLABEL, row, getValue(value))
        } else if (key.equals(COMMENTS)) {
            qry.setAtEL(COMMENTS, row, getValue(value))
        } else if (key.equals(KeyConstants._description)) {
            qry.setAtEL(CONTENT, row, getValue(value))
        } else if (key.equals(EXPIRATIONDATE)) {
            qry.setAtEL(EXPIRATIONDATE, row, getValue(value))
        } else if (key.equals(GUID)) {
            val sct: Struct? = toStruct(value)
            if (sct != null) {
                qry.setAtEL(ID, row, getValue(sct))
                qry.setAtEL(IDPERMALINK, row, sct.get(ISPERMALINK, null))
            } else qry.setAtEL(ID, row, getValue(value))
        } else if (key.equals(ENCLOSURE)) {
            val sct: Struct? = toStruct(value)
            if (sct != null) {
                qry.setAtEL(LINKHREF, row, sct.get(URL, null))
                qry.setAtEL(LINKLENGTH, row, sct.get(LENGTH, null))
                qry.setAtEL(LINKTYPE, row, sct.get(KeyConstants._type, null))
            }
        } else if (key.equals(PUBDATE)) {
            qry.setAtEL(PUBLISHEDDATE, row, getValue(value))
        } else if (key.equals(RDF_ABOUT)) {
            qry.setAtEL(URI, row, getValue(value))
        } else if (key.equals(LINK)) {
            val sct: Struct? = toStruct(value)
            if (sct != null) {
                qry.setAtEL(RSSLINK, row, getValue(sct))
                val v: Object = sct.get(RDF_ABOUT, null)
                if (v != null) qry.setAtEL(URI, row, v)
            } else qry.setAtEL(RSSLINK, row, getValue(value))
        } else if (key.equals(SOURCE)) {
            val sct: Struct? = toStruct(value)
            if (sct != null) {
                qry.setAtEL(SOURCE, row, getValue(sct))
                qry.setAtEL(SOURCEURL, row, sct.get(URL, null))
            } else qry.setAtEL(SOURCE, row, getValue(value))
        } else if (key.equals(SUMMARY)) {
            val sct: Struct? = toStruct(value)
            if (sct != null) {
                qry.setAtEL(SUMMARY, row, getValue(sct))
                qry.setAtEL(SUMMARYMODE, row, sct.get(MODE, null))
                qry.setAtEL(SUMMARYTYPE, row, sct.get(KeyConstants._type, null))
            } else qry.setAtEL(SUMMARY, row, getValue(value))
        } else if (key.equals(TITLE)) {
            qry.setAtEL(TITLE, row, getValue(value))
        }

        // Dublin Core
        if (key.getLowerString().startsWith("dc_")) {
            if (key.equals(DC_CONTRIBUTOR)) {
                qry.setAtEL(DC_CONTRIBUTOR, row, getValue(value))
            } else if (key.equals(DC_COVERAGE)) {
                qry.setAtEL(DC_COVERAGE, row, getValue(value))
            } else if (key.equals(DC_CREATOR)) {
                qry.setAtEL(DC_CREATOR, row, getValue(value))
            } else if (key.equals(DC_DATE)) {
                qry.setAtEL(DC_DATE, row, getValue(value))
            } else if (key.equals(DC_DESCRIPTION)) {
                qry.setAtEL(DC_DESCRIPTION, row, getValue(value))
            } else if (key.equals(DC_FORMAT)) {
                qry.setAtEL(DC_FORMAT, row, getValue(value))
            } else if (key.equals(DC_IDENTIFIER)) {
                qry.setAtEL(DC_IDENTIFIER, row, getValue(value))
            } else if (key.equals(DC_LANGUAGE)) {
                qry.setAtEL(DC_LANGUAGE, row, getValue(value))
            } else if (key.equals(DC_PUBLISHER)) {
                qry.setAtEL(DC_PUBLISHER, row, getValue(value))
            } else if (key.equals(DC_RELATION)) {
                qry.setAtEL(DC_RELATION, row, getValue(value))
            } else if (key.equals(DC_RIGHT)) {
                qry.setAtEL(DC_RIGHT, row, getValue(value))
            } else if (key.equals(DC_SOURCE)) {
                qry.setAtEL(DC_SOURCE, row, getValue(value))
            } else if (key.equals(DC_SUBJECT_TAXONOMYURI)) {
                qry.setAtEL(DC_SUBJECT_TAXONOMYURI, row, getValue(value))
            } else if (key.equals(DC_SUBJECT)) {
                qry.setAtEL(DC_SUBJECT_VALUE, row, getValue(value))
            } else if (key.equals(DC_TITLE)) {
                qry.setAtEL(DC_TITLE, row, getValue(value))
            } else if (key.equals(DC_TYPE)) {
                qry.setAtEL(DC_TYPE, row, getValue(value))
            }
        }
    }

    fun getValue(value: Object?): Object? {
        return getValue(value, false)
    }

    fun getValue(value: Object?, includeChildren: Boolean): Object? {
        return if (value is Struct) getValue(value as Struct?, includeChildren) else Caster.toString(value, null)
    }

    fun getValue(sct: Struct?, includeChildren: Boolean): Object? {
        var obj: Object = sct.get(KeyConstants._value, null)
        if (obj == null) obj = sct.get(TEXT, null)
        return obj
    }

    private fun toStruct(value: Object?): Struct? {
        if (value is Struct) return value as Struct?
        if (value is Array) {
            val sct: Struct = StructImpl()
            var row: Struct
            val arr: Array? = value
            val len: Int = arr.size()
            // Key[] keys;
            var it: Iterator<Entry<Key?, Object?>?>
            var e: Entry<Key?, Object?>?
            var nw: String
            var ext: Object
            for (i in 1..len) {
                row = Caster.toStruct(arr.get(i, null), null, false)
                if (row == null) continue
                it = row.entryIterator()
                // keys = row.keys();
                while (it.hasNext()) {
                    e = it.next()
                    ext = sct.get(e.getKey(), null)
                    nw = Caster.toString(e.getValue(), null)
                    if (nw != null) {
                        if (ext == null) sct.setEL(e.getKey(), nw) else if (ext is CastableArray) {
                            (ext as CastableArray).appendEL(nw)
                        } else {
                            val ca = CastableArray()
                            ca.appendEL(Caster.toString(ext, null))
                            ca.appendEL(nw)
                            sct.setEL(e.getKey(), ca)
                        }
                    }
                }
            }
            return sct
        }
        return null
    }

    fun toQuery(qry: Query?): Query? {
        return qry
    }
}