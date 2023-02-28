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
package lucee.runtime.sql.old

import java.io.InputStream

// Referenced classes of package Zql:
//            ParseException, ZTransactStmt, ZLockTable, ZUpdate, 
//            ZInsert, ZExpression, ZDelete, ZQuery, 
//            ZSelectItem, ZFromItem, ZGroupBy, ZOrderBy, 
//            ZConstant, SimpleCharStream, ZqlJJParserTokenManager, Token, 
//            ZqlJJParserConstants, ZUtils, ZStatement, ZExp
class ZqlJJParser : ZqlJJParserConstants {
    internal class JJCalls {
        var gen = 0
        var first: Token? = null
        var arg = 0
        var next: JJCalls? = null
    }

    /*
	 * public static void main(String args[]) throws ParseException { ZqlJJParser zqljjparser = null; if
	 * (args.length < 1) { //System.out.println("Reading from stdin (exit; to finish)"); zqljjparser =
	 * new ZqlJJParser(System.in); } else { try { zqljjparser = new ZqlJJParser(new DataInputStream(new
	 * FileInputStream(args[0]))); } catch (FileNotFoundException filenotfoundexception) {
	 * //System.out.println("File " + args[0] + " not found. Reading from stdin"); zqljjparser = new
	 * ZqlJJParser(System.in); } } if (args.length > 0) System.out.println(args[0]); for (ZStatement
	 * zstatement = null; (zstatement = zqljjparser.SQLStatement()) != null;)
	 * //System.out.println(zstatement.toString());
	 * 
	 * System.out.println("Parse Successful"); }
	 */
    @Throws(ParseException::class)
    fun BasicDataTypeDeclaration() {
        when (if (jj_ntk != -1) jj_ntk else jj_ntk()) {
            15, 27, 34, 44, 48, 56, 70, 71 -> {
                when (if (jj_ntk != -1) jj_ntk else jj_ntk()) {
                    15 -> jj_consume_token(15)
                    71 -> jj_consume_token(71)
                    70 -> jj_consume_token(70)
                    34 -> jj_consume_token(34)
                    48 -> jj_consume_token(48)
                    44 -> jj_consume_token(44)
                    56 -> jj_consume_token(56)
                    27 -> jj_consume_token(27)
                    else -> {
                        jj_la1!![0] = jj_gen
                        jj_consume_token(-1)
                        throw ParseException()
                    }
                }
                when (if (jj_ntk != -1) jj_ntk else jj_ntk()) {
                    88 -> {
                        jj_consume_token(88)
                        jj_consume_token(76)
                        when (if (jj_ntk != -1) jj_ntk else jj_ntk()) {
                            89 -> {
                                jj_consume_token(89)
                                jj_consume_token(76)
                            }
                            else -> jj_la1!![1] = jj_gen
                        }
                        jj_consume_token(90)
                    }
                    else -> jj_la1!![2] = jj_gen
                }
            }
            20 -> jj_consume_token(20)
            12 -> jj_consume_token(12)
            13 -> jj_consume_token(13)
            else -> {
                jj_la1!![3] = jj_gen
                jj_consume_token(-1)
                throw ParseException()
            }
        }
    }

    @Throws(ParseException::class)
    fun SQLStatements(): Vector? {
        val vector = Vector()
        label0@ do {
            val zstatement: ZStatement = SQLStatement() ?: return vector
            vector.addElement(zstatement)
            when (if (jj_ntk != -1) jj_ntk else jj_ntk()) {
                17, 21, 26, 33, 39, 54, 57, 59, 60, 68 -> {
                }
                else -> {
                    jj_la1!![4] = jj_gen
                    break@label0
                }
            }
        } while (true)
        return vector
    }

    @Throws(ParseException::class)
    fun SQLStatement(): ZStatement? {
        // Object obj = null;
        when (if (jj_ntk != -1) jj_ntk else jj_ntk()) {
            17 -> {
                return CommitStatement()
            }
            21 -> {
                return DeleteStatement()
            }
            33 -> {
                return InsertStatement()
            }
            39 -> {
                return LockTableStatement()
            }
            57 -> {
                return RollbackStatement()
            }
            59 -> {
                return QueryStatement()
            }
            60 -> {
                return SetTransactionStatement()
            }
            68 -> {
                return UpdateStatement()
            }
            26, 54 -> {
                when (if (jj_ntk != -1) jj_ntk else jj_ntk()) {
                    26 -> jj_consume_token(26)
                    54 -> jj_consume_token(54)
                    else -> {
                        jj_la1!![5] = jj_gen
                        jj_consume_token(-1)
                        throw ParseException()
                    }
                }
                jj_consume_token(91)
                return null
            }
        }
        jj_la1!![6] = jj_gen
        jj_consume_token(-1)
        throw ParseException()
    }

    @Throws(ParseException::class)
    fun CommitStatement(): ZTransactStmt? {
        val ztransactstmt = ZTransactStmt("COMMIT")
        jj_consume_token(17)
        when (if (jj_ntk != -1) jj_ntk else jj_ntk()) {
            74 -> jj_consume_token(74)
            else -> jj_la1!![7] = jj_gen
        }
        when (if (jj_ntk != -1) jj_ntk else jj_ntk()) {
            16 -> {
                jj_consume_token(16)
                val token1: Token? = jj_consume_token(86)
                ztransactstmt.setComment(token1.toString())
            }
            else -> jj_la1!![8] = jj_gen
        }
        jj_consume_token(91)
        return ztransactstmt
    }

    @Throws(ParseException::class)
    fun LockTableStatement(): ZLockTable? {
        val zlocktable = ZLockTable()
        val vector = Vector()
        jj_consume_token(39)
        jj_consume_token(65)
        var s = TableReference()
        vector.addElement(s)
        label0@ do when (if (jj_ntk != -1) jj_ntk else jj_ntk()) {
            89 -> {
                jj_consume_token(89)
                s = TableReference()
                vector.addElement(s)
            }
            else -> {
                jj_la1!![9] = jj_gen
                break@label0
            }
        } while (true)
        jj_consume_token(32)
        s = LockMode()
        zlocktable.setLockMode(s)
        jj_consume_token(43)
        when (if (jj_ntk != -1) jj_ntk else jj_ntk()) {
            46 -> {
                jj_consume_token(46)
                zlocktable.nowait_ = true
            }
            else -> jj_la1!![10] = jj_gen
        }
        jj_consume_token(91)
        zlocktable!!.addTables(vector)
        return zlocktable
    }

    @Throws(ParseException::class)
    fun RollbackStatement(): ZTransactStmt? {
        val ztransactstmt = ZTransactStmt("ROLLBACK")
        jj_consume_token(57)
        when (if (jj_ntk != -1) jj_ntk else jj_ntk()) {
            74 -> jj_consume_token(74)
            else -> jj_la1!![11] = jj_gen
        }
        when (if (jj_ntk != -1) jj_ntk else jj_ntk()) {
            16 -> {
                jj_consume_token(16)
                val token1: Token? = jj_consume_token(86)
                ztransactstmt.setComment(token1.toString())
            }
            else -> jj_la1!![12] = jj_gen
        }
        jj_consume_token(91)
        return ztransactstmt
    }

    @Throws(ParseException::class)
    fun SetTransactionStatement(): ZTransactStmt? {
        val ztransactstmt = ZTransactStmt("SET TRANSACTION")
        var flag = false
        jj_consume_token(60)
        jj_consume_token(66)
        jj_consume_token(55)
        when (if (jj_ntk != -1) jj_ntk else jj_ntk()) {
            50 -> {
                jj_consume_token(50)
                flag = true
            }
            75 -> jj_consume_token(75)
            else -> {
                jj_la1!![13] = jj_gen
                jj_consume_token(-1)
                throw ParseException()
            }
        }
        jj_consume_token(91)
        ztransactstmt.readOnly_ = flag
        return ztransactstmt
    }

    @Throws(ParseException::class)
    fun LockMode(): String? {
        val stringbuffer = StringBuffer()
        when (if (jj_ntk != -1) jj_ntk else jj_ntk()) {
            58 -> {
                jj_consume_token(58)
                stringbuffer.append("ROW ")
                when (if (jj_ntk != -1) jj_ntk else jj_ntk()) {
                    61 -> {
                        jj_consume_token(61)
                        stringbuffer.append("SHARE")
                    }
                    24 -> {
                        jj_consume_token(24)
                        stringbuffer.append("EXCLUSIVE")
                    }
                    else -> {
                        jj_la1!![14] = jj_gen
                        jj_consume_token(-1)
                        throw ParseException()
                    }
                }
                return stringbuffer.toString()
            }
            61 -> {
                jj_consume_token(61)
                stringbuffer.append("SHARE")
                when (if (jj_ntk != -1) jj_ntk else jj_ntk()) {
                    58, 68 -> when (if (jj_ntk != -1) jj_ntk else jj_ntk()) {
                        68 -> {
                            jj_consume_token(68)
                            stringbuffer.append(" UPDATE")
                        }
                        58 -> {
                            jj_consume_token(58)
                            jj_consume_token(24)
                            stringbuffer.append(" ROW EXCLUSIVE")
                        }
                        else -> {
                            jj_la1!![15] = jj_gen
                            jj_consume_token(-1)
                            throw ParseException()
                        }
                    }
                    else -> jj_la1!![16] = jj_gen
                }
                return stringbuffer.toString()
            }
            24 -> {
                jj_consume_token(24)
                return String("EXCLUSIVE")
            }
        }
        jj_la1!![17] = jj_gen
        jj_consume_token(-1)
        throw ParseException()
    }

    @Throws(ParseException::class)
    fun UpdateStatement(): ZUpdate? {
        jj_consume_token(68)
        val s = TableReference()
        val zupdate = ZUpdate(s)
        jj_consume_token(60)
        ColumnValues(zupdate)
        when (if (jj_ntk != -1) jj_ntk else jj_ntk()) {
            72 -> {
                jj_consume_token(72)
                val zexp: ZExp? = SQLExpression()
                zupdate!!.addWhere(zexp)
            }
            else -> jj_la1!![18] = jj_gen
        }
        jj_consume_token(91)
        return zupdate
    }

    @Throws(ParseException::class)
    fun ColumnValues(zupdate: ZUpdate?) {
        val s = TableColumn()
        jj_consume_token(92)
        val zexp: ZExp? = UpdatedValue()
        zupdate!!.addColumnUpdate(s, zexp)
        label0@ do when (if (jj_ntk != -1) jj_ntk else jj_ntk()) {
            89 -> {
                jj_consume_token(89)
                val s1 = TableColumn()
                jj_consume_token(92)
                val zexp1: ZExp? = UpdatedValue()
                zupdate!!.addColumnUpdate(s1, zexp1)
            }
            else -> {
                jj_la1!![19] = jj_gen
                break@label0
            }
        } while (true)
    }

    @Throws(ParseException::class)
    fun UpdatedValue(): ZExp? {
        if (jj_2_1(0x7fffffff)) {
            jj_consume_token(88)
            val zquery: ZQuery? = SelectStatement()
            jj_consume_token(90)
            return zquery
        }
        when (if (jj_ntk != -1) jj_ntk else jj_ntk()) {
            10, 19, 25, 40, 41, 45, 47, 53, 64, 76, 82, 85, 86, 87, 88, 101, 102 -> {
                return SQLExpression()
            }
            105 -> {
                return PreparedCol()
            }
        }
        jj_la1!![20] = jj_gen
        jj_consume_token(-1)
        throw ParseException()
    }

    @Throws(ParseException::class)
    fun InsertStatement(): ZInsert? {
        jj_consume_token(33)
        jj_consume_token(36)
        val s = TableReference()
        val zinsert = ZInsert(s)
        when (if (jj_ntk != -1) jj_ntk else jj_ntk()) {
            88 -> {
                jj_consume_token(88)
                val s1 = TableColumn()
                val vector = Vector()
                vector.addElement(s1)
                label0@ do when (if (jj_ntk != -1) jj_ntk else jj_ntk()) {
                    89 -> {
                        jj_consume_token(89)
                        val s2 = TableColumn()
                        vector.addElement(s2)
                    }
                    else -> {
                        jj_la1!![21] = jj_gen
                        break@label0
                    }
                } while (true)
                jj_consume_token(90)
                zinsert!!.addColumns(vector)
            }
            else -> jj_la1!![22] = jj_gen
        }
        when (if (jj_ntk != -1) jj_ntk else jj_ntk()) {
            69 -> {
                jj_consume_token(69)
                jj_consume_token(88)
                val vector1: Vector? = SQLExpressionList()
                jj_consume_token(90)
                val zexpression = ZExpression(",")
                zexpression.setOperands(vector1)
                zinsert!!.addValueSpec(zexpression)
            }
            59 -> {
                val zquery: ZQuery? = SelectStatement()
                zinsert!!.addValueSpec(zquery)
            }
            else -> {
                jj_la1!![23] = jj_gen
                jj_consume_token(-1)
                throw ParseException()
            }
        }
        jj_consume_token(91)
        return zinsert
    }

    @Throws(ParseException::class)
    fun DeleteStatement(): ZDelete? {
        jj_consume_token(21)
        when (if (jj_ntk != -1) jj_ntk else jj_ntk()) {
            29 -> jj_consume_token(29)
            else -> jj_la1!![24] = jj_gen
        }
        val s = TableReference()
        val zdelete = ZDelete(s)
        when (if (jj_ntk != -1) jj_ntk else jj_ntk()) {
            72 -> {
                jj_consume_token(72)
                val zexp: ZExp? = SQLExpression()
                zdelete!!.addWhere(zexp)
            }
            else -> jj_la1!![25] = jj_gen
        }
        jj_consume_token(91)
        return zdelete
    }

    @Throws(ParseException::class)
    fun QueryStatement(): ZQuery? {
        val zquery: ZQuery? = SelectStatement()
        jj_consume_token(91)
        return zquery
    }

    @Throws(ParseException::class)
    fun TableColumn(): String? {
        val stringbuffer = StringBuffer()
        val s = OracleObjectName()
        stringbuffer.append(s)
        when (if (jj_ntk != -1) jj_ntk else jj_ntk()) {
            93 -> {
                jj_consume_token(93)
                val s1 = OracleObjectName()
                stringbuffer.append(".$s1")
                when (if (jj_ntk != -1) jj_ntk else jj_ntk()) {
                    93 -> {
                        jj_consume_token(93)
                        val s2 = OracleObjectName()
                        stringbuffer.append(".$s2")
                    }
                    else -> jj_la1!![26] = jj_gen
                }
            }
            else -> jj_la1!![27] = jj_gen
        }
        return stringbuffer.toString()
    }

