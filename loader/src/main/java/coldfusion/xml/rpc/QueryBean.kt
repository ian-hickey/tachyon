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
package coldfusion.xml.rpc

import java.io.Serializable

/**
 * Extends the Query with a Bean initializer for WebService deserializer
 */
class QueryBean : Serializable {
    private var columnList: Array<String>
    private var data: Array<Array<Object>>

    /**
     * @return Returns the columnList.
     */
    fun getColumnList(): Array<String> {
        return columnList
    }

    /**
     * @param columnList The columnList to set.
     */
    fun setColumnList(columnList: Array<String>) {
        this.columnList = columnList
    }

    /**
     * @return Returns the data.
     */
    fun getData(): Array<Array<Object>> {
        return data
    }

    /**
     * @param data The data to set.
     */
    fun setData(data: Array<Array<Object>>) {
        this.data = data
    }

    companion object {
        private const val serialVersionUID = 7970107175356034966L
    }
}