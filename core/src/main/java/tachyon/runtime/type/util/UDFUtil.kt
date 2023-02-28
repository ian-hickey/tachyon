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
package tachyon.runtime.type.util

import java.util.ArrayList

object UDFUtil {
    const val TYPE_UDF: Short = 1
    const val TYPE_BIF: Short = 2
    const val TYPE_CLOSURE: Short = 4
    const val TYPE_LAMBDA: Short = 8
    private const val CACHE_DEL = ';'
    private const val CACHE_DEL2 = ':'
    private val EMPTY: Array<FunctionArgument?>? = arrayOfNulls<FunctionArgument?>(0)

    /**
     * add detailed function documentation to the exception
     *
     * @param pe
     * @param flf
     */
    fun addFunctionDoc(pe: PageExceptionImpl?, flf: FunctionLibFunction?) {
        val args: ArrayList<FunctionLibFunctionArg?> = flf.getArg()
        var it: Iterator<FunctionLibFunctionArg?> = args.iterator()

        // Pattern
        val pattern = StringBuilder(flf.getName())
        val end = StringBuilder()
        pattern.append("(")
        var arg: FunctionLibFunctionArg?
        var c = 0
        while (it.hasNext()) {
            arg = it.next()
            if (!arg.isRequired()) {
                pattern.append(" [")
                end.append("]")
            }
            if (c++ > 0) pattern.append(", ")
            pattern.append(arg.getName())
            pattern.append(":")
            pattern.append(arg.getTypeAsString())
        }
        pattern.append(end)
        pattern.append("):")
        pattern.append(flf.getReturnTypeAsString())
        pe.setAdditional(KeyConstants._Pattern, pattern)

        // Documentation
        val doc = StringBuilder(flf.getDescription())
        val req = StringBuilder()
        val opt = StringBuilder()
        var tmp: StringBuilder
        doc.append("\n")
        it = args.iterator()
        while (it.hasNext()) {
            arg = it.next()
            tmp = if (arg.isRequired()) req else opt
            tmp.append("- ")
            tmp.append(arg.getName())
            tmp.append(" (")
            tmp.append(arg.getTypeAsString())
            tmp.append("): ")
            tmp.append(arg.getDescription())
            tmp.append("\n")
        }
        if (req.length() > 0) doc.append("\nRequired:\n").append(req)
        if (opt.length() > 0) doc.append("\nOptional:\n").append(opt)
        pe.setAdditional(KeyConstants._Documentation, doc)
    }

    // used in extension axis
    fun argumentCollection(values: Struct?) {
        argumentCollection(values, EMPTY)
    }

    fun argumentCollection(values: Struct?, funcArgs: Array<FunctionArgument?>?) {
        var value: Object = values.removeEL(KeyConstants._argumentCollection)
        if (value != null) {
            value = Caster.unwrap(value, value)
            if (value is Argument) {
                val argColl: Argument = value as Argument
                val it: Iterator<Key?> = argColl.keyIterator()
                var k: Key?
                var i = -1
                while (it.hasNext()) {
                    i++
                    k = it.next()
                    if (funcArgs!!.size > i && k is ArgumentIntKey) {
                        if (!values.containsKey(funcArgs[i].getName())) values.setEL(funcArgs[i].getName(), argColl.get(k, Argument.NULL)) else values.setEL(k, argColl.get(k, Argument.NULL))
                    } else if (!values.containsKey(k)) {
                        values.setEL(k, argColl.get(k, Argument.NULL))
                    }
                }
            } else if (value is Collection) {
                val argColl: Collection = value
                // Collection.Key[] keys = argColl.keys();
                val it: Iterator<Key?> = argColl.keyIterator()
                var k: Key?
                while (it.hasNext()) {
                    k = it.next()
                    if (!values.containsKey(k)) {
                        values.setEL(k, argColl.get(k, Argument.NULL))
                    }
                }
            } else if (value is Map) {
                val it: Iterator = value.entrySet().iterator()
                var entry: Map.Entry
                var key: Key
                while (it.hasNext()) {
                    entry = it.next() as Entry
                    key = Caster.toKey(entry.getKey(), null)
                    if (!values.containsKey(key)) {
                        values.setEL(key, entry.getValue())
                    }
                }
            } else if (value is List<*>) {
                val list = value as List<*>
                val it: Iterator = list.iterator()
                var v: Object
                var index = 0
                var k: Key
                while (it.hasNext()) {
                    v = it.next()
                    k = ArgumentIntKey.init(++index)
                    if (!values.containsKey(k)) {
                        values.setEL(k, v)
                    }
                }
            } else {
                values.setEL(KeyConstants._argumentCollection, value)
            }
        }
    }