    @Throws(ParseException::class)
    fun OracleObjectName(): String? {
        when (if (jj_ntk != -1) jj_ntk else jj_ntk()) {
            82 -> {
                val token1: Token? = jj_consume_token(82)
                return token1.toString()
            }
            87 -> {
                val token2: Token? = jj_consume_token(87)
                return token2.toString()
            }
        }
        val token1: Token? = jj_consume_token(82)
        return token1.toString()

        // MOD jj_la1[28] = jj_gen;
        // MOD jj_consume_token(-1);
        // MOD throw new ParseException();
    }

    @Throws(ParseException::class)
    fun Relop(): String? {
        return when (if (jj_ntk != -1) jj_ntk else jj_ntk()) {
            92 -> {
                val token1: Token? = jj_consume_token(92)
                token1.toString()
            }
            94 -> {
                val token2: Token? = jj_consume_token(94)
                token2.toString()
            }
            95 -> {
                val token3: Token? = jj_consume_token(95)
                token3.toString()
            }
            96 -> {
                val token4: Token? = jj_consume_token(96)
                token4.toString()
            }
            97 -> {
                val token5: Token? = jj_consume_token(97)
                token5.toString()
            }
            98 -> {
                val token6: Token? = jj_consume_token(98)
                token6.toString()
            }
            99 -> {
                val token7: Token? = jj_consume_token(99)
                token7.toString()
            }
            100 -> {
                val token8: Token? = jj_consume_token(100)
                token8.toString()
            }
            93 -> {
                jj_la1!![29] = jj_gen
                jj_consume_token(-1)
                throw ParseException()
            }
            else -> {
                jj_la1!![29] = jj_gen
                jj_consume_token(-1)
                throw ParseException()
            }
        }
    }

    @Throws(ParseException::class)
    fun TableReference(): String? {
        val stringbuffer = StringBuffer()
        val s = OracleObjectName()
        stringbuffer.append(s)
        /*
		 * changed by mic switch(jj_ntk != -1 ? jj_ntk : jj_ntk()) { case 93: // ']' jj_consume_token(93);
		 * String s1 = OracleObjectName(); stringbuffer.append("." + s1); break;
		 * 
		 * default: jj_la1[30] = jj_gen; break; }
		 */while (true) {
            if ((if (jj_ntk != -1) jj_ntk else jj_ntk()) == 93) {
                jj_consume_token(93)
                val s1 = OracleObjectName()
                stringbuffer.append(".$s1")
            } else {
                jj_la1!![30] = jj_gen
                break
            }
        }
        return stringbuffer.toString()
    }

    @Throws(ParseException::class)
    fun NumOrID() {
        when (if (jj_ntk != -1) jj_ntk else jj_ntk()) {
            82 -> jj_consume_token(82)
            76, 101, 102 -> {
                when (if (jj_ntk != -1) jj_ntk else jj_ntk()) {
                    101, 102 -> when (if (jj_ntk != -1) jj_ntk else jj_ntk()) {
                        101 -> jj_consume_token(101)
                        102 -> jj_consume_token(102)
                        else -> {
                            jj_la1!![31] = jj_gen
                            jj_consume_token(-1)
                            throw ParseException()
                        }
                    }
                    else -> jj_la1!![32] = jj_gen
                }
                jj_consume_token(76)
            }
            else -> {
                jj_la1!![33] = jj_gen
                jj_consume_token(-1)
                throw ParseException()
            }
        }
    }

    @Throws(ParseException::class)
    fun SelectStatement(): ZQuery? {
        val zquery: ZQuery? = SelectWithoutOrder()
        when (if (jj_ntk != -1) jj_ntk else jj_ntk()) {
            52 -> {
                val vector: Vector? = OrderByClause()
                zquery!!.addOrderBy(vector)
            }
            else -> jj_la1!![34] = jj_gen
        }
        when (if (jj_ntk != -1) jj_ntk else jj_ntk()) {
            28 -> {
                ForUpdateClause()
                zquery.forupdate_ = true
            }
            else -> jj_la1!![35] = jj_gen
        }
        return zquery
    }

    @Throws(ParseException::class)
    fun SelectWithoutOrder(): ZQuery? {
        val zquery = ZQuery()
        var zexp: ZExp? = null
        var zgroupby: ZGroupBy? = null
        var zexpression: ZExpression? = null
        jj_consume_token(59)
        when (if (jj_ntk != -1) jj_ntk else jj_ntk()) {
            5, 23 -> when (if (jj_ntk != -1) jj_ntk else jj_ntk()) {
                5 -> jj_consume_token(5)
                23 -> {
                    jj_consume_token(23)
                    zquery.distinct_ = true
                }
                else -> {
                    jj_la1!![36] = jj_gen
                    jj_consume_token(-1)
                    throw ParseException()
                }
            }
            else -> jj_la1!![37] = jj_gen
        }
        val vector: Vector? = SelectList()
        val vector1: Vector? = FromClause()
        when (if (jj_ntk != -1) jj_ntk else jj_ntk()) {
            72 -> zexp = WhereClause()
            else -> jj_la1!![38] = jj_gen
        }
        when (if (jj_ntk != -1) jj_ntk else jj_ntk()) {
            18, 63 -> ConnectClause()
            else -> jj_la1!![39] = jj_gen
        }
        when (if (jj_ntk != -1) jj_ntk else jj_ntk()) {
            30 -> zgroupby = GroupByClause()
            else -> jj_la1!![40] = jj_gen
        }
        when (if (jj_ntk != -1) jj_ntk else jj_ntk()) {
            35, 42, 67 -> zexpression = SetClause()
            else -> jj_la1!![41] = jj_gen
        }
        zquery!!.addSelect(vector)
        zquery!!.addFrom(vector1)
        zquery!!.addWhere(zexp)
        zquery!!.addGroupBy(zgroupby)
        zquery!!.addSet(zexpression)
        return zquery
    }

    @Throws(ParseException::class)
    fun SelectList(): Vector? {
        val vector = Vector(8)
        val i = if (jj_ntk != -1) jj_ntk else jj_ntk()
        when (i) {
            103 -> {
                jj_consume_token(103)
                vector.addElement(ZSelectItem("*"))
                return vector
            }
            10, 19, 40, 41, 47, 64, 76, 82, 85, 86, 87, 88, 101, 102 -> {
                // default:
                val zselectitem: ZSelectItem? = SelectItem()
                vector.addElement(zselectitem)
                // print.out("sel:"+zselectitem.column_+"::"+zselectitem.alias_);
                label0@ do when (if (jj_ntk != -1) jj_ntk else jj_ntk()) {
                    89 -> {
                        jj_consume_token(89)
                        val zselectitem1: ZSelectItem? = SelectItem()
                        vector.addElement(zselectitem1)
                    }
                    else -> {
                        jj_la1!![42] = jj_gen
                        break@label0
                    }
                } while (true)
                return vector
            }
        }
        jj_la1!![43] = jj_gen
        jj_consume_token(-1)
        throw ParseException()
    }

    @Throws(ParseException::class)
    fun SelectItem(): ZSelectItem? {
        if (jj_2_2(0x7fffffff)) {
            val s = SelectStar()
            return ZSelectItem(s)
        }
        when (if (jj_ntk != -1) jj_ntk else jj_ntk()) {
            10, 19, 40, 41, 47, 64, 76, 82, 85, 86, 87, 88, 101, 102 -> {
                // default:
                val zexp: ZExp? = SQLSimpleExpression()
                val zselectitem = ZSelectItem(zexp.toString())
                zselectitem.setExpression(zexp)
                when (if (jj_ntk != -1) jj_ntk else jj_ntk()) {
                    8, 82 -> {
                        val s1 = SelectAlias()
                        zselectitem.setAlias(s1)
                    }
                    else -> jj_la1!![44] = jj_gen
                }
                return zselectitem
            }
        }
        jj_la1!![45] = jj_gen
        jj_consume_token(-1)
        throw ParseException()
    }

    @Throws(ParseException::class)
    fun SelectAlias(): String? {
        val stringbuffer = StringBuffer()
        when (if (jj_ntk != -1) jj_ntk else jj_ntk()) {
            8 -> jj_consume_token(8)
            else -> jj_la1!![46] = jj_gen
        }
        label0@ do {
            val token1: Token? = jj_consume_token(82)
            stringbuffer.append(token1.toString().trim().toString() + " ")
            when (if (jj_ntk != -1) jj_ntk else jj_ntk()) {
                82 -> {
                }
                else -> {
                    jj_la1!![47] = jj_gen
                    break@label0
                }
            }
        } while (true)
        return stringbuffer.toString().trim()
    }

    @Throws(ParseException::class)
    fun SelectStar(): String? {
        if (jj_2_3(2)) {
            val s = OracleObjectName()
            jj_consume_token(104)
            return String(s.toString() + ".*")
        }
        if (jj_2_4(4)) {
            val s1 = OracleObjectName()
            jj_consume_token(93)
            val s2 = OracleObjectName()
            jj_consume_token(104)
            return String(s1.toString() + "." + s2 + ".*")
        }
        jj_consume_token(-1)
        throw ParseException()
    }

    @Throws(ParseException::class)
    fun FromClause(): Vector? {
        val vector = Vector(8)
        jj_consume_token(29)
        val zfromitem: ZFromItem? = FromItem()
        vector.addElement(zfromitem)
        label0@ do when (if (jj_ntk != -1) jj_ntk else jj_ntk()) {
            89 -> {
                jj_consume_token(89)
                val zfromitem1: ZFromItem? = FromItem()
                vector.addElement(zfromitem1)
            }
            else -> {
                jj_la1!![48] = jj_gen
                break@label0
            }
        } while (true)
        return vector
    }

    @Throws(ParseException::class)
    fun FromItem(): ZFromItem? {
        val s = TableReference()
        val zfromitem = ZFromItem(s)
        when (if (jj_ntk != -1) jj_ntk else jj_ntk()) {
            82 -> {
                val token1: Token? = jj_consume_token(82)
                zfromitem.setAlias(token1.toString())
            }
            else -> jj_la1!![49] = jj_gen
        }
        return zfromitem
    }

    @Throws(ParseException::class)
    fun WhereClause(): ZExp? {
        jj_consume_token(72)
        return SQLExpression()
    }

    @Throws(ParseException::class)
    fun ConnectClause() {
        when (if (jj_ntk != -1) jj_ntk else jj_ntk()) {
            63 -> {
                jj_consume_token(63)
                jj_consume_token(73)
                SQLExpression()
            }
            else -> jj_la1!![50] = jj_gen
        }
        jj_consume_token(18)
        jj_consume_token(14)
        SQLExpression()
        when (if (jj_ntk != -1) jj_ntk else jj_ntk()) {
            63 -> {
                jj_consume_token(63)
                jj_consume_token(73)
                SQLExpression()
            }
            else -> jj_la1!![51] = jj_gen
        }
    }

    @Throws(ParseException::class)
    fun GroupByClause(): ZGroupBy? {
        var zgroupby: ZGroupBy? = null
        jj_consume_token(30)
        jj_consume_token(14)
        val vector: Vector? = SQLExpressionList()
        zgroupby = ZGroupBy(vector)
        when (if (jj_ntk != -1) jj_ntk else jj_ntk()) {
            31 -> {
                jj_consume_token(31)
                val zexp: ZExp? = SQLExpression()
                zgroupby.setHaving(zexp)
            }
            else -> jj_la1!![52] = jj_gen
        }
        return zgroupby
    }

    @Throws(ParseException::class)
    fun SetClause(): ZExpression? {
        val token1: Token?
        when (if (jj_ntk != -1) jj_ntk else jj_ntk()) {
            67 -> {
                token1 = jj_consume_token(67)
                when (if (jj_ntk != -1) jj_ntk else jj_ntk()) {
                    5 -> jj_consume_token(5)
                    else -> jj_la1!![53] = jj_gen
                }
            }
            35 -> token1 = jj_consume_token(35)
            42 -> token1 = jj_consume_token(42)
            else -> {
                jj_la1!![54] = jj_gen
                jj_consume_token(-1)
                throw ParseException()
            }
        }
        val zexpression = ZExpression(token1.toString())
        if (jj_2_5(0x7fffffff)) {
            jj_consume_token(88)
            val zquery: ZQuery? = SelectWithoutOrder()
            zexpression!!.addOperand(zquery)
            jj_consume_token(90)
        } else {
            when (if (jj_ntk != -1) jj_ntk else jj_ntk()) {
                59 -> {
                    val zquery1: ZQuery? = SelectWithoutOrder()
                    zexpression!!.addOperand(zquery1)
                }
                else -> {
                    jj_la1!![55] = jj_gen
                    jj_consume_token(-1)
                    throw ParseException()
                }
            }
        }
        return zexpression
    }

    @Throws(ParseException::class)
    fun OrderByClause(): Vector? {
        val vector = Vector()
        jj_consume_token(52)
        jj_consume_token(14)
        val zexp: ZExp? = SQLSimpleExpression()
        val zorderby = ZOrderBy(zexp)
        when (if (jj_ntk != -1) jj_ntk else jj_ntk()) {
            9, 22 -> when (if (jj_ntk != -1) jj_ntk else jj_ntk()) {
                9 -> jj_consume_token(9)
                22 -> {
                    jj_consume_token(22)
                    zorderby.setAscOrder(false)
                }
                else -> {
                    jj_la1!![56] = jj_gen
                    jj_consume_token(-1)
                    throw ParseException()
                }
            }
            else -> jj_la1!![57] = jj_gen
        }
        vector.addElement(zorderby)
        label0@ do when (if (jj_ntk != -1) jj_ntk else jj_ntk()) {
            89 -> {
                jj_consume_token(89)
                val zexp1: ZExp? = SQLSimpleExpression()
                val zorderby1 = ZOrderBy(zexp1)
                when (if (jj_ntk != -1) jj_ntk else jj_ntk()) {
                    9, 22 -> when (if (jj_ntk != -1) jj_ntk else jj_ntk()) {
                        9 -> jj_consume_token(9)
                        22 -> {
                            jj_consume_token(22)
                            zorderby1.setAscOrder(false)
                        }
                        else -> {
                            jj_la1!![59] = jj_gen
                            jj_consume_token(-1)
                            throw ParseException()
                        }
                    }
                    else -> jj_la1!![60] = jj_gen
                }
                vector.addElement(zorderby1)
            }
            else -> {
                jj_la1!![58] = jj_gen
                break@label0
            }
        } while (true)
        return vector
    }

