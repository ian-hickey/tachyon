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
package lucee.transformer.cfml.tag

import java.io.IOException

/**
 * <pre>
 * EBNF (Extended Backus-Naur Form)
 *
 * transform	= {body}
 * body		= [comment] ("</pre>" | "<" tag body | literal body);
 * comment		= ""} "--->";
 * literal		= ("<" | {?-"#"-"<"} "<" | {"#" expression "#"} "<" ) | ({?-"<"} "<")
 * (* Welcher Teil der "oder" Bedingung ausgefuehrt wird, ist abhaengig was die Tag-Lib vorgibt,
 * dass Expression geparst werden sollen oder nicht. *)
 * tag		= name-space identifier spaces attributes ("/>" | ">" [body "" identifier spaces ">"]);
 * (* Ob dem Tag ein Body und ein End-Tag folgt ist abhaengig von Definition des body-content in Tag-Lib, gleices gilt fuer appendix *)
 * name-space	= < tagLib[].getNameSpaceAndSeperator() >;
 * (* Vergleicht Zeichen mit den Namespacedefinitionen der Tag Libraries. *)
 * attributes	= ({spaces attribute} "/>" | {spaces attribute} ">") | attribute-value;
 * (* Welcher Teil der "oder" Bedingung ausgefuehrt wird, ist abhaengig von der Tag Attribute Definition in der Tag Lib. *)
 * attribute	= attribute-name  spaces "=" spaces attribute-value;
 * attribute-name	= ("expression"|'expression'|expression) | identifier;
 * (* Ruft identifier oder den Expression Transformer auf je nach Attribute Definition in der Tag Lib. *)
 * attribute-value	= expression;
 * identifier     	= (letter | "_") {letter | "_"|digit};
 * letter			= "a".."z"|"A".."Z";
 * digit			= "0".."9";
 * expression      = <ExprTransfomer.expression></ExprTransfomer.expression>()>; (* Ruft den Expression Transformer auf. *)
 * spaces         = {space};
 * space          = "\s"|"\t"|"\f"|"\t"|"\n";
 *
 * {"x"}= 0 bis n mal "x"
 * ["x"]= 0 bis 1 mal "x"
 * ("x" | "y")"z" = "xz" oder "yz"
 *
 *
 *
 */
class CFMLTransformer @JvmOverloads constructor(private val codeIsland: Boolean = false) {
    private var done = false

    /**
     * Startmethode zum transfomieren einer CFML Datei. <br></br>
     * EBNF:<br></br>
     * `{body}`
     *
     * @param config
     * @param ps CFML File
     * @param tlibs Tag Library Deskriptoren, nach denen innerhalb der CFML Datei geprueft werden soll.
     * @param flibs Function Library Deskriptoren, nach denen innerhalb der Expressions der CFML Datei
     * geprueft werden soll.
     * @param returnValue if true the method returns the value of the last expression executed inside
     * when you call the method "call"
     * @return uebersetztes CFXD Dokument Element.
     * @throws TemplateException
     * @throws IOException
     */
    @Throws(TemplateException::class, IOException::class)
    fun transform(factory: Factory?, config: ConfigPro?, ps: PageSource?, tlibs: Array<TagLib?>?, flibs: Array<FunctionLib?>?, returnValue: Boolean, ignoreScopes: Boolean): Page? {
        var p: Page?
        var sc: SourceCode?
        var writeLog: Boolean = config.getExecutionLogEnabled()
        var charset: Charset = config.getTemplateCharset()
        var dotUpper = ps.getDialect() === CFMLEngine.DIALECT_CFML && (ps.getMapping() as MappingImpl).getDotNotationUpperCase()

        // parse regular
        while (true) {
            try {
                sc = PageSourceCode(ps, charset, writeLog)

                // script files (cfs)
                if (Constants.isCFMLScriptExtension(ListUtil.last(ps.getRealpath(), '.'))) {
                    val isCFML = ps.getDialect() === CFMLEngine.DIALECT_CFML
                    val scriptTag: TagLibTag? = getTLT(sc, if (isCFML) Constants.CFML_SCRIPT_TAG_NAME else Constants.LUCEE_SCRIPT_TAG_NAME, config.getIdentification())
                    sc.setPos(0)
                    val original: SourceCode? = sc

                    // try inside a cfscript
                    val text = """
                        <${scriptTag.getFullName().toString()}>${original.getText().toString()}
                        </${scriptTag.getFullName().toString()}>
                        """.trimIndent()
                    sc = PageSourceCode(ps, text, charset, writeLog)
                }
                p = transform(factory, config, sc, tlibs, flibs, ps.getResource().lastModified(), dotUpper, returnValue, ignoreScopes)
                break
            } catch (pde: ProcessingDirectiveException) {
                if (pde.getWriteLog() != null) writeLog = pde.getWriteLog().booleanValue()
                if (pde.getDotNotationUpperCase() != null) dotUpper = pde.getDotNotationUpperCase().booleanValue()
                if (!StringUtil.isEmpty(pde.getCharset())) charset = pde.getCharset()
            }
        }

        // could it be a component?
        val isCFML = ps.getDialect() === CFMLEngine.DIALECT_CFML
        val isCFMLCompExt = isCFML && Constants.isCFMLComponentExtension(ResourceUtil.getExtension(ps.getResource(), ""))
        var possibleUndetectedComponent = false

        // we don't have a component or interface
        if (p.isPage()) {
            if (isCFML) possibleUndetectedComponent = isCFMLCompExt else if (Constants.isLuceeComponentExtension(ResourceUtil.getExtension(ps.getResource(), ""))) {
                var expr: Expression?
                var stat: Statement
                var po: PrintOut
                var ls: LitString?
                val statements: List<Statement?> = p.getStatements()

                // check the root statements for component
                val it: Iterator<Statement?> = statements.iterator()
                var str: String
                while (it.hasNext()) {
                    stat = it.next()
                    if (stat is PrintOut && (stat as PrintOut).getExpr().also { expr = it } is LitString) {
                        ls = expr as LitString?
                        str = ls.getString()
                        if (str.indexOf(Constants.LUCEE_COMPONENT_TAG_NAME) !== -1 || str.indexOf(Constants.LUCEE_INTERFACE_TAG_NAME) !== -1 || str.indexOf(Constants.CFML_COMPONENT_TAG_NAME) !== -1 // cfml name is supported as alias
                        ) {
                            possibleUndetectedComponent = true
                            break
                        }
                    }
                }
            }
        }
        if (possibleUndetectedComponent) {
            var _p: Page?
            val scriptTag: TagLibTag? = getTLT(sc, if (isCFML) Constants.CFML_SCRIPT_TAG_NAME else Constants.LUCEE_SCRIPT_TAG_NAME, config.getIdentification())
            sc.setPos(0)
            val original: SourceCode? = sc

            // try inside a cfscript
            var text = """
                <${scriptTag.getFullName().toString()}>${original.getText().toString()}
                </${scriptTag.getFullName().toString()}>
                """.trimIndent()
            sc = PageSourceCode(ps, text, charset, writeLog)
            try {
                while (true) {
                    if (sc == null) {
                        sc = PageSourceCode(ps, charset, writeLog)
                        text = """
                            <${scriptTag.getFullName().toString()}>${sc.getText().toString()}
                            </${scriptTag.getFullName().toString()}>
                            """.trimIndent()
                        sc = PageSourceCode(ps, text, charset, writeLog)
                    }
                    try {
                        _p = transform(factory, config, sc, tlibs, flibs, ps.getResource().lastModified(), dotUpper, returnValue, ignoreScopes)
                        break
                    } catch (pde: ProcessingDirectiveException) {
                        if (pde.getWriteLog() != null) writeLog = pde.getWriteLog().booleanValue()
                        if (pde.getDotNotationUpperCase() != null) dotUpper = pde.getDotNotationUpperCase().booleanValue()
                        if (!StringUtil.isEmpty(pde.getCharset())) charset = pde.getCharset()
                        sc = null
                    }
                }
            } catch (e: ComponentTemplateException) {
                throw e.getTemplateException()
            }
            // we only use that result if it is a component now
            if (_p != null && !_p.isPage()) return _p
        }
        if (isCFMLCompExt && !p.isComponent() && !p.isInterface()) {
            val msg = "template [" + ps.getDisplayPath().toString() + "] must contain a component or an interface."
            if (sc != null) throw TemplateException(sc, msg)
            throw TemplateException(msg)
        }
        return p
    }

