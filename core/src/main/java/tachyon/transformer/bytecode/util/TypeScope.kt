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
package tachyon.transformer.bytecode.util

import org.objectweb.asm.Type

object TypeScope {
    var SCOPE_UNDEFINED_LOCAL = 16
    val SCOPE: Type? = Type.getType(Scope::class.java)
    val SCOPES: Array<Type?>? = arrayOfNulls<Type?>(ScopeSupport.SCOPE_COUNT)
    val METHODS: Array<Method?>? = arrayOfNulls<Method?>(ScopeSupport.SCOPE_COUNT + 1)

    // Argument argumentsScope (boolean)
    val METHOD_ARGUMENT_BIND: Method? = Method("argumentsScope", SCOPES!![Scope.SCOPE_ARGUMENTS], arrayOf<Type?>(Types.BOOLEAN_VALUE))
    val METHOD_VAR_BIND: Method? = Method("localScope", SCOPES!![ScopeSupport.SCOPE_VAR], arrayOf<Type?>(Types.BOOLEAN_VALUE))
    val METHOD_LOCAL_EL: Method? = Method("localGet", Types.OBJECT, arrayOf<Type?>(Types.BOOLEAN_VALUE, Types.OBJECT))
    val METHOD_LOCAL_BIND: Method? = Method("localGet", Types.OBJECT, arrayOf<Type?>(Types.BOOLEAN_VALUE))
    val METHOD_LOCAL_TOUCH: Method? = Method("localTouch", Types.OBJECT, arrayOf<Type?>())

    // public final static Method METHOD_THIS_BINDX=new Method("thisGet",Types.OBJECT,new
    // Type[]{Types.BOOLEAN_VALUE});
    // public final static Method METHOD_THIS_TOUCHX=new Method("thisTouch", Types.OBJECT,new Type[]{});
    val SCOPE_ARGUMENT: Type? = Type.getType(Argument::class.java)
    fun invokeScope(adapter: GeneratorAdapter?, scope: Int): Type? {
        return if (scope == SCOPE_UNDEFINED_LOCAL) {
            adapter.checkCast(Types.PAGE_CONTEXT_IMPL)
            invokeScope(adapter, METHODS!![scope], Types.PAGE_CONTEXT_IMPL)
        } else invokeScope(adapter, METHODS!![scope], Types.PAGE_CONTEXT)
    }

    fun invokeScope(adapter: GeneratorAdapter?, m: Method?, type: Type?): Type? {
        var type: Type? = type
        if (type == null) type = Types.PAGE_CONTEXT
        adapter.invokeVirtual(type, m)
        return m.getReturnType()
    }

    init {
        SCOPES!![Scope.SCOPE_APPLICATION] = Type.getType(Application::class.java)
        SCOPES[Scope.SCOPE_ARGUMENTS] = Type.getType(tachyon.runtime.type.scope.Argument::class.java)
        SCOPES[Scope.SCOPE_CGI] = Type.getType(CGI::class.java)
        SCOPES[Scope.SCOPE_CLIENT] = Type.getType(Client::class.java)
        SCOPES[Scope.SCOPE_COOKIE] = Type.getType(Cookie::class.java)
        SCOPES[Scope.SCOPE_FORM] = Type.getType(Form::class.java)
        SCOPES[Scope.SCOPE_LOCAL] = Type.getType(Local::class.java)
        SCOPES[Scope.SCOPE_REQUEST] = Type.getType(Request::class.java)
        SCOPES[Scope.SCOPE_SERVER] = Type.getType(Server::class.java)
        SCOPES[Scope.SCOPE_SESSION] = Type.getType(Session::class.java)
        SCOPES[Scope.SCOPE_UNDEFINED] = Type.getType(Undefined::class.java)
        SCOPES[Scope.SCOPE_URL] = Type.getType(URL::class.java)
        SCOPES[Scope.SCOPE_VARIABLES] = Types.VARIABLES
        SCOPES[Scope.SCOPE_CLUSTER] = Type.getType(Cluster::class.java)
        SCOPES[Scope.SCOPE_VAR] = SCOPES[Scope.SCOPE_LOCAL]
        // SCOPES[SCOPE_UNDEFINED_LOCAL]= SCOPES[Scope.SCOPE_LOCAL];
    }

    init {
        METHODS!![Scope.SCOPE_APPLICATION] = Method("applicationScope", SCOPES!![Scope.SCOPE_APPLICATION], arrayOf<Type?>())
        METHODS[Scope.SCOPE_ARGUMENTS] = Method("argumentsScope", SCOPES!![Scope.SCOPE_ARGUMENTS], arrayOf<Type?>())
        METHODS[Scope.SCOPE_CGI] = Method("cgiScope", SCOPES!![Scope.SCOPE_CGI], arrayOf<Type?>())
        METHODS[Scope.SCOPE_CLIENT] = Method("clientScope", SCOPES!![Scope.SCOPE_CLIENT], arrayOf<Type?>())
        METHODS[Scope.SCOPE_COOKIE] = Method("cookieScope", SCOPES!![Scope.SCOPE_COOKIE], arrayOf<Type?>())
        METHODS[Scope.SCOPE_FORM] = Method("formScope", SCOPES!![Scope.SCOPE_FORM], arrayOf<Type?>())
        METHODS[Scope.SCOPE_LOCAL] = Method("localGet", Types.OBJECT, arrayOf<Type?>())
        METHODS[Scope.SCOPE_REQUEST] = Method("requestScope", SCOPES!![Scope.SCOPE_REQUEST], arrayOf<Type?>())
        METHODS[Scope.SCOPE_SERVER] = Method("serverScope", SCOPES!![Scope.SCOPE_SERVER], arrayOf<Type?>())
        METHODS[Scope.SCOPE_SESSION] = Method("sessionScope", SCOPES!![Scope.SCOPE_SESSION], arrayOf<Type?>())
        METHODS[Scope.SCOPE_UNDEFINED] = Method("us", SCOPES!![Scope.SCOPE_UNDEFINED], arrayOf<Type?>())
        METHODS[Scope.SCOPE_URL] = Method("urlScope", SCOPES!![Scope.SCOPE_URL], arrayOf<Type?>())
        METHODS[Scope.SCOPE_VARIABLES] = Method("variablesScope", SCOPES!![Scope.SCOPE_VARIABLES], arrayOf<Type?>())
        METHODS[Scope.SCOPE_CLUSTER] = Method("clusterScope", SCOPES!![Scope.SCOPE_CLUSTER], arrayOf<Type?>())
        METHODS[Scope.SCOPE_VAR] = Method("localScope", SCOPES!![Scope.SCOPE_VAR], arrayOf<Type?>())
        METHODS[SCOPE_UNDEFINED_LOCAL] = Method("usl", SCOPE, arrayOf<Type?>())
    }
}