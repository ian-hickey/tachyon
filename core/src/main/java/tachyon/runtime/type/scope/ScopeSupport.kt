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
package tachyon.runtime.type.scope

import java.io.UnsupportedEncodingException

/**
 * Simple implementation of a Scope, for general use.
 */
abstract class ScopeSupport(private val name: String?, private val type: Int, mapType: Int) : StructImpl(mapType), Scope {
    private var dspName: String? = null
    private var id = 0

    /**
     * Field `isInit`
     */
    protected var isInit = false

    /**
     * constructor for the Simple class
     *
     * @param name name of the scope
     * @param type scope type (SCOPE_APPLICATION,SCOPE_COOKIE use)
     */
    private constructor(name: String?, type: Int) : this(name, type, Struct.TYPE_LINKED) {}

    @Override
    fun toDumpData(pageContext: PageContext?, maxlevel: Int, dp: DumpProperties?): DumpData? {
        return toDumpData(pageContext, maxlevel, dp, this, dspName)
    }

    protected fun invalidKey(key: String?): ExpressionException? {
        return ExpressionException("variable [" + key + "] doesn't exist in " + StringUtil.ucFirst(name) + " Scope (keys:" + ListUtil.arrayToList(keys(), ",") + ")")
    }

    protected fun fillDecodedEL(raw: Array<URLItem?>?, encoding: String?, scriptProteced: Boolean, sameAsArray: Boolean) {
        try {
            fillDecoded(raw, encoding, scriptProteced, sameAsArray)
        } catch (e: UnsupportedEncodingException) {
            try {
                fillDecoded(raw, "iso-8859-1", scriptProteced, sameAsArray)
            } catch (e1: UnsupportedEncodingException) {
            }
        }
    }

    /**
     * fill th data from given strut and decode it
     *
     * @param raw
     * @param encoding
     * @throws UnsupportedEncodingException
     */
    @Throws(UnsupportedEncodingException::class)
    protected fun fillDecoded(raw: Array<URLItem?>?, encoding: String?, scriptProteced: Boolean, sameAsArray: Boolean) {
        clear()
        var name: String
        var value: String
        // Object curr;
        for (i in raw.indices) {
            name = raw!![i].getName()
            value = raw[i].getValue()
            if (raw[i].isUrlEncoded()) {
                name = URLDecoder.decode(name, encoding, true)
                value = URLDecoder.decode(value, encoding, true)
            }
            // MUST valueStruct
            if (name.indexOf('.') !== -1) {
                val list: StringList = ListUtil.listToStringListRemoveEmpty(name, '.')
                if (list.size() > 0) {
                    var parent: Struct? = this
                    while (list.hasNextNext()) {
                        parent = _fill(parent, list.next(), CastableStruct(Struct.TYPE_LINKED), false, scriptProteced, sameAsArray)
                    }
                    _fill(parent, list.next(), value, true, scriptProteced, sameAsArray)
                }
            }
            // else
            _fill(this, name, value, true, scriptProteced, sameAsArray)
        }
    }

    private fun _fill(parent: Struct?, name: String?, value: Object?, isLast: Boolean, scriptProteced: Boolean, sameAsArray: Boolean): Struct? {
        var name = name
        var value: Object? = value
        val curr: Object
        var isArrayDef = false
        var key: Collection.Key = KeyImpl.init(name)

        // script protect
        if (scriptProteced && value is String) {
            value = ScriptProtect.translate(value as String?)
        }
        if (name!!.length() > 2 && name.endsWith("[]")) {
            isArrayDef = true
            name = name.substring(0, name.length() - 2)
            key = KeyImpl.getInstance(name)
            curr = parent.get(key, null)
        } else {
            curr = parent.get(key, null)
        }
        if (curr == null) {
            if (isArrayDef) {
                val arr: Array = ArrayImpl()
                arr.appendEL(value)
                parent.setEL(key, arr)
            } else parent.setEL(key, value)
        } else if (curr is Array) {
            curr.appendEL(value)
        } else if (curr is CastableStruct) {
            if (isLast) (curr as CastableStruct).setValue(value) else return curr as Struct
        } else if (curr is Struct) {
            if (isLast) parent.setEL(key, value) else return curr as Struct
        } else if (curr is String) {
            if (isArrayDef) {
                val arr: Array = ArrayImpl()
                arr.appendEL(curr)
                arr.appendEL(value)
                parent.setEL(key, arr)
            } else if (value is Struct) {
                parent.setEL(key, value)
            } else {
                if (sameAsArray) {
                    val arr: Array = ArrayImpl()
                    arr.appendEL(curr)
                    arr.appendEL(value)
                    parent.setEL(key, arr)
                } else {
                    val c: String = Caster.toString(curr, "")
                    val v: String = Caster.toString(value, "")
                    if (StringUtil.isEmpty(c)) {
                        parent.setEL(key, v)
                    } else if (!StringUtil.isEmpty(v)) {
                        parent.setEL(key, "$c,$v")
                    }
                }
            }
        }
        return if (!isLast) {
            value as Struct?
        } else null
    }

