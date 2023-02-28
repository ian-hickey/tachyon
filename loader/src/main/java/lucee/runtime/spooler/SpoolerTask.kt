/**
 * Copyright (c) 2014, the Railo Company Ltd.
 * Copyright (c) 2015, Lucee Assosication Switzerland
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
package lucee.runtime.spooler

import java.io.Serializable

interface SpoolerTask : Serializable {
    fun getId(): String?
    fun setId(id: String?)

    /**
     * execute Task
     *
     * @param config config
     * @return Task Object
     * @throws PageException Page Exception
     */
    @Throws(PageException::class)
    fun execute(config: Config?): Object?

    /**
     * returns a short info to the task
     *
     * @return Task subject
     */
    fun subject(): String?

    /**
     * returns task type as String
     *
     * @return Task subject
     */
    fun getType(): String?

    /**
     * returns advanced info to the task
     *
     * @return Task detail
     */
    fun detail(): Struct?

    /**
     * return last execution of this task
     *
     * @return last execution
     */
    fun lastExecution(): Long
    fun setNextExecution(nextExecution: Long)
    fun nextExecution(): Long

    /**
     * returns how many tries to send are already done
     *
     * @return tries
     */
    fun tries(): Int

    /**
     * @return the exceptions
     */
    fun getExceptions(): Array?
    fun setClosed(closed: Boolean)
    fun closed(): Boolean

    /**
     * @return the plans
     */
    fun getPlans(): Array<ExecutionPlan?>?

    /**
     * @return the creation
     */
    fun getCreation(): Long
    fun setLastExecution(lastExecution: Long)
}