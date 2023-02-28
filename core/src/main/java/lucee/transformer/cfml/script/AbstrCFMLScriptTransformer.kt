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
package lucee.transformer.cfml.script

import java.util.ArrayList

/**
 * Innerhalb des Tag script kann in CFML eine eigene Scriptsprache verwendet werden, welche sich an
 * Javascript orientiert. Da der data.srcCode Transformer keine Spezialfaelle zulaesst, also Tags
 * einfach anhand der eingegeben TLD einliest und transformiert, aus diesem Grund wird der Inhalt
 * des Tag script einfach als Zeichenkette eingelesen. Erst durch den Evaluator (siehe 3.3), der
 * fuer das Tag script definiert ist, wird der Inhalt des Tag script uebersetzt.
 *
 */
abstract class AbstrCFMLScriptTransformer : AbstrCFMLExprTransformer() {
    private val ATTR_TYPE_NONE: Short = TagLibTagAttr.SCRIPT_SUPPORT_NONE
    private val ATTR_TYPE_OPTIONAL: Short = TagLibTagAttr.SCRIPT_SUPPORT_OPTIONAL
    private val ATTR_TYPE_REQUIRED: Short = TagLibTagAttr.SCRIPT_SUPPORT_REQUIRED

    class ComponentTemplateException(te: TemplateException?) : TemplateException(te.getPageSource(), te.getLine(), 0, te.getMessage()) {
        private val te: TemplateException?

        /**
         * @return the te
         */
        fun getTemplateException(): TemplateException? {
            return te
        }

        companion object {
            private const val serialVersionUID = -8103635220891288231L
        }

        init {
            this.te = te
        }
    }

    /**
     * Liest saemtliche Statements des CFScriptString ein. <br></br>
     * EBNF:<br></br>
     * `{statement spaces};`
     *
     * @return a statement
     * @throws TemplateException
     */
    @Throws(TemplateException::class)
    protected fun statements(data: Data?): Body? {
        val body = ScriptBody(data.factory)
        statements(data, body, true)
        return body
    }

    /**
     * Liest saemtliche Statements des CFScriptString ein. <br></br>
     * EBNF:<br></br>
     * `{statement spaces};`
     *
     * @param parent uebergeornetes Element dem das Statement zugewiesen wird.
     * @param isRoot befindet sich der Parser im root des data.srcCode Docs
     * @throws TemplateException
     */
    @Throws(TemplateException::class)
    private fun statements(data: Data?, body: Body?, isRoot: Boolean) {
        do {
            if (isRoot && isFinish(data)) return
            statement(data, body)
            comments(data)
        } while (data.srcCode.isValidIndex() && !data.srcCode.isCurrent('}'))
    }

    /**
     * Liest ein einzelnes Statement ein (if,for,while usw.). <br></br>
     * EBNF:<br></br>
     * `";" | "if" spaces "(" ifStatement | "function " funcStatement |  "while" spaces "(" whileStatement  |
     * "do" spaces "{" doStatement  | "for" spaces "(" forStatement | "return" returnStatement |
     * "break" breakStatement | "continue" continueStatement | "/ *" comment | expressionStatement;`
     *
     * @param parent uebergeornetes Element dem das Statement zugewiesen wird.
     * @throws TemplateException
     */
    @Throws(TemplateException::class)
    private fun statement(data: Data?, parent: Body?) {
        statement(data, parent, data.context)
    }

    @Throws(TemplateException::class)
    private fun statement(data: Data?, parent: Body?, context: Short): Boolean {
        val prior: Short = data.context
        data.context = context
        comments(data)
        var child: Statement? = null
        if (data.srcCode.forwardIfCurrent(';')) {
            return true
        } else if (ifStatement(data).also { child = it } != null) parent.addStatement(child) else if (propertyStatement(data, parent).also { child = it } != null) parent.addStatement(child) else if (paramStatement(data, parent).also { child = it } != null) parent.addStatement(child) else if (funcStatement(data, parent).also { child = it } != null) parent.addStatement(child) else if (whileStatement(data).also { child = it } != null) parent.addStatement(child) else if (doStatement(data).also { child = it } != null) parent.addStatement(child) else if (forStatement(data).also { child = it } != null) parent.addStatement(child) else if (returnStatement(data).also { child = it } != null) parent.addStatement(child) else if (switchStatement(data).also { child = it } != null) parent.addStatement(child) else if (tryStatement(data).also { child = it } != null) parent.addStatement(child) else if (islandStatement(data, parent)) {
        } else if (staticStatement(data, parent).also { child = it } != null) parent.addStatement(child) else if (componentStatement(data, parent).also { child = it } != null) parent.addStatement(child) else if (tagStatement(data, parent).also { child = it } != null) parent.addStatement(child) else if (cftagStatement(data, parent).also { child = it } != null) parent.addStatement(child) else if (block(data, parent)) {
        } else parent.addStatement(expressionStatement(data, parent))
        data.docComment = null
        data.context = prior
        return false
    }

    /**
     * Liest ein if Statement ein. <br></br>
     * EBNF:<br></br>
     * `spaces condition spaces ")" spaces block {"else if" spaces "(" elseifStatement spaces }
     * [("else"  spaces "(" | "else ") elseStatement spaces];`
     *
     * @return if Statement
     * @throws TemplateException
     */
    @Throws(TemplateException::class)
    private fun ifStatement(data: Data?): Statement? {
        if (!data.srcCode.forwardIfCurrent("if", '(')) return null
        val line: Position = data.srcCode.getPosition()
        val body: Body = BodyBase(data.factory)
        val cont = Condition(data.factory, condition(data), body, line, null)
        if (!data.srcCode.forwardIfCurrent(')')) throw TemplateException(data.srcCode, "if statement must end with a [)]")
        // ex block
        val prior: Body = data.setParent(body)
        statement(data, body, CTX_IF)
        data.setParent(prior)
        // else if
        comments(data)
        while (elseifStatement(data, cont)) {
            comments(data)
        }
        // else
        if (elseStatement(data, cont)) {
            comments(data)
        }
        cont.setEnd(data.srcCode.getPosition())
        return cont
    }

    /**
     * Liest ein else if Statement ein. <br></br>
     * EBNF:<br></br>
     * `spaces condition spaces ")" spaces block;`
     *
     * @return else if Statement
     * @throws TemplateException
     */
    @Throws(TemplateException::class)
    private fun elseifStatement(data: Data?, cont: Condition?): Boolean {
        val pos: Int = data.srcCode.getPos()
        if (!data.srcCode.forwardIfCurrent("else")) return false
        comments(data)
        if (!data.srcCode.forwardIfCurrent("if", '(')) {
            data.srcCode.setPos(pos)
            return false
        }
        val line: Position = data.srcCode.getPosition()
        val body: Body = BodyBase(data.factory)
        val pair: Pair = cont.addElseIf(condition(data), body, line, null)
        if (!data.srcCode.forwardIfCurrent(')')) throw TemplateException(data.srcCode, "else if statement must end with a [)]")
        // ex block
        val prior: Body = data.setParent(body)
        statement(data, body, CTX_ELSE_IF)
        data.setParent(prior)
        pair.end = data.srcCode.getPosition()
        return true
    }

    /**
     * Liest ein else Statement ein. <br></br>
     * EBNF:<br></br>
     * `block;`
     *
     * @return else Statement
     * @throws TemplateException
     */
    @Throws(TemplateException::class)
    private fun elseStatement(data: Data?, cont: Condition?): Boolean {
        if (!data.srcCode.forwardIfCurrent("else", '{') && !data.srcCode.forwardIfCurrent("else ") && !data.srcCode.forwardIfCurrent("else", '/')) return false

        // start (
        data.srcCode.previous()
        // ex block
        val body: Body = BodyBase(data.factory)
        val p: Pair = cont.setElse(body, data.srcCode.getPosition(), null)
        val prior: Body = data.setParent(body)
        statement(data, body, CTX_ELSE)
        data.setParent(prior)
        p.end = data.srcCode.getPosition()
        return true
    }

    @Throws(TemplateException::class)
    private fun finallyStatement(data: Data?, tcf: TryCatchFinally?): Boolean {
        if (!data.srcCode.forwardIfCurrent("finally", '{') && !data.srcCode.forwardIfCurrent("finally ") && !data.srcCode.forwardIfCurrent("finally", '/')) return false

        // start (
        data.srcCode.previous()
        // ex block
        val body: Body = BodyBase(data.factory)
        tcf.setFinally(body, data.srcCode.getPosition())
        val prior: Body = data.setParent(body)
        statement(data, body, CTX_FINALLY)
        data.setParent(prior)
        return true
    }

    /**
     * Liest ein while Statement ein. <br></br>
     * EBNF:<br></br>
     * `spaces condition spaces ")" spaces block;`
     *
     * @return while Statement
     * @throws TemplateException
     */
    @Throws(TemplateException::class)
    private fun whileStatement(data: Data?): While? {
        val pos: Int = data.srcCode.getPos()

        // id
        var id = variableDec(data, false)
        if (id == null) {
            data.srcCode.setPos(pos)
            return null
        }
        if (id.equalsIgnoreCase("while")) {
            id = null
            data.srcCode.removeSpace()
            if (!data.srcCode.forwardIfCurrent('(')) {
                data.srcCode.setPos(pos)
                return null
            }
        } else {
            data.srcCode.removeSpace()
            if (!data.srcCode.forwardIfCurrent(':')) {
                data.srcCode.setPos(pos)
                return null
            }
            data.srcCode.removeSpace()
            if (!data.srcCode.forwardIfCurrent("while", '(')) {
                data.srcCode.setPos(pos)
                return null
            }
        }
        val line: Position = data.srcCode.getPosition()
        val body: Body = BodyBase(data.factory)
        val whil = While(condition(data), body, line, null, id)
        if (!data.srcCode.forwardIfCurrent(')')) throw TemplateException(data.srcCode, "while statement must end with a [)]")
        val prior: Body = data.setParent(body)
        statement(data, body, CTX_WHILE)
        data.setParent(prior)
        whil.setEnd(data.srcCode.getPosition())
        return whil
    }

    /**
     * Liest ein switch Statment ein
     *
     * @return switch Statement
     * @throws TemplateException
     */
    @Throws(TemplateException::class)
    private fun switchStatement(data: Data?): Switch? {
        if (!data.srcCode.forwardIfCurrent("switch", '(')) return null
        val line: Position = data.srcCode.getPosition()
        comments(data)
        val expr: Expression = super.expression(data)
        comments(data)
        // end )
        if (!data.srcCode.forwardIfCurrent(')')) throw TemplateException(data.srcCode, "switch statement must end with a [)]")
        comments(data)
        if (!data.srcCode.forwardIfCurrent('{')) throw TemplateException(data.srcCode, "switch statement must have a starting  [{]")
        val swit = Switch(expr, line, null)

        // cases
        // Node child=null;
        comments(data)
        while (caseStatement(data, swit)) {
            comments(data)
        }
        // default
        if (defaultStatement(data, swit)) {
            comments(data)
        }
        while (caseStatement(data, swit)) {
            comments(data)
        }

        // }
        if (!data.srcCode.forwardIfCurrent('}')) throw TemplateException(data.srcCode, "invalid construct in switch statement")
        swit.setEnd(data.srcCode.getPosition())
        return swit
    }

