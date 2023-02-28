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
package lucee.runtime.video

import lucee.commons.io.res.Resource

interface VideoUtil {
    fun createVideoProfile(): VideoProfile?
    fun createVideoOutput(output: Resource?): VideoOutput?
    fun createVideoInput(input: Resource?): VideoInput?

    @Throws(PageException::class)
    fun toBytes(byt: String?): Long

    @Throws(PageException::class)
    fun toHerz(byt: String?): Long

    @Throws(PageException::class)
    fun toMillis(time: String?): Long

    @Throws(PageException::class)
    fun calculateDimension(pc: PageContext?, sources: Array<VideoInput?>?, width: Int, strWidth: String?, height: Int, strHeight: String?): IntArray?
}