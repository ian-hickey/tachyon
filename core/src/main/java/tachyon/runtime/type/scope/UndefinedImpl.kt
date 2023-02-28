/**
 * Copyright (c) 2023, TachyonCFML.org
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
package tachyon.runtime.type.scope

import java.util.ArrayList

/**
 * Undefined Scope
 */
class UndefinedImpl(pc: PageContextImpl?, private var type: Short) : StructSupport(), Undefined, Objects {
    private var scopes: Array<Scope?>?
    private var qryStack: QueryStackImpl? = QueryStackImpl()
    private var variable: Variables? = null

    // private boolean allowImplicidQueryCall;
    private var checkArguments = false
    private var localAlways = false
    private var isInit = false
    private var local: Local? = null
    private var argument: Argument? = null
    private var pc: PageContextImpl?
    private var debug = false
    @Override
    fun localScope(): Local? {
        return local
    }

    @Override
    fun argumentsScope(): Argument? {
        return argument
    }

    @Override
    fun variablesScope(): Variables? {
        return variable
    }

    @Override
    fun setMode(mode: Int): Int {
        var m: Int = Undefined.MODE_NO_LOCAL_AND_ARGUMENTS
        if (checkArguments) {
            m = if (localAlways) Undefined.MODE_LOCAL_OR_ARGUMENTS_ALWAYS else Undefined.MODE_LOCAL_OR_ARGUMENTS_ONLY_WHEN_EXISTS
        }
        checkArguments = mode != Undefined.MODE_NO_LOCAL_AND_ARGUMENTS
        localAlways = mode == Undefined.MODE_LOCAL_OR_ARGUMENTS_ALWAYS
        return m
    }

    @Override
    fun getLocalAlways(): Boolean {
        return localAlways
    }

    @Override
    fun setFunctionScopes(local: Local?, argument: Argument?) {
        this.local = local
        this.argument = argument
    }

    @Override
    fun getQueryStack(): QueryStack? {
        return qryStack
    }

    @Override
    fun setQueryStack(qryStack: QueryStack?) {
        this.qryStack = qryStack as QueryStackImpl?
    }

    @Override
    fun addQuery(qry: Query?) {
        // if (allowImplicidQueryCall)
        qryStack.addQuery(qry)
    }

    @Override
    fun removeQuery() {
        // if (allowImplicidQueryCall)
        qryStack.removeQuery()
    }

    @Override
    fun size(): Int {
        return variable.size()
    }

    @Override
    fun keys(): Array<Collection.Key?>? {
        return CollectionUtil.keys(variable)
    }

    @Override
    @Throws(PageException::class)
    fun remove(key: Collection.Key?): Object? {
        return if (checkArguments && local.containsKey(key)) local.remove(key) else variable.remove(key)
    }

    @Override
    fun removeEL(key: Collection.Key?): Object? {
        return if (checkArguments && local.containsKey(key)) local.removeEL(key) else variable.removeEL(key)
    }

    @Override
    fun clear() {
        variable.clear()
    }

    @Override
    @Throws(PageException::class)
    operator fun get(key: Collection.Key?): Object? {
        return get(pc, key)
    }

    @Override
    @Throws(PageException::class)
    operator fun get(pc: PageContext?, key: Collection.Key?): Object? {
        val _null: Object = CollectionUtil.NULL
        var rtn: Object
        if (checkArguments) {
            rtn = local.get(pc, key, _null)
            if (rtn !== _null) return rtn
            rtn = argument.getFunctionArgument(key, _null)
            if (rtn !== _null) {
                if (debug) debugCascadedAccess(pc, argument.getTypeAsString(), key)
                return rtn
            }
        }

        // get data from queries
        if (this.pc.allowImplicidQueryCall() && pc.getCurrentTemplateDialect() === CFMLEngine.DIALECT_CFML && !qryStack.isEmpty()) {
            rtn = qryStack.getDataFromACollection(pc, key, _null)
            if (rtn !== _null) {
                if (debug) debugCascadedAccess(pc, "query", key)
                return if (rtn == null && !NullSupportHelper.full(pc)) "" else rtn
            }
        }

        // variable
        rtn = variable.get(pc, key, _null)
        if (rtn !== _null) {
            if (debug && checkArguments) debugCascadedAccess(pc, variable, rtn, key)
            return rtn
        }

        // thread scopes
        if (pc.hasFamily()) {
            rtn =  // ThreadTag.getThreadScope(pc, key, ThreadTag.LEVEL_CURRENT+ThreadTag.LEVEL_KIDS);
                    (pc as PageContextImpl?).getThreadScope(key, _null)
            if (rtn !== _null) {
                if (debug && !(pc as PageContextImpl?).isThreads(rtn)) debugCascadedAccess(pc, "thread", key)
                return rtn
            }
        }

        // get a scope value (only CFML is searching additional scopes)
        if (pc.getCurrentTemplateDialect() === CFMLEngine.DIALECT_CFML) {
            for (i in scopes.indices) {
                rtn = scopes!![i].get(pc, key, _null)
                if (rtn !== _null) {
                    if (debug) debugCascadedAccess(pc, scopes!![i].getTypeAsString(), key)
                    return rtn
                }
            }
        }
        val msg: String = ExceptionUtil.similarKeyMessage(this, key.getString(), "key", "keys", null, false)
        val detail: String = ExceptionUtil.similarKeyMessage(this, key.getString(), "keys", null, false)
        if (pc.getConfig().debug()) throw ExpressionException(msg, detail)
        throw ExpressionException("variable [" + key.getString().toString() + "] doesn't exist")
    }