    @Override
    @Throws(TemplateException::class)
    protected fun componentStatement(data: Data?, parent: Body?): TagComponent? {
        val pos: Int = data.srcCode.getPos()

        // get the idendifier in front of
        val id: String = identifier(data, false)
        if (id == null) {
            data.srcCode.setPos(pos)
            return null
        }
        val mod: Int = ComponentUtil.toModifier(id, Component.MODIFIER_NONE, Component.MODIFIER_NONE)
        if (mod == Component.MODIFIER_NONE) {
            data.srcCode.setPos(pos)
        }
        comments(data)

        // do we have a starting component?
        if (!data.srcCode.isCurrent(getComponentName(data.srcCode.getDialect()))
                && (data.srcCode.getDialect() === CFMLEngine.DIALECT_CFML || !data.srcCode.isCurrent(Constants.CFML_COMPONENT_TAG_NAME))) {
            data.srcCode.setPos(pos)
            return null
        }

        // parse the component
        val tlt: TagLibTag = CFMLTransformer.getTLT(data.srcCode, getComponentName(data.srcCode.getDialect()), data.config.getIdentification())
        val comp: TagComponent? = _multiAttrStatement(parent, data, tlt) as TagComponent?
        if (mod != Component.MODIFIER_NONE) comp.addAttribute(Attribute(false, "modifier", data.factory.createLitString(id), "string"))
        return comp
    }

    private fun getComponentName(dialect: Int): String? {
        return if (dialect == CFMLEngine.DIALECT_LUCEE) Constants.LUCEE_COMPONENT_TAG_NAME else Constants.CFML_COMPONENT_TAG_NAME
    }

    /**
     * Liest ein Case Statement ein
     *
     * @return case Statement
     * @throws TemplateException
     */
    @Throws(TemplateException::class)
    private fun caseStatement(data: Data?, swit: Switch?): Boolean {
        if (!data.srcCode.forwardIfCurrentAndNoWordAfter("case")) return false

        // int line=data.srcCode.getLine();
        comments(data)
        val expr: Expression = super.expression(data)
        comments(data)
        if (!data.srcCode.forwardIfCurrent(':')) throw TemplateException(data.srcCode, "case body must start with [:]")
        val body: Body = BodyBase(data.factory)
        switchBlock(data, body)
        swit.addCase(expr, body)
        return true
    }

    /**
     * Liest ein default Statement ein
     *
     * @return default Statement
     * @throws TemplateException
     */
    @Throws(TemplateException::class)
    private fun defaultStatement(data: Data?, swit: Switch?): Boolean {
        if (!data.srcCode.forwardIfCurrent("default", ':')) return false

        // int line=data.srcCode.getLine();
        val body: Body = BodyBase(data.factory)
        swit.setDefaultCase(body)
        switchBlock(data, body)
        return true
    }

    /**
     * Liest ein Switch Block ein
     *
     * @param block
     * @throws TemplateException
     */
    @Throws(TemplateException::class)
    private fun switchBlock(data: Data?, body: Body?) {
        while (data.srcCode.isValidIndex()) {
            comments(data)
            if (data.srcCode.isCurrent("case ") || data.srcCode.isCurrent("default", ':') || data.srcCode.isCurrent('}')) return
            val prior: Body = data.setParent(body)
            statement(data, body, CTX_SWITCH)
            data.setParent(prior)
        }
    }

    /**
     * Liest ein do Statement ein. <br></br>
     * EBNF:<br></br>
     * `block spaces "while" spaces "(" spaces condition spaces ")";`
     *
     * @return do Statement
     * @throws TemplateException
     */
    @Throws(TemplateException::class)
    private fun doStatement(data: Data?): DoWhile? {
        val pos: Int = data.srcCode.getPos()

        // id
        var id = variableDec(data, false)
        if (id == null) {
            data.srcCode.setPos(pos)
            return null
        }
        if (id.equalsIgnoreCase("do")) {
            id = null
            if (!data.srcCode.isCurrent('{') && !data.srcCode.isCurrent(' ') && !data.srcCode.isCurrent('/')) {
                data.srcCode.setPos(pos)
                return null
            }
        } else {
            data.srcCode.removeSpace()
            if (!data.srcCode.forwardIfCurrent(':')) {
                data.srcCode.setPos(pos)
                return null
            }
            data.srcCode.removeSpace()
            if (!data.srcCode.forwardIfCurrent("do", '{') && !data.srcCode.forwardIfCurrent("do ") && !data.srcCode.forwardIfCurrent("do", '/')) {
                data.srcCode.setPos(pos)
                return null
            }
            data.srcCode.previous()
        }

        // if(!data.srcCode.forwardIfCurrent("do",'{') && !data.srcCode.forwardIfCurrent("do ") &&
        // !data.srcCode.forwardIfCurrent("do",'/'))
        // return null;
        val line: Position = data.srcCode.getPosition()
        val body: Body = BodyBase(data.factory)

        // data.srcCode.previous();
        val prior: Body = data.setParent(body)
        statement(data, body, CTX_DO_WHILE)
        data.setParent(prior)
        comments(data)
        if (!data.srcCode.forwardIfCurrent("while", '(')) throw TemplateException(data.srcCode, "do statement must have a while at the end")
        val doWhile = DoWhile(condition(data), body, line, data.srcCode.getPosition(), id)
        if (!data.srcCode.forwardIfCurrent(')')) throw TemplateException(data.srcCode, "do statement must end with a [)]")
        return doWhile
    }
    /*
	 * private CFMLTransformer tag; private final Statement cfmlTagStatement(Data data,Body parent)
	 * throws TemplateException {
	 * 
	 * if(tag==null)tag=new CFMLTransformer(); tag.body(data, parent, parseExpression, transformer);
	 * 
	 * return null; }
	 */
    /**
     * Liest ein for Statement ein. <br></br>
     * EBNF:<br></br>
     * `expression spaces ";" spaces condition spaces ";" spaces expression spaces ")" spaces block;`
     *
     * @return for Statement
     * @throws TemplateException
     */
    @Throws(TemplateException::class)
    private fun forStatement(data: Data?): Statement? {
        val pos: Int = data.srcCode.getPos()

        // id
        var id = variableDec(data, false)
        if (id == null) {
            data.srcCode.setPos(pos)
            return null
        }
        if (id.equalsIgnoreCase("for")) {
            id = null
            data.srcCode.removeSpace()
            if (!data.srcCode.forwardIfCurrent('(')) {
                data.srcCode.setPos(pos)
                return null
            }
        } else {
            data.srcCode.removeSpace()
            if (!data.srcCode.forwardIfCurrent(':')) {
                data.srcCode.setPos(pos)
                return null
            }
            data.srcCode.removeSpace()
            if (!data.srcCode.forwardIfCurrent("for", '(')) {
                data.srcCode.setPos(pos)
                return null
            }
        }
        var left: Expression? = null
        val body: Body = BodyBase(data.factory)
        val line: Position = data.srcCode.getPosition()
        comments(data)
        if (!data.srcCode.isCurrent(';')) {
            // left
            left = expression(data)
            comments(data)
        }
        // middle for
        return if (data.srcCode.forwardIfCurrent(';')) {
            var cont: Expression? = null
            var update: Expression? = null
            // condition
            comments(data)
            if (!data.srcCode.isCurrent(';')) {
                cont = condition(data)
                comments(data)
            }
            // middle
            if (!data.srcCode.forwardIfCurrent(';')) throw TemplateException(data.srcCode, "invalid syntax in for statement")
            // update
            comments(data)
            if (!data.srcCode.isCurrent(')')) {
                update = expression(data)
                comments(data)
            }
            // start )
            if (!data.srcCode.forwardIfCurrent(')')) throw TemplateException(data.srcCode, "invalid syntax in for statement, for statement must end with a [)]")
            // ex block
            val prior: Body = data.setParent(body)
            statement(data, body, CTX_FOR)

            // performance improvement in special combination
            // TagLoop loop = asLoop(data.factory,left,cont,update,body,line,data.srcCode.getPosition(),id);
            // if(loop!=null) return loop;
            data.setParent(prior)
            For(data.factory, left, cont, update, body, line, data.srcCode.getPosition(), id)
        } else if (data.srcCode.forwardIfCurrent("in")) {
            // condition
            comments(data)
            val value: Expression = expression(data)
            comments(data)
            if (!data.srcCode.forwardIfCurrent(')')) throw TemplateException(data.srcCode, "invalid syntax in for statement, for statement must end with a [)]")

            // ex block
            val prior: Body = data.setParent(body)
            statement(data, body, CTX_FOR)
            data.setParent(prior)
            if (left !is Variable) throw TemplateException(data.srcCode, "invalid syntax in for statement, left value is invalid")
            ForEach(left as Variable?, value, body, line, data.srcCode.getPosition(), id)
        } else throw TemplateException(data.srcCode, "invalid syntax in for statement")
    }

    private fun toVariableName(bc: BytecodeContext?, variable: Expression?): String? {
        return if (variable !is Variable) null else try {
            VariableString.variableToString(bc, variable as Variable?, false)
        } catch (e: TransformerException) {
            null
        }
    }

