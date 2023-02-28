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
package tachyon.runtime.debug

import tachyon.commons.io.SystemUtil.TemplateLine

class DebugTextFragmentImpl : DebugTextFragment {
    private val text: String?
    private val template: String?
    private val line: Int

    constructor(text: String?, template: String?, line: Int) {
        this.text = text
        this.template = template
        this.line = line
    }

    constructor(text: String?, tl: TemplateLine?) {
        this.text = text
        template = tl!!.template
        line = tl!!.line
    }

    @Override
    fun getText(): String? {
        return text
    }

    @Override
    fun getTemplate(): String? {
        return template
    }

    @Override
    fun getLine(): Int {
        return line
    }
}