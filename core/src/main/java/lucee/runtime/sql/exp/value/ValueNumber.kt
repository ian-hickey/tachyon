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
package lucee.runtime.sql.exp.value

import lucee.runtime.op.Caster

class ValueNumber : ValueSupport, Literal {
    var valueAsDouble: Double
        private set

    constructor(value: Double) : super(Caster.toString(value)) {
        valueAsDouble = value
    }

    constructor(value: Double, strValue: String?) : super(strValue) {
        valueAsDouble = value
    }

    constructor(strValue: String?) : super(strValue) {
        valueAsDouble = Caster.toDoubleValue(strValue, 0)
    }

    @Override
    override fun toString(noAlias: Boolean): String? {
        return if (noAlias || getIndex() === 0) getString() else getString().toString() + " as " + getAlias()
    }

    @Override
    fun getValue(): Object? {
        return Caster.toDouble(valueAsDouble)
    }
}