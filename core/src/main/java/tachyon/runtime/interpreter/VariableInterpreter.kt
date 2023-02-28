/**
 * Copyright (c) 2014, the Railo Company Ltd.
 * Copyright (c) 2015, Tachyon Assosication Switzerland
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
package tachyon.runtime.interpreter

import tachyon.commons.lang.ParserString

/**
 * Class to check and interpret Variable Strings
 */
object VariableInterpreter {
    /**
     * reads a subelement from a struct
     *
     * @param pc
     * @param collection
     * @param var
     * @return matching Object
     * @throws PageException
     */
    @Throws(PageException::class)
    fun getVariable(pc: PageContext?, collection: Collection?, `var`: String?): Object? {
        var collection: Collection? = collection
        val list: StringList = parse(pc, ParserString(`var`!!), false)
                ?: throw InterpreterException("invalid variable declaration [$`var`]")
        while (list.hasNextNext()) {
            collection = Caster.toCollection(collection.get(KeyImpl.init(list.next())))
        }
        return collection.get(KeyImpl.init(list.next()))
    }

    fun scopeInt2String(type: Int): String? {
        when (type) {
            Scope.SCOPE_APPLICATION -> return "application"
            Scope.SCOPE_ARGUMENTS -> return "arguments"
            Scope.SCOPE_CGI -> return "cgi"
            Scope.SCOPE_COOKIE -> return "cookie"
            Scope.SCOPE_CLIENT -> return "client"
            Scope.SCOPE_FORM -> return "form"
            Scope.SCOPE_REQUEST -> return "request"
            Scope.SCOPE_SESSION -> return "session"
            Scope.SCOPE_SERVER -> return "server"
            Scope.SCOPE_URL -> return "url"
            Scope.SCOPE_VARIABLES -> return "variables"
            Scope.SCOPE_CLUSTER -> return "cluster"
            Scope.SCOPE_LOCAL -> return "local"
        }
        return null
    }

    fun getVariableEL(pc: PageContext?, collection: Collection?, `var`: String?): Object? {
        var collection: Collection? = collection
        val list: StringList = parse(pc, ParserString(`var`!!), false)
                ?: return null
        while (list.hasNextNext()) {
            collection = Caster.toCollection(collection.get(list.next(), null), null)
            if (collection == null) return null
        }
        return collection.get(list.next(), null)
    }

    /**
     * get a variable from page context
     *
     * @param pc Page Context
     * @param var variable string to get value to
     * @return the value
     * @throws PageException
     */
    @Throws(PageException::class)
    fun getVariable(pc: PageContext?, `var`: String?): Object? {
        val list: StringList = parse(pc, ParserString(`var`!!), false)
                ?: throw InterpreterException("invalid variable declaration [$`var`]")
        val scope = scopeString2Int(pc.ignoreScopes(), list.next())
        var coll: Object? = null
        coll = if (scope == Scope.SCOPE_UNDEFINED) {
            pc.undefinedScope().get(list.current())
        } else {
            scope(pc, scope, list.hasNext())
        }
        while (list.hasNext()) {
            coll = pc.getVariableUtil().get(pc, coll, list.next())
        }
        return coll
    }

    @Throws(PageException::class)
    fun getVariableAsCollection(pc: PageContext?, `var`: String?): Object? {
        val list: StringList = parse(pc, ParserString(`var`!!), false)
                ?: throw InterpreterException("invalid variable declaration [$`var`]")
        val scope = scopeString2Int(pc.ignoreScopes(), list.next())
        var coll: Object? = null
        coll = if (scope == Scope.SCOPE_UNDEFINED) {
            pc.undefinedScope().getCollection(list.current())
        } else {
            scope(pc, scope, list.hasNext())
        }
        while (list.hasNext()) {
            coll = pc.getVariableUtil().getCollection(pc, coll, list.next())
        }
        return coll
    }

    @Throws(PageException::class)
    fun getVariable(pc: PageContext?, str: String?, scope: Scope?): Object? {
        return _variable(pc, str, CollectionUtil.NULL, scope)
    }

    @Throws(PageException::class)
    fun setVariable(pc: PageContext?, str: String?, value: Object?, scope: Scope?): Object? {
        return _variable(pc, str, value, scope)
    }

