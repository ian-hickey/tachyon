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
package lucee.transformer.bytecode.util

import java.io.BufferedReader

object Types {
    // TODO muss wohl alle Prim typen sein plus Object
    const val _BOOLEAN = 1
    const val _NUMBER = 2
    private const val _SHORT = 7
    const val _OBJECT = 0
    const val _STRING = 3
    private const val _CHAR = _NUMBER
    private const val _FLOAT = _NUMBER
    private const val _LONG = _NUMBER
    private const val _INT = _NUMBER
    private const val _BYTE = _NUMBER
    private const val _DOUBLE = _NUMBER

    // public static final int SIZE_INT_TYPES=10;
    val ABORT: Type? = Type.getType(Abort::class.java)
    val ARRAY: Type? = Type.getType(Array::class.java)
    val ARRAY_IMPL: Type? = Type.getType(lucee.runtime.type.ArrayImpl::class.java)
    val BYTE: Type? = Type.getType(Byte::class.java)
    val BYTE_VALUE: Type? = Type.getType(Byte::class.javaPrimitiveType)
    val BYTE_ARRAY: Type? = Type.getType(Array<Byte>::class.java)
    val BYTE_VALUE_ARRAY: Type? = Type.getType(ByteArray::class.java)
    val BOOLEAN: Type? = Type.getType(Boolean::class.java)
    val BOOLEAN_VALUE: Type? = Type.getType(Boolean::class.javaPrimitiveType)
    val CHAR: Type? = Type.getType(Char::class.javaPrimitiveType)
    val CHARACTER: Type? = Type.getType(Character::class.java)
    val DOUBLE: Type? = Type.getType(Double::class.java)
    val DOUBLE_VALUE: Type? = Type.getType(Double::class.javaPrimitiveType)
    val FLOAT: Type? = Type.getType(Float::class.java)
    val FLOAT_VALUE: Type? = Type.getType(Float::class.javaPrimitiveType)

    // public static final Type IMAGE = Type.getType(Image.class);
    val INTEGER: Type? = Type.getType(Integer::class.java)
    val INT_VALUE: Type? = Type.getType(Int::class.javaPrimitiveType)
    val LONG: Type? = Type.getType(Long::class.java)
    val LOCALE: Type? = Type.getType(Locale::class.java)
    val LONG_VALUE: Type? = Type.getType(Long::class.javaPrimitiveType)
    val SHORT: Type? = Type.getType(Short::class.java)
    val SHORT_VALUE: Type? = Type.getType(Short::class.javaPrimitiveType)
    val NUMBER: Type? = Type.getType(Number::class.java)
    val COMPONENT: Type? = Type.getType(lucee.runtime.Component::class.java)
    val PAGE: Type? = Type.getType(Page::class.java)
    val PAGE_IMPL: Type? = Type.getType(PageImpl::class.java)
    val PAGE_SOURCE: Type? = Type.getType(PageSource::class.java)
    val COMPONENT_PAGE_IMPL: Type? = Type.getType(lucee.runtime.ComponentPageImpl::class.java)
    val INTERFACE_PAGE_IMPL: Type? = Type.getType(InterfacePageImpl::class.java)
    val COMPONENT_IMPL: Type? = Type.getType(lucee.runtime.ComponentImpl::class.java)
    val INTERFACE_IMPL: Type? = Type.getType(lucee.runtime.InterfaceImpl::class.java)
    val DATE_TIME: Type? = Type.getType(lucee.runtime.type.dt.DateTime::class.java)
    val DATE: Type? = Type.getType(java.util.Date::class.java)
    val FILE: Type? = Type.getType(java.io.File::class.java)