    @Override
    @Throws(PageException::class)
    fun getCollection(key: String?): Object? {
        return getCollection(KeyImpl.init(key))
    }

    @Override
    fun getScope(key: Collection.Key?): Struct? {
        var rtn: Object? = null
        val sct: Struct = StructImpl(Struct.TYPE_LINKED)
        val _null: Object = CollectionUtil.NULL
        if (checkArguments) {
            rtn = local.get(key, _null)
            if (rtn !== _null) sct.setEL(KeyConstants._local, rtn)
            rtn = argument.getFunctionArgument(key, _null)
            if (rtn !== _null) sct.setEL(KeyConstants._arguments, rtn)
        }

        // get data from queries
        if (pc.allowImplicidQueryCall() && pc.getCurrentTemplateDialect() === CFMLEngine.DIALECT_CFML && !qryStack.isEmpty()) {
            rtn = qryStack.getColumnFromACollection(key)
            if (rtn != null) sct.setEL(KeyConstants._query, rtn)
        }

        // variable
        rtn = variable.get(key, _null)
        if (rtn !== _null) {
            sct.setEL(KeyConstants._variables, rtn)
        }

        // thread scopes
        if (pc.hasFamily()) {
            rtn = pc.getThreadScope(key, _null)
            if (rtn !== _null) sct.setEL(KeyConstants._thread, rtn)
        }

        // get a scope value (only cfml is searching additional scopes)
        if (pc.getCurrentTemplateDialect() === CFMLEngine.DIALECT_CFML) {
            for (i in scopes.indices) {
                rtn = scopes!![i].get(key, _null)
                if (rtn !== _null) {
                    sct.setEL(KeyImpl.init(scopes!![i].getTypeAsString()), rtn)
                }
            }
        }
        return sct
    }

    /**
     * returns the scope that contains a specific key
     *
     * @param key
     * @return
     */
    fun getScopeFor(key: Collection.Key?, defaultValue: Scope?): Collection? {
        var rtn: Object? = null
        val _null: Object = CollectionUtil.NULL
        if (checkArguments) {
            rtn = local.get(key, _null)
            if (rtn !== _null) return local
            rtn = argument.getFunctionArgument(key, _null)
            if (rtn !== _null) return argument
        }

        // get data from queries
        if (pc.allowImplicidQueryCall() && pc.getCurrentTemplateDialect() === CFMLEngine.DIALECT_CFML && !qryStack.isEmpty()) {
            val qc: QueryColumn = qryStack.getColumnFromACollection(key)
            if (qc != null) return qc.getParent() as Query
        }

        // variable
        rtn = variable.get(key, _null)
        if (rtn !== _null) {
            return variable
        }

        // thread scopes
        if (pc.hasFamily()) {
            val t: Threads = pc.getThreadScope(key, _null) as Threads
            if (rtn !== _null) return t
        }

        // get a scope value (only cfml is searcing additional scopes)
        if (pc.getCurrentTemplateDialect() === CFMLEngine.DIALECT_CFML) {
            for (i in scopes.indices) {
                rtn = scopes!![i].get(key, _null)
                if (rtn !== _null) {
                    return scopes!![i]
                }
            }
        }
        return defaultValue
    }

