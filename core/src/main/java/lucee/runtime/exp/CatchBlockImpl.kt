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
package lucee.runtime.exp

import java.io.Serializable

class CatchBlockImpl private constructor(pe: PageException?, level: Int) : StructImpl(), CatchBlock, Castable, Objects {
    private val exception: PageException?

    constructor(pe: PageException?) : this(pe, 0) {}

    internal inner class SpecialItem(key: Key?, level: Int) : Serializable {
        private val key: Key?
        private val level: Int
        fun get(): Object? {
            if (level < Companion.MAX) {
                if (key === CAUSE) return causeAsCatchBlock
                if (key === ADDITIONAL) return exception.getAdditional()
            }
            if (key === KeyConstants._Message) return StringUtil.emptyIfNull(exception.getMessage())
            if (key === KeyConstants._Detail) return StringUtil.emptyIfNull(exception.getDetail())
            if (key === ERROR_CODE) return StringUtil.emptyIfNull(exception.getErrorCode())
            if (key === EXTENDEDINFO) return StringUtil.emptyIfNull(exception.getExtendedInfo())
            if (key === EXTENDED_INFO) return StringUtil.emptyIfNull(exception.getExtendedInfo())
            if (key === KeyConstants._type) return StringUtil.emptyIfNull(exception.getTypeAsString())
            if (key === STACK_TRACE) return StringUtil.emptyIfNull(exception.getStackTraceAsString())
            return if (key === TAG_CONTEXT && exception is PageExceptionImpl) (exception as PageExceptionImpl?)!!.getTagContext(ThreadLocalPageContext.getConfig()) else null
        }

        private val causeAsCatchBlock: CatchBlock?
            private get() {
                val cause: Throwable = exception.getCause()
                if (cause == null || exception === cause) return null
                return if (exception is NativeException && (exception as NativeException?).getException() === cause) null else CatchBlockImpl(NativeException.newInstance(cause), level + 1)
            }

        fun set(o: Object?) {
            try {
                if (o !is Pair) {
                    if (key === KeyConstants._Detail) {
                        exception.setDetail(Caster.toString(o))
                        return
                    } else if (key === ERROR_CODE) {
                        exception.setErrorCode(Caster.toString(o))
                        return
                    } else if (key === EXTENDEDINFO || key === EXTENDED_INFO) {
                        exception.setExtendedInfo(Caster.toString(o))
                        return
                    } else if (key === STACK_TRACE) {
                        if (o is Array<StackTraceElement>) {
                            exception.setStackTrace(o as Array<StackTraceElement?>?)
                            return
                        } else if (Decision.isCastableToArray(o)) {
                            val arr: Array<Object?> = Caster.toNativeArray(o)
                            val elements: Array<StackTraceElement?> = arrayOfNulls<StackTraceElement?>(arr.size)
                            for (i in arr.indices) {
                                if (arr[i] is StackTraceElement) elements[i] = arr[i] as StackTraceElement? else throw CasterException(o, Array<StackTraceElement>::class.java)
                            }
                            exception.setStackTrace(elements)
                            return
                        }
                    }
                }
            } catch (pe: PageException) {
            }
            superSetEL(key, o)
        }

        fun remove(): Object? {
            var rtn: Object? = null
            if (key === KeyConstants._Detail) {
                rtn = exception.getDetail()
                exception.setDetail("")
            } else if (key === ERROR_CODE) {
                rtn = exception.getErrorCode()
                exception.setErrorCode("0")
            } else if (key === EXTENDEDINFO || key === EXTENDED_INFO) {
                rtn = exception.getExtendedInfo()
                exception.setExtendedInfo(null)
            }
            return rtn
        }

        companion object {
            private const val MAX = 10
        }

        init {
            this.key = key
            this.level = level
        }
    }

    /**
     * @return the pe
     */
    @get:Override
    val pageException: PageException?
        get() = exception

    @Override
    @Throws(ExpressionException::class)
    fun castToString(): String? {
        return castToString(null)
    }

    @Override
    fun castToString(defaultValue: String?): String? {
        val pc: PageContext = ThreadLocalPageContext.get()
        if (pc is PageContextImpl) {
            try {
                return PageContextUtil.getHandlePageException(pc as PageContextImpl, exception)
            } catch (e: PageException) {
            }
        }
        return exception.getClass().getName()
    }

    @Override
    fun containsValue(value: Object?): Boolean {
        val keys: Array<Key?>? = keys()
        for (i in keys.indices) {
            if (get(keys!![i], null) === value) return true
        }
        return false
    }

    @Override
    fun duplicate(deepCopy: Boolean): Collection? {
        val sct: Struct = StructImpl()
        StructUtil.copy(this, sct, true)
        return sct
    }

    @Override
    fun entrySet(): Set? {
        return StructUtil.entrySet(this)
    }

    @Override
    fun print(pc: PageContext?) {
        pc.handlePageException(exception)
    }

