package lucee.commons.cpu

import java.util.Iterator

class ConsoleListener(private val showStacktrace: Boolean) : Listener {
    @Override
    fun listen(staticData: List<StaticData>) {
        val it: Iterator<StaticData> = staticData.iterator()
        var data: StaticData
        aprint.e("----------------------------------------")
        while (it.hasNext()) {
            data = it.next()
            aprint.e("-----")
            aprint.e("name: " + data.name)
            // print.e("time: " + data.time);
            aprint.e("percentage: " + data.getPercentage())
            if (showStacktrace) aprint.e(data.getStacktrace())
            // print.e("total: " + data.total);
        }
    }
}