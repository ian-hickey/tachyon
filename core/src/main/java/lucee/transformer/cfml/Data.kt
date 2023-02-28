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
package lucee.transformer.cfml

import lucee.runtime.config.Config

class Data(factory: Factory?, page: Page?, srcCode: SourceCode?, ep: EvaluatorPool?, settings: TransfomerSettings?, tlibs: Array<Array<TagLib?>?>?, flibs: Array<FunctionLib?>?, scriptTags: Array<TagLibTag?>?,
           allowLowerThan: Boolean) {
    val srcCode: SourceCode?
    val settings: TransfomerSettings?
    val tlibs: Array<Array<TagLib?>?>?
    val flibs: Array<FunctionLib?>?
    val page: Page?
    val scriptTags: Array<TagLibTag?>?
    val ep: EvaluatorPool?
    val factory: Factory?
    val config: Config?
    var allowLowerThan: Boolean
    var parseExpression = false
    private var set: SimpleExprTransformer? = null
    var mode: Short = 0
    var insideFunction = false
    var tagName: String? = null
    var isCFC = false
    var isInterface = false
    var context: Short = TagLibTagScript.CTX_NONE
    var docComment: DocComment? = null
    private var parent: Body? = null
    var transformer: ExprTransformer? = null
    fun getSimpleExprTransformer(): SimpleExprTransformer? {
        return set
    }

    fun setSimpleExprTransformer(set: SimpleExprTransformer?) {
        this.set = set
    }

    fun setParent(parent: Body?): Body? {
        val tmp: Body? = this.parent
        this.parent = parent
        return tmp
    }

    fun getParent(): Body? {
        return parent
    }

    init {
        this.page = page
        this.srcCode = srcCode
        this.settings = settings
        this.tlibs = tlibs
        this.flibs = flibs
        this.scriptTags = scriptTags
        this.ep = ep
        this.factory = factory
        config = factory.getConfig()
        this.allowLowerThan = allowLowerThan
    }
}