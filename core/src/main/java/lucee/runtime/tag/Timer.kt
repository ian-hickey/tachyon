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
package lucee.runtime.tag

import java.io.IOException

class Timer : BodyTagImpl() {
    private var label: String? = ""
    private var type = TYPE_DEBUG
    private var unit = UNIT_MILLI
    private var unitDesc: String? = "ms"

    //private double time;
    private var time: Long = 0
    private var exe: Long = 0
    private var variable: String? = null
    @Override
    fun release() {
        super.release()
        type = TYPE_DEBUG
        unit = UNIT_MILLI
        label = ""
        unitDesc = "ms"
        variable = null
    }

    /**
     * @param label the label to set
     */
    fun setLabel(label: String?) {
        this.label = label
    }

    /**
     * @param type the type to set
     * @throws ApplicationException
     */
    @Throws(ApplicationException::class)
    fun setType(strType: String?) {
        var strType = strType
        strType = strType.toLowerCase().trim()
        type = if ("comment".equals(strType)) TYPE_COMMENT else if ("console".equals(strType)) TYPE_CONSOLE else if ("debug".equals(strType)) TYPE_DEBUG else if ("inline".equals(strType)) TYPE_INLINE else if ("outline".equals(strType)) TYPE_OUTLINE else throw ApplicationException("Tag [timer] has an invalid value [$strType] for attribute [type], valid values are [comment, console, debug, inline, outline]")
    }

    /**
     * @param unit the unit to set
     * @throws ApplicationException
     */
    @Throws(ApplicationException::class)
    fun setUnit(strUnit: String?) {
        if (!StringUtil.isEmpty(strUnit, true)) {
            val c: Char = strUnit.charAt(0)
            if (c == 'n' || c == 'N') {
                unit = UNIT_NANO
                unitDesc = "ns"
                return
            } else if (c == 'm' || c == 'M') {
                if ("micro".equalsIgnoreCase(strUnit.trim())) {
                    unit = UNIT_MICRO
                    unitDesc = "us"
                    return
                }
                unit = UNIT_MILLI
                unitDesc = "ms" // default
                return
            } else if (c == 's' || c == 'S') {
                unit = UNIT_SECOND
                unitDesc = "s"
                return
            }
            throw ApplicationException("Tag [timer] has an invalid value [$strUnit] for attribute [unit], valid values are [nano, micro, milli, second]")
        }
        unit = UNIT_MILLI
        unitDesc = "ms" // default
    }

    /**
     * Set the value variable, tThe name of the variable in which to save the execution time into
     * tag.
     *
     * @param variable value to set
     */
    fun setVariable(variable: String?) {
        this.variable = variable
    }

    private val currentTime: Long
        private get() = when (unit) {
            UNIT_NANO -> System.nanoTime()
            UNIT_MICRO -> System.nanoTime() / 1000
            UNIT_SECOND -> System.currentTimeMillis() / 1000
            else -> System.currentTimeMillis()
        }

    @Override
    fun doStartTag(): Int {
        time = currentTime
        if (TYPE_OUTLINE == type) {
            try {
                pageContext.write("<fieldset class=\"cftimer\">")
            } catch (e: IOException) {
            }
        }
        return EVAL_BODY_INCLUDE
    }

    @Override
    @Throws(PageException::class)
    fun doEndTag(): Int {
        try {
            _doEndTag()
        } catch (e: IOException) {
        }
        return EVAL_PAGE
    }

    @Throws(IOException::class, PageException::class)
    fun _doEndTag() {
        exe = Caster.toLong(currentTime - time)
        if (!StringUtil.isEmpty(variable, true)) pageContext.setVariable(variable, exe)
        if (TYPE_INLINE == type) {
            pageContext.write("$label: $exe$unitDesc")
        } else if (TYPE_OUTLINE == type) {
            pageContext.write("<legend align=\"top\">$label: $exe$unitDesc</legend></fieldset>")
        } else if (TYPE_COMMENT == type) {
            pageContext.write("<!-- $label: $exe$unitDesc -->")
        } else if (TYPE_DEBUG == type) {
            if (pageContext.getConfig().debug()) {
                val curr: PageSource = pageContext.getCurrentTemplatePageSource()
                // TODO need to include unitDesc?
                pageContext.getDebugger().addTimer(label, exe, if (curr == null) "unknown template" else curr.getDisplayPath())
            }
        } else if (TYPE_CONSOLE == type) {
            val curr: PageSource = pageContext.getCurrentTemplatePageSource()
            val currTemplate = if (curr != null) " from  template: " + curr.getDisplayPath() else ""
            if (StringUtil.isEmpty(label, true)) label = "CFTimer"
            CFMLEngineImpl.CONSOLE_OUT.println("$label: $exe$unitDesc$currTemplate")
        }
    }

    @Override
    fun doInitBody() {
    }

    @Override
    fun doAfterBody(): Int {
        return SKIP_BODY
    }

    companion object {
        private const val TYPE_DEBUG = 0
        private const val TYPE_INLINE = 1
        private const val TYPE_OUTLINE = 2
        private const val TYPE_COMMENT = 3
        private const val TYPE_CONSOLE = 4
        private const val UNIT_NANO = 1
        private const val UNIT_MILLI = 2
        private const val UNIT_MICRO = 4
        private const val UNIT_SECOND = 8
    }
}