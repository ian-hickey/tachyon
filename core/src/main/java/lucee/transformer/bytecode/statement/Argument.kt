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
package lucee.transformer.bytecode.statement

import java.util.Map

class Argument(name: Expression?, type: Expression?, required: Expression?, defaultValue: Expression?, passByReference: ExprBoolean?, displayName: Expression?, hint: Expression?,
               meta: Map?) {
    private val name: ExprString?
    private val type: ExprString?
    private val required: ExprBoolean?
    private val defaultValue: Expression?
    private val displayName: ExprString?
    private val hint: ExprString?
    private val meta: Map?
    private val passByReference: ExprBoolean?
    private fun litString(expr: Expression?, defaultValue: LitString?): LitString? {
        val str: ExprString = expr.getFactory().toExprString(expr)
        return if (str is LitString) str as LitString else defaultValue
    }

    /**
     * @return the defaultValue
     */
    fun getDefaultValue(): Expression? {
        return defaultValue
    }

    fun getDefaultValueType(f: Factory?): Expression? {
        if (defaultValue == null) return f.createLitInteger(FunctionArgument.DEFAULT_TYPE_NULL)
        return if (defaultValue is Literal) f.createLitInteger(FunctionArgument.DEFAULT_TYPE_LITERAL) else f.createLitInteger(FunctionArgument.DEFAULT_TYPE_RUNTIME_EXPRESSION)
    }

    /**
     * @return the displayName
     */
    fun getDisplayName(): ExprString? {
        return displayName
    }

    /**
     * @return the hint
     */
    fun getHint(): ExprString? {
        return hint
    }

    /**
     * @return the name
     */
    fun getName(): ExprString? {
        return name
    }

    /**
     * @return the passBy
     */
    fun isPassByReference(): ExprBoolean? {
        return passByReference
    }

    /**
     * @return the required
     */
    fun getRequired(): ExprBoolean? {
        return required
    }

    fun getType(): ExprString? {
        return type
    }

    fun getMetaData(): Map? {
        return meta
    }

    /**
     * Constructor of the class
     *
     * @param name
     * @param type
     * @param required
     * @param defaultValue
     * @param displayName
     * @param hint
     * @param hint2
     * @param meta
     */
    init {
        val re: LitString = name.getFactory().createLitString("[runtime expression]")
        this.name = name.getFactory().toExprString(name)
        this.type = name.getFactory().toExprString(type)
        this.required = name.getFactory().toExprBoolean(required)
        this.defaultValue = defaultValue
        this.displayName = litString(name.getFactory().toExprString(displayName), re)
        this.hint = litString(hint, re)
        this.passByReference = passByReference
        this.meta = meta
    }
}