    /**
     * Liest ein function Statement ein. <br></br>
     * EBNF:<br></br>
     * `identifier spaces "(" spaces identifier spaces {"," spaces identifier spaces} ")" spaces block;`
     *
     * @return function Statement
     * @throws TemplateException
     */
    @Throws(TemplateException::class)
    private fun funcStatement(data: Data?, parent: Body?): Statement? {
        val pos: Int = data.srcCode.getPos()

        // read 5 tokens (returntype,access modifier,"abstract|final|static","function", function name)
        val str = variableDec(data, false)
        // if there is no token at all we have no function
        if (str == null) {
            data.srcCode.setPos(pos)
            return null
        }
        comments(data)
        val tokens = arrayOf(str, null, null, null, null)
        tokens[1] = variableDec(data, false)
        comments(data)
        if (tokens[1] != null) {
            tokens[2] = variableDec(data, false)
            comments(data)
            if (tokens[2] != null) {
                tokens[3] = variableDec(data, false)
                comments(data)
                if (tokens[3] != null) {
                    tokens[4] = identifier(data, false)
                    comments(data)
                }
            }
        }

        // function name
        var functionName: String? = null
        for (i in tokens.indices.reversed()) {
            // first from right is the function name
            if (tokens[i] != null) {
                functionName = tokens[i]
                tokens[i] = null
                break
            }
        }
        if (functionName == null || functionName.indexOf(',') !== -1 || functionName.indexOf('[') !== -1) {
            data.srcCode.setPos(pos)
            return null
        }
        // throw new TemplateException(data.srcCode, "invalid syntax");
        var returnType: String? = null

        // search for "function"
        var hasOthers = false
        var first = true
        for (i in tokens.indices.reversed()) {
            if ("function".equalsIgnoreCase(tokens[i])) {
                // if it is the first "function" (from right) and we had already something else, the syntax is
                // broken!
                if (hasOthers && first) throw TemplateException(data.srcCode, "invalid syntax") else if (returnType != null) throw TemplateException(data.srcCode, "invalid syntax") else if (!first) returnType = tokens[i]
                first = false
                tokens[i] = null
            } else if (tokens[i] != null) {
                hasOthers = true
            }
        }
        // no "function" found
        if (first) {
            data.srcCode.setPos(pos)
            return null
        }

        // access modifier
        var _access: Int
        var access = -1
        for (i in tokens.indices) {
            if (tokens[i] != null && ComponentUtil.toIntAccess(tokens[i], -1).also { _access = it } != -1) {
                // we already have an access modifier
                if (access != -1) {
                    // we already have a return type
                    if (returnType != null) throw TemplateException(data.srcCode, "invalid syntax")
                    returnType = tokens[i]
                } else access = _access
                tokens[i] = null
            }
        }
        // no access defined
        if (access == -1) access = Component.ACCESS_PUBLIC

        // Non access modifier
        var _modifier: Int
        var modifier: Int = Component.MODIFIER_NONE
        var isStatic = false
        for (i in tokens.indices) {
            if (tokens[i] != null) {
                _modifier = ComponentUtil.toModifier(tokens[i], Component.MODIFIER_NONE, Component.MODIFIER_NONE)

                // abstract|final
                if (_modifier != Component.MODIFIER_NONE) {
                    // we already have an Non access modifier
                    if (modifier != Component.MODIFIER_NONE || isStatic) {
                        // we already have a return type
                        if (returnType != null) throw TemplateException(data.srcCode, "invalid syntax")
                        returnType = tokens[i]
                    } else modifier = _modifier
                    tokens[i] = null
                } else if (tokens[i].equalsIgnoreCase("static")) {
                    // we already have an Non access modifier
                    if (modifier != Component.MODIFIER_NONE || isStatic) {
                        // we already have a return type
                        if (returnType != null) throw TemplateException(data.srcCode, "invalid syntax")
                        returnType = tokens[i]
                    } else isStatic = true
                    tokens[i] = null
                }
            }
        }

        // return type
        for (i in tokens.indices) {
            if (tokens[i] != null) {
                if (returnType != null) throw TemplateException(data.srcCode, "invalid syntax")
                returnType = tokens[i]
            }
        }
        val line: Position = data.srcCode.getPosition()

        // comments(data);

        // Name
        if (!data.isCFC && !data.isInterface) {
            val flf: FunctionLibFunction = getFLF(data, functionName)
            try {
                if (flf != null && flf.getFunctionClassDefinition().getClazz() !== CFFunction::class.java) {
                    var ps: PageSource? = null
                    if (data.srcCode is PageSourceCode) {
                        ps = (data.srcCode as PageSourceCode).getPageSource()
                    }
                    var path: String? = null
                    if (ps != null) {
                        path = ps.getDisplayPath()
                        path = path.replace('\\', '/')
                    }
                    if (path == null || path.indexOf("/library/function/") === -1) throw TemplateException(data.srcCode, "The name [$functionName] is already used by a built in Function")
                }
            } catch (t: Throwable) {
                ExceptionUtil.rethrowIfNecessary(t)
                throw PageRuntimeException(Caster.toPageException(t))
            }
        }
        val res: Function? = closurePart(data, functionName, access, modifier, returnType, line, false)
        if (isStatic) {
            if (data.context === CTX_INTERFACE) throw TemplateException(data.srcCode, "static functions are not allowed within the interface body")
            val tag: TagOther? = createStaticTag(data, res.getStart())
            tag.getBody().addStatement(res)
            return tag
        }
        return res
    }

    @Override
    @Throws(TemplateException::class)
    fun getScriptFunctionArguments(data: Data?): ArrayList<Argument?>? {
        // arguments
        var passByRef: LitBoolean
        var displayName: Expression
        var hint: Expression
        var meta: Map<String?, Attribute?>?
        var _name: String
        val result: ArrayList<Argument?> = ArrayList<Argument?>()
        do {
            comments(data)
            // finish
            if (data.srcCode.isCurrent(')')) break

            // attribute

            // name
            // String idName=identifier(data,false,true);
            var required = false
            var idName = variableDec(data, false)
            // required
            if ("required".equalsIgnoreCase(idName)) {
                comments(data)
                val idName2 = variableDec(data, false)
                if (idName2 != null) {
                    idName = idName2
                    required = true
                }
                if (idName == null) throw TemplateException(data.srcCode, "invalid argument definition")
            }
            var typeName: String? = "any"
            if (idName == null) throw TemplateException(data.srcCode, "invalid argument definition")
            comments(data)
            if (!data.srcCode.isCurrent(')') && !data.srcCode.isCurrent('=') && !data.srcCode.isCurrent(':') && !data.srcCode.isCurrent(',')) {
                typeName = idName
                idName = identifier(data, false) // MUST was upper case before, is this a problem?
            } else if (idName.indexOf('.') !== -1 || idName.indexOf('[') !== -1) {
                throw TemplateException(data.srcCode, "invalid argument name [$idName] definition")
            }
            if (idName == null) throw TemplateException(data.srcCode, "invalid argument definition")
            comments(data)
            var defaultValue: Expression?
            if (data.srcCode.isCurrent('=') || data.srcCode.isCurrent(':')) {
                data.srcCode.next()
                comments(data)
                defaultValue = expression(data)
            } else defaultValue = null

            // assign meta data defined in doc comment
            passByRef = data.factory.TRUE()
            displayName = data.factory.EMPTY()
            hint = data.factory.EMPTY()
            meta = null
            if (data.docComment != null) {
                val params: Map<String?, Attribute?> = data.docComment.getParams()
                val attrs: Array<Attribute?> = params.values().toArray(arrayOfNulls<Attribute?>(params.size()))
                var attr: Attribute?
                var name: String
                for (i in attrs.indices) {
                    attr = attrs[i]
                    name = attr.getName()
                    // hint
                    if (idName.equalsIgnoreCase(name) || name.equalsIgnoreCase("$idName.hint")) {
                        hint = data.factory.toExprString(attr.getValue())
                        params.remove(name)
                    }
                    // meta
                    if (StringUtil.startsWithIgnoreCase(name, "$idName.")) {
                        if (name.length() > idName.length() + 1) {
                            if (meta == null) meta = HashMap<String?, Attribute?>()
                            _name = name.substring(idName.length() + 1)
                            meta.put(_name, Attribute(attr.isDynamicType(), _name, attr.getValue(), attr.getType()))
                        }
                        params.remove(name)
                    }
                }
            }

            // argument attributes
            val _attrs: Array<Attribute?>? = attributes(null, null, data, COMMA_ENDBRACKED, data.factory.EMPTY(), Boolean.TRUE, null, false, NO_ATTR_SEP, true)
            var _attr: Attribute?
            if (!ArrayUtil.isEmpty(_attrs)) {
                if (meta == null) meta = HashMap<String?, Attribute?>()
                for (i in _attrs.indices) {
                    _attr = _attrs!![i]
                    meta.put(_attr.getName(), _attr)
                }
            }
            result.add(Argument(data.factory.createLitString(idName), data.factory.createLitString(typeName), data.factory.createLitBoolean(required), defaultValue, passByRef,
                    displayName, hint, meta))
            comments(data)
        } while (data.srcCode.forwardIfCurrent(','))
        return result
    }

    @Override
    @Throws(TemplateException::class)
    protected fun closurePart(data: Data?, id: String?, access: Int, modifier: Int, rtnType: String?, line: Position?, closure: Boolean): Function? {
        var access = access
        var modifier = modifier
        var rtnType = rtnType
        val body: Body = FunctionBody(data.factory)
        val func: Function = if (closure) Closure(id, access, modifier, rtnType, body, line, null) else FunctionImpl(id, access, modifier, rtnType, body, line, null)
        comments(data)
        if (!data.srcCode.forwardIfCurrent('(')) throw TemplateException(data.srcCode, "invalid syntax in function head, missing begin [(]")

        // arguments
        val args: ArrayList<Argument?>? = getScriptFunctionArguments(data)
        for (arg in args) {
            func.addArgument(arg.getName(), arg.getType(), arg.getRequired(), arg.getDefaultValue(), arg.isPassByReference(), arg.getDisplayName(), arg.getHint(),
                    arg.getMetaData())
        }
        // end )
        comments(data)
        if (!data.srcCode.forwardIfCurrent(')')) throw TemplateException(data.srcCode, "invalid syntax in function head, missing ending [)]")

        // TagLibTag tlt = CFMLTransformer.getTLT(data.srcCode,"function");

        // doc comment
        var hint: String? = null
        if (data.docComment != null) {
            func.setHint(data.factory, data.docComment.getHint().also { hint = it })
            func.setMetaData(data.docComment.getParams())
            data.docComment = null
        }
        comments(data)

        // attributes
        var attrs: Array<Attribute?>? = attributes(null, null, data, SEMI_BLOCK, data.factory.EMPTY(), Boolean.TRUE, null, false, NO_ATTR_SEP, true)
        var isJava = false
        for (attr in attrs!!) {
            // check type
            if ("type".equalsIgnoreCase(attr.getName())) {
                if (attr.getValue() is LitString) {
                    if ((attr.getValue() as LitString).getString().equalsIgnoreCase("java")) isJava = true
                } else throw TemplateException(data.srcCode, "attribute type must be a literal string, ")
            }
            func.addAttribute(null, attr)
        }

        // body
        val oldInsideFunction: Boolean = data.insideFunction
        data.insideFunction = true
        try {
            // ex block
            val prior: Body = data.setParent(body)
            if (isJava) {

                // return type
                var lit: Literal? = extract(attrs, "returntype")
                if (lit != null) {
                    rtnType = lit.getString()
                    attrs = remove(attrs, "returntype")
                }

                // output
                var output: Boolean? = null
                lit = extract(attrs, "output")
                if (lit != null) {
                    output = lit.getBoolean(null)
                    attrs = remove(attrs, "output")
                }

                // bufferoutput
                var bufferOutput: Boolean? = null
                lit = extract(attrs, "bufferoutput")
                if (lit != null) {
                    bufferOutput = lit.getBoolean(null)
                    attrs = remove(attrs, "bufferoutput")
                }

                // modifier
                lit = extract(attrs, "modifier")
                if (lit != null) {
                    modifier = ComponentUtil.toModifier(lit.getString(), Component.MODIFIER_NONE, modifier)
                    attrs = remove(attrs, "modifier")
                }

                // access
                lit = extract(attrs, "access")
                if (lit != null) {
                    access = ComponentUtil.toIntAccess(lit.getString(), access)
                    attrs = remove(attrs, "access")
                }

                // displayname
                var displayName: String? = null
                lit = extract(attrs, "displayname")
                if (lit != null) {
                    displayName = lit.getString()
                    attrs = remove(attrs, "displayname")
                }

                // hint
                lit = extract(attrs, "hint")
                if (lit != null) {
                    hint = lit.getString()
                    attrs = remove(attrs, "hint")
                }

                // description
                var description: String? = null
                lit = extract(attrs, "description")
                if (lit != null) {
                    description = lit.getString()
                    attrs = remove(attrs, "description")
                }

                // returnFormat
                var returnFormat: Int = UDF.RETURN_FORMAT_WDDX
                lit = extract(attrs, "returnformat")
                if (lit != null) {
                    returnFormat = UDFUtil.toReturnFormat(lit.getString(), returnFormat)
                    attrs = remove(attrs, "returnformat")
                }

                // secureJson
                var secureJson: Boolean? = null
                lit = extract(attrs, "securejson")
                if (lit != null) {
                    secureJson = lit.getBoolean(null)
                    attrs = remove(attrs, "securejson")
                }

                // verifyClient
                var verifyClient: Boolean? = null
                lit = extract(attrs, "verifyclient")
                if (lit != null) {
                    verifyClient = lit.getBoolean(null)
                    attrs = remove(attrs, "verifyclient")
                }

                // localMode
                var localMode: Int = Undefined.MODE_LOCAL_OR_ARGUMENTS_ONLY_WHEN_EXISTS
                lit = extract(attrs, "localmode")
                if (lit != null) {
                    localMode = AppListenerUtil.toLocalMode(lit.getString(), Undefined.MODE_LOCAL_OR_ARGUMENTS_ONLY_WHEN_EXISTS)
                    attrs = remove(attrs, "localmode")
                }

                // TODO cachedwithin
                func.setJavaFunction(java(data, body, id, access, modifier, hint, args, attrs, rtnType, output, bufferOutput, displayName, description, returnFormat, secureJson,
                        verifyClient, localMode))
            } else {
                func.register(data.page)
                statement(data, body, CTX_FUNCTION)
            }
            data.setParent(prior)
        } finally {
            data.insideFunction = oldInsideFunction
        }
        func.setEnd(data.srcCode.getPosition())
        if (closure) comments(data)
        return func
    }