    /**
     * return a list of String with the scope names
     *
     * @param key
     * @return
     */
    @Override
    fun getScopeNames(): List<String?>? {
        val scopeNames: List<String?> = ArrayList<String?>()
        if (checkArguments) {
            scopeNames.add("local")
            scopeNames.add("arguments")
        }
        scopeNames.add("variables")

        // thread scopes
        if (pc.hasFamily()) {
            val names: Array<String?> = pc.getThreadScopeNames()
            for (i in names.indices) scopeNames.add(i, names[i])
        }
        for (i in scopes.indices) {
            scopeNames.add(scopes!![i].getTypeAsString())
        }
        return scopeNames
    }

    @Override
    @Throws(PageException::class)
    fun getCollection(key: Key?): Object? {
        var rtn: Object? = null
        val _null: Object = CollectionUtil.NULL
        if (checkArguments) {
            rtn = local.get(key, _null)
            if (rtn !== _null) return rtn
            rtn = argument.getFunctionArgument(key, _null)
            if (rtn !== _null) {
                if (debug) debugCascadedAccess(pc, argument.getTypeAsString(), key)
                return rtn
            }
        }

        // get data from queries
        if (pc.allowImplicidQueryCall() && pc.getCurrentTemplateDialect() === CFMLEngine.DIALECT_CFML && !qryStack.isEmpty()) {
            rtn = qryStack.getColumnFromACollection(key)
            if (rtn != null) {
                if (debug) debugCascadedAccess(pc, "query", key)
                return rtn
            }
        }

        // variable
        rtn = variable.get(key, _null)
        if (rtn !== _null) {
            if (debug && checkArguments) debugCascadedAccess(pc, variable, rtn, key)
            return rtn
        }

        // thread scopes
        if (pc.hasFamily()) {
            rtn = pc.getThreadScope(key, _null)
            if (rtn !== _null) {
                if (debug && !pc.isThreads(rtn)) debugCascadedAccess(pc, "thread", key)
                return rtn
            }
        }

        // get a scope value (only CFML is searching addioanl scopes)
        if (pc.getCurrentTemplateDialect() === CFMLEngine.DIALECT_CFML) {
            for (i in scopes.indices) {
                rtn = scopes!![i].get(key, _null)
                if (rtn !== _null) {
                    if (debug) debugCascadedAccess(pc, scopes!![i].getTypeAsString(), key)
                    return rtn
                }
            }
        }
        throw ExpressionException("variable [" + key.getString().toString() + "] doesn't exist")
    }

    @Override
    operator fun get(key: Collection.Key?, defaultValue: Object?): Object? {
        return get(pc, key, defaultValue)
    }

    @Override
    operator fun get(pc: PageContext?, key: Collection.Key?, defaultValue: Object?): Object? {
        var rtn: Object? = null
        val _null: Object = CollectionUtil.NULL
        if (checkArguments) {
            rtn = local.get(pc, key, _null)
            if (rtn !== _null) return rtn
            rtn = argument.getFunctionArgument(key, _null)
            if (rtn !== _null) {
                if (debug) debugCascadedAccess(pc, argument.getTypeAsString(), key)
                return rtn
            }
        }

        // get data from queries
        if (this.pc.allowImplicidQueryCall() && pc.getCurrentTemplateDialect() === CFMLEngine.DIALECT_CFML && !qryStack.isEmpty()) {
            rtn = qryStack.getDataFromACollection(pc, key, _null)
            if (rtn !== _null) {
                if (debug) debugCascadedAccess(pc, "query", key)
                return rtn
            }
        }

        // variable
        rtn = variable.get(pc, key, _null)
        if (rtn !== _null) {
            if (debug && checkArguments) debugCascadedAccess(pc, variable, rtn, key)
            return rtn
        }

        // thread scopes
        if (pc.hasFamily()) {
            rtn = (pc as PageContextImpl?).getThreadScope(key, _null)
            if (rtn !== _null) {
                if (debug && checkArguments && !(pc as PageContextImpl?).isThreads(rtn)) debugCascadedAccess(pc, "thread", key)
                return rtn
            }
        }

        // get a scope value (only CFML is searching additional scopes)
        if (pc.getCurrentTemplateDialect() === CFMLEngine.DIALECT_CFML) {
            for (i in scopes.indices) {
                rtn = scopes!![i].get(pc, key, _null)
                if (rtn !== _null) {
                    if (debug) debugCascadedAccess(pc, scopes!![i].getTypeAsString(), key)
                    return rtn
                }
            }
        }
        return defaultValue
    }

