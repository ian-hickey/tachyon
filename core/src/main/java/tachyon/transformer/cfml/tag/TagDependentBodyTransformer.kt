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
package tachyon.transformer.cfml.tag

import tachyon.runtime.exp.TemplateException

/**
 * Interface zum implementieren von individullen Parsersn fuer einezelne Tags (cfscript)
 */
interface TagDependentBodyTransformer {
    /**
     * @param parent
     * @param flibs
     * @param cfxdTag
     * @param tagLibTag
     * @param cfml
     * @throws TemplateException
     */
    // public Body transform(Factory factory,Root root,EvaluatorPool ep,TagLib[][] tlibs, FunctionLib[]
    // flibs, String surroundingTagName,
    // TagLibTag[] scriptTags, SourceCode cfml,TransfomerSettings setting)
    // throws TemplateException;
    @Throws(TemplateException::class)
    fun transform(data: Data?, surroundingTagName: String?): Body?
}