    @Throws(ParseException::class)
    fun ForUpdateClause() {
        jj_consume_token(28)
        jj_consume_token(68)
        label0@ when (if (jj_ntk != -1) jj_ntk else jj_ntk()) {
            49 -> {
                jj_consume_token(49)
                TableColumn()
                do when (if (jj_ntk != -1) jj_ntk else jj_ntk()) {
                    89 -> {
                        jj_consume_token(89)
                        TableColumn()
                    }
                    else -> {
                        jj_la1!![61] = jj_gen
                        break@label0
                    }
                } while (true)
                jj_la1!![62] = jj_gen
            }
            else -> jj_la1!![62] = jj_gen
        }
    }

    @Throws(ParseException::class)
    fun SQLExpression(): ZExp? {
        var zexpression: ZExpression? = null
        var flag = true
        val zexp: ZExp? = SQLAndExpression()
        label0@ do when (if (jj_ntk != -1) jj_ntk else jj_ntk()) {
            51 -> {
                jj_consume_token(51)
                val zexp1: ZExp? = SQLAndExpression()
                if (flag) zexpression = ZExpression("OR", zexp)
                flag = false
                zexpression!!.addOperand(zexp1)
            }
            else -> {
                jj_la1!![63] = jj_gen
                break@label0
            }
        } while (true)
        return if (flag) zexp else zexpression
    }

    @Throws(ParseException::class)
    fun SQLAndExpression(): ZExp? {
        var zexpression: ZExpression? = null
        var flag = true
        val zexp: ZExp? = SQLUnaryLogicalExpression()
        label0@ do when (if (jj_ntk != -1) jj_ntk else jj_ntk()) {
            6 -> {
                jj_consume_token(6)
                val zexp1: ZExp? = SQLUnaryLogicalExpression()
                if (flag) zexpression = ZExpression("AND", zexp)
                flag = false
                zexpression!!.addOperand(zexp1)
            }
            else -> {
                jj_la1!![64] = jj_gen
                break@label0
            }
        } while (true)
        return if (flag) zexp else zexpression
    }

    @Throws(ParseException::class)
    fun SQLUnaryLogicalExpression(): ZExp? {
        var flag = false
        if (jj_2_6(2)) {
            return ExistsClause()
        }
        when (if (jj_ntk != -1) jj_ntk else jj_ntk()) {
            10, 19, 40, 41, 45, 47, 53, 64, 76, 82, 85, 86, 87, 88, 101, 102 -> {
                when (if (jj_ntk != -1) jj_ntk else jj_ntk()) {
                    45 -> {
                        jj_consume_token(45)
                        flag = true
                    }
                    else -> jj_la1!![65] = jj_gen
                }
                val zexp: ZExp? = SQLRelationalExpression()
                val obj: Object?
                if (flag) obj = ZExpression("NOT", zexp) else obj = zexp
                return obj
            }
        }
        jj_la1!![66] = jj_gen
        jj_consume_token(-1)
        throw ParseException()
    }

    @Throws(ParseException::class)
    fun ExistsClause(): ZExpression? {
        var flag = false
        when (if (jj_ntk != -1) jj_ntk else jj_ntk()) {
            45 -> {
                jj_consume_token(45)
                flag = true
            }
            else -> jj_la1!![67] = jj_gen
        }
        jj_consume_token(25)
        jj_consume_token(88)
        val zquery: ZQuery? = SubQuery()
        jj_consume_token(90)
        val zexpression1 = ZExpression("EXISTS", zquery)
        val zexpression: ZExpression?
        if (flag) zexpression = ZExpression("NOT", zexpression1) else zexpression = zexpression1
        return zexpression
    }

    @Throws(ParseException::class)
    fun SQLRelationalExpression(): ZExp? {
        var zexpression: ZExpression? = null
        var flag = false
        val obj: Object?
        if (jj_2_7(0x7fffffff)) {
            jj_consume_token(88)
            val vector: Vector? = SQLExpressionList()
            jj_consume_token(90)
            obj = ZExpression(",")
            (obj as ZExpression?).setOperands(vector)
        } else {
            when (if (jj_ntk != -1) jj_ntk else jj_ntk()) {
                10, 19, 40, 41, 47, 53, 64, 76, 82, 85, 86, 87, 88, 101, 102 -> {
                    when (if (jj_ntk != -1) jj_ntk else jj_ntk()) {
                        53 -> {
                            jj_consume_token(53)
                            flag = true
                        }
                        else -> jj_la1!![68] = jj_gen
                    }
                    val zexp: ZExp? = SQLSimpleExpression()
                    if (flag) obj = ZExpression("PRIOR", zexp) else obj = zexp
                }
                else -> {
                    jj_la1!![69] = jj_gen
                    jj_consume_token(-1)
                    throw ParseException()
                }
            }
        }
        when (if (jj_ntk != -1) jj_ntk else jj_ntk()) {
            11, 32, 37, 38, 45, 92, 94, 95, 96, 97, 98, 99, 100 -> when (if (jj_ntk != -1) jj_ntk else jj_ntk()) {
                92, 94, 95, 96, 97, 98, 99, 100 -> zexpression = SQLRelationalOperatorExpression()
                93 -> {
                    jj_la1!![70] = jj_gen
                    if (jj_2_8(2)) zexpression = SQLInClause() else if (jj_2_9(2)) zexpression = SQLBetweenClause() else if (jj_2_10(2)) zexpression = SQLLikeClause() else when (if (jj_ntk != -1) jj_ntk else jj_ntk()) {
                        37 -> zexpression = IsNullClause()
                        else -> {
                            jj_la1[71] = jj_gen
                            jj_consume_token(-1)
                            throw ParseException()
                        }
                    }
                }
                else -> {
                    jj_la1!![70] = jj_gen
                    if (jj_2_8(2)) zexpression = SQLInClause() else if (jj_2_9(2)) zexpression = SQLBetweenClause() else if (jj_2_10(2)) zexpression = SQLLikeClause() else when (if (jj_ntk != -1) jj_ntk else jj_ntk()) {
                        37 -> zexpression = IsNullClause()
                        else -> {
                            jj_la1[71] = jj_gen
                            jj_consume_token(-1)
                            throw ParseException()
                        }
                    }
                }
            }
            else -> jj_la1!![72] = jj_gen
        }
        if (zexpression == null) return obj
        var vector1: Vector? = zexpression.getOperands()
        if (vector1 == null) vector1 = Vector()
        vector1.insertElementAt(obj, 0)
        zexpression.setOperands(vector1)
        return zexpression
    }

    @Throws(ParseException::class)
    fun SQLExpressionList(): Vector? {
        val vector = Vector(8)
        val zexp: ZExp? = SQLSimpleExpressionOrPreparedCol()
        vector.addElement(zexp)
        label0@ do when (if (jj_ntk != -1) jj_ntk else jj_ntk()) {
            89 -> {
                jj_consume_token(89)
                val zexp1: ZExp? = SQLSimpleExpressionOrPreparedCol()
                vector.addElement(zexp1)
            }
            else -> {
                jj_la1!![73] = jj_gen
                break@label0
            }
        } while (true)
        return vector
    }

    @Throws(ParseException::class)
    fun SQLRelationalOperatorExpression(): ZExpression? {
        var s1: String? = null
        val s = Relop()
        val zexpression = ZExpression(s)
        val obj: Object?
        if (jj_2_11(0x7fffffff)) {
            when (if (jj_ntk != -1) jj_ntk else jj_ntk()) {
                5, 7 -> when (if (jj_ntk != -1) jj_ntk else jj_ntk()) {
                    5 -> {
                        jj_consume_token(5)
                        s1 = "ALL"
                    }
                    7 -> {
                        jj_consume_token(7)
                        s1 = "ANY"
                    }
                    else -> {
                        jj_la1!![74] = jj_gen
                        jj_consume_token(-1)
                        throw ParseException()
                    }
                }
                else -> jj_la1!![75] = jj_gen
            }
            jj_consume_token(88)
            val zquery: ZQuery? = SubQuery()
            jj_consume_token(90)
            if (s1 == null) obj = zquery else obj = ZExpression(s1, zquery)
        } else {
            when (if (jj_ntk != -1) jj_ntk else jj_ntk()) {
                10, 19, 40, 41, 47, 53, 64, 76, 82, 85, 86, 87, 88, 101, 102, 105 -> {
                    when (if (jj_ntk != -1) jj_ntk else jj_ntk()) {
                        53 -> {
                            jj_consume_token(53)
                            s1 = "PRIOR"
                        }
                        else -> jj_la1!![76] = jj_gen
                    }
                    val zexp: ZExp? = SQLSimpleExpressionOrPreparedCol()
                    if (s1 == null) obj = zexp else obj = ZExpression(s1, zexp)
                }
                else -> {
                    jj_la1!![77] = jj_gen
                    jj_consume_token(-1)
                    throw ParseException()
                }
            }
        }
        zexpression!!.addOperand(obj as ZExp?)
        return zexpression
    }

    @Throws(ParseException::class)
    fun SQLSimpleExpressionOrPreparedCol(): ZExp? {
        when (if (jj_ntk != -1) jj_ntk else jj_ntk()) {
            10, 19, 40, 41, 47, 64, 76, 82, 85, 86, 87, 88, 101, 102 -> {
                return SQLSimpleExpression()
            }
            105 -> {
                return PreparedCol()
            }
        }
        jj_la1!![78] = jj_gen
        jj_consume_token(-1)
        throw ParseException()
    }

    @Throws(ParseException::class)
    fun PreparedCol(): ZExp? {
        jj_consume_token(105)
        return ZExpression("?")
    }

    @Throws(ParseException::class)
    fun SQLInClause(): ZExpression? {
        var flag = false
        when (if (jj_ntk != -1) jj_ntk else jj_ntk()) {
            45 -> {
                jj_consume_token(45)
                flag = true
            }
            else -> jj_la1!![79] = jj_gen
        }
        jj_consume_token(32)
        val zexpression = ZExpression(if (flag) "NOT IN" else "IN")
        jj_consume_token(88)
        when (if (jj_ntk != -1) jj_ntk else jj_ntk()) {
            10, 19, 40, 41, 47, 64, 76, 82, 85, 86, 87, 88, 101, 102, 105 -> {
                val vector: Vector? = SQLExpressionList()
                zexpression.setOperands(vector)
            }
            59 -> {
                val zquery: ZQuery? = SubQuery()
                zexpression!!.addOperand(zquery)
            }
            else -> {
                jj_la1!![80] = jj_gen
                jj_consume_token(-1)
                throw ParseException()
            }
        }
        jj_consume_token(90)
        return zexpression
    }

    @Throws(ParseException::class)
    fun SQLBetweenClause(): ZExpression? {
        var flag = false
        when (if (jj_ntk != -1) jj_ntk else jj_ntk()) {
            45 -> {
                jj_consume_token(45)
                flag = true
            }
            else -> jj_la1!![81] = jj_gen
        }
        jj_consume_token(11)
        val zexp: ZExp? = SQLSimpleExpression()
        jj_consume_token(6)
        val zexp1: ZExp? = SQLSimpleExpression()
        val zexpression: ZExpression?
        if (flag) zexpression = ZExpression("NOT BETWEEN", zexp, zexp1) else zexpression = ZExpression("BETWEEN", zexp, zexp1)
        return zexpression
    }

    @Throws(ParseException::class)
    fun SQLLikeClause(): ZExpression? {
        var flag = false
        when (if (jj_ntk != -1) jj_ntk else jj_ntk()) {
            45 -> {
                jj_consume_token(45)
                flag = true
            }
            else -> jj_la1!![82] = jj_gen
        }
        jj_consume_token(38)
        val zexp: ZExp? = SQLSimpleExpression()
        val zexpression: ZExpression?
        if (flag) zexpression = ZExpression("NOT LIKE", zexp) else zexpression = ZExpression("LIKE", zexp)
        return zexpression
    }

    @Throws(ParseException::class)
    fun IsNullClause(): ZExpression? {
        var flag = false
        jj_consume_token(37)
        when (if (jj_ntk != -1) jj_ntk else jj_ntk()) {
            45 -> {
                jj_consume_token(45)
                flag = true
            }
            else -> jj_la1!![83] = jj_gen
        }
        jj_consume_token(47)
        return if (flag) ZExpression("IS NOT NULL") else ZExpression("IS NULL")
    }

    @Throws(ParseException::class)
    fun SQLSimpleExpression(): ZExp? {
        // Object obj1 = null;
        var obj: Object? = SQLMultiplicativeExpression()
        label0@ do when (if (jj_ntk != -1) jj_ntk else jj_ntk()) {
            101, 102, 106 -> {
                var token1: Token?
                when (if (jj_ntk != -1) jj_ntk else jj_ntk()) {
                    101 -> token1 = jj_consume_token(101)
                    102 -> token1 = jj_consume_token(102)
                    106 -> token1 = jj_consume_token(106)
                    else -> {
                        jj_la1!![85] = jj_gen
                        jj_consume_token(-1)
                        throw ParseException()
                    }
                }
                val zexp: ZExp? = SQLMultiplicativeExpression()
                val zexpression = ZExpression(token1.toString(), obj as ZExp?)
                zexpression!!.addOperand(zexp)
                obj = zexpression
            }
            else -> {
                jj_la1!![84] = jj_gen
                break@label0
            }
        } while (true)
        return obj
    }

    @Throws(ParseException::class)
    fun SQLMultiplicativeExpression(): ZExp? {
        // Object obj1 = null;
        var obj: Object? = SQLExpotentExpression()
        label0@ do when (if (jj_ntk != -1) jj_ntk else jj_ntk()) {
            103, 107 -> {
                var token1: Token?
                when (if (jj_ntk != -1) jj_ntk else jj_ntk()) {
                    103 -> token1 = jj_consume_token(103)
                    107 -> token1 = jj_consume_token(107)
                    else -> {
                        jj_la1!![87] = jj_gen
                        jj_consume_token(-1)
                        throw ParseException()
                    }
                }
                val zexp: ZExp? = SQLExpotentExpression()
                val zexpression = ZExpression(token1.toString(), obj as ZExp?)
                zexpression!!.addOperand(zexp)
                obj = zexpression
            }
            else -> {
                jj_la1!![86] = jj_gen
                break@label0
            }
        } while (true)
        return obj
    }

