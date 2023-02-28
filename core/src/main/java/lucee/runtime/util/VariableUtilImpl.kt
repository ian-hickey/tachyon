/**
 * Copyright (c) 2014, the Railo Company Ltd.
 * Copyright (c) 2015, Lucee Assosication Switzerland
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
package lucee.runtime.util

import java.util.Date

/**
 * Class to handle CF Variables (set,get,call)
 */
class VariableUtilImpl : VariableUtil {
    @Override
    fun getCollection(pc: PageContext?, coll: Object?, key: String?, defaultValue: Object?): Object? {
        return if (coll is Query) {
            // TODO sollte nicht null sein
            (coll as Query?).getColumn(key, null)
        } else get(pc, coll, key, defaultValue)
    }

    @Override
    fun getCollection(pc: PageContext?, coll: Object?, key: Collection.Key?, defaultValue: Object?): Object? {
        return if (coll is Query) {
            (coll as Query?).getColumn(key, null) ?: return defaultValue
        } else get(pc, coll, key, defaultValue)
    }

    @Override
    operator fun get(pc: PageContext?, coll: Object?, key: String?, defaultValue: Object?): Object? {
        // Objects
        if (coll is Objects) {
            return (coll as Objects?).get(pc, KeyImpl.init(key), defaultValue)
        } else if (coll is Collection) {
            return (coll as Collection?).get(key, defaultValue)
        } else if (coll is Map) {
            val rtn: Object? = (coll as Map?)!!.get(key)
            // if(rtn==null)rtn=((Map)coll).get(MapAsStruct.getCaseSensitiveKey((Map)coll, key));
            return if (rtn != null) rtn else defaultValue
        } else if (coll is List) {
            val index: Int = Caster.toIntValue(key, Integer.MIN_VALUE)
            return if (index == Integer.MIN_VALUE) defaultValue else try {
                (coll as List?)!![index - 1]
            } catch (e: IndexOutOfBoundsException) {
                defaultValue
            }
        } else if (Decision.isNativeArray(coll)) {
            return ArrayUtil.get(coll, Caster.toIntValue(key, Integer.MIN_VALUE) - 1, defaultValue)
        } else if (coll is Node) {
            return XMLStructFactory.newInstance(coll as Node?, false).get(key, defaultValue)
        }
        // Direct Object Access
        if (pc.getConfig().getSecurityManager().getAccess(SecurityManager.TYPE_DIRECT_JAVA_ACCESS) === SecurityManager.VALUE_YES) {
            if (doLogReflectionCalls()) LogUtil.log(pc, Log.LEVEL_INFO, "reflection", "get-property:" + key + " from class " + Caster.toTypeName(coll))
            return Reflector.getProperty(coll, key, defaultValue)
        }
        return null
    }

    @Override
    operator fun get(pc: PageContext?, coll: Object?, key: Collection.Key?, defaultValue: Object?): Object? {
        // Objects
        if (coll is Objects) {
            return (coll as Objects?).get(pc, key, defaultValue)
        } else if (coll is Collection) {
            return (coll as Collection?).get(key, defaultValue)
        } else if (coll is Map) {
            val rtn: Object? = (coll as Map?)!![key.getString()]
            // if(rtn==null)rtn=((Map)coll).get(MapAsStruct.getCaseSensitiveKey((Map)coll, key.getString()));
            return if (rtn != null) rtn else defaultValue
        } else if (coll is List) {
            val index: Int = Caster.toIntValue(key, Integer.MIN_VALUE)
            return if (index == Integer.MIN_VALUE) defaultValue else try {
                (coll as List?)!![index - 1]
            } catch (e: IndexOutOfBoundsException) {
                defaultValue
            }
        } else if (Decision.isNativeArray(coll)) {
            return ArrayUtil.get(coll, Caster.toIntValue(key, Integer.MIN_VALUE) - 1, defaultValue)
        } else if (coll is Node) {
            return XMLStructFactory.newInstance(coll as Node?, false).get(key, defaultValue)
        } else if (coll == null) return defaultValue

        // Direct Object Access
        if (pc.getConfig().getSecurityManager().getAccess(SecurityManager.TYPE_DIRECT_JAVA_ACCESS) === SecurityManager.VALUE_YES) {
            if (doLogReflectionCalls()) LogUtil.log(pc, Log.LEVEL_INFO, "reflection", "get-property:" + key + " from class " + Caster.toTypeName(coll))
            return Reflector.getProperty(coll, key.getString(), defaultValue)
        }
        return defaultValue
    }

