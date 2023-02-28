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
package lucee.runtime.tag

import lucee.commons.cli.Command

/**
 *
 */
class _Execute(pageContext: PageContext?, monitor: Object?, commands: Array<String?>?, outputfile: Resource?, variable: String?, errorFile: Resource?, errorVariable: String?, directory: String?) : PageContextThread(pageContext) {
    private val outputfile: Resource?
    private val errorFile: Resource?
    private val variable: String?
    private val errorVariable: String?
    private var aborted = false
    private val commands: Array<String?>?

    // private static final int BLOCK_SIZE=4096;
    private val monitor: Object?
    private var exception: Exception? = null

    // private String body;
    private var finished = false
    private var process: Process? = null
    private val directory: String?
    @Override
    fun run(pc: PageContext?) {
        try {
            _run(pc)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun _run(pc: PageContext?) {
        try {
            process = Command.createProcess(pc, commands, directory)
            val result: CommandResult = Command.execute(process)
            val rst: String = result.getOutput()
            finished = true
            if (!aborted) {
                if (outputfile == null && variable == null) pc.write(rst) else {
                    if (outputfile != null) IOUtil.write(outputfile, rst, SystemUtil.getCharset(), false)
                    if (variable != null) pc.setVariable(variable, rst)
                }
                if (errorFile != null) IOUtil.write(errorFile, result.getError(), SystemUtil.getCharset(), false)
                if (errorVariable != null) pc.setVariable(errorVariable, result.getError())
            }
        } catch (ioe: Exception) {
            exception = ioe
        }
        // }
    }

    /**
     * define that execution is aborted
     */
    fun abort(terminateProcess: Boolean) {
        aborted = true
        if (terminateProcess) process.destroy()
    }

    fun hasException(): Boolean {
        return exception != null
    }

    fun hasFinished(): Boolean {
        return finished
    }

    /**
     * @return the exception
     */
    fun getException(): Exception? {
        return exception
    }

    /**
     * @param pageContext
     * @param monitor
     * @param process
     * @param outputfile
     * @param variable
     * @param body
     * @param terminateOnTimeout
     */
    init {
        this.monitor = monitor
        this.commands = commands
        this.outputfile = outputfile
        this.variable = variable
        this.errorFile = errorFile
        this.errorVariable = errorVariable
        // this.body=body;
        this.directory = directory
    }
}