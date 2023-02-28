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
package tachyon.transformer.bytecode.expression.`var`

import tachyon.runtime.db.ClassDefinition

class BIF(factory: Factory?, ts: TransfomerSettings?, flf: FunctionLibFunction?) : FunctionMember() {
    // private ExprString nameq;
    private var argType = 0
    private var cd: ClassDefinition? = null
    private var returnType = ANY
    private var flf: FunctionLibFunction?
    private val factory: Factory?
    val ts: TransfomerSettings?
    fun getFactory(): Factory? {
        return factory
    }

    fun setArgType(argType: Int) {
        this.argType = argType
    }

    fun setClassDefinition(cd: ClassDefinition?) {
        this.cd = cd
    }

    fun setReturnType(returnType: String?) {
        this.returnType = returnType
    }

    /**
     * @return the argType
     */
    fun getArgType(): Int {
        return argType
    }

    /**
     * @return the class
     */
    fun getClassDefinition(): ClassDefinition? {
        return cd
    }
    /**
     * @return the name
     *
     * public ExprString getNameX() { return name; }
     */
    /**
     * @return the returnType
     */
    fun getReturnType(): String? {
        return returnType
    }

    /**
     * @return the flf
     */
    fun getFlf(): FunctionLibFunction? {
        return flf
    }

    /**
     * @param flf the flf to set
     */
    fun setFlf(flf: FunctionLibFunction?) {
        this.flf = flf
    }

    companion object {
        private val ANY: String? = "any"
    }

    init {
        this.ts = ts
        // this.name=name;
        this.flf = flf
        this.factory = factory // name.getFactory();
    }
}