    @Throws(ParseException::class)
    fun SQLExpotentExpression(): ZExp? {
        var zexpression: ZExpression? = null
        var flag = true
        val zexp: ZExp? = SQLUnaryExpression()
        label0@ do when (if (jj_ntk != -1) jj_ntk else jj_ntk()) {
            108 -> {
                val token1: Token? = jj_consume_token(108)
                val zexp1: ZExp? = SQLUnaryExpression()
                if (flag) zexpression = ZExpression(token1.toString(), zexp)
                flag = false
                zexpression!!.addOperand(zexp1)
            }
            else -> {
                jj_la1!![88] = jj_gen
                break@label0
            }
        } while (true)
        return if (flag) zexp else zexpression
    }

    @Throws(ParseException::class)
    fun SQLUnaryExpression(): ZExp? {
        var token1: Token? = null
        when (if (jj_ntk != -1) jj_ntk else jj_ntk()) {
            101, 102 -> when (if (jj_ntk != -1) jj_ntk else jj_ntk()) {
                101 -> token1 = jj_consume_token(101)
                102 -> token1 = jj_consume_token(102)
                else -> {
                    jj_la1!![89] = jj_gen
                    jj_consume_token(-1)
                    throw ParseException()
                }
            }
            else -> jj_la1!![90] = jj_gen
        }
        val zexp: ZExp? = SQLPrimaryExpression()
        val obj: Object?
        if (token1 == null) obj = zexp else obj = ZExpression(token1.toString(), zexp)
        return obj
    }

    @Throws(ParseException::class)
    fun SQLPrimaryExpression(): ZExp? {
        var s4 = ""
        when (if (jj_ntk != -1) jj_ntk else jj_ntk()) {
            47 -> {
                jj_consume_token(47)
                return ZConstant("NULL", 1)
            }
        }
        jj_la1!![93] = jj_gen
        if (jj_2_12(0x7fffffff)) {
            OuterJoinExpression()
            return ZExpression("_NOT_SUPPORTED")
        }
        if (jj_2_13(3)) {
            jj_consume_token(19)
            jj_consume_token(88)
            jj_consume_token(103)
            jj_consume_token(90)
            return ZExpression("COUNT", ZConstant("*", 0))
        }
        if (jj_2_14(2)) {
            val s = AggregateFunc()
            jj_consume_token(88)
            when (if (jj_ntk != -1) jj_ntk else jj_ntk()) {
                5, 23 -> when (if (jj_ntk != -1) jj_ntk else jj_ntk()) {
                    5 -> {
                        jj_consume_token(5)
                        s4 = "all "
                    }
                    23 -> {
                        jj_consume_token(23)
                        s4 = "distinct "
                    }
                    else -> {
                        jj_la1[91] = jj_gen
                        jj_consume_token(-1)
                        throw ParseException()
                    }
                }
                else -> jj_la1[92] = jj_gen
            }
            val s3 = TableColumn()
            jj_consume_token(90)
            return ZExpression(s, ZConstant(s4 + s3, 0))
        }
        return if (jj_2_15(0x7fffffff)) {
            FunctionCall()
        } else when (if (jj_ntk != -1) jj_ntk else jj_ntk()) {
            82, 87 -> {
                val s1 = TableColumn()
                ZConstant(s1, 0)
            }
            76 -> {
                val token1: Token? = jj_consume_token(76)
                ZConstant(token1.toString(), 2)
            }
            86 -> {
                val token2: Token? = jj_consume_token(86)
                var s2: String? = token2.toString()
                if (s2.startsWith("'")) s2 = s2.substring(1)
                if (s2.endsWith("'")) s2 = s2.substring(0, s2!!.length() - 1)
                ZConstant(s2, 3)
            }
            85 -> {
                val token3: Token? = jj_consume_token(85)
                ZConstant(token3.toString(), 3)
            }
            88 -> {
                jj_consume_token(88)
                val zexp: ZExp? = SQLExpression()
                jj_consume_token(90)
                zexp
            }
            77, 78, 79, 80, 81, 83, 84 -> {
                jj_la1[94] = jj_gen
                jj_consume_token(-1)
                throw ParseException()
            }
            else -> {
                jj_la1[94] = jj_gen
                jj_consume_token(-1)
                throw ParseException()
            }
        }
    }

    @Throws(ParseException::class)
    fun AggregateFunc(): String? {
        when (if (jj_ntk != -1) jj_ntk else jj_ntk()) {
            64 -> {
                val token1: Token? = jj_consume_token(64)
                return token1.toString()
            }
            10 -> {
                val token2: Token? = jj_consume_token(10)
                return token2.toString()
            }
            40 -> {
                val token3: Token? = jj_consume_token(40)
                return token3.toString()
            }
            41 -> {
                val token4: Token? = jj_consume_token(41)
                return token4.toString()
            }
            19 -> {
                val token5: Token? = jj_consume_token(19)
                return token5.toString()
            }
        }
        jj_la1!![95] = jj_gen
        jj_consume_token(-1)
        throw ParseException()
    }

    @Throws(ParseException::class)
    fun FunctionCall(): ZExpression? {
        val token1: Token? = jj_consume_token(82)
        jj_consume_token(88)
        val vector: Vector? = SQLExpressionList()
        jj_consume_token(90)
        val i: Int = ZUtils.isCustomFunction(token1.toString())
        if (i <= 0) throw ParseException("Undefined function: " + token1.toString())
        if (false && vector.size() !== i) {
            throw ParseException("Function " + token1.toString().toString() + " should have " + i.toString() + " parameter(s)")
        }
        // else {
        val zexpression = ZExpression(token1.toString())
        zexpression.setOperands(vector)
        return zexpression
        // }
    }

    @Throws(ParseException::class)
    fun OuterJoinExpression() {
        OracleObjectName()
        when (if (jj_ntk != -1) jj_ntk else jj_ntk()) {
            93 -> {
                jj_consume_token(93)
                OracleObjectName()
                when (if (jj_ntk != -1) jj_ntk else jj_ntk()) {
                    93 -> {
                        jj_consume_token(93)
                        OracleObjectName()
                    }
                    else -> jj_la1!![96] = jj_gen
                }
            }
            else -> jj_la1!![97] = jj_gen
        }
        jj_consume_token(88)
        jj_consume_token(101)
        jj_consume_token(90)
    }

    @Throws(ParseException::class)
    fun SubQuery(): ZQuery? {
        return SelectWithoutOrder()
    }

    private fun jj_2_1(i: Int): Boolean {
        jj_la = i
        jj_scanpos = token
        jj_lastpos = jj_scanpos
        val flag = !jj_3_1()
        jj_save(0, i)
        return flag
    }

    private fun jj_2_2(i: Int): Boolean {
        jj_la = i
        jj_scanpos = token
        jj_lastpos = jj_scanpos
        val flag = !jj_3_2()
        jj_save(1, i)
        return flag
    }

    private fun jj_2_3(i: Int): Boolean {
        jj_la = i
        jj_scanpos = token
        jj_lastpos = jj_scanpos
        val flag = !jj_3_3()
        jj_save(2, i)
        return flag
    }

    private fun jj_2_4(i: Int): Boolean {
        jj_la = i
        jj_scanpos = token
        jj_lastpos = jj_scanpos
        val flag = !jj_3_4()
        jj_save(3, i)
        return flag
    }

    private fun jj_2_5(i: Int): Boolean {
        jj_la = i
        jj_scanpos = token
        jj_lastpos = jj_scanpos
        val flag = !jj_3_5()
        jj_save(4, i)
        return flag
    }

    private fun jj_2_6(i: Int): Boolean {
        jj_la = i
        jj_scanpos = token
        jj_lastpos = jj_scanpos
        val flag = !jj_3_6()
        jj_save(5, i)
        return flag
    }

    private fun jj_2_7(i: Int): Boolean {
        jj_la = i
        jj_scanpos = token
        jj_lastpos = jj_scanpos
        val flag = !jj_3_7()
        jj_save(6, i)
        return flag
    }

    private fun jj_2_8(i: Int): Boolean {
        jj_la = i
        jj_scanpos = token
        jj_lastpos = jj_scanpos
        val flag = !jj_3_8()
        jj_save(7, i)
        return flag
    }

    private fun jj_2_9(i: Int): Boolean {
        jj_la = i
        jj_scanpos = token
        jj_lastpos = jj_scanpos
        val flag = !jj_3_9()
        jj_save(8, i)
        return flag
    }

    private fun jj_2_10(i: Int): Boolean {
        jj_la = i
        jj_scanpos = token
        jj_lastpos = jj_scanpos
        val flag = !jj_3_10()
        jj_save(9, i)
        return flag
    }

    private fun jj_2_11(i: Int): Boolean {
        jj_la = i
        jj_scanpos = token
        jj_lastpos = jj_scanpos
        val flag = !jj_3_11()
        jj_save(10, i)
        return flag
    }

    private fun jj_2_12(i: Int): Boolean {
        jj_la = i
        jj_scanpos = token
        jj_lastpos = jj_scanpos
        val flag = !jj_3_12()
        jj_save(11, i)
        return flag
    }

    private fun jj_2_13(i: Int): Boolean {
        jj_la = i
        jj_scanpos = token
        jj_lastpos = jj_scanpos
        val flag = !jj_3_13()
        jj_save(12, i)
        return flag
    }

    private fun jj_2_14(i: Int): Boolean {
        jj_la = i
        jj_scanpos = token
        jj_lastpos = jj_scanpos
        val flag = !jj_3_14()
        jj_save(13, i)
        return flag
    }

    private fun jj_2_15(i: Int): Boolean {
        jj_la = i
        jj_scanpos = token
        jj_lastpos = jj_scanpos
        val flag = !jj_3_15()
        jj_save(14, i)
        return flag
    }

    private fun jj_3_7(): Boolean {
        if (jj_scan_token(88)) return true
        if (jj_la == 0 && jj_scanpos === jj_lastpos) return false
        if (jj_3R_20()) return true
        if (jj_la == 0 && jj_scanpos === jj_lastpos) return false
        if (jj_scan_token(89)) return true
        return if (jj_la != 0 || jj_scanpos !== jj_lastpos) false else false
    }

    private fun jj_3R_91(): Boolean {
        if (jj_scan_token(53)) return true
        return if (jj_la != 0 || jj_scanpos !== jj_lastpos) false else false
    }

    private fun jj_3R_88(): Boolean {
        val token1: Token? = jj_scanpos
        if (jj_3R_91()) jj_scanpos = token1 else if (jj_la == 0 && jj_scanpos === jj_lastpos) return false
        if (jj_3R_20()) return true
        return if (jj_la != 0 || jj_scanpos !== jj_lastpos) false else false
    }

    private fun jj_3R_87(): Boolean {
        if (jj_scan_token(88)) return true
        if (jj_la == 0 && jj_scanpos === jj_lastpos) return false
        if (jj_3R_72()) return true
        if (jj_la == 0 && jj_scanpos === jj_lastpos) return false
        if (jj_scan_token(90)) return true
        return if (jj_la != 0 || jj_scanpos !== jj_lastpos) false else false
    }

    private fun jj_3R_85(): Boolean {
        var token1: Token? = jj_scanpos
        if (jj_3R_87()) {
            jj_scanpos = token1
            if (jj_3R_88()) return true
            if (jj_la == 0 && jj_scanpos === jj_lastpos) return false
        } else if (jj_la == 0 && jj_scanpos === jj_lastpos) return false
        token1 = jj_scanpos
        if (jj_3R_89()) jj_scanpos = token1 else if (jj_la == 0 && jj_scanpos === jj_lastpos) return false
        return false
    }

    private fun jj_3R_16(): Boolean {
        if (jj_scan_token(88)) return true
        return if (jj_la != 0 || jj_scanpos !== jj_lastpos) false else false
    }

    private fun jj_3_1(): Boolean {
        if (jj_3R_16()) return true
        if (jj_la == 0 && jj_scanpos === jj_lastpos) return false
        do {
            val token1: Token? = jj_scanpos
            if (jj_3R_16()) {
                jj_scanpos = token1
                break
            }
            if (jj_la == 0 && jj_scanpos === jj_lastpos) return false
        } while (true)
        if (jj_scan_token(59)) return true
        return if (jj_la != 0 || jj_scanpos !== jj_lastpos) false else false
    }

    private fun jj_3R_31(): Boolean {
        if (jj_scan_token(45)) return true
        return if (jj_la != 0 || jj_scanpos !== jj_lastpos) false else false
    }

    private fun jj_3R_19(): Boolean {
        val token1: Token? = jj_scanpos
        if (jj_3R_31()) jj_scanpos = token1 else if (jj_la == 0 && jj_scanpos === jj_lastpos) return false
        if (jj_scan_token(25)) return true
        if (jj_la == 0 && jj_scanpos === jj_lastpos) return false
        if (jj_scan_token(88)) return true
        if (jj_la == 0 && jj_scanpos === jj_lastpos) return false
        if (jj_3R_86()) return true
        if (jj_la == 0 && jj_scanpos === jj_lastpos) return false
        if (jj_scan_token(90)) return true
        return if (jj_la != 0 || jj_scanpos !== jj_lastpos) false else false
    }

    private fun jj_3R_84(): Boolean {
        if (jj_scan_token(45)) return true
        return if (jj_la != 0 || jj_scanpos !== jj_lastpos) false else false
    }

    private fun jj_3R_82(): Boolean {
        val token1: Token? = jj_scanpos
        if (jj_3R_84()) jj_scanpos = token1 else if (jj_la == 0 && jj_scanpos === jj_lastpos) return false
        if (jj_3R_85()) return true
        return if (jj_la != 0 || jj_scanpos !== jj_lastpos) false else false
    }

    private fun jj_3_6(): Boolean {
        if (jj_3R_19()) return true
        return if (jj_la != 0 || jj_scanpos !== jj_lastpos) false else false
    }

    private fun jj_3R_78(): Boolean {
        val token1: Token? = jj_scanpos
        if (jj_3_6()) {
            jj_scanpos = token1
            if (jj_3R_82()) return true
            if (jj_la == 0 && jj_scanpos === jj_lastpos) return false
        } else if (jj_la == 0 && jj_scanpos === jj_lastpos) return false
        return false
    }

    private fun jj_3R_48(): Boolean {
        if (jj_scan_token(93)) return true
        if (jj_la == 0 && jj_scanpos === jj_lastpos) return false
        if (jj_3R_18()) return true
        return if (jj_la != 0 || jj_scanpos !== jj_lastpos) false else false
    }

