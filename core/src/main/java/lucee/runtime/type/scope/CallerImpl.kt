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
package lucee.runtime.type.scope

import java.util.Iterator

/**
 * caller scope
 */
class CallerImpl : StructSupport(), Caller {
    private var pc: PageContext? = null
    private var variablesScope: Variables? = null
    private var localScope: Local? = null
    private var argumentsScope: Argument? = null
    private var checkArgs = false

    @Override
    @Throws(PageException::class)
    operator fun get(key: Collection.Key?): Object? {
        return get(pc, key)
    }

    @Override
    @Throws(PageException::class)
    operator fun get(pc: PageContext?, key: Collection.Key?): Object? {
        val c: Char = key.lowerCharAt(0)
        if ('a' == c) {
            if (KeyConstants._application.equalsIgnoreCase(key)) return pc.applicationScope() else if (checkArgs && KeyConstants._arguments.equalsIgnoreCase(key)) return argumentsScope // pc.argumentsScope();
        } else if ('c' == c) {
            if (KeyConstants._cgi.equalsIgnoreCase(key)) return pc.cgiScope()
            if (KeyConstants._cookie.equalsIgnoreCase(key)) return pc.cookieScope()
            if (KeyConstants._client.equalsIgnoreCase(key)) return pc.clientScope()
            if (KeyConstants._cluster.equalsIgnoreCase(key)) return pc.clusterScope()
        } else if ('f' == c) {
            if (KeyConstants._form.equalsIgnoreCase(key)) return pc.formScope()
        } else if ('r' == c) {
            if (KeyConstants._request.equalsIgnoreCase(key)) return pc.requestScope()
        } else if ('l' == c) {
            if (KeyConstants._local.equalsIgnoreCase(key) && checkArgs) return localScope // pc.localScope();
        } else if ('s' == c) {
            if (KeyConstants._session.equalsIgnoreCase(key)) return pc.sessionScope()
            if (KeyConstants._server.equalsIgnoreCase(key)) return pc.serverScope()
        } else if ('u' == c) {
            if (KeyConstants._url.equalsIgnoreCase(key)) return pc.urlScope()
        } else if ('v' == c) {
            if (KeyConstants._variables.equalsIgnoreCase(key)) return variablesScope
        }

        // upper variable scope
        var o: Object
        val _null: Object = NullSupportHelper.NULL(pc)
        if (checkArgs) {
            o = localScope.get(key, _null)
            if (o !== _null) return o
            o = argumentsScope.get(key, _null)
            if (o !== _null) return o
        }
        o = variablesScope.get(key, _null)
        if (o !== _null) return o

        // get from cascaded scopes
        o = (pc.undefinedScope() as UndefinedImpl)!!.getCascading(key, _null)
        if (o !== _null) return o
        throw ExpressionException("[" + key.getString().toString() + "] not found in caller scope")
    }

    @Override
    operator fun get(key: Collection.Key?, defaultValue: Object?): Object? {
        return get(pc, key, defaultValue)
    }

    @Override
    operator fun get(pc: PageContext?, key: Collection.Key?, defaultValue: Object?): Object? {
        val c: Char = key.lowerCharAt(0)
        if ('a' == c) {
            if (KeyConstants._application.equalsIgnoreCase(key)) {
                try {
                    return pc.applicationScope()
                } catch (e: PageException) {
                }
            } else if (checkArgs && KeyConstants._arguments.equalsIgnoreCase(key)) return argumentsScope // pc.argumentsScope();
        } else if ('c' == c) {
            if (KeyConstants._cgi.equalsIgnoreCase(key)) return pc.cgiScope()
            if (KeyConstants._cookie.equalsIgnoreCase(key)) return pc.cookieScope()
            if (KeyConstants._client.equalsIgnoreCase(key)) {
                try {
                    return pc.clientScope()
                } catch (e: PageException) {
                }
            }
            if (KeyConstants._cluster.equalsIgnoreCase(key)) {
                try {
                    return pc.clusterScope()
                } catch (e: PageException) {
                }
            }
        } else if ('f' == c) {
            if (KeyConstants._form.equalsIgnoreCase(key)) return pc.formScope()
        } else if ('r' == c) {
            if (KeyConstants._request.equalsIgnoreCase(key)) return pc.requestScope()
        } else if ('l' == c) {
            if (checkArgs && KeyConstants._local.equalsIgnoreCase(key)) return localScope // pc.localScope();
        } else if ('s' == c) {
            if (KeyConstants._session.equalsIgnoreCase(key)) {
                try {
                    return pc.sessionScope()
                } catch (e: PageException) {
                }
            }
            if (KeyConstants._server.equalsIgnoreCase(key)) {
                try {
                    return pc.serverScope()
                } catch (e: PageException) {
                }
            }
        } else if ('u' == c) {
            if (KeyConstants._url.equalsIgnoreCase(key)) return pc.urlScope()
        } else if ('v' == c) {
            if (KeyConstants._variables.equalsIgnoreCase(key)) return variablesScope
        }
        val _null: Object = NullSupportHelper.NULL(pc)
        var o: Object
        if (checkArgs) {
            o = localScope.get(key, _null)
            if (o !== _null) return o
            o = argumentsScope.get(key, _null)
            if (o !== _null) return o
        }
        o = variablesScope.get(key, _null)
        if (o !== _null) return o

        // get from cascaded scopes
        o = (pc.undefinedScope() as UndefinedImpl)!!.getCascading(key, _null)
        return if (o !== _null) o else defaultValue
    }