    private fun extract(attrs: Array<Attribute?>?, name: String?): Literal? {
        if (attrs == null) return null
        for (attr in attrs) {
            if (name.equalsIgnoreCase(attr.getName())) {
                val `val`: Expression = attr.getValue()
                if (`val` is Literal) return `val` as Literal
                break
            }
        }
        return null
    }

    private fun remove(attrs: Array<Attribute?>?, name: String?): Array<Attribute?>? {
        if (attrs == null) return null
        val list: List<Attribute?> = ArrayList<Attribute?>()
        for (attr in attrs) {
            if (!name.equalsIgnoreCase(attr.getName())) {
                list.add(attr)
            }
        }
        return list.toArray(arrayOfNulls<Attribute?>(list.size()))
    }

    @Throws(TemplateException::class)
    private fun java(data: Data?, body: Body?, functionName: String?, access: Int, modifier: Int, hint: String?, args: ArrayList<Argument?>?, attrs: Array<Attribute?>?, rtnType: String?,
                     output: Boolean?, bufferOutput: Boolean?, displayName: String?, description: String?, returnFormat: Int, secureJson: Boolean?, verifyClient: Boolean?, localMode: Int): JavaFunction? {
        var fd: FunctionDef? = null
        fd = try {
            FunctionDefFactory.getFunctionDef(args, rtnType)
        } catch (e: JavaSourceException) {
            throw TemplateException(data.srcCode, e.getMessage())
        } catch (e: ClassException) {
            throw TemplateException(data.srcCode, e.getMessage())
        }
        val psc: PageSourceCode = data.srcCode as PageSourceCode // TODO get PS in an other way
        val ps: PageSource = psc.getPageSource()
        val sc: SourceCode = data.srcCode
        val start: Position = sc.getPosition()
        findTheEnd(data, start.line)
        val end: Position = sc.getPosition()
        val javaCode: String = sc.substring(start.pos, end.pos - start.pos)
        return try {
            val id: String = data.page.registerJavaFunctionName(functionName)
            // print.e("-->" + (jf.byteCode == null ? -1 : jf.byteCode.length));
            // jf.setTemplateName(ps.getRealpathWithVirtual());
            // jf.setFunctionName(fn);
            JavaCCompiler.compile(ps, fd.createSourceCode(ps, javaCode, id, functionName, access, modifier, hint, args, output, bufferOutput, displayName,
                    description, returnFormat, secureJson, verifyClient, localMode))
        } catch (e: JavaCompilerException) {
            val te = TemplateException(data.srcCode, (start.line + e.getLineNumber()) as Int, e.getColumnNumber() as Int, e.getMessage())
            te.setStackTrace(e.getStackTrace())
            throw te
        } catch (e: Exception) {
            val te = TemplateException(data.srcCode, start.line, 0, e.getMessage())
            te.setStackTrace(e.getStackTrace())
            throw te
        }
    }

    @Throws(TemplateException::class)
    private fun findTheEnd(data: Data?, lineOffset: Int) {
        comments(data)
        val sc: SourceCode = data.srcCode
        if (!sc.forwardIfCurrent('{')) throw TemplateException(sc, "missing starting {")
        var insideD = false
        var insideS = false
        var depth = 0
        var c: Char
        do {
            c = sc.getCurrent()
            if (insideD) {
                if (c == '"') {
                    if (!sc.isPrevious('\\')) insideD = false
                }
            } else if (insideS) {
                if (c == '\'') {
                    if (!sc.isPrevious('\\')) insideS = false
                }
            } else {
                if (c == '{') {
                    depth++
                } else if (c == '}') {
                    if (depth == 0) {
                        sc.next()
                        return
                    } else depth--
                } else if (c == '\'') {
                    insideS = true
                } else if (c == '"') {
                    insideD = true
                }
            }
            if (sc.hasNext()) sc.next() else {
                val pos: Position = sc.getPosition()
                throw TemplateException(sc, pos.line + lineOffset, pos.column, "reached end without finding the ending }")
            }
        } while (true)
    }

    @Override
    @Throws(TemplateException::class)
    protected fun lambdaPart(data: Data?, id: String?, access: Int, modifier: Int, rtnType: String?, line: Position?, args: ArrayList<Argument?>?): Function? {
        val body: Body = FunctionBody(data.factory)
        val func: Function = Lambda(data.page, id, access, modifier, rtnType, body, line, null)
        func.register(data.page)
        comments(data)

        // add arguments
        for (arg in args) {
            func.addArgument(arg.getName(), arg.getType(), arg.getRequired(), arg.getDefaultValue(), arg.isPassByReference(), arg.getDisplayName(), arg.getHint(),
                    arg.getMetaData())
        }
        comments(data)

        // body
        val oldInsideFunction: Boolean = data.insideFunction
        data.insideFunction = true
        try {
            if (data.srcCode.isCurrent('{')) {
                val prior: Body = data.setParent(body)
                statement(data, body, CTX_FUNCTION)
                data.setParent(prior)
            } else {
                if (data.srcCode.forwardIfCurrent("return ")) {
                    comments(data)
                }

                // ex block
                val prior: Short = data.context
                data.context = CTX_FUNCTION
                comments(data)
                val expr: Expression = expression(data)
                val rtn = Return(expr, line, data.srcCode.getPosition())
                body.addStatement(rtn)
                data.docComment = null
                data.context = prior
            }
        } finally {
            data.insideFunction = oldInsideFunction
        }
        func.setEnd(data.srcCode.getPosition())
        comments(data)
        return func
    }

    @Throws(TemplateException::class)
    private fun tagStatement(data: Data?, parent: Body?): Statement? {
        var child: Statement?
        for (i in 0 until data.scriptTags.length) {
            // single
            if (data.scriptTags.get(i).getScript().getType() === TagLibTagScript.TYPE_SINGLE) {
                if (_singleAttrStatement(parent, data, data.scriptTags.get(i)).also { child = it } != null) return child
            } else { // if(tags[i].getScript().getType()==TagLibTagScript.TYPE_MULTIPLE) {
                if (_multiAttrStatement(parent, data, data.scriptTags.get(i)).also { child = it } != null) return child
            }
        }
        return null
    }

    @Throws(TemplateException::class)
    private fun _multiAttrStatement(parent: Body?, data: Data?, tlt: TagLibTag?): Statement? {
        val pos: Int = data.srcCode.getPos()
        return try {
            __multiAttrStatement(parent, data, tlt)
        } catch (e: ProcessingDirectiveException) {
            throw e
        } catch (e: TemplateException) {
            try {
                data.srcCode.setPos(pos)
                expressionStatement(data, parent)
            } catch (e1: TemplateException) {
                if (tlt.getScript().getContext() === CTX_CFC) throw ComponentTemplateException(e)
                throw e
            }
        }
    }

    @Throws(TemplateException::class)
    private fun __multiAttrStatement(parent: Body?, data: Data?, tlt: TagLibTag?): Tag? {
        if (data.ep == null) return null
        val pos: Int = data.srcCode.getPos()
        val type: String = tlt.getName()
        var appendix: String? = null
        if (data.srcCode.forwardIfCurrent(type) ||  // lucee dialect support component as alias for class
                (data.srcCode.getDialect() === CFMLEngine.DIALECT_LUCEE && type.equalsIgnoreCase(Constants.LUCEE_COMPONENT_TAG_NAME)
                        && data.srcCode.forwardIfCurrent(Constants.CFML_COMPONENT_TAG_NAME))) {
            if (tlt.hasAppendix()) {
                appendix = CFMLTransformer.identifier(data.srcCode, false, true)
                if (StringUtil.isEmpty(appendix)) {
                    data.srcCode.setPos(pos)
                    return null
                }
            }
            var isValid = data.srcCode.isCurrent(' ') || tlt.getHasBody() && data.srcCode.isCurrent('{')
            if (isValid && (data.srcCode.isCurrent(" ", "=") || data.srcCode.isCurrent(" ", "("))) { // simply avoid a later exception
                isValid = false
            }
            if (!isValid) {
                data.srcCode.setPos(pos)
                // data.srcCode.setPos(data.srcCode.getPos() - type.length());
                return null
            }
        } else return null
        val line: Position = data.srcCode.getPosition(pos)
        val script: TagLibTagScript = tlt.getScript()
        // TagLibTag tlt = CFMLTransformer.getTLT(data.srcCode,type);
        if (script.getContext() === CTX_CFC) data.isCFC = true else if (script.getContext() === CTX_INTERFACE) data.isInterface = true
        // Tag tag=new TagComponent(line);
        val tag: Tag? = getTag(data, parent, tlt, line, null)
        tag.setTagLibTag(tlt)
        tag.setScriptBase(true)
        if (!StringUtil.isEmpty(appendix)) tag.setAppendix(appendix)

        // add component meta data
        if (data.isCFC) {
            addMetaData(data, tag, IGNORE_LIST_COMPONENT)
        }
        if (data.isInterface) {
            addMetaData(data, tag, IGNORE_LIST_INTERFACE)
        }
        // EvaluatorPool.getPool();
        comments(data)

        // attributes
        // attributes(func,data);
        val attrs: Array<Attribute?>? = attributes(tag, tlt, data, SEMI_BLOCK, data.factory.EMPTY(), if (script.getRtexpr()) Boolean.TRUE else Boolean.FALSE, null, false, ',', false)
        for (i in attrs.indices) {
            tag.addAttribute(attrs!![i])
        }
        comments(data)

        // body
        if (tlt.getHasBody()) {
            val body: Body = BodyBase(data.factory)
            val prior: Body = data.setParent(body)
            val wasSemiColon = statement(data, body, script.getContext())
            if (!wasSemiColon || !tlt.isBodyFree() || body.hasStatements()) tag.setBody(body)
            data.setParent(prior)
        } else checkSemiColonLineFeed(data, true, true, true)
        tag.setEnd(data.srcCode.getPosition())
        eval(tlt, data, tag)
        return tag
    }

