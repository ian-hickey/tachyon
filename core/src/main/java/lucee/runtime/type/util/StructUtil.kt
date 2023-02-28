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
package lucee.runtime.type.util

import java.util.ArrayList

/**
 *
 */
object StructUtil {
    /**
     * copy data from source struct to target struct
     *
     * @param source
     * @param target
     * @param overwrite overwrite data if exist in target
     */
    fun copy(source: Struct?, target: Struct?, overwrite: Boolean) {
        val it: Iterator<Entry<Key?, Object?>?> = source.entryIterator()
        var e: Entry<Key?, Object?>?
        while (it.hasNext()) {
            e = it.next()
            if (overwrite || !target.containsKey(e.getKey())) target.setEL(e.getKey(), e.getValue())
        }
    }

    fun toCollectionKeys(skeys: Array<String?>?): Array<lucee.runtime.type.Collection.Key?>? {
        val keys: Array<lucee.runtime.type.Collection.Key?> = arrayOfNulls<lucee.runtime.type.Collection.Key?>(skeys!!.size)
        for (i in keys.indices) {
            keys[i] = KeyImpl.init(skeys!![i])
        }
        return keys
    }

    /**
     * @param sct
     * @return
     */
    fun duplicate(sct: Struct?, deepCopy: Boolean): Struct? {
        val rtn: Struct = StructImpl()
        // lucee.runtime.type.Collection.Key[] keys=sct.keys();
        // lucee.runtime.type.Collection.Key key;
        val it: Iterator<Entry<Key?, Object?>?> = sct.entryIterator()
        var e: Entry<Key?, Object?>?
        while (it.hasNext()) {
            e = it.next()
            rtn.setEL(e.getKey(), Duplicator.duplicate(e.getValue(), deepCopy))
        }
        return rtn
    }

    fun putAll(struct: Struct?, map: Map?) {
        val it: Iterator<Entry?> = map.entrySet().iterator()
        var entry: Map.Entry?
        while (it.hasNext()) {
            entry = it.next()
            struct.setEL(KeyImpl.toKey(entry.getKey(), null), entry.getValue())
        }
    }

    fun entrySet(sct: Struct?): Set<Entry<String?, Object?>?>? {
        val linked = sct is StructImpl && (sct as StructImpl?).getType() === Struct.TYPE_LINKED
        val it: Iterator<Entry<Key?, Object?>?> = sct.entryIterator()
        var e: Entry<Key?, Object?>?
        val set: Set<Entry<String?, Object?>?> = if (linked) LinkedHashSet<Entry<String?, Object?>?>() else HashSet<Entry<String?, Object?>?>()
        while (it.hasNext()) {
            e = it.next()
            set.add(StructMapEntry(sct, e.getKey(), e.getValue()))
        }
        return set
    }

    fun keySet(sct: Struct?): Set<String?>? {
        val linked = sct is StructSupport && (sct as StructSupport?).getType() === Struct.TYPE_LINKED
        val it: Iterator<Key?> = sct.keyIterator()
        val set: Set<String?> = if (linked) LinkedHashSet<String?>() else HashSet<String?>()
        while (it.hasNext()) {
            set.add(it.next().getString())
        }
        return set
    }

    fun toDumpTable(sct: Struct?, title: String?, pageContext: PageContext?, maxlevel: Int, dp: DumpProperties?): DumpTable? {
        var maxlevel = maxlevel
        var keys: Array<Key?>? = CollectionUtil.keys(sct)
        if (sct !is StructSupport || (sct as StructSupport?).getType() !== Struct.TYPE_LINKED) keys = order(sct, CollectionUtil.keys(sct))
        val table = DumpTable("struct", "#9999ff", "#ccccff", "#000000") // "#9999ff","#ccccff","#000000"
        val maxkeys: Int = dp.getMaxKeys()
        if (maxkeys < sct.size()) {
            table.setComment("Entries: " + sct.size().toString() + " (showing top " + maxkeys.toString() + ")")
        } else if (sct.size() > 10 && dp.getMetainfo()) {
            table.setComment("Entries: " + sct.size())
        }

        // advanced
        /*
		 * Map<Key, FunctionLibFunction> members = MemberUtil.getMembers(pageContext, CFTypes.TYPE_STRUCT);
		 * if(members!=null) { StringBuilder sb=new
		 * StringBuilder("This Struct is supporting the following Object functions:"); Iterator<Entry<Key,
		 * FunctionLibFunction>> it = members.entrySet().iterator(); Entry<Key, FunctionLibFunction> e;
		 * while(it.hasNext()){ e = it.next(); sb.append("\n	.") .append(e.getKey()) .append('(');
		 * 
		 * 
		 * ArrayList<FunctionLibFunctionArg> args = e.getValue().getArg(); int optionals = 0; for(int
		 * i=1;i<args.size();i++) { FunctionLibFunctionArg arg=args.get(i); if(i!=0)sb.append(", ");
		 * if(!arg.getRequired()) { sb.append("["); optionals++; } sb.append(arg.getName()); sb.append(":");
		 * sb.append(arg.getTypeAsString()); } for(int i=0;i<optionals;i++) sb.append("]");
		 * sb.append("):"+e.getValue().getReturnTypeAsString());
		 * 
		 * 
		 * } table.setComment(sb.toString()); }
		 */if (!StringUtil.isEmpty(title)) table.setTitle(title)
        maxlevel--
        var index = 0
        for (i in keys.indices) {
            if (DumpUtil.keyValid(dp, maxlevel, keys!![i])) {
                if (maxkeys <= index++) break
                table.appendRow(1, SimpleDumpData(keys[i].toString()), DumpUtil.toDumpData(sct.get(keys[i], null), pageContext, maxlevel, dp))
            }
        }
        return table
    }

