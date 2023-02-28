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

object MemberUtil {
    private val DEFAULT: Object? = Object()
    private val matchesTachyon: Map<Short?, Map<Collection.Key?, FunctionLibFunction?>?>? = HashMap<Short?, Map<Collection.Key?, FunctionLibFunction?>?>()
    private val matchesCFML: Map<Short?, Map<Collection.Key?, FunctionLibFunction?>?>? = HashMap<Short?, Map<Collection.Key?, FunctionLibFunction?>?>()
    fun getMembers(pc: PageContext?, type: Short): Map<Collection.Key?, FunctionLibFunction?>? {
        val matches: Map<Short?, Map<Key?, FunctionLibFunction?>?>? = if (pc.getCurrentTemplateDialect() === CFMLEngine.DIALECT_LUCEE) matchesTachyon else matchesCFML
        var match: Map<Key?, FunctionLibFunction?>? = matches!![type]
        if (match != null) return match
        val flds: Array<FunctionLib?> = (pc.getConfig() as ConfigWebPro).getFLDs(pc.getCurrentTemplateDialect())
        var it: Iterator<FunctionLibFunction?>
        var f: FunctionLibFunction?
        match = HashMap<Collection.Key?, FunctionLibFunction?>()
        var names: Array<String?>
        for (i in flds.indices) {
            it = flds[i].getFunctions().values().iterator()
            while (it.hasNext()) {
                f = it.next()
                names = f.getMemberNames()
                if (!ArrayUtil.isEmpty(names) && f.getMemberType() === type && f.getArgType() === FunctionLibFunction.ARG_FIX) {
                    for (y in names.indices) match.put(KeyImpl.init(names[y]), f)
                }
            }
        }
        matches.put(type, match)
        return match
    }

    // used in extension Image
    @Throws(PageException::class)
    fun call(pc: PageContext?, coll: Object?, methodName: Collection.Key?, args: Array<Object?>?, types: ShortArray?, strTypes: Array<String?>?): Object? {
        // look for members
        var type: Short
        var strType: String?
        var members: Map<Key?, FunctionLibFunction?>? = null
        var hasAny = false
        var isChked = false
        for (i in 0..types!!.size) {
            if (i == types.size) {
                if (hasAny) break
                type = CFTypes.TYPE_ANY
                strType = "any"
            } else {
                type = types[i]
                strType = strTypes!![i]
                if (type == CFTypes.TYPE_ANY) hasAny = true
            }
            members = getMembers(pc, type)
            var member: FunctionLibFunction? = members!![methodName]
            if (member == null && !isChked) {
                if (type == CFTypes.TYPE_NUMERIC) {
                    members = getMembers(pc, CFTypes.TYPE_STRING)
                    member = members!![methodName]
                }
                if (type == CFTypes.TYPE_STRING && Decision.isNumber(coll)) {
                    members = getMembers(pc, CFTypes.TYPE_NUMERIC)
                    member = members!![methodName]
                }
                isChked = true
            }
            if (member != null) {
                val _args: List<FunctionLibFunctionArg?> = member.getArg()
                return if (args!!.size < _args.size()) {
                    val refs: ArrayList<Ref?> = ArrayList<Ref?>()
                    val pos: Int = member.getMemberPosition()
                    var flfa: FunctionLibFunctionArg
                    val it: Iterator<FunctionLibFunctionArg?> = _args.iterator()
                    var glbIndex = 0
                    var argIndex = -1
                    while (it.hasNext()) {
                        glbIndex++
                        flfa = it.next()
                        if (glbIndex == pos) {
                            refs.add(Casting(strType, type, coll))
                        } else if (args.size > ++argIndex) { // careful, argIndex is only incremented when condition above is false
                            refs.add(Casting(flfa.getTypeAsString(), flfa.getType(), args[argIndex]))
                        }
                    }
                    BIFCall(coll, member, refs.toArray(arrayOfNulls<Ref?>(refs.size()))).getValue(pc)
                } else throw FunctionException(pc, member.getName(), member.getArgMin(), _args.size(), args.size)
            }
        }

        // do reflection
        if (pc.getConfig().getSecurityManager().getAccess(tachyon.runtime.security.SecurityManager.TYPE_DIRECT_JAVA_ACCESS) === tachyon.runtime.security.SecurityManager.VALUE_YES) {
            if (coll !is Undefined) {
                val res: Object? = callMethod(coll, methodName, args)
                if (res !== DEFAULT) return res
            }
        }

        // merge
        val keys: Set<Key?> = HashSet()
        hasAny = false
        for (i in types.indices) {
            if (types[i] == CFTypes.TYPE_ANY) hasAny = true
            val it: Iterator<Key?> = getMembers(pc, types[i]).keySet().iterator()
            while (it.hasNext()) {
                keys.add(it.next())
            }
        }
        if (!hasAny) {
            val it: Iterator<Key?> = getMembers(pc, CFTypes.TYPE_ANY).keySet().iterator()
            while (it.hasNext()) {
                keys.add(it.next())
            }
        }
        val msg: String = ExceptionUtil.similarKeyMessage(keys.toArray(arrayOfNulls<Key?>(keys.size())), methodName.getString(), "function", "functions",
                if (types.size == 1 && types[0] != CFTypes.TYPE_ANY) StringUtil.ucFirst(CFTypes.toString(types[0], "Object")) else "Object", true)
        val detail: String = ExceptionUtil.similarKeyMessage(keys.toArray(arrayOfNulls<Key?>(keys.size())), methodName.getString(), "functions",
                if (types.size == 1 && types[0] != CFTypes.TYPE_ANY) StringUtil.ucFirst(CFTypes.toString(types[0], "Object")) else "Object", true)
        throw ExpressionException(msg, detail)
    }

