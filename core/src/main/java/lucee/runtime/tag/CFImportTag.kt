/**
 * Copyright (c) 2014, the Railo Company Ltd.
 * Copyright (c) 2015, Lucee Assosication Switzerland
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
package lucee.runtime.tag

import java.io.File

/**
 * To create cfimport custom tags
 */
class CFImportTag : CFTag() {
    @Override
    @Throws(PageException::class)
    override fun initFile() {
        val config: ConfigWeb = pageContext.getConfig()
        val filenames: Array<String?> = CustomTagUtil.getFileNames(config, getAppendix()) // = appendix+'.'+config.getCFMLExtension();
        val strRealPathes: String = attributesScope.remove("__custom_tag_path").toString()
        val realPathes: Array<String?> = ListUtil.listToStringArray(strRealPathes, File.pathSeparatorChar)
        for (i in realPathes.indices) {
            if (!StringUtil.endsWith(realPathes[i], '/')) realPathes[i] = realPathes[i].toString() + "/"
        }

        // MUSTMUST use cache like regular ct
        // page source
        var ps: PageSource
        for (rp in realPathes.indices) {
            for (fn in filenames.indices) {
                ps = (pageContext as PageContextImpl?).getRelativePageSourceExisting(realPathes[rp] + filenames[fn])
                if (ps != null) {
                    source = InitFile(pageContext, ps, filenames[fn])
                    return
                }
            }
        }

        // EXCEPTION
        // message
        val msg = StringBuffer("could not find template [")
        msg.append(CustomTagUtil.getDisplayName(config, getAppendix()))
        msg.append("] in the following directories [")
        msg.append(strRealPathes.replace(File.pathSeparatorChar, ','))
        msg.append(']')
        throw ExpressionException(msg.toString())
    }
}