    private fun jj_3R_79(): Boolean {
        if (jj_scan_token(6)) return true
        if (jj_la == 0 && jj_scanpos === jj_lastpos) return false
        if (jj_3R_78()) return true
        return if (jj_la != 0 || jj_scanpos !== jj_lastpos) false else false
    }

    private fun jj_3R_73(): Boolean {
        if (jj_3R_78()) return true
        if (jj_la == 0 && jj_scanpos === jj_lastpos) return false
        do {
            val token1: Token? = jj_scanpos
            if (jj_3R_79()) {
                jj_scanpos = token1
                break
            }
            if (jj_la == 0 && jj_scanpos === jj_lastpos) return false
        } while (true)
        return false
    }

    private fun jj_3R_128(): Boolean {
        if (jj_scan_token(42)) return true
        return if (jj_la != 0 || jj_scanpos !== jj_lastpos) false else false
    }

    private fun jj_3R_37(): Boolean {
        if (jj_scan_token(93)) return true
        if (jj_la == 0 && jj_scanpos === jj_lastpos) return false
        if (jj_3R_18()) return true
        if (jj_la == 0 && jj_scanpos === jj_lastpos) return false
        val token1: Token? = jj_scanpos
        if (jj_3R_48()) jj_scanpos = token1 else if (jj_la == 0 && jj_scanpos === jj_lastpos) return false
        return false
    }

    private fun jj_3R_74(): Boolean {
        if (jj_scan_token(51)) return true
        if (jj_la == 0 && jj_scanpos === jj_lastpos) return false
        if (jj_3R_73()) return true
        return if (jj_la != 0 || jj_scanpos !== jj_lastpos) false else false
    }

    private fun jj_3R_68(): Boolean {
        if (jj_3R_73()) return true
        if (jj_la == 0 && jj_scanpos === jj_lastpos) return false
        do {
            val token1: Token? = jj_scanpos
            if (jj_3R_74()) {
                jj_scanpos = token1
                break
            }
            if (jj_la == 0 && jj_scanpos === jj_lastpos) return false
        } while (true)
        return false
    }

    private fun jj_3R_86(): Boolean {
        if (jj_3R_90()) return true
        return if (jj_la != 0 || jj_scanpos !== jj_lastpos) false else false
    }

    private fun jj_3R_127(): Boolean {
        if (jj_scan_token(35)) return true
        return if (jj_la != 0 || jj_scanpos !== jj_lastpos) false else false
    }

    private fun jj_3R_27(): Boolean {
        if (jj_3R_18()) return true
        if (jj_la == 0 && jj_scanpos === jj_lastpos) return false
        val token1: Token? = jj_scanpos
        if (jj_3R_37()) jj_scanpos = token1 else if (jj_la == 0 && jj_scanpos === jj_lastpos) return false
        if (jj_scan_token(88)) return true
        if (jj_la == 0 && jj_scanpos === jj_lastpos) return false
        if (jj_scan_token(101)) return true
        if (jj_la == 0 && jj_scanpos === jj_lastpos) return false
        if (jj_scan_token(90)) return true
        return if (jj_la != 0 || jj_scanpos !== jj_lastpos) false else false
    }

    private fun jj_3R_144(): Boolean {
        if (jj_scan_token(5)) return true
        return if (jj_la != 0 || jj_scanpos !== jj_lastpos) false else false
    }

    private fun jj_3_5(): Boolean {
        if (jj_scan_token(88)) return true
        return if (jj_la != 0 || jj_scanpos !== jj_lastpos) false else false
    }

    private fun jj_3R_130(): Boolean {
        if (jj_3R_90()) return true
        return if (jj_la != 0 || jj_scanpos !== jj_lastpos) false else false
    }

    private fun jj_3R_129(): Boolean {
        if (jj_scan_token(88)) return true
        if (jj_la == 0 && jj_scanpos === jj_lastpos) return false
        if (jj_3R_90()) return true
        if (jj_la == 0 && jj_scanpos === jj_lastpos) return false
        if (jj_scan_token(90)) return true
        return if (jj_la != 0 || jj_scanpos !== jj_lastpos) false else false
    }

    private fun jj_3R_67(): Boolean {
        if (jj_scan_token(82)) return true
        if (jj_la == 0 && jj_scanpos === jj_lastpos) return false
        if (jj_scan_token(88)) return true
        if (jj_la == 0 && jj_scanpos === jj_lastpos) return false
        if (jj_3R_72()) return true
        if (jj_la == 0 && jj_scanpos === jj_lastpos) return false
        if (jj_scan_token(90)) return true
        return if (jj_la != 0 || jj_scanpos !== jj_lastpos) false else false
    }

    private fun jj_3R_126(): Boolean {
        if (jj_scan_token(67)) return true
        if (jj_la == 0 && jj_scanpos === jj_lastpos) return false
        val token1: Token? = jj_scanpos
        if (jj_3R_144()) jj_scanpos = token1 else if (jj_la == 0 && jj_scanpos === jj_lastpos) return false
        return false
    }

    private fun jj_3R_112(): Boolean {
        var token1: Token? = jj_scanpos
        if (jj_3R_126()) {
            jj_scanpos = token1
            if (jj_3R_127()) {
                jj_scanpos = token1
                if (jj_3R_128()) return true
                if (jj_la == 0 && jj_scanpos === jj_lastpos) return false
            } else if (jj_la == 0 && jj_scanpos === jj_lastpos) return false
        } else if (jj_la == 0 && jj_scanpos === jj_lastpos) return false
        token1 = jj_scanpos
        if (jj_3R_129()) {
            jj_scanpos = token1
            if (jj_3R_130()) return true
            if (jj_la == 0 && jj_scanpos === jj_lastpos) return false
        } else if (jj_la == 0 && jj_scanpos === jj_lastpos) return false
        return false
    }

    private fun jj_3R_70(): Boolean {
        if (jj_scan_token(23)) return true
        return if (jj_la != 0 || jj_scanpos !== jj_lastpos) false else false
    }

    private fun jj_3R_42(): Boolean {
        if (jj_scan_token(19)) return true
        return if (jj_la != 0 || jj_scanpos !== jj_lastpos) false else false
    }

    private fun jj_3R_41(): Boolean {
        if (jj_scan_token(41)) return true
        return if (jj_la != 0 || jj_scanpos !== jj_lastpos) false else false
    }

    private fun jj_3R_40(): Boolean {
        if (jj_scan_token(40)) return true
        return if (jj_la != 0 || jj_scanpos !== jj_lastpos) false else false
    }

    private fun jj_3R_39(): Boolean {
        if (jj_scan_token(10)) return true
        return if (jj_la != 0 || jj_scanpos !== jj_lastpos) false else false
    }

    private fun jj_3R_125(): Boolean {
        if (jj_scan_token(31)) return true
        if (jj_la == 0 && jj_scanpos === jj_lastpos) return false
        if (jj_3R_68()) return true
        return if (jj_la != 0 || jj_scanpos !== jj_lastpos) false else false
    }

    private fun jj_3R_38(): Boolean {
        if (jj_scan_token(64)) return true
        return if (jj_la != 0 || jj_scanpos !== jj_lastpos) false else false
    }

    private fun jj_3R_28(): Boolean {
        val token1: Token? = jj_scanpos
        if (jj_3R_38()) {
            jj_scanpos = token1
            if (jj_3R_39()) {
                jj_scanpos = token1
                if (jj_3R_40()) {
                    jj_scanpos = token1
                    if (jj_3R_41()) {
                        jj_scanpos = token1
                        if (jj_3R_42()) return true
                        if (jj_la == 0 && jj_scanpos === jj_lastpos) return false
                    } else if (jj_la == 0 && jj_scanpos === jj_lastpos) return false
                } else if (jj_la == 0 && jj_scanpos === jj_lastpos) return false
            } else if (jj_la == 0 && jj_scanpos === jj_lastpos) return false
        } else if (jj_la == 0 && jj_scanpos === jj_lastpos) return false
        return false
    }

    private fun jj_3R_111(): Boolean {
        if (jj_scan_token(30)) return true
        if (jj_la == 0 && jj_scanpos === jj_lastpos) return false
        if (jj_scan_token(14)) return true
        if (jj_la == 0 && jj_scanpos === jj_lastpos) return false
        if (jj_3R_72()) return true
        if (jj_la == 0 && jj_scanpos === jj_lastpos) return false
        val token1: Token? = jj_scanpos
        if (jj_3R_125()) jj_scanpos = token1 else if (jj_la == 0 && jj_scanpos === jj_lastpos) return false
        return false
    }

    private fun jj_3_15(): Boolean {
        if (jj_scan_token(82)) return true
        if (jj_la == 0 && jj_scanpos === jj_lastpos) return false
        if (jj_scan_token(88)) return true
        return if (jj_la != 0 || jj_scanpos !== jj_lastpos) false else false
    }

    private fun jj_3R_124(): Boolean {
        if (jj_scan_token(63)) return true
        if (jj_la == 0 && jj_scanpos === jj_lastpos) return false
        if (jj_scan_token(73)) return true
        if (jj_la == 0 && jj_scanpos === jj_lastpos) return false
        if (jj_3R_68()) return true
        return if (jj_la != 0 || jj_scanpos !== jj_lastpos) false else false
    }

    private fun jj_3R_64(): Boolean {
        if (jj_scan_token(88)) return true
        if (jj_la == 0 && jj_scanpos === jj_lastpos) return false
        if (jj_3R_68()) return true
        if (jj_la == 0 && jj_scanpos === jj_lastpos) return false
        if (jj_scan_token(90)) return true
        return if (jj_la != 0 || jj_scanpos !== jj_lastpos) false else false
    }

    private fun jj_3R_123(): Boolean {
        if (jj_scan_token(63)) return true
        if (jj_la == 0 && jj_scanpos === jj_lastpos) return false
        if (jj_scan_token(73)) return true
        if (jj_la == 0 && jj_scanpos === jj_lastpos) return false
        if (jj_3R_68()) return true
        return if (jj_la != 0 || jj_scanpos !== jj_lastpos) false else false
    }

    private fun jj_3R_63(): Boolean {
        if (jj_scan_token(85)) return true
        return if (jj_la != 0 || jj_scanpos !== jj_lastpos) false else false
    }

    private fun jj_3R_110(): Boolean {
        var token1: Token? = jj_scanpos
        if (jj_3R_123()) jj_scanpos = token1 else if (jj_la == 0 && jj_scanpos === jj_lastpos) return false
        if (jj_scan_token(18)) return true
        if (jj_la == 0 && jj_scanpos === jj_lastpos) return false
        if (jj_scan_token(14)) return true
        if (jj_la == 0 && jj_scanpos === jj_lastpos) return false
        if (jj_3R_68()) return true
        if (jj_la == 0 && jj_scanpos === jj_lastpos) return false
        token1 = jj_scanpos
        if (jj_3R_124()) jj_scanpos = token1 else if (jj_la == 0 && jj_scanpos === jj_lastpos) return false
        return false
    }

    private fun jj_3R_62(): Boolean {
        if (jj_scan_token(86)) return true
        return if (jj_la != 0 || jj_scanpos !== jj_lastpos) false else false
    }

    private fun jj_3R_61(): Boolean {
        if (jj_scan_token(76)) return true
        return if (jj_la != 0 || jj_scanpos !== jj_lastpos) false else false
    }

    private fun jj_3R_60(): Boolean {
        if (jj_3R_66()) return true
        return if (jj_la != 0 || jj_scanpos !== jj_lastpos) false else false
    }

    private fun jj_3R_69(): Boolean {
        if (jj_scan_token(5)) return true
        return if (jj_la != 0 || jj_scanpos !== jj_lastpos) false else false
    }

    private fun jj_3R_65(): Boolean {
        val token1: Token? = jj_scanpos
        if (jj_3R_69()) {
            jj_scanpos = token1
            if (jj_3R_70()) return true
            if (jj_la == 0 && jj_scanpos === jj_lastpos) return false
        } else if (jj_la == 0 && jj_scanpos === jj_lastpos) return false
        return false
    }

    private fun jj_3R_59(): Boolean {
        if (jj_3R_67()) return true
        return if (jj_la != 0 || jj_scanpos !== jj_lastpos) false else false
    }

    private fun jj_3_12(): Boolean {
        if (jj_3R_27()) return true
        return if (jj_la != 0 || jj_scanpos !== jj_lastpos) false else false
    }

    private fun jj_3R_109(): Boolean {
        if (jj_scan_token(72)) return true
        if (jj_la == 0 && jj_scanpos === jj_lastpos) return false
        if (jj_3R_68()) return true
        return if (jj_la != 0 || jj_scanpos !== jj_lastpos) false else false
    }

    private fun jj_3_14(): Boolean {
        if (jj_3R_28()) return true
        if (jj_la == 0 && jj_scanpos === jj_lastpos) return false
        if (jj_scan_token(88)) return true
        if (jj_la == 0 && jj_scanpos === jj_lastpos) return false
        val token1: Token? = jj_scanpos
        if (jj_3R_65()) jj_scanpos = token1 else if (jj_la == 0 && jj_scanpos === jj_lastpos) return false
        if (jj_3R_66()) return true
        if (jj_la == 0 && jj_scanpos === jj_lastpos) return false
        if (jj_scan_token(90)) return true
        return if (jj_la != 0 || jj_scanpos !== jj_lastpos) false else false
    }

    private fun jj_3_13(): Boolean {
        if (jj_scan_token(19)) return true
        if (jj_la == 0 && jj_scanpos === jj_lastpos) return false
        if (jj_scan_token(88)) return true
        if (jj_la == 0 && jj_scanpos === jj_lastpos) return false
        if (jj_scan_token(103)) return true
        if (jj_la == 0 && jj_scanpos === jj_lastpos) return false
        if (jj_scan_token(90)) return true
        return if (jj_la != 0 || jj_scanpos !== jj_lastpos) false else false
    }

    private fun jj_3R_58(): Boolean {
        if (jj_3R_27()) return true
        return if (jj_la != 0 || jj_scanpos !== jj_lastpos) false else false
    }

    private fun jj_3R_122(): Boolean {
        if (jj_scan_token(82)) return true
        return if (jj_la != 0 || jj_scanpos !== jj_lastpos) false else false
    }

    private fun jj_3R_57(): Boolean {
        if (jj_scan_token(47)) return true
        return if (jj_la != 0 || jj_scanpos !== jj_lastpos) false else false
    }

