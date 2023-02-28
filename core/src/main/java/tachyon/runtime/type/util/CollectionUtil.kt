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
package tachyon.runtime.type.util

import java.util.ArrayList

object CollectionUtil {
    val NULL: Object? = Object()
    fun equals(left: Collection?, right: Collection?): Boolean {
        if (left.size() !== right.size()) return false
        val it: Iterator<Key?> = left.keyIterator()
        var k: Key?
        var l: Object
        var r: Object
        while (it.hasNext()) {
            k = it.next()
            r = right.get(k, NULL)
            if (r === NULL) return false
            l = left.get(k, NULL)
            if (!OpUtil.equalsEL(ThreadLocalPageContext.get(), r, l, false, true)) return false
        }
        return true
    }

    /*
	 * public static String[] toStringArray(Key[] keys) { if(keys==null) return null; String[] arr=new
	 * String[keys.length]; for(int i=0;i<keys.length;i++){ arr[i]=keys[i].getString(); } return arr; }
	 */
    fun getKeyList(it: Iterator<Key?>?, delimiter: String?): String? {
        val sb = StringBuilder(it!!.next().getString())
        if (delimiter!!.length() === 1) {
            val c: Char = delimiter.charAt(0)
            while (it!!.hasNext()) {
                sb.append(c)
                sb.append(it.next().getString())
            }
        } else {
            while (it!!.hasNext()) {
                sb.append(delimiter)
                sb.append(it.next().getString())
            }
        }
        return sb.toString()
    }

    fun getKeyList(coll: Collection?, delimiter: String?): String? {
        return if (coll.size() === 0) "" else getKeyList(coll.keyIterator(), delimiter)
    }

    fun keys(coll: Collection?): Array<Key?>? {
        if (coll == null) return arrayOfNulls<Key?>(0)
        val it: Iterator<Key?> = coll.keyIterator()
        val rtn: List<Key?> = ArrayList<Key?>()
        if (it != null) while (it.hasNext()) {
            rtn.add(it.next())
        }
        return rtn.toArray(arrayOfNulls<Key?>(rtn.size()))
    }

    fun keysAsString(coll: Collection?): Array<String?>? {
        if (coll == null) return arrayOfNulls<String?>(0)
        val it: Iterator<Key?> = coll.keyIterator()
        val rtn: List<String?> = ArrayList<String?>()
        if (it != null) while (it.hasNext()) {
            rtn.add(it.next().getString())
        }
        return rtn.toArray(arrayOfNulls<String?>(rtn.size()))
    }

    fun isEmpty(map: Map<*, *>?): Boolean {
        return map == null || map.size() === 0
    }

    /*
	 * public static int hashCode(Collection coll) { produce infiniti loop when there is a refrerence to
	 * itself or an anchestor
	 * 
	 * int hashCode = 1; Iterator<Entry<Key, Object>> it = coll.entryIterator(); Entry<Key, Object> e;
	 * while(it.hasNext()) { e = it.next(); hashCode = 31*hashCode+
	 * 
	 * ( (e.getKey()==null?0:e.getKey().hashCode()) ^ (e.getValue()==null ? 0 : e.getValue().hashCode())
	 * ); } return hashCode; }
	 */
    fun toKeys(strArr: Array<String?>?, trim: Boolean): Array<Collection.Key?>? {
        val keyArr: Array<Collection.Key?> = arrayOfNulls<Collection.Key?>(strArr!!.size)
        for (i in keyArr.indices) {
            keyArr[i] = KeyImpl.init(if (trim) strArr!![i].trim() else strArr!![i])
        }
        return keyArr
    }

    fun toKeys(set: Set<String?>?): Array<Collection.Key?>? {
        val keyArr: Array<Collection.Key?> = arrayOfNulls<Collection.Key?>(set!!.size())
        val it = set!!.iterator()
        var index = 0
        while (it.hasNext()) {
            keyArr[index++] = KeyImpl.init(it.next())
        }
        return keyArr
    }

    fun toString(keys: Array<Collection.Key?>?, trim: Boolean): Array<String?>? {
        if (keys == null) return null
        val data = arrayOfNulls<String?>(keys.size)
        for (i in keys.indices) {
            data[i] = if (trim) keys[i].getString().trim() else keys[i].getString()
        }
        return data
    }

    fun <T> remove(list: List<T?>?, index: Int, defaultValue: T?): T? {
        return try {
            list.remove(index)
        } catch (t: Throwable) {
            ExceptionUtil.rethrowIfNecessary(t)
            defaultValue
        }
    }
}