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
package lucee.runtime.tag

import lucee.commons.io.SystemUtil

/**
 * Pauses the execution of the page for a given interval
 *
 *
 *
 */
class Sleep : TagImpl() {
    /** Expressed in milli seconds.  */
    private var time: Long = 0
    @Override
    fun release() {
        super.release()
        time = 0
    }

    /**
     * set the value interval Expressed in milli seconds.
     *
     * @param time value to set
     */
    fun setTime(time: Double) {
        this.time = time.toLong()
    }

    @Override
    @Throws(PageException::class)
    fun doStartTag(): Int {
        if (time >= 0) {
            SystemUtil.sleep(time)
        } else throw ExpressionException("attribute interval must be greater or equal to 0, now [$time]")
        return SKIP_BODY
    }

    @Override
    fun doEndTag(): Int {
        return EVAL_PAGE
    }
}