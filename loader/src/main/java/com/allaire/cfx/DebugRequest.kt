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
package com.allaire.cfx

import java.util.ArrayList

/**
 * Implementation of the Debug Request
 */
class DebugRequest(attributes: Hashtable?, query: Query?, settings: Hashtable?) : Request {
    private val attributes: Struct?
    private override val query: Query?
    private val settings: Struct?

    constructor(attributes: Hashtable?) : this(attributes, null, null) {}
    constructor(attributes: Hashtable?, query: Query?) : this(attributes, query, null) {}

    /**
     * @see com.allaire.cfx.Request.attributeExists
     */
    @Override
    override fun attributeExists(key: String?): Boolean {
        return attributes.containsKey(key)
    }

    @Override
    override fun debug(): Boolean {
        val o: Object = attributes.get("debug", Boolean.FALSE)
        return CFMLEngineFactory.getInstance().getCastUtil().toBooleanValue(o, false)
    }

    @Override
    override fun getAttribute(key: String?, defaultValue: String?): String {
        return CFMLEngineFactory.getInstance().getCastUtil().toString(attributes.get(key, defaultValue), defaultValue)
    }

    @Override
    override fun getAttribute(key: String?): String {
        return getAttribute(key, "")
    }

    @get:Override
    override val attributeList: Array<String?>?
        get() {
            val it: Iterator<Key> = attributes.keyIterator()
            val arr: List<String> = ArrayList<String>()
            while (it.hasNext()) arr.add(it.next().getString())
            return arr.toArray(arrayOfNulls<String>(arr.size()))
        }

    @Override
    override fun getIntAttribute(key: String?, defaultValue: Int): Int {
        val o: Object = attributes.get(key, null) ?: return defaultValue
        return CFMLEngineFactory.getInstance().getCastUtil().toDoubleValue(o, defaultValue)
    }

    @Override
    @Throws(NumberFormatException::class)
    override fun getIntAttribute(key: String?): Int {
        return getIntAttribute(key, -1)
    }

    @Override
    fun getQuery(): Query? {
        return query
    }

    @Override
    override fun getSetting(key: String?): String {
        return if (settings == null) "" else CFMLEngineFactory.getInstance().getCastUtil().toString(settings.get(key, ""), "")
    }

    companion object {
        /**
         * @param hashTable a Hashtable to a Struct
         * @return casted struct
         */
        private fun toStruct(hashTable: Hashtable?): Struct? {
            if (hashTable == null) return null
            val e: Enumeration = hashTable.keys()
            val sct: Struct = CFMLEngineFactory.getInstance().getCreationUtil().createStruct()
            while (e.hasMoreElements()) {
                val key: Object = e.nextElement()
                sct.setEL(key.toString(), hashTable.get(key))
            }
            return sct
        }
    }

    init {
        this.attributes = toStruct(attributes)
        this.query = query
        this.settings = toStruct(settings)
    }
}