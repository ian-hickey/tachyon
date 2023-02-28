/**
 * Copyright (c) 2015, Lucee Assosication Switzerland. All rights reserved.
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
package lucee.transformer.bytecode.expression.`var`

import java.util.ArrayList

class VariableImpl : ExpressionBase, Variable {
    private var scope: Int = Scope.SCOPE_UNDEFINED
    var members: List<Member?>? = ArrayList<Member?>()
    var countDM = 0
    var countFM = 0
    private var ignoredFirstMember = false
    private var fromHash = false
    private var defaultValue: Expression? = null
    private var asCollection: Boolean? = null
    private var assign: Assign? = null

    constructor(factory: Factory?, start: Position?, end: Position?) : super(factory, start, end) {}
    constructor(factory: Factory?, scope: Int, start: Position?, end: Position?) : super(factory, start, end) {
        this.scope = scope
    }

    @Override
    fun getDefaultValue(): Expression? {
        return defaultValue
    }

    @Override
    fun setDefaultValue(defaultValue: Expression?) {
        this.defaultValue = defaultValue
    }

    @Override
    fun getAsCollection(): Boolean? {
        return asCollection
    }

    @Override
    fun setAsCollection(asCollection: Boolean?) {
        this.asCollection = asCollection
    }

    @Override
    fun getScope(): Int {
        return scope
    }

    /**
     * @param scope the scope to set
     */
    fun setScope(scope: Int) {
        this.scope = scope
    }

    @Override
    fun addMember(member: Member?) {
        if (member is DataMember) countDM++ else countFM++
        member.setParent(this)
        members.add(member)
    }

    @Override
    fun removeMember(index: Int): Member? {
        val rtn: Member = members.remove(index)
        if (rtn is DataMember) countDM-- else countFM--
        return rtn
    }

    @Override
    @Throws(TransformerException::class)
    fun writeOutCollection(c: Context?, mode: Int): Class<*>? {
        return try {
            Types.toClass(writeOutCollectionAsType(c, mode))
        } catch (e: ClassException) {
            throw TransformerException(c, e, null)
        }
    }

    @Throws(TransformerException::class)
    fun writeOutCollectionAsType(c: Context?, mode: Int): Type? {
        val bc: BytecodeContext? = c as BytecodeContext?
        ExpressionUtil.visitLine(bc, getStart())
        val type: Type? = _writeOut(bc, mode, Boolean.TRUE)
        ExpressionUtil.visitLine(bc, getEnd())
        return type
    }

    @Override
    @Throws(TransformerException::class)
    fun _writeOut(bc: BytecodeContext?, mode: Int): Type? {
        return if (defaultValue != null && countFM == 0 && countDM != 0) _writeOutCallerUtil(bc, mode) else _writeOut(bc, mode, asCollection)
    }

    @Throws(TransformerException::class)
    private fun _writeOut(bc: BytecodeContext?, mode: Int, asCollection: Boolean?): Type? {
        val adapter: GeneratorAdapter = bc.getAdapter()
        val count = countFM + countDM

        // count 0
        if (count == 0) return _writeOutEmpty(bc)
        val doOnlyScope = scope == Scope.SCOPE_LOCAL

        // boolean last;
        var c = 0
        for (i in if (doOnlyScope) 0 else 1 until count) {
            val member: Member? = members!![count - 1 - c]
            c++
            adapter.loadArg(0)
            if (member.getSafeNavigated() && member is UDF) adapter.checkCast(Types.PAGE_CONTEXT_IMPL)
        }
        var rtn: Type? = _writeOutFirst(bc, members!![0], mode, count == 1, doOnlyScope, null, null)

        // pc.get(
        for (i in if (doOnlyScope) 0 else 1 until count) {
            val member: Member? = members!![i]
            val last = i + 1 == count

            // Data Member
            if (member is DataMember) {
                val name: ExprString = (member as DataMember?).getName()
                if (last && ASMUtil.isDotKey(name)) {
                    val ls: LitString = name as LitString
                    if (ls.getString().equalsIgnoreCase("RECORDCOUNT")) {
                        adapter.invokeStatic(Types.VARIABLE_UTIL_IMPL, RECORDCOUNT)
                    } else if (ls.getString().equalsIgnoreCase("CURRENTROW")) {
                        adapter.invokeStatic(Types.VARIABLE_UTIL_IMPL, CURRENTROW)
                    } else if (ls.getString().equalsIgnoreCase("COLUMNLIST")) {
                        adapter.invokeStatic(Types.VARIABLE_UTIL_IMPL, COLUMNLIST)
                    } else {
                        getFactory().registerKey(bc, name, false)
                        // safe nav
                        var type: Int
                        type = if (member.getSafeNavigated()) {
                            val `val`: Expression = member.getSafeNavigatedValue()
                            if (`val` == null) ASMConstants.NULL(adapter) else `val`.writeOut(bc, Expression.MODE_REF)
                            THREE
                        } else TWO
                        adapter.invokeVirtual(Types.PAGE_CONTEXT, if (asCollection(asCollection, last)) GET_COLLECTION!![type] else GET!![type])
                    }
                } else {
                    getFactory().registerKey(bc, name, false)
                    // safe nav
                    var type: Int
                    type = if (member.getSafeNavigated()) {
                        val `val`: Expression = member.getSafeNavigatedValue()
                        if (`val` == null) ASMConstants.NULL(adapter) else `val`.writeOut(bc, Expression.MODE_REF)
                        THREE
                    } else TWO
                    adapter.invokeVirtual(Types.PAGE_CONTEXT, if (asCollection(asCollection, last)) GET_COLLECTION!![type] else GET!![type])
                }
                rtn = Types.OBJECT
            } else if (member is UDF) {
                rtn = _writeOutUDF(bc, member as UDF?)
            }
        }
        return rtn
    }

    @Throws(TransformerException::class)
    private fun _writeOutCallerUtil(bc: BytecodeContext?, mode: Int): Type? {
        val adapter: GeneratorAdapter = bc.getAdapter()
        val count = countFM + countDM

        // count 0
        if (count == 0) return _writeOutEmpty(bc)

        // pc
        adapter.loadArg(0)

        // collection
        val startIndex: RefInteger = RefIntegerImpl()
        _writeOutFirst(bc, members!![0], mode, count == 1, true, defaultValue, startIndex)

        // keys
        val it: Iterator<Member?> = members!!.iterator()
        val av = ArrayVisitor()
        av.visitBegin(adapter, Types.COLLECTION_KEY, countDM - startIndex.toInt())
        var index = 0
        var i = 0
        while (it.hasNext()) {
            val member: DataMember? = it.next() as DataMember?
            if (i++ < startIndex.toInt()) continue
            av.visitBeginItem(adapter, index++)
            getFactory().registerKey(bc, member.getName(), false)
            av.visitEndItem(bc.getAdapter())
        }
        av.visitEnd()

        // defaultValue
        defaultValue.writeOut(bc, MODE_REF)
        bc.getAdapter().invokeStatic(Types.CALLER_UTIL, CALLER_UTIL_GET)
        return Types.OBJECT
    }

    private fun asCollection(asCollection: Boolean?, last: Boolean): Boolean {
        return if (!last) true else asCollection != null && asCollection.booleanValue()
    }

    /**
     * outputs an empty Variable, only scope Example: pc.formScope();
     *
     * @param adapter
     * @throws TemplateException
     */
    @Throws(TransformerException::class)
    private fun _writeOutEmpty(bc: BytecodeContext?): Type? {
        if (ignoredFirstMember && (scope == Scope.SCOPE_LOCAL || scope == Scope.SCOPE_VAR)) return Types.VOID
        val adapter: GeneratorAdapter = bc.getAdapter()
        adapter.loadArg(0)
        val m: Method
        var t: Type = Types.PAGE_CONTEXT
        if (scope == Scope.SCOPE_ARGUMENTS) {
            getFactory().TRUE().writeOut(bc, MODE_VALUE)
            m = TypeScope.METHOD_ARGUMENT_BIND
        } else if (scope == Scope.SCOPE_LOCAL) {
            t = Types.PAGE_CONTEXT
            getFactory().TRUE().writeOut(bc, MODE_VALUE)
            m = TypeScope.METHOD_LOCAL_BIND
        } else if (scope == Scope.SCOPE_VAR) {
            t = Types.PAGE_CONTEXT
            getFactory().TRUE().writeOut(bc, MODE_VALUE)
            m = TypeScope.METHOD_VAR_BIND
        } else m = TypeScope.METHODS.get(scope)
        TypeScope.invokeScope(adapter, m, t)
        return m.getReturnType()
    }

    @Throws(TransformerException::class)
    private fun _writeOutFirst(bc: BytecodeContext?, member: Member?, mode: Int, last: Boolean, doOnlyScope: Boolean, defaultValue: Expression?, startIndex: RefInteger?): Type? {
        return if (member is DataMember) _writeOutFirstDataMember(bc, member as DataMember?, scope, last, doOnlyScope, defaultValue, startIndex) else if (member is UDF) _writeOutFirstUDF(bc, member as UDF?, scope, doOnlyScope) else _writeOutFirstBIF(bc, member as BIF?, mode, last, getStart())
    }

    @Throws(TransformerException::class)
    fun _writeOutFirstDataMember(bc: BytecodeContext?, member: DataMember?, scope: Int, last: Boolean, doOnlyScope: Boolean, defaultValue: Expression?, startIndex: RefInteger?): Type? {
        val adapter: GeneratorAdapter = bc.getAdapter()
        if (startIndex != null) startIndex.setValue(if (doOnlyScope) 0 else 1)

        // this/static
        if (scope == Scope.SCOPE_UNDEFINED) {
            val name: ExprString = member.getName()
            if (ASMUtil.isDotKey(name)) {
                val ls: LitString = name as LitString

                // THIS
                if (ls.getString().equalsIgnoreCase("THIS")) {
                    if (startIndex != null) startIndex.setValue(1)
                    adapter.loadArg(0)
                    adapter.checkCast(Types.PAGE_CONTEXT_IMPL)
                    if (defaultValue != null) {
                        defaultValue.writeOut(bc, MODE_REF)
                        adapter.invokeVirtual(Types.PAGE_CONTEXT_IMPL, if (countFM + countDM == 1) THIS_GET1 else THIS_TOUCH1)
                    } else adapter.invokeVirtual(Types.PAGE_CONTEXT_IMPL, if (countFM + countDM == 1) THIS_GET0 else THIS_TOUCH0)
                    return Types.OBJECT
                }
                // STATIC
                if (ls.getString().equalsIgnoreCase("STATIC")) {
                    if (startIndex != null) startIndex.setValue(1)
                    adapter.loadArg(0)
                    adapter.checkCast(Types.PAGE_CONTEXT_IMPL)
                    if (defaultValue != null) {
                        defaultValue.writeOut(bc, MODE_REF)
                        adapter.invokeVirtual(Types.PAGE_CONTEXT_IMPL, if (countFM + countDM == 1) STATIC_GET1 else STATIC_TOUCH1)
                    } else adapter.invokeVirtual(Types.PAGE_CONTEXT_IMPL, if (countFM + countDM == 1) STATIC_GET0 else STATIC_TOUCH0)
                    return Types.OBJECT
                }
            }
        }

        // LDEV3496
        // subsequent logic will conditionally require a PageContext be pushed onto the stack, as part of a
        // call to resolve a save-nav expression member
        // But, we only want to push it if it will be consumed
        // root cause of LDEV3496 was this was pushed in cases where it would not be consumed, and an extra
        // unanticpated stack variable would break during class verification
        // (jvm would report "expected a stackmap frame", javassist would report "InvocationTargetException:
        // Operand stacks could not be merged, they are different sizes!")
        val needsAndWillConsumePageContextForSafeNavigationResolution = member.getSafeNavigated() && !doOnlyScope
        if (needsAndWillConsumePageContextForSafeNavigationResolution) {
            adapter.loadArg(0)
        }

        // collection
        val rtn: Type
        rtn = if (scope == Scope.SCOPE_LOCAL && defaultValue != null) { // local
            adapter.loadArg(0)
            adapter.checkCast(Types.PAGE_CONTEXT_IMPL)
            getFactory().FALSE().writeOut(bc, MODE_VALUE)
            defaultValue.writeOut(bc, MODE_VALUE)
            adapter.invokeVirtual(Types.PAGE_CONTEXT_IMPL, TypeScope.METHOD_LOCAL_EL)
            Types.OBJECT
        } else { // all other scopes
            adapter.loadArg(0)
            TypeScope.invokeScope(adapter, scope)
        }
        if (doOnlyScope) return rtn
        getFactory().registerKey(bc, member.getName(), false)
        val _last = !last && scope == Scope.SCOPE_UNDEFINED
        if (!member.getSafeNavigated()) {
            adapter.invokeInterface(TypeScope.SCOPES.get(scope), if (_last) METHOD_SCOPE_GET_COLLECTION_KEY else METHOD_SCOPE_GET_KEY)
        } else {
            val `val`: Expression = member.getSafeNavigatedValue() // LDEV-1201
            if (`val` == null) ASMConstants.NULL(bc.getAdapter()) else `val`.writeOut(bc, Expression.MODE_REF)
            adapter.invokeVirtual(Types.PAGE_CONTEXT, if (_last) GET_COLLECTION!![THREE] else GET!![THREE])
        }
        return Types.OBJECT
    }

    @Override
    fun getMembers(): List<Member?>? {
        return members
    }

    @Override
    fun getFirstMember(): Member? {
        return if (members!!.isEmpty()) null else members!![0]
    }

    @Override
    fun getLastMember(): Member? {
        return if (members!!.isEmpty()) null else members!![members!!.size() - 1]
    }

    @Override
    fun ignoredFirstMember(b: Boolean) {
        ignoredFirstMember = b
    }

    @Override
    fun ignoredFirstMember(): Boolean {
        return ignoredFirstMember
    }

    @Override
    fun fromHash(fromHash: Boolean) {
        this.fromHash = fromHash
    }

    @Override
    fun fromHash(): Boolean {
        return fromHash
    }

    @Override
    fun getCount(): Int {
        return countDM + countFM
    }

    @Override
    fun assign(assign: Assign?) {
        this.assign = assign
    }

    @Override
    fun assign(): Assign? {
        return assign
    }

    companion object {
        // java.lang.Object get(Key)
        val METHOD_SCOPE_GET_KEY: Method? = Method("get", Types.OBJECT, arrayOf<Type?>(Types.COLLECTION_KEY))

        // Object getCollection(Key)
        val METHOD_SCOPE_GET_COLLECTION_KEY: Method? = Method("getCollection", Types.OBJECT, arrayOf<Type?>(Types.COLLECTION_KEY))

        // public Object get(PageContext pc,Object coll, Key[] keys, Object defaultValue) {
        /* ??? */
        private val CALLER_UTIL_GET: Method? = Method("get", Types.OBJECT, arrayOf<Type?>(Types.PAGE_CONTEXT, Types.OBJECT, Types.COLLECTION_KEY_ARRAY, Types.OBJECT))
        val INIT: Method? = Method("init", Types.COLLECTION_KEY, arrayOf<Type?>(Types.STRING))
        val TO_KEY: Method? = Method("toKey", Types.COLLECTION_KEY, arrayOf<Type?>(Types.OBJECT))
        private const val TWO = 0
        private const val THREE = 1
        private const val THREE2 = 2

        // Object getCollection (Object,Key[,Object])
        private val GET_COLLECTION: Array<Method?>? = arrayOf<Method?>(Method("getCollection", Types.OBJECT, arrayOf<Type?>(Types.OBJECT, Types.COLLECTION_KEY)),
                Method("getCollection", Types.OBJECT, arrayOf<Type?>(Types.OBJECT, Types.COLLECTION_KEY, Types.OBJECT)))

        // Object get (Object,Key)
        private val GET: Array<Method?>? = arrayOf<Method?>(Method("get", Types.OBJECT, arrayOf<Type?>(Types.OBJECT, Types.COLLECTION_KEY)),
                Method("get", Types.OBJECT, arrayOf<Type?>(Types.OBJECT, Types.COLLECTION_KEY, Types.OBJECT)))
        private val GET_FUNCTION: Array<Method?>? = arrayOf<Method?>(Method("getFunction", Types.OBJECT, arrayOf<Type?>(Types.OBJECT, Types.COLLECTION_KEY, Types.OBJECT_ARRAY)),
                Method("getFunction", Types.OBJECT, arrayOf<Type?>(Types.OBJECT, Types.COLLECTION_KEY, Types.OBJECT_ARRAY, Types.OBJECT)),
                Method("getFunction2", Types.OBJECT, arrayOf<Type?>(Types.OBJECT, Types.COLLECTION_KEY, Types.OBJECT_ARRAY, Types.OBJECT)))

        // Object getFunctionWithNamedValues (Object,String,Object[])
        private val GET_FUNCTION_WITH_NAMED_ARGS: Array<Method?>? = arrayOf<Method?>(
                Method("getFunctionWithNamedValues", Types.OBJECT, arrayOf<Type?>(Types.OBJECT, Types.COLLECTION_KEY, Types.OBJECT_ARRAY)),
                Method("getFunctionWithNamedValues", Types.OBJECT, arrayOf<Type?>(Types.OBJECT, Types.COLLECTION_KEY, Types.OBJECT_ARRAY, Types.OBJECT)),
                Method("getFunctionWithNamedValues2", Types.OBJECT, arrayOf<Type?>(Types.OBJECT, Types.COLLECTION_KEY, Types.OBJECT_ARRAY, Types.OBJECT)))
        private val RECORDCOUNT: Method? = Method("recordcount", Types.OBJECT, arrayOf<Type?>(Types.PAGE_CONTEXT, Types.OBJECT))
        private val CURRENTROW: Method? = Method("currentrow", Types.OBJECT, arrayOf<Type?>(Types.PAGE_CONTEXT, Types.OBJECT))
        private val COLUMNLIST: Method? = Method("columnlist", Types.OBJECT, arrayOf<Type?>(Types.PAGE_CONTEXT, Types.OBJECT))

        // THIS
        private val THIS_GET0: Method? = Method("thisGet", Types.OBJECT, arrayOf<Type?>())
        private val THIS_TOUCH0: Method? = Method("thisTouch", Types.OBJECT, arrayOf<Type?>())
        private val THIS_GET1: Method? = Method("thisGet", Types.OBJECT, arrayOf<Type?>(Types.OBJECT))
        private val THIS_TOUCH1: Method? = Method("thisTouch", Types.OBJECT, arrayOf<Type?>(Types.OBJECT))

        // STATIC
        private val STATIC_GET0: Method? = Method("staticGet", Types.OBJECT, arrayOf<Type?>())
        private val STATIC_TOUCH0: Method? = Method("staticTouch", Types.OBJECT, arrayOf<Type?>())
        private val STATIC_GET1: Method? = Method("staticGet", Types.OBJECT, arrayOf<Type?>(Types.OBJECT))
        private val STATIC_TOUCH1: Method? = Method("staticTouch", Types.OBJECT, arrayOf<Type?>(Types.OBJECT))
        private val INVOKE: Method? = Method("invoke", Types.OBJECT, arrayOf<Type?>(Types.PAGE_CONTEXT, Types.OBJECT_ARRAY, Types.STRING, Types.STRING, Types.STRING))
        @Throws(TransformerException::class)
        fun _writeOutFirstBIF(bc: BytecodeContext?, bif: BIF?, mode: Int, last: Boolean, line: Position?): Type? {
            val adapter: GeneratorAdapter = bc.getAdapter()
            adapter.loadArg(0)
            // class
            val bifCD: ClassDefinition = bif!!.getClassDefinition()
            var clazz: Class? = null
            try {
                clazz = bifCD.getClazz()
            } catch (e: Exception) {
                LogUtil.log(VariableImpl::class.java.getName(), e)
            }
            var rtnType: Type = Types.toType(bc, bif!!.getReturnType())
            if (rtnType === Types.VOID) rtnType = Types.STRING

            // arguments
            var args: Array<Argument?> = bif!!.getArguments()
            var argTypes: Array<Type?>?
            val core: Boolean = bif!!.getFlf().isCore() // MUST setting this to false need to work !!!
            if (bif!!.getArgType() === FunctionLibFunction.ARG_FIX && !bifCD.isBundle() && core) {
                if (isNamed(bc, bif!!.getFlf().getName(), args)) {
                    val nargs: Array<NamedArgument?>? = toNamedArguments(args)
                    val names = arrayOfNulls<String?>(nargs!!.size)
                    // get all names
                    for (i in nargs.indices) {
                        names[i] = getName(bc, nargs[i]!!.getName())
                    }
                    val list: ArrayList<FunctionLibFunctionArg?> = bif!!.getFlf().getArg()
                    val it: Iterator<FunctionLibFunctionArg?> = list.iterator()
                    argTypes = arrayOfNulls<Type?>(list.size() + 1)
                    argTypes!![0] = Types.PAGE_CONTEXT
                    var flfa: FunctionLibFunctionArg?
                    var index = 0
                    var vt: VT?
                    while (it.hasNext()) {
                        flfa = it.next()
                        vt = getMatchingValueAndType(bc, bc.getFactory(), flfa, nargs, names, line)
                        if (vt!!.index != -1) names[vt.index] = null
                        argTypes[++index] = Types.toType(bc, vt.type)
                        if (vt.value == null) ASMConstants.NULL(bc.getAdapter()) else vt.value.writeOut(bc, if (Types.isPrimitiveType(argTypes[index])) MODE_VALUE else MODE_REF)
                    }
                    for (y in names.indices) {
                        if (names[y] != null) {
                            val bce = TransformerException(bc, "argument [" + names[y] + "] is not allowed for function [" + bif!!.getFlf().getName() + "]",
                                    args[y].getStart())
                            UDFUtil.addFunctionDoc(bce, bif!!.getFlf())
                            throw bce
                        }
                    }
                } else {
                    argTypes = arrayOfNulls<Type?>(args.size + 1)
                    argTypes!![0] = Types.PAGE_CONTEXT
                    for (y in args.indices) {
                        argTypes[y + 1] = Types.toType(bc, args[y]!!.getStringType())
                        args[y].writeOutValue(bc, if (Types.isPrimitiveType(argTypes[y + 1])) MODE_VALUE else MODE_REF)
                    }
                    // if no method exists for the exact match of arguments, call the method with all arguments (when
                    // exists)
                    if (methodExists(clazz, "call", argTypes, rtnType) === Boolean.FALSE) {
                        val _args: ArrayList<FunctionLibFunctionArg?> = bif!!.getFlf().getArg()
                        val tmp: Array<Type?> = arrayOfNulls<Type?>(_args.size() + 1)

                        // fill the existing
                        for (i in argTypes.indices) {
                            tmp[i] = argTypes[i]
                        }

                        // get the rest with default values
                        var flfa: FunctionLibFunctionArg
                        var def: VT?
                        for (i in argTypes.size until tmp.size) {
                            flfa = _args.get(i - 1)
                            tmp[i] = Types.toType(bc, flfa.getTypeAsString())
                            def = getDefaultValue(bc.getFactory(), flfa)
                            if (def!!.value != null) def.value.writeOut(bc, if (Types.isPrimitiveType(tmp[i])) MODE_VALUE else MODE_REF) else ASMConstants.NULL(bc.getAdapter())
                        }
                        argTypes = tmp
                    }
                }
            } else {
                ///////////////////////////////////////////////////////////////
                if (bif!!.getArgType() === FunctionLibFunction.ARG_FIX) {
                    if (isNamed(bc, bif!!.getFlf().getName(), args)) {
                        val nargs: Array<NamedArgument?>? = toNamedArguments(args)
                        val names = getNames(bc, nargs)
                        val list: ArrayList<FunctionLibFunctionArg?> = bif!!.getFlf().getArg()
                        val it: Iterator<FunctionLibFunctionArg?> = list.iterator()
                        val tmpArgs: LinkedList<Argument?> = LinkedList<Argument?>()
                        val nulls: LinkedList<Boolean?> = LinkedList<Boolean?>()
                        var flfa: FunctionLibFunctionArg?
                        var vt: VT?
                        while (it.hasNext()) {
                            flfa = it.next()
                            vt = getMatchingValueAndType(bc, bc.getFactory(), flfa, nargs, names, line)
                            if (vt!!.index != -1) names!![vt.index] = null
                            if (vt.value == null) tmpArgs.add(Argument(bif!!.getFactory().createNull(), "any")) // has to by any otherwise a caster is set
                            else tmpArgs.add(Argument(vt.value, vt.type))
                            nulls.add(vt.value == null)
                        }
                        for (y in names.indices) {
                            if (names!![y] != null) {
                                val bce = TransformerException(bc, "argument [" + names[y] + "] is not allowed for function [" + bif!!.getFlf().getName() + "]",
                                        args[y].getStart())
                                UDFUtil.addFunctionDoc(bce, bif!!.getFlf())
                                throw bce
                            }
                        }
                        // remove null at the end
                        var tmp: Boolean
                        while (nulls.pollLast().also { tmp = it } != null) {
                            if (!tmp.booleanValue()) break
                            tmpArgs.pollLast()
                        }
                        args = tmpArgs.toArray(arrayOfNulls<Argument?>(tmpArgs.size()))
                    }
                }
                ///////////////////////////////////////////////////////////////
                argTypes = arrayOfNulls<Type?>(2)
                argTypes!![0] = Types.PAGE_CONTEXT
                argTypes[1] = Types.OBJECT_ARRAY
                ExpressionUtil.writeOutExpressionArray(bc, Types.OBJECT, args)
            }

            // core
            if (core && !bifCD.isBundle()) {
                adapter.invokeStatic(Type.getType(clazz), Method("call", rtnType, argTypes))
            } else {
                // in that case we need 3 additional args
                // className
                if (bifCD.getClassName() != null) adapter.push(bifCD.getClassName()) else ASMConstants.NULL(adapter)
                if (bifCD.getName() != null) adapter.push(bifCD.getName()) // bundle name
                else ASMConstants.NULL(adapter)
                if (bifCD.getVersionAsString() != null) adapter.push(bifCD.getVersionAsString()) // bundle version
                else ASMConstants.NULL(adapter)
                adapter.invokeStatic(Types.FUNCTION_HANDLER_POOL, INVOKE)
                rtnType = Types.OBJECT
            }
            if (mode == MODE_REF || !last) {
                if (Types.isPrimitiveType(rtnType)) {
                    adapter.invokeStatic(Types.CASTER, Method("toRef", Types.toRefType(rtnType), arrayOf<Type?>(rtnType)))
                    rtnType = Types.toRefType(rtnType)
                }
            }
            return rtnType
        }

        /**
         * checks if a method exists
         *
         * @param clazz
         * @param methodName
         * @param args
         * @param returnType
         * @return returns null when checking fi
         */
        private fun methodExists(clazz: Class?, methodName: String?, args: Array<Type?>?, returnType: Type?): Boolean? {
            return try {
                val _args: Array<Class<*>?> = arrayOfNulls<Class?>(args!!.size)
                for (i in _args.indices) {
                    _args[i] = Types.toClass(args!![i])
                }
                val rtn: Class<*> = Types.toClass(returnType)
                try {
                    val m: java.lang.reflect.Method = clazz.getMethod(methodName, _args)
                    m.getReturnType() === rtn
                } catch (e: Exception) {
                    false
                }
            } catch (e: Exception) {
                LogUtil.log(VariableImpl::class.java.getName(), e)
                null
            }
        }

        @Throws(TransformerException::class)
        fun _writeOutFirstUDF(bc: BytecodeContext?, udf: UDF?, scope: Int, doOnlyScope: Boolean): Type? {
            val adapter: GeneratorAdapter = bc.getAdapter()
            // pc.getFunction (Object,String,Object[])
            // pc.getFunctionWithNamedValues (Object,String,Object[])
            adapter.loadArg(0)
            if (udf!!.getSafeNavigated()) adapter.checkCast(Types.PAGE_CONTEXT_IMPL) // FUTURE remove if no longer necessary to have PageContextImpl
            if (!doOnlyScope) adapter.loadArg(0)
            val rtn: Type = TypeScope.invokeScope(adapter, scope)
            return if (doOnlyScope) rtn else _writeOutUDF(bc, udf)
        }

        @Throws(TransformerException::class)
        private fun _writeOutUDF(bc: BytecodeContext?, udf: UDF?): Type? {
            bc.getFactory().registerKey(bc, udf!!.getName(), false)
            val args: Array<Argument?> = udf!!.getArguments()

            // no arguments
            if (args.size == 0) {
                bc.getAdapter().getStatic(Types.CONSTANTS, "EMPTY_OBJECT_ARRAY", Types.OBJECT_ARRAY)
            } else ExpressionUtil.writeOutExpressionArray(bc, Types.OBJECT, args)
            var type: Int
            if (udf!!.getSafeNavigated()) {
                type = THREE
                val `val`: Expression = udf!!.getSafeNavigatedValue()
                type = if (`val` == null) {
                    ASMConstants.NULL(bc.getAdapter())
                    THREE
                } else {
                    `val`.writeOut(bc, Expression.MODE_REF)
                    THREE2
                }
            } else type = TWO
            bc.getAdapter().invokeVirtual(if (udf!!.getSafeNavigated()) Types.PAGE_CONTEXT_IMPL else Types.PAGE_CONTEXT,
                    if (udf!!.hasNamedArgs()) GET_FUNCTION_WITH_NAMED_ARGS!![type] else GET_FUNCTION!![type])
            return Types.OBJECT
        }

        @Throws(TransformerException::class)
        private fun getMatchingValueAndType(bc: BytecodeContext?, factory: Factory?, flfa: FunctionLibFunctionArg?, nargs: Array<NamedArgument?>?, names: Array<String?>?, line: Position?): VT? {
            val flfan: String = flfa.getName()

            // first search if an argument match
            for (i in nargs.indices) {
                if (names!![i] != null && names[i].equalsIgnoreCase(flfan)) {
                    nargs!![i].setValue(nargs[i].getRawValue(), flfa.getTypeAsString())
                    return VT(nargs[i].getValue(), flfa.getTypeAsString(), i)
                }
            }

            // then check if an alias match
            val alias: String = flfa.getAlias()
            if (!StringUtil.isEmpty(alias)) {
                // String[] arrAlias =
                // lucee.runtime.type.List.toStringArray(lucee.runtime.type.List.trimItems(lucee.runtime.type.List.listToArrayRemoveEmpty(alias,
                // ',')));
                for (i in nargs.indices) {
                    if (names!![i] != null && lucee.runtime.type.util.ListUtil.listFindNoCase(alias, names[i], ",") !== -1) {
                        nargs!![i].setValue(nargs[i].getRawValue(), flfa.getTypeAsString())
                        return VT(nargs[i].getValue(), flfa.getTypeAsString(), i)
                    }
                }
            }

            // if not required return the default value
            if (!flfa.getRequired()) {
                return getDefaultValue(factory, flfa)
            }
            val be = TransformerException(bc, "missing required argument [" + flfan + "] for function [" + flfa.getFunction().getName() + "]", line)
            UDFUtil.addFunctionDoc(be, flfa.getFunction())
            throw be
        }

        private fun getDefaultValue(factory: Factory?, flfa: FunctionLibFunctionArg?): VT? {
            val defaultValue: String = flfa.getDefaultValue()
            val type: String = flfa.getTypeAsString()
            if (defaultValue == null) {
                if (type.equals("boolean") || type.equals("bool")) return VT(factory.FALSE(), type, -1)
                return if (type.equals("number") || type.equals("numeric") || type.equals("double")) VT(factory.NUMBER_ONE(), type, -1) else VT(null, type, -1)
            }
            return VT(factory.toExpression(factory.createLitString(defaultValue), type), type, -1)
        }

        @Throws(TransformerException::class)
        private fun getName(bc: BytecodeContext?, expr: Expression?): String? {
            return ASMUtil.toString(bc, expr)
                    ?: throw TransformerException(bc, "cannot extract a string from an object of type [" + expr.getClass().getName().toString() + "]", null)
        }

        @Throws(TransformerException::class)
        private fun getNames(bc: BytecodeContext?, args: Array<NamedArgument?>?): Array<String?>? {
            val names = arrayOfNulls<String?>(args!!.size)
            for (i in args.indices) {
                names[i] = getName(bc, args[i]!!.getName())
            }
            return names
        }

        /**
         * translate an array of arguments to an array of NamedArguments, attention no check if the elements
         * are really named arguments
         *
         * @param args
         * @return
         */
        private fun toNamedArguments(args: Array<Argument?>?): Array<NamedArgument?>? {
            val nargs: Array<NamedArgument?> = arrayOfNulls<NamedArgument?>(args!!.size)
            for (i in args.indices) {
                nargs[i] = args!![i] as NamedArgument?
            }
            return nargs
        }

        /**
         * check if the arguments are named arguments or regular arguments, throws an exception when mixed
         *
         * @param funcName
         * @param args
         * @param line
         * @return
         * @throws TransformerException
         */
        @Throws(TransformerException::class)
        private fun isNamed(bc: BytecodeContext?, funcName: String?, args: Array<Argument?>?): Boolean {
            if (ArrayUtil.isEmpty(args)) return false
            var named = false
            var unNamed = false
            for (i in args.indices) {
                if (args!![i] is NamedArgument) named = true else unNamed = true
                if (named && unNamed) throw TransformerException(bc, "Invalid argument for function [ $funcName ], You can't mix named and unNamed arguments", args[i].getStart())
            }
            return named
        }
    }
}

internal class VT(value: Expression?, type: String?, index: Int) {
    var value: Expression?
    var type: String?
    var index: Int

    init {
        this.value = value
        this.type = type
        this.index = index
    }
}