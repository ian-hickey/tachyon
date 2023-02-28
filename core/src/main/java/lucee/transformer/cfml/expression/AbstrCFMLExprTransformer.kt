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
package lucee.transformer.cfml.expression

import java.util.ArrayList

/**
 *
 *
 * Der CFMLExprTransfomer implementiert das Interface ExprTransfomer, er bildet die Parser Grammatik
 * ab, die unten definiert ist. Er erhaelt als Eingabe CFML Code, als String oder CFMLString, der
 * einen CFML Expression erhaelt und liefert ein CFXD Element zurueck, das diesen Ausdruck abbildet.
 * Mithilfe der FunctionLibs, kann er Funktionsaufrufe, die Teil eines Ausdruck sein koennen,
 * erkennen und validieren. Dies geschieht innerhalb der Methode function. Falls ein
 * Funktionsaufruf, einer Funktion innerhalb einer FunctionLib entspricht, werden diese
 * gegeneinander verglichen und der Aufruf wird als Built-In-Funktion uebernommen, andernfalls wird
 * der Funktionsaufruf als User-Defined-Funktion interpretiert. Die Klasse Cast, Operator und
 * ElementFactory (siehe 3.2) helfen ihm beim erstellen des Ausgabedokument CFXD.
 *
 * <pre>
 * Parser Grammatik EBNF (Extended Backus-Naur Form)
 *
 * transform      = spaces impOp;
 * impOp          = eqvOp {"imp" spaces eqvOp};
 * eqvOp          = xorOp {"eqv" spaces xorOp};
 * xorOp          = orOp {"xor" spaces  orOp};
 * orOp           = andOp {("or" | "||") spaces andOp};
 * (* "||" Existiert in CFMX nicht *)
 * andOp          = notOp {("and" | "&&") spaces notOp};
 * (* "&&" Existiert in CFMX nicht *)
 * notOp          = [("not"|"!") spaces] decsionOp;
 * (* "!" Existiert in CFMX nicht *)
 * decsionOp      = concatOp {("neq"|"eq"|"gte"|"gt"|"lte"|"lt"|"ct"|
 * "contains"|"nct"|"does not contain") spaces concatOp};
 * (* "ct"=conatains und "nct"=does not contain; Existiert in CFMX nicht *)
 * concatOp       = plusMinusOp {"&" spaces plusMinusOp};
 * plusMinusOp    = modOp {("-"|"+") spaces modOp};
 * modOp          = divMultiOp {("mod" | "%") spaces divMultiOp};
 * (* modulus operator , "%" Existiert in CFMX nicht *)
 * divMultiOp     = expoOp {("*"|"/") spaces expoOp};
 * expoOp         = clip {("exp"|"^") spaces clip};
 * (*exponent operator, " exp " Existiert in CFMX nicht *)
 * clip           = ("(" spaces impOp ")" spaces) | checker;
 * checker        = string | number | dynamic | sharp;
 * string         = ("'" {"##"|"''"|"#" impOp "#"| ?-"#"-"'" } "'") |
 * (""" {"##"|""""|"#" impOp "#"| ?-"#"-""" } """);
 * number         = ["+"|"-"] digit {digit} {"." digit {digit}};
 * digit          = "0"|..|"9";
 * dynamic        = "true" | "false" | "yes" | "no" | startElement
 * {("." identifier | "[" structElement "]")[function] };
 * startElement   = identifier "(" functionArg ")" | scope | identifier;
 * scope          = "variable" | "cgi" | "url" | "form" | "session" | "application" |
 * "arguments" | "cookie" | "client ";
 * identifier     = (letter | "_") {letter | "_"|digit};
 * structElement  = "[" impOp "]";
 * functionArg    = [impOp{"," impOp}];
 * sharp          = "#" checker "#";
 * spaces         = {space};
 * space          = "\s"|"\t"|"\f"|"\t"|"\n";
 * letter         = "a"|..|"z"|"A"|..|"Z";
 *
 * {"x"}= 0 bis n mal "x"
 * ["x"]= 0 bis 1 mal "x"
 * ("x" | "y")"z" = "xz" oder "yz"
 *
</pre> *
 *
 */
abstract class AbstrCFMLExprTransformer {
    private val docCommentTransformer: DocCommentTransformer? = DocCommentTransformer()
    protected var ATTR_TYPE_NONE: Short = TagLibTagAttr.SCRIPT_SUPPORT_NONE
    protected var ATTR_TYPE_OPTIONAL: Short = TagLibTagAttr.SCRIPT_SUPPORT_OPTIONAL
    protected var ATTR_TYPE_REQUIRED: Short = TagLibTagAttr.SCRIPT_SUPPORT_REQUIRED

    interface EndCondition {
        fun isEnd(data: Data?): Boolean
    }

    /*
	 * private short mode=0; protected CFMLString cfml; protected FunctionLib[] fld; private boolean
	 * ignoreScopes=false; private boolean allowLowerThan;
	 */
    /*
	 * public class Data extends Data {
	 * 
	 * public short mode=0; public boolean insideFunction; public String tagName; public boolean isCFC;
	 * public boolean isInterface; public short context=CTX_NONE; public DocComment docComment;
	 * 
	 * 
	 * public Data(Data data,boolean allowLowerThan) {
	 * super(data.factory,data.root,data.srcCode,data.ep,data.settings,data.tlibs,data.flibs,data.
	 * scriptTags,allowLowerThan); } }
	 */
    @Throws(TemplateException::class)
    protected fun transformAsString(data: Data?, breakConditions: Array<String?>?): Expression? {
        var el: Expression? = null

        // parse the houle Page String
        comments(data)

        // String
        if (string(data).also { el = it } != null) {
            data.mode = STATIC
            return el
        }
        // Sharp
        if (sharp(data).also { el = it } != null) {
            data.mode = DYNAMIC
            return el
        }
        // Simple
        return simple(data, breakConditions)
    }

    /**
     * Initialmethode, wird aufgerufen um den internen Zustand des Objektes zu setzten.
     *
     * @param fld Function Libraries zum validieren der Funktionen
     * @param cfml CFML Code der transfomiert werden soll.
     */
    protected fun init(data: Data?): Data? {
        if (JSON_ARRAY == null) JSON_ARRAY = getFLF(data, "_literalArray")
        if (JSON_STRUCT == null) JSON_STRUCT = getFLF(data, "_literalStruct")
        if (GET_STATIC_SCOPE == null) GET_STATIC_SCOPE = getFLF(data, "_getStaticScope")
        if (GET_SUPER_STATIC_SCOPE == null) GET_SUPER_STATIC_SCOPE = getFLF(data, "_getSuperStaticScope")
        return data
    }
    /*
	 * protected Data init(Factory factory,Root root,EvaluatorPool ep,TagLib[][] tld, FunctionLib[]
	 * fld,TagLibTag[] scriptTags, SourceCode cfml, TransfomerSettings settings, boolean allowLowerThan,
	 * int x) { Data data = new Data(factory,root,ep,cfml,tld,fld,settings,allowLowerThan,scriptTags);
	 * if(JSON_ARRAY==null)JSON_ARRAY=getFLF(data,"_literalArray");
	 * if(JSON_STRUCT==null)JSON_STRUCT=getFLF(data,"_literalStruct");
	 * if(GET_STATIC_SCOPE==null)GET_STATIC_SCOPE=getFLF(data,"_getStaticScope");
	 * if(GET_SUPER_STATIC_SCOPE==null)GET_SUPER_STATIC_SCOPE=getFLF(data,"_getSuperStaticScope");
	 * return data; }
	 */
    /**
     * Startpunkt zum transfomieren einer Expression, ohne dass das Objekt neu initialisiert wird, dient
     * vererbten Objekten als Einstiegspunkt.
     *
     * @return Element
     * @throws TemplateException
     */
    @Throws(TemplateException::class)
    protected fun expression(data: Data?): Expression? {
        return assignOp(data)
    }

    /**
     * Liest einen gelableten Funktionsparamter ein <br></br>
     * EBNF:<br></br>
     * `assignOp [":" spaces assignOp];`
     *
     * @return CFXD Element
     * @throws TemplateException
     */
    @Throws(TemplateException::class)
    private fun functionArgument(data: Data?, varKeyUpperCase: Boolean): Argument? {
        return functionArgument(data, null, varKeyUpperCase)
    }

    @Throws(TemplateException::class)
    private fun functionArgument(data: Data?, type: String?, varKeyUpperCase: Boolean): Argument? {
        val expr: Expression? = assignOp(data)
        try {
            if (data.srcCode.forwardIfCurrent(":")) {
                comments(data)
                return NamedArgument(expr, assignOp(data), type, varKeyUpperCase)
            } else if (expr is DynAssign) {
                val da: DynAssign? = expr as DynAssign?
                return NamedArgument(da.getName(), da.getValue(), type, varKeyUpperCase)
            } else if (expr is Assign && expr !is OpVariable) {
                val a: Assign? = expr as Assign?
                return NamedArgument(a.getVariable(), a.getValue(), type, varKeyUpperCase)
            }
        } catch (be: TransformerException) {
            throw TemplateException(data.srcCode, be.getMessage())
        }
        return Argument(expr, type)
    }