    fun getLight(pc: PageContext?, coll: Object?, key: Collection.Key?, defaultValue: Object?): Object? {
        // Objects
        if (coll is Objects) {
            return (coll as Objects?).get(pc, key, defaultValue)
        } else if (coll is Collection) {
            return (coll as Collection?).get(key, defaultValue)
        } else if (coll is Map) {
            // Object rtn=null;
            try {
                val rtn: Object? = (coll as Map?)!![key.getString()]
                // if(rtn==null)rtn=((Map)coll).get(MapAsStruct.getCaseSensitiveKey((Map)coll, key.getString()));
                if (rtn != null) return rtn
            } catch (t: Throwable) {
                ExceptionUtil.rethrowIfNecessary(t)
            }
            return Reflector.getField(coll, key.getString(), defaultValue)
            // return rtn;
        } else if (coll is List) {
            val index: Int = Caster.toIntValue(key.getString(), Integer.MIN_VALUE)
            return if (index == Integer.MIN_VALUE) null else try {
                (coll as List?)!![index - 1]
            } catch (e: IndexOutOfBoundsException) {
                defaultValue
            }
        }
        return defaultValue
    }

    @Override
    fun getLight(pc: PageContext?, coll: Object?, key: String?, defaultValue: Object?): Object? {
        // Objects
        if (coll is Objects) {
            return (coll as Objects?).get(pc, KeyImpl.init(key), defaultValue)
        } else if (coll is Collection) {
            return (coll as Collection?).get(key, defaultValue)
        } else if (coll is Map) {
            try {
                val rtn: Object? = (coll as Map?)!!.get(key)
                // if(rtn==null)rtn=((Map)coll).get(MapAsStruct.getCaseSensitiveKey((Map)coll, key));
                if (rtn != null) return rtn
            } catch (t: Throwable) {
                ExceptionUtil.rethrowIfNecessary(t)
            }
            return Reflector.getProperty(coll, key, defaultValue)
            // return rtn;
        } else if (coll is List) {
            val index: Int = Caster.toIntValue(key, Integer.MIN_VALUE)
            return if (index == Integer.MIN_VALUE) null else try {
                (coll as List?)!![index - 1]
            } catch (e: IndexOutOfBoundsException) {
                defaultValue
            }
        }
        return defaultValue
    }

    @Override
    @Throws(PageException::class)
    fun getCollection(pc: PageContext?, coll: Object?, key: String?): Object? {
        return if (coll is Query) {
            (coll as Query?).getColumn(key)
        } else get(pc, coll, key)
    }

    @Throws(PageException::class)
    fun getCollection(pc: PageContext?, coll: Object?, key: Collection.Key?): Object? {
        return if (coll is Query) {
            (coll as Query?).getColumn(key)
        } else get(pc, coll, key)
    }

