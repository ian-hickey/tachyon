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
package tachyon.runtime.sql.exp.value

import tachyon.commons.lang.StringUtil

class ValueString : ValueSupport, Literal {
    constructor(value: String?, alias: String?) : super(value, alias) {}
    constructor(value: String?) : super(value) {}

    @Override
    override fun toString(noAlias: Boolean): String? {
        return if (noAlias || getIndex() === 0) "'" + StringUtil.replace(getString(), "'", "''", false).toString() + "'" else toString(true).toString() + " as " + getAlias()
    }

    @get:Override
    override val value: Object?
        get() = getString()
}