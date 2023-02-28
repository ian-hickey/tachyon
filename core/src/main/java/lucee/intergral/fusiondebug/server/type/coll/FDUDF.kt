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
package lucee.intergral.fusiondebug.server.type.coll

import java.util.ArrayList

class FDUDF(frame: IFDStackFrame?, private val name: String?, udf: UDF?) : FDValueNotMutability() {
    private val children: ArrayList? = ArrayList()
    private val udf: UDF?
    @Override
    fun getChildren(): List? {
        return children
    }

    fun getName(): String? {
        return name
    }

    @Override
    fun hasChildren(): Boolean {
        return true
    }

    @Override
    override fun toString(): String {
        return toString(udf)!!
    }

    companion object {
        fun toString(udf: UDF?): String? {
            val args: Array<FunctionArgument?> = udf.getFunctionArguments()
            val sb = StringBuffer("function ")
            sb.append(udf.getFunctionName())
            sb.append("(")
            for (i in args.indices) {
                if (i > 0) sb.append(", ")
                sb.append(args[i].getTypeAsString())
                sb.append(" ")
                sb.append(args[i].getName())
            }
            sb.append("):")
            sb.append(udf.getReturnTypeAsString())
            return sb.toString()
        }
    }

    /**
     * Constructor of the class
     *
     * @param name
     * @param coll
     */
    init {
        this.udf = udf

        // meta
        var list: List<FDSimpleVariable?>? = ArrayList<FDSimpleVariable?>()
        children.add(FDSimpleVariable(frame, "Meta Data", "", list))
        list.add(FDSimpleVariable(frame, "Function Name", udf.getFunctionName(), null))
        if (!StringUtil.isEmpty(udf.getDisplayName())) list.add(FDSimpleVariable(frame, "Display Name", udf.getDisplayName(), null))
        if (!StringUtil.isEmpty(udf.getDescription())) list.add(FDSimpleVariable(frame, "Description", udf.getDescription(), null))
        if (!StringUtil.isEmpty(udf.getHint())) list.add(FDSimpleVariable(frame, "Hint", udf.getHint(), null))
        list.add(FDSimpleVariable(frame, "Return Type", udf.getReturnTypeAsString(), null))
        list.add(FDSimpleVariable(frame, "Return Format", UDFUtil.toReturnFormat(udf.getReturnFormat(), "plain"), null))
        list.add(FDSimpleVariable(frame, "Source", Caster.toString(udf.getSource()), null))
        list.add(FDSimpleVariable(frame, "Secure Json", Caster.toString(udf.getSecureJson(), ""), null))
        list.add(FDSimpleVariable(frame, "Verify Client", Caster.toString(udf.getVerifyClient(), ""), null))

        // arguments
        list = ArrayList()
        var el: List?
        children.add(FDSimpleVariable(frame, "Arguments", "", list))
        val args: Array<FunctionArgument?> = udf.getFunctionArguments()
        for (i in args.indices) {
            el = ArrayList()
            list.add(FDSimpleVariable(frame, "[" + (i + 1) + "]", "", el))
            el.add(FDSimpleVariable(frame, "Name", args[i].getName().getString(), null))
            el.add(FDSimpleVariable(frame, "Type", args[i].getTypeAsString(), null))
            el.add(FDSimpleVariable(frame, "Required", Caster.toString(args[i].isRequired()), null))
            if (!StringUtil.isEmpty(args[i].getDisplayName())) el.add(FDSimpleVariable(frame, "Display Name", args[i].getDisplayName(), null))
            if (!StringUtil.isEmpty(args[i].getHint())) el.add(FDSimpleVariable(frame, "Hint", args[i].getHint(), null))
        }

        // return
        children.add(FDSimpleVariable(frame, "return", udf.getReturnTypeAsString(), null))
    }
}