    fun toReturnFormat(returnFormat: Int, defaultValue: String?): String? {
        return if (UDF.RETURN_FORMAT_WDDX === returnFormat) "wddx" else if (UDF.RETURN_FORMAT_JSON === returnFormat) "json" else if (UDF.RETURN_FORMAT_PLAIN === returnFormat) "plain" else if (UDF.RETURN_FORMAT_SERIALIZE === returnFormat) "cfml" else if (UDF.RETURN_FORMAT_JAVA === returnFormat) "java" else defaultValue
    }

    fun isValidReturnFormat(returnFormat: Int): Boolean {
        return toReturnFormat(returnFormat, null) != null
    }

    fun toReturnFormat(returnFormats: Array<String?>?, defaultValue: Int): Int {
        if (ArrayUtil.isEmpty(returnFormats)) return defaultValue
        var rf: Int
        for (i in returnFormats.indices) {
            rf = toReturnFormat(returnFormats!![i].trim(), -1)
            if (rf != -1) return rf
        }
        return defaultValue
    }

    fun toReturnFormat(returnFormat: String?, defaultValue: Int): Int {
        var returnFormat = returnFormat
        if (StringUtil.isEmpty(returnFormat, true)) return defaultValue
        returnFormat = returnFormat.trim().toLowerCase()
        if ("wddx".equals(returnFormat)) return UDF.RETURN_FORMAT_WDDX else if ("json".equals(returnFormat)) return UDF.RETURN_FORMAT_JSON else if ("plain".equals(returnFormat)) return UDF.RETURN_FORMAT_PLAIN else if ("text".equals(returnFormat)) return UDF.RETURN_FORMAT_PLAIN else if ("serialize".equals(returnFormat)) return UDF.RETURN_FORMAT_SERIALIZE else if ("cfml".equals(returnFormat)) return UDF.RETURN_FORMAT_SERIALIZE else if ("cfm".equals(returnFormat)) return UDF.RETURN_FORMAT_SERIALIZE else if ("xml".equals(returnFormat)) return UDF.RETURN_FORMAT_XML else if ("java".equals(returnFormat)) return UDF.RETURN_FORMAT_JAVA
        return defaultValue
    }

    @Throws(ExpressionException::class)
    fun toReturnFormat(returnFormat: String?): Int {
        val rf = toReturnFormat(returnFormat, -1)
        if (rf != -1) return rf
        throw ExpressionException("Invalid returnFormat definition [$returnFormat], valid values are [wddx,plain,json,cfml]")
    }

    @Throws(ExpressionException::class)
    fun toReturnFormat(returnFormat: Int): String? {
        return if (UDF.RETURN_FORMAT_WDDX === returnFormat) "wddx" else if (UDF.RETURN_FORMAT_JSON === returnFormat) "json" else if (UDF.RETURN_FORMAT_PLAIN === returnFormat) "plain" else if (UDF.RETURN_FORMAT_SERIALIZE === returnFormat) "cfml" else if (UDF.RETURN_FORMAT_JAVA === returnFormat) "java" else throw ExpressionException("Invalid returnFormat definition, valid values are [wddx,plain,json,cfml]")
    }

