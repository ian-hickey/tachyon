package lucee.runtime.future

import java.util.concurrent.ExecutorService

class Future(future: java.util.concurrent.Future<Object?>?, timeout: Long) : Objects {
    private val future: java.util.concurrent.Future<Object?>?
    private var hasError = false
    private var error: String? = null
    private var hasCustomErrorHandler = false
    private var exception: Exception? = null
    private val timeout: Long
    private val names: Array<String?>? = arrayOf("cancel", "isCancelled", "isDone", "error", "get", "then")
    private val rtns: Array<String?>? = arrayOf("boolean", "boolean", "boolean", "Future", "Future", "Future")
    private val args: Array<Array<Array<String?>?>?>? = arrayOf(arrayOf(), arrayOf(), arrayOf(), arrayOf(), arrayOf(arrayOf("closure", "yes", "function"), arrayOf("timezone", "no", "timespan")), arrayOf(arrayOf("closure", "yes", "function"), arrayOf("timezone", "no", "timespan")))
    @Throws(PageException::class)
    fun then(pc: PageContext?, udf: UDF?, timeout: Long): Future? {
        return if (hasError) this else try {
            val arg: Object = get(pc, -1)
            val executor: ExecutorService = Executors.newSingleThreadExecutor()
            Future(executor.submit(CallableUDF(pc, udf, arg)), timeout)
        } catch (e: Exception) {
            handleExecutionError(pc, e)
        }
    }

    @Throws(PageException::class)
    operator fun get(pc: PageContext?, timeout: Long): Object? {
        var timeout = timeout
        if (timeout < 0) timeout = this.timeout
        return try {
            if (timeout > 0) future.get(timeout, TimeUnit.MILLISECONDS) else future.get()
        } catch (e: Exception) {
            setHasError(true)
            ThreadLocalPageContext.getLog(pc, "application").error("Async", e)
            throw Caster.toPageException(e)
        }
    }

    fun error(pc: PageContext?, udf: UDF?, timeout: Long): Future? {
        setHasCustomErrorHandler(true)
        return if (hasError) {
            executeErrorHandler(pc, udf, timeout, exception)
        } else try {
            future.get()
            this
        } catch (e: Exception) {
            executeErrorHandler(pc, udf, timeout, e)
        }
    }

    @Throws(PageException::class)
    private fun handleExecutionError(pc: PageContext?, e: Exception?): Future? {
        setHasError(true)
        exception = e
        if (!hasCustomErrorHandler) {
            ThreadLocalPageContext.getLog(pc, "application").error("Async", e)
            throw Caster.toPageException(e)
        }
        return this
    }

    private fun executeErrorHandler(pc: PageContext?, udf: UDF?, timeout: Long, e: Exception?): Future? {
        val executor: ExecutorService = Executors.newSingleThreadExecutor()
        return Future(executor.submit(CallableUDF(pc, udf, CatchBlockImpl(Caster.toPageException(e)))), timeout)
    }

    fun cancel(): Boolean {
        return future.cancel(true)
    }

    fun isCancelled(): Boolean {
        return future.isCancelled()
    }

    fun isDone(): Boolean {
        return future.isDone()
    }

    fun hasError(): Boolean {
        return hasError
    }

    fun setHasError(hasError: Boolean) {
        this.hasError = hasError
    }

    fun getError(): String? {
        return error
    }

    fun setError(error: String?) {
        this.error = error
    }

    fun hasCustomErrorHandler(): Boolean {
        return hasCustomErrorHandler
    }

    fun setHasCustomErrorHandler(hasCustomErrorHandler: Boolean) {
        this.hasCustomErrorHandler = hasCustomErrorHandler
    }

    fun getException(): Exception? {
        return exception
    }

    fun setException(exception: Exception?) {
        this.exception = exception
    }

