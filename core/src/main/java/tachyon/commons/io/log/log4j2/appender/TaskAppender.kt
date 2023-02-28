package tachyon.commons.io.log.log4j2.appender

import java.io.Serializable

class TaskAppender(config: Config, appender: Appender) : Appender {
    private val appender: Appender
    private val spoolerEngine: SpoolerEngine
    @Override
    fun append(event: LogEvent?) {
        spoolerEngine.add(Task(appender, event))
    }

    @get:Override
    @set:Override
    var handler: ErrorHandler
        get() = appender.getHandler()
        set(handler) {
            appender.setHandler(handler)
        }

    @get:Override
    val name: String
        get() = appender.getName()

    @get:Override
    val state: State
        get() = appender.getState()

    @Override
    fun initialize() {
        appender.initialize()
    }

    @Override
    fun start() {
        appender.start()
    }

    @Override
    fun stop() {
        appender.stop()
    }

    @get:Override
    val isStarted: Boolean
        get() = appender.isStarted()

    @get:Override
    val isStopped: Boolean
        get() = appender.isStopped()

    @get:Override
    val layout: Layout<out Serializable?>
        get() = appender.getLayout()

    @Override
    fun ignoreExceptions(): Boolean {
        return appender.ignoreExceptions()
    }

    init {
        this.appender = appender
        spoolerEngine = config.getSpoolerEngine()
    }
}