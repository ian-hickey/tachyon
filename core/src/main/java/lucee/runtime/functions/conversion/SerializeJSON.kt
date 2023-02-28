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
package lucee.runtime.functions.conversion

import java.nio.charset.Charset

/**
 * Decodes Binary Data that are encoded as String
 */
object SerializeJSON : Function {
    private const val serialVersionUID = -4632952919389635891L
    @Throws(PageException::class)
    fun call(pc: PageContext?, `var`: Object?): String? {
        return _call(pc, `var`, "", pc.getWebCharset(), false)
    }

    @Throws(PageException::class)
    fun call(pc: PageContext?, `var`: Object?, queryFormat: Object?): String? {
        return _call(pc, `var`, queryFormat, pc.getWebCharset(), false)
    }

    @Throws(PageException::class)
    fun call(pc: PageContext?, `var`: Object?, queryFormat: Object?, useSecureJSONPrefixOrCharset: Object?): String? {
        // TODO all options to be a struct

        // useSecureJSONPrefix
        var cs: Charset = pc.getWebCharset()
        var useSecureJSONPrefix = false
        if (Decision.isCastableToBoolean(useSecureJSONPrefixOrCharset)) {
            useSecureJSONPrefix = Caster.toBooleanValue(useSecureJSONPrefixOrCharset)
        } else if (!StringUtil.isEmpty(useSecureJSONPrefixOrCharset)) {
            cs = CharsetUtil.toCharset(Caster.toString(useSecureJSONPrefixOrCharset))
        }
        return _call(pc, `var`, queryFormat, cs, useSecureJSONPrefix)
    }

    @Throws(PageException::class)
    private fun _call(pc: PageContext?, `var`: Object?, queryFormat: Object?, charset: Charset?, useSecureJSONPrefix: Boolean): String? {
        return try {
            val json = JSONConverter(true, charset, JSONDateFormat.PATTERN_CF)
            var qf: Int = JSONConverter.toQueryFormat(queryFormat, SerializationSettings.SERIALIZE_AS_UNDEFINED)
            if (qf == SerializationSettings.SERIALIZE_AS_UNDEFINED) {
                if (!StringUtil.isEmpty(queryFormat)) throw FunctionException(pc, SerializeJSON::class.java.getSimpleName(), 2, "queryFormat",
                        "When var is a Query, argument [queryFormat] must be either a boolean value or a string with the value of [struct], [row], or [column]")
                val acs: ApplicationContextSupport = pc.getApplicationContext() as ApplicationContextSupport
                val settings: SerializationSettings = acs.getSerializationSettings()
                qf = settings.getSerializeQueryAs()
            }

            // TODO get secure prefix from application.cfc
            if (useSecureJSONPrefix) "// " + json.serialize(pc, `var`, qf) else json.serialize(pc, `var`, qf)
        } catch (e: ConverterException) {
            throw Caster.toPageException(e)
        }
    }
}