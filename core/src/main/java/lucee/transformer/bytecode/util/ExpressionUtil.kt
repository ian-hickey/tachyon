/**
 * Copyright (c) 2014, the Railo Company Ltd.
 * Copyright (c) 2015, Lucee Assosication Switzerland
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
package lucee.transformer.bytecode.util

import java.util.HashMap

object ExpressionUtil {
    private val tokens: ConcurrentHashMap<String?, Object?>? = ConcurrentHashMap<String?, Object?>()
    val START: Method? = Method("exeLogStart", Types.VOID, arrayOf<Type?>(Types.INT_VALUE, Types.STRING))
    val END: Method? = Method("exeLogEnd", Types.VOID, arrayOf<Type?>(Types.INT_VALUE, Types.STRING))
    val CURRENT_LINE: Method? = Method("currentLine", Types.VOID, arrayOf<Type?>(Types.INT_VALUE))
    private val last: Map<String?, String?>? = HashMap<String?, String?>()
    @Throws(TransformerException::class)
    fun writeOutExpressionArray(bc: BytecodeContext?, arrayType: Type?, array: Array<Expression?>?) {
        val adapter: GeneratorAdapter = bc.getAdapter()
        adapter.push(array!!.size)
        adapter.newArray(arrayType)
        for (i in array.indices) {
            adapter.dup()
            adapter.push(i)
            array[i].writeOut(bc, Expression.MODE_REF)
            adapter.visitInsn(Opcodes.AASTORE)
        }
    }

    /**
     * visit line number
     *
     * @param adapter
     * @param line
     * @param silent id silent this is ignored for log
     */
    fun visitLine(bc: BytecodeContext?, pos: Position?) {
        if (pos != null) {
            visitLine(bc, pos.line)
        }
    }

    private fun visitLine(bc: BytecodeContext?, line: Int) {
        if (line > 0) {
            synchronized(getToken(bc.getClassName())) {
                if (!("" + line).equals(last!![bc.getClassName().toString() + ":" + bc.getId()])) {
                    bc.visitLineNumber(line)
                    last.put(bc.getClassName().toString() + ":" + bc.getId(), "" + line)
                    last.put(bc.getClassName(), "" + line)
                }
            }
        }
    }

    fun lastLine(bc: BytecodeContext?) {
        synchronized(getToken(bc.getClassName())) {
            val line: Int = Caster.toIntValue(last!![bc.getClassName()], -1)
            visitLine(bc, line)
        }
    }

    private fun getToken(className: String?): Object? {
        val newLock = Object()
        var lock: Object = tokens.putIfAbsent(className, newLock)
        if (lock == null) {
            lock = newLock
        }
        return lock
    }

    /**
     * write out expression without LNT
     *
     * @param value
     * @param bc
     * @param mode
     * @throws TransformerException
     */
    @Throws(TransformerException::class)
    fun writeOutSilent(value: Expression?, bc: BytecodeContext?, mode: Int) {
        val start: Position = value.getStart()
        val end: Position = value.getEnd()
        value.setStart(null)
        value.setEnd(null)
        value.writeOut(bc, mode)
        value.setStart(start)
        value.setEnd(end)
    }

    @Throws(TransformerException::class)
    fun writeOut(value: Expression?, bc: BytecodeContext?, mode: Int) {
        value.writeOut(bc, mode)
    }

    @Throws(TransformerException::class)
    fun writeOut(s: Statement?, bc: BytecodeContext?) {
        if (doLog(bc)) {
            val id: String = CreateUniqueId.invoke()
            val tfv = TryFinallyVisitor(object : OnFinally() {
                @Override
                fun _writeOut(bc: BytecodeContext?) {
                    callEndLog(bc, s, id)
                }
            }, null)
            tfv.visitTryBegin(bc)
            callStartLog(bc, s, id)
            s.writeOut(bc)
            tfv.visitTryEnd(bc)
        } else s.writeOut(bc)
    }

    fun toShortType(expr: ExprString?, alsoAlias: Boolean, defaultValue: Short): Short {
        return if (expr is LitString) {
            CFTypes.toShort((expr as LitString?).getString(), alsoAlias, defaultValue)
        } else defaultValue
    }

    fun callStartLog(bc: BytecodeContext?, s: Statement?, id: String?) {
        call_Log(bc, START, s.getStart(), id)
    }

    fun callEndLog(bc: BytecodeContext?, s: Statement?, id: String?) {
        call_Log(bc, END, s.getEnd(), id)
    }

    private fun call_Log(bc: BytecodeContext?, method: Method?, pos: Position?, id: String?) {
        if (!bc.writeLog() || pos == null || StringUtil.indexOfIgnoreCase(bc.getMethod().getName(), "call") === -1) return
        try {
            val adapter: GeneratorAdapter = bc.getAdapter()
            adapter.loadArg(0)
            // adapter.checkCast(Types.PAGE_CONTEXT_IMPL);
            adapter.push(pos.pos)
            adapter.push(id)
            adapter.invokeVirtual(Types.PAGE_CONTEXT, method)
        } catch (t: Throwable) {
            ExceptionUtil.rethrowIfNecessary(t)
        }
    }

    fun doLog(bc: BytecodeContext?): Boolean {
        return bc.writeLog() && StringUtil.indexOfIgnoreCase(bc.getMethod().getName(), "call") !== -1
    }
}