    private fun jj_3R_54(): Boolean {
        val token1: Token? = jj_scanpos
        if (jj_3R_57()) {
            jj_scanpos = token1
            if (jj_3R_58()) {
                jj_scanpos = token1
                if (jj_3_13()) {
                    jj_scanpos = token1
                    if (jj_3_14()) {
                        jj_scanpos = token1
                        if (jj_3R_59()) {
                            jj_scanpos = token1
                            if (jj_3R_60()) {
                                jj_scanpos = token1
                                if (jj_3R_61()) {
                                    jj_scanpos = token1
                                    if (jj_3R_62()) {
                                        jj_scanpos = token1
                                        if (jj_3R_63()) {
                                            jj_scanpos = token1
                                            if (jj_3R_64()) return true
                                            if (jj_la == 0 && jj_scanpos === jj_lastpos) return false
                                        } else if (jj_la == 0 && jj_scanpos === jj_lastpos) return false
                                    } else if (jj_la == 0 && jj_scanpos === jj_lastpos) return false
                                } else if (jj_la == 0 && jj_scanpos === jj_lastpos) return false
                            } else if (jj_la == 0 && jj_scanpos === jj_lastpos) return false
                        } else if (jj_la == 0 && jj_scanpos === jj_lastpos) return false
                    } else if (jj_la == 0 && jj_scanpos === jj_lastpos) return false
                } else if (jj_la == 0 && jj_scanpos === jj_lastpos) return false
            } else if (jj_la == 0 && jj_scanpos === jj_lastpos) return false
        } else if (jj_la == 0 && jj_scanpos === jj_lastpos) return false
        return false
    }

    private fun jj_3R_107(): Boolean {
        if (jj_3R_121()) return true
        if (jj_la == 0 && jj_scanpos === jj_lastpos) return false
        val token1: Token? = jj_scanpos
        if (jj_3R_122()) jj_scanpos = token1 else if (jj_la == 0 && jj_scanpos === jj_lastpos) return false
        return false
    }

    private fun jj_3R_108(): Boolean {
        if (jj_scan_token(89)) return true
        if (jj_la == 0 && jj_scanpos === jj_lastpos) return false
        if (jj_3R_107()) return true
        return if (jj_la != 0 || jj_scanpos !== jj_lastpos) false else false
    }

    private fun jj_3R_56(): Boolean {
        if (jj_scan_token(102)) return true
        return if (jj_la != 0 || jj_scanpos !== jj_lastpos) false else false
    }

    private fun jj_3R_96(): Boolean {
        if (jj_scan_token(29)) return true
        if (jj_la == 0 && jj_scanpos === jj_lastpos) return false
        if (jj_3R_107()) return true
        if (jj_la == 0 && jj_scanpos === jj_lastpos) return false
        do {
            val token1: Token? = jj_scanpos
            if (jj_3R_108()) {
                jj_scanpos = token1
                break
            }
            if (jj_la == 0 && jj_scanpos === jj_lastpos) return false
        } while (true)
        return false
    }

    private fun jj_3R_55(): Boolean {
        if (jj_scan_token(101)) return true
        return if (jj_la != 0 || jj_scanpos !== jj_lastpos) false else false
    }

    private fun jj_3R_53(): Boolean {
        val token1: Token? = jj_scanpos
        if (jj_3R_55()) {
            jj_scanpos = token1
            if (jj_3R_56()) return true
            if (jj_la == 0 && jj_scanpos === jj_lastpos) return false
        } else if (jj_la == 0 && jj_scanpos === jj_lastpos) return false
        return false
    }

    private fun jj_3R_49(): Boolean {
        val token1: Token? = jj_scanpos
        if (jj_3R_53()) jj_scanpos = token1 else if (jj_la == 0 && jj_scanpos === jj_lastpos) return false
        if (jj_3R_54()) return true
        return if (jj_la != 0 || jj_scanpos !== jj_lastpos) false else false
    }

    private fun jj_3_4(): Boolean {
        if (jj_3R_18()) return true
        if (jj_la == 0 && jj_scanpos === jj_lastpos) return false
        if (jj_scan_token(93)) return true
        if (jj_la == 0 && jj_scanpos === jj_lastpos) return false
        if (jj_3R_18()) return true
        if (jj_la == 0 && jj_scanpos === jj_lastpos) return false
        if (jj_scan_token(104)) return true
        return if (jj_la != 0 || jj_scanpos !== jj_lastpos) false else false
    }

    private fun jj_3R_17(): Boolean {
        val token1: Token? = jj_scanpos
        if (jj_3_3()) {
            jj_scanpos = token1
            if (jj_3_4()) return true
            if (jj_la == 0 && jj_scanpos === jj_lastpos) return false
        } else if (jj_la == 0 && jj_scanpos === jj_lastpos) return false
        return false
    }

    private fun jj_3_3(): Boolean {
        if (jj_3R_18()) return true
        if (jj_la == 0 && jj_scanpos === jj_lastpos) return false
        if (jj_scan_token(104)) return true
        return if (jj_la != 0 || jj_scanpos !== jj_lastpos) false else false
    }

    private fun jj_3R_150(): Boolean {
        if (jj_scan_token(82)) return true
        return if (jj_la != 0 || jj_scanpos !== jj_lastpos) false else false
    }

    private fun jj_3R_50(): Boolean {
        if (jj_scan_token(108)) return true
        if (jj_la == 0 && jj_scanpos === jj_lastpos) return false
        if (jj_3R_49()) return true
        return if (jj_la != 0 || jj_scanpos !== jj_lastpos) false else false
    }

    private fun jj_3R_149(): Boolean {
        if (jj_scan_token(8)) return true
        return if (jj_la != 0 || jj_scanpos !== jj_lastpos) false else false
    }

    private fun jj_3R_43(): Boolean {
        if (jj_3R_49()) return true
        if (jj_la == 0 && jj_scanpos === jj_lastpos) return false
        do {
            val token1: Token? = jj_scanpos
            if (jj_3R_50()) {
                jj_scanpos = token1
                break
            }
            if (jj_la == 0 && jj_scanpos === jj_lastpos) return false
        } while (true)
        return false
    }

    private fun jj_3R_148(): Boolean {
        val token1: Token? = jj_scanpos
        if (jj_3R_149()) jj_scanpos = token1 else if (jj_la == 0 && jj_scanpos === jj_lastpos) return false
        if (jj_3R_150()) return true
        if (jj_la == 0 && jj_scanpos === jj_lastpos) return false
        do {
            val token2: Token? = jj_scanpos
            if (jj_3R_150()) {
                jj_scanpos = token2
                break
            }
            if (jj_la == 0 && jj_scanpos === jj_lastpos) return false
        } while (true)
        return false
    }

    private fun jj_3R_52(): Boolean {
        if (jj_scan_token(107)) return true
        return if (jj_la != 0 || jj_scanpos !== jj_lastpos) false else false
    }

    private fun jj_3R_147(): Boolean {
        if (jj_3R_148()) return true
        return if (jj_la != 0 || jj_scanpos !== jj_lastpos) false else false
    }

    private fun jj_3_2(): Boolean {
        if (jj_3R_17()) return true
        return if (jj_la != 0 || jj_scanpos !== jj_lastpos) false else false
    }

    private fun jj_3R_47(): Boolean {
        if (jj_scan_token(106)) return true
        return if (jj_la != 0 || jj_scanpos !== jj_lastpos) false else false
    }

    private fun jj_3R_51(): Boolean {
        if (jj_scan_token(103)) return true
        return if (jj_la != 0 || jj_scanpos !== jj_lastpos) false else false
    }

    private fun jj_3R_44(): Boolean {
        val token1: Token? = jj_scanpos
        if (jj_3R_51()) {
            jj_scanpos = token1
            if (jj_3R_52()) return true
            if (jj_la == 0 && jj_scanpos === jj_lastpos) return false
        } else if (jj_la == 0 && jj_scanpos === jj_lastpos) return false
        if (jj_3R_43()) return true
        return if (jj_la != 0 || jj_scanpos !== jj_lastpos) false else false
    }

    private fun jj_3R_142(): Boolean {
        if (jj_3R_20()) return true
        if (jj_la == 0 && jj_scanpos === jj_lastpos) return false
        val token1: Token? = jj_scanpos
        if (jj_3R_147()) jj_scanpos = token1 else if (jj_la == 0 && jj_scanpos === jj_lastpos) return false
        return false
    }

    private fun jj_3R_32(): Boolean {
        if (jj_3R_43()) return true
        if (jj_la == 0 && jj_scanpos === jj_lastpos) return false
        do {
            val token1: Token? = jj_scanpos
            if (jj_3R_44()) {
                jj_scanpos = token1
                break
            }
            if (jj_la == 0 && jj_scanpos === jj_lastpos) return false
        } while (true)
        return false
    }

    private fun jj_3R_119(): Boolean {
        val token1: Token? = jj_scanpos
        if (jj_3R_141()) {
            jj_scanpos = token1
            if (jj_3R_142()) return true
            if (jj_la == 0 && jj_scanpos === jj_lastpos) return false
        } else if (jj_la == 0 && jj_scanpos === jj_lastpos) return false
        return false
    }

    private fun jj_3R_141(): Boolean {
        if (jj_3R_17()) return true
        return if (jj_la != 0 || jj_scanpos !== jj_lastpos) false else false
    }

    private fun jj_3R_46(): Boolean {
        if (jj_scan_token(102)) return true
        return if (jj_la != 0 || jj_scanpos !== jj_lastpos) false else false
    }

    private fun jj_3R_45(): Boolean {
        if (jj_scan_token(101)) return true
        return if (jj_la != 0 || jj_scanpos !== jj_lastpos) false else false
    }

    private fun jj_3R_120(): Boolean {
        if (jj_scan_token(89)) return true
        if (jj_la == 0 && jj_scanpos === jj_lastpos) return false
        if (jj_3R_119()) return true
        return if (jj_la != 0 || jj_scanpos !== jj_lastpos) false else false
    }

    private fun jj_3R_33(): Boolean {
        val token1: Token? = jj_scanpos
        if (jj_3R_45()) {
            jj_scanpos = token1
            if (jj_3R_46()) {
                jj_scanpos = token1
                if (jj_3R_47()) return true
                if (jj_la == 0 && jj_scanpos === jj_lastpos) return false
            } else if (jj_la == 0 && jj_scanpos === jj_lastpos) return false
        } else if (jj_la == 0 && jj_scanpos === jj_lastpos) return false
        if (jj_3R_32()) return true
        return if (jj_la != 0 || jj_scanpos !== jj_lastpos) false else false
    }

    private fun jj_3R_106(): Boolean {
        if (jj_3R_119()) return true
        if (jj_la == 0 && jj_scanpos === jj_lastpos) return false
        do {
            val token1: Token? = jj_scanpos
            if (jj_3R_120()) {
                jj_scanpos = token1
                break
            }
            if (jj_la == 0 && jj_scanpos === jj_lastpos) return false
        } while (true)
        return false
    }

    private fun jj_3R_20(): Boolean {
        if (jj_3R_32()) return true
        if (jj_la == 0 && jj_scanpos === jj_lastpos) return false
        do {
            val token1: Token? = jj_scanpos
            if (jj_3R_33()) {
                jj_scanpos = token1
                break
            }
            if (jj_la == 0 && jj_scanpos === jj_lastpos) return false
        } while (true)
        return false
    }

    private fun jj_3R_95(): Boolean {
        val token1: Token? = jj_scanpos
        if (jj_3R_105()) {
            jj_scanpos = token1
            if (jj_3R_106()) return true
            if (jj_la == 0 && jj_scanpos === jj_lastpos) return false
        } else if (jj_la == 0 && jj_scanpos === jj_lastpos) return false
        return false
    }

    private fun jj_3R_105(): Boolean {
        if (jj_scan_token(103)) return true
        return if (jj_la != 0 || jj_scanpos !== jj_lastpos) false else false
    }

    private fun jj_3R_118(): Boolean {
        if (jj_scan_token(45)) return true
        return if (jj_la != 0 || jj_scanpos !== jj_lastpos) false else false
    }

    private fun jj_3R_104(): Boolean {
        if (jj_scan_token(23)) return true
        return if (jj_la != 0 || jj_scanpos !== jj_lastpos) false else false
    }

    private fun jj_3R_102(): Boolean {
        if (jj_scan_token(37)) return true
        if (jj_la == 0 && jj_scanpos === jj_lastpos) return false
        val token1: Token? = jj_scanpos
        if (jj_3R_118()) jj_scanpos = token1 else if (jj_la == 0 && jj_scanpos === jj_lastpos) return false
        if (jj_scan_token(47)) return true
        return if (jj_la != 0 || jj_scanpos !== jj_lastpos) false else false
    }

    private fun jj_3R_94(): Boolean {
        val token1: Token? = jj_scanpos
        if (jj_3R_103()) {
            jj_scanpos = token1
            if (jj_3R_104()) return true
            if (jj_la == 0 && jj_scanpos === jj_lastpos) return false
        } else if (jj_la == 0 && jj_scanpos === jj_lastpos) return false
        return false
    }

    private fun jj_3R_103(): Boolean {
        if (jj_scan_token(5)) return true
        return if (jj_la != 0 || jj_scanpos !== jj_lastpos) false else false
    }

    private fun jj_3R_100(): Boolean {
        if (jj_3R_112()) return true
        return if (jj_la != 0 || jj_scanpos !== jj_lastpos) false else false
    }

    private fun jj_3R_99(): Boolean {
        if (jj_3R_111()) return true
        return if (jj_la != 0 || jj_scanpos !== jj_lastpos) false else false
    }

    private fun jj_3R_98(): Boolean {
        if (jj_3R_110()) return true
        return if (jj_la != 0 || jj_scanpos !== jj_lastpos) false else false
    }

    private fun jj_3R_97(): Boolean {
        if (jj_3R_109()) return true
        return if (jj_la != 0 || jj_scanpos !== jj_lastpos) false else false
    }

    private fun jj_3R_36(): Boolean {
        if (jj_scan_token(45)) return true
        return if (jj_la != 0 || jj_scanpos !== jj_lastpos) false else false
    }

    private fun jj_3R_23(): Boolean {
        val token1: Token? = jj_scanpos
        if (jj_3R_36()) jj_scanpos = token1 else if (jj_la == 0 && jj_scanpos === jj_lastpos) return false
        if (jj_scan_token(38)) return true
        if (jj_la == 0 && jj_scanpos === jj_lastpos) return false
        if (jj_3R_20()) return true
        return if (jj_la != 0 || jj_scanpos !== jj_lastpos) false else false
    }

