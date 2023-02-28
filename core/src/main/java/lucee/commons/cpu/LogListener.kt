package lucee.commons.cpu

import java.util.Iterator

class LogListener : Listener {
    private var log: Log
    private var index: Long = 0

    constructor(config: Config?, logName: String?) {
        log = ThreadLocalPageContext.getLog(config, logName)
    }

    constructor(log: Log) {
        this.log = log
    }

    @Override
    fun listen(staticData: List<StaticData>) {
        val it: Iterator<StaticData> = staticData.iterator()
        var data: StaticData
        index++
        if (index < 0) index = 1
        var sb: StringBuilder
        while (it.hasNext()) {
            data = it.next()
            sb = StringBuilder()
            sb.append("{'id':").append(index)
            sb.append(",'name':").append(StringUtil.escapeJS(data.name, '"'))
            sb.append(",'percentage':").append(data.getPercentage())
            sb.append(",'stacktrace':").append(data.getStacktrace())
            sb.append("}")
            log.info("cpu", sb.toString())
        }
    }
}