    @Throws(PageException::class)
    operator fun get(pc: PageContext?, coll: Object?, key: Collection.Key?): Object? {
        // Objects
        if (coll is Objects) {
            return (coll as Objects?).get(pc, key)
        } else if (coll is Collection) {
            return (coll as Collection?).get(key)
        } else if (coll is Map) {
            var rtn: Object? = null
            try {
                rtn = (coll as Map?)!![key.getString()]
                if (rtn == null && coll.getClass().getName().startsWith("org.hibernate.")) rtn = (coll as Map?)!![MapAsStruct.getCaseSensitiveKey(coll as Map?, key.getString())]
                if (rtn != null) return rtn
            } catch (t: Throwable) {
                ExceptionUtil.rethrowIfNecessary(t)
            }
            rtn = Reflector.getProperty(coll, key.getString(), null)
            if (rtn != null) return rtn
            val realKey: String = MapAsStruct.getCaseSensitiveKey(coll as Map?, key.getString())
            var detail: String? = null
            if (realKey != null) {
                detail = ("The keys for this Map are case-sensitive, use bracked notation like this \"map['" + realKey + "']\" instead of dot notation like this  \"map." + realKey
                        + "\" to address the Map")
            }
            throw ExpressionException("Key [" + key.getString().toString() + "] doesn't exist in Map (" + (coll as Map?).getClass().getName().toString() + ")", detail)
        } else if (coll is List) {
            return try {
                val rtn = (coll as List?)!![Caster.toIntValue(key.getString()) - 1]
                        ?: throw ExpressionException("Key [" + key.getString().toString() + "] doesn't exist in List")
                rtn
            } catch (e: IndexOutOfBoundsException) {
                throw ExpressionException("Key [" + key.getString().toString() + "] doesn't exist in List")
            }
        } else if (Decision.isNativeArray(coll)) {
            return ArrayUtil.get(coll, Caster.toIntValue(key.getString()) - 1, null)
                    ?: throw ExpressionException("Key [" + key.getString().toString() + "] doesn't exist in Native Array")
        } else if (coll is Node) {
            // print.out("get:"+key);
            return XMLStructFactory.newInstance(coll as Node?, false).get(key)
        } else if (coll is String) {
            if (Decision.isInteger(key.getString())) { // i do the decision call and the caster call, because in most cases the if will be false
                val str = coll as String?
                val index: Int = Caster.toIntValue(key.getString(), -1)
                if (index > 0 && index <= str!!.length()) {
                    return str.substring(index - 1, index)
                }
            }
        }
        // HTTPSession
        /*
		 * else if(coll instanceof HttpSession) { return ((HttpSession)coll).getAttribute(key.getString());
		 * }
		 */

        // Direct Object Access
        if (coll != null && pc.getConfig().getSecurityManager().getAccess(SecurityManager.TYPE_DIRECT_JAVA_ACCESS) === SecurityManager.VALUE_YES) {
            if (doLogReflectionCalls()) LogUtil.log(pc, Log.LEVEL_INFO, "reflection", "get-property:" + key + " from class " + Caster.toTypeName(coll))
            return Reflector.getProperty(coll, key.getString())
        }
        throw ExpressionException("No matching property [" + key.getString().toString() + "] found")
    }

    @Override
    @Throws(PageException::class)
    operator fun get(pc: PageContext?, coll: Object?, key: String?): Object? {
        // Objects
        if (coll is Objects) {
            return (coll as Objects?).get(pc, KeyImpl.init(key))
        } else if (coll is Collection) {
            return (coll as Collection?).get(KeyImpl.init(key))
        } else if (coll is Map) {
            var rtn: Object? = null
            try {
                rtn = (coll as Map?)!!.get(key)
                // if(rtn==null)rtn=((Map)coll).get(MapAsStruct.getCaseSensitiveKey((Map)coll, key));
                if (rtn != null) return rtn
            } catch (t: Throwable) {
                ExceptionUtil.rethrowIfNecessary(t)
            }
            rtn = Reflector.getProperty(coll, key, null)
            if (rtn != null) return rtn
            throw ExpressionException("Key [" + key + "] doesn't exist in Map (" + Caster.toClassName(coll) + ")", "keys are [" + keyList(coll as Map?) + "]")
        } else if (coll is List) {
            return try {
                val rtn = (coll as List?)!![Caster.toIntValue(key) - 1]
                        ?: throw ExpressionException("Key [$key] doesn't exist in List")
                rtn
            } catch (e: IndexOutOfBoundsException) {
                throw ExpressionException("Key [$key] doesn't exist in List")
            }
        } else if (Decision.isNativeArray(coll)) {
            return ArrayUtil.get(coll, Caster.toIntValue(key) - 1, null)
                    ?: throw ExpressionException("Key [$key] doesn't exist in Native Array")
        } else if (coll is Node) {
            return XMLStructFactory.newInstance(coll as Node?, false).get(key)
        }
        // Direct Object Access
        if (pc.getConfig().getSecurityManager().getAccess(SecurityManager.TYPE_DIRECT_JAVA_ACCESS) === SecurityManager.VALUE_YES) {
            if (doLogReflectionCalls()) LogUtil.log(pc, Log.LEVEL_INFO, "reflection", "get-property:" + key + " from class " + Caster.toTypeName(coll))
            return Reflector.getProperty(coll, key)
        }
        throw ExpressionException("No matching property [$key] found")
    }

