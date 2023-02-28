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
package lucee.runtime.spooler.test

import lucee.runtime.config.Config

class TestTask(plans: Array<ExecutionPlan?>?, private val label: String?, private var fail: Int) : SpoolerTaskSupport(plans) {
    @Override
    fun getType(): String? {
        return "test"
    }

    @Override
    fun detail(): Struct? {
        return StructImpl()
    }

    @Override
    @Throws(PageException::class)
    fun execute(config: Config?): Object? {
        // print.out("execute:"+label+":"+fail+":"+new Date());
        if (fail-- > 0) throw ExpressionException("no idea")
        return null
    }

    @Override
    fun subject(): String? {
        return label
    }
}