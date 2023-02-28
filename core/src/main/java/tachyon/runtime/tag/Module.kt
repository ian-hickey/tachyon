/**
 * Copyright (c) 2014, the Railo Company Ltd.
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
package tachyon.runtime.tag

import tachyon.commons.lang.StringUtil

/**
 * Invokes a custom tag for use in CFML application pages.
 */
class Module : CFTag() {
    @Override
    @Throws(MissingIncludeException::class, ExpressionException::class)
    override fun initFile() {
        val config: ConfigWeb = pageContext.getConfig()
        // MUSTMUST cache like ct
        // String[] filenames=getFileNames(config,getAppendix());// =
        // appendix+'.'+config.getCFMLExtension();
        val objTemplate: Object = attributesScope.get(KeyConstants._template, null)
        val objName: Object = attributesScope.get(KeyConstants._name, null)
        source = null
        if (objTemplate != null) {
            attributesScope.removeEL(KeyConstants._template)
            val template: String = objTemplate.toString()
            if (StringUtil.startsWith(template, '/')) {
                val sources: Array<PageSource?> = (pageContext as PageContextImpl?).getPageSources(template)
                val ps: PageSource = MappingImpl.isOK(sources)
                        ?: throw MissingIncludeException(sources[0], "could not find template [" + template + "], file [" + sources[0].getDisplayPath() + "] doesn't exist")
                source = InitFile(pageContext, ps, template)
            } else {
                source = InitFile(pageContext, pageContext.getCurrentPageSource().getRealPage(template), template)
                if (!MappingImpl.isOK(source.getPageSource())) {
                    throw MissingIncludeException(source.getPageSource(),
                            "could not find template [" + template + "], file [" + source.getPageSource().getDisplayPath() + "] doesn't exist")
                }
            }

            // attributesScope.removeEL(TEMPLATE);
            appendix = source.getPageSource()
        } else if (objName != null) {
            attributesScope.removeEL(KeyConstants._name)
            val filenames = toRealPath(config, objName.toString())
            var exist = false

            // appcontext mappings
            var ctms: Array<Mapping?> = pageContext.getApplicationContext().getCustomTagMappings()
            if (ctms != null) {
                outer@ for (f in filenames.indices) {
                    for (i in ctms.indices) {
                        source = InitFile(pageContext, ctms[i].getPageSource(filenames!![f]), filenames[f])
                        if (MappingImpl.isOK(source.getPageSource())) {
                            exist = true
                            break@outer
                        }
                    }
                }
            }

            // config mappings
            if (!exist) {
                ctms = config.getCustomTagMappings()
                outer@ for (f in filenames.indices) {
                    for (i in ctms.indices) { // TODO optimieren siehe CFTag
                        source = InitFile(pageContext, ctms[i].getPageSource(filenames!![f]), filenames[f])
                        if (MappingImpl.isOK(source.getPageSource())) {
                            exist = true
                            break@outer
                        }
                    }
                }
            }
            if (!exist) throw ExpressionException("custom tag (" + CustomTagUtil.getDisplayName(config, objName.toString()).toString() + ") is not defined in custom tag directory [" + (if (ctms.size == 0) "no custom tag directory defined" else CustomTagUtil.toString(ctms)).toString() + "]")
            appendix = source.getPageSource()
        } else {
            throw ExpressionException("you must define attribute template or name for tag module")
        }
    }

    override var appendix: String?
        get() = super.appendix
        private set(source) {
            var appendix: String? = source.getFileName()
            val index: Int = appendix.lastIndexOf('.')
            appendix = appendix.substring(0, index)
            setAppendix(appendix)
        }

    companion object {
        /**
         * translate a dot-notation path to a realpath
         *
         * @param dotPath
         * @return realpath
         * @throws ExpressionException
         */
        @Throws(ExpressionException::class)
        private fun toRealPath(config: Config?, dotPath: String?): Array<String?>? {
            var dotPath = dotPath
            dotPath = dotPath.trim()
            while (dotPath.indexOf('.') === 0) {
                dotPath = dotPath.substring(1)
            }
            var len = -1
            while (dotPath!!.length().also { len = it } > 0 && dotPath.lastIndexOf('.') === len - 1) {
                dotPath = dotPath.substring(0, len - 2)
            }
            return CustomTagUtil.getFileNames(config, dotPath.replace('.', '/'))
        }
    }
}