    @Override
    @Throws(PageException::class)
    fun call(pc: PageContext?, funcName: Key?, args: Array<Object?>?): Object? {
        if ("cancel".equalsIgnoreCase(funcName.getString())) {
            return if (args!!.size == 0) cancel() else throw FunctionException(pc, "cancel", 0, 0, args.size)
        }
        if ("get".equalsIgnoreCase(funcName.getString())) {
            return if (args!!.size == 1) get(pc, toTimeout(args[0])) else if (args.size == 0) get(pc, -1) else throw FunctionException(pc, "get", 0, 1, args.size)
        }
        if ("isCancelled".equalsIgnoreCase(funcName.getString())) {
            return if (args!!.size == 0) isCancelled() else throw FunctionException(pc, "isCancelled", 0, 0, args.size)
        }
        if ("isDone".equalsIgnoreCase(funcName.getString())) {
            return if (args!!.size == 0) isDone() else throw FunctionException(pc, "isDone", 0, 0, args.size)
        }
        if ("error".equalsIgnoreCase(funcName.getString())) {
            return if (args!!.size == 2) error(pc, Caster.toFunction(args[0]), toTimeout(args[1])) else if (args.size == 1) error(pc, Caster.toFunction(args[0]), 0) else throw FunctionException(pc, "error", 1, 2, args.size)
        }
        if ("then".equalsIgnoreCase(funcName.getString())) {
            return if (args!!.size == 2) then(pc, Caster.toFunction(args[0]), toTimeout(args[1])) else if (args.size == 1) then(pc, Caster.toFunction(args[0]), 0) else throw FunctionException(pc, "then", 1, 2, args.size)
        }
        throw ApplicationException("invalid function name [" + funcName + "], valid names are [" + ListUtil.arrayToList(names, ", ") + "]")
    }

    @Override
    @Throws(PageException::class)
    fun callWithNamedValues(pc: PageContext?, funcName: Key?, args: Struct?): Object? {
        if ("cancel".equalsIgnoreCase(funcName.getString())) {
            return if (args.size() === 0) cancel() else throw FunctionException(pc, "cancel", 0, 0, args.size())
        }
        if ("get".equalsIgnoreCase(funcName.getString())) {
            return if (args.size() === 0) this.get(pc, extractTimeout(args)) else if (args.size() === 0) this.get(pc, -1) else throw FunctionException(pc, "get", 0, 0, args.size())
        }
        if ("isCancelled".equalsIgnoreCase(funcName.getString())) {
            return if (args.size() === 0) isCancelled() else throw FunctionException(pc, "isCancelled", 0, 0, args.size())
        }
        if ("isDone".equalsIgnoreCase(funcName.getString())) {
            return if (args.size() === 0) isDone() else throw FunctionException(pc, "isDone", 0, 0, args.size())
        }
        if ("error".equalsIgnoreCase(funcName.getString())) {
            return error(pc, extractUDF(args), extractTimeout(args))
        }
        if ("then".equalsIgnoreCase(funcName.getString())) {
            return then(pc, extractUDF(args), extractTimeout(args))
        }
        throw ApplicationException("invalid function name [" + funcName + "], valid names are [" + ListUtil.arrayToList(names, ", ") + "]")
    }

    @Throws(PageException::class)
    private fun extractUDF(args: Struct?): UDF? {
        var udf: Object = args.get(KeyConstants._closure, null)
        if (udf == null) udf = args.get(KeyConstants._callback, null)
        if (udf == null) udf = args.get(KeyConstants._function, null)
        if (udf == null) udf = args.get(KeyConstants._udf, null)
        if (udf == null) throw ApplicationException("argument [closure] is required but was not passed in")
        return Caster.toFunction(udf)
    }

    @Throws(CasterException::class)
    private fun extractTimeout(args: Struct?): Long {
        var obj: Object = args.get(KeyConstants._timeout, null)
        if (obj == null) obj = args.get(KeyConstants._timespan, null)
        return if (obj == null) 0 else toTimeout(obj)
    }

    @Throws(CasterException::class)
    private fun toTimeout(obj: Object?): Long {
        if (obj == null) return 0
        val ts: TimeSpan = Caster.toTimespan(obj, null)
        if (ts != null) return ts.getMillis()
        val l: Long = Caster.toLong(obj, null)
        if (l != null) return l.longValue()
        throw CasterException(obj, "timespan")
    }