    /**
     * Startmethode zum transfomieren einer CFMLString. <br></br>
     * EBNF:<br></br>
     * `{body}`
     *
     * @param config
     * @param sc CFMLString
     * @param tlibs Tag Library Deskriptoren, nach denen innerhalb der CFML Datei geprueft werden soll.
     * @param flibs Function Library Deskriptoren, nach denen innerhalb der Expressions der CFML Datei
     * geprueft werden soll.
     * @param sourceLastModified
     * @param dotNotationUpperCase
     * @param returnValue if true the method returns the value of the last expression executed inside
     * when you call the method "call"
     * @return uebersetztes CFXD Dokument Element.
     * @throws TemplateException
     */
    @Throws(TemplateException::class)
    fun transform(factory: Factory?, config: ConfigPro?, sc: SourceCode?, tlibs: Array<TagLib?>?, flibs: Array<FunctionLib?>?, sourceLastModified: Long, dotNotationUpperCase: Boolean?,
                  returnValue: Boolean, ignoreScope: Boolean): Page? {
        val dnuc: Boolean
        dnuc = dotNotationUpperCase
                ?: if (sc is PageSourceCode) sc.getDialect() === CFMLEngine.DIALECT_CFML && ((sc as PageSourceCode?).getPageSource().getMapping() as MappingImpl).getDotNotationUpperCase() else sc.getDialect() === CFMLEngine.DIALECT_CFML && config.getDotNotationUpperCase()
        val _tlibs: Array<Array<TagLib?>?> = arrayOf(null, arrayOfNulls<TagLib?>(0))
        _tlibs[TAG_LIB_GLOBAL.toInt()] = tlibs
        // reset page tlds
        if (_tlibs[TAG_LIB_PAGE.toInt()].length > 0) {
            _tlibs[TAG_LIB_PAGE.toInt()] = arrayOfNulls<TagLib?>(0)
        }
        val page = Page(factory, config, sc, null, ConfigWebUtil.getEngine(config).getInfo().getFullVersionInfo(), sourceLastModified, sc.getWriteLog(),
                sc.getDialect() === CFMLEngine.DIALECT_LUCEE || config.getSuppressWSBeforeArg(), config.getDefaultFunctionOutput(), returnValue, ignoreScope)
        val settings = TransfomerSettings(dnuc, sc.getDialect() === CFMLEngine.DIALECT_CFML && factory.getConfig().getHandleUnQuotedAttrValueAsString(),
                ignoreScope)
        val data = Data(factory, page, sc, EvaluatorPool(), settings, _tlibs, flibs, config.getCoreTagLib(sc.getDialect()).getScriptTags(), false)
        transform(data, page)
        return page
    }

    @Throws(TemplateException::class)
    fun transform(data: Data?, parent: Body?) {
        try {
            do {
                body(data, parent)
                if (done || data.srcCode.isAfterLast()) break
                if (data.srcCode.forwardIfCurrent("</")) {
                    val pos: Int = data.srcCode.getPos()
                    val tagLib: TagLib? = nameSpace(data)
                    if (tagLib == null) {
                        parent.addPrintOut(data.factory, "</", null, null)
                    } else {
                        val name = identifier(data.srcCode, true, true)
                        if (tagLib.getIgnoreUnknowTags()) {
                            val tlt: TagLibTag = tagLib.getTag(name)
                            if (tlt == null) {
                                data.srcCode.setPos(pos)
                                parent.addPrintOut(data.factory, "</", null, null)
                            }
                        } else throw TemplateException(data.srcCode, "no matching start tag for end tag [" + tagLib.getNameSpaceAndSeparator() + name.toString() + "]")
                    }
                } else throw TemplateException(data.srcCode, "Error while transforming CFML File")
            } while (true)

            // call-back of evaluators
            val pos: Int = data.srcCode.getPos()
            data.ep.run()
            data.srcCode.setPos(pos)
            return
        } catch (e: TemplateException) {
            data.ep.clear()
            throw e
        }
    }

    /**
     * Liest den Body eines Tag ein. Kommentare, Tags und Literale inkl. Expressions. <br></br>
     * EBNF:<br></br>
     * `[comment] ("`" | "<" tag body | literal body);
     *
     * @param body CFXD Body Element dem der Inhalt zugeteilt werden soll.
     * @param parseExpression Definiert ob Expressions innerhalb von Literalen uebersetzt werden sollen
     * oder nicht.
     * @param transformer Expression Transfomer zum uebersetzten von Expression.
     * @throws TemplateException
     */
    @Throws(TemplateException::class)
    fun body(data: Data?, body: Body?) {
        var parseLiteral = true

        // Comment
        comment(data.srcCode, false)
        // Tag
        // is Tag Beginning
        if (data.srcCode.isCurrent('<')) {
            // return if end tag and inside tag
            if (data.srcCode.isNext('/')) {
                // lucee.print.ln("early return");
                return
            }
            parseLiteral = !tag(data, body)
        }
        // no Tag
        if (parseLiteral) {
            literal(data, body)
        }
        // not at the end
        if (!done && data.srcCode.isValidIndex()) body(data, body)
    }