    @Override
    fun getCascading(key: Collection.Key?): Object? {
        throw RuntimeException("this method is no longer supported, use getCascading(Collection.Key key, Object defaultValue) instead")
    }

    @Override
    fun getCascading(key: Collection.Key?, defaultValue: Object?): Object? {
        var rtn: Object

        // get a scope value (only CFML is searching additional scopes)
        if (pc.getCurrentTemplateDialect() === CFMLEngine.DIALECT_CFML) {
            val _null: Object = CollectionUtil.NULL
            for (i in scopes.indices) {
                rtn = scopes!![i].get(key, _null)
                if (rtn !== _null) {
                    return rtn
                }
            }
        }
        return defaultValue
    }

    @Override
    fun setEL(key: Collection.Key?, value: Object?): Object? {
        if (checkArguments) {
            if (localAlways || local.containsKey(key)) return local.setEL(key, value)
            if (argument.containsFunctionArgumentKey(key)) {
                if (debug) debugCascadedAccess(pc, argument.getTypeAsString(), key)
                return argument.setEL(key, value)
            }
        }
        if (debug && checkArguments) debugCascadedAccess(pc, variable.getTypeAsString(), key)
        return variable.setEL(key, value)
    }

    @Override
    @Throws(PageException::class)
    operator fun set(key: Collection.Key?, value: Object?): Object? {
        if (checkArguments) {
            if (localAlways || local.containsKey(key)) return local.set(key, value)
            if (argument.containsFunctionArgumentKey(key)) {
                if (debug) debugCascadedAccess(pc, argument.getTypeAsString(), key)
                return argument.set(key, value)
            }
        }
        if (debug && checkArguments) debugCascadedAccess(pc, variable.getTypeAsString(), key)
        return variable.set(key, value)
    }

    @Override
    fun toDumpData(pageContext: PageContext?, maxlevel: Int, dp: DumpProperties?): DumpData? {
        return variable.toDumpData(pageContext, maxlevel, dp)
    }

    @Override
    fun keyIterator(): Iterator<Collection.Key?>? {
        return variable.keyIterator()
    }

    @Override
    fun keysAsStringIterator(): Iterator<String?>? {
        return variable.keysAsStringIterator()
    }

    @Override
    fun entryIterator(): Iterator<Entry<Key?, Object?>?>? {
        return variable.entryIterator()
    }

    @Override
    fun valueIterator(): Iterator<Object?>? {
        return variable.valueIterator()
    }

    @Override
    fun isInitalized(): Boolean {
        return isInit
    }

    @Override
    fun initialize(pc: PageContext?) {
        // if(isInitalized()) return;
        isInit = true
        variable = pc.variablesScope()
        argument = pc.argumentsScope()
        local = pc.localScope()
        // allowImplicidQueryCall = pc.getConfig().allowImplicidQueryCall();
        type = (pc as PageContextImpl?).getScopeCascadingType()
        debug = pc.getConfig().debug() && (pc.getConfig() as ConfigPro).hasDebugOptions(ConfigPro.DEBUG_IMPLICIT_ACCESS)

        // Strict
        if (type == Config.SCOPE_STRICT) {
            // print.ln("strict");
            scopes = arrayOf<Scope?>()
        } else if (type == Config.SCOPE_SMALL) {
            // print.ln("small");
            if (pc.getConfig().mergeFormAndURL()) {
                scopes = arrayOf<Scope?>(pc.formScope())
            } else {
                scopes = arrayOf<Scope?>(pc.urlScope(), pc.formScope())
            }
        } else {
            reinitialize(pc)
        }
    }

    @Override
    fun reinitialize(pc: PageContext?) {
        if (type != Config.SCOPE_STANDARD) return
        val cs: Client = pc.clientScopeEL()
        // print.ln("standard");
        if (pc.getConfig().mergeFormAndURL()) {
            scopes = arrayOfNulls<Scope?>(if (cs == null) 3 else 4)
            scopes!![0] = pc.cgiScope()
            scopes!![1] = pc.formScope()
            scopes!![2] = pc.cookieScope()
            if (cs != null) scopes!![3] = cs
        } else {
            scopes = arrayOfNulls<Scope?>(if (cs == null) 4 else 5)
            scopes!![0] = pc.cgiScope()
            scopes!![1] = pc.urlScope()
            scopes!![2] = pc.formScope()
            scopes!![3] = pc.cookieScope()
            if (cs != null) scopes!![4] = cs
        }
    }