    /**
     * Transfomiert Zuweisungs Operation. <br></br>
     * EBNF:<br></br>
     * `eqvOp ["=" spaces assignOp];`
     *
     * @return CFXD Element
     * @throws TemplateException
     */
    @Throws(TemplateException::class)
    protected fun assignOp(data: Data?): Expression? {
        var expr: Expression? = conditionalOp(data)
        if (data.srcCode.forwardIfCurrent('=')) {
            comments(data)
            if (data.mode === STATIC) expr = DynAssign(expr, assignOp(data)) else {
                if (expr is Variable) {
                    val value: Expression? = assignOp(data)
                    expr = Assign(expr as Variable?, value, data.srcCode.getPosition())
                } else if (expr is Null) {
                    val `var`: Variable = (expr as Null?).toVariable()
                    val value: Expression? = assignOp(data)
                    expr = Assign(`var`, value, data.srcCode.getPosition())
                } else if (expr is NullConstant) {
                    val `var`: Variable = (expr as NullConstant?).toVariable()
                    val value: Expression? = assignOp(data)
                    expr = Assign(`var`, value, data.srcCode.getPosition())
                } else throw TemplateException(data.srcCode, "invalid assignment left-hand side (" + expr.getClass().getName().toString() + ")")
            }
        }

        // patch for test()(); only works at the end of an expression!
        comments(data)
        while (data.srcCode.isCurrent('(')) {
            comments(data)
            val call = Call(expr)
            getFunctionMemberAttrs(data, null, false, call, null)
            call.setEnd(data.srcCode.getPosition())
            comments(data)
            expr = call
        }
        return expr
    }

    @Throws(TemplateException::class)
    private fun conditionalOp(data: Data?): Expression? {
        var expr: Expression? = impOp(data)
        if (data.srcCode.forwardIfCurrent('?')) {
            comments(data)
            // Elvis
            if (data.srcCode.forwardIfCurrent(':')) {
                comments(data)
                val right: Expression? = assignOp(data)
                if (expr is ExprBoolean) return expr
                if (expr !is Variable) throw TemplateException(data.srcCode, "left operand of the Elvis operator has to be a variable or a function call")
                val left: Variable? = expr as Variable?
                /// LDEV-1201
                /*
				 * List<Member> members = left.getMembers(); Member last=null; for(Member m:members) { last=m;
				 * m.setSafeNavigated(true); } if(last!=null) { last.setSafeNavigatedValue(right); } return left;
				 */return data.factory.opElvis(left, right)
            }
            val left: Expression? = assignOp(data)
            comments(data)
            if (!data.srcCode.forwardIfCurrent(':')) throw TemplateException(data.srcCode, "invalid conditional operator")
            comments(data)
            val right: Expression? = assignOp(data)
            expr = data.factory.opContional(expr, left, right)
        }
        return expr
    }

    /**
     * Transfomiert eine Implication (imp) Operation. <br></br>
     * EBNF:<br></br>
     * `eqvOp {"imp" spaces eqvOp};`
     *
     * @return CFXD Element
     * @throws TemplateException
     */
    @Throws(TemplateException::class)
    private fun impOp(data: Data?): Expression? {
        var expr: Expression? = eqvOp(data)
        while (data.srcCode.forwardIfCurrentAndNoWordAfter("imp")) {
            comments(data)
            expr = data.factory.opBool(expr, eqvOp(data), Factory.OP_BOOL_IMP)
        }
        return expr
    }

    /**
     * Transfomiert eine Equivalence (eqv) Operation. <br></br>
     * EBNF:<br></br>
     * `xorOp {"eqv" spaces xorOp};`
     *
     * @return CFXD Element
     * @throws TemplateException
     */
    @Throws(TemplateException::class)
    private fun eqvOp(data: Data?): Expression? {
        var expr: Expression? = xorOp(data)
        while (data.srcCode.forwardIfCurrentAndNoWordAfter("eqv")) {
            comments(data)
            expr = data.factory.opBool(expr, xorOp(data), Factory.OP_BOOL_EQV)
        }
        return expr
    }

    /**
     * Transfomiert eine Xor (xor) Operation. <br></br>
     * EBNF:<br></br>
     * `orOp {"xor" spaces  orOp};`
     *
     * @return CFXD Element
     * @throws TemplateException
     */
    @Throws(TemplateException::class)
    private fun xorOp(data: Data?): Expression? {
        var expr: Expression? = orOp(data)
        while (data.srcCode.forwardIfCurrentAndNoWordAfter("xor")) {
            comments(data)
            expr = data.factory.opBool(expr, orOp(data), Factory.OP_BOOL_XOR)
        }
        return expr
    }

    /**
     * Transfomiert eine Or (or) Operation. Im Gegensatz zu CFMX , werden "||" Zeichen auch als Or
     * Operatoren anerkannt. <br></br>
     * EBNF:<br></br>
     * `andOp {("or" | "||") spaces andOp}; (* "||" Existiert in CFMX nicht *)`
     *
     * @return CFXD Element
     * @throws TemplateException
     */
    @Throws(TemplateException::class)
    private fun orOp(data: Data?): Expression? {
        var expr: Expression? = andOp(data)
        while (data.srcCode.forwardIfCurrent("||") || data.srcCode.forwardIfCurrentAndNoWordAfter("or")) {
            comments(data)
            expr = data.factory.opBool(expr, andOp(data), Factory.OP_BOOL_OR)
        }
        return expr
    }

    /**
     * Transfomiert eine And (and) Operation. Im Gegensatz zu CFMX , werden "&&" Zeichen auch als And
     * Operatoren anerkannt. <br></br>
     * EBNF:<br></br>
     * `notOp {("and" | "&&") spaces notOp}; (* "&&" Existiert in CFMX nicht *)`
     *
     * @return CFXD Element
     * @throws TemplateException
     */
    @Throws(TemplateException::class)
    private fun andOp(data: Data?): Expression? {
        var expr: Expression? = notOp(data)
        while (data.srcCode.forwardIfCurrent("&&") || data.srcCode.forwardIfCurrentAndNoWordAfter("and")) {
            comments(data)
            expr = data.factory.opBool(expr, notOp(data), Factory.OP_BOOL_AND)
        }
        return expr
    }

    /**
     * Transfomiert eine Not (not) Operation. Im Gegensatz zu CFMX , wird das "!" Zeichen auch als Not
     * Operator anerkannt. <br></br>
     * EBNF:<br></br>
     * `[("not"|"!") spaces] decsionOp; (* "!" Existiert in CFMX nicht *)`
     *
     * @return CFXD Element
     * @throws TemplateException
     */
    @Throws(TemplateException::class)
    private fun notOp(data: Data?): Expression? {
        // And Operation
        val line: Position = data.srcCode.getPosition()
        if (data.srcCode.isCurrent('!') && !data.srcCode.isCurrent("!=")) {
            data.srcCode.next()
            comments(data)
            return data.factory.opNegate(notOp(data), line, data.srcCode.getPosition())
        } else if (data.srcCode.forwardIfCurrentAndNoWordAfter("not")) {
            comments(data)
            return data.factory.opNegate(notOp(data), line, data.srcCode.getPosition())
        }
        return decsionOp(data)
    }

