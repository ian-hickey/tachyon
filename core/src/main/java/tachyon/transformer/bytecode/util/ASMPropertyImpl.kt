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

class ASMPropertyImpl : ASMProperty {
    private var type: Type?
    private var name: String?
    private var clazz: Class? = null

    constructor(type: Class?, name: String?) {
        this.type = ASMUtil.toType(type, true)
        this.name = name
        clazz = type
    }

    constructor(type: String?, name: String?) {
        this.type = ASMUtil.toType(type, true)
        this.name = name
    }

    constructor(type: Type?, name: String?) {
        this.type = type
        this.name = name
    }

    /**
     * @return the name
     */
    @Override
    override fun getName(): String? {
        return name
    }

    /**
     * @return the type
     */
    @Override
    override fun getASMType(): Type? {
        return type
    }

    /**
     *
     * @see java.lang.Object.toString
     */
    @Override
    override fun toString(): String {
        return "class:" + (if (clazz == null) null else clazz.getName()) + ";name:" + name + ";type:" + type.getClassName()
    }

    /**
     * @return the clazz
     */
    @Override
    override fun getClazz(): Class? {
        return clazz
    }
}