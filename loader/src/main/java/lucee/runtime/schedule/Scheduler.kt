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
package lucee.runtime.schedule

import java.io.IOException

/**
 * Scheduler interface
 */
interface Scheduler {
    /**
     * returns a schedule tasks matching given name or throws an exception
     *
     * @param name Task name of Schedule Task to get
     * @return matching task
     * @throws ScheduleException Schedule Exception
     */
    @Throws(ScheduleException::class)
    fun getScheduleTask(name: String?): ScheduleTask?

    /**
     * returns all schedule tasks valid or not
     *
     * @return all tasks
     */
    fun getAllScheduleTasks(): Array<ScheduleTask?>?

    /**
     * returns a schedule tasks matching given name or null
     *
     * @param name Task name of Schedule Task to get
     * @param defaultValue default value
     * @return matching task
     */
    fun getScheduleTask(name: String?, defaultValue: ScheduleTask?): ScheduleTask?

    /**
     * Adds a Task to the scheduler
     *
     * @param task task
     * @param allowOverwrite allow overwrite
     * @throws ScheduleException Schedule Exception
     * @throws IOException IO Exception
     */
    @Throws(ScheduleException::class, IOException::class)
    fun addScheduleTask(task: ScheduleTask?, allowOverwrite: Boolean)

    /**
     * pause the scheduler task
     *
     * @param name name of the task to pause
     * @param pause pause
     * @param throwWhenNotExist define if method throws an exception if task doesn't exist
     * @throws ScheduleException Schedule Exception
     * @throws IOException IO Exception
     */
    @Throws(ScheduleException::class, IOException::class)
    fun pauseScheduleTask(name: String?, pause: Boolean, throwWhenNotExist: Boolean)

    /**
     * removes a task from scheduler
     *
     * @param name name of the task to remove
     * @param throwWhenNotExist define if method throws an exception if task doesn't exist
     * @throws IOException IO Exception
     * @throws ScheduleException Schedule Exception
     */
    @Throws(IOException::class, ScheduleException::class)
    fun removeScheduleTask(name: String?, throwWhenNotExist: Boolean)

    /**
     * runs a scheduler task
     *
     * @param name name of task to run
     * @param throwWhenNotExist define if method throws an exception if task doesn't exist
     * @throws IOException IO Exception
     * @throws ScheduleException Schedule Exception
     */
    @Throws(IOException::class, ScheduleException::class)
    fun runScheduleTask(name: String?, throwWhenNotExist: Boolean)
}