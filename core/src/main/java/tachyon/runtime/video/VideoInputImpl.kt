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
package tachyon.runtime.video

import tachyon.commons.io.res.Resource

class VideoInputImpl(resource: Resource?) : VideoInput {
    private val resource: Resource?
    private var args: String? = ""
    private var path: String? = null

    /**
     * @see tachyon.runtime.video.VideoInput.getResource
     */
    @Override
    fun getResource(): Resource? {
        return resource
    }

    /**
     * @see tachyon.runtime.video.VideoInput.setCommand
     */
    @Override
    fun setCommand(path: String?, args: List<*>?) {
        this.path = path
        try {
            addArgs(ListUtil.listToList(args, " "))
        } catch (pe: PageException) {
            throw PageRuntimeException(pe)
        }
    }

    @Override
    fun setCommand(path: String?, args: Array<String?>?) {
        this.path = path
        addArgs(ListUtil.arrayToList(args, " "))
    }

    /**
     * @see tachyon.runtime.video.VideoInput.getCommandAsString
     */
    @Override
    fun getCommandAsString(): String? {
        return path.toString() + " " + args
    }

    private fun addArgs(args: String?) {
        if (StringUtil.isEmpty(this.args, true)) this.args = args else this.args += "; $args"
    }

    /**
     * Constructor of the class
     *
     * @param resource
     */
    init {
        this.resource = resource
    }
}