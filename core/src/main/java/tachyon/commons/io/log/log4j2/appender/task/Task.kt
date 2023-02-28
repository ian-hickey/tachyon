package tachyon.commons.io.log.log4j2.appender.task

import org.apache.logging.log4j.core.Appender

class Task(appender: Appender, le: LogEvent) : SpoolerTaskPro {
    @get:Override
    @set:Override
    var id: String? = null
    private var lastExecution: Long = 0
    private var nextExecution: Long = 0
    private var tries = 0
    private val exceptions: Array

    @get:Override
    val creation: Long = System.currentTimeMillis()
    private var closed = false
    private val detail: Struct
    private val appender: Appender
    private val le: LogEvent
    @Override
    @Throws(PageException::class)
    fun execute(config: Config?): Object? {
        lastExecution = System.currentTimeMillis()
        tries++
        return try {
            appender.append(le)
            null
        } catch (t: Throwable) {
            ExceptionUtil.rethrowIfNecessary(t)
            val engine: CFMLEngine = CFMLEngineFactory.getInstance()
            val caster: Cast = engine.getCastUtil()
            val creator: Creation = engine.getCreationUtil()
            val pe: PageException = caster.toPageException(t)
            val exception: Struct = creator.createStruct()
            exception.put("message", pe.getMessage())
            exception.put("detail", pe.getDetail())
            exception.put("type", pe.getTypeAsString())
            exception.put("stacktrace", pe.getStackTraceAsString())
            exception.put("class", pe.getClass().getName())
            exception.put("time", caster.toLong(System.currentTimeMillis()))
            exceptions.appendEL(exception)
            throw pe
        } finally {
            lastExecution = System.currentTimeMillis()
        }
    }

    @Override
    fun detail(): Struct {
        return detail
    }

    @Override
    fun subject(): String {
        return appender.getName()
    }

    @get:Override
    val type: String
        get() = "log"

    @Override
    fun getExceptions(): Array {
        return exceptions
    }

    @Override
    fun setClosed(closed: Boolean) {
        this.closed = closed
    }

    @Override
    fun closed(): Boolean {
        return closed
    }

    @get:Override
    val plans: Array<Any>?
        get() = null

    @Override
    fun tries(): Int {
        return tries
    }

    @Override
    fun setLastExecution(lastExecution: Long) {
        this.lastExecution = lastExecution
    }

    @Override
    fun lastExecution(): Long {
        return lastExecution
    }

    @Override
    fun setNextExecution(nextExecution: Long) {
        this.nextExecution = nextExecution
    }

    @Override
    fun nextExecution(): Long {
        return nextExecution
    }

    // not supported
    @get:Override
    val listener: SpoolerTaskListener?
        get() = null // not supported

    companion object {
        private const val serialVersionUID = 5649820047520607442L
    }

    init {
        this.appender = appender
        this.le = le
        val engine: CFMLEngine = CFMLEngineFactory.getInstance()
        exceptions = engine.getCreationUtil().createArray()
        detail = engine.getCreationUtil().createStruct()
    }
}