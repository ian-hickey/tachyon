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
package tachyon.transformer.bytecode

import java.util.Iterator

class BytecodeContext : Context {
    private var classWriter: ClassWriter?
    private var adapter: GeneratorAdapter?
    private var className: String?
    private var keys: List<LitString?>?
    private var count = 0
    private var method: Method?
    private var doSubFunctions = true

    // private StaticConstrBytecodeContext staticConstr;
    private var constr: ConstrBytecodeContext?
    private val suppressWSbeforeArg: Boolean
    private val output: Boolean
    private val insideFinallies: Stack<OnFinally?>? = Stack<OnFinally?>()
    var tcf: Stack<OnFinally?>? = Stack<OnFinally?>()
    private var currentTag = 0
    private var line = 0
    private var root: BytecodeContext? = null
    private var writeLog: Boolean
    private var rtn = -1
    private val returnValue: Boolean
    private val id = id()
    private var page: Page?
    protected var ps: PageSource? = null

    constructor(ps: PageSource?, constr: ConstrBytecodeContext?, page: Page?, keys: List<LitString?>?, classWriter: ClassWriter?, className: String?, adapter: GeneratorAdapter?,
                method: Method?, writeLog: Boolean, suppressWSbeforeArg: Boolean, output: Boolean, returnValue: Boolean) {
        this.classWriter = classWriter
        this.className = className
        this.writeLog = writeLog
        this.adapter = adapter
        this.keys = keys
        this.method = method
        // this.staticConstr=statConstr;
        this.constr = constr
        this.page = page
        this.suppressWSbeforeArg = suppressWSbeforeArg
        this.returnValue = returnValue
        this.output = output
        if (ps != null) this.ps = ps else if (constr != null) this.ps = constr.ps
    }

    constructor(constr: ConstrBytecodeContext?, keys: List<LitString?>?, bc: BytecodeContext?, adapter: GeneratorAdapter?, method: Method?) {
        classWriter = bc!!.getClassWriter()
        className = bc.getClassName()
        writeLog = bc.writeLog()
        this.adapter = adapter
        this.keys = keys
        this.method = method
        // this.staticConstr=statConstr;
        this.constr = constr
        page = bc.getPage()
        suppressWSbeforeArg = bc.suppressWSbeforeArg
        returnValue = bc.returnValue
        output = bc.output
        ps = bc.ps
    }

    @Override
    fun getFactory(): Factory? {
        return page!!.getFactory()
    }

    /**
     * @return the id
     */
    fun getId(): String? {
        return id
    }

    /**
     * @return the count
     */
    fun getCount(): Int {
        return count
    }

    /**
     * @param count the count to set
     */
    fun incCount(): Int {
        return ++count
    }

    fun resetCount() {
        count = 0
    }

    /**
     * @return the adapter
     */
    fun getAdapter(): GeneratorAdapter? {
        return adapter
    }

    /**
     * @param adapter the adapter to set
     */
    fun setAdapter(bc: BytecodeContext?) {
        adapter = bc!!.getAdapter()
    }

    /**
     * @return the classWriter
     */
    fun getClassWriter(): ClassWriter? {
        return classWriter
    }

    /**
     * @param classWriter the classWriter to set
     */
    fun setClassWriter(classWriter: ClassWriter?) {
        this.classWriter = classWriter
    }

    /**
     * @return the className
     */
    fun getClassName(): String? {
        return className
    }

    /**
     * @param className the className to set
     */
    fun setClassName(className: String?) {
        this.className = className
    }

    @Synchronized
    fun registerKey(lit: LitString?): Int {
        // synchronized (keys) {
        val index = keys!!.indexOf(lit)
        if (index != -1) return index // calls the toString method of litString
        keys.add(lit)
        return keys!!.size() - 1
        // }
    }

    fun registerJavaFunction(jbc: JavaFunction?) {
        page!!.registerJavaFunction(jbc)
    }

    fun getKeys(): List<LitString?>? {
        return keys
    }

    // private static BytecodeContext staticConstr;
    fun pushOnFinally(onFinally: OnFinally?) {
        tcf.push(onFinally)
    }

    fun popOnFinally() {
        tcf.pop()
    }

    fun getOnFinallyStack(): Stack<OnFinally?>? {
        return tcf
    }

    /**
     * @return the method
     */
    fun getMethod(): Method? {
        return method
    }

    /**
     * @return the doSubFunctions
     */
    fun doSubFunctions(): Boolean {
        return doSubFunctions
    }

    /**
     * @param doSubFunctions the doSubFunctions to set
     * @return
     */
    fun changeDoSubFunctions(doSubFunctions: Boolean): Boolean {
        val old = this.doSubFunctions
        this.doSubFunctions = doSubFunctions
        return old
    }

    /**
     * @return the currentTag
     */
    fun getCurrentTag(): Int {
        return currentTag
    }

    /**
     * @param currentTag the currentTag to set
     */
    fun setCurrentTag(currentTag: Int) {
        this.currentTag = currentTag
    }

    fun getConstructor(): ConstrBytecodeContext? {
        return constr
    }

    fun visitLineNumber(line: Int) {
        this.line = line
        getAdapter().visitLineNumber(line, getAdapter().mark())
    }

    fun getLine(): Int {
        return line
    }

    fun getRoot(): BytecodeContext? {
        return root
    }

    fun setRoot(root: BytecodeContext?) {
        this.root = root
    }

    fun writeLog(): Boolean {
        return writeLog
    }

    fun getPage(): Page? {
        return page
    }

    fun getSupressWSbeforeArg(): Boolean {
        return suppressWSbeforeArg
    }

    fun getOutput(): Boolean {
        return output
    }

    fun getConfig(): Config? {
        return if (ps != null) ps.getMapping().getConfig() else ThreadLocalPageContext.getConfig()
    }

    /**
     * optional value maybe not exists!
     *
     * @return PageSource if available otherwise null
     */
    fun getPageSource(): PageSource? {
        return ps
    }

    fun finallyPush(onf: OnFinally?) {
        insideFinallies.push(onf)
    }

    fun finallyPop(): OnFinally? {
        return insideFinallies.pop()
    }

    fun insideFinally(onf: OnFinally?): Boolean {
        val it: Iterator<OnFinally?> = insideFinallies.iterator()
        while (it.hasNext()) {
            if (it.next() === onf) return true
        }
        return false
    }

    fun setReturn(rtn: Int) {
        this.rtn = rtn
    }

    fun getReturn(): Int {
        return rtn
    }

    /**
     * should the Page return the last expression or not
     *
     * @return
     */
    fun returnValue(): Boolean {
        return returnValue
    }

    companion object {
        private var _id: Long = 0
        @Synchronized
        private fun id(): String? {
            if (_id < 0) _id = 0
            return StringUtil.addZeros(++_id, 4)
        }
    }
}