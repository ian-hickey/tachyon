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
package lucee.runtime.net.rpc.client

import lucee.runtime.PageContext

abstract class WebserviceClient : Objects, Iteratorable {
    // TODO add the missing parts
    @Throws(PageException::class)
    abstract fun init(pc: PageContext?, wsdlUrl: String?, username: String?, password: String?, proxyData: ProxyData?): WebserviceClient?

    // public abstract void addHeader(Object header) throws PageException;
    // public abstract Call getLastCall()throws PageException;
    @Throws(PageException::class)
    abstract fun callWithNamedValues(config: Config?, methodName: Collection.Key?, arguments: Struct?): Object?
    @Override
    @Throws(PageException::class)
    abstract fun callWithNamedValues(pc: PageContext?, methodName: Collection.Key?, arguments: Struct?): Object?
}