    private fun keyList(map: Map?): String? {
        val sb = StringBuffer()
        val it: Iterator = map.keySet().iterator()
        while (it.hasNext()) {
            if (sb.length() > 0) sb.append(',')
            sb.append(StringUtil.toStringNative(it.next(), ""))
        }
        return sb.toString()
    }

    @Override
    @Throws(PageException::class)
    operator fun set(pc: PageContext?, coll: Object?, key: Collection.Key?, value: Object?): Object? {
        // Objects
        if (coll is Objects) {
            (coll as Objects?).set(pc, key, value)
            return value
        } else if (coll is Collection) {
            (coll as Collection?).set(key, value)
            return value
        } else if (coll is Map) {
            /*
			 * no idea why this is here try { Reflector.setProperty(coll,key.getString(),value); return value; }
			 * catch(Throwable t) {ExceptionUtil.rethrowIfNecessary(t);}
			 */
            (coll as Map?).put(key.getString(), value)
            return value
        } else if (coll is List) {
            val list: List? = coll
            val index: Int = Caster.toIntValue(key.getString())
            if (list.size() >= index) list.set(index - 1, value) else {
                while (list.size() < index - 1) list.add(null)
                list.add(value)
            }
            return value
        } else if (Decision.isNativeArray(coll)) {
            return try {
                ArrayUtil.set(coll, Caster.toIntValue(key.getString()) - 1, value)
            } catch (e: Exception) {
                throw ExpressionException("Invalid index [" + key.getString().toString() + "] for Native Array, can't expand Native Arrays")
            }
        } else if (coll is Node) {
            return XMLUtil.setProperty(coll as Node?, key, value)
        }
        // Direct Object Access
        if (pc.getConfig().getSecurityManager().getAccess(SecurityManager.TYPE_DIRECT_JAVA_ACCESS) === SecurityManager.VALUE_YES) {
            try {
                if (doLogReflectionCalls()) LogUtil.log(pc, Log.LEVEL_INFO, "reflection", "set-property:" + key + " in class " + Caster.toTypeName(coll))
                Reflector.setProperty(coll, key.getString(), value)
                return value
            } catch (pe: PageException) {
            }
        }
        throw ExpressionException("Can't assign value to an Object of this type [" + Type.getName(coll).toString() + "] with key [" + key.getString().toString() + "]")
    }

    /**
     * @see lucee.runtime.util.VariableUtil.set
     */
    @Override
    @Throws(PageException::class)
    operator fun set(pc: PageContext?, coll: Object?, key: String?, value: Object?): Object? {
        // Objects
        if (coll is Objects) {
            (coll as Objects?).set(pc, KeyImpl.init(key), value)
            return value
        } else if (coll is Collection) {
            (coll as Collection?).set(key, value)
            return value
        } else if (coll is Map) {
            /*
			 * try { Reflector.setProperty(coll,key,value); return value; } catch(Throwable t)
			 * {ExceptionUtil.rethrowIfNecessary(t);}
			 */
            (coll as Map?).put(key, value)
            return value
        } else if (coll is List) {
            val list: List? = coll
            val index: Int = Caster.toIntValue(key)
            if (list.size() >= index) list.set(index - 1, value) else {
                while (list.size() < index - 1) list.add(null)
                list.add(value)
            }
            return value
        } else if (Decision.isNativeArray(coll)) {
            return try {
                ArrayUtil.set(coll, Caster.toIntValue(key) - 1, value)
            } catch (e: Exception) {
                throw ExpressionException("invalid index [$key] for Native Array, can't expand Native Arrays")
            }
        } else if (coll is Node) {
            return XMLUtil.setProperty(coll as Node?, KeyImpl.init(key), value)
        }
        // Direct Object Access
        if (pc.getConfig().getSecurityManager().getAccess(SecurityManager.TYPE_DIRECT_JAVA_ACCESS) === SecurityManager.VALUE_YES) {
            try {
                if (doLogReflectionCalls()) LogUtil.log(pc, Log.LEVEL_INFO, "reflection", "set-property:" + key + " in class " + Caster.toTypeName(coll))
                Reflector.setProperty(coll, key, value)
                return value
            } catch (pe: PageException) {
            }
        }
        throw ExpressionException("Can't assign value to an Object of this type [" + Type.getName(coll).toString() + "] with key [" + key.toString() + "]")
    }