    /**
     * Liest Literale Zeichenketten ein die sich innerhalb und auserhalb von tgas befinden, beim
     * Einlesen wird unterschieden ob Expression geparsst werden muessen oder nicht, dies ist abhaengig,
     * von der Definition des Tag in dem man sich allenfalls befindet, innerhalb der TLD.
     *
     * @param parent uebergeordnetes Element.
     * @param parseExpression Definiert on Expressions geparset werden sollen oder nicht.
     * @param transformer Expression Transfomer zum uebersetzen der Expressions innerhalb des Literals.
     * @throws TemplateException
     *
     * <br></br>
     * EBNF:<br></br>
     * `("<" | {?-"#"-"<"} "<" | {"#" expression "#"} "<" ) | ({?-"<"} "<")
     * (* Welcher Teil der "oder" Bedingung ausgefuehrt wird, ist abhaengig ob die Tag-Lib vorgibt,
     * dass Expression geparst werden sollen oder nicht. *)`
     */
    @Throws(TemplateException::class)
    private fun literal(data: Data?, parent: Body?) {
        while (codeIsland && data.srcCode.isCurrent(AbstrCFMLScriptTransformer.TAG_ISLAND_INDICATOR)) {
            val start: Int = data.srcCode.getPos()
            if (data.srcCode.forwardIfCurrent(AbstrCFMLScriptTransformer.TAG_ISLAND_INDICATOR + AbstrCFMLScriptTransformer.TAG_ISLAND_INDICATOR)) {
                parent.addPrintOut(data.factory, AbstrCFMLScriptTransformer.TAG_ISLAND_INDICATOR, data.srcCode.getPosition(start), data.srcCode.getPosition())
            } else {
                done = true
                return
            }
        }
        var _end = false
        // with expression
        if (data.parseExpression) {
            if (data.srcCode.isAfterLast()) return
            // data.cfml.getCurrent()
            var text: StringBuilder? = StringBuilder()
            var count = 0
            while (data.srcCode.isValidIndex()) {
                count++
                // #
                if (data.srcCode.isCurrent('#')) {
                    data.srcCode.next()
                    if (data.srcCode.isCurrent('#')) {
                        text.append('#')
                    } else {
                        if (text.length() > 0) {
                            val end: Position = data.srcCode.getPosition()
                            var start: Position = data.srcCode.getPosition(end.pos - text.length())
                            parent.addPrintOut(data.factory, text.toString(), start, end)
                            start = end
                            text = StringBuilder()
                        }
                        val end: Position = data.srcCode.getPosition()
                        val start: Position = data.srcCode.getPosition(end.pos - text.length())
                        var po: PrintOut?
                        parent.addStatement(PrintOut(data.transformer.transform(data), start, end).also { po = it })
                        po.setEnd(data.srcCode.getPosition())
                        if (!data.srcCode.isCurrent('#')) throw TemplateException(data.srcCode, "missing terminating [#] for expression")
                    }
                } else if (data.srcCode.isCurrent('<') && count > 1) {
                    break
                } else if (codeIsland && data.srcCode.isCurrent(AbstrCFMLScriptTransformer.TAG_ISLAND_INDICATOR) && count > 1) {
                    // int start = data.srcCode.getPos();
                    if (data.srcCode.forwardIfCurrent(AbstrCFMLScriptTransformer.TAG_ISLAND_INDICATOR + AbstrCFMLScriptTransformer.TAG_ISLAND_INDICATOR)) {
                        text.append(AbstrCFMLScriptTransformer.TAG_ISLAND_INDICATOR)
                        // parent.addPrintOut(data.factory,AbstrCFMLScriptTransformer.TAG_ISLAND_INDICATOR,
                        // data.srcCode.getPosition(start),data.srcCode.getPosition());
                        data.srcCode.previous()
                    } else {
                        _end = true
                        break
                    }
                } else text.append(data.srcCode.getCurrent())
                data.srcCode.next()
            }
            if (text.length() > 0) {
                val end: Position = data.srcCode.getPosition()
                val start: Position = data.srcCode.getPosition(end.pos - text.length())
                parent.addPrintOut(data.factory, text.toString(), start, end)
                if (_end) {
                    done = true
                    return
                    // throw new CodeIslandEnd();
                }
            }
        } else {
            var start: Int = data.srcCode.getPos()
            data.srcCode.next()
            var end: Int = data.srcCode.indexOfNext('<')
            var endIsland = if (codeIsland) data.srcCode.indexOfNext(AbstrCFMLScriptTransformer.TAG_ISLAND_INDICATOR) else -1
            var endIslandEsc = if (codeIsland) data.srcCode.indexOfNext(AbstrCFMLScriptTransformer.TAG_ISLAND_INDICATOR + AbstrCFMLScriptTransformer.TAG_ISLAND_INDICATOR) else -1

            // next is escaped
            while (endIsland != -1 && endIslandEsc != -1 && endIsland == endIslandEsc && (end == -1 || endIsland < end)) {
                val txt: String = data.srcCode.substring(start, endIsland - start) + AbstrCFMLScriptTransformer.TAG_ISLAND_INDICATOR
                val s: Int = data.srcCode.getPos()
                data.srcCode.setPos(endIsland + AbstrCFMLScriptTransformer.TAG_ISLAND_INDICATOR.length() * 2)
                parent.addPrintOut(data.factory, txt, data.srcCode.getPosition(s), data.srcCode.getPosition())

                // now we need to check again
                start = data.srcCode.getPos()
                // data.srcCode.next();
                end = data.srcCode.indexOfNext('<')
                endIsland = data.srcCode.indexOfNext(AbstrCFMLScriptTransformer.TAG_ISLAND_INDICATOR)
                endIslandEsc = data.srcCode.indexOfNext(AbstrCFMLScriptTransformer.TAG_ISLAND_INDICATOR + AbstrCFMLScriptTransformer.TAG_ISLAND_INDICATOR)
            }
            val text: String
            if (endIsland != -1 && (end == -1 || endIsland < end)) {
                text = data.srcCode.substring(start, endIsland - start)
                data.srcCode.setPos(endIsland)
                _end = true
            } else if (end != -1) {
                text = data.srcCode.substring(start, end - start)
                data.srcCode.setPos(end)
            } else {
                text = data.srcCode.substring(start)
                data.srcCode.setPos(data.srcCode.length())
            }
            val e: Position = data.srcCode.getPosition()
            val s: Position = data.srcCode.getPosition(start)
            parent.addPrintOut(data.factory, text, s, e)
            if (_end) {
                done = true
                return
                // throw new CodeIslandEnd();
            }
        }
    }

    private fun p(data: Data?): Object? {
        return try {
            data.srcCode.getPosition()
        } catch (e: Exception) {
            data.srcCode.getPos()
        }
    }

