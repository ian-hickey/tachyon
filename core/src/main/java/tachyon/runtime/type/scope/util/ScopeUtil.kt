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
package tachyon.runtime.type.scope.util

import java.io.UnsupportedEncodingException

object ScopeUtil {
    fun getParameterMap(itemsArr: Array<Array<URLItem?>?>?, encodings: Array<String?>?): Map<String?, Array<String?>?>? {
        var n: String
        var v: String
        var arr: Array<String?>
        val parameters: Map<String?, Array<String?>?> = HashMap<String?, Array<String?>?>()
        var items: Array<URLItem?>?
        var encoding: String?
        for (x in itemsArr.indices) {
            items = itemsArr!![x]
            encoding = encodings!![x]
            for (i in items.indices) {
                n = items!![i].getName()
                v = items[i].getValue()
                if (items[i].isUrlEncoded()) {
                    try {
                        n = URLDecoder.decode(n, encoding, true)
                        v = URLDecoder.decode(v, encoding, true)
                    } catch (e: UnsupportedEncodingException) {
                    }
                }
                arr = parameters[n]
                if (arr == null) parameters.put(n, arrayOf<String?>(v)) else {
                    val tmp = arrayOfNulls<String?>(arr.size + 1)
                    System.arraycopy(arr, 0, tmp, 0, arr.size)
                    tmp[arr.size] = v
                    parameters.put(n, tmp)
                }
            }
        }
        return parameters
    }

    fun getParameterValues(itemsArr: Array<Array<URLItem?>?>?, encodings: Array<String?>?, name: String?): Array<String?>? {
        var n: String
        var v: String
        var encName: String?
        var arr: Array<String?>? = null
        var items: Array<URLItem?>?
        var encoding: String?
        for (x in itemsArr.indices) {
            items = itemsArr!![x]
            encoding = encodings!![x]
            encName = if (ReqRspUtil.needEncoding(name, false)) ReqRspUtil.encode(name, encoding) else null
            for (i in items.indices) {
                n = items!![i].getName()
                if (!name!!.equals(n) && (encName == null || !encName.equals(n))) {
                    continue
                }
                v = items[i].getValue()
                if (items[i].isUrlEncoded()) {
                    try {
                        n = URLDecoder.decode(n, encoding, true)
                        v = URLDecoder.decode(v, encoding, true)
                    } catch (e: UnsupportedEncodingException) {
                    }
                }
                if (arr == null) arr = arrayOf(v) else {
                    val tmp = arrayOfNulls<String?>(arr.size + 1)
                    System.arraycopy(arr, 0, tmp, 0, arr.size)
                    tmp[arr.size] = v
                    arr = tmp
                }
            }
        }
        return arr
    }

    fun generateCsrfToken(mapTokens: Map?, strKey: String?, forceNew: Boolean): String? {
        val key: Collection.Key = KeyImpl.init(if (strKey == null) "" else strKey.trim())
        if (mapTokens is Struct) {
            val tokens: Struct = Caster.toStruct(mapTokens, null, false)
            var token: String?
            if (!forceNew) {
                val tmp: Object = tokens.get(key, null)
                token = if (tmp == null) null else Caster.toString(tmp, null)
                if (!StringUtil.isEmpty(token)) return token
            }
            token = RandomUtil.createRandomStringLC(40)
            tokens.setEL(key, token)
            return token
        }
        var token: String?
        if (!forceNew) {
            val tmp: Object = mapTokens.get(key)
            token = if (tmp == null) null else Caster.toString(tmp, null)
            if (!StringUtil.isEmpty(token)) return token
        }
        token = RandomUtil.createRandomStringLC(40)
        mapTokens.put(key, token)
        return token
    }

    fun verifyCsrfToken(mapTokens: Map?, token: String?, strKey: String?): Boolean {
        val key: Collection.Key = KeyImpl.init(if (strKey == null) "" else strKey.trim())
        if (mapTokens is Struct) {
            val tokens: Struct? = mapTokens as Struct?
            val _token: String = Caster.toString(tokens.get(key, null), null)
            return _token != null && _token.equalsIgnoreCase(token)
        }
        val _token: String = Caster.toString(mapTokens.get(key), null)
        return _token != null && _token.equalsIgnoreCase(token)
    }
}