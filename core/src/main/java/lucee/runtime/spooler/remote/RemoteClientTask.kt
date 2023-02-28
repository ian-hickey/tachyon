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
package lucee.runtime.spooler.remote

import lucee.runtime.config.RemoteClient

class RemoteClientTask(plans: Array<ExecutionPlan?>?, client: RemoteClient?, attrColl: Struct?, callerId: String?, private val type: String?) : SpoolerTaskWS(plans, client) {
    private val args: StructImpl?
    private val action: String?
    @Override
    fun getType(): String? {
        return type
    }

    @Override
    fun subject(): String? {
        return action.toString() + " (" + super.subject() + ")"
    }

    @Override
    fun detail(): Struct? {
        val sct: Struct = super.detail()
        sct.setEL("action", action)
        return sct
    }

    @Override
    protected fun getMethodName(): String? {
        return "invoke"
    }

    @Override
    protected fun getArguments(): Struct? {
        return args
    }

    companion object {
        val PASSWORD: Collection.Key? = KeyImpl.getInstance("password")
        val ATTRIBUTE_COLLECTION: Collection.Key? = KeyImpl.getInstance("attributeCollection")
        val CALLER_ID: Collection.Key? = KeyImpl.getInstance("callerId")
    }

    init {
        action = attrColl.get(KeyConstants._action, null)
        args = StructImpl()
        args.setEL(KeyConstants._type, client.getType())
        args.setEL(PASSWORD, client.getAdminPasswordEncrypted())
        args.setEL(ATTRIBUTE_COLLECTION, attrColl)
        args.setEL(CALLER_ID, callerId)
    }
}