    /**
     * <font f>Transfomiert eine Vergleichs Operation. <br></br>
     * EBNF:<br></br>
     * `concatOp {("neq"|"eq"|"gte"|"gt"|"lte"|"lt"|"ct"|
     * "contains"|"nct"|"does not contain") spaces concatOp};
     * (* "ct"=conatains und "nct"=does not contain; Existiert in CFMX nicht *)`
     *
     * @return CFXD Element
     * @throws TemplateException
    </font> */
    @Throws(TemplateException::class)
    private fun decsionOp(data: Data?): Expression? {
        var expr: Expression? = concatOp(data)
        var hasChanged = false
        // ct, contains
        do {
            hasChanged = false
            if (data.srcCode.isCurrent('c')) {
                if (data.srcCode.forwardIfCurrent("ct", false, true)) {
                    expr = decisionOpCreate(data, Factory.OP_DEC_CT, expr)
                    hasChanged = true
                } else if (data.srcCode.forwardIfCurrent("contains", false, true)) {
                    expr = decisionOpCreate(data, Factory.OP_DEC_CT, expr)
                    hasChanged = true
                }
            } else if (data.srcCode.forwardIfCurrent("does", "not", "contain", false, true)) {
                expr = decisionOpCreate(data, Factory.OP_DEC_NCT, expr)
                hasChanged = true
            } else if (data.srcCode.isCurrent("eq") && !data.srcCode.isCurrent("eqv")) {
                var plus = 2
                data.srcCode.setPos(data.srcCode.getPos() + 2)
                if (data.srcCode.forwardIfCurrent("ual")) plus = 5
                if (data.srcCode.isCurrentVariableCharacter()) {
                    data.srcCode.setPos(data.srcCode.getPos() - plus)
                } else {
                    expr = decisionOpCreate(data, Factory.OP_DEC_EQ, expr)
                    hasChanged = true
                }
            } else if (data.srcCode.forwardIfCurrent("==")) {
                expr = if (data.srcCode.forwardIfCurrent('=')) decisionOpCreate(data, Factory.OP_DEC_EEQ, expr) else decisionOpCreate(data, Factory.OP_DEC_EQ, expr)
                hasChanged = true
            } else if (data.srcCode.forwardIfCurrent("!=")) {
                expr = if (data.srcCode.forwardIfCurrent('=')) decisionOpCreate(data, Factory.OP_DEC_NEEQ, expr) else decisionOpCreate(data, Factory.OP_DEC_NEQ, expr)
                hasChanged = true
            } else if (data.srcCode.isCurrent('<')) {
                hasChanged = true
                if (data.srcCode.isNext('=')) {
                    data.srcCode.next()
                    data.srcCode.next()
                    expr = decisionOpCreate(data, Factory.OP_DEC_LTE, expr)
                } else if (data.srcCode.isNext('>')) {
                    data.srcCode.next()
                    data.srcCode.next()
                    expr = decisionOpCreate(data, Factory.OP_DEC_NEQ, expr)
                } else if (data.srcCode.isNext('/')) {
                    hasChanged = false
                } else {
                    data.srcCode.next()
                    expr = decisionOpCreate(data, Factory.OP_DEC_LT, expr)
                }
            } else if (data.allowLowerThan && data.srcCode.forwardIfCurrent('>')) {
                expr = if (data.srcCode.forwardIfCurrent('=')) decisionOpCreate(data, Factory.OP_DEC_GTE, expr) else decisionOpCreate(data, Factory.OP_DEC_GT, expr)
                hasChanged = true
            } else if (data.srcCode.isCurrent('g')) {
                if (data.srcCode.forwardIfCurrent("gt")) {
                    if (data.srcCode.forwardIfCurrentAndNoWordAfter("e")) {
                        if (data.srcCode.isCurrentVariableCharacter()) {
                            data.srcCode.setPos(data.srcCode.getPos() - 3)
                        } else {
                            expr = decisionOpCreate(data, Factory.OP_DEC_GTE, expr)
                            hasChanged = true
                        }
                    } else {
                        if (data.srcCode.isCurrentVariableCharacter()) {
                            data.srcCode.setPos(data.srcCode.getPos() - 2)
                        } else {
                            expr = decisionOpCreate(data, Factory.OP_DEC_GT, expr)
                            hasChanged = true
                        }
                    }
                } else if (data.srcCode.forwardIfCurrent("greater", "than", false, true)) {
                    expr = if (data.srcCode.forwardIfCurrent("or", "equal", "to", true, true)) decisionOpCreate(data, Factory.OP_DEC_GTE, expr) else decisionOpCreate(data, Factory.OP_DEC_GT, expr)
                    hasChanged = true
                } else if (data.srcCode.forwardIfCurrent("ge", false, true)) {
                    expr = decisionOpCreate(data, Factory.OP_DEC_GTE, expr)
                    hasChanged = true
                }
            } else if (data.srcCode.forwardIfCurrent("is", false, true)) {
                expr = if (data.srcCode.forwardIfCurrent("not", true, true)) decisionOpCreate(data, Factory.OP_DEC_NEQ, expr) else decisionOpCreate(data, Factory.OP_DEC_EQ, expr)
                hasChanged = true
            } else if (data.srcCode.isCurrent('l')) {
                if (data.srcCode.forwardIfCurrent("lt")) {
                    if (data.srcCode.forwardIfCurrentAndNoWordAfter("e")) {
                        if (data.srcCode.isCurrentVariableCharacter()) {
                            data.srcCode.setPos(data.srcCode.getPos() - 3)
                        } else {
                            expr = decisionOpCreate(data, Factory.OP_DEC_LTE, expr)
                            hasChanged = true
                        }
                    } else {
                        if (data.srcCode.isCurrentVariableCharacter()) {
                            data.srcCode.setPos(data.srcCode.getPos() - 2)
                        } else {
                            expr = decisionOpCreate(data, Factory.OP_DEC_LT, expr)
                            hasChanged = true
                        }
                    }
                } else if (data.srcCode.forwardIfCurrent("less", "than", false, true)) {
                    expr = if (data.srcCode.forwardIfCurrent("or", "equal", "to", true, true)) decisionOpCreate(data, Factory.OP_DEC_LTE, expr) else decisionOpCreate(data, Factory.OP_DEC_LT, expr)
                    hasChanged = true
                } else if (data.srcCode.forwardIfCurrent("le", false, true)) {
                    expr = decisionOpCreate(data, Factory.OP_DEC_LTE, expr)
                    hasChanged = true
                }
            } else if (data.srcCode.isCurrent('n')) {
                // Not Equal
                if (data.srcCode.forwardIfCurrent("neq", false, true)) {
                    expr = decisionOpCreate(data, Factory.OP_DEC_NEQ, expr)
                    hasChanged = true
                } else if (data.srcCode.forwardIfCurrent("not", "equal", false, true)) {
                    expr = decisionOpCreate(data, Factory.OP_DEC_NEQ, expr)
                    hasChanged = true
                } else if (data.srcCode.forwardIfCurrent("nct", false, true)) {
                    expr = decisionOpCreate(data, Factory.OP_DEC_NCT, expr)
                    hasChanged = true
                }
            }
        } while (hasChanged)
        return expr
    }

    @Throws(TemplateException::class)
    private fun decisionOpCreate(data: Data?, operation: Int, left: Expression?): Expression? {
        comments(data)
        return data.factory.opDecision(left, concatOp(data), operation)
    }

    /**
     * Transfomiert eine Konkatinations-Operator (&) Operation. Im Gegensatz zu CFMX , wird das "!"
     * Zeichen auch als Not Operator anerkannt. <br></br>
     * EBNF:<br></br>
     * `plusMinusOp {"&" spaces concatOp};`
     *
     * @return CFXD Element
     * @throws TemplateException
     */
    @Throws(TemplateException::class)
    private fun concatOp(data: Data?): Expression? {
        var expr: Expression? = plusMinusOp(data)
        while (data.srcCode.isCurrent('&') && !data.srcCode.isCurrent("&&")) {
            data.srcCode.next()

            // &=
            expr = if (data.srcCode.isCurrent('=') && expr is Variable) {
                data.srcCode.next()
                comments(data)
                val value: Expression? = assignOp(data)
                data.factory.opUnaryString(expr as Variable?, value, Factory.OP_UNARY_PRE, Factory.OP_UNARY_CONCAT, expr.getStart(), data.srcCode.getPosition())

                // ExprString res = OpString.toExprString(expr, right);
                // expr=new OpVariable((Variable)expr,res,data.cfml.getPosition());
            } else {
                comments(data)
                data.factory.opString(expr, plusMinusOp(data))
            }
        }
        return expr
    }

    /**
     * Transfomiert die mathematischen Operatoren Plus und Minus (1,-). <br></br>
     * EBNF:<br></br>
     * `modOp [("-"|"+") spaces plusMinusOp];`
     *
     * @return CFXD Element
     * @throws TemplateException
     */
    @Throws(TemplateException::class)
    private fun plusMinusOp(data: Data?): Expression? {
        var expr: Expression? = modOp(data)
        while (!data.srcCode.isLast()) {

            // Plus Operation
            expr = if (data.srcCode.forwardIfCurrent('+')) _plusMinusOp(data, expr, Factory.OP_DBL_PLUS) else if (data.srcCode.forwardIfCurrent('-')) _plusMinusOp(data, expr, Factory.OP_DBL_MINUS) else break
        }
        return expr
    }

    @Throws(TemplateException::class)
    private fun _plusMinusOp(data: Data?, expr: Expression?, opr: Int): Expression? {
        // +=
        // plus|Minus Assignment
        var expr: Expression? = expr
        expr = if (data.srcCode.isCurrent('=') && expr is Variable) {
            data.srcCode.next()
            comments(data)
            val value: Expression? = assignOp(data)
            data.factory.opUnaryNumber(expr as Variable?, value, Factory.OP_UNARY_PRE, opr, expr.getStart(), data.srcCode.getPosition())
        } else {
            comments(data)
            data.factory.opNumber(expr, modOp(data), opr)
        }
        return expr
    }

    /**
     * Transfomiert eine Modulus Operation. Im Gegensatz zu CFMX , wird das "%" Zeichen auch als Modulus
     * Operator anerkannt. <br></br>
     * EBNF:<br></br>
     * `divMultiOp {("mod" | "%") spaces divMultiOp}; (* modulus operator , "%" Existiert in CFMX nicht *)`
     *
     * @return CFXD Element
     * @throws TemplateException
     */
    @Throws(TemplateException::class)
    private fun modOp(data: Data?): Expression? {
        var expr: Expression? = divMultiOp(data)

        // Modulus Operation
        while (data.srcCode.forwardIfCurrent('%') || data.srcCode.forwardIfCurrentAndNoWordAfter("mod")) {
            expr = _modOp(data, expr)
        }
        return expr
    }

    @Throws(TemplateException::class)
    private fun _modOp(data: Data?, expr: Expression?): Expression? {
        if (data.srcCode.isCurrent('=') && expr is Variable) {
            data.srcCode.next()
            comments(data)
            val right: Expression? = assignOp(data)
            val res: ExprNumber = data.factory.opNumber(expr, right, Factory.OP_DBL_MODULUS)
            return OpVariable(expr as Variable?, res, data.srcCode.getPosition())
        }
        comments(data)
        return data.factory.opNumber(expr, expoOp(data), Factory.OP_DBL_MODULUS)
    }

    /**
     * Transfomiert die mathematischen Operatoren Mal und Durch (*,/). <br></br>
     * EBNF:<br></br>
     * `expoOp {("*"|"/") spaces expoOp};`
     *
     * @return CFXD Element
     * @throws TemplateException
     */
    @Throws(TemplateException::class)
    private fun divMultiOp(data: Data?): Expression? {
        var expr: Expression? = expoOp(data)
        while (!data.srcCode.isLast()) {

            // Multiply Operation
            expr = if (data.srcCode.forwardIfCurrent('*')) {
                _divMultiOp(data, expr, Factory.OP_DBL_MULTIPLY)
            } else if (data.srcCode.isCurrent('/') && !data.srcCode.isCurrent('/', '>')) {
                data.srcCode.next()
                _divMultiOp(data, expr, Factory.OP_DBL_DIVIDE)
            } else if (data.srcCode.isCurrent('\\')) {
                data.srcCode.next()
                _divMultiOp(data, expr, Factory.OP_DBL_INTDIV)
            } else {
                break
            }
        }
        return expr
    }