    @Throws(TemplateException::class)
    private fun cftagStatement(data: Data?, parent: Body?): Statement? {
        if (data.ep == null) return null // that is because cfloop-contition evaluator does not pass this
        val start: Int = data.srcCode.getPos()

        // namespace and separator
        val tagLib: TagLib = CFMLTransformer.nameSpace(data)
        if (tagLib == null || !tagLib.isCore()) return null

        // print.e("namespace:"+tagLib.getNameSpaceAndSeparator());

        // get the name of the tag
        var id: String? = CFMLTransformer.identifier(data.srcCode, false, true)
        if (id == null) {
            data.srcCode.setPos(start)
            return null
        }
        id = id.toLowerCase()
        var appendix: String? = null
        var tlt: TagLibTag = tagLib.getTag(id)

        /*
		 * Iterator<TagLibTag> it = tagLib.getTags().values().iterator(); while(it.hasNext()) { TagLibTag
		 * tmp = it.next(); if(tmp.getScript()==null) print.e(tmp.getFullName()); }
		 */

        // get taglib
        if (tlt == null) {
            tlt = tagLib.getAppendixTag(id)
            // print.e("appendix:"+tlt);
            if (tlt == null) {
                // if(tagLib.getIgnoreUnknowTags()){ if we do this an expression like the following no longer work
                // cfwhatever=1;
                data.srcCode.setPos(start)
                return null
                // }
                // throw new TemplateException(data.srcCode,"undefined tag
                // ["+tagLib.getNameSpaceAndSeparator()+id+"]");
            }
            appendix = StringUtil.removeStartingIgnoreCase(id, tlt.getName())
        }
        if (tlt.getScript() == null) {
            data.srcCode.setPos(start)
            return null
        }

        // check for opening bracked or closing semicolon
        comments(data)
        var noAttrs = false
        if (!data.srcCode.forwardIfCurrent('(')) {
            noAttrs = if (checkSemiColonLineFeed(data, false, false, false)) {
                true
            } else {
                data.srcCode.setPos(start)
                return null
            }
        }
        val line: Position = data.srcCode.getPosition()

        // script specific behavior
        var context: Short = CTX_OTHER
        val allowExpression: Boolean = Boolean.TRUE
        run {
            val script: TagLibTagScript = tlt.getScript()
            if (script != null) {
                context = script.getContext()
                // always true for this tags allowExpression=script.getRtexpr()?Boolean.TRUE:Boolean.FALSE;
                if (context == CTX_CFC) data.isCFC = true else if (context == CTX_INTERFACE) data.isInterface = true
            }
        }
        val tag: Tag? = getTag(data, parent, tlt, line, null)
        if (appendix != null) {
            tag.setAppendix(appendix)
            tag.setFullname(tlt.getFullName().concat(appendix))
        } else {
            tag.setFullname(tlt.getFullName())
        }
        tag.setTagLibTag(tlt)
        tag.setScriptBase(true)

        // add component meta data
        if (data.isCFC) {
            addMetaData(data, tag, IGNORE_LIST_COMPONENT)
        }
        if (data.isInterface) {
            addMetaData(data, tag, IGNORE_LIST_INTERFACE)
        }
        comments(data)

        // attributes
        val attrs: Array<Attribute?>? = if (noAttrs) arrayOfNulls<Attribute?>(0) else attributes(tag, tlt, data, BRACKED, data.factory.EMPTY(), allowExpression, null, false, ',', true)
        data.srcCode.forwardIfCurrent(')')
        for (i in attrs.indices) {
            tag.addAttribute(attrs!![i])
        }
        comments(data)

        // body
        if (tlt.getHasBody()) {
            val body: Body = BodyBase(data.factory)
            val prior: Body = data.setParent(body)
            val wasSemiColon = statement(data, body, context)
            if (!wasSemiColon || !tlt.isBodyFree() || body.hasStatements()) tag.setBody(body)
            data.setParent(prior)
        } else checkSemiColonLineFeed(data, true, true, true)
        tag.setEnd(data.srcCode.getPosition())
        eval(tlt, data, tag)
        return tag
    }

    private fun addMetaData(data: Data?, tag: Tag?, ignoreList: Array<String?>?) {
        if (data.docComment == null) return
        tag.addMetaData(data.docComment.getHintAsAttribute(data.factory))
        val params: Map<String?, Attribute?> = data.docComment.getParams()
        val it: Iterator<Attribute?> = params.values().iterator()
        var attr: Attribute?
        outer@ while (it.hasNext()) {
            attr = it.next()
            // ignore list
            if (!ArrayUtil.isEmpty(ignoreList)) {
                for (i in ignoreList.indices) {
                    if (ignoreList!![i].equalsIgnoreCase(attr.getName())) continue@outer
                }
            }
            tag.addMetaData(attr)
        }
        data.docComment = null
    }

    @Throws(TemplateException::class)
    private fun propertyStatement(data: Data?, parent: Body?): Statement? {
        val pos: Int = data.srcCode.getPos()
        return try {
            _propertyStatement(data, parent)
        } catch (e: TemplateException) {
            try {
                data.srcCode.setPos(pos)
                expressionStatement(data, parent)
            } catch (e1: TemplateException) {
                throw e
            }
        }
    }

    @Throws(TemplateException::class)
    private fun _propertyStatement(data: Data?, parent: Body?): Tag? {
        if (data.context !== CTX_CFC || !data.srcCode.forwardIfCurrent("property ")) return null
        val line: Position = data.srcCode.getPosition()
        val tlt: TagLibTag = CFMLTransformer.getTLT(data.srcCode, "property", data.config.getIdentification())
        val property: Tag = TagOther(data.factory, line, null)
        addMetaData(data, property, IGNORE_LIST_PROPERTY)
        var hasName = false
        var hasType = false
        val pos: Int = data.srcCode.getPos()
        val tmp = variableDec(data, true)
        if (!StringUtil.isEmpty(tmp)) {
            if (tmp.indexOf('.') !== -1) {
                property.addAttribute(Attribute(false, "type", data.factory.createLitString(tmp), "string"))
                hasType = true
            } else {
                data.srcCode.setPos(pos)
            }
        } else data.srcCode.setPos(pos)

        // folgend wird tlt extra nicht uebergeben, sonst findet pruefung statt
        val attrs: Array<Attribute?>? = attributes(property, tlt, data, SEMI, data.factory.NULL(), Boolean.FALSE, "name", true, NO_ATTR_SEP, false)
        checkSemiColonLineFeed(data, true, true, false)
        property.setTagLibTag(tlt)
        property.setScriptBase(true)
        var attr: Attribute?

        // first fill all regular attribute -> name="value"
        for (i in attrs.indices.reversed()) {
            attr = attrs!![i]
            if (!isNull(attr.getValue())) {
                if (attr.getName().equalsIgnoreCase("name")) {
                    hasName = true
                } else if (attr.getName().equalsIgnoreCase("type")) {
                    hasType = true
                }
                property.addAttribute(attr)
            }
        }

        // now fill name named attributes -> attr1 attr2
        var first: String? = null
        var second: String? = null
        for (i in attrs.indices) {
            attr = attrs!![i]
            if (isNull(attr.getValue())) {
                // type
                if (first == null && (!hasName && !hasType || !hasName)) {
                    first = attr.getNameOC()
                } else if (second == null && !hasName && !hasType) {
                    second = attr.getNameOC()
                } else {
                    attr = Attribute(true, attr.getName(), data.factory.EMPTY(), "string")
                    property.addAttribute(attr)
                }
            }
        }
        if (first != null) {
            hasName = true
            if (second != null) {
                hasType = true
                property.addAttribute(Attribute(false, "name", data.factory.createLitString(second), "string"))
                property.addAttribute(Attribute(false, "type", data.factory.createLitString(first), "string"))
            } else {
                property.addAttribute(Attribute(false, "name", data.factory.createLitString(first), "string"))
            }
        }
        if (!hasType) {
            property.addAttribute(Attribute(false, "type", data.factory.createLitString("any"), "string"))
        }
        if (!hasName) throw TemplateException(data.srcCode, "missing name declaration for property")

        /*
		 * Tag property=new TagBase(line); property.setTagLibTag(tlt); property.addAttribute(new
		 * Attribute(false,"name",data.factory.createLitString(name),"string")); property.addAttribute(new
		 * Attribute(false,"type",data.factory.createLitString(type),"string"));
		 */property.setEnd(data.srcCode.getPosition())
        return property
    }

    private fun isNull(value: Expression?): Boolean {
        if (value == null) return true
        if (value is Null) return true
        return if (value is Literal && (value as Literal?).getString() == null) true else false
    }

    @Throws(TemplateException::class)
    private fun staticStatement(data: Data?, parent: Body?): Tag? {
        if (!data.srcCode.forwardIfCurrent("static", '{')) return null
        // get one back to have again { so the parser works
        data.srcCode.previous()
        val start: Position = data.srcCode.getPosition()
        val tag: TagOther? = createStaticTag(data, start)
        val body: Body = tag.getBody()
        val prior: Body = data.setParent(body)
        statement(data, body, CTX_STATIC)
        data.setParent(prior)
        return tag
    }

    @Throws(TemplateException::class)
    fun paramStatement(data: Data?, parent: Body?): Statement? {
        val pos: Int = data.srcCode.getPos()
        return try {
            _paramStatement(data, parent)
        } catch (e: TemplateException) {
            try {
                data.srcCode.setPos(pos)
                expressionStatement(data, parent)
            } catch (e1: TemplateException) {
                throw e
            }
        }
    }