    private fun jj_3R_90(): Boolean {
        if (jj_scan_token(59)) return true
        if (jj_la == 0 && jj_scanpos === jj_lastpos) return false
        var token1: Token? = jj_scanpos
        if (jj_3R_94()) jj_scanpos = token1 else if (jj_la == 0 && jj_scanpos === jj_lastpos) return false
        if (jj_3R_95()) return true
        if (jj_la == 0 && jj_scanpos === jj_lastpos) return false
        if (jj_3R_96()) return true
        if (jj_la == 0 && jj_scanpos === jj_lastpos) return false
        token1 = jj_scanpos
        if (jj_3R_97()) jj_scanpos = token1 else if (jj_la == 0 && jj_scanpos === jj_lastpos) return false
        token1 = jj_scanpos
        if (jj_3R_98()) jj_scanpos = token1 else if (jj_la == 0 && jj_scanpos === jj_lastpos) return false
        token1 = jj_scanpos
        if (jj_3R_99()) jj_scanpos = token1 else if (jj_la == 0 && jj_scanpos === jj_lastpos) return false
        token1 = jj_scanpos
        if (jj_3R_100()) jj_scanpos = token1 else if (jj_la == 0 && jj_scanpos === jj_lastpos) return false
        return false
    }

    private fun jj_3R_35(): Boolean {
        if (jj_scan_token(45)) return true
        return if (jj_la != 0 || jj_scanpos !== jj_lastpos) false else false
    }

    private fun jj_3R_22(): Boolean {
        val token1: Token? = jj_scanpos
        if (jj_3R_35()) jj_scanpos = token1 else if (jj_la == 0 && jj_scanpos === jj_lastpos) return false
        if (jj_scan_token(11)) return true
        if (jj_la == 0 && jj_scanpos === jj_lastpos) return false
        if (jj_3R_20()) return true
        if (jj_la == 0 && jj_scanpos === jj_lastpos) return false
        if (jj_scan_token(6)) return true
        if (jj_la == 0 && jj_scanpos === jj_lastpos) return false
        if (jj_3R_20()) return true
        return if (jj_la != 0 || jj_scanpos !== jj_lastpos) false else false
    }

    private fun jj_3R_30(): Boolean {
        if (jj_scan_token(87)) return true
        return if (jj_la != 0 || jj_scanpos !== jj_lastpos) false else false
    }

    private fun jj_3R_116(): Boolean {
        if (jj_3R_72()) return true
        return if (jj_la != 0 || jj_scanpos !== jj_lastpos) false else false
    }

    private fun jj_3R_117(): Boolean {
        if (jj_3R_86()) return true
        return if (jj_la != 0 || jj_scanpos !== jj_lastpos) false else false
    }

    private fun jj_3R_34(): Boolean {
        if (jj_scan_token(45)) return true
        return if (jj_la != 0 || jj_scanpos !== jj_lastpos) false else false
    }

    private fun jj_3R_21(): Boolean {
        var token1: Token? = jj_scanpos
        if (jj_3R_34()) jj_scanpos = token1 else if (jj_la == 0 && jj_scanpos === jj_lastpos) return false
        if (jj_scan_token(32)) return true
        if (jj_la == 0 && jj_scanpos === jj_lastpos) return false
        if (jj_scan_token(88)) return true
        if (jj_la == 0 && jj_scanpos === jj_lastpos) return false
        token1 = jj_scanpos
        if (jj_3R_116()) {
            jj_scanpos = token1
            if (jj_3R_117()) return true
            if (jj_la == 0 && jj_scanpos === jj_lastpos) return false
        } else if (jj_la == 0 && jj_scanpos === jj_lastpos) return false
        if (jj_scan_token(90)) return true
        return if (jj_la != 0 || jj_scanpos !== jj_lastpos) false else false
    }

    private fun jj_3R_146(): Boolean {
        if (jj_scan_token(7)) return true
        return if (jj_la != 0 || jj_scanpos !== jj_lastpos) false else false
    }

    private fun jj_3R_143(): Boolean {
        if (jj_scan_token(93)) return true
        if (jj_la == 0 && jj_scanpos === jj_lastpos) return false
        if (jj_3R_18()) return true
        return if (jj_la != 0 || jj_scanpos !== jj_lastpos) false else false
    }

    private fun jj_3R_26(): Boolean {
        if (jj_scan_token(88)) return true
        if (jj_la == 0 && jj_scanpos === jj_lastpos) return false
        if (jj_scan_token(59)) return true
        return if (jj_la != 0 || jj_scanpos !== jj_lastpos) false else false
    }

    private fun jj_3R_121(): Boolean {
        if (jj_3R_18()) return true
        if (jj_la == 0 && jj_scanpos === jj_lastpos) return false
        val token1: Token? = jj_scanpos
        if (jj_3R_143()) jj_scanpos = token1 else if (jj_la == 0 && jj_scanpos === jj_lastpos) return false
        return false
    }

    private fun jj_3R_83(): Boolean {
        if (jj_scan_token(105)) return true
        return if (jj_la != 0 || jj_scanpos !== jj_lastpos) false else false
    }

    private fun jj_3R_25(): Boolean {
        if (jj_scan_token(5)) return true
        return if (jj_la != 0 || jj_scanpos !== jj_lastpos) false else false
    }

    private fun jj_3R_81(): Boolean {
        if (jj_3R_83()) return true
        return if (jj_la != 0 || jj_scanpos !== jj_lastpos) false else false
    }

    private fun jj_3R_76(): Boolean {
        val token1: Token? = jj_scanpos
        if (jj_3R_80()) {
            jj_scanpos = token1
            if (jj_3R_81()) return true
            if (jj_la == 0 && jj_scanpos === jj_lastpos) return false
        } else if (jj_la == 0 && jj_scanpos === jj_lastpos) return false
        return false
    }

    private fun jj_3R_80(): Boolean {
        if (jj_3R_20()) return true
        return if (jj_la != 0 || jj_scanpos !== jj_lastpos) false else false
    }

    private fun jj_3R_138(): Boolean {
        if (jj_scan_token(100)) return true
        return if (jj_la != 0 || jj_scanpos !== jj_lastpos) false else false
    }

    private fun jj_3R_137(): Boolean {
        if (jj_scan_token(99)) return true
        return if (jj_la != 0 || jj_scanpos !== jj_lastpos) false else false
    }

    private fun jj_3R_136(): Boolean {
        if (jj_scan_token(98)) return true
        return if (jj_la != 0 || jj_scanpos !== jj_lastpos) false else false
    }

    private fun jj_3R_135(): Boolean {
        if (jj_scan_token(97)) return true
        return if (jj_la != 0 || jj_scanpos !== jj_lastpos) false else false
    }

    private fun jj_3R_134(): Boolean {
        if (jj_scan_token(96)) return true
        return if (jj_la != 0 || jj_scanpos !== jj_lastpos) false else false
    }

    private fun jj_3_11(): Boolean {
        val token1: Token? = jj_scanpos
        if (jj_3R_24()) {
            jj_scanpos = token1
            if (jj_3R_25()) {
                jj_scanpos = token1
                if (jj_3R_26()) return true
                if (jj_la == 0 && jj_scanpos === jj_lastpos) return false
            } else if (jj_la == 0 && jj_scanpos === jj_lastpos) return false
        } else if (jj_la == 0 && jj_scanpos === jj_lastpos) return false
        return false
    }

    private fun jj_3R_24(): Boolean {
        if (jj_scan_token(7)) return true
        return if (jj_la != 0 || jj_scanpos !== jj_lastpos) false else false
    }

    private fun jj_3R_133(): Boolean {
        if (jj_scan_token(95)) return true
        return if (jj_la != 0 || jj_scanpos !== jj_lastpos) false else false
    }

    private fun jj_3R_140(): Boolean {
        if (jj_scan_token(53)) return true
        return if (jj_la != 0 || jj_scanpos !== jj_lastpos) false else false
    }

    private fun jj_3R_132(): Boolean {
        if (jj_scan_token(94)) return true
        return if (jj_la != 0 || jj_scanpos !== jj_lastpos) false else false
    }

    private fun jj_3R_115(): Boolean {
        val token1: Token? = jj_scanpos
        if (jj_3R_140()) jj_scanpos = token1 else if (jj_la == 0 && jj_scanpos === jj_lastpos) return false
        if (jj_3R_76()) return true
        return if (jj_la != 0 || jj_scanpos !== jj_lastpos) false else false
    }

    private fun jj_3R_113(): Boolean {
        val token1: Token? = jj_scanpos
        if (jj_3R_131()) {
            jj_scanpos = token1
            if (jj_3R_132()) {
                jj_scanpos = token1
                if (jj_3R_133()) {
                    jj_scanpos = token1
                    if (jj_3R_134()) {
                        jj_scanpos = token1
                        if (jj_3R_135()) {
                            jj_scanpos = token1
                            if (jj_3R_136()) {
                                jj_scanpos = token1
                                if (jj_3R_137()) {
                                    jj_scanpos = token1
                                    if (jj_3R_138()) return true
                                    if (jj_la == 0 && jj_scanpos === jj_lastpos) return false
                                } else if (jj_la == 0 && jj_scanpos === jj_lastpos) return false
                            } else if (jj_la == 0 && jj_scanpos === jj_lastpos) return false
                        } else if (jj_la == 0 && jj_scanpos === jj_lastpos) return false
                    } else if (jj_la == 0 && jj_scanpos === jj_lastpos) return false
                } else if (jj_la == 0 && jj_scanpos === jj_lastpos) return false
            } else if (jj_la == 0 && jj_scanpos === jj_lastpos) return false
        } else if (jj_la == 0 && jj_scanpos === jj_lastpos) return false
        return false
    }

    private fun jj_3R_131(): Boolean {
        if (jj_scan_token(92)) return true
        return if (jj_la != 0 || jj_scanpos !== jj_lastpos) false else false
    }

    private fun jj_3R_145(): Boolean {
        if (jj_scan_token(5)) return true
        return if (jj_la != 0 || jj_scanpos !== jj_lastpos) false else false
    }

    private fun jj_3R_139(): Boolean {
        val token1: Token? = jj_scanpos
        if (jj_3R_145()) {
            jj_scanpos = token1
            if (jj_3R_146()) return true
            if (jj_la == 0 && jj_scanpos === jj_lastpos) return false
        } else if (jj_la == 0 && jj_scanpos === jj_lastpos) return false
        return false
    }

    private fun jj_3R_18(): Boolean {
        val token1: Token? = jj_scanpos
        if (jj_3R_29()) {
            jj_scanpos = token1
            if (jj_3R_30()) return true
            if (jj_la == 0 && jj_scanpos === jj_lastpos) return false
        } else if (jj_la == 0 && jj_scanpos === jj_lastpos) return false
        return false
    }

    private fun jj_3R_29(): Boolean {
        if (jj_scan_token(82)) return true
        return if (jj_la != 0 || jj_scanpos !== jj_lastpos) false else false
    }

    private fun jj_3R_114(): Boolean {
        val token1: Token? = jj_scanpos
        if (jj_3R_139()) jj_scanpos = token1 else if (jj_la == 0 && jj_scanpos === jj_lastpos) return false
        if (jj_scan_token(88)) return true
        if (jj_la == 0 && jj_scanpos === jj_lastpos) return false
        if (jj_3R_86()) return true
        if (jj_la == 0 && jj_scanpos === jj_lastpos) return false
        if (jj_scan_token(90)) return true
        return if (jj_la != 0 || jj_scanpos !== jj_lastpos) false else false
    }

    private fun jj_3R_75(): Boolean {
        if (jj_scan_token(93)) return true
        if (jj_la == 0 && jj_scanpos === jj_lastpos) return false
        if (jj_3R_18()) return true
        return if (jj_la != 0 || jj_scanpos !== jj_lastpos) false else false
    }

    private fun jj_3R_71(): Boolean {
        if (jj_scan_token(93)) return true
        if (jj_la == 0 && jj_scanpos === jj_lastpos) return false
        if (jj_3R_18()) return true
        if (jj_la == 0 && jj_scanpos === jj_lastpos) return false
        val token1: Token? = jj_scanpos
        if (jj_3R_75()) jj_scanpos = token1 else if (jj_la == 0 && jj_scanpos === jj_lastpos) return false
        return false
    }

    private fun jj_3R_101(): Boolean {
        if (jj_3R_113()) return true
        if (jj_la == 0 && jj_scanpos === jj_lastpos) return false
        val token1: Token? = jj_scanpos
        if (jj_3R_114()) {
            jj_scanpos = token1
            if (jj_3R_115()) return true
            if (jj_la == 0 && jj_scanpos === jj_lastpos) return false
        } else if (jj_la == 0 && jj_scanpos === jj_lastpos) return false
        return false
    }

    private fun jj_3R_66(): Boolean {
        if (jj_3R_18()) return true
        if (jj_la == 0 && jj_scanpos === jj_lastpos) return false
        val token1: Token? = jj_scanpos
        if (jj_3R_71()) jj_scanpos = token1 else if (jj_la == 0 && jj_scanpos === jj_lastpos) return false
        return false
    }

    private fun jj_3R_77(): Boolean {
        if (jj_scan_token(89)) return true
        if (jj_la == 0 && jj_scanpos === jj_lastpos) return false
        if (jj_3R_76()) return true
        return if (jj_la != 0 || jj_scanpos !== jj_lastpos) false else false
    }

    private fun jj_3R_72(): Boolean {
        if (jj_3R_76()) return true
        if (jj_la == 0 && jj_scanpos === jj_lastpos) return false
        do {
            val token1: Token? = jj_scanpos
            if (jj_3R_77()) {
                jj_scanpos = token1
                break
            }
            if (jj_la == 0 && jj_scanpos === jj_lastpos) return false
        } while (true)
        return false
    }

    private fun jj_3R_93(): Boolean {
        if (jj_3R_102()) return true
        return if (jj_la != 0 || jj_scanpos !== jj_lastpos) false else false
    }

    private fun jj_3_10(): Boolean {
        if (jj_3R_23()) return true
        return if (jj_la != 0 || jj_scanpos !== jj_lastpos) false else false
    }

    private fun jj_3_9(): Boolean {
        if (jj_3R_22()) return true
        return if (jj_la != 0 || jj_scanpos !== jj_lastpos) false else false
    }

    private fun jj_3_8(): Boolean {
        if (jj_3R_21()) return true
        return if (jj_la != 0 || jj_scanpos !== jj_lastpos) false else false
    }

