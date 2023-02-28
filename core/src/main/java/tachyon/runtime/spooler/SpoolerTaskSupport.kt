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

import tachyon.commons.lang.ExceptionUtil

abstract class SpoolerTaskSupport(plans: Array<ExecutionPlan?>?, nextExecution: Long) : SpoolerTaskPro {
    private val creation: Long
    private var lastExecution: Long = 0
    private var tries = 0
    private var nextExecution: Long = 0
    private val exceptions: Array? = ArrayImpl()
    private var closed = false
    private var id: String? = null
    private val plans: Array<ExecutionPlan?>?

    constructor(plans: Array<ExecutionPlan?>?) : this(plans, 0) {}

    @Override
    fun getId(): String? {
        return id
    }

    @Override
    fun setId(id: String?) {
        this.id = id
    }

    /**
     * return last execution of this task
     *
     * @return last execution
     */
    @Override
    fun lastExecution(): Long {
        return lastExecution
    }

    @Override
    fun setNextExecution(nextExecution: Long) {
        this.nextExecution = nextExecution
    }

    @Override
    fun nextExecution(): Long {
        return nextExecution
    }

    /**
     * returns how many tries to send are already done
     *
     * @return tries
     */
    @Override
    fun tries(): Int {
        return tries
    }

    @Throws(PageException::class)
    fun _execute(config: Config?) {
        lastExecution = System.currentTimeMillis()
        tries++
        try {
            execute(config)
        } catch (t: Throwable) {
            ExceptionUtil.rethrowIfNecessary(t)
            val pe: PageException = Caster.toPageException(t)
            val st: String = ExceptionUtil.getStacktrace(t, true)
            // config.getErrWriter().write(st+"\n");
            val sct: Struct = StructImpl()
            sct.setEL(KeyConstants._message, pe.getMessage())
            sct.setEL(KeyConstants._detail, pe.getDetail())
            sct.setEL(KeyConstants._stacktrace, st)
            sct.setEL(KeyConstants._time, Caster.toLong(System.currentTimeMillis()))
            exceptions.appendEL(sct)
            throw pe
        } finally {
            lastExecution = System.currentTimeMillis()
        }
    }

    /**
     * @return the exceptions
     */
    @Override
    fun getExceptions(): Array? {
        return exceptions
    }

    @Override
    fun setClosed(closed: Boolean) {
        this.closed = closed
    }

    @Override
    fun closed(): Boolean {
        return closed
    }

    /**
     * @return the plans
     */
    @Override
    fun getPlans(): Array<ExecutionPlan?>? {
        return plans
    }

    /**
     * @return the creation
     */
    @Override
    fun getCreation(): Long {
        return creation
    }

    @Override
    fun setLastExecution(lastExecution: Long) {
        this.lastExecution = lastExecution
    }

    @Override
    override fun getListener(): SpoolerTaskListener? {
        return null // not supported
    }

    companion object {
        private const val serialVersionUID = 2150341858025259745L
    }

    /**
     * Constructor of the class
     *
     * @param plans
     * @param timeOffset offset from the local time to the config time
     */
    init {
        this.plans = plans
        creation = System.currentTimeMillis()
        if (nextExecution > 0) this.nextExecution = nextExecution
    }
}