    @Throws(TemplateException::class)
    private fun _divMultiOp(data: Data?, expr: Expression?, iOp: Int): Expression? {
        if (data.srcCode.isCurrent('=') && expr is Variable) {
            data.srcCode.next()
            comments(data)
            val value: Expression? = assignOp(data)
            return data.factory.opUnaryNumber(expr as Variable?, value, Factory.OP_UNARY_PRE, iOp, expr.getStart(), data.srcCode.getPosition())
        }
        comments(data)
        return data.factory.opNumber(expr, expoOp(data), iOp)
    }

    /**
     * Transfomiert den Exponent Operator (^,exp). Im Gegensatz zu CFMX , werden die Zeichen " exp "
     * auch als Exponent anerkannt. <br></br>
     * EBNF:<br></br>
     * `clip {("exp"|"^") spaces clip};`
     *
     * @return CFXD Element
     * @throws TemplateException
     */
    @Throws(TemplateException::class)
    private fun expoOp(data: Data?): Expression? {
        var expr: Expression? = unaryOp(data)

        // Modulus Operation
        while (data.srcCode.forwardIfCurrent('^') || data.srcCode.forwardIfCurrentAndNoWordAfter("exp")) {
            comments(data)
            expr = data.factory.opNumber(expr, unaryOp(data), Factory.OP_DBL_EXP)
        }
        return expr
    }

    @Throws(TemplateException::class)
    private fun unaryOp(data: Data?): Expression? {
        var expr: Expression? = negatePlusMinusOp(data)

        // Plus Operation
        if (data.srcCode.forwardIfCurrent("++") && expr is Variable) expr = _unaryOp(data, expr, Factory.OP_DBL_PLUS) else if (data.srcCode.forwardIfCurrent("--") && expr is Variable) expr = _unaryOp(data, expr, Factory.OP_DBL_MINUS)
        return expr
    }

    @Throws(TemplateException::class)
    private fun _unaryOp(data: Data?, expr: Expression?, op: Int): Expression? {
        val leftEnd: Position = expr.getEnd()
        var start: Position? = null
        var end: Position? = null
        comments(data)
        if (leftEnd != null) {
            start = leftEnd
            end = Position(leftEnd.line, leftEnd.column + 2, leftEnd.pos + 2)
        }
        return if (op == Factory.OP_UNARY_CONCAT) data.factory.opUnaryString(expr as Variable?, data.factory.NUMBER_ONE(), Factory.OP_UNARY_POST, op, start, end) else data.factory.opUnaryNumber(expr as Variable?, data.factory.NUMBER_ONE(), Factory.OP_UNARY_POST, op, start, end)
    }

    /**
     * Negate Numbers
     *
     * @return CFXD Element
     * @throws TemplateException
     */
    @Throws(TemplateException::class)
    private fun negatePlusMinusOp(data: Data?): Expression? {
        // And Operation
        val line: Position = data.srcCode.getPosition()
        if (data.srcCode.forwardIfCurrent('-')) {
            // pre increment
            if (data.srcCode.forwardIfCurrent('-')) {
                comments(data)
                val expr: Expression? = clip(data)
                return if (expr is Variable) {
                    data.factory.opUnaryNumber(expr as Variable?, data.factory.NUMBER_ONE(), Factory.OP_UNARY_PRE, Factory.OP_UNARY_MINUS, line, data.srcCode.getPosition())
                } else data.factory.opNumber(data.factory.toExprNumber(expr), data.factory.createLitNumber(1), Factory.OP_DBL_MINUS)
            }
            comments(data)
            return data.factory.opNegateNumber(clip(data), Factory.OP_NEG_NBR_MINUS, line, data.srcCode.getPosition())
        } else if (data.srcCode.forwardIfCurrent('+')) {
            if (data.srcCode.forwardIfCurrent('+')) {
                comments(data)
                val expr: Expression? = clip(data)
                return if (expr is Variable) {
                    data.factory.opUnaryNumber(expr as Variable?, data.factory.NUMBER_ONE(), Factory.OP_UNARY_PRE, Factory.OP_UNARY_PLUS, line, data.srcCode.getPosition())
                } else data.factory.opNumber(data.factory.toExprNumber(expr), data.factory.createLitNumber(1), Factory.OP_DBL_PLUS)
            }
            comments(data)
            return data.factory.toExprNumber(clip(data))
        }
        return clip(data)
    }

    /**
     * Verarbeitet Ausdruecke die inerhalb einer Klammer stehen. <br></br>
     * EBNF:<br></br>
     * `("(" spaces impOp ")" spaces) | checker;`
     *
     * @return CFXD Element
     * @throws TemplateException
     */
    @Throws(TemplateException::class)
    private fun clip(data: Data?): Expression? {
        return checker(data)
    }

    /**
     * Hier werden die verschiedenen Moeglichen Werte erkannt und jenachdem wird mit der passenden
     * Methode weitergefahren <br></br>
     * EBNF:<br></br>
     * `string | number | dynamic | sharp;`
     *
     * @return CFXD Element
     * @throws TemplateException
     */
    @Throws(TemplateException::class)
    private fun checker(data: Data?): Expression? {
        var expr: Expression? = null
        // String
        if (string(data).also { expr = it } != null) {
            expr = subDynamic(data, expr, false, false)
            data.mode = STATIC // (expr instanceof Literal)?STATIC:DYNAMIC;// STATIC
            return expr
        }
        // Number
        if (number(data).also { expr = it } != null) {
            expr = subDynamic(data, expr, false, false)
            data.mode = STATIC // (expr instanceof Literal)?STATIC:DYNAMIC;// STATIC
            return expr
        }
        // component
        if (component(data).also { expr = it } != null) {
            data.mode = DYNAMIC
            return expr
        }
        // closure
        if (closure(data).also { expr = it } != null) {
            data.mode = DYNAMIC
            return expr
        }
        // lambda
        if (lambda(data).also { expr = it } != null) {
            data.mode = DYNAMIC
            return expr
        }

        // Dynamic
        if (dynamic(data).also { expr = it } != null) {
            expr = newOp(data, expr)
            expr = subDynamic(data, expr, true, false)
            data.mode = DYNAMIC
            return expr
        }
        // Sharp
        if (sharp(data).also { expr = it } != null) {
            data.mode = DYNAMIC
            return expr
        }
        // JSON
        if (json(data, JSON_ARRAY, '[', ']').also { expr = it } != null) {
            expr = subDynamic(data, expr, false, false)
            data.mode = DYNAMIC
            return expr
        }
        if (json(data, JSON_STRUCT, '{', '}').also { expr = it } != null) {
            expr = subDynamic(data, expr, false, false)
            data.mode = DYNAMIC
            return expr
        }
        throw TemplateException(data.srcCode, "Syntax Error, Invalid Construct")
    }
    /*
	 * private Expression variable(Data data) throws TemplateException { Expression expr=null;
	 * 
	 * // Dynamic if((expr=dynamic(data))!=null) { expr = subDynamic(data,expr); data.mode=DYNAMIC;
	 * return expr; } return null; }
	 */
    /**
     * Transfomiert einen lierale Zeichenkette. <br></br>
     * EBNF:<br></br>
     * `("'" {"##"|"''"|"#" impOp "#"| ?-"#"-"'" } "'") |
     * (""" {"##"|""""|"#" impOp "#"| ?-"#"-""" } """);`
     *
     * @param data
     * @return CFXD Element
     * @throws TemplateException
     */
    @Throws(TemplateException::class)
    protected fun string(data: Data?): Expression? {

        // check starting character for a string literal
        if (!data.srcCode.isCurrent('"') && !data.srcCode.isCurrent('\'')) return null
        val line: Position = data.srcCode.getPosition()

        // Init Parameter
        val quoter: Char = data.srcCode.getCurrentLower()
        var str: StringBuilder? = StringBuilder()
        var expr: Expression? = null
        while (data.srcCode.hasNext()) {
            data.srcCode.next()
            // check sharp
            if (data.srcCode.isCurrent('#')) {

                // Ecaped sharp
                if (data.srcCode.isNext('#')) {
                    data.srcCode.next()
                    str.append('#')
                } else {
                    data.srcCode.next()
                    comments(data)
                    val inner: Expression? = assignOp(data)
                    comments(data)
                    if (!data.srcCode.isCurrent('#')) throw TemplateException(data.srcCode, "Invalid Syntax Closing [#] not found")
                    var exprStr: ExprString? = null
                    if (str.length() !== 0) {
                        exprStr = data.factory.createLitString(str.toString(), line, data.srcCode.getPosition())
                        expr = if (expr != null) {
                            data.factory.opString(expr, exprStr)
                        } else exprStr
                        str = StringBuilder()
                    }
                    expr = if (expr == null) {
                        inner
                    } else {
                        data.factory.opString(expr, inner)
                    }
                }
            } else if (data.srcCode.isCurrent(quoter)) {
                // Ecaped sharp
                if (data.srcCode.isNext(quoter)) {
                    data.srcCode.next()
                    str.append(quoter)
                } else {
                    break
                }
            } else {
                str.append(data.srcCode.getCurrent())
            }
        }
        if (!data.srcCode.forwardIfCurrent(quoter)) throw TemplateException(data.srcCode, "Invalid Syntax Closing [$quoter] not found")
        if (expr == null) expr = data.factory.createLitString(str.toString(), line, data.srcCode.getPosition()) else if (str.length() !== 0) {
            expr = data.factory.opString(expr, data.factory.createLitString(str.toString(), line, data.srcCode.getPosition()))
        }
        comments(data)
        if (expr is Variable) {
            val `var`: Variable? = expr as Variable?
            `var`.fromHash(true)
        }
        return expr
    }

