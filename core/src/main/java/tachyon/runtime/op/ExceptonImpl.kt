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

import java.io.IOException

/**
 * Implementation of Exception Util
 */
class ExceptonImpl : Excepton {
    companion object {
        private val exceptions: Array<Class?>? = arrayOfNulls<Class?>(14)
        private var singelton: ExceptonImpl? = null

        /**
         * @return singleton instance
         */
        val instance: Excepton?
            get() {
                if (singelton == null) singelton = ExceptonImpl()
                return singelton
            }

        init {
            exceptions!![TYPE_ABORT] = Abort::class.java
            exceptions[TYPE_ABORT_EXP] = AbortException::class.java
            exceptions[TYPE_APPLICATION_EXP] = ApplicationException::class.java
            exceptions[TYPE_CASTER_EXP] = CasterException::class.java
            exceptions[TYPE_CUSTOM_TYPE_EXP] = CustomTypeException::class.java
            exceptions[TYPE_DATABASE_EXP] = DatabaseException::class.java
            exceptions[TYPE_EXPRESSION_EXP] = ExpressionException::class.java
            exceptions[TYPE_FUNCTION_EXP] = FunctionException::class.java
            exceptions[TYPE_LOCK_EXP] = LockException::class.java
            exceptions[TYPE_MISSING_INCLUDE_EXP] = MissingIncludeException::class.java
            exceptions[TYPE_NATIVE_EXP] = NativeException::class.java
            exceptions[TYPE_SECURITY_EXP] = SecurityException::class.java
            exceptions[TYPE_TEMPLATE_EXP] = TemplateException::class.java
            exceptions[TYPE_XML_EXP] = XMLException::class.java
        }
    }

    @Override
    fun createAbort(): PageException? {
        return Abort(Abort.SCOPE_REQUEST)
    }

    @Override
    fun createAbortException(showError: String?): PageException? {
        return AbortException(showError)
    }

    @Override
    fun createApplicationException(message: String?): PageException? {
        return ApplicationException(message)
    }

    @Override
    fun createApplicationException(message: String?, detail: String?): PageException? {
        return ApplicationException(message, detail)
    }

    @Override
    fun createCasterException(message: String?): PageException? {
        return CasterException(message)
    }

    @Override
    fun createCasterException(obj: Object?, className: String?): PageException? {
        return CasterException(obj, className)
    }

    @Override
    fun createCasterException(obj: Object?, clazz: Class?): PageException? {
        return CasterException(obj, clazz)
    }

    @Override
    fun createCustomTypeException(message: String?, detail: String?, errorcode: String?, customType: String?): PageException? {
        return createCustomTypeException(message, detail, errorcode, customType, null)
    }

    @Override
    fun createCustomTypeException(message: String?, detail: String?, errorcode: String?, customType: String?, extendedInfo: String?): PageException? {
        return CustomTypeException(message, detail, errorcode, customType, extendedInfo)
    }

    @Override
    fun createDatabaseException(message: String?): PageException? {
        return DatabaseException(message, null, null, null)
    }

    @Override
    fun createDatabaseException(message: String?, detail: String?): PageException? {
        return DatabaseException(message, detail, null, null)
    }

    @Override
    fun createDatabaseException(message: String?, sql: SQL?): PageException? {
        return DatabaseException(message, null, sql, null)
    }

    @Override
    fun createExpressionException(message: String?): PageException? {
        return ExpressionException(message)
    }

    @Override
    fun createExpressionException(message: String?, detail: String?): PageException? {
        return ExpressionException(message, detail)
    }

    @Override
    fun createFunctionException(pc: PageContext?, functionName: String?, badArgumentPosition: String?, badArgumentName: String?, message: String?): PageException? {
        return FunctionException(pc, functionName, badArgumentPosition, badArgumentName, message, null)
    }

    @Override
    fun createFunctionException(pc: PageContext?, functionName: String?, badArgumentPosition: Int, badArgumentName: String?, message: String?, detail: String?): PageException? {
        return FunctionException(pc, functionName, badArgumentPosition, badArgumentName, message, detail)
    }

    @Override
    fun createFunctionException(pc: PageContext?, functionName: String?, min: Int, max: Int, actual: Int): PageException? {
        return FunctionException(pc, functionName, min, max, actual)
    }

    @Override
    fun createLockException(operation: String?, name: String?, message: String?): PageException? {
        return LockException(operation, name, message)
    }

    @Override
    fun createMissingIncludeException(ps: PageSource?): PageException? {
        return MissingIncludeException(ps)
    }

    @Override
    fun createNativeException(t: Throwable?): PageException? {
        return NativeException.newInstance(t)
    }

    @Override
    fun createSecurityException(message: String?): PageException? {
        return SecurityException(message)
    }

    @Override
    fun createSecurityException(message: String?, detail: String?): PageException? {
        return SecurityException(message, detail)
    }

    @Override
    fun createTemplateException(message: String?): PageException? {
        return TemplateException(message)
    }

    @Override
    fun createTemplateException(message: String?, detail: String?): PageException? {
        return TemplateException(message, detail)
    }

    @Override
    fun createXMLException(message: String?): PageException? {
        return XMLException(message)
    }

    @Override
    fun createXMLException(message: String?, detail: String?): PageException? {
        return XMLException(message, detail)
    }

    @Override
    fun isOfType(type: Int, t: Throwable?): Boolean {
        when (type) {
            TYPE_ABORT -> return Abort.isSilentAbort(t)
            TYPE_ABORT_EXP -> return t is AbortException
            TYPE_APPLICATION_EXP -> return t is ApplicationException
            TYPE_CASTER_EXP -> return t is CasterException
            TYPE_CUSTOM_TYPE_EXP -> return t is CustomTypeException
            TYPE_DATABASE_EXP -> return t is DatabaseException
            TYPE_EXPRESSION_EXP -> return t is ExpressionException
            TYPE_FUNCTION_EXP -> return t is FunctionException
            TYPE_LOCK_EXP -> return t is LockException
            TYPE_MISSING_INCLUDE_EXP -> return t is MissingIncludeException
            TYPE_NATIVE_EXP -> return t is NativeException
            TYPE_SECURITY_EXP -> return t is SecurityException
            TYPE_TEMPLATE_EXP -> return t is TemplateException
            TYPE_XML_EXP -> return t is XMLException
        }
        return Reflector.isInstaneOf(t.getClass(), exceptions!![type], false)
    }

    @Override
    fun similarKeyMessage(keys: Array<Collection.Key?>?, keySearched: String?, keyLabel: String?, keyLabels: String?, `in`: String?, listAll: Boolean): String? {
        return ExceptionUtil.similarKeyMessage(keys, keySearched, keyLabel, keyLabels, `in`, listAll)
    }

    @Override
    fun createPageRuntimeException(pe: PageException?): RuntimeException? {
        return PageRuntimeException(pe)
    }

    @Override
    fun toIOException(t: Throwable?): IOException? {
        return ExceptionUtil.toIOException(t)
    }
}