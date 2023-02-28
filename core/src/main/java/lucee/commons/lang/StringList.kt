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
package lucee.commons.lang

import java.util.ArrayList

/**
 * a Simple single direction string list
 */
class StringList {
    private val root: Entry = Entry(null, Entry.Companion.NUL)
    private var curr: Entry?
    private var count = 0

    /**
     * constructor of the class
     */
    constructor() {
        curr = root
    }

    /**
     * constructor of the class
     *
     * @param str String Element
     */
    constructor(str: String?) {
        root.next = Entry(str, Entry.Companion.NUL)
        curr = root.next
        count = 1
    }

    /**
     * constructor of the class, initalize with 2 values
     *
     * @param str1
     * @param str2
     */
    constructor(str1: String?, str2: String?) : this(str1) {
        add(str2)
    }

    /**
     * @return returns if List has a next Element
     */
    operator fun hasNext(): Boolean {
        return curr!!.next != null
    }

    /**
     * @return returns if List has a next Element
     */
    fun hasNextNext(): Boolean {
        return curr!!.next != null && curr!!.next!!.next != null
    }

    /**
     * @return returns next element in the list
     */
    operator fun next(): String {
        curr = curr!!.next
        return curr!!.data
    }

    fun delimiter(): Char {
        return curr!!.delimiter
    }

    /**
     * @return returns current element in the list
     */
    fun current(): String {
        return curr!!.data
    }

    /**
     * reset the String List
     *
     * @return
     */
    fun reset(): StringList {
        curr = root
        return this
    }

    /**
     * @return returns the size of the list
     */
    fun size(): Int {
        return count
    }

    /**
     * adds an element to the list
     *
     * @param str String Element to add
     */
    fun add(str: String?) {
        curr!!.next = Entry(str, Entry.Companion.NUL)
        curr = curr!!.next
        count++
    }

    fun add(str: String?, delimiter: Char) {
        curr!!.next = Entry(str, delimiter)
        curr = curr!!.next
        count++
    }

    private inner class Entry private constructor(val data: String, val delimiter: Char) {
        val next: Entry? = null

        companion object {
            const val NUL = 0.toChar()
        }
    }

    fun toArray(): Array<String> {
        val list: ArrayList<String> = ArrayList<String>()
        while (hasNext()) {
            list.add(next())
        }
        return list.toArray(arrayOfNulls<String>(list.size()))
    }
}