    @Override
    fun toDumpData(pc: PageContext?, arg1: Int, arg2: DumpProperties?): DumpData? {
        val table = DumpTable("component", "#77694f", "#c2baad", "#0099ff")
        table.setTitle("Future")
        var td: DumpTable?
        var _arg: Array<Array<String?>?>?
        for (i in names.indices) {
            td = DumpTable("component", "#77694f", "#c2baad", "#0099ff")
            td.setTitle("Function " + names!![i])

            // arguments
            val arg = DumpTable("component", "#77694f", "#c2baad", "#0099ff")
            _arg = args!![i]
            arg.appendRow(255, SimpleDumpData("name"), SimpleDumpData("required"), SimpleDumpData("type"))
            for (a in _arg!!) {
                arg.appendRow(0, SimpleDumpData(a!![0]), SimpleDumpData(a!![1]), SimpleDumpData(a!![2]))
            }
            td.appendRow(1, SimpleDumpData("arguments"), arg)

            // label name required type default hint

            // return
            td.appendRow(1, SimpleDumpData("return type"), SimpleDumpData(rtns!![i]))
            table.appendRow(1, SimpleDumpData(names[i]), td)
        }
        return table
    }

    @Override
    @Throws(PageException::class)
    operator fun get(pc: PageContext?, arg1: Key?): Object? {
        throw notSupported()
    }

    @Override
    operator fun get(pc: PageContext?, arg1: Key?, arg2: Object?): Object? {
        throw notSupported()
    }

    @Override
    @Throws(PageException::class)
    operator fun set(pc: PageContext?, arg1: Key?, arg2: Object?): Object? {
        throw notSupported()
    }

    @Override
    fun setEL(pc: PageContext?, arg1: Key?, arg2: Object?): Object? {
        throw notSupported()
    }

    private fun notSupported(): PageRuntimeException? {
        return PageRuntimeException(ApplicationException("this object only support calling functions."))
    }

    @Override
    @Throws(ExpressionException::class)
    fun castToString(): String? {
        throw ExpressionException("Can't cast Complex Object Type Query to String", "Use Built-In-Function \"serialize(Query):String\" to create a String from Query")
    }

    @Override
    fun castToString(defaultValue: String?): String? {
        return defaultValue
    }

    @Override
    @Throws(ExpressionException::class)
    fun castToBooleanValue(): Boolean {
        throw ExpressionException("Can't cast Complex Object Type Query to a boolean value")
    }

    @Override
    fun castToBoolean(defaultValue: Boolean?): Boolean? {
        return defaultValue
    }

    @Override
    @Throws(ExpressionException::class)
    fun castToDoubleValue(): Double {
        throw ExpressionException("Can't cast Complex Object Type Query to a number value")
    }

    @Override
    fun castToDoubleValue(defaultValue: Double): Double {
        return defaultValue
    }

    @Override
    @Throws(ExpressionException::class)
    fun castToDateTime(): DateTime? {
        throw ExpressionException("Can't cast Complex Object Type Query to a Date")
    }

    @Override
    fun castToDateTime(defaultValue: DateTime?): DateTime? {
        return defaultValue
    }

    @Override
    @Throws(ExpressionException::class)
    operator fun compareTo(b: Boolean): Int {
        throw ExpressionException("can't compare Complex Object Type Query with a boolean value")
    }

    @Override
    @Throws(PageException::class)
    operator fun compareTo(dt: DateTime?): Int {
        throw ExpressionException("can't compare Complex Object Type Query with a DateTime Object")
    }

    @Override
    @Throws(PageException::class)
    operator fun compareTo(d: Double): Int {
        throw ExpressionException("can't compare Complex Object Type Query with a numeric value")
    }

    @Override
    @Throws(PageException::class)
    operator fun compareTo(str: String?): Int {
        throw ExpressionException("can't compare Complex Object Type Query with a String")
    }

    companion object {
        private const val serialVersionUID = -769725314696461494L
        var ARG_NULL: Object? = Object()
        @Throws(PageException::class)
        fun _then(pc: PageContext?, udf: UDF?, timeout: Long): Future? {
            val executor: ExecutorService = Executors.newSingleThreadExecutor()
            return Future(executor.submit(CallableUDF(pc, udf, ARG_NULL)), timeout)
        }
    }

    init {
        this.future = future
        this.timeout = timeout
    }
}