    // public static final Type EXCEL=Type.getType(Excel.class);
    // public static final Type EXCEL_UTIL=Type.getType(ExcelUtil.class);
    val RESOURCE: Type? = Type.getType(Resource::class.java)
    val FUNCTION_VALUE: Type? = Type.getType(FunctionValue::class.java)
    val ITERATOR: Type? = Type.getType(Iterator::class.java)
    val ITERATORABLE: Type? = Type.getType(Iteratorable::class.java)
    val NODE: Type? = Type.getType(org.w3c.dom.Node::class.java)
    val OBJECT: Type? = Type.getType(Object::class.java)
    val OBJECT_ARRAY: Type? = Type.getType(Array<Object>::class.java)
    val PAGE_CONTEXT: Type? = Type.getType(PageContext::class.java)
    val PAGE_CONTEXT_IMPL: Type? = Type.getType(PageContextImpl::class.java)
    val PAGE_CONTEXT_UTIL: Type? = Type.getType(PageContextUtil::class.java)
    val QUERY: Type? = Type.getType(lucee.runtime.type.Query::class.java)
    val QUERY_COLUMN: Type? = Type.getType(lucee.runtime.type.QueryColumn::class.java)
    val PAGE_EXCEPTION: Type? = Type.getType(PageException::class.java)
    val REFERENCE: Type? = Type.getType(Reference::class.java)
    val CASTER: Type? = Type.getType(Caster::class.java)
    val COLLECTION: Type? = Type.getType(Collection::class.java)
    val STRING: Type? = Type.getType(String::class.java)
    val STRING_ARRAY: Type? = Type.getType(Array<String>::class.java)
    val STRING_UTIL: Type? = Type.getType(StringUtil::class.java)
    val STRUCT: Type? = Type.getType(lucee.runtime.type.Struct::class.java)
    val STRUCT_IMPL: Type? = Type.getType(lucee.runtime.type.StructImpl::class.java)
    val OP_UTIL: Type? = Type.getType(OpUtil::class.java)
    val CONFIG: Type? = Type.getType(Config::class.java)
    val CONFIG_WEB: Type? = Type.getType(ConfigWeb::class.java)
    val SCOPE: Type? = Type.getType(Scope::class.java)
    val VARIABLES: Type? = Type.getType(Variables::class.java)
    val TIMESPAN: Type? = Type.getType(lucee.runtime.type.dt.TimeSpan::class.java)
    val THROWABLE: Type? = Type.getType(Throwable::class.java)
    val EXCEPTION: Type? = Type.getType(Exception::class.java)
    val VOID: Type? = Type.VOID_TYPE
    val LIST_UTIL: Type? = Type.getType(ListUtil::class.java)
    val VARIABLE_INTERPRETER: Type? = Type.getType(VariableInterpreter::class.java)
    val VARIABLE_REFERENCE: Type? = Type.getType(VariableReference::class.java)
    val JSP_WRITER: Type? = Type.getType(JspWriter::class.java)
    val TAG: Type? = Type.getType(Tag::class.java)
    val NUMBER_RANGE: Type? = Type.getType(NumberRange::class.java)
    val NULL_SUPPORT_HELPER: Type? = Type.getType(NullSupportHelper::class.java)
    val SECURITY_MANAGER: Type? = Type.getType(SecurityManager::class.java)
    val READER: Type? = Type.getType(Reader::class.java)
    val BUFFERED_READER: Type? = Type.getType(BufferedReader::class.java)
    val ARRAY_UTIL: Type? = Type.getType(ArrayUtil::class.java)
    val EXCEPTION_HANDLER: Type? = Type.getType(ExceptionHandler::class.java)

    // public static final Type RETURN_ EXCEPTION = Type.getType(ReturnException.class);
    val TIMEZONE: Type? = Type.getType(java.util.TimeZone::class.java)
    val STRING_BUFFER: Type? = Type.getType(StringBuffer::class.java)
    val STRING_BUILDER: Type? = Type.getType(StringBuilder::class.java)
    val MEMBER: Type? = Type.getType(Member::class.java)
    val UDF: Type? = Type.getType(UDF::class.java)
    val UDF_PROPERTIES: Type? = Type.getType(UDFProperties::class.java)
    val UDF_PROPERTIES_IMPL: Type? = Type.getType(UDFPropertiesImpl::class.java)
    val UDF_IMPL: Type? = Type.getType(UDFImpl::class.java)
    val CLOSURE: Type? = Type.getType(Closure::class.java)
    val LAMBDA: Type? = Type.getType(Lambda::class.java)
    val UDF_PROPERTIES_ARRAY: Type? = Type.getType(Array<UDFProperties>::class.java)