    @Throws(PageException::class)
    fun _variable(pc: PageContext?, str: String?, value: Object?, scope: Scope?): Object? {
        // define another environment for the function
        if (scope != null) {

            // Variables Scope
            var `var`: Variables? = null
            if (scope is Variables) {
                `var` = scope as Variables?
            } else if (scope is CallerImpl) {
                `var` = (scope as CallerImpl?).getVariablesScope()
            }
            if (`var` != null) {
                val current: Variables = pc.variablesScope()
                if (current !== `var`) pc.setVariablesScope(`var`)
                return try {
                    if (value !== CollectionUtil.NULL) setVariable(pc, str, value) else getVariable(pc, str)
                } finally {
                    if (current !== `var`) pc.setVariablesScope(current)
                }
            } else if (scope is Undefined) {
                val pci: PageContextImpl? = pc as PageContextImpl?
                val undefined: Undefined? = scope as Undefined?
                val check: Boolean = undefined.getCheckArguments()
                val orgVar: Variables = pc.variablesScope()
                val orgArgs: Argument = pc.argumentsScope()
                val orgLocal: Local = pc.localScope()
                val vs: Variables = undefined.variablesScope()
                if (vs !== orgVar) pci.setVariablesScope(vs)
                if (check) pci.setFunctionScopes(undefined.localScope(), undefined.argumentsScope())
                return try {
                    if (value !== CollectionUtil.NULL) setVariable(pc, str, value) else getVariable(pc, str)
                } finally {
                    if (vs !== orgVar) pc.setVariablesScope(orgVar)
                    if (check) pci.setFunctionScopes(orgLocal, orgArgs)
                }
            }
        }
        return if (value !== CollectionUtil.NULL) setVariable(pc, str, value) else getVariable(pc, str)
    }

    /**
     * get a variable from page context
     *
     * @param pc Page Context
     * @param var variable string to get value to
     * @param defaultValue value returnded if variable was not found
     * @return the value or default value if not found
     */
    fun getVariableEL(pc: PageContext?, `var`: String?, defaultValue: Object?): Object? {
        val list: StringList = parse(pc, ParserString(`var`!!), false)
                ?: return defaultValue
        val _null: Object = NullSupportHelper.NULL(pc)
        val scope = scopeString2Int(pc.ignoreScopes(), list.next())
        var coll: Object? = null
        if (scope == Scope.SCOPE_UNDEFINED) {
            coll = pc.undefinedScope().get(KeyImpl.init(list.current()), _null)
            if (coll === _null) return defaultValue
        } else {
            coll = try {
                scope(pc, scope, list.hasNext())
                // coll=pc.scope(scope);
            } catch (e: PageException) {
                return defaultValue
            }
        }
        while (list.hasNext()) {
            coll = pc.getVariableUtil().get(pc, coll, KeyImpl.init(list.next()), _null)
            if (coll === _null) return defaultValue
        }
        return coll
    }

    fun getVariableELAsCollection(pc: PageContext?, `var`: String?, defaultValue: Object?): Object? {
        val list: StringList = parse(pc, ParserString(`var`!!), false)
                ?: return defaultValue
        val scope = scopeString2Int(pc.ignoreScopes(), list.next())
        var coll: Object? = null
        if (scope == Scope.SCOPE_UNDEFINED) {
            coll = try {
                pc.undefinedScope().getCollection(list.current())
            } catch (e: PageException) {
                null
            }
            if (coll == null) return defaultValue
        } else {
            coll = try {
                scope(pc, scope, list.hasNext())
                // coll=pc.scope(scope);
            } catch (e: PageException) {
                return defaultValue
            }
        }
        while (list.hasNext()) {
            coll = pc.getVariableUtil().getCollection(pc, coll, list.next(), null)
            if (coll == null) return defaultValue
        }
        return coll
    }

    /**
     * return a variable reference by string syntax ("scopename.key.key" -> "url.name") a variable
     * reference, references to variable, to modifed it, with global effect.
     *
     * @param pc
     * @param var variable name to get
     * @return variable as Reference
     * @throws PageException
     */
    @Throws(PageException::class)
    fun getVariableReference(pc: PageContext?, `var`: String?): VariableReference? {
        val list: StringList = parse(pc, ParserString(`var`!!), false)
                ?: throw InterpreterException("invalid variable declaration [$`var`]")
        if (list.size() === 1) {
            return VariableReference(pc.undefinedScope(), list.next())
        }
        val scope = scopeString2Int(pc.ignoreScopes(), list.next())
        var coll: Object
        coll = if (scope == Scope.SCOPE_UNDEFINED) {
            pc.touch(pc.undefinedScope(), KeyImpl.init(list.current()))
        } else {
            scope(pc, scope, list.hasNext())
            // coll=pc.scope(scope);
        }
        while (list.hasNextNext()) {
            coll = pc.touch(coll, KeyImpl.init(list.next()))
        }
        if (coll !is Collection) throw InterpreterException("invalid variable [$`var`]")
        return VariableReference(coll as Collection, list.next())
    }

