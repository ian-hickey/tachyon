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
package lucee.runtime.com

import java.util.ArrayList

/**
 *
 */
class COMObject : Objects, Iteratorable {
    /**
     * @return Returns the name.
     */
    var name: String?
        private set
    private var dispatch: Dispatch?
    private var parent: Variant? = null

    /**
     * Public Constructor of the class
     *
     * @param dispatch
     * @throws ExpressionException
     */
    constructor(dispatch: String?) {
        // if(!SystemUtil.isWindows()) throw new ExpressionException("Com Objects are only supported in
        // Windows Environments");
        name = dispatch
        this.dispatch = Dispatch(dispatch)
    }

    /**
     * Private Constructor of the class for sub Objects
     *
     * @param parent
     * @param dispatch
     * @param name
     */
    internal constructor(parent: Variant?, dispatch: Dispatch?, name: String?) {
        this.parent = parent
        this.name = name
        this.dispatch = dispatch
    }

    /*
	 * public Object get(PageContext pc, String propertyName) throws PageException { return
	 * COMUtil.toObject(this,Dispatch.call(dispatch,propertyName),propertyName); }
	 */
    @Override
    @Throws(PageException::class)
    operator fun get(pc: PageContext?, key: Collection.Key?): Object? {
        return COMUtil.toObject(this, Dispatch.call(dispatch, key.getString()), key.getString())
    }

    /*
	 * public Object get(PageContext pc, String propertyName, Object defaultValue) { return
	 * COMUtil.toObject(this,Dispatch.call(dispatch,propertyName),propertyName,defaultValue); }
	 */
    @Override
    operator fun get(pc: PageContext?, key: Collection.Key?, defaultValue: Object?): Object? {
        return COMUtil.toObject(this, Dispatch.call(dispatch, key.getString()), key.getString(), defaultValue)
    }

    /*
	 * public Object set(PageContext pc, String propertyName, Object value) { return
	 * setEL(pc,propertyName,value); }
	 */
    @Override
    @Throws(PageException::class)
    operator fun set(pc: PageContext?, propertyName: Collection.Key?, value: Object?): Object? {
        Dispatch.put(dispatch, propertyName.getString(), value)
        return value
    }

    /*
	 * public Object setEL(PageContext pc, String propertyName, Object value) {
	 * Dispatch.put(dispatch,propertyName,value); return value; }
	 */
    @Override
    fun setEL(pc: PageContext?, propertyName: Collection.Key?, value: Object?): Object? {
        Dispatch.put(dispatch, propertyName.getString(), value)
        return value
    }

    /*
	 * public Object call(PageContext pc, String methodName, Object[] args) throws PageException {
	 * Object[] arr=new Object[args.length]; for(int i=0;i<args.length;i++) { if(args[i] instanceof
	 * COMObject)arr[i]=((COMObject)args[i]).dispatch; else arr[i]=args[i]; } return
	 * COMUtil.toObject(this,Dispatch.callN(dispatch,methodName,arr),methodName); }
	 */
    @Override
    @Throws(PageException::class)
    fun call(pc: PageContext?, key: Collection.Key?, args: Array<Object?>?): Object? {
        val methodName: String = key.getString()
        val arr: Array<Object?> = arrayOfNulls<Object?>(args!!.size)
        for (i in args.indices) {
            if (args!![i] is COMObject) arr[i] = (args[i] as COMObject?)!!.dispatch else arr[i] = args[i]
        }
        return COMUtil.toObject(this, Dispatch.callN(dispatch, methodName, arr), methodName)
    }

    /*
	 * public Object callWithNamedValues(PageContext pc, String methodName, Struct args) throws
	 * PageException { // TODO gibt es hier eine bessere moeglichkeit? Iterator<Object> it =
	 * args.valueIterator(); List<Object> values=new ArrayList<Object>(); while(it.hasNext()) {
	 * values.add(it.next()); } return call(pc,KeyImpl.init(methodName),values.toArray(new
	 * Object[values.size()])); }
	 */
    @Override
    @Throws(PageException::class)
    fun callWithNamedValues(pc: PageContext?, key: Collection.Key?, args: Struct?): Object? {
        val methodName: String = key.getString()
        val it: Iterator<Object?> = args.valueIterator()
        val values: List<Object?> = ArrayList<Object?>()
        while (it.hasNext()) {
            values.add(it.next())
        }
        return call(pc, KeyImpl.init(methodName), values.toArray(arrayOfNulls<Object?>(values.size())))
    }

    val isInitalized: Boolean
        get() = true

    @Override
    fun toDumpData(pageContext: PageContext?, maxlevel: Int, dp: DumpProperties?): DumpData? {
        val table = DumpTable("com", "#ff3300", "#ff9966", "#660000")
        table.appendRow(1, SimpleDumpData("COM Object"), SimpleDumpData(name))
        return table
    }

    @Override
    @Throws(ExpressionException::class)
    fun castToString(): String? {
        throw ExpressionException("can't cast Com Object to a String")
    }

    @Override
    fun castToString(defaultValue: String?): String? {
        return defaultValue
    }

    @Override
    @Throws(ExpressionException::class)
    fun castToBooleanValue(): Boolean {
        throw ExpressionException("can't cast Com Object to a boolean value")
    }

    @Override
    fun castToBoolean(defaultValue: Boolean?): Boolean? {
        return defaultValue
    }

    @Override
    @Throws(ExpressionException::class)
    fun castToDoubleValue(): Double {
        throw ExpressionException("can't cast Com Object to a number")
    }

    @Override
    fun castToDoubleValue(defaultValue: Double): Double {
        return defaultValue
    }

    @Override
    @Throws(ExpressionException::class)
    fun castToDateTime(): DateTime? {
        throw ExpressionException("can't cast Com Object to a Date")
    }

    @Override
    fun castToDateTime(defaultValue: DateTime?): DateTime? {
        return defaultValue
    }

    /**
     * @return Returns the dispatch.
     */
    fun getDispatch(): Dispatch? {
        return dispatch
    }

    /**
     * @return Returns the parent.
     */
    fun getParent(): Variant? {
        return parent
    }

    /**
     * release the com Object
     */
    fun release() {
        dispatch.safeRelease()
    }

    @Override
    @Throws(ExpressionException::class)
    operator fun compareTo(b: Boolean): Int {
        throw ExpressionException("can't compare Com Object with a boolean value")
    }

    @Override
    @Throws(PageException::class)
    operator fun compareTo(dt: DateTime?): Int {
        throw ExpressionException("can't compare Com Object with a DateTime Object")
    }

    @Override
    @Throws(PageException::class)
    operator fun compareTo(d: Double): Int {
        throw ExpressionException("can't compare Com Object with a numeric value")
    }

    @Override
    @Throws(PageException::class)
    operator fun compareTo(str: String?): Int {
        throw ExpressionException("can't compare Com Object with a String")
    }

    operator fun iterator(): Iterator? {
        return valueIterator()
    }

    @Override
    fun keyIterator(): Iterator<Collection.Key?>? {
        return COMKeyWrapperIterator(this)
    }

    @Override
    fun keysAsStringIterator(): Iterator<String?>? {
        return KeyAsStringIterator(keyIterator())
    }

    @Override
    fun valueIterator(): Iterator<Object?>? {
        return COMValueWrapperIterator(this)
    }

    @Override
    fun entryIterator(): Iterator<Entry<Key?, Object?>?>? {
        return ObjectsEntryIterator(keyIterator(), this)
    }
}