    // public static final Type UDF_IMPL_ARRAY = Type.getType(UDFImpl[].class);
    val KEY_CONSTANTS: Type? = Type.getType(KeyConstants::class.java)
    val COLLECTION_KEY: Type? = Type.getType(Collection.Key::class.java)
    val COLLECTION_KEY_ARRAY: Type? = Type.getType(Array<Collection.Key>::class.java)
    val UNDEFINED: Type? = Type.getType(Undefined::class.java)
    val MAP: Type? = Type.getType(Map::class.java)
    val MAP_ENTRY: Type? = Type.getType(Map.Entry::class.java)
    val CHAR_ARRAY: Type? = Type.getType(CharArray::class.java)
    val IOUTIL: Type? = Type.getType(IOUtil::class.java)
    val BODY_CONTENT: Type? = Type.getType(BodyContent::class.java)
    val BODY_CONTENT_UTIL: Type? = Type.getType(BodyContentUtil::class.java)
    val IMPORT_DEFINITIONS: Type? = Type.getType(ImportDefintion::class.java)
    val IMPORT_DEFINITIONS_IMPL: Type? = Type.getType(ImportDefintionImpl::class.java)
    val IMPORT_DEFINITIONS_ARRAY: Type? = Type.getType(Array<ImportDefintion>::class.java)
    val CI_PAGE: Type? = Type.getType(CIPage::class.java)
    val CI_PAGE_ARRAY: Type? = Type.getType(Array<CIPage>::class.java)
    val CLASS: Type? = Type.getType(Class::class.java)
    val CLASS_ARRAY: Type? = Type.getType(Array<Class>::class.java)
    val CLASS_LOADER: Type? = Type.getType(ClassLoader::class.java)
    val BIG_DECIMAL: Type? = Type.getType(BigDecimal::class.java)
    val FUNCTION_VALUE_IMPL: Type? = Type.getType(FunctionValueImpl::class.java)
    val CALLER_UTIL: Type? = Type.getType(CallerUtil::class.java)
    val VARIABLE_UTIL_IMPL: Type? = Type.getType(VariableUtilImpl::class.java)
    val CONSTANTS: Type? = Type.getType(Constants::class.java)
    val CONSTANTS_DOUBLE: Type? = Type.getType(ConstantsDouble::class.java)
    val BODY_TAG: Type? = Type.getType(BodyTag::class.java)
    val DYNAMIC_ATTRIBUTES: Type? = Type.getType(DynamicAttributes::class.java)
    val IDENTIFICATION: Type? = Type.getType(Identification::class.java)
    val TAG_UTIL: Type? = Type.getType(TagUtil::class.java)
    val FUNCTION_HANDLER_POOL: Type? = Type.getType(FunctionHandlerPool::class.java)
    val BIF: Type? = Type.getType(lucee.runtime.ext.function.BIF::class.java)
    val DATA_MEMBER: Type? = Type.getType(lucee.runtime.component.DataMember::class.java)
    val EXPRESSION_EXCEPTION: Type? = Type.getType(ExpressionException::class.java)
    val STATIC_STRUCT: Type? = Type.getType(StaticStruct::class.java)

    /**
     * translate sString classname to a real type
     *
     * @param type
     * @return
     * @throws lucee.runtime.exp.TemplateExceptionption
     */
    @Throws(TransformerException::class)
    fun toType(bc: BytecodeContext?, type: String?): Type? {
        var type: String? = type ?: return OBJECT
        type = type.trim()
        val lcType: String = StringUtil.toLowerCase(type)
        val first: Char = lcType.charAt(0)
        when (first) {
            'a' -> {
                if ("any".equals(lcType)) return OBJECT
                if ("array".equals(lcType)) return ARRAY
            }
            'b' -> {
                if ("base64".equals(lcType)) return STRING
                if ("binary".equals(lcType)) return BYTE_VALUE_ARRAY
                if ("bool".equals(lcType) || "boolean".equals(type)) return BOOLEAN_VALUE
                if ("boolean".equals(lcType)) return BOOLEAN
                if ("byte".equals(type)) return BYTE_VALUE
                if ("byte".equals(lcType)) return BYTE
            }
            'c' -> {
                if ("char".equals(lcType)) return CHAR
                if ("character".equals(lcType)) return CHARACTER
                if ("collection".equals(lcType)) return BYTE_VALUE_ARRAY
                if ("component".equals(lcType)) return COMPONENT
                if ("class".equals(lcType)) return COMPONENT
            }
            'd' -> {
                if ("date".equals(lcType) || "datetime".equals(lcType)) return DATE_TIME
                if ("decimal".equals(lcType)) return STRING
                if ("double".equals(type)) return DOUBLE_VALUE
                if ("double".equals(lcType)) return DOUBLE
            }
            'e' -> {
            }
            'f' -> {
                if ("file".equals(lcType)) return FILE
                if ("float".equals(type)) return FLOAT_VALUE
                if ("float".equals(lcType)) return FLOAT
                if ("function".equals(lcType)) return UDF
            }
            'i' -> if ("int".equals(lcType)) return INT_VALUE else if ("integer".equals(lcType)) return INTEGER
            'j' -> {
                if ("java.lang.boolean".equals(lcType)) return BOOLEAN
                if ("java.lang.byte".equals(lcType)) return BYTE
                if ("java.lang.character".equals(lcType)) return CHARACTER
                if ("java.lang.short".equals(lcType)) return SHORT
                if ("java.lang.integer".equals(lcType)) return INTEGER
                if ("java.lang.long".equals(lcType)) return LONG
                if ("java.lang.float".equals(lcType)) return FLOAT
                if ("java.lang.double".equals(lcType)) return DOUBLE
                if ("java.io.file".equals(lcType)) return FILE
                if ("java.lang.string".equals(lcType)) return STRING
                if ("java.lang.string[]".equals(lcType)) return STRING_ARRAY
                if ("java.util.date".equals(lcType)) return DATE
                if ("java.lang.object".equals(lcType)) return OBJECT
            }
            'l' -> {
                if ("long".equals(type)) return LONG_VALUE
                if ("long".equals(lcType)) return LONG
                if ("locale".equals(lcType)) return LOCALE
                if ("lucee.runtime.type.Collection\$Key".equals(type)) return COLLECTION_KEY
            }
            'n' -> {
                if ("node".equals(lcType)) return NODE
                if ("number".equals(lcType)) return DOUBLE_VALUE
                if ("numeric".equals(lcType)) return DOUBLE_VALUE
            }
            'o' -> if ("object".equals(lcType)) return OBJECT
            'q' -> {
                if ("query".equals(lcType)) return QUERY
                if ("querycolumn".equals(lcType)) return QUERY_COLUMN
            }
            's' -> {
                if ("string".equals(lcType)) return STRING
                if ("struct".equals(lcType)) return STRUCT
                if ("short".equals(type)) return SHORT_VALUE
                if ("short".equals(lcType)) return SHORT
            }
            't' -> {
                if ("timezone".equals(lcType)) return TIMEZONE
                if ("timespan".equals(lcType)) return TIMESPAN
            }
            'u' -> if ("udf".equals(lcType)) return UDF
            'v' -> {
                if ("void".equals(lcType)) return VOID
                if ("variablestring".equals(lcType)) return STRING
                if ("variable_string".equals(lcType)) return STRING
            }
            'x' -> if ("xml".equals(lcType)) return NODE
            '[' -> if ("[Ljava.lang.String;".equals(lcType)) return STRING_ARRAY
        }
        // TODO Array als Lbyte und auch byte[]
        return try {
            Type.getType(ClassUtil.loadClass(type))
        } catch (e: ClassException) {
            throw TransformerException(bc, e, null)
        }
    }