    fun getVariableReference(pc: PageContext?, key: Collection.Key?, keepScope: Boolean): VariableReference? {
        if (keepScope) {
            val coll: Collection = (pc.undefinedScope() as UndefinedImpl).getScopeFor(key, null)
            if (coll != null) return VariableReference(coll, key)
        }
        return VariableReference(pc.undefinedScope(), key)
    }

    @Throws(PageException::class)
    fun getVariableReference(pc: PageContext?, keys: Array<Collection.Key?>?, keepScope: Boolean): VariableReference? {
        if (keys!!.size == 1) {
            if (keepScope) {
                val coll: Collection = (pc.undefinedScope() as UndefinedImpl).getScopeFor(keys[0], null)
                if (coll != null) return VariableReference(coll, keys[0])
            }
            return VariableReference(pc.undefinedScope(), keys[0])
        }
        val scope = scopeKey2Int(keys[0])
        var coll: Object
        coll = if (scope == Scope.SCOPE_UNDEFINED) {
            pc.touch(pc.undefinedScope(), keys[0])
        } else {
            scope(pc, scope, keys.size > 1)
        }
        for (i in 1 until keys.size - 1) {
            coll = pc.touch(coll, keys[i])
        }
        if (coll !is Collection) throw InterpreterException("invalid variable [" + ListUtil.arrayToList(keys, ".").toString() + "]")
        return VariableReference(coll as Collection, keys[keys.size - 1])
    }

    /**
     * sets a variable to page Context
     *
     * @param pc pagecontext of the new variable
     * @param var String of variable definition
     * @param value value to set to variable
     * @return value setted
     * @throws PageException
     */
    @Throws(PageException::class)
    fun setVariable(pc: PageContext?, `var`: String?, value: Object?): Object? {
        val list: StringList = parse(pc, ParserString(`var`!!), false)
                ?: throw InterpreterException("invalid variable name declaration [$`var`]")
        if (list.size() === 1) {
            return pc.undefinedScope().set(list.next(), value)
        }

        // min 2 elements
        val scope = scopeString2Int(pc.ignoreScopes(), list.next())
        var coll: Object
        coll = if (scope == Scope.SCOPE_UNDEFINED) {
            pc.touch(pc.undefinedScope(), KeyImpl.init(list.current()))
        } else {
            scope(pc, scope, true)
            // coll=pc.scope(scope);
        }
        while (list.hasNextNext()) {
            coll = pc.touch(coll, KeyImpl.init(list.next()))
        }
        return pc.set(coll, KeyImpl.init(list.next()), value)
    }

    /**
     * removes a variable eith matching name from page context
     *
     * @param pc
     * @param var
     * @return has removed or not
     * @throws PageException
     */
    @Throws(PageException::class)
    fun removeVariable(pc: PageContext?, `var`: String?): Object? {
        // print.ln("var:"+var);
        val list: StringList = parse(pc, ParserString(`var`!!), false)
                ?: throw InterpreterException("invalid variable declaration [$`var`]")
        if (list.size() === 1) {
            return pc.undefinedScope().remove(KeyImpl.init(list.next()))
        }
        val scope = scopeString2Int(pc.ignoreScopes(), list.next())
        var coll: Object
        coll = if (scope == Scope.SCOPE_UNDEFINED) {
            pc.undefinedScope().get(list.current())
        } else {
            scope(pc, scope, true)
            // coll=pc.scope(scope);
        }
        while (list.hasNextNext()) {
            coll = pc.get(coll, list.next())
        }
        return Caster.toCollection(coll).remove(KeyImpl.init(list.next()))
    }

