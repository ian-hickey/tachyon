/**
 * Copyright (c) 2015, Tachyon Assosication Switzerland. All rights reserved.
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
package tachyon.transformer.expression.literal

import tachyon.transformer.expression.ExprBoolean

/**
 * Literal Boolean
 */
interface LitBoolean : Literal, ExprBoolean {
    // public static final LitBoolean TRUE=new LitBoolean(true,null,null);
    // public static final LitBoolean FALSE=new LitBoolean(false,null,null);
    /**
     * @return return value as a boolean value
     */
    fun getBooleanValue(): Boolean
}