    @Override
    operator fun get(key: Key?, defaultValue: Object?): Object? {
        val value: Object = super.get(key, defaultValue)
        if (value is SpecialItem) {
            return (value as SpecialItem).get()
        } else if (value is Pair) {
            val pair = value as Pair
            return try {
                val res: Object = pair.getter.invoke(pair.throwable, arrayOf<Object?>())
                if (pair.doEmptyStringWhenNull && res == null) "" else res
            } catch (e: Exception) {
                defaultValue
            }
        }
        return value
    }

    @Override
    @Throws(PageException::class)
    operator fun set(key: Key?, value: Object?): Object? {
        val curr: Object = super.get(key, null)
        if (curr is SpecialItem) {
            (curr as SpecialItem).set(value)
            return value
        } else if (curr is Pair) {
            val pair = curr as Pair
            val setter: MethodInstance = Reflector.getSetter(pair.throwable, pair.name.getString(), value, null)
            if (setter != null) {
                return try {
                    setter.invoke(pair.throwable)
                    value
                } catch (e: Exception) {
                    throw Caster.toPageException(e)
                }
            }
        }
        return super.set(key, value)
    }

    @Override
    fun setEL(key: Key?, value: Object?): Object? {
        val curr: Object = super.get(key, null)
        if (curr is SpecialItem) {
            (curr as SpecialItem).set(value)
            return value
        } else if (curr is Pair) {
            val pair = curr as Pair
            val setter: MethodInstance = Reflector.getSetter(pair.throwable, pair.name.getString(), value, null)
            if (setter != null) {
                try {
                    setter.invoke(pair.throwable)
                } catch (e: Exception) {
                }
                return value
            }
        }
        return super.setEL(key, value)
    }

    private fun superSetEL(key: Key?, value: Object?): Object? {
        return super.setEL(key, value)
    }

    @Override
    fun size(): Int {
        return keys()!!.size
    }

    @Override
    fun keys(): Array<Key?>? {
        val keys: Array<Key?> = super.keys()
        val list: List<Key?> = ArrayList<Key?>()
        for (i in keys.indices) {
            if (get(keys[i], null) != null) list.add(keys[i])
        }
        return list.toArray(arrayOfNulls<Key?>(list.size()))
    }

    @Override
    @Throws(PageException::class)
    fun remove(key: Key?): Object? {
        val curr: Object = super.get(key, null)
        if (curr is SpecialItem) {
            return (curr as SpecialItem).remove()
        } else if (curr is Pair) {
            val pair = curr as Pair
            val setter: MethodInstance = Reflector.getSetter(pair.throwable, pair.name.getString(), null, null)
            if (setter != null) {
                return try {
                    val before: Object = pair.getter.invoke(pair.throwable, arrayOfNulls<Object?>(0))
                    setter.invoke(pair.throwable)
                    before
                } catch (e: Exception) {
                    throw Caster.toPageException(e)
                }
            }
        }
        return super.remove(key)
    }

    @Override
    fun keyIterator(): Iterator<Collection.Key?>? {
        return KeyIterator(keys())
    }

    @Override
    fun keysAsStringIterator(): Iterator<String?>? {
        return StringIterator(keys())
    }

    @Override
    fun entryIterator(): Iterator<Entry<Key?, Object?>?>? {
        return EntryIterator(this, keys())
    }

    @Override
    fun valueIterator(): Iterator<Object?>? {
        return ValueIterator(this, keys())
    }

    @Override
    fun values(): Collection<*>? {
        return StructUtil.values(this)
    }

    @Override
    fun toDumpData(pageContext: PageContext?, maxlevel: Int, dp: DumpProperties?): DumpData? {
        return StructUtil.toDumpTable(this, "Catch", pageContext, maxlevel, dp)
    }

    internal inner class Pair(throwable: Throwable?, name: Key?, method: Method?, doEmptyStringWhenNull: Boolean) {
        var throwable: Throwable? = null
        var name: Collection.Key? = null
        var getter: Method? = null
        val doEmptyStringWhenNull = false

        constructor(throwable: Throwable?, name: String?, method: Method?, doEmptyStringWhenNull: Boolean) : this(throwable, KeyImpl.init(name), method, doEmptyStringWhenNull) {}

        @Override
        override fun toString(): String {
            return try {
                Caster.toString(getter.invoke(throwable, arrayOf<Object?>()))
            } catch (e: Exception) {
                throw PageRuntimeException(Caster.toPageException(e))
            }
        }

        init {
            this.throwable = throwable
            this.name = name
            getter = method
            this.doEmptyStringWhenNull = doEmptyStringWhenNull
        }
    }

    @Throws(PageException::class)
    fun call(pc: PageContext?, methodName: String?, arguments: Array<Object?>?): Object? {
        var obj: Object? = exception
        if (exception is NativeException) obj = (exception as NativeException?).getException()
        if ("dump".equalsIgnoreCase(methodName)) {
            print(pc)
            return null
        }
        return MemberUtil.call(pc, this, KeyImpl.init(methodName), arguments, shortArrayOf(CFTypes.TYPE_STRUCT), arrayOf<String?>("struct"))

        /*
		 * try{ return Reflector.callMethod(obj, methodName, arguments); } catch(PageException e){ return
		 * Reflector.callMethod(exception, methodName, arguments); }
		 */
    }