    fun toDumpData(pageContext: PageContext?, maxlevel: Int, dp: DumpProperties?, udf: UDF?, type: Short): DumpData? {
        if (!dp.getShowUDFs()) {
            if (TYPE_UDF == type) return SimpleDumpData("<UDF>")
            if (TYPE_BIF == type) return SimpleDumpData("<BIF>")
            if (TYPE_CLOSURE == type) return SimpleDumpData("<Closure>")
            if (TYPE_LAMBDA == type) return SimpleDumpData("<Lambda>")
        }
        val isJavaFunction = udf is JF
        val jf: Class<*>? = getJavaFunction(udf)

        // arguments
        val args: Array<FunctionArgument?> = udf.getFunctionArguments()
        val atts: DumpTable?
        if (isJavaFunction) atts = DumpTable("udf", "#7aa7ce", "#e2eb8b", "#000000") else if (TYPE_UDF == type) atts = DumpTable("udf", "#ca5095", "#e9accc", "#000000") else if (TYPE_CLOSURE == type) atts = DumpTable("udf", "#9cb770", "#c7e1ba", "#000000") else if (TYPE_BIF == type) atts = DumpTable("udf", "#e1c039", "#f1e2a3", "#000000") else atts = DumpTable("udf", "#f3d5bd", "#f6e4cc", "#000000")
        atts.appendRow(DumpRow(63, if (isJavaFunction) arrayOf<DumpData?>(SimpleDumpData("label"), SimpleDumpData("name"), SimpleDumpData("required"), SimpleDumpData("type"), SimpleDumpData("hint")) else arrayOf<DumpData?>(SimpleDumpData("label"), SimpleDumpData("name"), SimpleDumpData("required"), SimpleDumpData("type"),
                SimpleDumpData("default"), SimpleDumpData("hint"))
        ))
        for (i in args.indices) {
            val arg: FunctionArgument? = args[i]
            var def: DumpData?
            try {
                var oa: Object = udf.getDefaultValue(pageContext, i, null)
                if (oa == null) oa = "null"
                def = SimpleDumpData(Caster.toString(oa))
            } catch (e: PageException) {
                def = SimpleDumpData("")
            }
            if (isJavaFunction) atts.appendRow(DumpRow(0, arrayOf<DumpData?>(SimpleDumpData(arg.getDisplayName()), SimpleDumpData(arg.getName().getString()),
                    SimpleDumpData(arg.isRequired()), SimpleDumpData(arg.getTypeAsString()), SimpleDumpData(arg.getHint())))) else atts.appendRow(DumpRow(0, arrayOf<DumpData?>(SimpleDumpData(arg.getDisplayName()), SimpleDumpData(arg.getName().getString()),
                    SimpleDumpData(arg.isRequired()), SimpleDumpData(arg.getTypeAsString()), def, SimpleDumpData(arg.getHint()))))
            // atts.setRow(0,arg.getHint());
        }
        val func: DumpTable?
        val label: String = udf.getDisplayName()
        if (TYPE_CLOSURE == type) {
            func = DumpTable("#9cb770", "#c7e1ba", "#000000")
            func.setTitle((if (isJavaFunction) "Java " else "") + if (StringUtil.isEmpty(label)) "Closure" else "Closure $label")
        } else if (TYPE_UDF == type) {
            func = if (isJavaFunction) DumpTable("#7aa7ce", "#e2eb8b", "#000000") else DumpTable("#ca5095", "#e9accc", "#000000")
            var f = if (isJavaFunction) "Java Function " else "Function "
            try {
                f = StringUtil.ucFirst(ComponentUtil.toStringAccess(udf.getAccess()).toLowerCase()).toString() + " " + f
            } catch (e: ApplicationException) {
            }
            f += udf.getFunctionName()
            if (udf is UDFGSProperty) f += " (generated)"
            func.setTitle(f)
        } else if (TYPE_BIF == type) {
            val f = "Built in Function " + if (!StringUtil.isEmpty(label)) label else udf.getFunctionName()
            func = DumpTable("#e1c039", "#f1e2a3", "#000000")
            func.setTitle(f)
        } else {
            func = DumpTable("#f3d5bd", "#f6e4cc", "#000000")
            func.setTitle(if (StringUtil.isEmpty(label)) "Lambda" else "Lambda $label")
        }

        // Source
        val src: String = udf.getSource()
        if (!StringUtil.isEmpty(src)) func.setComment("source: $src")
        if (jf != null) func.setComment("implements: " + jf.getName())
        val hint: String = udf.getHint()
        val desc: String = udf.getDescription()
        if (!StringUtil.isEmpty(desc)) addComment(func, desc)
        if (!StringUtil.isEmpty(hint)) addComment(func, hint)
        if (Component.MODIFIER_NONE !== udf.getModifier()) func.appendRow(1, SimpleDumpData("modifier"), SimpleDumpData(ComponentUtil.toModifier(udf.getModifier(), "")))
        func.appendRow(1, SimpleDumpData("arguments"), atts)
        func.appendRow(1, SimpleDumpData("return type"), SimpleDumpData(udf.getReturnTypeAsString()))
        return func
    }