    /**
     *
     * @see lucee.runtime.util.VariableUtil.setEL
     */
    @Override
    fun setEL(pc: PageContext?, coll: Object?, key: String?, value: Object?): Object? {
        // Objects
        if (coll is Objects) {
            (coll as Objects?).setEL(pc, KeyImpl.init(key), value)
            return value
        } else if (coll is Collection) {
            (coll as Collection?).setEL(KeyImpl.init(key), value)
            return value
        } else if (coll is Map) {
            try {
                Reflector.setProperty(coll, key, value)
                return value
            } catch (t: Throwable) {
                ExceptionUtil.rethrowIfNecessary(t)
            }
            (coll as Map?).put(key, value)
            return value
        } else if (coll is List) {
            val list: List? = coll
            val index: Int = Caster.toIntValue(key, Integer.MIN_VALUE)
            if (index == Integer.MIN_VALUE) return null
            if (list.size() >= index) list.set(index - 1, value) else {
                while (list.size() < index - 1) list.add(null)
                list.add(value)
            }
            return value
        } else if (Decision.isNativeArray(coll)) {
            return ArrayUtil.setEL(coll, Caster.toIntValue(key, Integer.MIN_VALUE) - 1, value)
        } else if (coll is Node) {
            return XMLUtil.setPropertyEL(coll as Node?, KeyImpl.init(key), value)
        }
        // Direct Object Access
        if (pc.getConfig().getSecurityManager().getAccess(SecurityManager.TYPE_DIRECT_JAVA_ACCESS) === SecurityManager.VALUE_YES) {
            if (doLogReflectionCalls()) LogUtil.log(pc, Log.LEVEL_INFO, "reflection", "set-property:" + key + " in class " + Caster.toTypeName(coll))
            Reflector.setPropertyEL(coll, key, value)
            return value
        }
        return null
    }

    /**
     * @see lucee.runtime.util.VariableUtil.setEL
     */
    @Override
    fun setEL(pc: PageContext?, coll: Object?, key: Collection.Key?, value: Object?): Object? {
        // Objects
        if (coll is Objects) {
            (coll as Objects?).setEL(pc, key, value)
            return value
        } else if (coll is Collection) {
            (coll as Collection?).setEL(key, value)
            return value
        } else if (coll is Map) {
            try {
                Reflector.setProperty(coll, key.getString(), value)
                return value
            } catch (t: Throwable) {
                ExceptionUtil.rethrowIfNecessary(t)
            }
            (coll as Map?).put(key, value)
            return value
        } else if (coll is List) {
            val list: List? = coll
            val index: Int = Caster.toIntValue(key, Integer.MIN_VALUE)
            if (index == Integer.MIN_VALUE) return null
            if (list.size() >= index) list.set(index - 1, value) else {
                while (list.size() < index - 1) list.add(null)
                list.add(value)
            }
            return value
        } else if (Decision.isNativeArray(coll)) {
            return ArrayUtil.setEL(coll, Caster.toIntValue(key, Integer.MIN_VALUE) - 1, value)
        } else if (coll is Node) {
            return XMLUtil.setPropertyEL(coll as Node?, key, value)
        }
        // Direct Object Access
        if (pc.getConfig().getSecurityManager().getAccess(SecurityManager.TYPE_DIRECT_JAVA_ACCESS) === SecurityManager.VALUE_YES) {
            if (doLogReflectionCalls()) LogUtil.log(pc, Log.LEVEL_INFO, "reflection", "set-property:" + key + " in class " + Caster.toTypeName(coll))
            Reflector.setPropertyEL(coll, key.getString(), value)
            return value
        }
        return null
    }