    /*
	 * private String decode(Object value,String encoding) throws UnsupportedEncodingException { return
	 * URLDecoder.decode(new
	 * String(Caster.toString(value,"").getBytes("ISO-8859-1"),encoding),encoding); }
	 */
    @Override
    fun isInitalized(): Boolean {
        return isInit
    }

    @Override
    fun initialize(pc: PageContext?) {
        isInit = true
    }

    @Override
    fun release(pc: PageContext?) {
        clear()
        isInit = false
    }

    /**
     * @return Returns the id.
     */
    fun _getId(): Int {
        return id
    }

    /**
     * display name for dump
     *
     * @param dspName
     */
    protected fun setDisplayName(dspName: String?) {
        this.dspName = dspName
    }

    @Override
    fun getType(): Int {
        return type
    }

    @Override
    fun getTypeAsString(): String? {
        return name
    }

    companion object {
        private const val serialVersionUID = -4185219623238374574L
        private var _id = 0
        private val EMPTY: ByteArray? = "".getBytes()
        fun toDumpData(pageContext: PageContext?, maxlevel: Int, dp: DumpProperties?, sct: Struct?, dspName: String?): DumpData? {
            var dspName = dspName
            if (StringUtil.isEmpty(dspName)) dspName = "Scope"
            return StructUtil.toDumpTable(sct, dspName, pageContext, maxlevel, dp)
        }

        /**
         * write parameter defined in a query string (name1=value1&name2=value2) to the scope
         *
         * @param str Query String
         * @return parsed name value pair
         */
        protected fun setFromQueryString(str: String?): Array<URLItem?>? {
            return setFrom___(str, '&')
        }

        protected fun setFromTextPlain(str: String?): Array<URLItem?>? {
            return setFrom___(str, '\n')
        }

        protected fun setFrom___(tp: String?, delimiter: Char): Array<URLItem?>? {
            if (tp == null) return arrayOfNulls<URLItem?>(0)
            val arr: Array = ListUtil.listToArrayRemoveEmpty(tp, delimiter)
            val pairs: Array<URLItem?> = arrayOfNulls<URLItem?>(arr.size())

            // Array item;
            var index: Int
            var name: String
            for (i in 1..pairs.size) {
                name = Caster.toString(arr.get(i, ""), "")
                // if(name.length()==0) continue;
                index = name.indexOf('=')
                if (index != -1) pairs[i - 1] = URLItem(name.substring(0, index), name.substring(index + 1), true) else pairs[i - 1] = URLItem(name, "", true)
            }
            return pairs
        }

        protected fun getBytes(str: String?): ByteArray? {
            return str.getBytes()
        }

        protected fun getBytes(str: String?, encoding: String?): ByteArray? {
            return try {
                str.getBytes(encoding)
            } catch (e: UnsupportedEncodingException) {
                EMPTY
            }
        }
    }

    /**
     * constructor for ScopeSupport
     *
     * @param name name of the scope
     * @param type scope type (SCOPE_APPLICATION,SCOPE_COOKIE use)
     * @param mapType mean that the struct has predictable iteration order this make the input order fix
     */
    init {
        id = ++_id
    }
}