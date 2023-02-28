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
package lucee.runtime.engine

import java.util.Map

abstract class ExecutionLogSupport : ExecutionLog {
    private val map: Map<String?, Pair?>? = ConcurrentHashMap<String?, Pair?>()
    protected var min = Long.MIN_VALUE
    protected var unit = UNIT_UNDEFINED
    @Override
    fun init(pc: PageContext?, arguments: Map<String?, String?>?) {
        // min
        if (min == Long.MIN_VALUE) {
            min = toNanos(arguments!!["min-time"], 0)
        }
        // unit
        if (UNIT_UNDEFINED == unit) {
            unit = UNIT_NANO
            // unit
            var _unit = arguments!!["unit"]
            if (_unit != null) {
                _unit = _unit.trim()
                if (_unit.equalsIgnoreCase("micro")) unit = UNIT_MICRO else if (_unit.equalsIgnoreCase(SystemUtil.SYMBOL_MICRO.toString() + "s")) unit = UNIT_MICRO else if (_unit.equalsIgnoreCase("milli")) unit = UNIT_MILLI else if (_unit.equalsIgnoreCase("ms")) unit = UNIT_MILLI
            }
        }
        _init(pc, arguments)
    }

    @Override
    fun release() {
        map.clear()
        _release()
    }

    @Override
    fun start(pos: Int, id: String?) {
        val current: Long = System.nanoTime()
        map.put(id, Pair(current, pos))
    }

    @Override
    fun end(pos: Int, id: String?) {
        val current: Long = System.nanoTime()
        val pair: Pair = map.remove(id)
        if (pair != null) {
            if (current - pair.time >= min) _log(pair.pos, pos, pair.time, current)
        }
    }

    protected abstract fun _init(pc: PageContext?, arguments: Map<String?, String?>?)
    protected abstract fun _log(startPos: Int, endPos: Int, startTime: Long, endTime: Long)
    protected abstract fun _release()
    protected fun timeLongToString(current: Long): String? {
        if (unit == UNIT_MICRO) return (current / 1000L).toString() + " " + SystemUtil.SYMBOL_MICRO + "s"
        return if (unit == UNIT_MILLI) (current / 1000000L).toString() + " ms" else "$current ns"
    }

    private class Pair(val time: Long, val pos: Int)
    companion object {
        protected const val UNIT_NANO: Short = 1
        protected const val UNIT_MICRO: Short = 2
        protected const val UNIT_MILLI: Short = 4
        protected const val UNIT_UNDEFINED: Short = 0
        private fun toNanos(str: String?, defaultValue: Int): Long {
            var str = str
            if (StringUtil.isEmpty(str)) return defaultValue.toLong()
            str = str.trim().toLowerCase()
            var l: Long = Caster.toLongValue(str, Long.MIN_VALUE)
            if (l != Long.MIN_VALUE) return l
            if (str.endsWith("ns")) {
                val sub: String = str.substring(0, str.length() - 2)
                l = Caster.toLongValue(sub.trim(), Long.MIN_VALUE)
                if (l != Long.MIN_VALUE) return l
            } else if (str.endsWith(SystemUtil.SYMBOL_MICRO.toString() + "s")) {
                val sub: String = str.substring(0, str.length() - 2)
                val d: Double = Caster.toDoubleValue(sub.trim(), Double.NaN)
                if (!Double.isNaN(d)) return (d * 1000).toLong()
            } else if (str.endsWith("ms")) {
                val sub: String = str.substring(0, str.length() - 2)
                val d: Double = Caster.toDoubleValue(sub.trim(), Double.NaN)
                if (!Double.isNaN(d)) return (d * 1000 * 1000).toLong()
            } else if (str.endsWith("s")) {
                val sub: String = str.substring(0, str.length() - 1)
                val d: Double = Caster.toDoubleValue(sub.trim(), Double.NaN)
                if (!Double.isNaN(d)) return (d * 1000 * 1000).toLong()
            }
            return defaultValue.toLong()
        }

        protected fun unitShortToString(unit: Short): String? {
            if (unit == UNIT_MICRO) return SystemUtil.SYMBOL_MICRO.toString() + "s"
            return if (unit == UNIT_MILLI) "ms" else "ns"
        }
    }
}