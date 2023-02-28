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
package tachyon.runtime.op

import tachyon.commons.lang.StringUtil

/**
 * implementation of the interface Decision
 */
class DecisionImpl : tachyon.runtime.util.Decision {
    @Override
    fun isArray(o: Object?): Boolean {
        return Decision.isArray(o)
    }

    @Override
    fun isBinary(`object`: Object?): Boolean {
        return Decision.isBinary(`object`)
    }

    @Override
    fun isBoolean(value: Object?): Boolean {
        return Decision.isBoolean(value)
    }

    @Override
    fun isBoolean(str: String?): Boolean {
        return Decision.isBoolean(str)
    }

    @Override
    fun isComponent(`object`: Object?): Boolean {
        return Decision.isComponent(`object`)
    }

    @Override
    fun isDate(value: Object?, alsoNumbers: Boolean): Boolean {
        return Decision.isDateAdvanced(value, alsoNumbers)
    }

    @Override
    fun isEmpty(str: String?, trim: Boolean): Boolean {
        return StringUtil.isEmpty(str, trim)
    }

    @Override
    fun isEmpty(str: String?): Boolean {
        return StringUtil.isEmpty(str)
    }

    @Override
    fun isHex(str: String?): Boolean {
        return Decision.isHex(str)
    }

    @Override
    fun isLeapYear(year: Int): Boolean {
        return Decision.isLeapYear(year)
    }

    @Override
    fun isNativeArray(o: Object?): Boolean {
        return Decision.isNativeArray(o)
    }

    @Override
    fun isNumeric(value: Object?): Boolean {
        return isNumber(value)
    }

    @Override
    fun isNumeric(str: String?): Boolean {
        return isNumber(str)
    }

    @Override
    fun isNumber(value: Object?): Boolean {
        return Decision.isNumber(value)
    }

    @Override
    fun isNumber(str: String?): Boolean {
        return Decision.isNumber(str)
    }

    @Override
    fun isObject(o: Object?): Boolean {
        return Decision.isObject(o)
    }

    @Override
    fun isQuery(`object`: Object?): Boolean {
        return Decision.isQuery(`object`)
    }

    @Override
    fun isSimpleValue(value: Object?): Boolean {
        return Decision.isSimpleValue(value)
    }

    @Override
    fun isSimpleVariableName(string: String?): Boolean {
        return Decision.isSimpleVariableName(string)
    }

    @Override
    fun isStruct(o: Object?): Boolean {
        return Decision.isStruct(o)
    }

    @Override
    fun isUserDefinedFunction(`object`: Object?): Boolean {
        return Decision.isUserDefinedFunction(`object`)
    }

    @Override
    fun isUUID(str: String?): Boolean {
        return Decision.isUUId(str)
    }

    @Override
    fun isVariableName(string: String?): Boolean {
        return Decision.isVariableName(string)
    }

    @Override
    fun isWddx(o: Object?): Boolean {
        return Decision.isWddx(o)
    }

    @Override
    fun isXML(o: Object?): Boolean {
        return Decision.isXML(o)
    }

    @Override
    fun isXMLDocument(o: Object?): Boolean {
        return Decision.isXMLDocument(o)
    }

    @Override
    fun isXMLElement(o: Object?): Boolean {
        return Decision.isXMLElement(o)
    }

    @Override
    fun isXMLRootElement(o: Object?): Boolean {
        return Decision.isXMLRootElement(o)
    }

    @Override
    @Throws(PageException::class)
    fun toKey(obj: Object?): Key? {
        return KeyImpl.toKey(obj)
    }

    @Override
    fun toKey(obj: Object?, defaultValue: Key?): Key? {
        return KeyImpl.toKey(obj, defaultValue)
    }

    @Override
    fun isAnyType(type: String?): Boolean {
        return Decision.isAnyType(type)
    }

    @Override
    fun isValid(dbl: Double): Boolean {
        return Decision.isValid(dbl)
    }

    /*
	 * public boolean isTemplateExtension(String ext) { return Constants.isTemplateExtension(ext); }
	 * 
	 * public boolean isComponentExtension(String ext) { return Constants.isComponentExtension(ext); }
	 */
    @Override
    fun isCastableToBoolean(obj: Object?): Boolean {
        return Decision.isCastableToBoolean(obj)
    }

    @Override
    fun isCastableTo(type: String?, o: Object?, alsoAlias: Boolean, alsoPattern: Boolean, maxlength: Int): Boolean {
        return Decision.isCastableTo(type, o, alsoAlias, alsoPattern, maxlength)
    }

    @Override
    fun isCastableToArray(o: Object?): Boolean {
        return Decision.isCastableToArray(o)
    }

    @Override
    fun isCastableToBinary(`object`: Object?, checkBase64String: Boolean): Boolean {
        return Decision.isCastableToBinary(`object`, checkBase64String)
    }

    @Override
    fun isCastableToDate(o: Object?): Boolean {
        return Decision.isCastableToDate(o)
    }

    @Override
    fun isCastableToNumeric(o: Object?): Boolean {
        return Decision.isCastableToNumeric(o)
    }

    @Override
    fun isCastableToString(o: Object?): Boolean {
        return Decision.isCastableToString(o)
    }

    @Override
    fun isCastableToStruct(o: Object?): Boolean {
        return Decision.isCastableToStruct(o)
    }

    @Override
    fun isClosure(o: Object?): Boolean {
        return Decision.isClosure(o)
    }

    @Override
    fun isLambda(o: Object?): Boolean {
        return Decision.isLambda(o)
    }

    @Override
    fun isCreditCard(o: Object?): Boolean {
        return Decision.isCreditCard(o)
    }

    @Override
    fun isEmpty(o: Object?): Boolean {
        return Decision.isEmpty(o)
    }

    @Override
    fun isGUid(o: Object?): Boolean {
        return Decision.isGUId(o)
    }

    @Override
    @Throws(ExpressionException::class)
    fun `is`(type: String?, o: Object?): Boolean {
        return Decision.isValid(type, o)
    }

    @Override
    fun isFunction(o: Object?): Boolean {
        return Decision.isFunction(o)
    }

    companion object {
        private var singelton: DecisionImpl? = null
        val instance: tachyon.runtime.util.Decision?
            get() {
                if (singelton == null) singelton = DecisionImpl()
                return singelton
            }
    }
}