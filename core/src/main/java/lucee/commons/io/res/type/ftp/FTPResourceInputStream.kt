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
package lucee.commons.io.res.type.ftp

import java.io.IOException

class FTPResourceInputStream(client: FTPResourceClient, res: FTPResource?, `is`: InputStream?) : ResourceInputStream(res, `is`) {
    private val client: FTPResourceClient
    @Override
    @Throws(IOException::class)
    fun close() {
        try {
            super.close()
        } finally {
            client.completePendingCommand()
            (getResource().getResourceProvider() as FTPResourceProvider).returnClient(client)
        }
    }

    /**
     * Constructor of the class
     *
     * @param res
     * @param is
     */
    init {
        // print.ln("is:"+is);
        this.client = client
    }
}