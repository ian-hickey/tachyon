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
package lucee.runtime.net.rpc

import javax.xml.namespace.QName

object RPCConstants {
    val COMPONENT: QName? = QName(Constants.WEBSERVICE_NAMESPACE_URI, "Component")
    var QUERY_QNAME: QName? = QName(Constants.WEBSERVICE_NAMESPACE_URI, "QueryBean")
    var ARRAY_QNAME: QName? = QName(Constants.WEBSERVICE_NAMESPACE_URI, "Array")

    // private static QName componentQName=new QName("http://components.test.jm","address");
    // private static QName dateTimeQName=new QName("http://www.w3.org/2001/XMLSchema","dateTime");
    val STRING_QNAME: QName? = QName("http://www.w3.org/2001/XMLSchema", "string")
}