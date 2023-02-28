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
package lucee.runtime.dump

import kotlin.Throws
import lucee.loader.util.Util
import kotlin.jvm.Synchronized
import kotlin.jvm.Transient
import org.osgi.framework.Version

class DumpRow(
        /**
         * @return the highlightType
         */
        val highlightType: Int, items: Array<DumpData>) {
    private val items: Array<DumpData>

    /**
     * Constructor of the class
     *
     * @param highlightType binary Values define which columns are highlighted
     * @param item1 item for the array
     */
    constructor(highlightType: Int, item1: DumpData) : this(highlightType, arrayOf<DumpData>(item1)) {}

    /**
     * Constructor of the class
     *
     * @param highlightType binary Values define which columns are highlighted
     * @param item1 item for the array
     * @param item2 item for the array
     */
    constructor(highlightType: Int, item1: DumpData, item2: DumpData) : this(highlightType, arrayOf<DumpData>(item1, item2)) {}

    /**
     * Constructor of the class
     *
     * @param highlightType binary Values define which columns are highlighted
     * @param item1 item for the array
     * @param item2 item for the array
     * @param item3 item for the array
     */
    constructor(highlightType: Int, item1: DumpData, item2: DumpData, item3: DumpData) : this(highlightType, arrayOf<DumpData>(item1, item2, item3)) {}

    /**
     * Constructor of the class
     *
     * @param highlightType binary Values define which columns are highlighted
     * @param item1 item for the array
     * @param item2 item for the array
     * @param item3 item for the array
     * @param item4 item for the array
     */
    constructor(highlightType: Int, item1: DumpData, item2: DumpData, item3: DumpData, item4: DumpData) : this(highlightType, arrayOf<DumpData>(item1, item2, item3, item4)) {}

    /**
     * Constructor of the class
     *
     * @param highlightType binary Values define which columns are highlighted
     * @param item1 item for the array
     * @param item2 item for the array
     * @param item3 item for the array
     * @param item4 item for the array
     * @param item5 item for the array
     */
    constructor(highlightType: Int, item1: DumpData, item2: DumpData, item3: DumpData, item4: DumpData, item5: DumpData) : this(highlightType, arrayOf<DumpData>(item1, item2, item3, item4, item5)) {}

    /**
     * Constructor of the class
     *
     * @param highlightType binary Values define which columns are highlighted
     * @param item1 item for the array
     * @param item2 item for the array
     * @param item3 item for the array
     * @param item4 item for the array
     * @param item5 item for the array
     * @param item6 item for the array
     */
    constructor(highlightType: Int, item1: DumpData, item2: DumpData, item3: DumpData, item4: DumpData, item5: DumpData, item6: DumpData) : this(highlightType, arrayOf<DumpData>(item1, item2, item3, item4, item5, item6)) {}

    /**
     * @return the items
     */
    fun getItems(): Array<DumpData> {
        return items
    }

    /**
     * constructor of the class
     *
     * @param highlightType binary Values define which columns are highlighted
     * @param items items as DumpData Array
     */
    init {
        this.items = items
    }
}