    @Throws(PageException::class)
    private fun callMethod(obj: Object?, methodName: Collection.Key?, args: Array<Object?>?): Object? {
        val mi: MethodInstance = Reflector.getMethodInstanceEL(obj, obj.getClass(), methodName, args)
                ?: return DEFAULT
        return try {
            mi.invoke(obj)
        } catch (e: Exception) {
            throw Caster.toPageException(e)
        }
    }

    // used in extension image
    @Throws(PageException::class)
    fun callWithNamedValues(pc: PageContext?, coll: Object?, methodName: Collection.Key?, args: Struct?, type: Short, strType: String?): Object? {
        val members: Map<Key?, FunctionLibFunction?>? = getMembers(pc, type)
        val member: FunctionLibFunction? = members!![methodName]
        if (member != null) {
            val _args: List<FunctionLibFunctionArg?> = member.getArg()
            var arg: FunctionLibFunctionArg?
            return if (args.size() < _args.size()) {
                var `val`: Object
                val refs: ArrayList<Ref?> = ArrayList<Ref?>()
                arg = _args[0]
                refs.add(Casting(arg.getTypeAsString(), arg.getType(), LFunctionValue(LString(arg.getName()), coll)))
                for (y in 1 until _args.size()) {
                    arg = _args[y]

                    // match by name
                    `val` = args.get(arg.getName(), null)

                    // match by alias
                    if (`val` == null) {
                        val alias: String = arg.getAlias()
                        if (!StringUtil.isEmpty(alias, true)) {
                            val aliases: Array<String?> = tachyon.runtime.type.util.ListUtil.trimItems(tachyon.runtime.type.util.ListUtil.listToStringArray(alias, ','))
                            for (x in aliases.indices) {
                                `val` = args.get(aliases[x], null)
                                if (`val` != null) break
                            }
                        }
                    }
                    if (`val` == null) {
                        if (arg.getRequired()) {
                            val names: Array<String?> = member.getMemberNames()
                            val n = if (ArrayUtil.isEmpty(names)) "" else names[0]
                            throw ExpressionException("missing required argument [" + arg.getName().toString() + "] for member function call [" + n.toString() + "]")
                        }
                    } else {
                        refs.add(Casting(arg.getTypeAsString(), arg.getType(), LFunctionValue(LString(arg.getName()), `val`)))
                        // refs.add(new LFunctionValue(new LString(arg.getName()),new
                        // Casting(pc,arg.getTypeAsString(),arg.getType(),val)));
                    }
                }
                BIFCall(coll, member, refs.toArray(arrayOfNulls<Ref?>(refs.size()))).getValue(pc)
            } else {
                throw ExpressionException("There are to many arguments (" + args.size().toString() + ") passed into the member function  [" + methodName
                        .toString() + "], the maximum number of arguments is [" + (_args.size() - 1).toString() + "]")
            }
        }
        throw ExpressionException("No matching function member [" + methodName + "] for call with named arguments found, available function members are ["
                + tachyon.runtime.type.util.ListUtil.sort(CollectionUtil.getKeyList(members.keySet().iterator(), ","), "textnocase", "asc", ",") + "]")
    }
}