    private fun jj_3R_89(): Boolean {
        val token1: Token? = jj_scanpos
        if (jj_3R_92()) {
            jj_scanpos = token1
            if (jj_3_8()) {
                jj_scanpos = token1
                if (jj_3_9()) {
                    jj_scanpos = token1
                    if (jj_3_10()) {
                        jj_scanpos = token1
                        if (jj_3R_93()) return true
                        if (jj_la == 0 && jj_scanpos === jj_lastpos) return false
                    } else if (jj_la == 0 && jj_scanpos === jj_lastpos) return false
                } else if (jj_la == 0 && jj_scanpos === jj_lastpos) return false
            } else if (jj_la == 0 && jj_scanpos === jj_lastpos) return false
        } else if (jj_la == 0 && jj_scanpos === jj_lastpos) return false
        return false
    }

    private fun jj_3R_92(): Boolean {
        if (jj_3R_101()) return true
        return if (jj_la != 0 || jj_scanpos !== jj_lastpos) false else false
    }

    constructor(inputstream: InputStream?) {
        lookingAhead = false
        jj_la1 = IntArray(98)
        jj_2_rtns = arrayOfNulls<JJCalls?>(15)
        jj_rescan = false
        jj_gc = 0
        jj_expentries = Vector()
        jj_kind = -1
        jj_lasttokens = IntArray(100)
        jj_input_stream = SimpleCharStream(inputstream, 1, 1)
        token_source = ZqlJJParserTokenManager(jj_input_stream)
        token = Token()
        jj_ntk = -1
        jj_gen = 0
        for (i in 0..97) jj_la1[i] = -1
        for (j in jj_2_rtns.indices) jj_2_rtns[j] = JJCalls()
    }

    fun ReInit(inputstream: InputStream?) {
        jj_input_stream.ReInit(inputstream, 1, 1)
        token_source!!.ReInit(jj_input_stream)
        token = Token()
        jj_ntk = -1
        jj_gen = 0
        for (i in 0..97) jj_la1!![i] = -1
        for (j in jj_2_rtns.indices) jj_2_rtns!![j] = JJCalls()
    }

    constructor(reader: Reader?) {
        lookingAhead = false
        jj_la1 = IntArray(98)
        jj_2_rtns = arrayOfNulls<JJCalls?>(15)
        jj_rescan = false
        jj_gc = 0
        jj_expentries = Vector()
        jj_kind = -1
        jj_lasttokens = IntArray(100)
        jj_input_stream = SimpleCharStream(reader, 1, 1)
        token_source = ZqlJJParserTokenManager(jj_input_stream)
        token = Token()
        jj_ntk = -1
        jj_gen = 0
        for (i in 0..97) jj_la1[i] = -1
        for (j in jj_2_rtns.indices) jj_2_rtns[j] = JJCalls()
    }

    fun ReInit(reader: Reader?) {
        jj_input_stream.ReInit(reader, 1, 1)
        token_source!!.ReInit(jj_input_stream)
        token = Token()
        jj_ntk = -1
        jj_gen = 0
        for (i in 0..97) jj_la1!![i] = -1
        for (j in jj_2_rtns.indices) jj_2_rtns!![j] = JJCalls()
    }

    constructor(zqljjparsertokenmanager: ZqlJJParserTokenManager?) {
        lookingAhead = false
        jj_la1 = IntArray(98)
        jj_2_rtns = arrayOfNulls<JJCalls?>(15)
        jj_rescan = false
        jj_gc = 0
        jj_expentries = Vector()
        jj_kind = -1
        jj_lasttokens = IntArray(100)
        token_source = zqljjparsertokenmanager
        token = Token()
        jj_ntk = -1
        jj_gen = 0
        for (i in 0..97) jj_la1[i] = -1
        for (j in jj_2_rtns.indices) jj_2_rtns[j] = JJCalls()
    }

    fun ReInit(zqljjparsertokenmanager: ZqlJJParserTokenManager?) {
        token_source = zqljjparsertokenmanager
        token = Token()
        jj_ntk = -1
        jj_gen = 0
        for (i in 0..97) jj_la1!![i] = -1
        for (j in jj_2_rtns.indices) jj_2_rtns!![j] = JJCalls()
    }

    @Throws(ParseException::class)
    private fun jj_consume_token(i: Int): Token? {

        // System. out.println("char:"+((char)i));
        var token1: Token?
        if (token.also { token1 = it }.next != null) token = token!!.next else {
            token!!.next = token_source.getNextToken()
            token = token!!.next
        }
        jj_ntk = -1
        // print.out("img:"+token.image+"+"+token.kind);
        if (token!!.kind === i) {
            jj_gen++
            if (++jj_gc > 100) {
                jj_gc = 0
                for (j in jj_2_rtns.indices) {
                    var jjcalls = jj_2_rtns!![j]
                    while (jjcalls != null) {
                        if (jjcalls.gen < jj_gen) jjcalls.first = null
                        jjcalls = jjcalls.next
                    }
                }
            }
            return token
        }
        // else{
        token = token1
        jj_kind = i
        throw generateParseException()
        // }
    }

    private fun jj_scan_token(i: Int): Boolean {
        if (jj_scanpos === jj_lastpos) {
            jj_la--
            if (jj_scanpos!!.next == null) {
                jj_scanpos!!.next = token_source.getNextToken()
                jj_scanpos = jj_scanpos!!.next
                jj_lastpos = jj_scanpos
            } else {
                jj_scanpos = jj_scanpos!!.next
                jj_lastpos = jj_scanpos
            }
        } else {
            jj_scanpos = jj_scanpos!!.next
        }
        if (jj_rescan) {
            var j = 0
            var token1: Token?
            token1 = token
            while (token1 != null && token1 !== jj_scanpos) {
                j++
                token1 = token1.next
            }
            if (token1 != null) jj_add_error_token(i, j)
        }
        return jj_scanpos!!.kind !== i
    }

    val nextToken: lucee.runtime.sql.old.Token?
        get() {
            if (token!!.next != null) token = token!!.next else {
                token!!.next = token_source.getNextToken()
                token = token!!.next
            }
            jj_ntk = -1
            jj_gen++
            return token
        }

    fun getToken(i: Int): Token? {
        var token1: Token = if (lookingAhead) jj_scanpos else token
        for (j in 0 until i) if (token1!!.next != null) token1 = token1!!.next else {
            token1!!.next = token_source.getNextToken()
            token1 = token1!!.next
        }
        return token1
    }

    private fun jj_ntk(): Int {
        if (token!!.next.also { jj_nt = it } == null) {
            token!!.next = token_source.getNextToken()
            return token!!.next!!.kind.also { jj_ntk = it }
        }
        return jj_nt!!.kind.also { jj_ntk = it }
    }

    private fun jj_add_error_token(i: Int, j: Int) {
        if (j >= 100) return
        if (j == jj_endpos + 1) jj_lasttokens!![jj_endpos++] = i else if (jj_endpos != 0) {
            jj_expentry = IntArray(jj_endpos)
            for (k in 0 until jj_endpos) jj_expentry!![k] = jj_lasttokens!![k]
            var flag = false
            val enumeration: Enumeration = jj_expentries.elements()
            while (enumeration.hasMoreElements()) {
                val ai = enumeration.nextElement() as IntArray
                if (ai.size != jj_expentry!!.size) continue
                flag = true
                for (l in jj_expentry.indices) {
                    if (ai[l] == jj_expentry!![l]) continue
                    flag = false
                    break
                }
                if (flag) break
            }
            if (!flag) jj_expentries.addElement(jj_expentry)
            if (j != 0) jj_lasttokens!![j.also { jj_endpos = it } - 1] = i
        }
    }

    fun generateParseException(): ParseException? {
        jj_expentries.removeAllElements()
        val aflag = BooleanArray(109)
        for (i in 0..108) aflag[i] = false
        if (jj_kind >= 0) {
            aflag[jj_kind] = true
            jj_kind = -1
        }
        for (j in 0..97) if (jj_la1!![j] == jj_gen) {
            for (k in 0..31) {
                if (jj_la1_0!![j] and 1 shl k != 0) aflag[k] = true
                if (jj_la1_1!![j] and 1 shl k != 0) aflag[32 + k] = true
                if (jj_la1_2!![j] and 1 shl k != 0) aflag[64 + k] = true
                if (jj_la1_3!![j] and 1 shl k != 0) aflag[96 + k] = true
            }
        }
        for (l in 0..108) if (aflag[l]) {
            jj_expentry = IntArray(1)
            jj_expentry!![0] = l
            jj_expentries.addElement(jj_expentry)
        }
        jj_endpos = 0
        jj_rescan_token()
        jj_add_error_token(0, 0)
        val ai = arrayOfNulls<IntArray?>(jj_expentries.size())
        for (i1 in 0 until jj_expentries.size()) ai[i1] = jj_expentries.elementAt(i1) as IntArray
        return ParseException(token, ai, ZqlJJParserConstants.tokenImage)
    }

    fun enable_tracing() {}
    fun disable_tracing() {}
    private fun jj_rescan_token() {
        jj_rescan = true
        for (i in 0..14) {
            var jjcalls = jj_2_rtns!![i]
            do {
                if (jjcalls!!.gen > jj_gen) {
                    jj_la = jjcalls.arg
                    jj_scanpos = jjcalls.first
                    jj_lastpos = jj_scanpos
                    when (i) {
                        0 -> jj_3_1()
                        1 -> jj_3_2()
                        2 -> jj_3_3()
                        3 -> jj_3_4()
                        4 -> jj_3_5()
                        5 -> jj_3_6()
                        6 -> jj_3_7()
                        7 -> jj_3_8()
                        8 -> jj_3_9()
                        9 -> jj_3_10()
                        10 -> jj_3_11()
                        11 -> jj_3_12()
                        12 -> jj_3_13()
                        13 -> jj_3_14()
                        14 -> jj_3_15()
                    }
                }
                jjcalls = jjcalls.next
            } while (jjcalls != null)
        }
        jj_rescan = false
    }

    private fun jj_save(i: Int, j: Int) {
        var jjcalls: JJCalls?
        jjcalls = jj_2_rtns!![i]
        while (jjcalls!!.gen > jj_gen) {
            if (jjcalls.next != null) {
                jjcalls = jjcalls.next
                continue
            }
            jjcalls.next = JJCalls()
            jjcalls = jjcalls.next
            break
            jjcalls = jjcalls!!.next
        }
        jjcalls.gen = jj_gen + j - jj_la
        jjcalls.first = token
        jjcalls.arg = j
    }

    var token_source: ZqlJJParserTokenManager?
    var jj_input_stream: SimpleCharStream? = null
    var token: Token?
    var jj_nt: Token? = null
    private var jj_ntk: Int
    private var jj_scanpos: Token? = null
    private var jj_lastpos: Token? = null
    private var jj_la = 0
    var lookingAhead: Boolean

    // private boolean jj_semLA;
    private var jj_gen: Int
    private val jj_la1: IntArray?
    private val jj_la1_0: IntArray? = intArrayOf(0x8008000, 0, 0, 0x810b000, 0x4220000, 0x4000000, 0x4220000, 0, 0x10000, 0, 0, 0, 0x10000, 0, 0x1000000, 0, 0, 0x1000000, 0, 0, 0x2080400, 0,
            0, 0, 0x20000000, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0x10000000, 0x800020, 0x800020, 0, 0x40000, 0x40000000, 0, 0, 0x80400, 256, 0x80400, 256, 0, 0, 0, 0, 0, -0x80000000, 32,
            0, 0, 0x400200, 0x400200, 0, 0x400200, 0x400200, 0, 0, 0, 64, 0, 0x80400, 0, 0, 0x80400, 0, 0, 2048, 0, 160, 160, 0, 0x80400, 0x80400, 0, 0x80400, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0x800020, 0x800020, 0, 0, 0x80400, 0, 0)
    private val jj_la1_1: IntArray? = intArrayOf(0x1011004, 0, 0, 0x1011004, 0x1a400082, 0x400000, 0x1a400082, 0, 0, 0, 16384, 0, 0, 0x40000, 0x20000000, 0x4000000, 0x4000000, 0x24000000, 0,
            0, 0x20a300, 0, 0, 0x8000000, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0x100000, 0, 0, 0, 0, -0x80000000, 0, 1032, 0, 33536, 0, 33536, 0, 0, 0, 0, -0x80000000, -0x80000000, 0, 0,
            1032, 0x8000000, 0, 0, 0, 0, 0, 0, 0x20000, 0x80000, 0, 8192, 0x20a300, 8192, 0x200000, 0x208300, 0, 32, 8289, 0, 0, 0, 0x200000, 0x208300, 33536, 8192, 0x8008300,
            8192, 8192, 8192, 0, 0, 0, 0, 0, 0, 0, 0, 0, 32768, 0, 768, 0, 0)
    private val jj_la1_2: IntArray? = intArrayOf(192, 0x2000000, 0x1000000, 192, 16, 0, 16, 1024, 0, 0x2000000, 0, 1024, 0, 2048, 0, 16, 16, 0, 256, 0x2000000, 0x1e41001, 0x2000000, 0x1000000,
            32, 0, 256, 0x20000000, 0x20000000, 0x840000, -0x30000000, 0x20000000, 0, 0, 0x41000, 0, 0, 0, 0, 256, 0, 0, 8, 0x2000000, 0x1e41001, 0x40000, 0x1e41001, 0, 0x40000,
            0x2000000, 0x40000, 0, 0, 0, 0, 8, 0, 0, 0, 0x2000000, 0, 0, 0x2000000, 0, 0, 0, 0, 0x1e41001, 0, 0, 0x1e41001, -0x30000000, 0, -0x30000000, 0x2000000, 0, 0, 0,
            0x1e41001, 0x1e41001, 0, 0x1e41001, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0x1e41000, 1, 0x20000000, 0x20000000)
    private val jj_la1_3: IntArray? = intArrayOf(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 608, 0, 0, 0, 0, 0, 0, 0, 0, 31, 0, 96, 96, 96, 0, 0, 0, 0, 0, 0, 0, 0, 0, 224, 0,
            96, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 96, 0, 0, 96, 31, 0, 31, 0, 0, 0, 0, 608, 608, 0, 608, 0, 0, 0, 1120, 1120, 2176, 2176, 4096, 96, 96, 0,
            0, 0, 0, 0, 0, 0)
    private val jj_2_rtns: Array<JJCalls?>?
    private var jj_rescan: Boolean
    private var jj_gc: Int
    private var jj_expentries: Vector?
    private var jj_expentry: IntArray?
    private var jj_kind: Int
    private var jj_lasttokens: IntArray?
    private var jj_endpos = 0
}