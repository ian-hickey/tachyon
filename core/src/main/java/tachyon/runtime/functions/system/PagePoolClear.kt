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
/**
 * Implements the CFML Function gettemplatepath
 */
package tachyon.runtime.functions.system

import java.util.Collection

class PagePoolClear : BIF(), Function {
    @Override
    @Throws(PageException::class)
    operator fun invoke(pc: PageContext?, args: Array<Object?>?): Object? {
        return if (args!!.size == 0) call(pc) else throw FunctionException(pc, "PagePoolClear", 0, 0, args.size)
    }

    companion object {
        private const val serialVersionUID = -2777306151061026079L
        fun call(pc: PageContext?): Boolean {
            clear(pc, null, false)
            return true
        }

        fun clear(pc: PageContext?, c: Config?, unused: Boolean) {
            var pc: PageContext? = pc
            val config: ConfigWebPro
            pc = ThreadLocalPageContext.get(pc)
            config = if (c == null) ThreadLocalPageContext.getConfig(pc) as ConfigWebPro else c as ConfigWebPro?

            // application context
            if (pc != null) {
                val ac: ApplicationContext = pc.getApplicationContext()
                if (ac != null) {
                    clear(config, ac.getMappings(), unused)
                    clear(config, ac.getComponentMappings(), unused)
                    clear(config, ac.getCustomTagMappings(), unused)
                }
            }

            // config
            clear(config, config.getMappings(), unused)
            clear(config, config.getCustomTagMappings(), unused)
            clear(config, config.getComponentMappings(), unused)
            clear(config, config.getFunctionMappings(), unused)
            clear(config, config.getServerFunctionMappings(), unused)
            clear(config, config.getTagMappings(), unused)
            clear(config, config.getServerTagMappings(), unused)
        }

        fun clear(config: Config?, mappings: Collection<Mapping?>?, unused: Boolean) {
            if (mappings == null) return
            val it: Iterator<Mapping?> = mappings.iterator()
            while (it.hasNext()) {
                clear(config, it.next(), unused)
            }
        }

        fun clear(config: Config?, mappings: Array<Mapping?>?, unused: Boolean) {
            if (mappings == null) return
            for (i in mappings.indices) {
                clear(config, mappings[i], unused)
            }
        }

        fun clear(config: Config?, mapping: Mapping?, unused: Boolean) {
            if (mapping == null) return
            val mi: MappingImpl? = mapping as MappingImpl?
            if (unused) {
                mi.clearUnused(config)
            } else {
                mi.clearPages(null)
            }
        }
    }
}