    @Override
    fun release(pc: PageContext?) {
        isInit = false
        argument = null
        local = null
        variable = null
        scopes = null
        checkArguments = false
        localAlways = false
        // if (allowImplicidQueryCall)
        qryStack.clear()
    }

    @Override
    fun duplicate(deepCopy: Boolean): Collection? {
        val dupl = UndefinedImpl(pc, type)
        // dupl.allowImplicidQueryCall = allowImplicidQueryCall;
        dupl.checkArguments = checkArguments
        dupl.argument = if (deepCopy) Duplicator.duplicate(argument, deepCopy) as Argument else argument
        dupl.isInit = isInit
        dupl.local = if (deepCopy) Duplicator.duplicate(local, deepCopy) as Local else local
        dupl.localAlways = localAlways
        dupl.qryStack = if (deepCopy) Duplicator.duplicate(qryStack, deepCopy) as QueryStackImpl else qryStack
        dupl.variable = if (deepCopy) Duplicator.duplicate(variable, deepCopy) as Variables else variable
        dupl.pc = pc
        dupl.debug = debug

        // scopes
        if (deepCopy) {
            dupl.scopes = arrayOfNulls<Scope?>(scopes!!.size)
            for (i in scopes.indices) {
                dupl.scopes!![i] = Duplicator.duplicate(scopes!![i], deepCopy) as Scope
            }
        } else dupl.scopes = scopes
        return dupl
    }

    @Override
    fun containsKey(key: Key?): Boolean {
        return get(pc, key, null) != null
    }

    @Override
    fun containsKey(pc: PageContext?, key: Key?): Boolean {
        return get(pc, key, null) != null
    }

    @Override
    @Throws(ExpressionException::class)
    fun castToString(): String? {
        throw ExpressionException("Can't cast Complex Object Type Struct to String", "Use Built-In-Function \"serialize(Struct):String\" to create a String from Struct")
    }

    @Override
    fun castToString(defaultValue: String?): String? {
        return defaultValue
    }

    @Override
    @Throws(ExpressionException::class)
    fun castToBooleanValue(): Boolean {
        throw ExpressionException("Can't cast Complex Object Type Struct to a boolean value")
    }

    @Override
    fun castToBoolean(defaultValue: Boolean?): Boolean? {
        return defaultValue
    }

    @Override
    @Throws(ExpressionException::class)
    fun castToDoubleValue(): Double {
        throw ExpressionException("Can't cast Complex Object Type Struct to a number value")
    }

    @Override
    fun castToDoubleValue(defaultValue: Double): Double {
        return defaultValue
    }

    @Override
    @Throws(ExpressionException::class)
    fun castToDateTime(): DateTime? {
        throw ExpressionException("Can't cast Complex Object Type Struct to a Date")
    }

    @Override
    fun castToDateTime(defaultValue: DateTime?): DateTime? {
        return defaultValue
    }

    @Override
    @Throws(ExpressionException::class)
    operator fun compareTo(b: Boolean): Int {
        throw ExpressionException("can't compare Complex Object Type Struct with a boolean value")
    }

    @Override
    @Throws(PageException::class)
    operator fun compareTo(dt: DateTime?): Int {
        throw ExpressionException("can't compare Complex Object Type Struct with a DateTime Object")
    }

    @Override
    @Throws(PageException::class)
    operator fun compareTo(d: Double): Int {
        throw ExpressionException("can't compare Complex Object Type Struct with a numeric value")
    }

    @Override
    @Throws(PageException::class)
    operator fun compareTo(str: String?): Int {
        throw ExpressionException("can't compare Complex Object Type Struct with a String")
    }

    @Override
    fun setVariableScope(scope: Variables?) {
        variable = scope
    }

    @Override
    fun getType(): Int {
        return SCOPE_UNDEFINED
    }

    @Override
    fun getTypeAsString(): String? {
        return "undefined"
    }

    /**
     * @return the allowImplicidQueryCall
     */
    fun isAllowImplicidQueryCall(): Boolean {
        return pc.allowImplicidQueryCall()
    }

