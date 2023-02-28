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
package lucee.transformer.bytecode

import java.util.ArrayList

class ConstrBytecodeContext(ps: PageSource?, page: Page?, keys: List<LitString?>?, classWriter: ClassWriter?, className: String?, adapter: GeneratorAdapter?, method: Method?,
                            writeLog: Boolean, suppressWSbeforeArg: Boolean, output: Boolean, returnValue: Boolean) : BytecodeContext(ps, null, page, keys, classWriter, className, adapter, method, writeLog, suppressWSbeforeArg, output, returnValue) {
    private val properties: List<Data?>? = ArrayList<Data?>()
    fun addUDFProperty(function: Function?, arrayIndex: Int, valueIndex: Int, type: Int) {
        properties.add(Data(function, arrayIndex, valueIndex, type))
    }

    fun getUDFProperties(): List<Data?>? {
        return properties
    }

    /*
	 * cga.visitVarInsn(ALOAD, 0); cga.visitFieldInsn(GETFIELD, bc.getClassName(), "udfs",
	 * Types.UDF_PROPERTIES_ARRAY.toString()); cga.push(arrayIndex);
	 * createUDFProperties(constr,valueIndex,type); cga.visitInsn(AASTORE);
	 */
    class Data(function: Function?, arrayIndex: Int, valueIndex: Int, type: Int) {
        val function: Function?
        val arrayIndex: Int
        val valueIndex: Int
        val type: Int

        init {
            this.function = function
            this.arrayIndex = arrayIndex
            this.valueIndex = valueIndex
            this.type = type
        }
    }
}