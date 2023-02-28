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

import java.util.ArrayList

/**
 * Enables CFML developers to execute a process on a server computer.
 *
 *
 *
 */
class Execute : BodyTagImpl() {
    /** Command-line arguments passed to the application.  */
    private var arguments: List<String?>? = null

    /**
     * Indicates how long, in seconds, the CFML executing thread waits for the spawned process. A
     * timeout of 0 is equivalent to the non-blocking mode of executing. A very high timeout value is
     * equivalent to a blocking mode of execution. The default is 0; therefore, the CFML thread spawns a
     * process and returns without waiting for the process to terminate.If no output file is specified,
     * and the timeout value is 0, the program output is discarded.
     */
    private var timeout: Long = 0

    /**
     * The full pathname of the application to execute. Note: On Windows, you must specify the extension
     * as part of the application's name. For example, myapp.exe,
     */
    private var name: String? = null

    /**
     * The file to which to direct the output of the program. If not specified, the output is displayed
     * on the page from which it was called.
     */
    private var outputfile: Resource? = null
    private var errorFile: Resource? = null
    private var variable: String? = null
    private var errorVariable: String? = null
    private var body: String? = null
    private var directory: String? = null
    private var terminateOnTimeout = false
    @Override
    fun release() {
        super.release()
        arguments = null
        timeout = 0L
        name = null
        outputfile = null
        errorFile = null
        variable = null
        errorVariable = null
        body = null
        terminateOnTimeout = false
        directory = null
    }

    /**
     * set the value arguments Command-line arguments passed to the application.
     *
     * @param args value to set
     * @throws PageException
     */
    @Throws(PageException::class)
    fun setArguments(args: Object?) {
        if (args is lucee.runtime.type.Collection) {
            arguments = ArrayList<String?>()
            val coll: lucee.runtime.type.Collection? = args as lucee.runtime.type.Collection?
            val it: Iterator<Object?> = coll.valueIterator()
            while (it.hasNext()) {
                arguments.add(Caster.toString(it.next()))
            }
        } else {
            arguments = Command.toList(Caster.toString(args))
        }
    }

    /**
     * set the value timeout Indicates how long, in seconds, the CFML executing thread waits for the
     * spawned process. A timeout of 0 is equivalent to the non-blocking mode of executing. A very high
     * timeout value is equivalent to a blocking mode of execution. The default is 0; therefore, the
     * CFML thread spawns a process and returns without waiting for the process to terminate.If no
     * output file is specified, and the timeout value is 0, the program output is discarded.
     *
     * @param timeout value to set
     * @throws ApplicationException
     */
    @Throws(ApplicationException::class)
    fun setTimeout(timeout: Double) {
        if (timeout < 0) throw ApplicationException("value must be a positive number now [" + Caster.toString(timeout).toString() + "]")
        this.timeout = (timeout * 1000L).toLong()
    }

    fun setTerminateontimeout(terminateontimeout: Boolean) {
        terminateOnTimeout = terminateontimeout
    }

    /**
     * set the value name The full pathname of the application to execute. Note: On Windows, you must
     * specify the extension as part of the application's name. For example, myapp.exe,
     *
     * @param name value to set
     */
    fun setName(name: String?) {
        this.name = name
    }

    /**
     * define name of variable where output is written to
     *
     * @param variable
     * @throws PageException
     */
    @Throws(PageException::class)
    fun setVariable(variable: String?) {
        this.variable = variable
        pageContext.setVariable(variable, "")
    }

    @Throws(PageException::class)
    fun setErrorvariable(errorVariable: String?) {
        this.errorVariable = errorVariable
        pageContext.setVariable(errorVariable, "")
    }

    /**
     * set the value outputfile The file to which to direct the output of the program. If not specified,
     * the output is displayed on the page from which it was called.
     *
     * @param outputfile value to set
     * @throws SecurityException
     */
    fun setOutputfile(outputfile: String?) {
        try {
            this.outputfile = ResourceUtil.toResourceExistingParent(pageContext, outputfile)
            pageContext.getConfig().getSecurityManager().checkFileLocation(this.outputfile)
        } catch (e: PageException) {
            this.outputfile = pageContext.getConfig().getTempDirectory().getRealResource(outputfile)
            if (!this.outputfile.getParentResource().exists()) this.outputfile = null else if (!this.outputfile.isFile()) this.outputfile = null else if (!this.outputfile.exists()) {
                ResourceUtil.createFileEL(this.outputfile, false)
                // try {
                // this.outputfile.createNewFile();
                /*
				 * } catch (IOException e1) { this.outputfile=null; }
				 */
            }
        }
    }

    fun setErrorfile(errorfile: String?) {
        try {
            errorFile = ResourceUtil.toResourceExistingParent(pageContext, errorfile)
            pageContext.getConfig().getSecurityManager().checkFileLocation(errorFile)
        } catch (e: PageException) {
            errorFile = pageContext.getConfig().getTempDirectory().getRealResource(errorfile)
            if (!errorFile.getParentResource().exists()) errorFile = null else if (!errorFile.isFile()) errorFile = null else if (!errorFile.exists()) {
                ResourceUtil.createFileEL(errorFile, false)
            }
        }
    }

    fun setDirectory(directory: String?) {
        this.directory = directory
    }

    @Override
    @Throws(PageException::class)
    fun doStartTag(): Int {
        return EVAL_BODY_BUFFERED
    }

    @Throws(Exception::class)
    private fun _execute() {
        val monitor: Object = SerializableObject()
        if (name == null) {
            if (StringUtil.isEmpty(body)) {
                required("execute", "name", name)
                required("execute", "arguments", arguments)
            } else {
                name = body
            }
        }
        if (arguments == null || arguments!!.size() === 0) {
            arguments = Command.toList(name)
        } else {
            arguments.add(0, name)
        }
        val execute = _Execute(pageContext, monitor, arguments.toArray(arrayOfNulls<String?>(arguments!!.size())), outputfile, variable, errorFile, errorVariable, directory)

        // if(timeout<=0)execute._run();
        // else {
        execute.start()
        val start: Long = System.currentTimeMillis()
        if (timeout > 0) execute.join(timeout) else execute.join()
        if (execute!!.hasException()) throw Exception(execute!!.getException())
        if (!execute!!.hasFinished()) {
            execute!!.abort(terminateOnTimeout)
            throw ApplicationException("timeout [" + timeout + " ms] expired while executing [" + ListUtil.listToList(arguments, " ") + "]")
        }
    }

    @Override
    @Throws(PageException::class)
    fun doEndTag(): Int {
        if (pageContext.getConfig().getSecurityManager().getAccess(SecurityManager.TYPE_TAG_EXECUTE) === SecurityManager.VALUE_NO) throw SecurityException("can't access tag [execute]", "access is prohibited by security manager")
        try {
            _execute()
        } catch (pe: PageException) {
            throw pe
        } catch (e: Exception) {
            throw ApplicationException("Error invoking external process", e.getMessage())
        }
        return EVAL_PAGE
    }

    @Override
    fun doInitBody() {
    }

    @Override
    fun doAfterBody(): Int {
        body = bodyContent.getString()
        if (!StringUtil.isEmpty(body)) body = body.trim()
        return SKIP_BODY
    }

    companion object {
        @Throws(Exception::class)
        fun main(args: Array<String?>?) {
            val cr: CommandResult = Command.execute("curl https://update.lucee.org/rest/update/provider/echoGet", true)
            val e = _Execute(null, null, arrayOf<String?>("curl", "https://update.lucee.org/rest/update/provider/echoGet"), null, null, null, null, null)
            e!!._run(null)
        }
    }
}