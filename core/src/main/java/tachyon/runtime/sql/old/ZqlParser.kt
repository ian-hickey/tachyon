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
package tachyon.runtime.sql.old

import java.io.DataInputStream

// Referenced classes of package Zql:
//            ZqlJJParser, ParseException, ZUtils, ZStatement, 
//            ZExp
class ZqlParser {
    constructor(inputstream: InputStream?) {
        _parser = null
        initParser(inputstream)
    }

    constructor() {
        _parser = null
    }

    fun initParser(inputstream: InputStream?) {
        if (_parser == null) _parser = ZqlJJParser(inputstream) else _parser.ReInit(inputstream)
    }

    fun addCustomFunction(s: String?, i: Int) {
        ZUtils.addCustomFunction(s, i)
    }

    @Throws(ParseException::class)
    fun readStatement(): ZStatement? {
        if (_parser == null) throw ParseException("Parser not initialized: use initParser(InputStream);")
        return _parser.SQLStatement()
    }

    @Throws(ParseException::class)
    fun readStatements(): Vector? {
        if (_parser == null) throw ParseException("Parser not initialized: use initParser(InputStream);")
        return _parser.SQLStatements()
    }

    @Throws(ParseException::class)
    fun readExpression(): ZExp? {
        if (_parser == null) throw ParseException("Parser not initialized: use initParser(InputStream);")
        return _parser.SQLExpression()
    }

    var _parser: ZqlJJParser?

    companion object {
        @Throws(ParseException::class)
        fun main(args: Array<String?>?) {
            var zqlparser: ZqlParser? = null
            zqlparser = if (args!!.size < 1) {
                System.out.println("/* Reading from stdin (exit; to finish) */")
                ZqlParser(System.`in`)
            } else {
                try {
                    ZqlParser(DataInputStream(FileInputStream(args[0])))
                } catch (filenotfoundexception: FileNotFoundException) {
                    System.out.println("/* File " + args[0] + " not found. Reading from stdin */")
                    ZqlParser(System.`in`)
                }
            }
            if (args.size > 0) System.out.println("/* Reading from " + args[0] + "*/")
            var zstatement: ZStatement? = null
            while (zqlparser!!.readStatement().also { zstatement = it } != null) {
                System.out.println(zstatement.toString().toString() + ";")
            }
            System.out.println("exit;")
            System.out.println("/* Parse Successful */")
        }
    }
}