    @Throws(TemplateException::class)
    private fun _paramStatement(data: Data?, parent: Body?): Tag? {
        if (!data.srcCode.forwardIfCurrent("param ")) return null
        val line: Position = data.srcCode.getPosition()
        val tlt: TagLibTag = CFMLTransformer.getTLT(data.srcCode, "param", data.config.getIdentification())
        val param = TagParam(data.factory, line, null)

        // type
        var hasType = false
        var hasName = false
        val pos: Int = data.srcCode.getPos()

        // first 2 arguments can be type/name directly
        val tmp = variableDec(data, true)
        do {
            if (!StringUtil.isEmpty(tmp)) {
                var attr: TagLibTagAttr = tlt.getAttribute(tmp.toLowerCase(), true)
                // name is not a defined attribute
                if (attr == null) {
                    comments(data)

                    // it could be a name followed by default value
                    if (data.srcCode.forwardIfCurrent('=')) {
                        comments(data)
                        val v: Expression? = attributeValue(data, true)
                        param.addAttribute(Attribute(false, "name", data.factory.createLitString(tmp), "string"))
                        param.addAttribute(Attribute(false, "default", v, "string"))
                        hasName = true
                        break // if we had a value this was already name
                    }
                    // can be type or name
                    val pos2: Int = data.srcCode.getPos()

                    // first could be type, followed by name
                    comments(data)
                    val tmp2 = variableDec(data, true)
                    if (!StringUtil.isEmpty(tmp2)) {
                        attr = tlt.getAttribute(tmp2.toLowerCase(), true)
                        if (attr == null) {
                            param.addAttribute(Attribute(false, "name", data.factory.createLitString(tmp2), "string"))
                            param.addAttribute(Attribute(false, "type", data.factory.createLitString(tmp), "string"))
                            if (data.srcCode.forwardIfCurrent('=')) {
                                val v: Expression? = attributeValue(data, true)
                                param.addAttribute(Attribute(false, "default", v, "string"))
                            }
                            hasName = true
                            hasType = true
                            break
                        }
                    }
                    param.addAttribute(Attribute(false, "name", data.factory.createLitString(tmp), "string"))
                    data.srcCode.setPos(pos2)
                    hasName = true
                } else data.srcCode.setPos(pos)
            } else data.srcCode.setPos(pos)
        } while (false)

        // folgend wird tlt extra nicht uebergeben, sonst findet pruefung statt
        val attrs: Array<Attribute?>? = attributes(param, tlt, data, SEMI, data.factory.NULL(), Boolean.TRUE, "name", true, ',', false)
        checkSemiColonLineFeed(data, true, true, true)
        param.setTagLibTag(tlt)
        param.setScriptBase(true)
        var attr: Attribute?

        // first fill all regular attribute -> name="value"
        var hasDynamic = false
        for (i in attrs.indices.reversed()) {
            attr = attrs!![i]
            if (!isNull(attr.getValue())) {
                if (attr.getName().equalsIgnoreCase("name")) {
                    hasName = true
                    param.addAttribute(attr)
                } else if (attr.getName().equalsIgnoreCase("type")) {
                    hasType = true
                    param.addAttribute(attr)
                } else if (attr.isDynamicType()) {
                    hasName = true
                    if (hasDynamic) throw attrNotSupported(data.srcCode, tlt, attr.getName())
                    hasDynamic = true
                    param.addAttribute(Attribute(false, "name", data.factory.createLitString(attr.getName()), "string"))
                    param.addAttribute(Attribute(false, "default", attr.getValue(), "any"))
                } else param.addAttribute(attr)
            }
        }

        // now fill name named attributes -> attr1 attr2
        var first: String? = null
        var second: String? = null
        for (i in attrs.indices) {
            attr = attrs!![i]
            if (isNull(attr.getValue())) {
                // type
                if (first == null && (!hasName || !hasType)) {
                    first = attr.getName()
                } else if (second == null && !hasName && !hasType) {
                    second = attr.getName()
                } else {
                    attr = Attribute(true, attr.getName(), data.factory.EMPTY(), "string")
                    param.addAttribute(attr)
                }
            }
        }
        if (first != null) {
            if (second != null) {
                hasName = true
                hasType = true
                if (hasDynamic) throw attrNotSupported(data.srcCode, tlt, first)
                hasDynamic = true
                param.addAttribute(Attribute(false, "name", data.factory.createLitString(second), "string"))
                param.addAttribute(Attribute(false, "type", data.factory.createLitString(first), "string"))
            } else {
                param.addAttribute(Attribute(false, if (hasName) "type" else "name", data.factory.createLitString(first), "string"))
                hasName = true
            }
        }

        // if(!hasType)
        // param.addAttribute(ANY);
        if (!hasName) throw TemplateException(data.srcCode, "missing name declaration for param")
        param.setEnd(data.srcCode.getPosition())
        return param
    }

    private fun attrNotSupported(cfml: SourceCode?, tag: TagLibTag?, id: String?): TemplateException? {
        val names: String = tag.getAttributeNames()
        return if (StringUtil.isEmpty(names)) TemplateException(cfml, "Attribute [" + id + "] is not allowed for tag [" + tag.getFullName() + "]") else TemplateException(cfml, "Attribute [" + id + "] is not allowed for statement [" + tag.getName() + "]", "valid attribute names are [$names]")
    }

    private fun variableDec(data: Data?, firstCanBeNumber: Boolean): String? {
        var id: String? = identifier(data, firstCanBeNumber) ?: return null
        val rtn = StringBuffer(id)
        data.srcCode.removeSpace()
        while (data.srcCode.forwardIfCurrent('.')) {
            data.srcCode.removeSpace()
            rtn.append('.')
            id = identifier(data, firstCanBeNumber)
            if (id == null) return null
            rtn.append(id)
            data.srcCode.removeSpace()
        }
        while (data.srcCode.forwardIfCurrent("[]")) {
            data.srcCode.removeSpace()
            rtn.append("[]")
        }
        data.srcCode.revertRemoveSpace()
        return rtn.toString()
    }

    /**
     * Liest ein return Statement ein. <br></br>
     * EBNF:<br></br>
     * `spaces expressionStatement spaces;`
     *
     * @return return Statement
     * @throws TemplateException
     */
    @Throws(TemplateException::class)
    private fun returnStatement(data: Data?): Return? {
        if (!data.srcCode.forwardIfCurrentAndNoVarExt("return")) return null
        val line: Position = data.srcCode.getPosition()
        val rtn: Return?
        comments(data)
        if (checkSemiColonLineFeed(data, false, false, false)) rtn = Return(data.factory, line, data.srcCode.getPosition()) else {
            val expr: Expression = expression(data)
            checkSemiColonLineFeed(data, true, true, false)
            rtn = Return(expr, line, data.srcCode.getPosition())
        }
        comments(data)
        return rtn
    }

    @Throws(TemplateException::class)
    private fun islandStatement(data: Data?, parent: Body?): Boolean {
        if (!data.srcCode.forwardIfCurrent(TAG_ISLAND_INDICATOR)) return false
        // now we have to jump into the tag parser
        val tag = CFMLTransformer(true)
        tag.transform(data, parent)
        if (!data.srcCode.forwardIfCurrent(TAG_ISLAND_INDICATOR)) throw TemplateException(data.srcCode, "missing closing tag indicator [" + TAG_ISLAND_INDICATOR + "]")
        comments(data)
        return true
    }

    @Throws(TemplateException::class)
    private fun _singleAttrStatement(parent: Body?, data: Data?, tlt: TagLibTag?): Statement? {
        val pos: Int = data.srcCode.getPos()
        return try {
            __singleAttrStatement(parent, data, tlt, false)
        } catch (e: ProcessingDirectiveException) {
            throw e
        } catch (e: TemplateException) {
            data.srcCode.setPos(pos)
            try {
                expressionStatement(data, parent)
            } catch (e1: TemplateException) {
                throw e
            }
        }
    }

    @Throws(TemplateException::class)
    private fun __singleAttrStatement(parent: Body?, data: Data?, tlt: TagLibTag?, allowTwiceAttr: Boolean): Statement? {
        val tagName: String = tlt.getName()
        if (data.srcCode.forwardIfCurrent(tagName)) {
            if (!data.srcCode.isCurrent(' ') && !data.srcCode.isCurrent(';')) {
                data.srcCode.setPos(data.srcCode.getPos() - tagName.length())
                return null
            }
        } else return null
        val pos: Int = data.srcCode.getPos() - tagName.length()
        val line: Position = data.srcCode.getPosition()
        // TagLibTag tlt =
        // CFMLTransformer.getTLT(data.srcCode,tagName.equals("pageencoding")?"processingdirective":tagName);
        val tag: Tag? = getTag(data, parent, tlt, line, null)
        tag.setScriptBase(true)
        tag.setTagLibTag(tlt)
        comments(data)

        // attribute
        val attr: TagLibTagAttr = tlt.getScript().getSingleAttr()
        var attrName: String? = null
        var attrValue: Expression? = null
        var attrType = ATTR_TYPE_NONE
        if (attr != null) {
            attrType = attr.getScriptSupport()
            val c: Char = data.srcCode.getCurrent()
            if (ATTR_TYPE_REQUIRED == attrType || !data.srcCode.isCurrent(';') && ATTR_TYPE_OPTIONAL == attrType) {
                if (data.srcCode.isCurrent('{')) { // this can be only a json string
                    val p: Int = data.srcCode.getPos()
                    try {
                        attrValue = if (isSimpleValue(attr.getType())) null else json(data, JSON_STRUCT, '{', '}')
                    } catch (t: Throwable) {
                        ExceptionUtil.rethrowIfNecessary(t)
                        data.srcCode.setPos(p)
                    }
                } else attrValue = attributeValue(data, tlt.getScript().getRtexpr())
                if (attrValue != null && isOperator(c)) {
                    data.srcCode.setPos(pos)
                    return null
                }
            }
        }
        if (attrValue != null) {
            attrName = attr.getName()
            val tlta: TagLibTagAttr = tlt.getAttribute(attr.getName(), true)
            tag.addAttribute(Attribute(false, attrName, data.factory.toExpression(attrValue, tlta.getType()), tlta.getType()))
        } else if (ATTR_TYPE_REQUIRED == attrType) {
            data.srcCode.setPos(pos)
            return null
        }
        // body
        if (tlt.getHasBody()) {
            val body: Body = BodyBase(data.factory)
            val prior: Body = data.setParent(body)
            val wasSemiColon = statement(data, body, tlt.getScript().getContext())
            if (!wasSemiColon || !tlt.isBodyFree() || body.hasStatements()) tag.setBody(body)
            data.setParent(prior)
        } else checkSemiColonLineFeed(data, true, true, true)
        if (tlt.hasTTE()) data.ep.add(tlt, tag, data.flibs, data.srcCode)
        if (!StringUtil.isEmpty(attrName)) validateAttributeName(attrName, data.srcCode, ArrayList<String?>(), tlt, RefBooleanImpl(false), StringBuffer(), allowTwiceAttr)
        tag.setEnd(data.srcCode.getPosition())
        eval(tlt, data, tag)
        return tag
    }