    @Override
    fun removeEL(coll: Object?, key: String?): Object? {
        // Collection
        if (coll is Collection) {
            return (coll as Collection?).removeEL(KeyImpl.init(key))
        } else if (coll is Map) {
            // if(obj==null)obj=((Map)coll).remove(MapAsStruct.getCaseSensitiveKey((Map)coll, key));
            return (coll as Map?).remove(key)
        } else if (coll is List) {
            val i: Int = Caster.toIntValue(key, Integer.MIN_VALUE)
            return if (i == Integer.MIN_VALUE) null else (coll as List?).remove(i)
        }
        return null
    }

    @Override
    fun removeEL(coll: Object?, key: Collection.Key?): Object? {
        // Collection
        if (coll is Collection) {
            return (coll as Collection?).removeEL(key)
        } else if (coll is Map) {
            // if(obj==null)obj=((Map)coll).remove(MapAsStruct.getCaseSensitiveKey((Map)coll, key));
            return (coll as Map?).remove(key.getString())
        } else if (coll is List) {
            val i: Int = Caster.toIntValue(key, Integer.MIN_VALUE)
            return if (i == Integer.MIN_VALUE) null else (coll as List?).remove(i)
        }
        return null
    }

    /**
     * @see lucee.runtime.util.VariableUtil.remove
     */
    @Override
    @Throws(PageException::class)
    fun remove(coll: Object?, key: String?): Object? {
        // Collection
        if (coll is Collection) {
            return (coll as Collection?).remove(KeyImpl.init(key))
        } else if (coll is Map) {
            // if(obj==null)obj=((Map)coll).remove(MapAsStruct.getCaseSensitiveKey((Map)coll, key));
            return (coll as Map?).remove(key) ?: throw ExpressionException("Can't remove key [$key] from map")
        } else if (coll is List) {
            val i: Int = Caster.toIntValue(key)
            return (coll as List?).remove(i) ?: throw ExpressionException("Can't remove index [$key] from list")
        }
        throw ExpressionException("Can't remove key [" + key + "] from Object of type [" + Caster.toTypeName(coll) + "]")
    }

    @Override
    @Throws(PageException::class)
    fun remove(coll: Object?, key: Collection.Key?): Object? {
        // Collection
        if (coll is Collection) {
            return (coll as Collection?).remove(key)
        } else if (coll is Map) {
            // if(obj==null)obj=((Map)coll).remove(MapAsStruct.getCaseSensitiveKey((Map)coll, key));
            return (coll as Map?).remove(key.getString())
                    ?: throw ExpressionException("Can't remove key [$key] from map")
        } else if (coll is List) {
            val i: Int = Caster.toIntValue(key)
            return (coll as List?).remove(i) ?: throw ExpressionException("can't remove index [$key] from list")
        }
        throw ExpressionException("Can't remove key [" + key + "] from Object of type [" + Caster.toTypeName(coll) + "]")
    }

    /**
     * @see lucee.runtime.util.VariableUtil.callFunction
     */
    @Override
    @Throws(PageException::class)
    fun callFunction(pc: PageContext?, coll: Object?, key: String?, args: Array<Object?>?): Object? {
        return if (args!!.size > 0 && args[0] is FunctionValue) callFunctionWithNamedValues(pc, coll, key, args) else callFunctionWithoutNamedValues(pc, coll, key, args)
    }

