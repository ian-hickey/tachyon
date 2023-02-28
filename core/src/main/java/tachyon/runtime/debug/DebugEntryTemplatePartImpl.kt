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

import tachyon.runtime.PageSource

class DebugEntryTemplatePartImpl : DebugEntrySupport, DebugEntryTemplatePart {
    private var startPos: Int
    private var startLine = 0
    private var endPos: Int
    private var endLine = 0
    private var snippet: String? = ""

    protected constructor(source: PageSource?, startPos: Int, endPos: Int) : super(source) {
        this.startPos = startPos
        this.endPos = endPos
    }

    constructor(source: PageSource?, startPos: Int, endPos: Int, startLine: Int, endLine: Int, snippet: String?) : super(source) {
        this.startPos = startPos
        this.endPos = endPos
        this.startLine = startLine
        this.endLine = endLine
        this.snippet = snippet
    }

    @Override
    fun getSrc(): String? {
        return getSrc(getPath(), startPos, endPos)
    }

    @Override
    fun getStartPosition(): Int {
        return startPos
    }

    @Override
    fun getEndPosition(): Int {
        return endPos
    }

    @Override
    fun getStartLine(): Int {
        return startLine
    }

    @Override
    fun getEndLine(): Int {
        return endLine
    }

    @Override
    fun getSnippet(): String? {
        return snippet
    }

    companion object {
        fun getSrc(path: String?, startPos: Int, endPos: Int): String? {
            return path.toString() + ":" + startPos + " - " + endPos
        }
    }
}