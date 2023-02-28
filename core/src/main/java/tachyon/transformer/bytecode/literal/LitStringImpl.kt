/**
 * Copyright (c) 2015, Tachyon Assosication Switzerland. All rights reserved.
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
package tachyon.transformer.bytecode.literal

import org.objectweb.asm.Opcodes

/**
 * A Literal String
 */
class LitStringImpl
/**
 * constructor of the class
 *
 * @param str
 * @param line
 */(f: Factory?, private var str: String?, start: Position?, end: Position?) : ExpressionBase(f, start, end), LitString, ExprString {
    private var fromBracket = false
    @Override
    fun getString(): String? {
        return str
    }

    @Override
    @Throws(TransformerException::class)
    fun _writeOut(bc: BytecodeContext?, mode: Int): Type? {
        return _writeOut(bc, mode, str)
    }

    @Throws(TransformerException::class)
    fun writeOut(bc: BytecodeContext?, mode: Int, caseType: Int): Type? {
        if (TYPE_UPPER == caseType) return _writeOut(bc, mode, str.toUpperCase())
        return if (TYPE_LOWER == caseType) _writeOut(bc, mode, str.toLowerCase()) else _writeOut(bc, mode, str)
    }

    @Override
    fun getNumber(defaultValue: Number?): Number? {
        val res: Number
        res = if (AppListenerUtil.getPreciseMath(null, null)) Caster.toBigDecimal(str, null) else Caster.toDouble(getString(), null)
        return res ?: defaultValue
    }

    @Override
    fun getBoolean(defaultValue: Boolean?): Boolean? {
        return Caster.toBoolean(getString(), defaultValue)
    }

    @Override
    override fun equals(obj: Object?): Boolean {
        if (this === obj) return true
        return if (obj !is LitString) false else str!!.equals((obj as LitStringImpl?)!!.getString())
    }

    @Override
    override fun toString(): String {
        return str!!
    }

    @Override
    fun upperCase() {
        str = str.toUpperCase()
    }

    fun lowerCase() {
        str = str.toLowerCase()
    }

    @Override
    fun duplicate(): LitString? {
        return LitStringImpl(getFactory(), str, getStart(), getEnd())
    }

    @Override
    fun fromBracket(fromBracket: Boolean) {
        this.fromBracket = fromBracket
    }

    @Override
    fun fromBracket(): Boolean {
        return fromBracket
    }

    companion object {
        const val MAX_SIZE = 65535
        const val TYPE_ORIGINAL = 0
        const val TYPE_UPPER = 1
        const val TYPE_LOWER = 2

        /**
         * @see tachyon.transformer.expression.Expression._writeOut
         */
        @Throws(TransformerException::class)
        private fun _writeOut(bc: BytecodeContext?, mode: Int, str: String?): Type? {
            // write to a file instead to the bytecode
            // str(0,10);
            // print.ds(str);
            val externalizeStringGTE: Int = (bc.getConfig() as ConfigPro).getExternalizeStringGTE()
            if (externalizeStringGTE > -1 && str!!.length() > externalizeStringGTE && StringUtil.indexOfIgnoreCase(bc.getMethod().getName(), "call") !== -1) {
                try {
                    val ga: GeneratorAdapter = bc.getAdapter()
                    val page: Page = bc.getPage()
                    val range: Range = page.registerString(bc, str)
                    if (range != null) {
                        ga.visitVarInsn(Opcodes.ALOAD, 0)
                        ga.visitVarInsn(Opcodes.ALOAD, 1)
                        ga.push(range.from)
                        ga.push(range.to)
                        ga.visitMethodInsn(Opcodes.INVOKEVIRTUAL, bc.getClassName(), "str", "(Ltachyon/runtime/PageContext;II)Ljava/lang/String;")
                        return Types.STRING
                    }
                } catch (t: Throwable) {
                    ExceptionUtil.rethrowIfNecessary(t)
                }
            }
            if (toBig(str)) {
                _toExpr(bc.getFactory(), str).writeOut(bc, mode)
            } else {
                bc.getAdapter().push(str)
            }
            return Types.STRING
        }

        private fun toBig(str: String?): Boolean {
            return if (str == null || str.length() < MAX_SIZE / 2) false else str.getBytes(CharsetUtil.UTF8).length > MAX_SIZE // a char is max 2 bytes
        }

        private fun _toExpr(factory: Factory?, str: String?): ExprString? {
            val size: Int = str!!.length() / 2
            val l: String = str.substring(0, size)
            val r: String = str.substring(size)
            val left: ExprString = if (toBig(l)) _toExpr(factory, l) else factory.createLitString(l)
            val right: ExprString = if (toBig(r)) _toExpr(factory, r) else factory.createLitString(r)
            return factory.opString(left, right, false)
        }
    }
    /*
	 * public static ExprString toExprString(String str, Position start,Position end) { return new
	 * LitStringImpl(str,start,end); }
	 * 
	 * public static ExprString toExprString(String str) { return new LitStringImpl(str,null,null); }
	 * 
	 * public static LitString toLitString(String str) { return new LitStringImpl(str,null,null); }
	 */
}