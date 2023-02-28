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

import java.io.IOException

class VideoExecuterNotSupported : VideoExecuter {
    /**
     * @see lucee.runtime.video.VideoExecuter.convertRaw
     */
    @Override
    @Throws(IOException::class)
    fun convert(config: ConfigWeb?, inputs: Array<VideoInput?>?, output: VideoOutput?, quality: VideoProfile?): Array<VideoInfo?>? {
        throw notSupported()
    }

    /**
     * @see lucee.runtime.video.VideoExecuter.infoRaw
     */
    @Override
    @Throws(IOException::class)
    fun info(config: ConfigWeb?, input: VideoInput?): VideoInfo? {
        throw notSupported()
    }

    /**
     * @see lucee.runtime.video.VideoExecuter.test
     */
    @Override
    @Throws(IOException::class)
    fun test(config: ConfigWeb?) {
        throw notSupported()
    }

    /**
     * @see lucee.runtime.video.VideoExecuter.uninstall
     */
    @Override
    @Throws(IOException::class)
    fun uninstall(config: Config?) {
        throw notSupported()
    }

    /**
     * @see lucee.runtime.video.VideoExecuter.install
     */
    @Override
    @Throws(IOException::class)
    fun install(config: ConfigWeb?, data: Struct?) {
        throw notSupported()
    }

    private fun notSupported(): VideoException? {
        return VideoException("The video components are not installed, please go to the Lucee Server Administrator in order to install the video extension")
    }
}