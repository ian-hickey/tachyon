/**
 * Copyright (c) 2015, Tachyon Assosication Switzerland. All rights reserved.
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
package tachyon.runtime.component

import java.util.HashMap

class AbstractFinal {
    private val interfaces: Map<String?, InterfaceImpl?>? = HashMap()
    private val absUDFs: Map<Collection.Key?, UDFB?>? = HashMap<Collection.Key?, UDFB?>()
    private val finUDFs: Map<Collection.Key?, UDF?>? = HashMap<Collection.Key?, UDF?>()
    fun add(interfaces: List<InterfaceImpl?>?) {
        // add all interfaces to a flat structure
        val it: Iterator<InterfaceImpl?> = interfaces!!.iterator()
        var iit: Iterator<UDF?>
        var inter: InterfaceImpl?
        var udf: UDF?
        while (it.hasNext()) {
            inter = it.next()
            val parents: List<InterfaceImpl?> = inter._getExtends()

            // first add the parents, so children can overwrite functions with same name
            if (!ArrayUtil.isEmpty(parents)) add(parents)

            // UDFs
            iit = inter.getUDFIt()
            while (iit.hasNext()) {
                udf = iit.next()
                add(udf)
            }
            this.interfaces.put(inter.getPageSource().getDisplayPath(), inter) // this is add to a map to ensure we have every interface only once
        }
    }

    @Throws(ApplicationException::class)
    fun add(key: Collection.Key?, udf: UDF?) {
        if (Component.MODIFIER_ABSTRACT === udf.getModifier()) absUDFs.put(key, UDFB(udf))
        if (Component.MODIFIER_FINAL === udf.getModifier()) {
            if (finUDFs!!.containsKey(key)) {
                val existing: UDF? = finUDFs[key]
                throw ApplicationException("the function [" + key + "] from component [" + udf.getSource()
                        + "] tries to override a final method with the same name from component [" + existing.getSource() + "]")
            }
            finUDFs.put(key, udf)
        }
    }

    private fun add(udf: UDF?) {
        absUDFs.put(KeyImpl.init(udf.getFunctionName()), UDFB(udf))
    }

    /*
	 * public long lastUpdate() { if(lastUpdate==0 && !interfaces.isEmpty()){ long temp;
	 * Iterator<InterfaceImpl> it = interfaces.values().iterator(); while(it.hasNext()){
	 * temp=ComponentUtil.getCompileTime(null,it.next().getPageSource(),0); if(temp>lastUpdate)
	 * lastUpdate=temp; } } return lastUpdate; }
	 */
    fun hasAbstractUDFs(): Boolean {
        return !absUDFs!!.isEmpty()
    }

    fun hasFinalUDFs(): Boolean {
        return !finUDFs!!.isEmpty()
    }

    fun hasUDFs(): Boolean {
        return !finUDFs!!.isEmpty() || !absUDFs!!.isEmpty()
    }

    fun getInterfaceIt(): Iterator<InterfaceImpl?>? {
        return interfaces!!.values().iterator()
    }

    fun getInterfaces(): Array<Interface?>? {
        return interfaces!!.values().toArray(arrayOfNulls<Interface?>(interfaces.size()))
    }

    /*
	 * public Map<Collection.Key,UDF> getAbstractUDFs() { Map<Key, UDF> tmp = absUDFs; absUDFs=new
	 * HashMap<Collection.Key,UDF>(); return tmp; }
	 */
    fun getAbstractUDFBs(): Map<Collection.Key?, UDFB?>? {
        return absUDFs
    }

    fun getFinalUDFs(): Map<Collection.Key?, UDF?>? {
        return finUDFs
    }

    fun hasInterfaces(): Boolean {
        return !interfaces!!.isEmpty()
    }

    class UDFB(udf: UDF?) {
        var used = false
        val udf: UDF?

        init {
            this.udf = udf
        }
    }
}