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
package lucee.runtime.schedule

import java.net.URL

/**
 * a single scheduler task
 */
interface ScheduleTask {
    /**
     * @return Returns the credentials.
     */
    fun getCredentials(): Credentials?

    /**
     * @return Returns has credentials.
     */
    fun hasCredentials(): Boolean

    /**
     * @return Returns the file.
     */
    fun getResource(): Resource?

    /**
     * @return Returns the interval.
     */
    fun getInterval(): Int

    /**
     * @return Returns the operation.
     */
    fun getOperation(): Short

    /**
     * @return Returns the proxyHost.
     */
    fun getProxyData(): ProxyData?

    /**
     * @return Returns the resolveURL.
     */
    fun isResolveURL(): Boolean

    /**
     * @return Returns the task name.
     */
    fun getTask(): String?

    /**
     * @return Returns the timeout.
     */
    fun getTimeout(): Long

    /**
     * @return Returns the url.
     */
    fun getUrl(): URL?

    /**
     * @param nextExecution Next Execution
     */
    fun setNextExecution(nextExecution: Long)

    /**
     * @return Returns the nextExecution.
     */
    fun getNextExecution(): Long

    /**
     * @return Returns the endDate.
     */
    fun getEndDate(): Date?

    /**
     * @return Returns the startDate.
     */
    fun getStartDate(): Date?

    /**
     * @return Returns the endTime.
     */
    fun getEndTime(): Time?

    /**
     * @return Returns the startTime.
     */
    fun getStartTime(): Time?

    /**
     * @return returns interval definition as String
     */
    fun getIntervalAsString(): String?

    /**
     * @return Returns the strInterval.
     */
    fun getStringInterval(): String?

    /**
     * @return Returns the publish.
     */
    fun isPublish(): Boolean

    /**
     * @return Returns the valid.
     */
    fun isValid(): Boolean

    /**
     * @param valid The valid to set.
     */
    fun setValid(valid: Boolean)

    /**
     * @return the hidden
     */
    fun isHidden(): Boolean

    /**
     * @param hidden the hidden to set
     */
    fun setHidden(hidden: Boolean)
    fun isPaused(): Boolean

    companion object {
        /**
         * Field `OPERATION_HTTP_REQUEST`
         */
        const val OPERATION_HTTP_REQUEST: Short = 0

        /**
         * Field `INTERVAL_ONCE`
         */
        const val INTERVAL_ONCE = 0

        /**
         * Field `INTERVAL_DAY`
         */
        const val INTERVAL_DAY = 1

        /**
         * Field `INTERVAL_WEEK`
         */
        const val INTERVAL_WEEK = 2

        /**
         * Field `INTERVAL_MONTH`
         */
        const val INTERVAL_MONTH = 3
    }
}