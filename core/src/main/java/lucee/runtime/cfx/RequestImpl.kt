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
package lucee.runtime.cfx

import com.allaire.cfx.Query

/**
 * Implementation of the CFX Request Interface
 */
class RequestImpl : Request {
    private var attributes: Struct?
    private var settings: Struct? = null
    private var query: Query? = null

    /**
     * constructor of the class
     *
     * @param pc
     * @param attributes
     * @throws PageException
     */
    constructor(pc: PageContext?, attributes: Struct?) {
        this.attributes = attributes
        val o: Object = attributes.get(QUERY, null)
        val varName: String = Caster.toString(o, null)
        if (o != null) {
            if (varName != null) {
                query = QueryWrap(Caster.toQuery(pc.getVariable(varName)))
                attributes.removeEL(QUERY)
            } else if (Decision.isQuery(o)) {
                query = QueryWrap(Caster.toQuery(o))
                attributes.removeEL(QUERY)
            } else {
                throw ApplicationException("Attribute query doesn't contain a Query or a Name of a Query")
            }
        }
    }

    /**
     * constructor of the class
     *
     * @param attributes
     * @param query
     * @param settings
     */
    constructor(attributes: Struct?, query: Query?, settings: Struct?) {
        this.attributes = attributes
        this.query = query
        this.settings = settings
    }

    @Override
    fun attributeExists(key: String?): Boolean {
        return attributes.get(key, null) != null
    }

    @Override
    fun debug(): Boolean {
        val o: Object = attributes.get(DEBUG, Boolean.FALSE) ?: return false
        return Caster.toBooleanValue(o, false)
    }

    @Override
    fun getAttribute(key: String?): String? {
        return getAttribute(key, "")
    }

    @Override
    fun getAttribute(key: String?, defaultValue: String?): String? {
        return Caster.toString(attributes.get(key, defaultValue), defaultValue)
    }

    @get:Override
    val attributeList: Array<String?>?
        get() = CollectionUtil.keysAsString(attributes)

    @Override
    @Throws(NumberFormatException::class)
    fun getIntAttribute(key: String?): Int {
        return getIntAttribute(key, -1)
    }

    @Override
    fun getIntAttribute(key: String?, defaultValue: Int): Int {
        val o: Object = attributes.get(key, null) ?: return defaultValue
        return try {
            Caster.toIntValue(o)
        } catch (e: PageException) {
            defaultValue
        }
    }

    @Override
    fun getQuery(): Query? {
        return query
    }

    @Override
    fun getSetting(key: String?): String? {
        return if (settings == null) "" else Caster.toString(settings.get(key, ""), "")
    }

    companion object {
        private val QUERY: Collection.Key? = KeyConstants._query
        private val DEBUG: Collection.Key? = KeyConstants._debug
    }
}