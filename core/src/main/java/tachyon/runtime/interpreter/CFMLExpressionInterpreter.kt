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
package tachyon.runtime.interpreter

import java.util.ArrayList

/**
 *
 *
 * Der CFMLExprTransfomer implementiert das Interface ExprTransfomer, er bildet die Parser Grammatik
 * ab, die unten definiert ist. Er erhaelt als Eingabe CFML Code, als String oder CFMLString, der
 * einen CFML Expression erhaelt und liefert ein CFXD Element zurueck, das diesen Ausdruck abbildet.
 * Mithilfe der FunctionLib's, kann er Funktionsaufrufe, die Teil eines Ausdruck sein koennen,
 * erkennen und validieren. Dies geschieht innerhalb der Methode function. Falls ein
 * Funktionsaufruf, einer Funktion innerhalb einer FunctionLib entspricht, werden diese
 * gegeneinander verglichen und der Aufruf wird als Build-In-Funktion uebernommen, andernfalls wird
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
 *
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
 * "arguments" | "cookie" | " client";
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
class CFMLExpressionInterpreter @JvmOverloads constructor(limited: Boolean = true) {
    // private static final int CASE_TYPE_UPPER = 0;
    // private static final int CASE_TYPE_LOWER = 1;
    // private static final int CASE_TYPE_ORIGINAL = 2;
    protected var mode: Short = 0
    protected var cfml: ParserString? = null
    protected var pc: PageContext? = null
    private var fld: FunctionLib? = null
    protected var allowNullConstant = false
    private var preciseMath = false
    private val isJson: Boolean
    private val limited: Boolean
    private var config: ConfigPro? = null
    @Throws(PageException::class)
    fun interpret(pc: PageContext?, str: String?): Object? {
        return interpret(pc, str, false)
    }

    @Throws(PageException::class)
    fun interpret(pc: PageContext?, str: String?, preciseMath: Boolean): Object? {
        cfml = ParserString(str)
        this.preciseMath = preciseMath
        init(pc)
        if (fld != null) {
            if (LITERAL_ARRAY == null) LITERAL_ARRAY = fld.getFunction("_literalArray")
            if (LITERAL_STRUCT == null) LITERAL_STRUCT = fld.getFunction("_literalStruct")
            if (JSON_ARRAY == null) JSON_ARRAY = fld.getFunction("_jsonArray")
            if (JSON_STRUCT == null) JSON_STRUCT = fld.getFunction("_jsonStruct")
            if (LITERAL_ORDERED_STRUCT == null) LITERAL_ORDERED_STRUCT = fld.getFunction("_literalOrderedStruct")
        } else {
            if (JSON_ARRAY == null) {
                // TODO read from FLD
                JSON_ARRAY = FunctionLibFunction(true)
                JSON_ARRAY.setName("_jsonArray")
                JSON_ARRAY.setFunctionClass(ClassDefinitionImpl(JsonArray::class.java))
                JSON_ARRAY.setArgType(FunctionLibFunction.ARG_DYNAMIC)
                JSON_ARRAY.setReturn("array")
            }
            if (JSON_STRUCT == null) {
                // TODO read from FLD
                JSON_STRUCT = FunctionLibFunction(true)
                JSON_STRUCT.setName("_jsonStruct")
                JSON_STRUCT.setFunctionClass(ClassDefinitionImpl(JsonStruct::class.java))
                JSON_STRUCT.setArgType(FunctionLibFunction.ARG_DYNAMIC)
                JSON_STRUCT.setReturn("struct")
            }
        }
        cfml.removeSpace()
        val ref: Ref? = assignOp()
        cfml.removeSpace()
        if (cfml.isAfterLast()) {
            // data.put(str+":"+preciseMath,ref);
            return ref.getValue(pc)
        }
        if (cfml.toString().length() > 1024) throw InterpreterException("Syntax Error, invalid Expression", "[" + cfml.toString().substring(0, 1024).toString() + "]")
        throw InterpreterException("Syntax Error, invalid Expression [" + cfml.toString().toString() + "]")
    }

    private fun init(pc: PageContext?) {
        var pc: PageContext? = pc
        pc = ThreadLocalPageContext.get(pc)
        this.pc = pc
        var dialect: Int = CFMLEngine.DIALECT_CFML
        if (this.pc != null) {
            config = this.pc.getConfig() as ConfigPro
            dialect = this.pc.getCurrentTemplateDialect()
        } else {
            config = ThreadLocalPageContext.getConfig() as ConfigPro
            if (config == null) {
                try {
                    config = CFMLEngineFactory.getInstance().createConfig(null, "localhost", "/index.cfm") as ConfigPro // TODO set a context root
                } catch (e: Exception) {
                }
            }
        }
        fld = if (config == null) null else config.getCombinedFLDs(dialect)
    }

    /*
	 * private FunctionLibFunction getFLF(String name) { FunctionLibFunction flf=null; for (int i = 0; i
	 * < flds.length; i++) { flf = flds[i].getFunction(name); if (flf != null) break; } return flf; }
	 */
    @Throws(PageException::class)
    fun interpretPart(pc: PageContext?, cfml: ParserString?): Object? {
        this.cfml = cfml
        init(pc)
        cfml.removeSpace()
        return assignOp().getValue(pc)
    }

    /**
     * Liest einen gelableten Funktionsparamter ein <br></br>
     * EBNF:<br></br>
     * `assignOp [":" spaces assignOp];`
     *
     * @return CFXD Element
     * @throws PageException
     */
    @Throws(PageException::class)
    private fun functionArgDeclarationVarString(): Ref? {
        cfml.removeSpace()
        val str = StringBuilder()
        var id: String? = null
        while (identifier(false).also { id = it } != null) {
            if (str.length() > 0) str.append('.')
            str.append(id)
            cfml.removeSpace()
            if (!cfml.forwardIfCurrent('.')) break
            cfml.removeSpace()
        }
        cfml.removeSpace()
        if (str.length() > 0 && cfml.charAt(cfml.getPos() - 1) !== '.') return LString(str.toString())
        throw InterpreterException("invalid variable name definition")
    }

    /**
     * Liest einen gelableten Funktionsparamter ein <br></br>
     * EBNF:<br></br>
     * `assignOp [":" spaces assignOp];`
     *
     * @return CFXD Element
     * @throws PageException
     */
    @Throws(PageException::class)
    private fun functionArgDeclaration(): Ref? {
        var ref: Ref? = impOp()
        if (cfml.forwardIfCurrent(':') || cfml.forwardIfCurrent('=')) {
            cfml.removeSpace()
            ref = LFunctionValue(ref, assignOp())
        }
        return ref
    }

    /**
     * Transfomiert Zuweisungs Operation. <br></br>
     * EBNF:<br></br>
     * `eqvOp ["=" spaces assignOp];`
     *
     * @return CFXD Element
     * @throws PageException
     */
    @Throws(PageException::class)
    protected fun assignOp(): Ref? {
        var ref: Ref? = contOp()
        if (cfml.forwardIfCurrent('=')) {
            cfml.removeSpace()
            if (mode == STATIC || ref is Literal) {
                ref = DynAssign(ref, assignOp(), limited)
            } else {
                ref = Assign(ref, assignOp(), limited)
            }
        }
        return ref
    }

    @Throws(PageException::class)
    private fun contOp(): Ref? {
        var ref: Ref? = impOp()
        while (cfml.forwardIfCurrent('?')) {
            cfml.removeSpace()
            if (cfml.forwardIfCurrent(':')) {
                cfml.removeSpace()
                val right: Ref? = assignOp()
                ref = Elvis(ref, right, limited)
            } else {
                val left: Ref? = assignOp()
                if (!cfml.forwardIfCurrent(':')) throw InterpreterException("Syntax Error, invalid conditional operator [" + cfml.toString().toString() + "]")
                cfml.removeSpace()
                val right: Ref? = assignOp()
                ref = Cont(ref, left, right, limited)
            }
        }
        return ref
    }

    /**
     * Transfomiert eine Implication (imp) Operation. <br></br>
     * EBNF:<br></br>
     * `eqvOp {"imp" spaces eqvOp};`
     *
     * @return CFXD Element
     * @throws PageException
     */
    @Throws(PageException::class)
    private fun impOp(): Ref? {
        var ref: Ref? = eqvOp()
        while (cfml.forwardIfCurrentAndNoWordAfter("imp")) {
            cfml.removeSpace()
            ref = Imp(ref, eqvOp(), limited)
        }
        return ref
    }

    /**
     * Transfomiert eine Equivalence (eqv) Operation. <br></br>
     * EBNF:<br></br>
     * `xorOp {"eqv" spaces xorOp};`
     *
     * @return CFXD Element
     * @throws PageException
     */
    @Throws(PageException::class)
    private fun eqvOp(): Ref? {
        var ref: Ref? = xorOp()
        while (cfml.forwardIfCurrent("eqv")) {
            cfml.removeSpace()
            ref = EQV(ref, xorOp(), limited)
        }
        return ref
    }

    /**
     * Transfomiert eine Xor (xor) Operation. <br></br>
     * EBNF:<br></br>
     * `orOp {"xor" spaces  orOp};`
     *
     * @return CFXD Element
     * @throws PageException
     */
    @Throws(PageException::class)
    private fun xorOp(): Ref? {
        var ref: Ref? = orOp()
        while (cfml.forwardIfCurrent("xor")) {
            cfml.removeSpace()
            ref = Xor(ref, orOp(), limited)
        }
        return ref
    }

    /**
     * Transfomiert eine Or (or) Operation. Im Gegensatz zu CFMX , werden "||" Zeichen auch als Or
     * Operatoren anerkannt. <br></br>
     * EBNF:<br></br>
     * `andOp {("or" | "||") spaces andOp}; (* "||" Existiert in CFMX nicht *)`
     *
     * @return CFXD Element
     * @throws PageException
     */
    @Throws(PageException::class)
    private fun orOp(): Ref? {
        var ref: Ref? = andOp()
        while (cfml.isValidIndex() && (cfml.forwardIfCurrent("||") || cfml.forwardIfCurrent("or"))) {
            cfml.removeSpace()
            ref = Or(ref, andOp(), limited)
        }
        return ref
    }

    /**
     * Transfomiert eine And (and) Operation. Im Gegensatz zu CFMX , werden "&&" Zeichen auch als And
     * Operatoren anerkannt. <br></br>
     * EBNF:<br></br>
     * `notOp {("and" | "&&") spaces notOp}; (* "&&" Existiert in CFMX nicht *)`
     *
     * @return CFXD Element
     * @throws PageException
     */
    @Throws(PageException::class)
    private fun andOp(): Ref? {
        var ref: Ref? = notOp()
        while (cfml.isValidIndex() && (cfml.forwardIfCurrent("&&") || cfml.forwardIfCurrent("and"))) {
            cfml.removeSpace()
            ref = And(ref, notOp(), limited)
        }
        return ref
    }

    /**
     * Transfomiert eine Not (not) Operation. Im Gegensatz zu CFMX , wird das "!" Zeichen auch als Not
     * Operator anerkannt. <br></br>
     * EBNF:<br></br>
     * `[("not"|"!") spaces] decsionOp; (* "!" Existiert in CFMX nicht *)`
     *
     * @return CFXD Element
     * @throws PageException
     */
    @Throws(PageException::class)
    private fun notOp(): Ref? {
        if (cfml.isValidIndex()) {
            if (cfml.isCurrent('!') && !cfml.isCurrent("!=")) {
                cfml.next()
                cfml.removeSpace()
                return Not(decsionOp(), limited)
            } else if (cfml.forwardIfCurrentAndNoWordAfter("not")) {
                cfml.removeSpace()
                return Not(decsionOp(), limited)
            }
        }
        return decsionOp()
    }

    /**
     * <font f>Transfomiert eine Vergleichs Operation. <br></br>
     * EBNF:<br></br>
     * `concatOp {("neq"|"eq"|"gte"|"gt"|"lte"|"lt"|"ct"|
     * "contains"|"nct"|"does not contain") spaces concatOp};
     * (* "ct"=conatains und "nct"=does not contain; Existiert in CFMX nicht *)`
     *
     * @return CFXD Element
     * @throws PageException
    </font> */
    @Throws(PageException::class)
    private fun decsionOp(): Ref? {
        var ref: Ref? = concatOp()
        var hasChanged = false
        // ct, contains
        if (cfml.isValidIndex()) {
            do {
                hasChanged = false
                if (cfml.isCurrent('c')) {
                    if (cfml.forwardIfCurrent("ct")) {
                        cfml.removeSpace()
                        ref = CT(ref, concatOp(), limited)
                        hasChanged = true
                    } else if (cfml.forwardIfCurrent("contains")) {
                        cfml.removeSpace()
                        ref = CT(ref, concatOp(), limited)
                        hasChanged = true
                    }
                } else if (cfml.forwardIfCurrent("does", "not", "contain")) {
                    cfml.removeSpace()
                    ref = NCT(ref, concatOp(), limited)
                    hasChanged = true
                } else if (cfml.isCurrent("eq") && !cfml.isCurrent("eqv")) {
                    cfml.setPos(cfml.getPos() + 2)
                    cfml.forwardIfCurrent("ual")
                    cfml.removeSpace()
                    ref = EQ(ref, concatOp(), limited)
                    hasChanged = true
                } else if (cfml.forwardIfCurrent("==")) {
                    if (cfml.forwardIfCurrent('=')) {
                        cfml.removeSpace()
                        ref = EEQ(ref, concatOp(), limited)
                    } else {
                        cfml.removeSpace()
                        ref = EQ(ref, concatOp(), limited)
                    }
                    hasChanged = true
                } else if (cfml.forwardIfCurrent("!=")) {
                    if (cfml.forwardIfCurrent('=')) {
                        cfml.removeSpace()
                        ref = NEEQ(ref, concatOp(), limited)
                    } else {
                        cfml.removeSpace()
                        ref = NEQ(ref, concatOp(), limited)
                    }
                    hasChanged = true
                } else if (cfml.forwardIfCurrent('<')) {
                    if (cfml.forwardIfCurrent('=')) {
                        cfml.removeSpace()
                        ref = LTE(ref, concatOp(), limited)
                    } else if (cfml.forwardIfCurrent('>')) {
                        cfml.removeSpace()
                        ref = NEQ(ref, concatOp(), limited)
                    } else {
                        cfml.removeSpace()
                        ref = LT(ref, concatOp(), limited)
                    }
                    hasChanged = true
                } else if (cfml.forwardIfCurrent('>')) {
                    if (cfml.forwardIfCurrent('=')) {
                        cfml.removeSpace()
                        ref = GTE(ref, concatOp(), limited)
                    } else {
                        cfml.removeSpace()
                        ref = GT(ref, concatOp(), limited)
                    }
                    hasChanged = true
                } else if (cfml.isCurrent('g')) {
                    if (cfml.forwardIfCurrent("gt")) {
                        if (cfml.forwardIfCurrent('e')) {
                            cfml.removeSpace()
                            ref = GTE(ref, concatOp(), limited)
                        } else {
                            cfml.removeSpace()
                            ref = GT(ref, concatOp(), limited)
                        }
                        hasChanged = true
                    } else if (cfml.forwardIfCurrent("greater", "than")) {
                        if (cfml.forwardIfCurrent("or", "equal", "to", true)) {
                            cfml.removeSpace()
                            ref = GTE(ref, concatOp(), limited)
                        } else {
                            cfml.removeSpace()
                            ref = GT(ref, concatOp(), limited)
                        }
                        hasChanged = true
                    } else if (cfml.forwardIfCurrent("ge")) {
                        cfml.removeSpace()
                        ref = GTE(ref, concatOp(), limited)
                        hasChanged = true
                    }
                } else if (cfml.forwardIfCurrent("is")) {
                    if (cfml.forwardIfCurrent("not", true)) {
                        cfml.removeSpace()
                        ref = NEQ(ref, concatOp(), limited)
                    } else {
                        cfml.removeSpace()
                        ref = EQ(ref, concatOp(), limited)
                    }
                    hasChanged = true
                } else if (cfml.isCurrent('l')) {
                    if (cfml.forwardIfCurrent("lt")) {
                        if (cfml.forwardIfCurrent('e')) {
                            cfml.removeSpace()
                            ref = LTE(ref, concatOp(), limited)
                        } else {
                            cfml.removeSpace()
                            ref = LT(ref, concatOp(), limited)
                        }
                        hasChanged = true
                    } else if (cfml.forwardIfCurrent("less", "than")) {
                        if (cfml.forwardIfCurrent("or", "equal", "to", true)) {
                            cfml.removeSpace()
                            ref = LTE(ref, concatOp(), limited)
                        } else {
                            cfml.removeSpace()
                            ref = LT(ref, concatOp(), limited)
                        }
                        hasChanged = true
                    } else if (cfml.forwardIfCurrent("le")) {
                        cfml.removeSpace()
                        ref = LTE(ref, concatOp(), limited)
                        hasChanged = true
                    }
                } else if (cfml.isCurrent('n')) {
                    // Not Equal
                    if (cfml.forwardIfCurrent("neq")) {
                        cfml.removeSpace()
                        ref = NEQ(ref, concatOp(), limited)
                        hasChanged = true
                    } else if (cfml.forwardIfCurrent("not", "equal")) {
                        cfml.removeSpace()
                        ref = NEQ(ref, concatOp(), limited)
                        hasChanged = true
                    } else if (cfml.forwardIfCurrent("nct")) {
                        cfml.removeSpace()
                        ref = NCT(ref, concatOp(), limited)
                        hasChanged = true
                    }
                }
            } while (hasChanged)
        }
        return ref
    }

    /**
     * Transfomiert eine Konkatinations-Operator (&) Operation. Im Gegensatz zu CFMX , wird das "!"
     * Zeichen auch als Not Operator anerkannt. <br></br>
     * EBNF:<br></br>
     * `plusMinusOp {"&" spaces concatOp};`
     *
     * @return CFXD Element
     * @throws PageException
     */
    @Throws(PageException::class)
    private fun concatOp(): Ref? {
        var ref: Ref? = plusMinusOp()
        while (cfml.isCurrent('&') && !cfml.isNext('&')) {
            cfml.next()
            ref = _concat(ref)
        }
        return ref
    }

    /**
     * Transfomiert die mathematischen Operatoren Plus und Minus (1,-). <br></br>
     * EBNF:<br></br>
     * `modOp [("-"|"+") spaces plusMinusOp];`
     *
     * @return CFXD Element
     * @throws PageException
     */
    @Throws(PageException::class)
    private fun plusMinusOp(): Ref? {
        var ref: Ref? = modOp()
        while (!cfml.isLast()) {
            // Plus Operation
            ref = if (cfml.forwardIfCurrent('+')) {
                _plus(ref)
            } else if (cfml.forwardIfCurrent('-')) {
                _minus(ref)
            } else break
        }
        return ref
    }

    @Throws(PageException::class)
    private fun _plus(ref: Ref?): Ref? {
        // +=
        var ref: Ref? = ref
        if (cfml.isCurrent('=')) {
            cfml.next()
            cfml.removeSpace()
            val right: Ref? = assignOp()
            val res: Ref = if (preciseMath) BigPlus(ref, right, limited) else Plus(ref, right, limited)
            ref = Assign(ref, res, limited)
        } else {
            cfml.removeSpace()
            ref = if (preciseMath) BigPlus(ref, modOp(), limited) else Plus(ref, modOp(), limited)
        }
        return ref
    }

    @Throws(PageException::class)
    private fun _minus(ref: Ref?): Ref? {
        // -=
        var ref: Ref? = ref
        if (cfml.isCurrent('=')) {
            cfml.next()
            cfml.removeSpace()
            val right: Ref? = assignOp()
            val res: Ref = if (preciseMath) BigMinus(ref, right, limited) else Minus(ref, right, limited)
            ref = Assign(ref, res, limited)
        } else {
            cfml.removeSpace()
            ref = if (preciseMath) BigMinus(ref, modOp(), limited) else Minus(ref, modOp(), limited)
        }
        return ref
    }

    @Throws(PageException::class)
    private fun _div(ref: Ref?): Ref? {
        // /=
        var ref: Ref? = ref
        if (cfml.forwardIfCurrent('=')) {
            cfml.removeSpace()
            val right: Ref? = assignOp()
            val res: Ref = if (preciseMath) BigDiv(ref, right, limited) else Div(ref, right, limited)
            ref = Assign(ref, res, limited)
        } else {
            cfml.removeSpace()
            ref = if (preciseMath) BigDiv(ref, expoOp(), limited) else Div(ref, expoOp(), limited)
        }
        return ref
    }

    @Throws(PageException::class)
    private fun _intdiv(ref: Ref?): Ref? {
        // \=
        var ref: Ref? = ref
        if (cfml.forwardIfCurrent('=')) {
            cfml.removeSpace()
            val right: Ref? = assignOp()
            val res: Ref = if (preciseMath) BigIntDiv(ref, right, limited) else IntDiv(ref, right, limited)
            ref = Assign(ref, res, limited)
        } else {
            cfml.removeSpace()
            ref = if (preciseMath) BigIntDiv(ref, expoOp(), limited) else IntDiv(ref, expoOp(), limited)
        }
        return ref
    }

    @Throws(PageException::class)
    private fun _mod(ref: Ref?): Ref? {
        // %=
        var ref: Ref? = ref
        if (cfml.forwardIfCurrent('=')) {
            cfml.removeSpace()
            val right: Ref? = assignOp()
            val res: Ref = if (preciseMath) BigMod(ref, right, limited) else Mod(ref, right, limited)
            ref = Assign(ref, res, limited)
        } else {
            cfml.removeSpace()
            ref = if (preciseMath) BigMod(ref, divMultiOp(), limited) else Mod(ref, divMultiOp(), limited)
        }
        return ref
    }

    @Throws(PageException::class)
    private fun _concat(ref: Ref?): Ref? {
        // &=
        var ref: Ref? = ref
        if (cfml.forwardIfCurrent('=')) {
            cfml.removeSpace()
            val right: Ref? = assignOp()
            val res: Ref = Concat(ref, right, limited)
            ref = Assign(ref, res, limited)
        } else {
            cfml.removeSpace()
            ref = Concat(ref, plusMinusOp(), limited)
        }
        return ref
    }

    @Throws(PageException::class)
    private fun _multi(ref: Ref?): Ref? {
        // \=
        var ref: Ref? = ref
        if (cfml.forwardIfCurrent('=')) {
            cfml.removeSpace()
            val right: Ref? = assignOp()
            val res: Ref = if (preciseMath) BigMulti(ref, right, limited) else Multi(ref, right, limited)
            ref = Assign(ref, res, limited)
        } else {
            cfml.removeSpace()
            ref = if (preciseMath) BigMulti(ref, expoOp(), limited) else Multi(ref, expoOp(), limited)
        }
        return ref
    }

    /**
     * Transfomiert eine Modulus Operation. Im Gegensatz zu CFMX , wird das "%" Zeichen auch als Modulus
     * Operator anerkannt. <br></br>
     * EBNF:<br></br>
     * `divMultiOp {("mod" | "%") spaces divMultiOp}; (* modulus operator , "%" Existiert in CFMX nicht *)`
     *
     * @return CFXD Element
     * @throws PageException
     */
    @Throws(PageException::class)
    private fun modOp(): Ref? {
        var ref: Ref? = divMultiOp()
        while (cfml.isValidIndex() && (cfml.forwardIfCurrent('%') || cfml.forwardIfCurrent("mod"))) {
            ref = _mod(ref)
        }
        return ref
    }

    /**
     * Transfomiert die mathematischen Operatoren Mal und Durch (*,/). <br></br>
     * EBNF:<br></br>
     * `expoOp {("*"|"/") spaces expoOp};`
     *
     * @return CFXD Element
     * @throws PageException
     */
    @Throws(PageException::class)
    private fun divMultiOp(): Ref? {
        var ref: Ref? = expoOp()
        while (!cfml.isLast()) {
            // Multiply Operation
            ref = if (cfml.forwardIfCurrent('*')) {
                _multi(ref)
            } else if (cfml.isCurrent('/') && !cfml.isCurrent("/>")) {
                cfml.next()
                _div(ref)
            } else if (cfml.isCurrent('\\')) {
                cfml.next()
                _intdiv(ref)
            } else {
                break
            }
        }
        return ref
    }

    /**
     * Transfomiert den Exponent Operator (^,exp). Im Gegensatz zu CFMX , werden die Zeichen " exp "
     * auch als Exponent anerkannt. <br></br>
     * EBNF:<br></br>
     * `clip {("exp"|"^") spaces clip};`
     *
     * @return CFXD Element
     * @throws PageException
     */
    @Throws(PageException::class)
    private fun expoOp(): Ref? {
        var ref: Ref? = unaryOp()
        while (cfml.isValidIndex() && (cfml.forwardIfCurrent('^') || cfml.forwardIfCurrent("exp"))) {
            cfml.removeSpace()
            ref = Exp(ref, unaryOp(), limited)
        }
        return ref
    }

    @Throws(PageException::class)
    private fun unaryOp(): Ref? {
        var ref: Ref? = negateMinusOp()
        if (cfml.forwardIfCurrent("--")) ref = _unaryOp(ref, false) else if (cfml.forwardIfCurrent("++")) ref = _unaryOp(ref, true)
        return ref
    }

    @Throws(PageException::class)
    private fun _unaryOp(ref: Ref?, isPlus: Boolean): Ref? {
        var ref: Ref? = ref
        cfml.removeSpace()
        val res: Ref = if (preciseMath) BigPlus(ref, if (isPlus) LNumber.ONE else LNumber.MINUS_ONE, limited) else Plus(ref, if (isPlus) LNumber.ONE else LNumber.MINUS_ONE, limited)
        ref = Assign(ref, res, limited)
        return if (preciseMath) BigPlus(ref, if (isPlus) LNumber.MINUS_ONE else LNumber.ONE, limited) else Plus(ref, if (isPlus) LNumber.MINUS_ONE else LNumber.ONE, limited)
    }

    /**
     * Liest die Vordlobe einer Zahl ein
     *
     * @return CFXD Element
     * @throws PageException
     */
    @Throws(PageException::class)
    private fun negateMinusOp(): Ref? {
        // And Operation
        if (cfml.forwardIfCurrent('-')) {
            if (cfml.forwardIfCurrent('-')) {
                cfml.removeSpace()
                val expr: Ref? = clip()
                val res: Ref = if (preciseMath) BigMinus(expr, LNumber.ONE, limited) else Minus(expr, LNumber.ONE, limited)
                return Assign(expr, res, limited)
            }
            cfml.removeSpace()
            return Negate(clip(), limited)
        }
        if (cfml.forwardIfCurrent('+')) {
            if (cfml.forwardIfCurrent('+')) {
                cfml.removeSpace()
                val expr: Ref? = clip()
                val res: Ref = if (preciseMath) BigPlus(expr, LNumber.ONE, limited) else Plus(expr, LNumber.ONE, limited)
                return Assign(expr, res, limited)
            }
            cfml.removeSpace()
            return Casting("numeric", CFTypes.TYPE_NUMERIC, clip())
        }
        return clip()
    }

    /**
     * Verarbeitet Ausdruecke die inerhalb einer Klammer stehen. <br></br>
     * EBNF:<br></br>
     * `("(" spaces impOp ")" spaces) | checker;`
     *
     * @return CFXD Element
     * @throws PageException
     */
    @Throws(PageException::class)
    private fun clip(): Ref? {
        return checker()
    }

    /**
     * Hier werden die verschiedenen Moeglichen Werte erkannt und jenachdem wird mit der passenden
     * Methode weitergefahren <br></br>
     * EBNF:<br></br>
     * `string | number | dynamic | sharp;`
     *
     * @return CFXD Element
     * @throws PageException
     */
    @Throws(PageException::class)
    private fun checker(): Ref? {
        var ref: Ref? = null
        // String
        if (cfml.isCurrentQuoter()) {
            // mode=STATIC; is at the end of the string function because must set after execution
            return string()
        }
        // Number
        if (cfml.isCurrentDigit() || cfml.isCurrent('.')) {
            // mode=STATIC; is at the end of the string function because must set after execution
            return number()
        }
        // Dynamic
        if (dynamic().also { ref = it } != null) {
            mode = DYNAMIC
            return ref
        }
        // Sharp
        if (!limited && sharp().also { ref = it } != null) {
            mode = DYNAMIC
            return ref
        }
        // JSON
        if (json(if (isJson) JSON_ARRAY else LITERAL_ARRAY, '[', ']').also { ref = it } != null) {
            mode = DYNAMIC
            return ref
        }
        if (json(if (isJson) JSON_STRUCT else LITERAL_STRUCT, '{', '}').also { ref = it } != null) {
            mode = DYNAMIC
            return ref
        }
        if (cfml.isAfterLast() && cfml.toString().trim().length() === 0) return LString("")

        // else Error
        var str: String = cfml.toString()
        val pos: Int = cfml.getPos()
        if (str.length() > 100) {
            // Failure is in the beginning
            str = if (pos <= 10) {
                str.substring(0, 20).toString() + " ..."
            } else if (str.length() - pos <= 10) {
                "... " + str.substring(str.length() - 20, str.length())
            } else {
                "... " + str.substring(pos - 10, pos + 10).toString() + " ..."
            }
        }
        throw InterpreterException("Syntax Error, Invalid Construct", " at position " + (pos + 1) + " in [" + str + "]")
    }

    @Throws(PageException::class)
    protected fun json(flf: FunctionLibFunction?, start: Char, end: Char): Ref? {
        var flf: FunctionLibFunction? = flf
        if (!cfml.isCurrent(start)) return null
        /*
		 * String[] str = cfml.toString().split(","); if(cfml.getCurrent() == '{' && cfml.getNext() != '}'
		 * && str.length >1) { outer:for(int i=0; i<str.length; i++) { String strr = str[i].toString();
		 * if(str[i].charAt(0) == '{') strr = new StringBuilder(strr).deleteCharAt(0).toString(); String[]
		 * strsplit = strr.split("[:]"); if((strsplit[1].charAt(0) == '{' || strsplit[1].charAt(0) == '[')
		 * && strsplit[0].charAt(0) == '"') { str = strsplit[1].toString().split(","); continue outer; }
		 * else if(strsplit[0].charAt(0) != '"' || (strsplit[1].charAt(0) != '"' &&
		 * !Character.isDigit(strsplit[1].charAt(0)) && strsplit[1].charAt(0) != '[')) { throw new
		 * TemplateException("Invalid json value" +cfml); } } }
		 */if (cfml.forwardIfCurrent('[', ':', ']') || cfml.forwardIfCurrent('[', '=', ']')) {
            return BIFCall(LITERAL_ORDERED_STRUCT, arrayOfNulls<Ref?>(0))
        }
        val args: Array<Ref?>? = if (flf == null) null else functionArg(flf.getName(), false, flf, end)
        if (args != null && args.size > 0 && flf === LITERAL_ARRAY) {
            if (args[0] is LFunctionValue) {
                for (i in 1 until args.size) {
                    if (args[i] !is LFunctionValue) throw TemplateException("invalid argument for literal ordered struct, only named arguments are allowed like {name:\"value\",name2:\"value2\"}")
                }
                flf = LITERAL_ORDERED_STRUCT
            } else {
                for (i in 1 until args.size) {
                    if (args[i] is LFunctionValue) throw TemplateException("invalid argument for literal array, no named arguments are allowed")
                }
            }
        }
        return BIFCall(flf, args)
    }

    /**
     * Transfomiert einen lierale Zeichenkette. <br></br>
     * EBNF:<br></br>
     * `("'" {"##"|"''"|"#" impOp "#"| ?-"#"-"'" } "'") |
     * (""" {"##"|""""|"#" impOp "#"| ?-"#"-""" } """);`
     *
     * @return CFXD Element
     * @throws PageException
     */
    @Throws(PageException::class)
    protected fun string(): Ref? {

        // Init Parameter
        val quoter: Char = cfml.getCurrentLower()
        val str = LStringBuffer()
        var value: Ref? = null
        while (cfml.hasNext()) {
            cfml.next()
            // check sharp
            if (!limited && cfml.isCurrent('#')) {
                if (cfml.isNext('#')) {
                    cfml.next()
                    str.append('#')
                } else {
                    cfml.next()
                    cfml.removeSpace()
                    if (!str.isEmpty() || value != null) str.append(assignOp()) else value = assignOp()
                    cfml.removeSpace()
                    if (!cfml.isCurrent('#')) throw InterpreterException("Invalid Syntax Closing [#] not found")
                }
            } else if (cfml.isCurrent(quoter)) {
                if (cfml.isNext(quoter)) {
                    cfml.next()
                    str.append(quoter)
                } else {
                    break
                }
            } else {
                str.append(cfml.getCurrent())
            }
        }
        if (!cfml.forwardIfCurrent(quoter)) throw InterpreterException("Invalid String Literal Syntax Closing [$quoter] not found")
        cfml.removeSpace()
        mode = STATIC
        return if (value != null) {
            if (str.isEmpty()) value else Concat(value, str, limited)
        } else str
    }

    /**
     * Transfomiert einen numerische Wert. Die Laenge des numerischen Wertes interessiert nicht zu
     * uebersetzungszeit, ein "Overflow" fuehrt zu einem Laufzeitfehler. Da die zu erstellende CFXD,
     * bzw. dieser Transfomer, keine Vorwegnahme des Laufzeitsystems vornimmt. <br></br>
     * EBNF:<br></br>
     * `["+"|"-"] digit {digit} {"." digit {digit}};`
     *
     * @return CFXD Element
     * @throws PageException
     */
    @Throws(PageException::class)
    private fun number(): Ref? {
        // check first character is a number literal representation
        val rtn = StringBuilder(6)

        // get digit on the left site of the dot
        if (cfml.isCurrent('.')) rtn.append('0') else digit(rtn)
        // read dot if exist
        if (cfml.forwardIfCurrent('.')) {
            rtn.append('.')
            val before: Int = cfml.getPos()
            digit(rtn)
            if (before < cfml.getPos() && cfml.forwardIfCurrent('e')) {
                var expOp: Boolean? = null
                if (cfml.forwardIfCurrent('+')) expOp = Boolean.TRUE else if (cfml.forwardIfCurrent('-')) expOp = Boolean.FALSE
                if (cfml.isCurrentDigit()) {
                    if (expOp === Boolean.FALSE) rtn.append("e-") else if (expOp === Boolean.TRUE) rtn.append("e+") else rtn.append('e')
                    digit(rtn)
                } else {
                    if (expOp != null) cfml.previous()
                    cfml.previous()
                }
            }
            // read right side of the dot
            if (before == cfml.getPos()) throw InterpreterException("Number can't end with [.]")
        } else if (cfml.forwardIfCurrent('e')) {
            var expOp: Boolean? = null
            if (cfml.forwardIfCurrent('+')) expOp = Boolean.TRUE else if (cfml.forwardIfCurrent('-')) expOp = Boolean.FALSE
            if (cfml.isCurrentBetween('0', '9')) {
                rtn.append('e')
                if (expOp === Boolean.FALSE) rtn.append('-') else if (expOp === Boolean.TRUE) rtn.append('+')
                digit(rtn)
            } else {
                if (expOp != null) cfml.previous()
                cfml.previous()
            }
        }
        cfml.removeSpace()
        mode = STATIC
        return LNumber(rtn.toString())
    }

    /**
     * Liest die reinen Zahlen innerhalb des CFMLString aus und gibt diese als Zeichenkette zurueck.
     * <br></br>
     * EBNF:<br></br>
     * `"0"|..|"9";`
     *
     * @param rtn
     */
    private fun digit(rtn: StringBuilder?) {
        while (cfml.isValidIndex()) {
            if (!cfml.isCurrentDigit()) break
            rtn.append(cfml.getCurrentLower())
            cfml.next()
        }
    }

    /**
     * Liest den folgenden idetifier ein und prueft ob dieser ein boolscher Wert ist. Im Gegensatz zu
     * CFMX wird auch "yes" und "no" als bolscher <wert akzeptiert, was bei CFMX nur beim Umwandeln einer Zeichenkette zu einem boolschen Wert der Fall ist.></wert><br></br>
     * Wenn es sich um keinen bolschen Wert handelt wird der folgende Wert eingelesen mit seiner ganzen
     * Hirarchie. <br></br>
     * EBNF:<br></br>
     * `"true" | "false" | "yes" | "no" | startElement
     * {("." identifier | "[" structElement "]" )[function] };`
     *
     * @return CFXD Element
     * @throws PageException
     */
    @Throws(PageException::class)
    private fun dynamic(): Ref? {

        // get First Element of the Variable
        val pos: Int = cfml.getPos()
        val name = identifier(false)
        if (name == null) {
            if (!cfml.forwardIfCurrent('(')) return null
            cfml.removeSpace()
            val ref: Ref? = assignOp()
            if (!cfml.forwardIfCurrent(')')) throw InterpreterException("Invalid Syntax Closing [)] not found")
            cfml.removeSpace()
            return if (limited) ref else subDynamic(ref)
        }
        cfml.removeSpace()

        // Boolean constant
        if (name.equalsIgnoreCase("TRUE")) {
            cfml.removeSpace()
            return LBoolean.TRUE
        } else if (name.equalsIgnoreCase("FALSE")) {
            cfml.removeSpace()
            return LBoolean.FALSE
        } else if (!isJson && name.equalsIgnoreCase("YES")) {
            cfml.removeSpace()
            return LBoolean.TRUE
        } else if (!isJson && name.equalsIgnoreCase("NO")) {
            cfml.removeSpace()
            return LBoolean.FALSE
        } else if (allowNullConstant && name.equalsIgnoreCase("NULL")) {
            cfml.removeSpace()
            return LString(null)
        } else if (!limited && name.equalsIgnoreCase("NEW")) {
            val res: Ref? = newOp()
            if (res != null) return res
        }
        return if (limited) startElement(name) else subDynamic(startElement(name))
    }

    @Throws(PageException::class)
    private fun subDynamic(ref: Ref?): Ref? {
        var ref: Ref? = ref
        var name: String? = null

        // Loop over nested Variables
        while (cfml.isValidIndex()) {
            // .
            if (cfml.forwardIfCurrent('.')) {
                // Extract next Var String
                cfml.removeSpace()
                name = identifier(true)
                if (name == null) throw InterpreterException("Invalid identifier")
                cfml.removeSpace()
                ref = Variable(ref, name, limited)
            } else if (cfml.forwardIfCurrent('[')) {
                cfml.removeSpace()
                ref = Variable(ref, assignOp(), limited)
                cfml.removeSpace()
                if (!cfml.forwardIfCurrent(']')) throw InterpreterException("Invalid Syntax Closing []] not found")
            } else {
                break
            }
            cfml.removeSpace()
            if (cfml.isCurrent('(')) {
                if (ref !is Set) throw InterpreterException("invalid syntax " + ref.getTypeName().toString() + " can't called as function")
                val set: Set? = ref
                ref = UDFCall(set.getParent(pc), set.getKey(pc), functionArg(name, false, null, ')'))
            }
        }
        if (ref is tachyon.runtime.interpreter.ref.`var`.Scope) {
            val s: tachyon.runtime.interpreter.ref.`var`.Scope? = ref
            if (s!!.getScope() === Scope.SCOPE_ARGUMENTS || s!!.getScope() === Scope.SCOPE_LOCAL || s!!.getScope() === ScopeSupport.SCOPE_VAR) {
                ref = Bind(s)
            }
        }
        return ref
    }

    /**
     * Extrahiert den Start Element einer Variale, dies ist entweder eine Funktion, eine Scope
     * Definition oder eine undefinierte Variable. <br></br>
     * EBNF:<br></br>
     * `identifier "(" functionArg ")" | scope | identifier;`
     *
     * @param name Einstiegsname
     * @return CFXD Element
     * @throws PageException
     */
    @Throws(PageException::class)
    private fun startElement(name: String?): Ref? {

        // check function
        if (!limited && cfml.isCurrent('(')) {
            val function: FunctionLibFunction = fld.getFunction(name)
            val arguments: Array<Ref?>? = functionArg(name, true, function, ')')
            if (function != null) return BIFCall(function, arguments)
            val ref: Ref = Scope(Scope.SCOPE_UNDEFINED)
            return UDFCall(ref, name, arguments)
        }
        // check scope
        return scope(name)
    }

    @Throws(PageException::class)
    private fun newOp(): Ref? {
        val start: Int = cfml.getPos()
        var name: String? = null
        cfml.removeSpace()

        // first identifier
        name = identifier(true)
        var refName: Ref? = null
        if (name != null) {
            val fullName = StringBuilder()
            fullName.append(name)
            // Loop over additional identifier
            while (cfml.isValidIndex()) {
                if (cfml.forwardIfCurrent('.')) {
                    cfml.removeSpace()
                    name = identifier(true)
                    if (name == null) throw InterpreterException("invalid Component declaration")
                    cfml.removeSpace()
                    fullName.append('.')
                    fullName.append(name)
                } else break
            }
            refName = LString(fullName.toString())
        } else {
            if (cfml.isCurrentQuoter()) refName = string()
            if (refName == null) {
                cfml.setPos(start)
                return null
            }
        }
        cfml.removeSpace()
        if (cfml.isCurrent('(')) {
            val function: FunctionLibFunction = fld.getFunction("_createComponent")
            val arguments: Array<Ref?>? = functionArg("_createComponent", true, function, ')')
            val args: Array<Ref?> = arrayOfNulls<Ref?>(arguments!!.size + 1)
            for (i in arguments.indices) {
                args[i] = arguments!![i]
            }
            args[args.size - 1] = refName
            val bif = BIFCall(function, args)
            cfml.removeSpace()
            return bif
        }
        throw InterpreterException("invalid Component declaration ")
    }

    /**
     * Liest einen CFML Scope aus, falls der folgende identifier keinem Scope entspricht, gibt die
     * Variable null zurueck. <br></br>
     * EBNF:<br></br>
     * `"variable" | "cgi" | "url" | "form" | "session" | "application" | "arguments" | "cookie" | " client";`
     *
     * @param idStr String identifier, wird aus Optimierungszwechen nicht innerhalb dieser Funktion
     * ausgelsen.
     * @return CFXD Variable Element oder null
     */
    private fun scope(idStr: String?): Ref? {
        if (!limited && idStr!!.equals("var")) {
            val name = identifier(false)
            if (name != null) {
                cfml.removeSpace()
                return Variable(Scope(ScopeSupport.SCOPE_VAR), name, limited)
            }
        }
        val scope: Int = if (limited) Scope.SCOPE_UNDEFINED else VariableInterpreter.scopeString2Int(pc != null && pc.ignoreScopes(), idStr)
        return if (scope == Scope.SCOPE_UNDEFINED) {
            Variable(Scope(Scope.SCOPE_UNDEFINED), idStr, limited)
        } else Scope(scope)
    }

    /**
     * Liest einen Identifier aus und gibt diesen als String zurueck. <br></br>
     * EBNF:<br></br>
     * `(letter | "_") {letter | "_"|digit};`
     *
     * @param firstCanBeNumber
     * @return Identifier.
     */
    private fun identifier(firstCanBeNumber: Boolean): String? {
        if (!cfml.isCurrentLetter() && !cfml.isCurrentSpecial()) {
            if (!firstCanBeNumber) return null else if (!cfml.isCurrentDigit()) return null
        }
        val doUpper: Boolean
        val ps: PageSource? = if (pc == null) null else pc.getCurrentPageSource()
        doUpper = if (ps != null) !isJson && ps.getDialect() === CFMLEngine.DIALECT_CFML && (ps.getMapping() as MappingImpl).getDotNotationUpperCase() else !isJson && (config as ConfigWebPro?).getDotNotationUpperCase() // MUST .tachyon should not be upper case
        val sb = StringBuilder()
        sb.append(if (doUpper) cfml.getCurrentUpper() else cfml.getCurrent())
        do {
            cfml.next()
            if (!(cfml.isCurrentLetter() || cfml.isCurrentDigit() || cfml.isCurrentSpecial())) {
                break
            }
            sb.append(if (doUpper) cfml.getCurrentUpper() else cfml.getCurrent())
        } while (cfml.isValidIndex())
        return sb.toString() // cfml.substringLower(start,cfml.getPos()-start);
    }

    /**
     * Liest die Argumente eines Funktonsaufruf ein und prueft ob die Funktion innerhalb der FLD
     * (Function Library Descriptor) definiert ist. Falls sie existiert wird die Funktion gegen diese
     * geprueft und ein build-in-function CFXD Element generiert, ansonsten ein normales funcion-call
     * Element. <br></br>
     * EBNF:<br></br>
     * `[impOp{"," impOp}];`
     *
     * @param name Identifier der Funktion als Zeichenkette
     * @param checkLibrary Soll geprueft werden ob die Funktion innerhalb der Library existiert.
     * @param flf FLD Function definition .
     * @return CFXD Element
     * @throws PageException
     */
    @Throws(PageException::class)
    private fun functionArg(name: String?, checkLibrary: Boolean, flf: FunctionLibFunction?, end: Char): Array<Ref?>? {

        // get Function Library
        var checkLibrary = checkLibrary
        checkLibrary = checkLibrary && flf != null

        // Function Attributes
        val arr: List<Ref?> = ArrayList<Ref?>()
        var arrFuncLibAtt: List<FunctionLibFunctionArg?>? = null
        var libLen = 0
        if (checkLibrary) {
            arrFuncLibAtt = flf.getArg()
            libLen = arrFuncLibAtt.size()
        }
        var count = 0
        var ref: Ref?
        do {
            cfml.next()
            cfml.removeSpace()

            // finish
            if (cfml.isCurrent(end)) break

            // too many Attributes
            var isDynamic = false
            var max = -1
            if (checkLibrary) {
                isDynamic = isDynamic(flf)
                max = flf.getArgMax()
                // Dynamic
                if (isDynamic) {
                    if (max != -1 && max <= count) throw InterpreterException("too many Attributes in function [$name]")
                } else {
                    if (libLen <= count) throw InterpreterException("too many Attributes in function [$name]")
                }
            }
            if (checkLibrary && !isDynamic) {
                // current attribues from library
                val funcLibAtt: FunctionLibFunctionArg? = arrFuncLibAtt!![count]
                val type: Short = CFTypes.toShort(funcLibAtt.getTypeAsString(), false, CFTypes.TYPE_UNKNOW)
                if (type == CFTypes.TYPE_VARIABLE_STRING) {
                    arr.add(functionArgDeclarationVarString())
                } else {
                    ref = functionArgDeclaration()
                    arr.add(Casting(funcLibAtt.getTypeAsString(), type, ref))
                }
            } else {
                arr.add(functionArgDeclaration())
            }
            cfml.removeSpace()
            count++
        } while (cfml.isCurrent(','))

        // end with ) ??
        if (!cfml.forwardIfCurrent(end)) {
            if (name.startsWith("_json")) throw InterpreterException("Invalid Syntax Closing [$end] not found")
            throw InterpreterException("Invalid Syntax Closing [$end] for function [$name] not found")
        }

        // check min attributes
        if (checkLibrary && flf.getArgMin() > count) throw InterpreterException("to less Attributes in function [$name]")
        cfml.removeSpace()
        return arr.toArray(arrayOfNulls<Ref?>(arr.size()))
    }

    private fun isDynamic(flf: FunctionLibFunction?): Boolean {
        return flf.getArgType() === FunctionLibFunction.ARG_DYNAMIC
    }

    /**
     * Sharps (#) die innerhalb von Expressions auftauchen haben in CFML keine weitere Beteutung und
     * werden durch diese Methode einfach entfernt. <br></br>
     * Beispiel:<br></br>
     * `arrayLen(#arr#)` und `arrayLen(arr)` sind identisch. EBNF:<br></br>
     * `"#" checker "#";`
     *
     * @return CFXD Element
     * @throws PageException
     */
    @Throws(PageException::class)
    private fun sharp(): Ref? {
        if (!cfml.forwardIfCurrent('#')) return null
        val ref: Ref?
        cfml.removeSpace()
        ref = assignOp()
        cfml.removeSpace()
        if (!cfml.forwardIfCurrent('#')) throw InterpreterException("Syntax Error, Invalid Construct")
        cfml.removeSpace()
        return ref
    }

    companion object {
        protected const val STATIC: Short = 0
        private const val DYNAMIC: Short = 1
        private var LITERAL_ARRAY: FunctionLibFunction? = null
        private var LITERAL_STRUCT: FunctionLibFunction? = null
        private var JSON_ARRAY: FunctionLibFunction? = null
        private var JSON_STRUCT: FunctionLibFunction? = null
        private var LITERAL_ORDERED_STRUCT: FunctionLibFunction? = null
    }

    init {
        isJson = this is JSONExpressionInterpreter
        this.limited = limited || isJson // json is always limited
    }
}