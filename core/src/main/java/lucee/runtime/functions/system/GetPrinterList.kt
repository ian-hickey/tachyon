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
package lucee.runtime.functions.system

import javax.print.PrintService

class GetPrinterList : BIF() {
    @Override
    @Throws(PageException::class)
    operator fun invoke(pc: PageContext?, args: Array<Object?>?): Object? {
        return call(pc, ",")
    }

    companion object {
        private const val serialVersionUID = -3863471828670823815L
        fun call(pc: PageContext?, delimiter: String?): String? {
            var delimiter = delimiter
            if (delimiter == null) delimiter = ","
            val sb = StringBuilder()
            val services: Array<PrintService?> = PrintServiceLookup.lookupPrintServices(null, null)
            for (i in services.indices) {
                if (i > 0) sb.append(delimiter)
                sb.append(services[i].getName())
            }
            return sb.toString()
        }
    }
}