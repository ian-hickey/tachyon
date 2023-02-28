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
package lucee.runtime.util

import lucee.runtime.PageContext

/**
 * A Number Iterator Implementation to iterate from to
 */
class NumberIterator private constructor(from: Int, to: Int, recordcount: Int) {
    private var _from = 0
    private var _to = 0
    private var _current = 0
    private var recordcount = 0
    private fun init(from: Int, to: Int, recordcount: Int): NumberIterator? {
        _from = from
        _current = from
        _to = to
        this.recordcount = recordcount
        return this
    }

    /**
     * @return returns if there is a next value
     */
    operator fun hasNext(): Boolean {
        return _current < _to
    }

    fun hasNext(useRecordcount: Boolean): Boolean {
        return _current < if (useRecordcount) recordcount else _to
    }

    /**
     * @return increment and return new value
     */
    operator fun next(): Int {
        return ++_current
    }

    /**
     * @return returns if there is a previous value
     */
    fun hasPrevious(): Boolean {
        return _current > _from
    }

    /**
     * @return decrement and return new value
     */
    fun previous(): Int {
        return --_current
    }

    /**
     * @return returns smallest possible value
     */
    fun from(): Int {
        return _from
    }

    /**
     * @return returns greatest value
     */
    fun to(): Int {
        return _to
    }

    /**
     * @return set value to first and return
     */
    fun first(): Int {
        return _from.also { _current = it }
    }

    /**
     * @return set value to last and return thid value
     */
    fun last(): Int {
        return _to.also { _current = it }
    }

    /**
     * @return returns current value
     */
    fun current(): Int {
        return _current
    }

    /**
     * sets the current position
     *
     * @param current
     */
    fun setCurrent(current: Int) {
        _current = current
    }

    /**
     * @return is after last
     */
    fun isAfterLast(): Boolean {
        return _current > _to
    }

    /**
     * @return is pointer on a valid position
     */
    fun isValid(): Boolean {
        return _current >= _from && _current <= _to
    }

    fun isValid(current: Int): Boolean {
        _current = current
        return _current >= _from && _current <= _to
    }

    companion object {
        private val iterators: Array<NumberIterator?>? = arrayOf(NumberIterator(1, 1, 1), NumberIterator(1, 1, 1), NumberIterator(1, 1, 1),
                NumberIterator(1, 1, 1), NumberIterator(1, 1, 1), NumberIterator(1, 1, 1), NumberIterator(1, 1, 1), NumberIterator(1, 1, 1),
                NumberIterator(1, 1, 1), NumberIterator(1, 1, 1))
        private var pointer = 0

        /**
         * load an iterator
         *
         * @param from
         * @param to iterate to
         * @return NumberIterator
         */
        private fun _load(from: Int, to: Int, recordcount: Int = to): NumberIterator? {
            return if (pointer >= iterators!!.size) NumberIterator(from, to, recordcount) else iterators[pointer++]!!.init(from, to, recordcount)
        }

        /**
         * create a Number Iterator with value from and to
         *
         * @param from
         * @param to
         * @return NumberIterator
         */
        @Synchronized
        fun load(from: Double, to: Double): NumberIterator? {
            return _load(from.toInt(), to.toInt(), to.toInt())
        }

        @Synchronized
        fun load(from: Int, to: Int): NumberIterator? {
            return _load(from, to, to)
        }

        /**
         * create a Number Iterator with value from and to
         *
         * @param from
         * @param to
         * @param max
         * @return NumberIterator
         */
        @Synchronized
        fun load(from: Double, to: Double, max: Double): NumberIterator? {
            return loadMax(from.toInt(), to.toInt(), max.toInt())
        }

        @Synchronized
        fun loadMax(from: Int, to: Int, max: Int): NumberIterator? {
            return _load(from, if (from + max - 1 < to) from + max - 1 else to, to)
        }

        @Synchronized
        fun loadEnd(from: Int, to: Int, end: Int): NumberIterator? {
            return _load(from, if (end < to) end else to, to)
        }

        /**
         * @param ni
         * @param query
         * @param groupName
         * @param caseSensitive
         * @return number iterator for group
         * @throws PageException
         */
        @Synchronized
        @Throws(PageException::class)
        fun load(pc: PageContext?, ni: NumberIterator?, query: Query?, groupName: String?, caseSensitive: Boolean): NumberIterator? {
            val startIndex: Int = query.getCurrentrow(pc.getId())
            val startValue: Object = query.get(KeyImpl.init(groupName))
            while (ni!!.hasNext(true)) {
                if (!OpUtil.equals(pc, startValue, query.getAt(groupName, ni.next()), caseSensitive)) {
                    ni.previous()
                    return _load(startIndex, ni.current())
                }
            }
            return _load(startIndex, ni.current())
        }

        /**
         * @param ni Iterator to release
         */
        @Synchronized
        fun release(ni: NumberIterator?) {
            if (pointer > 0) {
                iterators!![--pointer] = ni
            }
        }
    }
    // private static int count=0;
    /**
     * constructor of the number iterator
     *
     * @param from iterate from
     * @param to iterate to
     * @param recordcount
     */
    init {
        // lucee.print.ln(count++);
        init(from, to, recordcount)
    }
}