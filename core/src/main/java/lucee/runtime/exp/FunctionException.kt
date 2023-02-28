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
package lucee.runtime.exp

import java.util.ArrayList

/**
 * specified exception for Built-In Function
 */
class FunctionException : ExpressionException {
    /*
	 * * constructor of the class
	 * 
	 * @param pc current Page Context
	 * 
	 * @param functionName Name of the function that thorw the Exception
	 * 
	 * @param badArgumentPosition Position of the bad argument in the Argument List of the function
	 * 
	 * @param badArgumentName Name of the bad Argument
	 * 
	 * @param message additional Exception message / public FunctionException(PageContext pc,String
	 * functionName, String badArgumentPosition, String badArgumentName, String message) {
	 * this((PageContext)pc,functionName,badArgumentPosition,badArgumentName,message); }
	 */
    /**
     * constructor of the class
     *
     * @param pc current Page Context
     * @param functionName Name of the function that thorw the Exception
     * @param badArgumentPosition Position of the bad argument in the Argument List of the function
     * @param badArgumentName Name of the bad Argument
     * @param message additional Exception message
     */
    constructor(pc: PageContext?, functionName: String?, badArgumentPosition: Int, badArgumentName: String?, message: String?) : this(pc, functionName, toStringBadArgumentPosition(badArgumentPosition), badArgumentName, message, null) {}
    constructor(pc: PageContext?, functionName: String?, badArgumentPosition: Int, badArgumentName: String?, message: String?, detail: String?) : this(pc, functionName, toStringBadArgumentPosition(badArgumentPosition), badArgumentName, message, detail) {}
    constructor(pc: PageContext?, functionName: String?, badArgumentPosition: String?, badArgumentName: String?, message: String?, detail: String?) : super("Invalid call of the function [$functionName], $badArgumentPosition Argument [$badArgumentName] is invalid, $message", detail) {
        setAdditional(KeyConstants._pattern, getFunctionInfo(pc, functionName))
    }

    constructor(pc: PageContext?, functionName: String?, min: Int, max: Int, actual: Int) : super(if (actual < min) "too few arguments for function [$functionName] call" else "too many arguments for function [$functionName] call") {}

    companion object {
        private fun toStringBadArgumentPosition(pos: Int): String? {
            when (pos) {
                1 -> return "first"
                2 -> return "second"
                3 -> return "third"
                4 -> return "forth"
                5 -> return "fifth"
                6 -> return "sixth"
                7 -> return "seventh"
                8 -> return "eighth"
                9 -> return "ninth"
                10 -> return "tenth"
                11 -> return "eleventh"
                12 -> return "twelfth"
            }
            // TODO Auto-generated method stub
            return pos.toString() + "th"
        }

        private fun getFunctionInfo(pc: PageContext?, functionName: String?): String? {
            val flds: Array<FunctionLib?>
            val dialect: Int = pc.getCurrentTemplateDialect()
            flds = (pc.getConfig() as ConfigPro).getFLDs(dialect)
            var function: FunctionLibFunction? = null
            for (i in flds.indices) {
                function = flds[i].getFunction(functionName.toLowerCase())
                if (function != null) break
            }
            if (function == null) return ""
            val rtn = StringBuilder()
            rtn.append(function.getName().toString() + "(")
            var optionals = 0
            val args: ArrayList<FunctionLibFunctionArg?> = function.getArg()
            for (i in 0 until args.size()) {
                val arg: FunctionLibFunctionArg = args.get(i)
                if (i != 0) rtn.append(", ")
                if (!arg.getRequired()) {
                    rtn.append("[")
                    optionals++
                }
                rtn.append(arg.getName())
                rtn.append(":")
                rtn.append(arg.getTypeAsString())
            }
            for (i in 0 until optionals) rtn.append("]")
            rtn.append("):" + function.getReturnTypeAsString())
            return rtn.toString()
        }
    }
}