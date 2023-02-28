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
package lucee.runtime.sql.old

import java.io.Serializable

class ZAliasedName : Serializable {
    constructor() {
        strform_ = ""
        schema = null
        table = null
        column = null
        alias_ = null
        form_ = FORM_COLUMN
    }

    constructor(s: String?, i: Int) {
        strform_ = ""
        schema = null
        table = null
        column = null
        alias_ = null
        form_ = FORM_COLUMN
        form_ = i
        strform_ = String(s)
        val stringtokenizer = StringTokenizer(s, ".")
        when (stringtokenizer.countTokens()) {
            1 -> if (i == FORM_TABLE) table = String(stringtokenizer.nextToken()) else column = String(stringtokenizer.nextToken())
            2 -> if (i == FORM_TABLE) {
                schema = String(stringtokenizer.nextToken())
                table = String(stringtokenizer.nextToken())
            } else {
                table = String(stringtokenizer.nextToken())
                column = String(stringtokenizer.nextToken())
            }
            3 -> {
                schema = String(stringtokenizer.nextToken())
                table = String(stringtokenizer.nextToken())
                column = String(stringtokenizer.nextToken())
            }
            else -> {
                schema = String(stringtokenizer.nextToken())
                table = String(stringtokenizer.nextToken())
                column = String(stringtokenizer.nextToken())
            }
        }
    }

    @Override
    override fun toString(): String {
        return if (alias_ == null) strform_!! else strform_.toString() + " " + alias_
    }

    val isWildcard: Boolean
        get() = if (form_ == FORM_TABLE) table != null && table!!.equals("*") else column != null && column.indexOf('*') >= 0
    var alias: String?
        get() = alias_
        set(s) {
            alias_ = String(s)
        }
    var strform_: String?
    var schema: String?
    var table: String?
    var column: String?
    var alias_: String?
    var form_: Int

    companion object {
        var FORM_TABLE = 1
        var FORM_COLUMN = 2
    }
}