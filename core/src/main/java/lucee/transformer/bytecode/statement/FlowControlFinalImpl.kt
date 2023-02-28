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
package lucee.transformer.bytecode.statement

import org.objectweb.asm.Label

class FlowControlFinalImpl : FlowControlFinal {
    private val entryLabel: Label?
    private var gotoLabel: Label? = null

    @Override
    override fun setAfterFinalGOTOLabel(gotoLabel: Label?) {
        this.gotoLabel = gotoLabel
    }

    @Override
    override fun getAfterFinalGOTOLabel(): Label? {
        return gotoLabel
    }

    @Override
    override fun getFinalEntryLabel(): Label? {
        return entryLabel
    }

    init {
        entryLabel = Label()
    }
}