    private fun isSimpleValue(type: String?): Boolean {
        return type.equalsIgnoreCase("string") || type.equalsIgnoreCase("boolean") || type.equalsIgnoreCase("number") || type.equalsIgnoreCase("numeric")
    }

    private fun isOperator(c: Char): Boolean {
        return c == '=' || c == '+' || c == '-'
    }

    /*
	 * protected Statement __singleAttrStatement(Body parent, Data data, String tagName,String
	 * attrName,int attrType, boolean allowExpression, boolean allowTwiceAttr) throws TemplateException
	 * {
	 * 
	 * if(data.srcCode.forwardIfCurrent(tagName)){ if(!data.srcCode.isCurrent(' ') &&
	 * !data.srcCode.isCurrent(';')){ data.srcCode.setPos(data.srcCode.getPos()-tagName.length());
	 * return null; } } else return null;
	 * 
	 * 
	 * int pos=data.srcCode.getPos()-tagName.length(); int line=data.srcCode.getLine(); TagLibTag tlt =
	 * CFMLTransformer.getTLT(data.srcCode,tagName.equals("pageencoding")?"processingdirective":tagName)
	 * ;
	 * 
	 * Tag tag=getTag(parent,tlt,line); tag.setScriptBase(true); tag.setTagLibTag(tlt);
	 * 
	 * comments(data);
	 * 
	 * // attribute Expression attrValue=null; if(ATTR_TYPE_REQUIRED==attrType ||
	 * (!data.srcCode.isCurrent(';') && ATTR_TYPE_OPTIONAL==attrType)) attrValue =attributeValue(data,
	 * allowExpression); //allowExpression?super.expression(data):string(data);
	 * 
	 * if(attrValue!=null){ TagLibTagAttr tlta = tlt.getAttribute(attrName); tag.addAttribute(new
	 * Attribute(false,attrName,Cast.toExpression(attrValue,tlta.getType()),tlta.getType())); } else
	 * if(ATTR_TYPE_REQUIRED==attrType){ data.srcCode.setPos(pos); return null; }
	 * 
	 * checkSemiColonLineFeed(data,true); if(!StringUtil.isEmpty(tlt.getTteClassName()))data.ep.add(tlt,
	 * tag, data.fld, data.srcCode);
	 * 
	 * if(!StringUtil.isEmpty(attrName))validateAttributeName(attrName, data.srcCode, new
	 * ArrayList<String>(), tlt, new RefBooleanImpl(false), new StringBuffer(), allowTwiceAttr);
	 * 
	 * eval(tlt,data,tag); return tag; }
	 */
    @Throws(TemplateException::class)
    private fun eval(tlt: TagLibTag?, data: Data?, tag: Tag?) {
        if (tlt.hasTTE()) {
            try {
                tlt.getEvaluator().execute(data.config, tag, tlt, data.flibs, data)
            } catch (e: EvaluatorException) {
                throw TemplateException(data.srcCode, e.getMessage())
            }
            data.ep.add(tlt, tag, data.flibs, data.srcCode)
        }
    }

    @Throws(TemplateException::class)
    private fun getTag(data: Data?, parent: Body?, tlt: TagLibTag?, start: Position?, end: Position?): Tag? {
        return try {
            val tag: Tag = tlt.getTag(data.factory, start, end)
            tag.setParent(parent)
            tag
        } catch (e: TagLibException) {
            throw TemplateException(data.srcCode, e)
        }
    }

    /**
     * List mithilfe des data.CFMLExprTransformer einen Ausruck ein. <br></br>
     * EBNF:<br></br>
     * `expression ";";`
     *
     * @param parent
     * @return Ausdruck
     * @throws TemplateException
     */
    @Throws(TemplateException::class)
    private fun expressionStatement(data: Data?, parent: Body?): Statement? {

        // first we check if we have an access modifier
        val pos: Int = data.srcCode.getPos()
        var access = -1
        var _final = false
        if (data.context === CTX_CFC || data.context === CTX_STATIC) {
            if (data.srcCode.forwardIfCurrent("final ")) {
                _final = true
                comments(data)
            }
            if (data.srcCode.forwardIfCurrent("private ")) {
                access = Component.ACCESS_PRIVATE
                comments(data)
            } else if (data.srcCode.forwardIfCurrent("package ")) {
                access = Component.ACCESS_PACKAGE
                comments(data)
            } else if (data.srcCode.forwardIfCurrent("public ")) {
                access = Component.ACCESS_PUBLIC
                comments(data)
            } else if (data.srcCode.forwardIfCurrent("remote ")) {
                access = Component.ACCESS_REMOTE
                throw TemplateException(data.srcCode, "access modifier [remote] not supported in this context")
            }
            if (!_final && data.srcCode.forwardIfCurrent("final ")) {
                _final = true
                comments(data)
            }
        }
        val prior: Body = data.setParent(parent)
        var expr: Expression? = expression(data)
        data.setParent(prior)
        checkSemiColonLineFeed(data, true, true, false)

        // variable declaration (variable in body)
        if (expr is Variable) {
            val v: Variable? = expr as Variable?
            if (ASMUtil.isOnlyDataMember(v)) {
                expr = Assign(v, data.factory.createEmpty(), data.srcCode.getPosition())
            }
        }

        // if a specific access was defined
        if (access > -1 || _final) {
            if (expr !is Assign) {
                data.srcCode.setPos(pos)
                throw TemplateException(data.srcCode, "invalid syntax, access modifier cannot be used in this context")
            }
            if (access > -1) {
                // this is only supported with the Lucee dialect
                // if(data.srcCode.getDialect()==CFMLEngine.DIALECT_CFML)
                // throw new TemplateException(data.srcCode,
                // "invalid syntax, access modifier cannot be used in this context");
                (expr as Assign?).setAccess(access)
            }
            if (_final) (expr as Assign?).setModifier(Member.MODIFIER_FINAL)
        }
        return if (expr is FunctionAsExpression) (expr as FunctionAsExpression?).getFunction() else ExpressionAsStatement(expr)
    }

    @Throws(TemplateException::class)
    private fun checkSemiColonLineFeed(data: Data?, throwError: Boolean, checkNLBefore: Boolean, allowEmptyCurlyBracked: Boolean): Boolean {
        comments(data)
        if (!data.srcCode.forwardIfCurrent(';')) {

            // curly brackets?
            if (allowEmptyCurlyBracked) {
                val pos: Int = data.srcCode.getPos()
                if (data.srcCode.forwardIfCurrent('{')) {
                    comments(data)
                    if (data.srcCode.forwardIfCurrent('}')) return true
                    data.srcCode.setPos(pos)
                }
            }
            if ((!checkNLBefore || !data.srcCode.hasNLBefore()) && !isFinish(data) && !data.srcCode.isCurrent('}')) {
                if (!throwError) return false
                throw TemplateException(data.srcCode, "Missing [;] or [line feed] after expression")
            }
        }
        return true
    }

    /**
     * Ruft die Methode expression der zu vererbenten Klasse auf und prueft ob der Rueckgabewert einen
     * boolschen Wert repraesentiert und castet den Wert allenfalls. <br></br>
     * EBNF:<br></br>
     * `TemplateException::expression;`
     *
     * @return condition
     * @throws TemplateException
     */
    @Throws(TemplateException::class)
    private fun condition(data: Data?): ExprBoolean? {
        var condition: ExprBoolean? = null
        comments(data)
        condition = data.factory.toExprBoolean(super.expression(data))
        comments(data)
        return condition
    }

    /**
     * Liest eine try Block ein <br></br>
     * EBNF:<br></br>
     * `;`
     *
     * @return Try Block
     * @throws TemplateException
     */
    @Throws(TemplateException::class)
    private fun tryStatement(data: Data?): TryCatchFinally? {
        if (!data.srcCode.forwardIfCurrent("try", '{') && !data.srcCode.forwardIfCurrent("try ") && !data.srcCode.forwardIfCurrent("try", '/')) return null
        data.srcCode.previous()
        val body: Body = BodyBase(data.factory)
        val tryCatchFinally = TryCatchFinally(data.factory, body, data.srcCode.getPosition(), null)
        val prior: Body = data.setParent(body)
        statement(data, body, CTX_TRY)
        data.setParent(prior)
        comments(data)

        // catches
        var catchCount: Short = 0
        while (data.srcCode.forwardIfCurrent("catch", '(')) {
            catchCount++
            comments(data)

            // type
            val pos: Int = data.srcCode.getPos()
            val line: Position = data.srcCode.getPosition()
            var name: Expression? = null
            var type: Expression? = null
            val sbType = StringBuffer()
            var id: String?
            while (true) {
                id = identifier(data, false)
                if (id == null) break
                sbType.append(id)
                data.srcCode.removeSpace()
                if (!data.srcCode.forwardIfCurrent('.')) break
                sbType.append('.')
                data.srcCode.removeSpace()
            }
            if (sbType.length() === 0) {
                type = string(data)
                if (type == null) throw TemplateException(data.srcCode, "a catch statement must begin with the throwing type (query, application ...).")
            } else {
                type = data.factory.createLitString(sbType.toString())
            }

            // name = expression();
            comments(data)

            // name
            if (!data.srcCode.isCurrent(')')) {
                name = expression(data)
            } else {
                data.srcCode.setPos(pos)
                name = expression(data)
                type = data.factory.createLitString("any")
            }
            comments(data)
            val b: Body = BodyBase(data.factory)
            try {
                tryCatchFinally.addCatch(null, type, name, b, line)
            } catch (e: TransformerException) {
                throw TemplateException(data.srcCode, e.getMessage())
            }
            comments(data)
            if (!data.srcCode.forwardIfCurrent(')')) throw TemplateException(data.srcCode, "invalid catch statement, missing closing )")
            val _prior: Body = data.setParent(b)
            statement(data, b, CTX_CATCH)
            data.setParent(_prior)
            comments(data)
        }

        // finally
        if (finallyStatement(data, tryCatchFinally)) {
            comments(data)
        } else if (catchCount.toInt() == 0) throw TemplateException(data.srcCode, "a try statement must have at least one catch statement")

        // if(body.isEmpty()) return null;
        tryCatchFinally.setEnd(data.srcCode.getPosition())
        return tryCatchFinally
    }

    /**
     * Prueft ob sich der Zeiger am Ende eines Script Blockes befindet
     *
     * @return Ende ScriptBlock?
     * @throws TemplateException
     */
    @Throws(TemplateException::class)
    private fun isFinish(data: Data?): Boolean {
        comments(data)
        return if (data.tagName == null) false else data.srcCode.isCurrent("</", data.tagName)
    }