    @Throws(PageException::class)
    fun callWithNamedValues(pc: PageContext?, methodName: String?, args: Struct?): Object? {
        throw ApplicationException("Named arguments not supported")
    }

    @Override
    @Throws(PageException::class)
    fun callWithNamedValues(pc: PageContext?, methodName: Key?, args: Struct?): Object? {
        throw ApplicationException("Named arguments not supported")
    }

    val isInitalized: Boolean
        get() = true

    @Throws(PageException::class)
    operator fun set(pc: PageContext?, propertyName: String?, value: Object?): Object? {
        return set(propertyName, value)
    }

    @Override
    @Throws(PageException::class)
    operator fun set(pc: PageContext?, propertyName: Key?, value: Object?): Object? {
        return set(propertyName, value)
    }

    fun setEL(pc: PageContext?, propertyName: String?, value: Object?): Object? {
        return setEL(propertyName, value)
    }

    @Override
    fun setEL(pc: PageContext?, propertyName: Key?, value: Object?): Object? {
        return setEL(propertyName, value)
    }

    @Override
    @Throws(PageException::class)
    operator fun get(key: Key?): Object? {
        val res: Object = get(key, CollectionUtil.NULL)
        if (res !== CollectionUtil.NULL) return res
        throw StructSupport.invalidKey(null, this, key, "catch block")
    }

    operator fun get(pc: PageContext?, key: String?, defaultValue: Object?): Object? {
        return get(key, defaultValue)
    }

    @Override
    operator fun get(pc: PageContext?, key: Key?, defaultValue: Object?): Object? {
        return get(key, defaultValue)
    }

    @Throws(PageException::class)
    operator fun get(pc: PageContext?, key: String?): Object? {
        return get(key)
    }

    @Override
    @Throws(PageException::class)
    operator fun get(pc: PageContext?, key: Key?): Object? {
        return get(key)
    }

    @Override
    @Throws(PageException::class)
    fun call(pc: PageContext?, methodName: Key?, arguments: Array<Object?>?): Object? {
        return call(pc, methodName.getString(), arguments)
    }

    /*
	 * public Object remove (String key) throws PageException { return remove(KeyImpl.init(key)); }
	 */
    @Override
    fun removeEL(key: Key?): Object? {
        return try {
            remove(key)
        } catch (e: PageException) {
            null
        }
    }

    companion object {
        private const val serialVersionUID = -3680961614605720352L
        val ERROR_CODE: Key? = KeyImpl.getInstance("ErrorCode")
        val CAUSE: Key? = KeyConstants._Cause
        val EXTENDEDINFO: Key? = KeyImpl.getInstance("ExtendedInfo")
        val EXTENDED_INFO: Key? = KeyImpl.getInstance("Extended_Info")
        val TAG_CONTEXT: Key? = KeyImpl.getInstance("TagContext")
        val STACK_TRACE: Key? = KeyImpl.getInstance("StackTrace")
        val ADDITIONAL: Key? = KeyImpl.getInstance("additional")
    }

    init {
        exception = pe
        setEL(KeyConstants._Message, SpecialItem(KeyConstants._Message, level))
        setEL(KeyConstants._Detail, SpecialItem(KeyConstants._Detail, level))
        setEL(ERROR_CODE, SpecialItem(ERROR_CODE, level))
        setEL(EXTENDEDINFO, SpecialItem(EXTENDEDINFO, level))
        setEL(EXTENDED_INFO, SpecialItem(EXTENDED_INFO, level))
        setEL(ADDITIONAL, SpecialItem(ADDITIONAL, level))
        setEL(TAG_CONTEXT, SpecialItem(TAG_CONTEXT, level))
        setEL(KeyConstants._type, SpecialItem(KeyConstants._type, level))
        setEL(STACK_TRACE, SpecialItem(STACK_TRACE, level))
        setEL(CAUSE, SpecialItem(CAUSE, level))
        if (pe is NativeException) {
            val throwable: Throwable = (pe as NativeException?).getException()
            val mGetters: Array<Method?> = Reflector.getGetters(throwable.getClass())
            var getter: Method?
            var key: Collection.Key
            if (!ArrayUtil.isEmpty(mGetters)) {
                for (i in mGetters.indices) {
                    getter = mGetters[i]
                    if (getter.getDeclaringClass() === Throwable::class.java) {
                        continue
                    }
                    key = KeyImpl.init(Reflector.removeGetterPrefix(getter.getName()))
                    if (KeyConstants._Message.equalsIgnoreCase(key) || KeyConstants._Detail.equalsIgnoreCase(key)) {
                        if (getter.getReturnType() !== String::class.java) continue
                    } else if (STACK_TRACE.equalsIgnoreCase(key) || KeyConstants._type.equalsIgnoreCase(key) || CAUSE.equalsIgnoreCase(key)) continue
                    setEL(key, Pair(throwable, key, getter, false))
                }
            }
        }
    }
}