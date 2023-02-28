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
package lucee.runtime.db

import java.io.ByteArrayInputStream

/**
 * utilities for sql statements
 */
class HSQLUtil(sql: String?) {
    private val parser: ZqlParser

    /**
     * @return return the sql state inside
     */
    var sQL: String
        private set
    var isUnion = false
        private set
    /*
	 * * transalte SQL syntax to a ZQL combatible form
	 * 
	 * @param sql sql to transalte
	 * 
	 * @param changePlaceHolder
	 * 
	 * @return translated sql / private static String sqlToZQL(String sql,boolean changePlaceHolder) {
	 * sql=sql.trim(); char c=' ';//,last=' '; int len=sql.length(); boolean insideString=false;
	 * StringBuilder sb=new StringBuilder(len);
	 * 
	 * 
	 * 
	 * for(int i=0;i<len;i++) { c=sql.charAt(i); if(insideString) { if(c=='\'') { if(i+1>=len ||
	 * sql.charAt(i+1)!='\'')insideString=false; } } else { if(c=='\'')insideString=true; else
	 * if(changePlaceHolder && c=='?') { sb.append("QUESTION_MARK_SIGN"); //last=c; continue; } else
	 * if(c=='a'|| c=='A') { if( (i!=0 && isWhiteSpace(sql.charAt(i-1))) && (i+1<len &&
	 * (sql.charAt(i+1)=='s' || sql.charAt(i+1)=='S')) && (i+2<len && isWhiteSpace(sql.charAt(i+2))) ) {
	 * i++; //last=c; continue; } } else if(c=='*') {
	 * 
	 * } } //last=c; sb.append(c); }
	 * 
	 * if(c!=';')sb.append(';');
	 * 
	 * return sb.toString();
	 * 
	 * }
	 */
    /*
	 * private static boolean isWhiteSpace(char c) { return (c==' ' || c=='\t' || c=='\b' || c=='\r' ||
	 * c=='\n'); }
	 */// An SQL query: query the DB// Read all SQL statements from input
    /**
     * return all invoked tables by a sql statement
     *
     * @return invoked tables in an ArrayList
     * @throws ParseException
     */
    @get:Throws(ParseException::class)
    val invokedTables: Set<String>
        get() {

            // Read all SQL statements from input
            var st: ZStatement
            val tables: Set<String> = HashSet<String>()
            while (parser.readStatement().also { st = it } != null) {
                sQL = st.toString()
                if (st is ZQuery) { // An SQL query: query the DB
                    getInvokedTables(st as ZQuery, tables)
                }
                break
            }
            return tables
        }

    fun getInvokedTables(query: ZQuery, tablesNames: Set<String>) {
        // print.out("qry:"+query.getSet());
        val tables: Vector = query.getFrom()
        val e: Enumeration = tables.elements()

        // from
        while (e.hasMoreElements()) {
            val fromItem: ZFromItem = e.nextElement() as ZFromItem
            tablesNames.add(fromItem.getFullName())
        }
        // where
        val where: ZExp = query.getWhere()
        if (where is ZExpression) {
            parseZExpression(where as ZExpression, tablesNames)
        }
        // set
        val set: ZExpression = query.getSet()
        if (set != null) {
            isUnion = true
            val op: ZExp = set.getOperand(0)
            if (op is ZQuery) getInvokedTables(op as ZQuery, tablesNames)
        }
    }

    private fun parseZExpression(expression: ZExpression, tablesNames: Set) {
        val operands: Vector = expression.getOperands()
        val e: Enumeration = operands.elements()
        while (e.hasMoreElements()) {
            val el: Object = e.nextElement()
            if (el is ZExpression) parseZExpression(el as ZExpression, tablesNames) else if (el is ZQuery) getInvokedTables(el as ZQuery, tablesNames)
        }
    }

    /**
     * or of the class construct
     *
     * @param sql SQl Statement as String
     */
    init {
        sQL = SQLPrettyfier.prettyfie(sql, true) // sqlToZQL(sql,true);
        parser = ZqlParser(ByteArrayInputStream(sQL.getBytes()))
    }
}