    /**
     * returns if given type is a "primitve" type or in other words a value type (no reference type, no
     * object)
     *
     * @param type
     * @return
     */
    fun isPrimitiveType(type: Int): Boolean {
        return type != _OBJECT && type != _STRING
    }

    /**
     * returns if given type is a "primitve" type or in other words a value type (no reference type, no
     * object)
     *
     * @param type
     * @return
     */
    fun isPrimitiveType(type: Type?): Boolean {
        val className: String = type.getClassName()
        if (className.indexOf('.') !== -1) return false
        if ("boolean".equals(className)) return true
        if ("short".equals(className)) return true
        if ("float".equals(className)) return true
        if ("long".equals(className)) return true
        if ("double".equals(className)) return true
        if ("char".equals(className)) return true
        if ("int".equals(className)) return true
        return if ("byte".equals(className)) true else false
    }

    fun toRefType(type: Type?): Type? {
        val className: String = type.getClassName()
        if (className.indexOf('.') !== -1) return type
        if ("boolean".equals(className)) return BOOLEAN
        if ("short".equals(className)) return SHORT
        if ("float".equals(className)) return FLOAT
        if ("long".equals(className)) return LONG
        if ("double".equals(className)) return DOUBLE
        if ("char".equals(className)) return CHARACTER
        if ("int".equals(className)) return INT_VALUE
        return if ("byte".equals(className)) BYTE else type
    }

    @Throws(ClassException::class)
    fun toClass(type: Type?): Class? {
        if (STRING.equals(type)) return String::class.java
        if (BOOLEAN_VALUE.equals(type)) return Boolean::class.javaPrimitiveType
        if (DOUBLE_VALUE.equals(type)) return Double::class.javaPrimitiveType
        if (PAGE_CONTEXT.equals(type)) return PageContext::class.java
        if (OBJECT.equals(type)) return Object::class.java
        if (STRUCT.equals(type)) return Struct::class.java
        if (ARRAY.equals(type)) return Array::class.java
        if (COLLECTION_KEY.equals(type)) return Collection.Key::class.java
        if (COLLECTION_KEY_ARRAY.equals(type)) return Array<Collection.Key>::class.java
        if (QUERY.equals(type)) return Query::class.java
        if (DATE_TIME.equals(type)) return lucee.runtime.type.dt.DateTime::class.java
        if (TIMESPAN.equals(type)) return TimeSpan::class.java
        if (QUERY_COLUMN.equals(type)) return QueryColumn::class.java
        if (NODE.equals(type)) return Node::class.java
        if (TIMEZONE.equals(type)) return TimeZone::class.java
        if (LOCALE.equals(type)) return Locale::class.java
        if (UDF.equals(type)) return UDF::class.java
        /*
		 * if(Types.IMAGE.equals(type)) { Class clazz = ImageUtil.getImageClass(); if(clazz!=null) return
		 * clazz; throw new
		 * PageRuntimeException("Cannot provide Image class, you neeed to install the Image Extension to do so."
		 * ); }
		 */return if (BYTE_VALUE_ARRAY.equals(type)) ByteArray::class.java else ClassUtil.toClass(type.getClassName())
    }
}