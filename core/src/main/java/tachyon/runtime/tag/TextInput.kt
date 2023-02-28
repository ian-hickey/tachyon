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
package tachyon.runtime.tag

import tachyon.runtime.exp.DeprecatedException

class TextInput : TagImpl() {
    fun setAlign(align: String?) {}
    fun setBgcolor(bgColor: String?) {}
    fun setBold(bold: Boolean) {}
    fun setFont(font: String?) {}
    fun setFontsize(fontSize: Double) {}
    fun setHeight(height: Double) {}
    fun setHspace(space: Double) {}
    fun setItalic(italic: Boolean) {}
    fun setMaxlength(maxLength: Double) {}
    fun setMessage(message: String?) {}
    fun setName(name: String?) {}
    fun setNotsupported(notSupported: String?) {}
    fun setOnerror(onError: String?) {}
    fun setOnvalidate(onValidate: String?) {}
    fun setRange(range: String?) {}
    fun setRequired(required: Boolean) {}
    fun setSize(size: Double) {}
    fun setTextcolor(textColor: String?) {}
    fun setValidate(validate: Boolean) {}
    fun setValue(value: String?) {}
    fun setVspace(space: Double) {}
    fun setWidth(width: Double) {}

    init {
        throw DeprecatedException("textinput", null)
    }
}