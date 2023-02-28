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
package tachyon.runtime.type

import tachyon.runtime.exp.PageException

/**
 * Interface for a simple Iterator
 */
interface Iterator {
    /**
     * set the intern pointer of the iterator to the next position, return true if next position exist
     * otherwise false.
     *
     * @return boolean
     * @throws PageException thrown when fail to execute action
     */
    @Deprecated
    @Deprecated("use instead <code>{@link #next(int)}</code>")
    @Throws(PageException::class)
    operator fun next(): Boolean

    /**
     * set the intern pointer of the iterator to the next position, return true if next position exist
     * otherwise false.
     *
     * @param pid pointer id
     * @return boolean
     * @throws PageException thrown when fail to execute action
     */
    @Throws(PageException::class)
    fun next(pid: Int): Boolean
    fun previous(pid: Int): Boolean

    /**
     * reset the intern pointer
     *
     * @throws PageException thrown when fail to reset
     */
    @Deprecated
    @Deprecated("use instead <code>{@link #reset(int)}</code>")
    @Throws(PageException::class)
    fun reset()

    /**
     *
     * reset the intern pointer
     *
     * @param pid pointer id
     * @throws PageException thrown when fail to reset
     */
    @Throws(PageException::class)
    fun reset(pid: Int)

    /**
     * return recordcount of the iterator object
     *
     * @return int
     */
    val recordcount: Int

    /**
     * return the current position of the internal pointer
     *
     * @param pid pointer id
     * @return int
     */
    fun getCurrentrow(pid: Int): Int

    /**
     *
     * set the internal pointer to defined position
     *
     * @param index index
     * @param pid pointer id
     * @return if it was successful or not
     * @throws PageException thrown when fail to execute action
     */
    @Throws(PageException::class)
    fun go(index: Int, pid: Int): Boolean

    /**
     * @return returns if iterator is empty or not
     */ // public ArrayList column(String strColumn)throws PageException;
    val isEmpty: Boolean
    // public String[] row(int number);
}