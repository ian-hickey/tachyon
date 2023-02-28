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
package tachyon.runtime.query.caster

import java.io.IOException

object _OracleOpaqueCast {
    private val ZERO_ARGS: Array<Object?>? = arrayOfNulls<Object?>(0)

    @Throws(SQLException::class, IOException::class)
    fun toCFType(rst: ResultSet?, columnIndex: Int): Object? {
        validateClasses()

        // we do not have oracle.sql.OPAQUE in the core, so we need reflection for this
        /*
		 * try{ String typeName=Caster.toString(Reflector.callMethod(o, "getSQLTypeName", ZERO_ARGS),null);
		 * 
		 * //OPAQUE opaque = ((oracle.sql.OPAQUE)o); if("SYS.XMLTYPE".equals(typeName)) {
		 * 
		 * // first we need to load the class in question Class
		 * clazz=ClassUtil.loadClass(o.getClass().getClassLoader(),"oracle.xdb.XMLType"); return
		 * Reflector.callStaticMethod(clazz, "createXML", new Object[]{o}); } } catch(PageException pe){
		 * throw ExceptionUtil.toIOException(pe); }
		 */return rst.getObject(columnIndex) ?: return null
    }

    @Throws(IOException::class)
    private fun validateClasses() {
        val clazz1: Class<*> = ClassUtil.loadClass("oracle.xdb.XMLType", null)
        val clazz2: Class<*> = ClassUtil.loadClass("oracle.xml.parser.v2.XMLParseException", null)
        if (clazz1 == null || clazz2 == null) throw IOException("the xdb.jar/xmlparserv2.jar is missing, please download at "
                + "http://www.oracle.com/technology/tech/xml/xdk/xdk_java.html and copy it into the Tachyon lib directory")
    }
}