    /**
     * Transfomiert einen numerische Wert. Die Laenge des numerischen Wertes interessiert nicht zu
     * uebersetzungszeit, ein "Overflow" fuehrt zu einem Laufzeitfehler. Da die zu erstellende CFXD,
     * bzw. dieser Transfomer, keine Vorwegnahme des Laufzeitsystems vornimmt. <br></br>
     * EBNF:<br></br>
     * `["+"|"-"] digit {digit} {"." digit {digit}};`
     *
     * @return CFXD Element
     * @throws TemplateException
     */
    @Throws(TemplateException::class)
    private fun number(data: Data?): LitNumber? {
        // check first character is a number literal representation
        if (!(data.srcCode.isCurrentBetween('0', '9') || data.srcCode.isCurrent('.'))) return null
        val line: Position = data.srcCode.getPosition()
        val rtn = StringBuffer()

        // get digit on the left site of the dot
        if (data.srcCode.isCurrent('.')) rtn.append('0') else rtn.append(digit(data))
        // read dot if exist
        if (data.srcCode.forwardIfCurrent('.')) {
            rtn.append('.')
            var rightSite = digit(data)
            if (rightSite!!.length() > 0 && data.srcCode.forwardIfCurrent('e')) {
                var expOp: Boolean? = null
                if (data.srcCode.forwardIfCurrent('+')) expOp = Boolean.TRUE else if (data.srcCode.forwardIfCurrent('-')) expOp = Boolean.FALSE
                if (data.srcCode.isCurrentBetween('0', '9')) {
                    rightSite += if (expOp === Boolean.FALSE) "e-" else if (expOp === Boolean.TRUE) "e+" else "e"
                    rightSite += digit(data)
                } else {
                    if (expOp != null) data.srcCode.previous()
                    data.srcCode.previous()
                }
            }
            // read right side of the dot
            if (rightSite.length() === 0) rightSite = "0" // throw new TemplateException(cfml, "Number can't end with [.]"); // DIFF 23
            rtn.append(rightSite)
        } else if (data.srcCode.forwardIfCurrent('e')) {
            var expOp: Boolean? = null
            if (data.srcCode.forwardIfCurrent('+')) expOp = Boolean.TRUE else if (data.srcCode.forwardIfCurrent('-')) expOp = Boolean.FALSE
            if (data.srcCode.isCurrentBetween('0', '9')) {
                var rightSite: String? = "e"
                if (expOp === Boolean.FALSE) rightSite += "-" else if (expOp === Boolean.TRUE) rightSite += "+"
                rightSite += digit(data)
                rtn.append(rightSite)
            } else {
                if (expOp != null) data.srcCode.previous()
                data.srcCode.previous()
            }
        }
        comments(data)
        return try {
            data.factory.createLitNumber(rtn.toString(), line, data.srcCode.getPosition())
        } catch (e: PageException) {
            throw TemplateException(data.srcCode, e)
        }
    }

    /**
     * Liest die reinen Zahlen innerhalb des CFMLString aus und gibt diese als Zeichenkette zurueck.
     * <br></br>
     * EBNF:<br></br>
     * `"0"|..|"9";`
     *
     * @return digit Ausgelesene Zahlen als Zeichenkette.
     */
    private fun digit(data: Data?): String? {
        var rtn = ""
        while (data.srcCode.isValidIndex()) {
            if (!data.srcCode.isCurrentBetween('0', '9')) break
            rtn += data.srcCode.getCurrentLower()
            data.srcCode.next()
        }
        return rtn
    }

    /**
     * Liest den folgenden idetifier ein und prueft ob dieser ein boolscher Wert ist. Im Gegensatz zu
     * CFMX wird auch "yes" und "no" als bolscher <wert akzeptiert, was bei CFMX nur beim Umwandeln einer Zeichenkette zu einem boolschen Wert der Fall ist.></wert><br></br>
     * Wenn es sich um keinen bolschen Wert handelt wird der folgende Wert eingelesen mit seiner ganzen
     * Hirarchie. <br></br>
     * EBNF:<br></br>
     * `"true" | "false" | "yes" | "no" | startElement {("." identifier | "[" structElement "]" )[function] };`
     *
     * @return CFXD Element
     * @throws TemplateException
     */
    @Throws(TemplateException::class)
    private fun dynamic(data: Data?): Expression? {
        // Die Implementation weicht ein wenig von der Grammatik ab,
        // aber nicht in der Logik sondern rein wie es umgesetzt wurde.

        // get First Element of the Variable
        val line: Position = data.srcCode.getPosition()
        val id: Identifier? = identifier(data, false, true)
        if (id == null) {
            if (!data.srcCode.forwardIfCurrent('(')) return null
            comments(data)
            val expr: Expression? = assignOp(data)
            if (!data.srcCode.forwardIfCurrent(')')) throw TemplateException(data.srcCode, "Invalid Syntax Closing [)] not found")
            comments(data)
            return expr // subDynamic(expr);
        }
        val `var`: Variable?
        comments(data)

        // Boolean constant
        if (id.getString().equalsIgnoreCase("TRUE")) { // || name.equals("YES")) {
            comments(data)
            return id.getFactory().createLitBoolean(true, line, data.srcCode.getPosition())
        } else if (id.getString().equalsIgnoreCase("FALSE")) { // || name.equals("NO")) {
            comments(data)
            return id.getFactory().createLitBoolean(false, line, data.srcCode.getPosition())
        } else if (id.getString().equalsIgnoreCase("NULL") && !data.srcCode.isCurrent('.') && !data.srcCode.isCurrent('[')) {
            comments(data)
            return id.getFactory().createNullConstant(line, data.srcCode.getPosition())
        }

        // Extract Scope from the Variable
        `var` = startElement(data, id, line)
        `var`.setStart(line)
        `var`.setEnd(data.srcCode.getPosition())
        return `var`
    }

    @Throws(TemplateException::class)
    protected fun json(data: Data?, flf: FunctionLibFunction?, start: Char, end: Char): Expression? {
        var flf: FunctionLibFunction? = flf
        if (!data.srcCode.forwardIfCurrent(start)) return null
        val line: Position = data.srcCode.getPosition()
        data.srcCode.removeSpace()
        // [:|=]
        if (data.srcCode.forwardIfCurrent(':', ']') || data.srcCode.forwardIfCurrent('=', ']')) {
            flf = flf.getFunctionLib().getFunction("_literalOrderedStruct")
            val bif = BIF(data.factory, data.settings, flf)
            bif.setArgType(flf.getArgType())
            try {
                bif.setClassDefinition(flf.getFunctionClassDefinition())
            } catch (t: Throwable) {
                ExceptionUtil.rethrowIfNecessary(t)
                throw PageRuntimeException(t)
            }
            bif.setReturnType(flf.getReturnTypeAsString())
            data.ep.add(flf, bif, data.srcCode)
            val `var`: Variable = data.factory.createVariable(line, data.srcCode.getPosition())
            `var`.addMember(bif)
            return `var`
        }
        val bif = BIF(data.factory, data.settings, flf)
        bif.setArgType(flf.getArgType())
        try {
            bif.setClassDefinition(flf.getFunctionClassDefinition())
        } catch (t: Throwable) {
            ExceptionUtil.rethrowIfNecessary(t)
            throw PageRuntimeException(t)
        }
        bif.setReturnType(flf.getReturnTypeAsString())
        do {
            comments(data)
            if (data.srcCode.isCurrent(end)) break
            bif.addArgument(functionArgument(data, data.settings.dotNotationUpper))
            comments(data)
        } while (data.srcCode.forwardIfCurrent(','))
        comments(data)
        if (!data.srcCode.forwardIfCurrent(end)) throw TemplateException(data.srcCode, "Invalid Syntax Closing [$end] not found")
        comments(data)
        if (flf.hasTteClass()) {
            val tmp: FunctionLibFunction = flf.getEvaluator().pre(bif, flf)
            if (tmp != null && tmp !== flf) {
                bif.setFlf(tmp.also { flf = it })
                bif.setArgType(flf.getArgType())
                try {
                    bif.setClassDefinition(flf.getFunctionClassDefinition())
                } catch (t: Throwable) {
                    ExceptionUtil.rethrowIfNecessary(t)
                    throw PageRuntimeException(t)
                }
                bif.setReturnType(flf.getReturnTypeAsString())
            }
        }
        data.ep.add(flf, bif, data.srcCode)
        val `var`: Variable = data.factory.createVariable(line, data.srcCode.getPosition())
        `var`.addMember(bif)
        return `var`
    }

    @Throws(TemplateException::class)
    private fun closure(data: Data?): Expression? {
        if (!data.srcCode.forwardIfCurrent("function", '(')) return null
        data.srcCode.previous()
        val func: Function? = closurePart(data, "closure_" + CreateUniqueId.invoke(), Component.ACCESS_PUBLIC, Component.MODIFIER_NONE, "any", data.srcCode.getPosition(), true)
        func.setParent(data.getParent())
        return FunctionAsExpression(func)
    }

