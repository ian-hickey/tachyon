/**
 * Copyright (c) 2023, TachyonCFML.org
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
package tachyon.runtime.type.scope

import java.util.ArrayList

/**
 * implementation of the argument scope
 */
class ArgumentImpl  // private boolean supportFunctionArguments;
/**
 * constructor of the class
 */
    : ScopeSupport("arguments", SCOPE_ARGUMENTS, Struct.TYPE_LINKED), Argument, ArrayPro {
    private var bind = false
    private var functionArgumentNames: Set? = null

    @Override
    override fun release(pc: PageContext?) {
        functionArgumentNames = null
        super.release(ThreadLocalPageContext.get(pc))
    }

    @Override
    fun setBind(bind: Boolean) {
        this.bind = bind
    }

    @Override
    fun isBind(): Boolean {
        return bind
    }

    @Override
    fun getFunctionArgument(key: String?, defaultValue: Object?): Object? {
        return getFunctionArgument(KeyImpl.getInstance(key), defaultValue)
    }

    @Override
    fun getFunctionArgument(key: Collection.Key?, defaultValue: Object?): Object? {
        return super.get(key, defaultValue)
    }

    @Override
    fun containsFunctionArgumentKey(key: Key?): Boolean {
        return super.containsKey(key) // functionArgumentNames!=null && functionArgumentNames.contains(key);
    }

    @Override
    operator fun get(key: Collection.Key?, defaultValue: Object?): Object? {
        /*
		 * if(NullSupportHelper.full()) { Object o=super.get(key,NullSupportHelper.NULL());
		 * if(o!=NullSupportHelper.NULL())return o;
		 * 
		 * o=get(Caster.toIntValue(key.getString(),-1),NullSupportHelper.NULL());
		 * if(o!=NullSupportHelper.NULL())return o; return defaultValue; }
		 */
        var o: Object? = super.g(key, Null.NULL)
        if (o !== Null.NULL) return o
        if (key.length() > 0) {
            val c: Char = key.charAt(0)
            if (c >= '0' && c <= '9' || c == '+') {
                o = get(Caster.toIntValue(key.getString(), -1), Null.NULL)
                if (o !== Null.NULL) return o
            }
        }
        return defaultValue
    }

    @Override
    @Throws(ExpressionException::class)
    operator fun get(key: Collection.Key?): Object? {
        // null is supported as returned value with argument scope
        var o: Object? = super.g(key, Null.NULL)
        if (o !== Null.NULL) return o
        if (key.length() > 0) {
            val c: Char = key.charAt(0)
            if (c >= '0' && c <= '9' || c == '+') {
                o = get(Caster.toIntValue(key.getString(), -1), Null.NULL)
                if (o !== Null.NULL) return o
            }
        }
        throw ExpressionException("The key [" + key.getString().toString() + "] doesn't exist in the arguments scope. The existing keys are ["
                + tachyon.runtime.type.util.ListUtil.arrayToList(CollectionUtil.keys(this), ", ").toString() + "]")
    }

    @Override
    operator fun get(intKey: Int, defaultValue: Object?): Object? {
        val it: Iterator<Object?> = valueIterator() // keyIterator();//getMap().keySet().iterator();
        var count = 0
        var o: Object?
        while (it.hasNext()) {
            o = it.next()
            if (++count == intKey) {
                return o // super.get(o.toString(),defaultValue);
            }
        }
        return defaultValue
    }

    /**
     * return a value matching to key
     *
     * @param intKey
     * @return value matching key
     * @throws PageException
     */
    @Override
    @Throws(PageException::class)
    fun getE(intKey: Int): Object? {
        val it: Iterator = valueIterator() // getMap().keySet().iterator();
        var count = 0
        var o: Object
        while (it.hasNext()) {
            o = it.next()
            if (++count == intKey) {
                return o // super.get(o.toString());
            }
        }
        throw ExpressionException("invalid index [$intKey] for argument scope")
    }

    @Override
    override fun toDumpData(pageContext: PageContext?, maxlevel: Int, dp: DumpProperties?): DumpData? {
        var maxlevel = maxlevel
        val htmlBox = DumpTable("struct", "#9999ff", "#ccccff", "#000000")
        htmlBox.setTitle("Scope Arguments")
        if (size() > 10 && dp.getMetainfo()) htmlBox.setComment("Entries:" + size())
        maxlevel--
        // Map mapx=getMap();
        val it: Iterator<Key?> = keyIterator() // mapx.keySet().iterator();
        var count = 0
        var key: Collection.Key?
        val maxkeys: Int = dp.getMaxKeys()
        var index = 0
        while (it.hasNext()) {
            key = it.next() // it.next();
            if (DumpUtil.keyValid(dp, maxlevel, key)) {
                if (maxkeys <= index++) break
                htmlBox.appendRow(3, SimpleDumpData(key.getString()), SimpleDumpData(++count), DumpUtil.toDumpData(get(key, null), pageContext, maxlevel, dp))
            }
        }
        return htmlBox
    }

    @Override
    fun getDimension(): Int {
        return 1
    }

    @Override
    fun setEL(intKey: Int, value: Object?): Object? {
        var count = 0
        if (intKey > size()) {
            return setEL(Caster.toString(intKey), value)
        }
        // Iterator it = keyIterator();
        val keys: Array<Key?> = keys()
        for (i in keys.indices) {
            if (++count == intKey) {
                return super.setEL(keys[i], value)
            }
        }
        return value
    }

    @Override
    @Throws(PageException::class)
    fun setE(intKey: Int, value: Object?): Object? {
        if (intKey > size()) {
            return set(Caster.toString(intKey), value)
        }
        // Iterator it = keyIterator();
        val keys: Array<Key?> = keys()
        for (i in keys.indices) {
            if (i + 1 == intKey) {
                return super.set(keys[i], value)
            }
        }
        throw ExpressionException("invalid index [$intKey] for argument scope")
    }

    @Override
    fun intKeys(): IntArray? {
        val ints = IntArray(size())
        for (i in ints.indices) ints[i] = i + 1
        return ints
    }

    @Override
    @Throws(ExpressionException::class)
    fun insert(index: Int, value: Object?): Boolean {
        return insert(index, "" + index, value)
    }

    @Override
    @Throws(ExpressionException::class)
    fun insert(index: Int, key: String?, value: Object?): Boolean {
        val len: Int = size()
        if (index < 1 || index > len) throw ExpressionException("invalid index to insert a value to argument scope",
                if (len == 0) "can't insert in an empty argument scope" else "valid index goes from 1 to " + (len - 1))

        // remove all upper
        val lhm = LinkedHashMap()
        val keys: Array<Collection.Key?> = keys()
        var k: Collection.Key?
        for (i in 1..keys.size) {
            if (i < index) continue
            k = keys[i - 1]
            lhm.put(k.getString(), get(k, null))
            removeEL(k)
        }

        // set new value
        setEL(key, value)

        // reset upper values
        val it: Iterator = lhm.entrySet().iterator()
        var entry: Map.Entry
        while (it.hasNext()) {
            entry = it.next() as Entry
            setEL(KeyImpl.toKey(entry.getKey()), entry.getValue())
        }
        return true
    }

    @Override
    @Throws(PageException::class)
    fun append(o: Object?): Object? {
        return set(Caster.toString(size() + 1), o)
    }

    @Override
    fun appendEL(o: Object?): Object? {
        return try {
            append(o)
        } catch (e: PageException) {
            null
        }
    }

    @Override
    @Throws(PageException::class)
    fun prepend(o: Object?): Object? {
        for (i in size() downTo 1) {
            setE(i + 1, getE(i))
        }
        setE(1, o)
        return o
    }

    @Override
    @Throws(PageException::class)
    fun resize(to: Int) {
        for (i in size() until to) {
            append(null)
        }
        // throw new ExpressionException("can't resize this array");
    }

    @Override
    @Throws(ExpressionException::class)
    fun sort(sortType: String?, sortOrder: String?) {
        // TODO Impl.
        throw ExpressionException("can't sort [$sortType-$sortOrder] Argument Scope", "not Implemnted Yet")
    }

    @Override
    fun sortIt(com: Comparator?) {
        // TODO Impl.
        throw PageRuntimeException("can't sort Argument Scope", "not Implemnted Yet")
    }

    @Override
    fun toArray(): Array<Object?>? {
        val it: Iterator = keyIterator() // getMap().keySet().iterator();
        val arr: Array<Object?> = arrayOfNulls<Object?>(size())
        var count = 0
        while (it.hasNext()) {
            arr[count++] = it.next()
        }
        return arr
    }

    @Override
    @Throws(PageException::class)
    fun setArgument(obj: Object?): Object? {
        if (obj === this) return obj
        if (Decision.isStruct(obj)) {
            clear() // TODO bessere impl. anstelle vererbung wrao auf struct
            val sct: Struct = Caster.toStruct(obj)
            val it: Iterator<Key?> = sct.keyIterator()
            var key: Key?
            while (it.hasNext()) {
                key = it.next()
                setEL(key, sct.get(key, null))
            }
            return obj
        }
        throw ExpressionException("can not overwrite arguments scope")
    }

    fun toArrayList(): ArrayList<Object?>? {
        val list: ArrayList<Object?> = ArrayList<Object?>()
        val arr: Array<Object?>? = toArray()
        for (i in arr.indices) {
            list.add(arr!![i])
        }
        return list
    }

    @Override
    @Throws(PageException::class)
    fun removeE(intKey: Int): Object? {
        val keys: Array<Key?> = keys()
        for (i in keys.indices) {
            if (i + 1 == intKey) {
                return super.remove(keys[i])
            }
        }
        throw ExpressionException("can't remove argument number [$intKey], argument doesn't exist")
    }

    @Override
    fun removeEL(intKey: Int): Object? {
        return remove(intKey, null)
    }

    fun remove(intKey: Int, defaultValue: Object?): Object? {
        val keys: Array<Key?> = keys()
        for (i in keys.indices) {
            if (i + 1 == intKey) {
                return super.removeEL(keys[i])
            }
        }
        return defaultValue
    }

    @Override
    @Throws(PageException::class)
    fun pop(): Object? {
        return removeE(size())
    }

    @Override
    @Synchronized
    fun pop(defaultValue: Object?): Object? {
        return remove(size(), defaultValue)
    }

    @Override
    @Throws(PageException::class)
    fun shift(): Object? {
        return removeE(1)
    }

    @Override
    @Synchronized
    fun shift(defaultValue: Object?): Object? {
        return remove(1, defaultValue)
    }

    @Override
    fun containsKey(key: Collection.Key?): Boolean {
        val `val`: Object = super.g(key, CollectionUtil.NULL)
        if (`val` === CollectionUtil.NULL) return false
        return if (`val` == null && !NullSupportHelper.full()) false else true
    }

    @Override
    fun containsKey(pc: PageContext?, key: Collection.Key?): Boolean {
        val `val`: Object = super.g(key, CollectionUtil.NULL)
        if (`val` === CollectionUtil.NULL) return false
        return if (`val` == null && !NullSupportHelper.full(pc)) false else true
    }

    /*
	 * public boolean containsKey(Collection.Key key) { return get(key,null)!=null &&
	 * super.containsKey(key); }
	 */
    @Override
    fun containsKey(key: Int): Boolean {
        return key > 0 && key <= size()
    }

    @Override
    fun toList(): List? {
        return ArrayAsList.toList(this)
    }

    @Override
    fun duplicate(deepCopy: Boolean): Collection? {
        val trg = ArgumentImpl()
        trg.bind = false
        trg.functionArgumentNames = functionArgumentNames
        // trg.supportFunctionArguments=supportFunctionArguments;
        copy(this, trg, deepCopy)
        return trg
    }

    @Override
    fun setFunctionArgumentNames(functionArgumentNames: Set?) { // future add to interface
        this.functionArgumentNames = functionArgumentNames
    }

    @Override
    operator fun get(pc: PageContext?, key: Key?, defaultValue: Object?): Object? {
        return get(key, defaultValue)
    }

    @Override
    @Throws(PageException::class)
    operator fun get(pc: PageContext?, key: Key?): Object? {
        return get(key)
    }

    @Override
    @Throws(PageException::class)
    operator fun set(pc: PageContext?, propertyName: Key?, value: Object?): Object? {
        return set(propertyName, value)
    }

    @Override
    fun setEL(pc: PageContext?, propertyName: Key?, value: Object?): Object? {
        return setEL(propertyName, value)
    }

    @Override
    @Throws(PageException::class)
    fun call(pc: PageContext?, methodName: Key?, args: Array<Object?>?): Object? {
        val obj: Object = get(methodName, null)
        return if (obj is UDF) {
            (obj as UDF).call(pc, methodName, args, false)
        } else MemberUtil.call(pc, this, methodName, args, shortArrayOf(CFTypes.TYPE_STRUCT), arrayOf<String?>("struct"))
        // return MemberUtil.call(pc, this, methodName, args, CFTypes.TYPE_ARRAY, "array");
    }

    @Override
    @Throws(PageException::class)
    fun callWithNamedValues(pc: PageContext?, methodName: Key?, args: Struct?): Object? {
        val obj: Object = get(methodName, null)
        return if (obj is UDF) {
            (obj as UDF).callWithNamedValues(pc, methodName, args, false)
        } else MemberUtil.callWithNamedValues(pc, this, methodName, args, CFTypes.TYPE_STRUCT, "struct")
        // return MemberUtil.callWithNamedValues(pc,this,methodName,args, CFTypes.TYPE_ARRAY, "array");
    }

    @Override
    fun entryArrayIterator(): Iterator<Entry<Integer?, Object?>?>? {
        return EntryArrayIterator(this, intKeys())
    }

    companion object {
        private const val serialVersionUID = 4346997451403177136L
        /*
	 * public void setNamedArguments(boolean namedArguments) { this.namedArguments=namedArguments; }
	 * public boolean isNamedArguments() { return namedArguments; }
	 */
        /**
         * converts an argument scope to a regular struct
         *
         * @param arg argument scope to convert
         * @return resulting struct
         */
        fun toStruct(arg: Argument?): Struct? {
            val trg: Struct = StructImpl()
            StructImpl.copy(arg, trg, false)
            return trg
        }

        /**
         * converts an argument scope to a regular array
         *
         * @param arg argument scope to convert
         * @return resulting array
         */
        fun toArray(arg: Argument?): Array? {
            val trg = ArrayImpl()
            val keys: IntArray = arg.intKeys()
            for (i in keys.indices) {
                trg.setEL(keys[i], arg.get(keys[i], null))
            }
            return trg
        }
    }
}