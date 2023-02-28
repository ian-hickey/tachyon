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
package lucee.runtime.functions.video

import lucee.commons.lang.StringUtil

object IsVideoFile {
    @Throws(PageException::class)
    fun call(pc: PageContext?, path: String?): Boolean {
        try {
            val config: ConfigWeb = pc.getConfig()
            val ve: VideoExecuter = VideoUtilImpl.createVideoExecuter(config)
            ve.info(config, VideoInputImpl(Caster.toResource(pc, path, true)))
        } catch (e: Exception) {
            if (StringUtil.contains(e.getMessage(), "missing ffmpeg installation")) throw Caster.toPageException(e)
            return false
        }
        return true
    }
}