    /**
     * @see lucee.runtime.util.VariableUtil.callFunctionWithoutNamedValues
     */
    @Override
    @Throws(PageException::class)
    fun callFunctionWithoutNamedValues(pc: PageContext?, coll: Object?, key: String?, args: Array<Object?>?): Object? {
        return callFunctionWithoutNamedValues(pc, coll, KeyImpl.init(key), args)
    }

    @Override
    @Throws(PageException::class)
    fun callFunctionWithoutNamedValues(pc: PageContext?, coll: Object?, key: Collection.Key?, args: Array<Object?>?): Object? {
        // Objects
        if (coll is Objects) {
            return (coll as Objects?).call(pc, key, args)
        }
        // call UDF
        val prop: Object = getLight(pc, coll, key, null)
        if (prop is UDF) {
            return (prop as UDF).call(pc, key, args, false)
        }
        // Strings
        if (coll is String) {
            return MemberUtil.call(pc, coll, key, args, shortArrayOf(CFTypes.TYPE_STRING), arrayOf<String?>("string"))
        }
        // Map || XML
        if (coll is Map) {
            return if (coll is Node) MemberUtil.call(pc, coll, key, args, shortArrayOf(CFTypes.TYPE_XML, CFTypes.TYPE_STRUCT), arrayOf<String?>("xml", "struct")) else MemberUtil.call(pc, coll, key, args, shortArrayOf(CFTypes.TYPE_STRUCT), arrayOf<String?>("struct"))
        }
        // List
        if (coll is List) {
            return MemberUtil.call(pc, coll, key, args, shortArrayOf(CFTypes.TYPE_ARRAY), arrayOf<String?>("array"))
        }
        // Date
        if (coll is Date) {
            return MemberUtil.call(pc, coll, key, args, shortArrayOf(CFTypes.TYPE_DATETIME), arrayOf<String?>("date"))
        }
        // Boolean
        if (coll is Boolean) {
            return MemberUtil.call(pc, coll, key, args, shortArrayOf(CFTypes.TYPE_BOOLEAN), arrayOf<String?>("boolean"))
        }
        // Number
        if (coll is Number) {
            return MemberUtil.call(pc, coll, key, args, shortArrayOf(CFTypes.TYPE_NUMERIC), arrayOf<String?>("numeric"))
        }
        // Locale
        if (coll is Locale) {
            return MemberUtil.call(pc, coll, key, args, shortArrayOf(CFTypes.TYPE_LOCALE), arrayOf<String?>("locale"))
        }
        // TimeZone
        if (coll is TimeZone) {
            return MemberUtil.call(pc, coll, key, args, shortArrayOf(CFTypes.TYPE_TIMEZONE), arrayOf<String?>("timezone"))
        }

        // call Object Wrapper
        if (pc.getConfig().getSecurityManager().getAccess(SecurityManager.TYPE_DIRECT_JAVA_ACCESS) === SecurityManager.VALUE_YES) {
            if (doLogReflectionCalls()) LogUtil.log(pc, Log.LEVEL_INFO, "reflection", "call-method:" + key + " from class " + Caster.toTypeName(coll))
            if (coll !is Undefined) return Reflector.callMethod(coll, key, args)
        }
        throw ExpressionException("No matching Method/Function for " + key + "(" + Reflector.getDspMethods(Reflector.getClasses(args)) + ")")
    }

    // FUTURE add to interface
    fun callFunctionWithoutNamedValues(pc: PageContext?, coll: Object?, key: Collection.Key?, args: Array<Object?>?, noNull: Boolean, defaultValue: Object?): Object? {
        // MUST make an independent impl for performance reasons
        return try {
            if (!noNull || NullSupportHelper.full(pc)) return callFunctionWithoutNamedValues(pc, coll, key, args)
            val obj: Object = callFunctionWithoutNamedValues(pc, coll, key, args)
            if (obj == null) defaultValue else obj
        } catch (t: Throwable) {
            ExceptionUtil.rethrowIfNecessary(t)
            defaultValue
        }
    }

