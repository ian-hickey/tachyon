package tachyon.commons.cpu

import java.util.List

interface Listener {
    fun listen(list: List<StaticData?>?)
}