    /**
     * @param allowImplicidQueryCall the allowImplicidQueryCall to set
     */
    @Override
    fun setAllowImplicidQueryCall(allowImplicidQueryCall: Boolean): Boolean {
        val old: Boolean = pc.allowImplicidQueryCall()
        (pc.getApplicationContext() as ApplicationContextSupport).setAllowImplicidQueryCall(allowImplicidQueryCall)
        // this.allowImplicidQueryCall = allowImplicidQueryCall;
        return old
    }

    /**
     * @return the checkArguments
     */
    @Override
    fun getCheckArguments(): Boolean {
        return checkArguments
    }

    @Override
    @Throws(PageException::class)
    fun call(pc: PageContext?, methodName: Key?, args: Array<Object?>?): Object? {
        val obj: Object? = get(pc, methodName, null) // every none UDF value is fine as default argument
        if (obj is UDF) {
            return (obj as UDF?).call(pc, methodName, args, false)
        }
        val udf: UDF? = getUDF(pc, methodName)
        if (udf is UDF) {
            return udf.call(pc, methodName, args, false)
        }
        val bif: BIF = BIF.getInstance(pc, methodName.getLowerString(), null)
        if (bif != null) {
            return bif.call(pc, methodName, args, false)
        }
        throw ExpressionException("No matching function [$methodName] found")
    }

    @Override
    @Throws(PageException::class)
    fun callWithNamedValues(pc: PageContext?, methodName: Key?, args: Struct?): Object? {
        val obj: Object? = get(pc, methodName, null)
        if (obj is UDF) {
            return (obj as UDF?).callWithNamedValues(pc, methodName, args, false)
        }
        val udf: UDF? = getUDF(pc, methodName)
        if (udf is UDF) {
            return udf.callWithNamedValues(pc, methodName, args, false)
        }
        val bif: BIF = BIF.getInstance(pc, methodName.getLowerString(), null)
        if (bif != null) {
            return bif.callWithNamedValues(pc, methodName, args, false)
        }
        throw ExpressionException("No matching function [$methodName] found")
    }

    @Throws(PageException::class)
    private fun getUDF(pc: PageContext?, methodName: Key?): UDF? {
        val ac: ApplicationContextSupport = pc.getApplicationContext() as ApplicationContextSupport
        if (ac != null) {
            val dirs: List<Resource?> = ac.getFunctionDirectories()
            var files: Array<Resource?>
            if (dirs != null && dirs.size() > 0) {
                var file: Resource? = null
                val it: Iterator<Resource?> = dirs.iterator()
                var dir: Resource?
                while (it.hasNext()) {
                    dir = it.next()
                    files = dir.listResources(object : ResourceNameFilter() {
                        @Override
                        fun accept(dir: Resource?, name: String?): Boolean {
                            val exts: Array<String?> = Constants.getTemplateExtensions()
                            for (ex in exts) {
                                if (name.equalsIgnoreCase(methodName.toString() + "." + ex)) return true
                            }
                            return false
                        }
                    })
                    if (files != null && files.size > 0) {
                        file = files[0]
                        break
                    }
                }
                if (file != null) {
                    return CFFunction.loadUDF(pc, file, methodName, pc.getConfig() is ConfigWeb, false)
                }
            }
        }
        return null
    }

    fun getScopeCascadingType(): Short {
        return type
    }

    companion object {
        private const val serialVersionUID = -5626787508494702023L
        fun debugCascadedAccess(pc: PageContext?, `var`: Variables?, value: Object?, key: Collection.Key?) {
            if (`var` is ComponentScope) {
                if (key.equals(KeyConstants._THIS) || key.equals(KeyConstants._SUPER) || key.equals(KeyConstants._STATIC)) return
                if (value is UDF) {
                    return
                }
            }
            debugCascadedAccess(pc, "variables", key)
        }

        fun debugCascadedAccess(pc: PageContext?, name: String?, key: Collection.Key?) {
            if (pc != null) (pc.getDebugger() as DebuggerImpl).addImplicitAccess(pc, name, key.getString())
        }
    }

    /**
     * constructor of the class
     *
     * @param pageContextImpl
     * @param type type of the undefined scope
     * (ServletConfig.SCOPE_STRICT;ServletConfig.SCOPE_SMALL;ServletConfig.SCOPE_STANDART)
     */
    init {
        this.pc = pc
    }
}