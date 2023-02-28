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
package lucee.runtime.debug

import lucee.commons.io.res.Resource

/**
 *
 *
 * To change the template for this generated type comment go to Window - Preferences - Java - Code
 * Generation - Code and Comments
 */
class DebugPageImpl(file: Resource?) : Dumpable, DebugPage {
    private var count = 0
    private val file: Resource?
    private var min = 0
    private var max = 0
    private var all = 0
    private var time: Long = 0
    @Override
    fun set(t: Long) {
        time = t
        if (count == 0) {
            min = time.toInt()
            max = time.toInt()
        } else {
            if (min > time) min = time.toInt()
            if (max < time) max = time.toInt()
        }
        all += time.toInt()
        count++
    }

    @Override
    fun getMinimalExecutionTime(): Int {
        return min
    }

    @Override
    fun getMaximalExecutionTime(): Int {
        return max
    }

    @Override
    fun getAverageExecutionTime(): Int {
        return all / count
    }

    @Override
    fun getCount(): Int {
        return count
    }

    @Override
    fun getFile(): Resource? {
        return file
    }

    @Override
    fun toDumpData(pageContext: PageContext?, maxlevel: Int, dp: DumpProperties?): DumpData? {
        val table = DumpTable("#cccc66", "#cccc99", "#000000")
        table.setTitle(file.getAbsolutePath())
        table.appendRow(1, SimpleDumpData("min (ms)"), SimpleDumpData(min))
        table.appendRow(1, SimpleDumpData("avg (ms)"), SimpleDumpData(getAverageExecutionTime()))
        table.appendRow(1, SimpleDumpData("max (ms)"), SimpleDumpData(max))
        table.appendRow(1, SimpleDumpData("total (ms)"), SimpleDumpData(all))
        return table
    }
    // private long time;
    /**
     * @param file
     */
    init {
        this.file = file
    }
}