    private fun getJavaFunction(udf: UDF?): Class<*>? {
        if (udf is UDFImpl) return null
        if (udf is BiConsumer) return BiConsumer::class.java
        if (udf is BiFunction) return BiFunction::class.java
        if (udf is BiPredicate) return BiPredicate::class.java
        if (udf is BooleanSupplier) return BooleanSupplier::class.java
        if (udf is Consumer) return Consumer::class.java
        if (udf is DoubleBinaryOperator) return DoubleBinaryOperator::class.java
        if (udf is DoubleConsumer) return DoubleConsumer::class.java
        if (udf is DoubleFunction) return DoubleFunction::class.java
        if (udf is DoublePredicate) return DoublePredicate::class.java
        if (udf is DoubleSupplier) return DoubleSupplier::class.java
        if (udf is DoubleToIntFunction) return DoubleToIntFunction::class.java
        if (udf is DoubleToLongFunction) return DoubleToLongFunction::class.java
        if (udf is DoubleUnaryOperator) return DoubleUnaryOperator::class.java
        if (udf is Function) return Function::class.java
        if (udf is IntBinaryOperator) return IntBinaryOperator::class.java
        if (udf is IntConsumer) return IntConsumer::class.java
        if (udf is IntFunction) return IntFunction::class.java
        if (udf is IntPredicate) return IntPredicate::class.java
        if (udf is IntSupplier) return IntSupplier::class.java
        if (udf is IntToDoubleFunction) return IntToDoubleFunction::class.java
        if (udf is IntToLongFunction) return IntToLongFunction::class.java
        if (udf is IntUnaryOperator) return IntUnaryOperator::class.java
        if (udf is LongBinaryOperator) return LongBinaryOperator::class.java
        if (udf is LongConsumer) return LongConsumer::class.java
        if (udf is LongFunction) return LongFunction::class.java
        if (udf is LongPredicate) return LongPredicate::class.java
        if (udf is LongSupplier) return LongSupplier::class.java
        if (udf is LongToDoubleFunction) return LongToDoubleFunction::class.java
        if (udf is LongToIntFunction) return LongToIntFunction::class.java
        if (udf is LongUnaryOperator) return LongUnaryOperator::class.java
        if (udf is ObjDoubleConsumer) return ObjDoubleConsumer::class.java
        if (udf is ObjIntConsumer) return ObjIntConsumer::class.java
        if (udf is ObjLongConsumer) return ObjLongConsumer::class.java
        if (udf is Predicate) return Predicate::class.java
        if (udf is Supplier) return Supplier::class.java
        if (udf is ToDoubleBiFunction) return ToDoubleBiFunction::class.java
        if (udf is ToDoubleFunction) return ToDoubleFunction::class.java
        if (udf is ToIntBiFunction) return ToIntBiFunction::class.java
        if (udf is ToIntFunction) return ToIntFunction::class.java
        if (udf is ToLongBiFunction) return ToLongBiFunction::class.java
        return if (udf is ToLongFunction) ToLongFunction::class.java else null
    }

    private fun addComment(dt: DumpTable?, comment: String?) {
        if (StringUtil.isEmpty(dt.getComment()) || dt.getComment().indexOf(comment) !== -1) dt.setComment(comment) else dt.setComment(dt.getComment().toString() + "\n" + comment)
    }
}