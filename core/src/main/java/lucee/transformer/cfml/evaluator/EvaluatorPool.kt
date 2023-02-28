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
package lucee.transformer.cfml.evaluator

import java.util.ArrayList

/**
 *
 * Wenn der CFML Transformer waehrend des uebersetzungsprozess auf einen Tag stoesst, prueft er
 * mithilfe der passenden TagLib, ob dieses Tag eine Evaluator definiert hat. Wenn ein Evaluator
 * definiert ist, kann der CFML Transformer diesen aber nicht sofort aufrufen, da zuerst das
 * komplette Dokument uebersetzt werden muss, bevor ein Evaluator aufgerufen werden kann. Hier kommt
 * der EvaluatorPool zum Einsatz, der CFMLTransfomer uebergibt den Evaluator den er von der TagLib
 * erhalten hat, an den EvaluatorPool weiter. Sobald der CFMLTransfomer den uebersetzungsprozess
 * abgeschlossen hat, ruft er dann den EvaluatorPool auf und dieser ruft dann alle Evaluatoren auf
 * die im uebergeben wurden.
 *
 */
class EvaluatorPool {
    var tags: List<TagData?>? = ArrayList<TagData?>()
    var functions: List<FunctionData?>? = ArrayList<FunctionData?>()

    /**
     * add a tag to the pool to evaluate at the end
     */
    fun add(libTag: TagLibTag?, tag: Tag?, flibs: Array<FunctionLib?>?, cfml: SourceCode?) {
        tags.add(TagData(libTag, tag, flibs, cfml))
    }

    fun add(flf: FunctionLibFunction?, bif: BIF?, cfml: SourceCode?) {
        functions.add(FunctionData(flf, bif, cfml))
    }

    /**
     * Die Methode run wird aufgerufen sobald, der CFML Transformer den uebersetzungsprozess
     * angeschlossen hat. Die metode run rauft darauf alle Evaluatoren auf die intern gespeicher wurden
     * und loescht den internen Speicher.
     *
     * @throws TemplateException
     */
    @Throws(TemplateException::class)
    fun run() {
        run({

            // tags
            val it = tags!!.iterator()
            while (it.hasNext()) {
                val td = it.next()
                val cfml: SourceCode? = td!!.cfml
                cfml.setPos(td.pos)
                try {
                    if (td.libTag.getEvaluator() != null) td.libTag.getEvaluator().evaluate(td.tag, td.libTag, td.flibs)
                } catch (e: EvaluatorException) {
                    clear() // print.printST(e);
                    throw TemplateException(cfml, e)
                } catch (e: Throwable) {
                    ExceptionUtil.rethrowIfNecessary(e)
                    clear()
                    throw TemplateException(cfml, e)
                }
            }
            tags.clear()
        })
        // functions
        val it = functions!!.iterator()
        while (it.hasNext()) {
            val td = it.next()
            val cfml: SourceCode? = td!!.cfml
            cfml.setPos(td.pos)
            try {
                if (td.flf.getEvaluator() != null) td.flf.getEvaluator().evaluate(td.bif, td.flf)
            } catch (e: EvaluatorException) {
                clear() // print.printST(e);
                throw TemplateException(cfml, e)
            } catch (e: Throwable) {
                ExceptionUtil.rethrowIfNecessary(e)
                clear()
                throw TemplateException(cfml, e)
            }
        }
        functions.clear()
    }

    /**
     * internal class to store all tag related data
     */
    class TagData(libTag: TagLibTag?, tag: Tag?, flibs: Array<FunctionLib?>?, cfml: SourceCode?) {
        val libTag: TagLibTag?
        val tag: Tag?
        val flibs: Array<FunctionLib?>?
        val cfml: SourceCode?
        val pos: Int

        init {
            this.libTag = libTag
            this.tag = tag
            this.flibs = flibs
            this.cfml = cfml
            pos = cfml.getPos()
        }
    }

    class FunctionData(flf: FunctionLibFunction?, bif: BIF?, cfml: SourceCode?) {
        val flf: FunctionLibFunction?
        val bif: BIF?
        val cfml: SourceCode?
        val pos: Int

        init {
            this.flf = flf
            this.bif = bif
            this.cfml = cfml
            pos = cfml.getPos()
        }
    }

    /**
     * clears the ppol
     */
    fun clear() {
        tags.clear()
        functions.clear()
    } /*
	 * public static void getPool() { // TODO Auto-generated method stub
	 * 
	 * }
	 */
}