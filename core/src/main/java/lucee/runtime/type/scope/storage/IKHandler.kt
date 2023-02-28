package lucee.runtime.type.scope.storage

import java.util.Map

interface IKHandler {
    @Throws(PageException::class)
    fun loadData(pc: PageContext?, appName: String?, name: String?, strType: String?, type: Int, log: Log?): IKStorageValue?
    fun store(storageScope: IKStorageScopeSupport?, pc: PageContext?, appName: String?, name: String?, data: Map<Collection.Key?, IKStorageScopeItem?>?, log: Log?)
    fun unstore(storageScope: IKStorageScopeSupport?, pc: PageContext?, appName: String?, name: String?, log: Log?)
    fun getType(): String?
}