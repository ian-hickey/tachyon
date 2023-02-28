/**
 * Copyright (c) 2014, the Railo Company Ltd.
 * Copyright (c) 2015, Tachyon Assosication Switzerland
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
package tachyon.runtime.spooler

import tachyon.runtime.exp.PageException

interface SpoolerEngine {
    /**
     * return the label of the engine
     *
     * @return the label
     */
    fun getLabel(): String?

    /**
     * adds a task to the engine
     *
     * @param task task
     */
    fun add(task: SpoolerTask?)

    /**
     * remove that task from Spooler
     *
     * @param task task
     */
    fun remove(task: SpoolerTask?)

    /**
     * remove a task that match given id
     *
     * @param id task id
     */
    fun remove(id: String?)

    /**
     * execute task by id and return error thrown by task
     *
     * @param id task id
     * @return Exception thrown by task
     */
    fun execute(id: String?): PageException?

    /**
     * execute task and return error thrown by task
     *
     * @param task task
     * @return Exception thrown by task
     */
    fun execute(task: SpoolerTask?): PageException?

    @Throws(PageException::class)
    fun getOpenTasksAsQuery(startrow: Int, maxrow: Int): Query?

    @Throws(PageException::class)
    fun getClosedTasksAsQuery(startrow: Int, maxrow: Int): Query?

    @Throws(PageException::class)
    fun getAllTasksAsQuery(startrow: Int, maxrow: Int): Query?
    fun getOpenTaskCount(): Int
    fun getClosedTaskCount(): Int // public void setLabel(String label);
    // public void setPersisDirectory(Resource persisDirectory);
    // public void setLog(Log log);
    // public void setConfig(Config config);
}