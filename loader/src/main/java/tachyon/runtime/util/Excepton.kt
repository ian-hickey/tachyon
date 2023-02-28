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
package tachyon.runtime.util

import java.io.IOException

/**
 * class to get exceptions of different types
 */
interface Excepton {
    /**
     * create exception "Abort"
     *
     * @return Abort
     */
    fun createAbort(): PageException?

    /**
     * create exception "AbortException"
     *
     * @param showError show error
     * @return AbortException
     */
    fun createAbortException(showError: String?): PageException?

    /**
     * create exception "ApplicationException"
     *
     * @param message Message
     * @return ApplicationException
     */
    fun createApplicationException(message: String?): PageException?

    /**
     * create exception "ApplicationException"
     *
     * @param message Message
     * @param detail Detail
     * @return ApplicationException
     */
    fun createApplicationException(message: String?, detail: String?): PageException?

    /**
     * create exception "CasterException"
     *
     * @param message Message
     * @return CasterException
     */
    fun createCasterException(message: String?): PageException?
    fun createCasterException(obj: Object?, className: String?): PageException?
    fun createCasterException(obj: Object?, clazz: Class?): PageException?

    /**
     * create exception "CustomTypeException"
     *
     * @param message Message
     * @param detail Detail
     * @param errorcode Error Code
     * @param customType Custom Type
     * @return CustomTypeException
     */
    @Deprecated
    @Deprecated("""use instead
	              <code>createCustomTypeException(String message, String detail, String errorcode, String customType, String extendedInfo);</code>""")
    fun createCustomTypeException(message: String?, detail: String?, errorcode: String?, customType: String?): PageException?
    fun createCustomTypeException(message: String?, detail: String?, errorcode: String?, customType: String?, extendedInfo: String?): PageException?

    /**
     * create exception "DatabaseException"
     *
     * @param message Message
     * @return DatabaseException
     */
    fun createDatabaseException(message: String?): PageException?

    /**
     * create exception "DatabaseException"
     *
     * @param message Message
     * @param detail Detail
     * @return DatabaseException
     */
    fun createDatabaseException(message: String?, detail: String?): PageException?

    /**
     * create exception "DatabaseException"
     *
     * @param message Message
     * @param sql SQL
     * @return DatabaseException
     */
    fun createDatabaseException(message: String?, sql: SQL?): PageException?

    /**
     * create exception "ExpressionException"
     *
     * @param message Message
     * @return ExpressionException
     */
    fun createExpressionException(message: String?): PageException?

    /**
     * create exception "ExpressionException"
     *
     * @param message Message
     * @param detail Detail
     * @return ExpressionException
     */
    fun createExpressionException(message: String?, detail: String?): PageException?

    /**
     * create exception "FunctionException"
     *
     * @param pc Page Context
     * @param functionName Function Name
     * @param badArgumentPosition Bad Argument Position
     * @param badArgumentName Bad Argument Name
     * @param message Message
     * @return FunctionException
     */
    @Deprecated
    @Deprecated("""use instead
	              <code>createFunctionException(PageContext pc,String functionName, int badArgumentPosition, String badArgumentName, String message, String detail))</code>""")
    fun createFunctionException(pc: PageContext?, functionName: String?, badArgumentPosition: String?, badArgumentName: String?, message: String?): PageException?

    /**
     * create exception "FunctionException"
     *
     * @param pc Page Context
     * @param functionName Function Name
     * @param badArgumentPosition Bad Argument Position
     * @param badArgumentName Bad Argument Name
     * @param message Message
     * @param detail Detail
     * @return FunctionException
     */
    fun createFunctionException(pc: PageContext?, functionName: String?, badArgumentPosition: Int, badArgumentName: String?, message: String?, detail: String?): PageException?

    /**
     * create exception "LockException"
     *
     * @param operation operation
     * @param name name
     * @param message Message
     * @return LockException
     */
    fun createLockException(operation: String?, name: String?, message: String?): PageException?

    /**
     * create exception "LockException"
     *
     * @param ps Page Source
     * @return LockException
     */
    fun createMissingIncludeException(ps: PageSource?): PageException?

    /**
     * create exception "NativeException"
     *
     * @param t Throwable
     * @return NativeException
     */
    fun createNativeException(t: Throwable?): PageException?

    /**
     * create exception "SecurityException"
     *
     * @param message Message
     * @return SecurityException
     */
    fun createSecurityException(message: String?): PageException?

    /**
     * create exception "SecurityException"
     *
     * @param message Message
     * @param detail Detail
     * @return SecurityException
     */
    fun createSecurityException(message: String?, detail: String?): PageException?

    /**
     * create exception "TemplateException"
     *
     * @param message Message
     * @return TemplateException
     */
    fun createTemplateException(message: String?): PageException?

    /**
     * create exception "TemplateException"
     *
     * @param message Message
     * @param detail Detail
     * @return TemplateException
     */
    fun createTemplateException(message: String?, detail: String?): PageException?

    /**
     * create exception "XMLException"
     *
     * @param message Message
     * @return XMLException
     */
    fun createXMLException(message: String?): PageException?

    /**
     * create exception "XMLException"
     *
     * @param message Message
     * @param detail Detail
     * @return XMLException
     */
    fun createXMLException(message: String?, detail: String?): PageException?

    /**
     * check if exception is of given type
     *
     * @param type type to check
     * @param t exception to check
     * @return is of type
     */
    fun isOfType(type: Int, t: Throwable?): Boolean
    fun similarKeyMessage(keys: Array<Collection.Key?>?, keySearched: String?, keyLabel: String?, keyLabels: String?, `in`: String?, listAll: Boolean): String?
    fun createPageRuntimeException(pe: PageException?): RuntimeException?
    fun createFunctionException(pc: PageContext?, functionName: String?, min: Int, max: Int, actual: Int): PageException?
    fun toIOException(t: Throwable?): IOException?

    companion object {
        /**
         * Field `TYPE_ABORT`
         */
        const val TYPE_ABORT = 0

        /**
         * Field `TYPE_ABORT_EXP`
         */
        const val TYPE_ABORT_EXP = 1

        /**
         * Field `TYPE_APPLICATION_EXP`
         */
        const val TYPE_APPLICATION_EXP = 2

        /**
         * Field `TYPE_CASTER_EXP`
         */
        const val TYPE_CASTER_EXP = 3

        /**
         * Field `TYPE_CUSTOM_TYPE_EXP`
         */
        const val TYPE_CUSTOM_TYPE_EXP = 4

        /**
         * Field `TYPE_DATABASE_EXP`
         */
        const val TYPE_DATABASE_EXP = 5

        /**
         * Field `TYPE_EXPRESSION_EXP`
         */
        const val TYPE_EXPRESSION_EXP = 6

        /**
         * Field `TYPE_FUNCTION_EXP`
         */
        const val TYPE_FUNCTION_EXP = 7

        /**
         * Field `TYPE_LOCK_EXP`
         */
        const val TYPE_LOCK_EXP = 8

        /**
         * Field `TYPE_MISSING_INCLUDE_EXP`
         */
        const val TYPE_MISSING_INCLUDE_EXP = 9

        /**
         * Field `TYPE_NATIVE_EXP`
         */
        const val TYPE_NATIVE_EXP = 10

        /**
         * Field `TYPE_SECURITY_EXP`
         */
        const val TYPE_SECURITY_EXP = 11

        /**
         * Field `TYPE_TEMPLATE_EXP`
         */
        const val TYPE_TEMPLATE_EXP = 12

        /**
         * Field `TYPE_XML_EXP`
         */
        const val TYPE_XML_EXP = 13
    }
}