    @Throws(TemplateException::class)
    protected abstract fun closurePart(data: Data?, id: String?, access: Int, modifier: Int, rtnType: String?, line: Position?, closure: Boolean): Function?
    @Throws(TemplateException::class)
    private fun component(data: Data?): Expression? {
        val start: Int = data.srcCode.getPos()
        if (!data.srcCode.forwardIfCurrent("new", "component")) return null

        // component need to be followed by attributes (component test=1 {) or directly by a curly bracked
        if (!data.srcCode.isCurrent(' ') && !data.srcCode.isCurrent('{')) {
            data.srcCode.setPos(start)
            return null
        }

        // exclude "new Component("
        /*
		 * data.srcCode.removeSpace(); if (data.srcCode.isCurrent('(')) { data.srcCode.setPos(start); return
		 * null; } data.srcCode.revertRemoveSpace();
		 */data.srcCode.setPos(data.srcCode.getPos() - 9) // go before "component"
        val tc: TagComponent? = componentStatement(data, data.getParent())
        tc.setParent(data.getParent())
        tc.setInline(true)
        tc.addAttribute(Attribute(false, "name", data.factory.createLitString("inlinecomponent_" + CreateUniqueId.invoke()), "string"))
        return ComponentAsExpression(tc)
    }

    @Throws(TemplateException::class)
    protected abstract fun componentStatement(data: Data?, parent: Body?): TagComponent?
    @Throws(TemplateException::class)
    private fun lambda(data: Data?): Expression? {
        val pos: Int = data.srcCode.getPos()
        if (!data.srcCode.forwardIfCurrent("(")) return null
        var args: ArrayList<lucee.transformer.bytecode.statement.Argument?>? = null
        // data.cfml.previous();
        args = try {
            getScriptFunctionArguments(data)
        } catch (e: TemplateException) {
            // if there is a template exception, the argument syntax is not correct, and must not be a lambda
            // expression
            // TODO find a better way to test for lambda than to attempt processing the arguments and catch an
            // exception if it fails.
            data.srcCode.setPos(pos)
            return null
        }
        if (!data.srcCode.forwardIfCurrent(")")) {
            data.srcCode.setPos(pos)
            return null
        }
        data.srcCode.removeSpace()
        if (!data.srcCode.forwardIfCurrent("=>")) {
            data.srcCode.setPos(pos)
            return null
        }
        return FunctionAsExpression(
                lambdaPart(data, "lambda_" + CreateUniqueId.invoke(), Component.ACCESS_PUBLIC, Component.MODIFIER_NONE, "any", data.srcCode.getPosition(), args))
    }

    @Throws(TemplateException::class)
    protected abstract fun lambdaPart(data: Data?, id: String?, access: Int, modifier: Int, rtnType: String?, line: Position?,
                                      args: ArrayList<lucee.transformer.bytecode.statement.Argument?>?): Function?

    @Throws(TemplateException::class)
    protected abstract fun getScriptFunctionArguments(data: Data?): ArrayList<lucee.transformer.bytecode.statement.Argument?>?
    protected fun getFLF(data: Data?, name: String?): FunctionLibFunction? {
        var flf: FunctionLibFunction? = null
        for (i in 0 until data.flibs.length) {
            flf = data.flibs.get(i).getFunction(name)
            if (flf != null) break
        }
        return flf
    }

    @Throws(TemplateException::class)
    private fun subDynamic(data: Data?, expr: Expression?, tryStatic: Boolean, isStaticChild: Boolean): Expression? {
        var expr: Expression? = expr
        var isStaticChild = isStaticChild
        var name: String? = null
        var invoker: Invoker? = null
        // Loop over nested Variables
        var safeNavigation: Boolean
        while (data.srcCode.isValidIndex()) {
            safeNavigation = false
            var nameProp: ExprString? = null
            var namePropUC: ExprString? = null
            // []
            if (data.srcCode.forwardIfCurrent('[')) {
                isStaticChild = false
                // get Next Var
                nameProp = structElement(data)
                namePropUC = nameProp
                // Valid Syntax ???
                if (!data.srcCode.forwardIfCurrent(']')) throw TemplateException(data.srcCode, "Invalid Syntax Closing []] not found")
            } else if (isStaticChild || data.srcCode.forwardIfCurrent('.') || data.srcCode.forwardIfCurrent('?', '.').also { safeNavigation = it }) {
                isStaticChild = false
                // Extract next Var String
                comments(data)
                val line: Position = data.srcCode.getPosition()
                name = identifier(data, true)
                if (name == null) throw TemplateException(data.srcCode, "Invalid identifier")
                comments(data)
                nameProp = Identifier.toIdentifier(data.factory, name, line, data.srcCode.getPosition())
                namePropUC = Identifier.toIdentifier(data.factory, name, if (data.settings.dotNotationUpper) Identifier.CASE_UPPER else Identifier.CASE_ORIGNAL, line,
                        data.srcCode.getPosition())
            } else {
                break
            }
            comments(data)
            if (expr is Invoker) {
                invoker = expr as Invoker?
            } else {
                invoker = ExpressionInvoker(expr)
                expr = invoker
            }

            // safe navigation
            var member: Member?
            if (safeNavigation) {
                val members: List<Member?> = invoker.getMembers()
                if (members.size() > 0) {
                    member = members[members.size() - 1]
                    member.setSafeNavigated(true)
                }
            }

            // Method
            if (data.srcCode.isCurrent('(')) {
                if (nameProp == null && name != null) nameProp = Identifier.toIdentifier(data.factory, name, Identifier.CASE_ORIGNAL, null, null) // properly this is never used
                invoker.addMember(getFunctionMember(data, nameProp, false).also { member = it })
            } else invoker.addMember(data.factory.createDataMember(namePropUC).also { member = it })
            if (safeNavigation) {
                member.setSafeNavigated(true)
            }
        }

        // static scipe call?

        // STATIC SCOPE CALL
        if (tryStatic) {
            comments(data)
            val staticCall: Expression? = staticScope(data, expr)
            if (staticCall != null) return staticCall
        }
        return expr
    }

    @Throws(TemplateException::class)
    private fun staticScope(data: Data?, expr: Expression?): Expression? {
        if (data.srcCode.forwardIfCurrent("::")) {
            if (expr !is Variable) throw TemplateException(data.srcCode, "Invalid syntax before [::]")
            val old: Variable? = expr as Variable?
            // set back to read again as a component path
            data.srcCode.setPos(old.getStart().pos)

            // now we read the component path
            val componentPath: ExprString? = readComponentPath(data)
            if (!data.srcCode.forwardIfCurrent("::")) throw TemplateException(data.srcCode, "Invalid syntax before [::]" + data.srcCode.getCurrent())
            comments(data)
            var bif: BIF? = null
            if (componentPath is LitString) {
                val ls: LitString? = componentPath as LitString?
                if ("super".equalsIgnoreCase(ls.getString())) {
                    bif = ASMUtil.createBif(data, GET_SUPER_STATIC_SCOPE)
                }
            }

            // now we generate a _getStaticScope function call with that path
            if (bif == null) {
                bif = ASMUtil.createBif(data, GET_STATIC_SCOPE)
                bif.addArgument(Argument(componentPath, "string"))
            }
            val `var`: Variable = data.factory.createVariable(old.getStart(), data.srcCode.getPosition())
            `var`.addMember(bif)

            // now we are reading what is coming after ":::"
            return subDynamic(data, `var`, false, true)
        }
        return null
    }

    @Throws(TemplateException::class)
    private fun newOp(data: Data?, expr: Expression?): Expression? {
        if (expr !is Variable) return expr
        val `var`: Variable? = expr as Variable?
        val m: Member = `var`.getFirstMember() as? DataMember ?: return expr
        val n: ExprString = (m as DataMember).getName() as? LitString ?: return expr
        val ls: LitString = n as LitString
        if (!"new".equalsIgnoreCase(ls.getString())) return expr
        val start: Int = data.srcCode.getPos()
        val exprName: ExprString? = readComponentPath(data)
        if (exprName == null) {
            data.srcCode.setPos(start)
            return expr
        }
        comments(data)
        if (data.srcCode.isCurrent('(')) {
            val func: FunctionMember? = getFunctionMember(data, Identifier.toIdentifier(data.factory, "_createComponent", Identifier.CASE_ORIGNAL, null, null), true)
            func.addArgument(Argument(exprName, "string"))
            val v: Variable = expr.getFactory().createVariable(expr.getStart(), expr.getEnd())
            v.addMember(func)
            comments(data)
            return v
        }
        data.srcCode.setPos(start)
        return expr
    }

    @Throws(TemplateException::class)
    private fun readComponentPath(data: Data?): ExprString? {
        // first identifier
        var name = identifier(data, true)
        if (name != null) {
            val fullName = StringBuilder()
            fullName.append(name)
            // Loop over additional identifier
            while (data.srcCode.isValidIndex()) {
                if (data.srcCode.forwardIfCurrent('.')) {
                    comments(data)
                    name = identifier(data, true)
                    if (name == null) return null
                    fullName.append('.')
                    fullName.append(name)
                    comments(data)
                } else break
            }

            // sub component
            /*
			 * if (data.srcCode.forwardIfCurrent(':')) { fullName.append(':'); name = identifier(data,true);
			 * if(name==null) return null; fullName.append(name); }
			 */return data.factory.createLitString(fullName.toString())
        }
        val str: Expression? = string(data)
        return if (str != null) {
            data.factory.toExprString(str)
        } else null
    }

    /**
     * Extrahiert den Start Element einer Variale, dies ist entweder eine Funktion, eine Scope
     * Definition oder eine undefinierte Variable. <br></br>
     * EBNF:<br></br>
     * `identifier "(" functionArg ")" | scope | identifier;`
     *
     * @param name Einstiegsname
     * @return CFXD Element
     * @throws TemplateException
     */
    @Throws(TemplateException::class)
    private fun startElement(data: Data?, name: Identifier?, line: Position?): Variable? {

        // check function
        if (data.srcCode.isCurrent('(')) {
            val func: FunctionMember? = getFunctionMember(data, name, true)
            val `var`: Variable = name.getFactory().createVariable(line, data.srcCode.getPosition())
            `var`.addMember(func)
            comments(data)
            return `var`
        }

        // check scope
        var `var`: Variable? = scope(data, name, line)
        if (`var` != null) return `var`

        // undefined variable
        `var` = name.getFactory().createVariable(line, data.srcCode.getPosition())
        `var`.addMember(data.factory.createDataMember(name))
        comments(data)
        return `var`
    }