    /**
     * check if a variable is defined in Page Context
     *
     * @param pc PageContext to check
     * @param var variable String
     * @return exists or not
     */
    fun isDefined(pc: PageContext?, `var`: String?): Boolean {
        val list: StringList = parse(pc, ParserString(`var`!!), false)
                ?: return false
        try {
            val scope = scopeString2Int(pc.ignoreScopes(), list.next())
            var coll: Object? = CollectionUtil.NULL
            if (scope == Scope.SCOPE_UNDEFINED) {
                coll = pc.undefinedScope().get(list.current(), null)
                if (coll == null) return false
            } else {
                coll = scope(pc, scope, list.hasNext())
                // coll=pc.scope(scope);
            }
            while (list.hasNext()) {
                coll = pc.getVariableUtil().getCollection(pc, coll, list.next(), null)
                if (coll == null) return false
            }
        } catch (e: PageException) {
            return false
        }
        return true
    }
    /*
	 * public static boolean isDefined(PageContext pc,String var) { StringList list = parse(pc,new
	 * ParserString(var)); if(list==null) return false;
	 * 
	 * int scope=scopeString2Int(list.next()); Object coll =NULL; if(scope==Scope.SCOPE_UNDEFINED) {
	 * coll=pc.undefinedScope().get(list.current(),NULL); if(coll==NULL) return false; } else { try {
	 * coll=pc.scope(scope); } catch (PageException e) { return false; } }
	 * 
	 * while(list.hasNext()) { coll=pc.getVariableUtil().get(pc,coll,list.next(),NULL);
	 * //print.out(coll); if(coll==NULL) return false; }
	 * 
	 * return true; }
	 */
    /**
     * parse a Literal variable String and return result as String List
     *
     * @param pc Page Context
     * @param ps ParserString to read
     * @return Variable Definition in a String List
     */
    private fun parse(pc: PageContext?, ps: ParserString?, doLowerCase: Boolean): StringList? {
        var id: String? = readIdentifier(ps, doLowerCase) ?: return null
        val list = StringList(id)
        var interpreter: CFMLExpressionInterpreter? = null
        while (true) {
            if (ps!!.forwardIfCurrent('.')) {
                id = readIdentifier(ps, doLowerCase)
                if (id == null) return null
                list.add(id)
            } else if (ps!!.forwardIfCurrent('[')) {
                if (interpreter == null) interpreter = CFMLExpressionInterpreter(false)
                try {
                    list.add(Caster.toString(interpreter.interpretPart(pc, ps)))
                } catch (e: PageException) {
                    return null
                }
                if (!ps!!.forwardIfCurrent(']')) return null
                ps!!.removeSpace()
            } else break
        }
        if (ps!!.isValidIndex()) return null
        list.reset()
        return list
    }

    fun parse(`var`: String?, doLowerCase: Boolean): StringList? {
        val ps = ParserString(`var`!!)
        var id: String? = readIdentifier(ps, doLowerCase) ?: return null
        val list = StringList(id)
        while (true) {
            if (ps!!.forwardIfCurrent('.')) {
                id = readIdentifier(ps, doLowerCase)
                if (id == null) return null
                list.add(id)
            } else break
        }
        if (ps!!.isValidIndex()) return null
        list.reset()
        return list
    }

    /**
     * translate a string type definition to its int representation
     *
     * @param type type to translate
     * @return int representation matching to given string
     */
    fun scopeString2Int(ignoreScope: Boolean, type: String?): Int {
        var type = type
        type = StringUtil.toLowerCase(type)
        val c: Char = type.charAt(0)

        // ignore scope only handles only reconize local,arguments as scope, the rest is ignored
        if (ignoreScope) {
            if ('a' == c) {
                if ("arguments".equals(type)) return Scope.SCOPE_ARGUMENTS
            } else if ('l' == c) {
                if ("local".equals(type)) return Scope.SCOPE_LOCAL // LLL
            } else if ('r' == c) {
                if ("request".equals(type)) return Scope.SCOPE_REQUEST
            } else if ('v' == c) {
                if ("variables".equals(type)) return Scope.SCOPE_VARIABLES
            } else if ('s' == c) {
                if ("server".equals(type)) return Scope.SCOPE_SERVER
            }
            return Scope.SCOPE_UNDEFINED
        }
        if ('a' == c) {
            if ("application".equals(type)) return Scope.SCOPE_APPLICATION else if ("arguments".equals(type)) return Scope.SCOPE_ARGUMENTS
        } else if ('c' == c) {
            if ("cgi".equals(type)) return Scope.SCOPE_CGI
            if ("cookie".equals(type)) return Scope.SCOPE_COOKIE
            if ("client".equals(type)) return Scope.SCOPE_CLIENT
            if ("cluster".equals(type)) return Scope.SCOPE_CLUSTER
        } else if ('f' == c) {
            if ("form".equals(type)) return Scope.SCOPE_FORM
        } else if ('l' == c) {
            if ("local".equals(type)) return Scope.SCOPE_LOCAL // LLL
        } else if ('r' == c) {
            if ("request".equals(type)) return Scope.SCOPE_REQUEST
        } else if ('s' == c) {
            if ("session".equals(type)) return Scope.SCOPE_SESSION
            if ("server".equals(type)) return Scope.SCOPE_SERVER
        } else if ('u' == c) {
            if ("url".equals(type)) return Scope.SCOPE_URL
        } else if ('v' == c) {
            if ("variables".equals(type)) return Scope.SCOPE_VARIABLES
        }
        return Scope.SCOPE_UNDEFINED
    }

