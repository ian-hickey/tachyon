/**
 *
 * Copyright (c) 2014, the Railo Company Ltd. All rights reserved.
 * Copyright (c) 2015, Tachyon Assosication Switzerland
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
package tachyon.transformer.cfml.evaluator

import tachyon.runtime.config.Config

/**
 * evaluator interface for tags, this allows tags to check their environment and themself.
 */
interface TagEvaluator {
    /**
     * this method is executed to check the tag itself, the method is invoked after Tachyon has read that
     * tag, but before reading following tags. so you have not the complete environment of the tag.
     *
     * @param config
     * @param tag the tag to check
     * @param libTag the definition of the tag from the tld file
     * @param flibs all fld libraries.
     * @param data data object of the running parser
     * @throws TemplateException
     */
    @Throws(TemplateException::class)
    fun execute(config: Config?, tag: Tag?, libTag: TagLibTag?, flibs: Array<FunctionLib?>?, data: Data?): TagLib?

    /**
     * This method is invoked to check the environment of a tag, the method is invoked AFTER the parser
     * has read the complete template, so you have the full environment.
     *
     * @param tag the tag to check
     * @param libTag the definition of the tag from the tld file
     * @param flibs all fld libraries.
     * @throws EvaluatorException
     */
    @Throws(EvaluatorException::class)
    fun evaluate(tag: Tag?, libTag: TagLibTag?, flibs: Array<FunctionLib?>?)
}