    /**
     * Liest einen CFML Scope aus, falls der folgende identifier keinem Scope entspricht, gibt die
     * Variable null zurueck. <br></br>
     * EBNF:<br></br>
     * `"variable" | "cgi" | "url" | "form" | "session" | "application" | "arguments" | "cookie" | " client";`
     *
     * @param id String identifier, wird aus Optimierungszwechen nicht innerhalb dieser Funktion
     * ausgelsen.
     * @return CFXD Variable Element oder null
     * @throws TemplateException
     */
    @Throws(TemplateException::class)
    private fun scope(data: Data?, id: Identifier?, line: Position?): Variable? {
        val idStr: String = id.getUpper()
        if (idStr.equals("ARGUMENTS")) return data.factory.createVariable(Scope.SCOPE_ARGUMENTS, line, data.srcCode.getPosition()) else if (idStr.equals("LOCAL")) return data.factory.createVariable(Scope.SCOPE_LOCAL, line, data.srcCode.getPosition()) else if (idStr.equals("VAR")) {
            val _id: Identifier? = identifier(data, false, true)
            if (_id != null) {
                comments(data)
                val local: Variable = data.factory.createVariable(ScopeSupport.SCOPE_VAR, line, data.srcCode.getPosition())
                if (!"LOCAL".equalsIgnoreCase(_id.getString())) local.addMember(data.factory.createDataMember(_id)) else {
                    local.ignoredFirstMember(true)
                }
                return local
            }
        } else if (idStr.equals("VARIABLES")) return data.factory.createVariable(Scope.SCOPE_VARIABLES, line, data.srcCode.getPosition()) else if (idStr.equals("REQUEST")) return data.factory.createVariable(Scope.SCOPE_REQUEST, line, data.srcCode.getPosition()) else if (idStr.equals("SERVER")) return data.factory.createVariable(Scope.SCOPE_SERVER, line, data.srcCode.getPosition())
        if (data.settings.ignoreScopes) return null
        if (idStr.equals("CGI")) return data.factory.createVariable(Scope.SCOPE_CGI, line, data.srcCode.getPosition()) else if (idStr.equals("SESSION")) return data.factory.createVariable(Scope.SCOPE_SESSION, line, data.srcCode.getPosition()) else if (idStr.equals("APPLICATION")) return data.factory.createVariable(Scope.SCOPE_APPLICATION, line, data.srcCode.getPosition()) else if (idStr.equals("FORM")) return data.factory.createVariable(Scope.SCOPE_FORM, line, data.srcCode.getPosition()) else if (idStr.equals("URL")) return data.factory.createVariable(Scope.SCOPE_URL, line, data.srcCode.getPosition()) else if (idStr.equals("CLIENT")) return data.factory.createVariable(Scope.SCOPE_CLIENT, line, data.srcCode.getPosition()) else if (idStr.equals("COOKIE")) return data.factory.createVariable(Scope.SCOPE_COOKIE, line, data.srcCode.getPosition()) else if (idStr.equals("CLUSTER")) return data.factory.createVariable(Scope.SCOPE_CLUSTER, line, data.srcCode.getPosition())
        return null
    }

    /**
     * Liest einen Identifier aus und gibt diesen als String zurueck. <br></br>
     * EBNF:<br></br>
     * `(letter | "_") {letter | "_"|digit};`
     *
     * @param firstCanBeNumber
     * @param upper
     * @return Identifier.
     */
    protected fun identifier(data: Data?, firstCanBeNumber: Boolean, upper: Boolean): Identifier? {
        val start: Position = data.srcCode.getPosition()
        if (!data.srcCode.isCurrentLetter() && !data.srcCode.isCurrentSpecial()) {
            if (!firstCanBeNumber) return null else if (!data.srcCode.isCurrentBetween('0', '9')) return null
        }
        do {
            data.srcCode.next()
            if (!(data.srcCode.isCurrentLetter() || data.srcCode.isCurrentBetween('0', '9') || data.srcCode.isCurrentSpecial())) {
                break
            }
        } while (data.srcCode.isValidIndex())
        return Identifier.toIdentifier(data.factory, data.srcCode.substring(start.pos, data.srcCode.getPos() - start.pos),
                if (upper && data.settings.dotNotationUpper) Identifier.CASE_UPPER else Identifier.CASE_ORIGNAL, start, data.srcCode.getPosition())
    }

    protected fun identifier(data: Data?, firstCanBeNumber: Boolean): String? {
        val start: Int = data.srcCode.getPos()
        if (!data.srcCode.isCurrentLetter() && !data.srcCode.isCurrentSpecial()) {
            if (!firstCanBeNumber) return null else if (!data.srcCode.isCurrentBetween('0', '9')) return null
        }
        do {
            data.srcCode.next()
            if (!(data.srcCode.isCurrentLetter() || data.srcCode.isCurrentBetween('0', '9') || data.srcCode.isCurrentSpecial())) {
                break
            }
        } while (data.srcCode.isValidIndex())
        return data.srcCode.substring(start, data.srcCode.getPos() - start)
    }

    /**
     * Transfomiert ein Collection Element das in eckigen Klammern aufgerufen wird. <br></br>
     * EBNF:<br></br>
     * `"[" impOp "]"`
     *
     * @return CFXD Element
     * @throws TemplateException
     */
    @Throws(TemplateException::class)
    private fun structElement(data: Data?): ExprString? {
        comments(data)
        val name: ExprString = data.factory.toExprString(assignOp(data))
        if (name is LitString) (name as LitString).fromBracket(true)
        comments(data)
        return name
    }

    /**
     * Liest die Argumente eines Funktonsaufruf ein und prueft ob die Funktion innerhalb der FLD
     * (Function Library Descriptor) definiert ist. Falls sie existiert wird die Funktion gegen diese
     * geprueft und ein built-in-function CFXD Element generiert, ansonsten ein normales funcion-call
     * Element. <br></br>
     * EBNF:<br></br>
     * `[impOp{"," impOp}];`
     *
     * @param name Identifier der Funktion als Zeichenkette
     * @param checkLibrary Soll geprueft werden ob die Funktion innerhalb der Library existiert.
     * @return CFXD Element
     * @throws TemplateException
     */
    @Throws(TemplateException::class)
    private fun getFunctionMember(data: Data?, name: ExprString?, checkLibrary: Boolean): FunctionMember? {

        // get Function Library
        var checkLibrary = checkLibrary
        checkLibrary = checkLibrary && data.flibs != null
        var flf: FunctionLibFunction? = null
        if (checkLibrary) {
            if (name !is Literal) throw TemplateException(data.srcCode, "Syntax error") // should never happen!
            for (i in 0 until data.flibs.length) {
                flf = data.flibs.get(i).getFunction((name as Literal?).getString())
                if (flf != null) break
            }
            if (flf == null) {
                checkLibrary = false
            }
        }
        var fm: FunctionMember? = null
        while (true) {
            val pos: Int = data.srcCode.getPos()
            // Element Function
            if (checkLibrary) {
                val bif = BIF(data.factory, data.settings, flf)
                // TODO data.ep.add(flf, bif, data.srcCode);
                bif.setArgType(flf.getArgType())
                try {
                    bif.setClassDefinition(flf.getFunctionClassDefinition())
                } catch (t: Throwable) {
                    ExceptionUtil.rethrowIfNecessary(t)
                    throw PageRuntimeException(t)
                }
                bif.setReturnType(flf.getReturnTypeAsString())
                fm = bif
                if (flf.getArgType() === FunctionLibFunction.ARG_DYNAMIC && flf.hasDefaultValues()) {
                    val args: ArrayList<FunctionLibFunctionArg?> = flf.getArg()
                    val it: Iterator<FunctionLibFunctionArg?> = args.iterator()
                    var arg: FunctionLibFunctionArg?
                    while (it.hasNext()) {
                        arg = it.next()
                        if (arg.getDefaultValue() != null) bif.addArgument(
                                NamedArgument(data.factory.createLitString(arg.getName()), data.factory.createLitString(arg.getDefaultValue()), arg.getTypeAsString(), false))
                    }
                }
            } else {
                fm = UDF(name)
            }
            val count = getFunctionMemberAttrs(data, name, checkLibrary, fm, flf)
            if (checkLibrary) {
                // pre
                if (flf.hasTteClass()) {
                    val tmp: FunctionLibFunction = flf.getEvaluator().pre(fm as BIF?, flf)
                    if (tmp != null && tmp !== flf) {
                        flf = tmp
                        data.srcCode.setPos(pos)
                        continue
                    }
                }

                // check max attributes
                run {
                    val isDynamic = flf.getArgType() === FunctionLibFunction.ARG_DYNAMIC
                    val max: Int = flf.getArgMax()
                    // Dynamic
                    if (isDynamic) {
                        if (max != -1 && max < fm.getArguments().length) throw TemplateException(data.srcCode,
                                "Too many arguments (" + max + ":" + fm.getArguments().length + ") in function [ " + ASMUtil.display(name) + " ]")
                    } else {
                        if (flf.getArg().size() < fm.getArguments().length) {
                            val te = TemplateException(data.srcCode, "Too many arguments (" + flf.getArg().size().toString() + ":" + fm.getArguments().length.toString() + ") in function call [" + ASMUtil.display(name).toString() + "]")
                            UDFUtil.addFunctionDoc(te, flf)
                            throw te
                        }
                    }
                }

                // check min attributes
                if (flf.getArgMin() > count) {
                    val te = TemplateException(data.srcCode, "Too few arguments in function [" + ASMUtil.display(name).toString() + "]")
                    if (flf.getArgType() === FunctionLibFunction.ARG_FIX) UDFUtil.addFunctionDoc(te, flf)
                    throw te
                }

                // evaluator
                if (flf.hasTteClass()) {
                    flf.getEvaluator().execute(fm as BIF?, flf)
                }
            }
            comments(data)
            if (checkLibrary) data.ep.add(flf, fm as BIF?, data.srcCode)
            break
        }
        return fm
    }