    /**
     * Liest den Block mit Statements ein. <br></br>
     * EBNF:<br></br>
     * `"{" spaces {statements} "}" | statement;`
     *
     * @param block
     * @return was a block
     * @throws TemplateException
     */
    @Throws(TemplateException::class)
    private fun block(data: Data?, body: Body?): Boolean {
        if (!data.srcCode.forwardIfCurrent('{')) return false
        comments(data)
        if (data.srcCode.forwardIfCurrent('}')) {
            return true
        }
        statements(data, body, false)
        if (!data.srcCode.forwardIfCurrent('}')) throw TemplateException(data.srcCode, "Missing ending [}]")
        return true
    }

    @Throws(TemplateException::class)
    private fun attributes(tag: Tag?, tlt: TagLibTag?, data: Data?, endCond: EndCondition?, defaultValue: Expression?, oAllowExpression: Object?, ignoreAttrReqFor: String?,
                           allowTwiceAttr: Boolean, attributeSeparator: Char, allowColonAsNameValueSeparator: Boolean): Array<Attribute?>? {
        val attrs: Map<String?, Attribute?> = LinkedHashMap<String?, Attribute?>() // need to be linked hashmap to keep the right order
        val ids: ArrayList<String?> = ArrayList<String?>()
        while (data.srcCode.isValidIndex()) {
            data.srcCode.removeSpace()
            // if no more attributes break
            if (endCond!!.isEnd(data)) break
            val attr: Attribute? = attribute(tlt, data, ids, defaultValue, oAllowExpression, allowTwiceAttr, allowColonAsNameValueSeparator)
            attrs.put(attr.getName().toLowerCase(), attr)

            // seperator
            if (attributeSeparator.toInt() > 0) {
                data.srcCode.removeSpace()
                data.srcCode.forwardIfCurrent(attributeSeparator)
            }
        }

        // not defined attributes
        if (tlt != null) {
            var hasAttributeCollection = false
            val iii: Iterator<Attribute?> = attrs.values().iterator()
            while (iii.hasNext()) {
                if ("attributecollection".equalsIgnoreCase(iii.next().getName())) {
                    hasAttributeCollection = true
                    break
                }
            }
            val type: Int = tlt.getAttributeType()
            if (type == TagLibTag.ATTRIBUTE_TYPE_FIXED || type == TagLibTag.ATTRIBUTE_TYPE_MIXED) {
                val hash: Map<String?, TagLibTagAttr?> = tlt.getAttributes()
                val it: Iterator<Entry<String?, TagLibTagAttr?>?> = hash.entrySet().iterator()
                var e: Entry<String?, TagLibTagAttr?>?
                while (it.hasNext()) {
                    e = it.next()
                    val att: TagLibTagAttr = e.getValue()
                    if (att.isRequired() && !contains(attrs.values(), att) && att.getDefaultValue() == null && !att.getName().equals(ignoreAttrReqFor)) {
                        if (!hasAttributeCollection) throw TemplateException(data.srcCode, "attribute [" + att.getName().toString() + "] is required for statement [" + tlt.getName().toString() + "]")
                        if (tag != null) tag.addMissingAttribute(att)
                    }
                }
            }
        }

        // set default values
        if (tlt != null && tlt.hasDefaultValue()) {
            val hash: Map<String?, TagLibTagAttr?> = tlt.getAttributes()
            val it: Iterator<TagLibTagAttr?> = hash.values().iterator()
            var att: TagLibTagAttr?
            while (it.hasNext()) {
                att = it.next()
                if (!attrs.containsKey(att.getName().toLowerCase()) && att.hasDefaultValue()) {
                    val attr = Attribute(tlt.getAttributeType() === TagLibTag.ATTRIBUTE_TYPE_DYNAMIC, att.getName(),
                            data.factory.toExpression(data.factory.createLitString(Caster.toString(att.getDefaultValue(), null)), att.getType()), att.getType())
                    attr.setDefaultAttribute(true)
                    attrs.put(att.getName().toLowerCase(), attr)
                }
            }
        }
        return attrs.values().toArray(arrayOfNulls<Attribute?>(attrs.size()))
    }

    private fun contains(attrs: Collection<Attribute?>?, attr: TagLibTagAttr?): Boolean {
        val it: Iterator<Attribute?> = attrs!!.iterator()
        var name: String
        var alias: Array<String?>
        while (it.hasNext()) {
            name = it.next().getName()

            // check name
            if (name.equals(attr.getName())) return true

            // and aliases
            alias = attr.getAlias()
            if (!ArrayUtil.isEmpty(alias)) for (i in alias.indices) {
                if (alias[i]!!.equals(attr.getName())) return true
            }
        }
        return false
    }

    @Throws(TemplateException::class)
    private fun attribute(tlt: TagLibTag?, data: Data?, args: ArrayList<String?>?, defaultValue: Expression?, oAllowExpression: Object?, allowTwiceAttr: Boolean,
                          allowColonSeparator: Boolean): Attribute? {
        val sbType = StringBuffer()
        val dynamic: RefBoolean = RefBooleanImpl(false)

        // Name
        val name = attributeName(data.srcCode, args, tlt, dynamic, sbType, allowTwiceAttr, !allowColonSeparator)
        var nameLC: String = name?.toLowerCase()
        var allowExpression = false
        if (oAllowExpression is Boolean) allowExpression = (oAllowExpression as Boolean?).booleanValue() else if (oAllowExpression is String) allowExpression = (oAllowExpression as String?).equalsIgnoreCase(nameLC)
        var value: Expression? = null
        comments(data)

        // value
        val hasValue = data.srcCode.forwardIfCurrent('=') || allowColonSeparator && data.srcCode.forwardIfCurrent(':')
        value = if (hasValue) {
            comments(data)
            attributeValue(data, allowExpression)
        } else {
            defaultValue
        }
        comments(data)

        // Type
        var tlta: TagLibTagAttr? = null
        if (tlt != null) {
            tlta = tlt.getAttribute(nameLC, true)
            if (tlta != null && tlta.getName() != null) nameLC = tlta.getName()
        }
        return Attribute(dynamic.toBooleanValue(), name, if (tlta != null) data.factory.toExpression(value, tlta.getType()) else value, sbType.toString(), !hasValue)
    }

    @Throws(TemplateException::class)
    private fun attributeName(cfml: SourceCode?, args: ArrayList<String?>?, tag: TagLibTag?, dynamic: RefBoolean?, sbType: StringBuffer?, allowTwiceAttr: Boolean, allowColon: Boolean): String? {
        val id: String = CFMLTransformer.identifier(cfml, true, allowColon)
        return validateAttributeName(id, cfml, args, tag, dynamic, sbType, allowTwiceAttr)
    }

    @Throws(TemplateException::class)
    private fun validateAttributeName(idOC: String?, cfml: SourceCode?, args: ArrayList<String?>?, tag: TagLibTag?, dynamic: RefBoolean?, sbType: StringBuffer?, allowTwiceAttr: Boolean): String? {
        var idOC = idOC
        var idLC: String? = idOC.toLowerCase()
        if (args.contains(idLC) && !allowTwiceAttr) throw TemplateException(cfml, "you can't use the same attribute [$idOC] twice")
        args.add(idLC)
        if (tag == null) return idOC
        val typeDef: Int = tag.getAttributeType()
        if ("attributecollection".equals(idLC)) {
            dynamic.setValue(tag.getAttribute(idLC, true) == null)
            sbType.append("struct")
        } else if (typeDef == TagLibTag.ATTRIBUTE_TYPE_FIXED || typeDef == TagLibTag.ATTRIBUTE_TYPE_MIXED) {
            val attr: TagLibTagAttr = tag.getAttribute(idLC, true)
            if (attr == null) {
                if (typeDef == TagLibTag.ATTRIBUTE_TYPE_FIXED) {
                    val names: String = tag.getAttributeNames()
                    if (StringUtil.isEmpty(names)) throw TemplateException(cfml, "Attribute [" + idOC + "] is not allowed for tag [" + tag.getFullName() + "]")
                    throw TemplateException(cfml, "Attribute [" + idOC + "] is not allowed for statement [" + tag.getName() + "]", "Valid Attribute names are [$names]")
                }
                dynamic.setValue(true)
            } else {
                idOC = attr.getName()
                idLC = idOC.toLowerCase()
                sbType.append(attr.getType())
                // parseExpression[0]=attr.getRtexpr();
            }
        } else if (typeDef == TagLibTag.ATTRIBUTE_TYPE_DYNAMIC) {
            dynamic.setValue(true)
        }
        return idOC
    }

    @Throws(TemplateException::class)
    private fun attributeValue(data: Data?, allowExpression: Boolean): Expression? {
        return if (allowExpression) super.expression(data) else transformAsString(data, arrayOf<String?>(" ", ";", "{"))
    }

    interface EndCondition {
        fun isEnd(data: Data?): Boolean
    }

    companion object {
        private val IGNORE_LIST_COMPONENT: Array<String?>? = arrayOf("output", "synchronized", "extends", "implements", "displayname", "style", "persistent", "accessors")
        private val IGNORE_LIST_INTERFACE: Array<String?>? = arrayOf("output", "extends", "displayname", "style", "persistent", "accessors")
        private val IGNORE_LIST_PROPERTY: Array<String?>? = arrayOf("default", "fieldtype", "name", "type", "persistent", "remotingFetch", "column", "generator", "length",
                "ormtype", "params", "unSavedValue", "dbdefault", "formula", "generated", "insert", "optimisticlock", "update", "notnull", "precision", "scale", "unique", "uniquekey",
                "source")
        private val SEMI_BLOCK: EndCondition? = object : EndCondition {
            @Override
            override fun isEnd(data: Data?): Boolean {
                return data.srcCode.isCurrent('{') || data.srcCode.isCurrent(';')
            }
        }
        private val SEMI: EndCondition? = object : EndCondition {
            @Override
            override fun isEnd(data: Data?): Boolean {
                return data.srcCode.isCurrent(';')
            }
        }
        private val COMMA_ENDBRACKED: EndCondition? = object : EndCondition {
            @Override
            override fun isEnd(data: Data?): Boolean {
                return data.srcCode.isCurrent(',') || data.srcCode.isCurrent(')')
            }
        }
        private val BRACKED: EndCondition? = object : EndCondition {
            @Override
            override fun isEnd(data: Data?): Boolean {
                return data.srcCode.isCurrent(')')
            }
        }

        // private static final Expression NULL = data.factory.createLitString("NULL");
        // private static final Attribute ANY = new
        // Attribute(false,"type",data.factory.createLitString("any"),"string");
        private const val NO_ATTR_SEP = 0.toChar()
        val TAG_ISLAND_INDICATOR: String? = "```"
        @Throws(TemplateException::class)
        fun createStaticTag(data: Data?, start: Position?): TagOther? {
            val tlt: TagLibTag = CFMLTransformer.getTLT(data.srcCode, "static", data.config.getIdentification())
            val body = BodyBase(data.factory)
            val tag = TagOther(data.factory, start, data.srcCode.getPosition())
            tag.setTagLibTag(tlt)
            tag.setBody(body)
            data.ep.add(tlt, tag, data.flibs, data.srcCode)
            return tag
        }
    }
}