    fun scopeKey2Int(type: Collection.Key?): Int {
        val c: Char = type.lowerCharAt(0)
        if ('a' == c) {
            if (KeyConstants._application.equalsIgnoreCase(type)) return Scope.SCOPE_APPLICATION else if (KeyConstants._arguments.equalsIgnoreCase(type)) return Scope.SCOPE_ARGUMENTS
        } else if ('c' == c) {
            if (KeyConstants._cgi.equalsIgnoreCase(type)) return Scope.SCOPE_CGI
            if (KeyConstants._cookie.equalsIgnoreCase(type)) return Scope.SCOPE_COOKIE
            if (KeyConstants._client.equalsIgnoreCase(type)) return Scope.SCOPE_CLIENT
            if (KeyConstants._cluster.equalsIgnoreCase(type)) return Scope.SCOPE_CLUSTER
        } else if ('f' == c) {
            if (KeyConstants._form.equalsIgnoreCase(type)) return Scope.SCOPE_FORM
        } else if ('r' == c) {
            if (KeyConstants._request.equalsIgnoreCase(type)) return Scope.SCOPE_REQUEST
        } else if ('s' == c) {
            if (KeyConstants._session.equalsIgnoreCase(type)) return Scope.SCOPE_SESSION
            if (KeyConstants._server.equalsIgnoreCase(type)) return Scope.SCOPE_SERVER
        } else if ('u' == c) {
            if (KeyConstants._url.equalsIgnoreCase(type)) return Scope.SCOPE_URL
        } else if ('v' == c) {
            if (KeyConstants._variables.equalsIgnoreCase(type)) return Scope.SCOPE_VARIABLES
        }
        return Scope.SCOPE_UNDEFINED
    }

    private fun readIdentifier(ps: ParserString?, doLowerCase: Boolean): String? {
        ps!!.removeSpace()
        if (ps!!.isAfterLast()) return null
        val start: Int = ps.getPos()
        if (!isFirstVarLetter(ps.getCurrentLower())) return null
        ps!!.next()
        while (ps!!.isValidIndex()) {
            if (isVarLetter(ps.getCurrentLower())) ps!!.next() else break
        }
        ps!!.removeSpace()
        return if (doLowerCase) ps!!.substringLower(start, ps.getPos() - start) else ps!!.substring(start, ps.getPos() - start)
    }

    private fun isFirstVarLetter(c: Char): Boolean {
        return c >= 'a' && c <= 'z' || c == '_' || c == '$'
    }

    private fun isVarLetter(c: Char): Boolean {
        return c >= 'a' && c <= 'z' || c >= '0' && c <= '9' || c == '_' || c == '$'
    }

    @Throws(PageException::class)
    fun scope(pc: PageContext?, scope: Int, touch: Boolean): Object? {
        when (scope) {
            Scope.SCOPE_UNDEFINED -> return pc.undefinedScope()
            Scope.SCOPE_URL -> return pc.urlScope()
            Scope.SCOPE_FORM -> return pc.formScope()
            Scope.SCOPE_VARIABLES -> return pc.variablesScope()
            Scope.SCOPE_REQUEST -> return pc.requestScope()
            Scope.SCOPE_CGI -> return pc.cgiScope()
            Scope.SCOPE_APPLICATION -> return pc.applicationScope()
            Scope.SCOPE_ARGUMENTS -> return pc.argumentsScope()
            Scope.SCOPE_SESSION -> return pc.sessionScope()
            Scope.SCOPE_SERVER -> return pc.serverScope()
            Scope.SCOPE_COOKIE -> return pc.cookieScope()
            Scope.SCOPE_CLIENT -> return pc.clientScope()
            Scope.SCOPE_VAR -> return pc.localScope()
            Scope.SCOPE_CLUSTER -> return pc.clusterScope()
            Scope.SCOPE_LOCAL -> {
                return if (touch) (pc as PageContextImpl?).localTouch() else (pc as PageContextImpl?).localGet()
            }
        }
        return pc.variablesScope()
    }
}