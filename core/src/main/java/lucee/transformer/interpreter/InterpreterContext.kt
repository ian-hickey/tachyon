package lucee.transformer.interpreter

import java.util.Stack

class InterpreterContext(pc: PageContext?) : Context {
    private val pc: PageContext?
    private val stack: Stack<Object?>? = Stack<Object?>()
    @Override
    fun getFactory(): Factory? {
        // TODO Auto-generated method stub
        return null
    }

    /*
	 * public void stack(boolean b) { // TODO Auto-generated method stub
	 * 
	 * }
	 * 
	 * public void stack(Double d) { // TODO Auto-generated method stub
	 * 
	 * }
	 * 
	 * public void stack(double d) { // TODO Auto-generated method stub
	 * 
	 * }
	 * 
	 * public void stack(Float f) { // TODO Auto-generated method stub
	 * 
	 * } public void stack(float f) { // TODO Auto-generated method stub
	 * 
	 * }
	 * 
	 * public void stack(Long l) { // TODO Auto-generated method stub
	 * 
	 * } public void stack(long l) { // TODO Auto-generated method stub
	 * 
	 * }
	 * 
	 * public void stack(Integer i) { // TODO Auto-generated method stub
	 * 
	 * } public void stack(int i) { // TODO Auto-generated method stub
	 * 
	 * }
	 * 
	 * public void stack(String str) { // TODO Auto-generated method stub
	 * 
	 * }
	 */
    fun stack(obj: Object?) {
        stack.add(obj)
    }

    fun getPageContext(): PageContext? {
        return pc
    }
    /**
     * removes the element from top of the stack
     *
     * @return
     *
     * public String stackPopTopAsString() { // TODO Auto-generated method stub return null; }
     * public boolean stackPopTopAsBoolean() { // TODO Auto-generated method stub return false;
     * }
     */
    /**
     * removes the element from top of the stack
     *
     * @return
     *
     * public String stackPopBottomAsString() { // TODO Auto-generated method stub return null;
     * } public boolean stackPopBottomAsBoolean() { // TODO Auto-generated method stub return
     * false; }
     */
    @Throws(PageException::class)
    fun getValueAsString(expr: Expression?): String? {
        expr.writeOut(this, Expression.MODE_REF)
        return Caster.toString(stack.pop())
    }

    @Throws(PageException::class)
    fun getValueAsBoolean(expr: Expression?): Boolean? {
        expr.writeOut(this, Expression.MODE_REF)
        return Caster.toBoolean(stack.pop())
    }

    @Throws(PageException::class)
    fun getValueAsBooleanValue(expr: Expression?): Boolean {
        expr.writeOut(this, Expression.MODE_VALUE)
        return Caster.toBooleanValue(stack.pop())
    }

    @Throws(PageException::class)
    fun getValueAsByte(expr: Expression?): Byte? {
        expr.writeOut(this, Expression.MODE_REF)
        return Caster.toByte(stack.pop())
    }

    @Throws(PageException::class)
    fun getValueAsByteValue(expr: Expression?): Byte {
        expr.writeOut(this, Expression.MODE_VALUE)
        return Caster.toByteValue(stack.pop())
    }

    @Throws(PageException::class)
    fun getValueAsInteger(expr: Expression?): Integer? {
        expr.writeOut(this, Expression.MODE_REF)
        return Caster.toInteger(stack.pop())
    }

    @Throws(PageException::class)
    fun getValueAsIntValue(expr: Expression?): Int {
        expr.writeOut(this, Expression.MODE_VALUE)
        return Caster.toIntValue(stack.pop())
    }

    @Throws(PageException::class)
    fun getValueAsFloat(expr: Expression?): Float? {
        expr.writeOut(this, Expression.MODE_REF)
        return Caster.toFloat(stack.pop())
    }

    @Throws(PageException::class)
    fun getValueAsFloatValue(expr: Expression?): Float {
        expr.writeOut(this, Expression.MODE_VALUE)
        return Caster.toFloatValue(stack.pop())
    }

    @Throws(PageException::class)
    fun getValueAsDouble(expr: Expression?): Double? {
        expr.writeOut(this, Expression.MODE_REF)
        return Caster.toDouble(stack.pop())
    }

    @Throws(PageException::class)
    fun getValueAsNumber(expr: Expression?): Number? {
        expr.writeOut(this, Expression.MODE_REF)
        return Caster.toNumber(stack.pop())
    }

    @Throws(PageException::class)
    fun getValueAsDoubleValue(expr: Expression?): Double {
        expr.writeOut(this, Expression.MODE_VALUE)
        return Caster.toDoubleValue(stack.pop())
    }

    @Throws(PageException::class)
    fun getValueAsCharacter(expr: Expression?): Character? {
        expr.writeOut(this, Expression.MODE_REF)
        return Caster.toCharacter(stack.pop())
    }

    @Throws(PageException::class)
    fun getValueAsCharValue(expr: Expression?): Char {
        expr.writeOut(this, Expression.MODE_VALUE)
        return Caster.toCharValue(stack.pop())
    }

    @Throws(PageException::class)
    fun getValueAsShort(expr: Expression?): Short? {
        expr.writeOut(this, Expression.MODE_REF)
        return Caster.toShort(stack.pop())
    }

    @Throws(PageException::class)
    fun getValueAsShortValue(expr: Expression?): Short {
        expr.writeOut(this, Expression.MODE_VALUE)
        return Caster.toShortValue(stack.pop())
    }

    @Throws(PageException::class)
    fun getValueAsLong(expr: Expression?): Long? {
        expr.writeOut(this, Expression.MODE_REF)
        return Caster.toLong(stack.pop())
    }

    @Throws(PageException::class)
    fun getValueAsLongValue(expr: Expression?): Long {
        expr.writeOut(this, Expression.MODE_VALUE)
        return Caster.toLongValue(stack.pop())
    }

    @Throws(PageException::class)
    fun getValue(expr: Expression?): Object? {
        expr.writeOut(this, Expression.MODE_REF)
        return stack.pop()
    }

    init {
        this.pc = pc
    }
}