    /**
     * Liest einen Tag ein, prueft hierbei ob das Tag innerhalb einer der geladenen Tag-Lib existiert,
     * ansonsten wird ein Tag einfach als literal-string aufgenommen. <br></br>
     * EBNF:<br></br>
     * `name-space identifier spaces attributes ("/>" | ">" [body "`" identifier spaces ">"]);(* Ob dem Tag ein Body und ein End-Tag folgt ist abhaengig von Definition des body-content in Tag-Lib, gleices gilt fuer appendix *)
     *
     * @param parent uebergeornetes Tag
     * @param parseExpression sollen Expresson innerhalb des Body geparste werden oder nicht.
     * @return Gibt zurueck ob es sich um ein Tag as einer Tag-Lib handelte oder nicht.
     * @throws TemplateException
     */
    @Throws(TemplateException::class)
    private fun tag(data: Data?, parent: Body?): Boolean {
        val startingParseExpression: Boolean = data.parseExpression
        val startingTransformer: ExprTransformer = data.transformer
        return try {
            var hasBody = false
            val line: Position = data.srcCode.getPosition()
            // int column=data.cfml.getColumn();
            val start: Int = data.srcCode.getPos()
            data.srcCode.next()

            // read in namespace of tag
            val tagLib: TagLib? = nameSpace(data)

            // return if no matching tag lib
            if (tagLib == null) {
                data.srcCode.previous()
                return false
            }

            // Get matching tag from tag lib
            val strNameNormal = identifier(data.srcCode, false, true)
            if (strNameNormal == null) {
                data.srcCode.setPos(data.srcCode.getPos() - tagLib.getNameSpaceAndSeparator().length() - 1)
                return false
            }
            val strName: String = strNameNormal.toLowerCase()
            var appendix: String? = null
            var tagLibTag: TagLibTag = tagLib.getTag(strName)

            // get taglib
            if (tagLibTag == null) {
                tagLibTag = tagLib.getAppendixTag(strName)
                if (tagLibTag == null) {
                    if (tagLib.getIgnoreUnknowTags()) {
                        data.srcCode.setPos(start)
                        return false
                    }
                    throw TemplateException(data.srcCode, "undefined tag [" + tagLib.getNameSpaceAndSeparator() + strName + "]")
                }
                appendix = StringUtil.removeStartingIgnoreCase(strNameNormal, tagLibTag.getName())
            }
            if (tagLibTag.getStatus() === TagLib.STATUS_UNIMPLEMENTED) {
                throw TemplateException(data.srcCode, "the tag [" + tagLibTag.getFullName().toString() + "] is not implemented yet.")
            }

            // CFXD Element
            val tag: Tag
            tag = try {
                tagLibTag.getTag(data.factory, line, data.srcCode.getPosition())
            } catch (e: Exception) {
                throw TemplateException(data.srcCode, e)
            }
            parent.addStatement(tag)

            // get tag from tag library
            if (appendix != null) {
                tag.setAppendix(appendix)
                tag.setFullname(tagLibTag.getFullName().concat(appendix))
            } else {
                tag.setFullname(tagLibTag.getFullName())
            }
            // if(tag.getFullname().equalsIgnoreCase("cfcomponent"))data.page.setIsComponent(true); // MUST to
            // hardcoded, to better
            // else if(tag.getFullname().equalsIgnoreCase("cfinterface"))data.page.setIsInterface(true); // MUST
            // to hardcoded, to better
            tag.setTagLibTag(tagLibTag)
            comment(data.srcCode, true)

            // Tag Translator Evaluator
            if (tagLibTag.hasTTE()) {
                data.ep.add(tagLibTag, tag, data.flibs, data.srcCode)
            }

            // get Attributes
            attributes(data, tagLibTag, tag)
            if (tagLibTag.hasAttributeEvaluator()) {
                tagLibTag = try {
                    tagLibTag.getAttributeEvaluator().evaluate(tagLibTag, tag)
                } catch (e: AttributeEvaluatorException) {
                    throw TemplateException(data.srcCode, e)
                }
            }

            // End of begin Tag
            // TODO muss erlaubt sein
            if (data.srcCode.forwardIfCurrent('>')) {
                hasBody = tagLibTag.getHasBody()
            } else if (data.srcCode.forwardIfCurrent('/', '>')) {
                if (tagLibTag.getHasBody()) tag.setBody(BodyBase(data.factory))
            } else {
                throw createTemplateException(data.srcCode, "tag [" + tagLibTag.getFullName().toString() + "] is not closed", tagLibTag)
            }

            // Body
            if (hasBody) {

                // get Body
                if (tagLibTag.isTagDependent()) {
                    // get TagDependentBodyTransformer
                    var tdbt: TagDependentBodyTransformer? = null
                    tdbt = try {
                        tagLibTag.getBodyTransformer()
                    } catch (e: TagLibException) {
                        throw TemplateException(data.srcCode, e)
                    }
                    if (tdbt == null) throw createTemplateException(data.srcCode, "Tag dependent body Transformer is invalid for Tag [" + tagLibTag.getFullName().toString() + "]", tagLibTag)

                    // tag.setBody(tdbt.transform(data.factory,data.root,data.ep,data.tlibs,data.flibs,
                    // tagLibTag.getFullName(),data.scriptTags,data.srcCode,data.settings));
                    tag.setBody(tdbt.transform(data, tagLibTag.getFullName()))

                    // get TagLib of end Tag
                    if (!data.srcCode.forwardIfCurrent("</")) {
                        // MUST this is a patch, do a more proper implementation
                        val te = TemplateException(data.srcCode, "invalid construct")
                        if (tdbt is CFMLScriptTransformer && ASMUtil.containsComponent(tag.getBody())) {
                            throw ComponentTemplateException(te)
                        }
                        throw te
                    }
                    val tagLibEnd: TagLib? = nameSpace(data)
                    // same NameSpace
                    if (!(tagLibEnd != null && tagLibEnd.getNameSpaceAndSeparator().equals(tagLib.getNameSpaceAndSeparator()))) throw TemplateException(data.srcCode, "invalid construct")
                    // get end Tag
                    val strNameEnd: String = identifier(data.srcCode, true, true).toLowerCase()

                    // not the same name Tag
                    if (!strName.equals(strNameEnd)) {
                        data.srcCode.setPos(start)
                        throw TemplateException(data.srcCode, "Start and End Tag has not the same Name [" + tagLib.getNameSpaceAndSeparator() + strName + "-"
                                + tagLibEnd.getNameSpaceAndSeparator() + strNameEnd + "]")
                    }
                    data.srcCode.removeSpace()
                    if (!data.srcCode.forwardIfCurrent('>')) throw TemplateException(data.srcCode, "End Tag [" + tagLibEnd.getNameSpaceAndSeparator() + strNameEnd + "] not closed")
                } else {
                    // get body of Tag
                    val body = BodyBase(data.factory)
                    body.setParent(tag)
                    // tag.setBody(body);
                    // parseExpression=(tagLibTag.getParseBody())?true:parseExpression;
                    if (tagLibTag.getParseBody()) data.parseExpression = true
                    while (true) {

                        // Load Expession Transformer from TagLib
                        data.transformer = startingTransformer
                        if (data.parseExpression) {
                            try {
                                data.transformer = tagLibTag.getTagLib().getExprTransfomer()
                            } catch (e: TagLibException) {
                                throw TemplateException(data.srcCode, e)
                            }
                        }

                        // call body
                        body(data, body)

                        // no End Tag
                        if (done || data.srcCode.isAfterLast()) {
                            if (tagLibTag.isBodyReq()) {
                                data.srcCode.setPos(start)
                                throw createTemplateException(data.srcCode, "No matching end tag found for tag [" + tagLibTag.getFullName().toString() + "]", tagLibTag)
                            }
                            body.moveStatmentsTo(parent)
                            return executeEvaluator(data, tagLibTag, tag)
                        }

                        // Invalid Construct
                        val posBeforeEndTag: Int = data.srcCode.getPos()
                        if (!data.srcCode.forwardIfCurrent('<', '/')) throw createTemplateException(data.srcCode, "Missing end tag for [" + tagLibTag.getFullName().toString() + "]", tagLibTag)

                        // get TagLib of end Tag
                        val _start: Int = data.srcCode.getPos()
                        val tagLibEnd: TagLib? = nameSpace(data)

                        // same NameSpace
                        if (tagLibEnd != null) {
                            var strNameEnd: String? = ""
                            // lucee.print.ln(data.cfml.getLine()+" - "+data.cfml.getColumn()+" -
                            // "+tagLibEnd.getNameSpaceAndSeperator()+".equals("+tagLib.getNameSpaceAndSeperator()+")");
                            if (tagLibEnd.getNameSpaceAndSeparator().equals(tagLib.getNameSpaceAndSeparator())) {

                                // get end Tag
                                strNameEnd = identifier(data.srcCode, true, true).toLowerCase()
                                // not the same name Tag

                                // new part
                                data.srcCode.removeSpace()
                                if (strName.equals(strNameEnd)) {
                                    if (!data.srcCode.forwardIfCurrent('>')) throw TemplateException(data.srcCode, "End Tag [" + tagLibEnd.getNameSpaceAndSeparator() + strNameEnd.toString() + "] not closed")
                                    break
                                }
                            }
                            // new part
                            if (tagLibTag.isBodyReq()) {
                                val endTag: TagLibTag = tagLibEnd.getTag(strNameEnd)
                                if (endTag != null && !endTag.getHasBody()) throw TemplateException(data.srcCode, "End Tag [" + tagLibEnd.getNameSpaceAndSeparator() + strNameEnd.toString() + "] is not allowed, for this tag only a Start Tag is allowed")
                                data.srcCode.setPos(start)
                                if (tagLibEnd.getIgnoreUnknowTags() && tagLibEnd.getTag(strNameEnd) == null) {
                                    data.srcCode.setPos(_start)
                                } else throw TemplateException(data.srcCode, "Start and End Tag has not the same Name [" + tagLib.getNameSpaceAndSeparator() + strName + "-"
                                        + tagLibEnd.getNameSpaceAndSeparator() + strNameEnd.toString() + "]")
                            } else {
                                body.moveStatmentsTo(parent)
                                data.srcCode.setPos(posBeforeEndTag)
                                return executeEvaluator(data, tagLibTag, tag)
                            }
                            /// new part
                        }
                        body.addPrintOut(data.factory, "</", null, null)
                    }
                    tag.setBody(body)
                }
            }
            if (tag is StatementBase) (tag as StatementBase).setEnd(data.srcCode.getPosition())
            if (tag is TagFunction) (tag as TagFunction).register(data.factory, data.page) // MUST6 more general solution
            // Tag Translator Evaluator
            executeEvaluator(data, tagLibTag, tag)
        } finally {
            data.parseExpression = startingParseExpression
            data.transformer = startingTransformer
        }
    }