    @Throws(TemplateException::class)
    private fun getFunctionMemberAttrs(data: Data?, name: ExprString?, checkLibrary: Boolean, fm: Func?, flf: FunctionLibFunction?): Int {
        // Function Attributes
        var arrFuncLibAtt: ArrayList<FunctionLibFunctionArg?>? = null
        // int libLen = 0;
        if (checkLibrary) {
            arrFuncLibAtt = flf.getArg()
            // libLen = arrFuncLibAtt.size();
        }
        var count = 0
        do {
            data.srcCode.next()
            comments(data)

            // finish
            if (count == 0 && data.srcCode.isCurrent(')')) break

            // Argument arg;
            if (checkLibrary && flf.getArgType() !== FunctionLibFunction.ARG_DYNAMIC) {
                // current attribues from library
                var _type: String?
                _type = try {
                    arrFuncLibAtt.get(count).getTypeAsString()
                } catch (e: IndexOutOfBoundsException) {
                    null
                }
                fm.addArgument(functionArgument(data, _type, false))
            } else {
                fm.addArgument(functionArgument(data, false))
            }
            comments(data)
            count++
            if (data.srcCode.isCurrent(')')) break
        } while (data.srcCode.isCurrent(','))
        if (!data.srcCode.forwardIfCurrent(')')) {
            if (name != null) {
                throw TemplateException(data.srcCode, "Invalid Syntax Closing [)] for function [" + (if (flf != null) flf.getName() else ASMUtil.display(name)).toString() + "] not found")
            }
            throw TemplateException(data.srcCode, "Invalid Syntax Closing [)] not found")
        }
        return count
    }

    /**
     * Sharps (#) die innerhalb von Expressions auftauchen haben in CFML keine weitere Beteutung und
     * werden durch diese Methode einfach entfernt. <br></br>
     * Beispiel:<br></br>
     * `arrayLen(#arr#)` und `arrayLen(arr)` sind identisch. EBNF:<br></br>
     * `"#" checker "#";`
     *
     * @return CFXD Element
     * @throws TemplateException
     */
    @Throws(TemplateException::class)
    private fun sharp(data: Data?): Expression? {
        if (!data.srcCode.forwardIfCurrent('#')) return null
        val expr: Expression?
        comments(data)
        val old: Boolean = data.allowLowerThan
        data.allowLowerThan = true
        expr = assignOp(data)
        data.allowLowerThan = old
        comments(data)
        if (!data.srcCode.forwardIfCurrent('#')) throw TemplateException(data.srcCode, "Syntax Error, Invalid Construct " + if (data.srcCode.length() < 30) data.srcCode.toString() else "")
        comments(data)
        return expr
    }

    /**
     * @param data
     * @return parsed Element
     * @throws TemplateException
     */
    @Throws(TemplateException::class)
    private fun simple(data: Data?, breakConditions: Array<String?>?): Expression? {
        val sb = StringBuffer()
        val line: Position = data.srcCode.getPosition()
        outer@ while (data.srcCode.isValidIndex()) {
            for (i in breakConditions.indices) {
                if (data.srcCode.isCurrent(breakConditions!![i])) break@outer
            }
            if (data.srcCode.isCurrent('"') || data.srcCode.isCurrent('#') || data.srcCode.isCurrent('\'')) {
                throw TemplateException(data.srcCode, "Simple attribute value can't contain [" + data.srcCode.getCurrent().toString() + "]")
            }
            sb.append(data.srcCode.getCurrent())
            data.srcCode.next()
        }
        comments(data)
        return data.factory.createLitString(sb.toString(), line, data.srcCode.getPosition())
    }

    /**
     * Liest alle folgenden Komentare ein. <br></br>
     * EBNF:<br></br>
     * `{?-"\n"} "\n";`
     *
     * @param data
     * @throws TemplateException
     */
    @Throws(TemplateException::class)
    protected fun comments(data: Data?) {
        data.srcCode.removeSpace()
        while (comment(data)) {
            data.srcCode.removeSpace()
        }
    }

    /**
     * Liest einen Einzeiligen Kommentar ein. <br></br>
     * EBNF:<br></br>
     * `{?-"\n"} "\n";`
     *
     * @return bool Wurde ein Kommentar entfernt?
     * @throws TemplateException
     */
    @Throws(TemplateException::class)
    private fun comment(data: Data?): Boolean {
        return if (singleLineComment(data.srcCode) || multiLineComment(data) || CFMLTransformer.comment(data.srcCode)) true else false
    }
    /**
     * Liest einen Mehrzeiligen Kommentar ein. <br></br>
     * EBNF:<br></br>
     * `?-"*/
    ";`
    *
    * @ return bool Wurde ein Kommentar entfernt?
    * @throws TemplateException
    */
    @Throws(TemplateException::class)
    private fun multiLineComment(data: Data?): Boolean {
        val cfml: SourceCode = data.srcCode
        if (!cfml.forwardIfCurrent("/*")) return false
        val pos: Int = cfml.getPos()
        val isDocComment: Boolean = cfml.isCurrent('*')
        while (cfml.isValidIndex()) {
            if (cfml.isCurrent("*/")) break
            cfml.next()
        }
        if (!cfml.forwardIfCurrent("*/")) {
            cfml.setPos(pos)
            throw TemplateException(cfml, "comment is not closed")
        }
        if (isDocComment) {
            val comment: String = cfml.substring(pos - 2, cfml.getPos() - pos)
            data.docComment = docCommentTransformer.transform(data.factory, comment)
        }
        return true
    }

    /**
     * Liest einen Einzeiligen Kommentar ein. <br></br>
     * EBNF:<br></br>
     * `{?-"\n"} "\n";`
     *
     * @return bool Wurde ein Kommentar entfernt?
     */
    private fun singleLineComment(cfml: SourceCode?): Boolean {
        return if (!cfml.forwardIfCurrent("//")) false else cfml.nextLine()
    }

    companion object {
        private const val STATIC: Short = 0
        private const val DYNAMIC: Short = 1
        private var GET_STATIC_SCOPE: FunctionLibFunction? = null
        private var GET_SUPER_STATIC_SCOPE: FunctionLibFunction? = null
        private var JSON_ARRAY: FunctionLibFunction? = null
        protected var JSON_STRUCT: FunctionLibFunction? = null
        val CTX_OTHER: Short = TagLibTagScript.CTX_OTHER
        val CTX_NONE: Short = TagLibTagScript.CTX_NONE
        val CTX_IF: Short = TagLibTagScript.CTX_IF
        val CTX_ELSE_IF: Short = TagLibTagScript.CTX_ELSE_IF
        val CTX_ELSE: Short = TagLibTagScript.CTX_ELSE
        val CTX_FOR: Short = TagLibTagScript.CTX_FOR
        val CTX_WHILE: Short = TagLibTagScript.CTX_WHILE
        val CTX_DO_WHILE: Short = TagLibTagScript.CTX_DO_WHILE
        val CTX_CFC: Short = TagLibTagScript.CTX_CFC
        val CTX_INTERFACE: Short = TagLibTagScript.CTX_INTERFACE
        val CTX_FUNCTION: Short = TagLibTagScript.CTX_FUNCTION
        val CTX_BLOCK: Short = TagLibTagScript.CTX_BLOCK
        val CTX_FINALLY: Short = TagLibTagScript.CTX_FINALLY
        val CTX_SWITCH: Short = TagLibTagScript.CTX_SWITCH
        val CTX_TRY: Short = TagLibTagScript.CTX_TRY
        val CTX_CATCH: Short = TagLibTagScript.CTX_CATCH
        val CTX_TRANSACTION: Short = TagLibTagScript.CTX_TRANSACTION
        val CTX_THREAD: Short = TagLibTagScript.CTX_THREAD
        val CTX_SAVECONTENT: Short = TagLibTagScript.CTX_SAVECONTENT
        val CTX_LOCK: Short = TagLibTagScript.CTX_LOCK
        val CTX_LOOP: Short = TagLibTagScript.CTX_LOOP
        val CTX_QUERY: Short = TagLibTagScript.CTX_QUERY
        val CTX_ZIP: Short = TagLibTagScript.CTX_ZIP
        val CTX_STATIC: Short = TagLibTagScript.CTX_STATIC
        protected var SEMI_BLOCK: EndCondition? = object : EndCondition {
            @Override
            override fun isEnd(data: Data?): Boolean {
                return data.srcCode.isCurrent('{') || data.srcCode.isCurrent(';')
            }
        }
        protected var SEMI: EndCondition? = object : EndCondition {
            @Override
            override fun isEnd(data: Data?): Boolean {
                return data.srcCode.isCurrent(';')
            }
        }
        protected var COMMA_ENDBRACKED: EndCondition? = object : EndCondition {
            @Override
            override fun isEnd(data: Data?): Boolean {
                return data.srcCode.isCurrent(',') || data.srcCode.isCurrent(')')
            }
        }
    }
}