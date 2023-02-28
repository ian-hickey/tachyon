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
package tachyon.runtime.thread

import tachyon.runtime.config.Config

class ChildSpoolerTask(ct: ChildThreadImpl?, plans: Array<ExecutionPlan?>?) : SpoolerTaskSupport(plans) {
    private val ct: ChildThreadImpl?
    @Override
    fun detail(): Struct? {
        val detail = StructImpl()
        detail.setEL("template", ct!!.getTemplate())
        return detail
    }

    @Override
    @Throws(PageException::class)
    fun execute(config: Config?): Object? {
        val pe: PageException = ct!!.execute(config)
        if (pe != null) throw pe
        return null
    }

    @Override
    fun getType(): String? {
        return "cfthread"
    }

    @Override
    fun subject(): String? {
        return ct!!.getTagName()
    }

    init {
        this.ct = ct
    }
}