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
/**
 * Implements the CFML Function getpagecontext
 */
package lucee.runtime.functions.other

import java.util.ArrayList

object CreatePageContext : Function {
    @Throws(PageException::class)
    fun call(pc: PageContext?, serverName: String?, scriptName: String?): Object? {
        return call(pc, serverName, scriptName, "", StructImpl(), StructImpl(), StructImpl(), StructImpl())
    }

    @Throws(PageException::class)
    fun call(pc: PageContext?, serverName: String?, scriptName: String?, queryString: String?): Object? {
        return call(pc, serverName, scriptName, queryString, StructImpl(), StructImpl(), StructImpl(), StructImpl())
    }

    @Throws(PageException::class)
    fun call(pc: PageContext?, serverName: String?, scriptName: String?, queryString: String?, cookies: Struct?): Object? {
        return call(pc, serverName, scriptName, queryString, cookies, StructImpl(), StructImpl(), StructImpl())
    }

    @Throws(PageException::class)
    fun call(pc: PageContext?, serverName: String?, scriptName: String?, queryString: String?, cookies: Struct?, headers: Struct?): Object? {
        return call(pc, serverName, scriptName, queryString, cookies, headers, StructImpl(), StructImpl())
    }

    @Throws(PageException::class)
    fun call(pc: PageContext?, serverName: String?, scriptName: String?, queryString: String?, cookies: Struct?, headers: Struct?, parameters: Struct?): Object? {
        return call(pc, serverName, scriptName, queryString, cookies, headers, parameters, StructImpl())
    }

    @Throws(PageException::class)
    fun call(pc: PageContext?, serverName: String?, scriptName: String?, queryString: String?, cookies: Struct?, headers: Struct?, parameters: Struct?, attributes: Struct?): Object? {
        val session: HttpSession? = if (pc != null && pc.getSessionType() === Config.SESSION_TYPE_JEE) pc.getSession() else null
        return ThreadUtil.createPageContext(pc.getConfig(), DevNullOutputStream.DEV_NULL_OUTPUT_STREAM, serverName, scriptName, queryString, toCookies(cookies),
                toPair(headers, true), null, toPair(parameters, true), castValuesToString(attributes), true, -1, session)
    }

    @Throws(PageException::class)
    fun castValuesToString(sct: Struct?): Struct? {
        val keys: Array<Key?> = CollectionUtil.keys(sct)
        for (i in keys.indices) {
            sct.set(keys[i], Caster.toString(sct.get(keys[i])))
        }
        return sct
    }

    @Throws(PageException::class)
    fun toPair(sct: Struct?, doStringCast: Boolean): Array<Pair<String?, Object?>?>? {
        if (sct == null) return arrayOfNulls<Pair?>(0)
        val it: Iterator<Entry<Key?, Object?>?> = sct.entryIterator()
        var e: Entry<Key?, Object?>?
        var value: Object
        val pairs: List<Pair<String?, Object?>?> = ArrayList<Pair<String?, Object?>?>()
        while (it.hasNext()) {
            e = it.next()
            value = e.getValue()
            if (doStringCast) value = Caster.toString(value)
            pairs.add(Pair<String?, Object?>(e.getKey().getString(), value))
        }
        return pairs.toArray(arrayOfNulls<Pair?>(pairs.size()))
    }

    @Throws(PageException::class)
    fun toCookies(sct: Struct?): Array<Cookie?>? {
        if (sct == null) return arrayOfNulls<Cookie?>(0)
        val it: Iterator<Entry<Key?, Object?>?> = sct.entryIterator()
        var e: Entry<Key?, Object?>?
        val cookies: List<Cookie?> = ArrayList<Cookie?>()
        var c: Cookie
        while (it.hasNext()) {
            e = it.next()
            c = ReqRspUtil.toCookie(e.getKey().getString(), Caster.toString(e.getValue()), null)
            if (c != null) cookies.add(c) else throw ApplicationException("Cookie name [" + e.getKey().getString().toString() + "] is invalid")
        }
        return cookies.toArray(arrayOfNulls<Cookie?>(cookies.size()))
    }
}