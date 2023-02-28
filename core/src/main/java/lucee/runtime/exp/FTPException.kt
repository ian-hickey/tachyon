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

import org.apache.commons.net.ftp.FTPClient

class FTPException : ApplicationException {
    private var code: Int
    private var msg: String?

    constructor(action: String?, client: FTPClient?) : super("Action [$action] from tag [ftp] failed", client.getReplyString()) {
        // setAdditional("ReplyCode",Caster.toDouble(client.getReplyCode()));
        // setAdditional("ReplyMessage",client.getReplyString());
        code = client.getReplyCode()
        msg = client.getReplyString()
    }

    constructor(action: String?, client: AFTPClient?) : super("Action [$action] from tag [ftp] failed", client.getReplyString()) {
        // setAdditional("ReplyCode",Caster.toDouble(client.getReplyCode()));
        // setAdditional("ReplyMessage",client.getReplyString());
        code = client.getReplyCode()
        msg = client.getReplyString()
    }

    @Override
    override fun getCatchBlock(config: Config?): CatchBlock? {
        val cb: CatchBlock = super.getCatchBlock(config)
        cb.setEL("Cause", msg)
        cb.setEL("Code", Caster.toDouble(code))
        return cb
    }
}