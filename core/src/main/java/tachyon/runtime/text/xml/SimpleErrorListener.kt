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
package tachyon.runtime.text.xml

import javax.xml.transform.ErrorListener

class SimpleErrorListener(private val ignoreFatal: Boolean, private val ignoreError: Boolean, private val ignoreWarning: Boolean) : ErrorListener {
    @Override
    @Throws(TransformerException::class)
    fun error(te: TransformerException?) {
        if (!ignoreError) throw te
    }

    @Override
    @Throws(TransformerException::class)
    fun fatalError(te: TransformerException?) {
        if (!ignoreFatal) throw te
    }

    @Override
    @Throws(TransformerException::class)
    fun warning(te: TransformerException?) {
        if (!ignoreWarning) throw te
    }

    companion object {
        val THROW_FATAL: ErrorListener? = SimpleErrorListener(false, true, true)
        val THROW_ERROR: ErrorListener? = SimpleErrorListener(false, false, true)
        val THROW_WARNING: ErrorListener? = SimpleErrorListener(false, false, false)
    }
}