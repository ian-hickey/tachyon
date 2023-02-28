/**
 * Copyright (c) 2015, Lucee Assosication Switzerland. All rights reserved.
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
package lucee.transformer.bytecode.statement.tag

import java.io.ByteArrayInputStream

abstract class TagCIObject
/**
 * Constructor of the class
 *
 * @param startLine
 * @param endLine
 */
(f: Factory?, start: Position?, end: Position?) : TagBase(f, start, end) {
    private var main = false
    private var name: String? = null
    private var subClassName: String? = null

    @Override
    @Throws(TransformerException::class)
    override fun _writeOut(bc: BytecodeContext?) {
        _writeOut(bc, true, null)
    }

    @Override
    @Throws(TransformerException::class)
    override fun _writeOut(bc: BytecodeContext?, doReuse: Boolean) {
        _writeOut(bc, doReuse, null)
    }

    @Override
    @Throws(TransformerException::class)
    protected override fun _writeOut(bc: BytecodeContext?, doReuse: Boolean, fcf: FlowControlFinal?) {
        writeOut(bc, bc.getPage())
    }

    @Throws(TransformerException::class)
    fun writeOut(bc: BytecodeContext?, parent: Page?) {
        val functions: List<Function?> = parent.getFunctions()
        var psc: SourceCode? = null
        run {
            var tmp: SourceCode
            psc = parent.getSourceCode()
            while (true) {
                tmp = psc.getParent()
                if (tmp == null || tmp === psc) break
                psc = tmp
            }
        }
        val sc: SourceCode = parent.getSourceCode().subCFMLString(getStart().pos, getEnd().pos - getStart().pos)
        val page = Page(parent.getFactory(), parent.getConfig(), sc, this, CFMLEngineFactory.getInstance().getInfo().getFullVersionInfo(), parent.getLastModifed(),
                parent.writeLog(), parent.getSupressWSbeforeArg(), parent.getOutput(), parent.returnValue(), parent.ignoreScopes)

        // add functions from this component
        for (f in functions) {
            if (ASMUtil.getAncestorComponent(f) === this) {
                page.addFunction(f)
            }
        }

        // page.setIsComponent(true); // MUST can be an interface as well
        page.addStatement(this)
        setParent(page)
        val className = getSubClassName(parent)
        val barr: ByteArray = page.execute(className)
        val classFile: Resource = (psc as PageSourceCode?).getPageSource().getMapping().getClassRootDirectory().getRealResource(page.getClassName().toString() + ".class")
        val classDir: Resource = classFile.getParentResource()
        if (!classDir.isDirectory()) classDir.mkdirs()
        if (classFile.isFile()) classFile.delete()
        try {
            IOUtil.copy(ByteArrayInputStream(barr), classFile, true)
        } catch (e: IOException) {
            TransformerException(bc, ExceptionUtil.getMessage(e), getStart())
        }
    }

    fun getSubClassName(parent: Page?): String? {
        if (subClassName == null) subClassName = Page.createSubClass(parent.getClassName(), getName(), parent.getSourceCode().getDialect())
        return subClassName
    }

    @Override
    fun getFlowControlFinal(): FlowControlFinal? {
        return null
    }

    fun setMain(main: Boolean) {
        this.main = main
    }

    fun isMain(): Boolean {
        return main
    }

    @Throws(EvaluatorException::class)
    fun setName(name: String?) {
        if (!Decision.isVariableName(name)) throw EvaluatorException("component name [$name] is invalid")
        this.name = name
    }

    fun getName(): String? {
        return name
    }

    fun getStaticBodies(): List<StaticBody?>? {
        val b: Body = getBody()
        var list: List<StaticBody?>? = null
        if (!ASMUtil.isEmpty(b)) {
            var stat: Statement
            val it: Iterator<Statement?> = b.getStatements().iterator()
            while (it.hasNext()) {
                stat = it.next()
                // StaticBody
                if (stat is StaticBody) {
                    it.remove()
                    if (list == null) list = ArrayList<StaticBody?>()
                    list.add(stat as StaticBody)
                    // return (StaticBody) stat;
                }
            }
        }
        return list
    }
}