    @Override
    fun initialize(pc: PageContext?) {
        this.pc = pc
    }

    @Override
    fun setScope(variablesScope: Variables?, localScope: Local?, argumentsScope: Argument?, checkArgs: Boolean) {
        this.variablesScope = variablesScope
        this.localScope = localScope
        this.argumentsScope = argumentsScope
        this.checkArgs = checkArgs
    }

    @Override
    fun isInitalized(): Boolean {
        return pc != null
    }

    @Override
    fun release(pc: PageContext?) {
        this.pc = null
    }

    @Override
    fun size(): Int {
        return variablesScope.size()
    }

    @Override
    fun keys(): Array<Collection.Key?>? {
        return CollectionUtil.keys(this)
    }

    @Override
    @Throws(PageException::class)
    fun remove(key: Collection.Key?): Object? {
        return if (checkArgs && localScope.containsKey(key)) localScope.remove(key) else variablesScope.remove(key)
    }

    @Override
    fun removeEL(key: Collection.Key?): Object? {
        return if (checkArgs && localScope.containsKey(key)) localScope.removeEL(key) else variablesScope.removeEL(key)
    }

    @Override
    fun clear() {
        variablesScope.clear()
    }

    @Override
    @Throws(PageException::class)
    operator fun set(key: Key?, value: Object?): Object? {
        if (checkArgs) {
            if (localScope.containsKey(key)) return localScope.set(key, value)
            if (argumentsScope.containsKey(key)) return argumentsScope.set(key, value)
        }
        return variablesScope.set(key, value)
    }

    @Override
    fun setEL(key: Key?, value: Object?): Object? {
        if (checkArgs) {
            if (localScope.containsKey(key)) return localScope.setEL(key, value)
            if (argumentsScope.containsKey(key)) return argumentsScope.setEL(key, value)
        }
        return variablesScope.setEL(key, value)
    }

    @Override
    fun keyIterator(): Iterator<Collection.Key?>? {
        return variablesScope.keyIterator()
    }

    @Override
    fun keysAsStringIterator(): Iterator<String?>? {
        return variablesScope.keysAsStringIterator()
    }

    @Override
    fun entryIterator(): Iterator<Entry<Key?, Object?>?>? {
        return variablesScope.entryIterator()
    }

    @Override
    fun valueIterator(): Iterator<Object?>? {
        return variablesScope.valueIterator()
    }

    @Override
    fun duplicate(deepCopy: Boolean): Collection? {
        return Duplicator.duplicate(variablesScope, deepCopy)
    }

    @Override
    fun containsKey(key: Collection.Key?): Boolean {
        return get(key, null) != null
    }

    @Override
    fun containsKey(pc: PageContext?, key: Collection.Key?): Boolean {
        return get(key, null) != null
    }

    @Override
    fun toDumpData(pageContext: PageContext?, maxlevel: Int, dp: DumpProperties?): DumpData? {
        var maxlevel = maxlevel
        return variablesScope.toDumpData(pageContext, --maxlevel, dp)
    }

    @Override
    @Throws(PageException::class)
    fun castToString(): String? {
        return variablesScope.castToString()
    }

    @Override
    fun castToString(defaultValue: String?): String? {
        return variablesScope.castToString(defaultValue)
    }

    @Override
    @Throws(PageException::class)
    fun castToBooleanValue(): Boolean {
        return variablesScope.castToBooleanValue()
    }

    @Override
    fun castToBoolean(defaultValue: Boolean?): Boolean? {
        return variablesScope.castToBoolean(defaultValue)
    }

    @Override
    @Throws(PageException::class)
    fun castToDoubleValue(): Double {
        return variablesScope.castToDoubleValue()
    }

    @Override
    fun castToDoubleValue(defaultValue: Double): Double {
        return variablesScope.castToDoubleValue(defaultValue)
    }

    @Override
    @Throws(PageException::class)
    fun castToDateTime(): DateTime? {
        return variablesScope.castToDateTime()
    }

    @Override
    fun castToDateTime(defaultValue: DateTime?): DateTime? {
        return variablesScope.castToDateTime(defaultValue)
    }

    @Override
    @Throws(PageException::class)
    operator fun compareTo(b: Boolean): Int {
        return variablesScope.compareTo(b)
    }

    @Override
    @Throws(PageException::class)
    operator fun compareTo(dt: DateTime?): Int {
        return variablesScope.compareTo(dt)
    }

    @Override
    @Throws(PageException::class)
    operator fun compareTo(d: Double): Int {
        return variablesScope.compareTo(d)
    }

    @Override
    @Throws(PageException::class)
    operator fun compareTo(str: String?): Int {
        return variablesScope.compareTo(str)
    }

    @Override
    fun getType(): Int {
        return SCOPE_CALLER
    }

    @Override
    fun getTypeAsString(): String? {
        return "caller"
    }

    @Override
    fun containsValue(value: Object?): Boolean {
        return variablesScope.containsValue(value)
    }

    @Override
    fun values(): Collection<*>? {
        return variablesScope.values()
    }

    @Override
    fun getVariablesScope(): Variables? {
        return variablesScope
    }

    @Override
    fun getLocalScope(): Local? {
        return localScope
    }

    @Override
    fun getArgumentsScope(): Argument? {
        return argumentsScope
    }

    companion object {
        private const val serialVersionUID = -6228400815042475435L
    }
}