    @Throws(TemplateException::class)
    private fun executeEvaluator(data: Data?, tagLibTag: TagLibTag?, tag: Tag?): Boolean {
        if (tagLibTag.hasTTE()) {
            try {
                val lib: TagLib = tagLibTag.getEvaluator().execute(data.config, tag, tagLibTag, data.flibs, data)
                if (lib != null) {
                    // set
                    for (i in 0 until data.tlibs.get(TAG_LIB_PAGE.toInt()).length) {
                        if (data.tlibs.get(TAG_LIB_PAGE.toInt()).get(i).getNameSpaceAndSeparator().equalsIgnoreCase(lib.getNameSpaceAndSeparator())) {
                            val extIsCustom = data.tlibs.get(TAG_LIB_PAGE.toInt()).get(i) is CustomTagLib
                            val newIsCustom = lib is CustomTagLib
                            // TagLib + CustomTagLib (visa/versa)
                            if (extIsCustom) {
                                (data.tlibs.get(TAG_LIB_PAGE.toInt()).get(i) as CustomTagLib).append(lib)
                                return true
                            } else if (newIsCustom) {
                                (lib as CustomTagLib).append(data.tlibs.get(TAG_LIB_PAGE.toInt()).get(i))
                                data.tlibs.get(TAG_LIB_PAGE.toInt()).get(i) = lib
                                return true
                            }
                        }
                    }
                    // TODO make sure longer namespace ar checked firts to support subsets, same for core libs
                    // insert
                    val newTlibs: Array<TagLib?> = arrayOfNulls<TagLib?>(data.tlibs.get(TAG_LIB_PAGE.toInt()).length + 1)
                    for (i in 0 until data.tlibs.get(TAG_LIB_PAGE.toInt()).length) {
                        newTlibs[i] = data.tlibs.get(TAG_LIB_PAGE.toInt()).get(i)
                    }
                    newTlibs[data.tlibs.get(TAG_LIB_PAGE.toInt()).length] = lib
                    data.tlibs.get(TAG_LIB_PAGE.toInt()) = newTlibs
                }
            } catch (e: EvaluatorException) {
                throw TemplateException(data.srcCode, e)
            }
        }
        return true
    }