    /**
     * @see lucee.runtime.util.VariableUtil.callFunctionWithNamedValues
     */
    @Override
    @Throws(PageException::class)
    fun callFunctionWithNamedValues(pc: PageContext?, coll: Object?, key: String?, args: Array<Object?>?): Object? {
        return callFunctionWithNamedValues(pc, coll, KeyImpl.init(key), args)
    }

    @Override
    @Throws(PageException::class)
    fun callFunctionWithNamedValues(pc: PageContext?, coll: Object?, key: Collection.Key?, args: Array<Object?>?): Object? {
        // Objects
        if (coll is Objects) {
            return (coll as Objects?).callWithNamedValues(pc, key, Caster.toFunctionValues(args))
        }
        // call UDF
        val prop: Object = getLight(pc, coll, key, null)
        if (prop is UDF) {
            return (prop as UDF).callWithNamedValues(pc, key, Caster.toFunctionValues(args), false)
        }

        // Strings
        if (coll is String) {
            return MemberUtil.callWithNamedValues(pc, coll, key, Caster.toFunctionValues(args), CFTypes.TYPE_STRING, "string")
        }
        throw ExpressionException("No matching Method/Function [$key] for call with named arguments found ")
    }

    // FUTURE add to interface
    fun callFunctionWithNamedValues(pc: PageContext?, coll: Object?, key: Collection.Key?, args: Array<Object?>?, noNull: Boolean, defaultValue: Object?): Object? {
        // MUST make an independent impl for performance reasons
        return try {
            if (!noNull || NullSupportHelper.full(pc)) return callFunctionWithNamedValues(pc, coll, key, args)
            val obj: Object = callFunctionWithNamedValues(pc, coll, key, args)
            if (obj == null) defaultValue else obj
        } catch (t: Throwable) {
            ExceptionUtil.rethrowIfNecessary(t)
            defaultValue
        }
    }

    @Override
    @Throws(PageException::class)
    fun callFunctionWithNamedValues(pc: PageContext?, coll: Object?, key: Collection.Key?, args: Struct?): Object? {
        // Objects
        if (coll is Objects) {
            return (coll as Objects?).callWithNamedValues(pc, key, args)
        }
        // call UDF
        val prop: Object = getLight(pc, coll, key, null)
        if (prop is UDF) {
            return (prop as UDF).callWithNamedValues(pc, key, args, false)
        }
        throw ExpressionException("No matching Method/Function for call with named arguments found")
    }

    companion object {
        private var _logReflectionCalls: Boolean? = null
        fun doLogReflectionCalls(): Boolean {
            if (_logReflectionCalls == null) _logReflectionCalls = Caster.toBoolean(lucee.commons.io.SystemUtil.getSystemPropOrEnvVar("lucee.log.reflection", null), Boolean.FALSE)
            return _logReflectionCalls.booleanValue()
        }

        // used by generated bytecode
        @Throws(PageException::class)
        fun recordcount(pc: PageContext?, obj: Object?): Object? {
            return if (obj is Query) Caster.toDouble((obj as Query?).getRecordcount()) else pc.getCollection(obj, KeyConstants._RECORDCOUNT)
        }

        // used by generated bytecode
        @Throws(PageException::class)
        fun currentrow(pc: PageContext?, obj: Object?): Object? {
            return if (obj is Query) Caster.toDouble((obj as Query?).getCurrentrow(pc.getId())) else pc.getCollection(obj, KeyConstants._CURRENTROW)
        }

        // used by generated bytecode
        @Throws(PageException::class)
        fun columnlist(pc: PageContext?, obj: Object?): Object? {
            if (obj is Query) {
                val columnNames: Array<Key?> = (obj as Query?).getColumnNames()
                val upperCase = pc.getCurrentTemplateDialect() === CFMLEngine.DIALECT_CFML
                val sb = StringBuilder()
                for (i in columnNames.indices) {
                    if (i > 0) sb.append(',')
                    sb.append(if (upperCase) columnNames[i].getUpperString() else columnNames[i].getString())
                }
                return sb.toString()
            }
            return pc.getCollection(obj, KeyConstants._COLUMNLIST)
        }
    }
}