    private fun order(sct: Struct?, keys: Array<Key?>?): Array<Key?>? {
        if (sct is StructImpl && (sct as StructImpl?).getType() === Struct.TYPE_LINKED) return keys
        val comp = TextComparator(true, true)
        Arrays.sort(keys, comp)
        return keys
    }

    /**
     * create a value return value out of a struct
     *
     * @param sct
     * @return
     */
    fun values(sct: Struct?): Collection<*>? {
        val arr: ArrayList<Object?> = ArrayList<Object?>()
        // Key[] keys = sct.keys();
        val it: Iterator<Object?> = sct.valueIterator()
        while (it.hasNext()) {
            arr.add(it.next())
        }
        return arr
    }

    @Throws(PageException::class)
    fun copyToStruct(map: Map?): Struct? {
        val sct: Struct = StructImpl()
        val it: Iterator = map.entrySet().iterator()
        var entry: Map.Entry
        while (it.hasNext()) {
            entry = it.next() as Entry
            sct.setEL(Caster.toString(entry.getKey()), entry.getValue())
        }
        return sct
    }

    fun setELIgnoreWhenNull(sct: Struct?, key: String?, value: Object?) {
        setELIgnoreWhenNull(sct, KeyImpl.init(key), value)
    }

    fun setELIgnoreWhenNull(sct: Struct?, key: Collection.Key?, value: Object?) {
        if (value != null) sct.setEL(key, value)
    }

    /**
     * remove every entry hat has this value
     *
     * @param map
     * @param obj
     */
    fun removeValue(map: Map?, value: Object?) {
        val it: Iterator = map.entrySet().iterator()
        var entry: Map.Entry
        while (it.hasNext()) {
            entry = it.next() as Entry
            if (entry.getValue() === value) it.remove()
        }
    }

    fun merge(scts: Array<Struct?>?): Struct? {
        val sct: Struct = StructImpl()
        for (i in scts.indices.reversed()) {
            val it: Iterator<Entry<Key?, Object?>?> = scts!![i].entryIterator()
            var e: Entry<Key?, Object?>?
            while (it.hasNext()) {
                e = it.next()
                sct.setEL(e.getKey(), e.getValue())
            }
        }
        return sct
    }

    fun getType(m: Map?): Int {
        if (m is LinkedHashMap) return Struct.TYPE_LINKED
        if (m is WeakHashMap) return Struct.TYPE_WEAKED
        if (m is ConcurrentHashMap) return Struct.TYPE_SYNC
        return if (m is ReferenceMap) Struct.TYPE_SOFT else Struct.TYPE_REGULAR
    }

    fun toType(type: Int, defaultValue: String?): String? {
        if (Struct.TYPE_LINKED === type) return "ordered"
        if (Struct.TYPE_WEAKED === type) return "weak"
        if (Struct.TYPE_REGULAR === type) return "regular"
        if (Struct.TYPE_SOFT === type) return "soft"
        return if (Struct.TYPE_SYNC === type) "synchronized" else defaultValue
    }

    /**
     * creates a hash based on the keys of the Map/Struct
     *
     * @param map
     * @return
     */
    fun keyHash(sct: Struct?): String? {
        var keys: Array<Key?>?
        Arrays.sort(CollectionUtil.keys(sct).also { keys = it })
        val sb = StringBuilder()
        for (i in keys.indices) {
            sb.append(keys!![i].getString()).append(';')
        }
        return toString(HashUtil.create64BitHash(sb), Character.MAX_RADIX)
    }

    @Throws(PageException::class)
    fun getMetaData(sct: Struct?): Struct? {
        val res: Struct = StructImpl()
        if (sct is StructImpl) {
            val type: Int = (sct as StructImpl?).getType()
            res.set(KeyConstants._type, toType(type, "unsynchronized"))
            res.set("ordered", if (type == Struct.TYPE_LINKED) "ordered" else "unordered")
        }
        return res
    }
}