    companion object {
        var TAG_LIB_GLOBAL: Short = 0
        var TAG_LIB_PAGE: Short = 1
        @Throws(TemplateException::class)
        fun getTLT(cfml: SourceCode?, name: String?, id: Identification?): TagLibTag? {
            val tl: TagLib
            return try {
                // this is already loaded, oherwise we where not here
                tl = TagLibFactory.loadFromSystem(cfml.getDialect(), id)
                tl.getTag(name)
            } catch (e: TagLibException) {
                throw TemplateException(cfml, e)
            }
        }

        /**
         * Liest einen Kommentar ein, Kommentare werden nicht in die CFXD uebertragen sondern verworfen.
         * Komentare koennen auch Kommentare enthalten. <br></br>
         * EBNF:<br></br>
         * `""} "--->";`
         *
         * @throws TemplateException
         */
        @Throws(TemplateException::class)
        private fun comment(cfml: SourceCode?, removeSpace: Boolean) {
            if (!removeSpace) {
                comment(cfml)
            } else {
                cfml.removeSpace()
                if (comment(cfml)) cfml.removeSpace()
            }
        }

        @Throws(TemplateException::class)
        fun comment(cfml: SourceCode?): Boolean {
            if (!cfml.forwardIfCurrent("<!---")) return false
            val start: Int = cfml.getPos()
            var counter: Short = 1
            while (true) {
                if (cfml.isAfterLast()) {
                    cfml.setPos(start)
                    throw TemplateException(cfml, "no end comment found")
                } else if (cfml.forwardIfCurrent("<!---")) {
                    counter++
                } else if (cfml.forwardIfCurrent("--->")) {
                    if (--counter.toInt() == 0) {
                        comment(cfml)
                        return true
                    }
                } else {
                    cfml.next()
                }
            }
        }

        /**
         * Vergleicht folgende Zeichen mit den Namespacedefinitionen der Tag Libraries, gibt eine Tag-Lib
         * zurueck falls eine passt, ansonsten null. <br></br>
         * EBNF:<br></br>
         * `< tagLib[].getNameSpaceAndSeperator() >(* Vergleicht Zeichen mit den Namespacedefinitionen der Tag Libraries. *) `
         *
         * @return TagLib Passende Tag Lirary oder null.
         */
        fun nameSpace(data: Data?): TagLib? {
            var hasTag = false
            val start: Int = data.srcCode.getPos()
            var tagLib: TagLib? = null

            // loop over NameSpaces
            for (i in 1 downTo 0) {
                for (ii in 0 until data.tlibs.get(i).length) {
                    tagLib = data.tlibs.get(i).get(ii)
                    val c: CharArray = tagLib.getNameSpaceAndSeperatorAsCharArray()
                    // Loop over char of NameSpace and Sepearator
                    hasTag = true
                    for (y in c.indices) {
                        if (!(data.srcCode.isValidIndex() && c[y] == data.srcCode.getCurrentLower())) {
                            // hasTag=true;
                            // } else {
                            hasTag = false
                            data.srcCode.setPos(start)
                            break
                        }
                        data.srcCode.next()
                    }
                    if (hasTag) return tagLib // break;
                }
                // if(hasTag) return tagLib;
            }
            return null
        }

        /**
         * Liest die Attribute eines Tags ein, dies Abhaengig von der Definition innerhalb der Tag-Lib.
         * Hierbei unterscheiden wir vier verschiedene Arten von Attributen:<br></br>
         *
         *  * FIX: Definierte Attribute Fix, fuer jedes Attribut ist definiert ob es required ist oder
         * nicht (gleich wie JSP).
         *  * DYNAMIC: Die Attribute des Tag sind frei, keine Namen sind vorgegeben. Es kann aber definiert
         * sein wieviele Attribute maximal und minimal verwendetet werden duerfen.
         *  * FULLDYNAMIC: Gleich wie DYNAMIC, jedoch kann der Name des Attribut auch ein dynamischer Wert
         * sein (wie bei cfset).
         *  * NONAME: Ein Tag welches nur ein Attribut besitzt ohne Name, sondern einfach nur mit einem
         * Attribut Wert
         *
         * <br></br>
         * EBNF:<br></br>
         * `({spaces attribute} "/>" | {spaces attribute} ">") | attribute-value;(* Welcher Teil der "oder" Bedingung ausgefuehrt wird, ist abhaengig von der Tag Attribute Definition in der Tag Lib. *)`
         *
         * @param tag
         * @param parent
         * @throws TemplateException
         */
        @Throws(TemplateException::class)
        fun attributes(data: Data?, tag: TagLibTag?, parent: Tag?) {
            val type: Int = tag.getAttributeType()
            val start: Int = data.srcCode.getPos()
            // Tag with attribute names
            if (type != TagLibTag.ATTRIBUTE_TYPE_NONAME) {
                try {
                    val min: Int = tag.getMin()
                    val max: Int = tag.getMax()
                    var count = 0
                    val args: ArrayList<String?> = ArrayList<String?>()
                    val allowDefaultValue: RefBoolean = RefBooleanImpl(tag.getDefaultAttribute() != null)
                    while (data.srcCode.isValidIndex()) {
                        data.srcCode.removeSpace()
                        // if no more attributes break
                        if (data.srcCode.isCurrent('/') || data.srcCode.isCurrent('>')) break
                        parent.addAttribute(attribute(data, tag, args, allowDefaultValue))
                        count++
                    }

                    // set default values
                    if (tag.hasDefaultValue()) {
                        val hash: Map<String?, TagLibTagAttr?> = tag.getAttributes()
                        val it: Iterator<Entry<String?, TagLibTagAttr?>?> = hash.entrySet().iterator()
                        var e: Entry<String?, TagLibTagAttr?>?
                        var att: TagLibTagAttr
                        while (it.hasNext()) {
                            e = it.next()
                            att = e.getValue()
                            if (!parent.containsAttribute(att.getName()) && att.hasDefaultValue()) {
                                val attr = Attribute(tag.getAttributeType() === TagLibTag.ATTRIBUTE_TYPE_DYNAMIC, att.getName(),
                                        data.factory.toExpression(data.factory.createLitString(Caster.toString(att.getDefaultValue(), null)), att.getType()), att.getType())
                                attr.setDefaultAttribute(true)
                                parent.addAttribute(attr)
                            }
                        }
                    }
                    val hasAttributeCollection: Boolean = args.contains("attributecollection")

                    // to less attributes
                    if (!hasAttributeCollection && min > count) throw createTemplateException(data.srcCode, "the tag [" + tag.getFullName().toString() + "] must have at least [" + min.toString() + "] attributes", tag)

                    // too much attributes
                    if (!hasAttributeCollection && max > 0 && max < count) throw createTemplateException(data.srcCode, "the tag [" + tag.getFullName().toString() + "] can have a maximum of [" + max.toString() + "] attributes", tag)

                    // not defined attributes
                    if (type == TagLibTag.ATTRIBUTE_TYPE_FIXED || type == TagLibTag.ATTRIBUTE_TYPE_MIXED) {
                        // Map<String, TagLibTagAttr> hash = tag.getAttributes();
                        val it: Iterator<TagLibTagAttr?> = tag.getAttributes().values().iterator()
                        while (it.hasNext()) {
                            val att: TagLibTagAttr? = it.next()
                            if (att.isRequired() && !contains(args, att) && att.getDefaultValue() == null) {
                                if (!hasAttributeCollection) throw createTemplateException(data.srcCode, "attribute [" + att.getName().toString() + "] is required for tag [" + tag.getFullName().toString() + "]", tag)
                                parent.addMissingAttribute(att)
                            }
                        }
                    }
                } catch (te: TemplateException) {
                    data.srcCode.setPos(start)
                    // if the tag supports a non name attribute try this
                    val sa: TagLibTagAttr = tag.getSingleAttr()
                    if (sa != null) attrNoName(parent, tag, data, sa) else throw te
                }
            } else {
                attrNoName(parent, tag, data, null)
            }
        }

        private fun contains(names: ArrayList<String?>?, attr: TagLibTagAttr?): Boolean {
            val it: Iterator<String?> = names.iterator()
            var name: String?
            var alias: Array<String?>
            while (it.hasNext()) {
                name = it.next()

                // check name
                if (name!!.equals(attr.getName())) return true

                // and aliases
                alias = attr.getAlias()
                if (!ArrayUtil.isEmpty(alias)) for (i in alias.indices) {
                    if (alias[i]!!.equals(attr.getName())) return true
                }
            }
            return false
        }

        @Throws(TemplateException::class)
        private fun attrNoName(parent: Tag?, tag: TagLibTag?, data: Data?, attr: TagLibTagAttr?) {
            var attr: TagLibTagAttr? = attr
            if (attr == null) attr = tag.getFirstAttribute()
            var strName = "noname"
            var strType = "any"
            var pe = true
            if (attr != null) {
                strName = attr.getName()
                strType = attr.getType()
                pe = attr.getRtexpr()
            }
            // LitString.toExprString("",-1);
            val att = Attribute(false, strName, attributeValue(data, tag, strType, pe, true, data.factory.createNull()), strType)
            parent.addAttribute(att)
        }

        /**
         * Liest ein einzelnes Atribut eines tag ein (nicht NONAME). <br></br>
         * EBNF:<br></br>
         * `attribute-name  spaces "=" spaces attribute-value;`
         *
         * @param tag Definition des Tag das dieses Attribut enthaelt.
         * @param args Container zum Speichern einzelner Attribute Namen zum nachtraeglichen Prufen gegen
         * die Tag-Lib.
         * @return Element Attribute Element.
         * @throws TemplateException
         */
        @Throws(TemplateException::class)
        private fun attribute(data: Data?, tag: TagLibTag?, args: ArrayList<String?>?, allowDefaultValue: RefBoolean?): Attribute? {
            var value: Expression? = null

            // Name
            val sbType = StringBuffer()
            val dynamic: RefBoolean = RefBooleanImpl(false)
            var isDefaultValue = false
            val parseExpression = BooleanArray(2)
            parseExpression[0] = true
            parseExpression[1] = false
            var name = attributeName(data.srcCode, dynamic, args, tag, sbType, parseExpression, allowDefaultValue.toBooleanValue())

            // mixed in a noname attribute
            if (StringUtil.isEmpty(name)) {
                allowDefaultValue.setValue(false)
                val attr: TagLibTagAttr = tag.getDefaultAttribute()
                        ?: throw TemplateException(data.srcCode, "Invalid Identifier.")
                name = attr.getName()
                sbType.append(attr.getType())
                isDefaultValue = true
            }
            comment(data.srcCode, true)
            if (isDefaultValue || data.srcCode.forwardIfCurrent('=')) {
                comment(data.srcCode, true)
                // Value
                value = attributeValue(data, tag, sbType.toString(), parseExpression[0], false, data.factory.createLitString(""))
            } else {
                val attr: TagLibTagAttr = tag.getAttribute(name)
                value = if (attr != null) attr.getUndefinedValue(data.factory) else tag.getAttributeUndefinedValue(data.factory)
                if (sbType.toString().length() > 0) {
                    value = data.factory.toExpression(value, sbType.toString())
                }
            }
            comment(data.srcCode, true)
            return Attribute(dynamic.toBooleanValue(), name, value, sbType.toString())
        }

        /**
         * Liest den Namen eines Attribut ein, je nach Attribut-Definition innerhalb der Tag-Lib, wird der
         * Name ueber den identifier oder den Expression Transformer eingelesen.
         *
         *  * FIX und DYNAMIC --> identifier
         *  * FULLDYNAMIC --> Expression Transformer
         *
         * <br></br>
         * EBNF:<br></br>
         * `("expression"|'expression'|expression) | identifier;(* Ruft identifier oder den Expression Transformer auf je nach Attribute Definition in der Tag Lib. *)`
         *
         * @param dynamic
         * @param args Container zum Speichern einzelner Attribute Namen zum nachtraeglichen Prufen gegen
         * die Tag-Lib.
         * @param tag Aktuelles tag aus der Tag-Lib
         * @param sbType Die Methode speichert innerhalb von sbType den Typ des Tags, zur Interpretation in
         * der attribute Methode.
         * @param parseExpression Soll der Wert des Attributes geparst werden
         * @return Attribute Name
         * @throws TemplateException
         */
        @Throws(TemplateException::class)
        private fun attributeName(cfml: SourceCode?, dynamic: RefBoolean?, args: ArrayList<String?>?, tag: TagLibTag?, sbType: StringBuffer?, parseExpression: BooleanArray?,
                                  allowDefaultValue: Boolean): String? {
            val _id = identifier(cfml, !allowDefaultValue, true)
            if (StringUtil.isEmpty(_id)) {
                return null
            }
            val typeDef: Int = tag.getAttributeType()
            var id: String = StringUtil.toLowerCase(_id)
            if (args.contains(id)) throw createTemplateException(cfml, "you can't use the same tag attribute [$id] twice", tag)
            args.add(id)
            if ("attributecollection".equals(id)) {
                dynamic.setValue(tag.getAttribute(id, true) == null)
                sbType.append("struct")
                parseExpression!![0] = true
                parseExpression[1] = true
            } else if (typeDef == TagLibTag.ATTRIBUTE_TYPE_FIXED || typeDef == TagLibTag.ATTRIBUTE_TYPE_MIXED) {
                val attr: TagLibTagAttr = tag.getAttribute(id, true)
                if (attr == null) {
                    if (typeDef == TagLibTag.ATTRIBUTE_TYPE_FIXED) {
                        var names: String = tag.getAttributeNames()
                        if (StringUtil.isEmpty(names)) throw createTemplateException(cfml, "Attribute [" + id + "] is not allowed for tag [" + tag.getFullName() + "]", tag)
                        try {
                            names = ListUtil.sort(names, "textnocase", null, null)
                        } catch (t: Throwable) {
                            ExceptionUtil.rethrowIfNecessary(t)
                        }
                        throw createTemplateException(cfml, "Attribute [" + id + "] is not allowed for tag [" + tag.getFullName() + "]", "valid attribute names are [$names]",
                                tag)
                    }
                    dynamic.setValue(true)
                } else {
                    id = attr.getName()
                    sbType.append(attr.getType())
                    parseExpression!![0] = attr.getRtexpr()
                }
            } else if (typeDef == TagLibTag.ATTRIBUTE_TYPE_DYNAMIC) {
                dynamic.setValue(true)
            }
            return id
        }

        /**
         * Liest den Wert eines Attribut, mithilfe des innerhalb der Tag-Lib definierten Expression
         * Transformer, ein. <br></br>
         * EBNF:<br></br>
         * `expression;`
         *
         * @param tag
         * @param type
         * @param parseExpression
         * @param isNonName
         * @return Element Eingelesener uebersetzer Wert des Attributes.
         * @throws TemplateException
         */
        @Throws(TemplateException::class)
        fun attributeValue(data: Data?, tag: TagLibTag?, type: String?, parseExpression: Boolean, isNonName: Boolean, noExpression: Expression?): Expression? {
            var expr: Expression
            try {
                var transfomer: ExprTransformer? = null
                transfomer = if (parseExpression) {
                    tag.getTagLib().getExprTransfomer()
                } else {
                    if (data.getSimpleExprTransformer() == null) {
                        data.setSimpleExprTransformer(SimpleExprTransformer('#'))
                        // set.setSpecialChar();
                    }
                    data.getSimpleExprTransformer()
                }
                if (isNonName) {
                    val pos: Int = data.srcCode.getPos()
                    expr = try {
                        transfomer.transform(data)
                    } catch (ete: TemplateException) {
                        if (data.srcCode.getPos() === pos) noExpression else throw ete
                    }
                } else {
                    if (data.settings.handleUnQuotedAttrValueAsString) {
                        val alt: Boolean = data.allowLowerThan
                        data.allowLowerThan = true
                        expr = try {
                            transfomer.transformAsString(data)
                        } finally {
                            data.allowLowerThan = alt
                        }
                    } else expr = transfomer.transform(data)
                }
                if (type!!.length() > 0) {
                    expr = data.factory.toExpression(expr, type)
                }
            } catch (e: TagLibException) {
                throw TemplateException(data.srcCode, e)
            }
            return expr
        }

        /**
         * Liest einen Identifier ein und gibt diesen als String zurueck. <br></br>
         * EBNF:<br></br>
         * `(letter | "_") {letter | "_"|digit};`
         *
         * @param throwError throw error or return null if name is invalid
         * @return Identifier String.
         * @throws TemplateException
         */
        @Throws(TemplateException::class)
        fun identifier(cfml: SourceCode?, throwError: Boolean, allowColon: Boolean): String? {
            val start: Int = cfml.getPos()
            if (!cfml.isCurrentBetween('a', 'z') && !cfml.isCurrent('_')) {
                if (throwError) throw TemplateException(cfml, "Invalid Identifier, the following character cannot be part of an identifier [" + cfml.getCurrent().toString() + "]")
                return null
            }
            do {
                cfml.next()
                if (!(cfml.isCurrentBetween('a', 'z') || cfml.isCurrentBetween('0', '9') || cfml.isCurrent('_') || allowColon && cfml.isCurrent(':') || cfml.isCurrent('-'))) {
                    break
                }
            } while (cfml.isValidIndex())
            return cfml.substring(start, cfml.getPos() - start)
        }

        fun createTemplateException(cfml: SourceCode?, msg: String?, detail: String?, tag: TagLibTag?): TemplateException? {
            val te = TemplateException(cfml, msg, detail)
            setAddional(te, tag)
            return te
        }

        fun createTemplateException(cfml: SourceCode?, msg: String?, tag: TagLibTag?): TemplateException? {
            val te = TemplateException(cfml, msg)
            setAddional(te, tag)
            return te
        }

        fun setAddional(te: TemplateException?, tlt: TagLibTag?): TemplateException? {
            setAddional(te as PageExceptionImpl?, tlt)
            return te
        }

        fun setAddional(ae: ApplicationException?, tlt: TagLibTag?): ApplicationException? {
            setAddional(ae as PageExceptionImpl?, tlt)
            return ae
        }

        private fun setAddional(pe: PageExceptionImpl?, tlt: TagLibTag?) {
            val attrs: Map<String?, TagLibTagAttr?> = tlt.getAttributes()
            var it: Iterator<Entry<String?, TagLibTagAttr?>?> = attrs.entrySet().iterator()
            var entry: Entry<String?, TagLibTagAttr?>?
            var attr: TagLibTagAttr

            // Pattern
            val pattern = StringBuilder("<")
            pattern.append(tlt.getFullName())
            var req: StringBuilder? = StringBuilder()
            var opt: StringBuilder? = StringBuilder()
            var tmp: StringBuilder
            pattern.append(" ")
            var c = 0
            while (it.hasNext()) {
                entry = it.next()
                attr = entry.getValue()
                tmp = if (attr.isRequired()) req else opt
                tmp.append(" ")
                if (!attr.isRequired()) tmp.append("[")
                if (c++ > 0) pattern.append(" ")
                tmp.append(attr.getName())
                tmp.append("=\"")
                tmp.append(attr.getType())
                tmp.append("\"")
                if (!attr.isRequired()) tmp.append("]")
            }
            if (req.length() > 0) pattern.append(req)
            if (opt.length() > 0) pattern.append(opt)
            if (tlt.getAttributeType() === TagLibTag.ATTRIBUTE_TYPE_MIXED || tlt.getAttributeType() === TagLibTag.ATTRIBUTE_TYPE_DYNAMIC) pattern.append(" ...")
            pattern.append(">")
            if (tlt.getHasBody()) {
                if (tlt.isBodyReq()) {
                    pattern.append("</")
                    pattern.append(tlt.getFullName())
                    pattern.append(">")
                } else if (tlt.isBodyFree()) {
                    pattern.append("[</")
                    pattern.append(tlt.getFullName())
                    pattern.append(">]")
                }
            }
            pe.setAdditional(KeyConstants._Pattern, pattern)

            // Documentation
            val doc = StringBuilder(tlt.getDescription())
            req = StringBuilder()
            opt = StringBuilder()
            doc.append("\n")
            it = attrs.entrySet().iterator()
            while (it.hasNext()) {
                entry = it.next()
                attr = entry.getValue()
                tmp = if (attr.isRequired()) req else opt
                tmp.append("* ")
                tmp.append(attr.getName())
                tmp.append(" (")
                tmp.append(attr.getType())
                tmp.append("): ")
                tmp.append(attr.getDescription())
                tmp.append("\n")
            }
            if (req.length() > 0) doc.append("\nRequired:\n").append(req)
            if (opt.length() > 0) doc.append("\nOptional:\n").append(opt)
            pe.setAdditional(KeyConstants._Documentation, doc)
        }
    }
}