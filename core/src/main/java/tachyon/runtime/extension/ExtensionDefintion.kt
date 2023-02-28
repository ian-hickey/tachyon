package tachyon.runtime.extension

import java.io.IOException

class ExtensionDefintion {
    private var id: String? = null
    private val params: Map<String?, String?>? = HashMap<String?, String?>()
    private var source: Resource? = null
    private var config: Config? = null
    private var rhe: RHExtension? = null

    constructor() {}
    constructor(id: String?) {
        this.id = id
    }

    constructor(id: String?, version: String?) {
        this.id = id
        setParam("version", version)
    }

    /*
	 * public static ExtensionDefintion getInstanceEL(Config config, Element el) { try { return
	 * getInstance(config, el); } catch (Exception e) { return null; } }
	 * 
	 * 
	 * public static ExtensionDefintion getInstance(Config config, Element el) throws PageException,
	 * IOException, BundleException { String id=el.getAttribute("id"); String
	 * version=el.getAttribute("version"); if(!StringUtil.isEmpty(id) && !StringUtil.isEmpty(version)) {
	 * Resource res = RHExtension.toResource(config, el); ExtensionDefintion ed = new
	 * ExtensionDefintion(id, version); ed.setSource(config, res); return ed; }
	 * 
	 * RHExtension rhe=new RHExtension(config,el); id=rhe.getId(); version=rhe.getVersion();
	 * 
	 * ExtensionDefintion ed=new ExtensionDefintion(id,version); ed.setSource(rhe); return ed; }
	 */
    fun setId(id: String?) {
        this.id = id
    }

    fun getId(): String? {
        return id
    }

    fun getSymbolicName(): String? {
        val sn = params!!["symbolic-name"]
        return if (StringUtil.isEmpty(sn, true)) getId() else sn.trim()
    }

    fun setParam(name: String?, value: String?) {
        params.put(name, value)
    }

    fun getParams(): Map<String?, String?>? {
        return params
    }

    fun getVersion(): String? {
        var version = params!!["version"]
        if (StringUtil.isEmpty(version)) version = params["extension-version"]
        return if (StringUtil.isEmpty(version)) null else version
    }

    fun getSince(): Version? {
        val since = params!!["since"]
        return if (StringUtil.isEmpty(since)) null else OSGiUtil.toVersion(since, null)
    }

    @Override
    override fun toString(): String {
        val sb = StringBuilder()
        sb.append(getId())
        val it: Iterator<Entry<String?, String?>?> = params.entrySet().iterator()
        var e: Entry<String?, String?>?
        while (it.hasNext()) {
            e = it.next()
            sb.append(';').append(e.getKey()).append('=').append(e.getValue())
        }
        return sb.toString()
    }

    @Override
    override fun equals(other: Object?): Boolean {
        if (other is ExtensionDefintion) {
            val ed = other as ExtensionDefintion?
            if (!ed!!.getId().equalsIgnoreCase(getId())) return false
            return if (ed.getVersion() == null || getVersion() == null) true else ed.getVersion().equalsIgnoreCase(getVersion())
        } else if (other is RHExtension) {
            val ed: RHExtension? = other
            if (!ed!!.getId().equalsIgnoreCase(getId())) return false
            return if (ed!!.getVersion() == null || getVersion() == null) true else ed!!.getVersion().equalsIgnoreCase(getVersion())
        }
        return false
    }

    fun setSource(rhe: RHExtension?) {
        this.rhe = rhe
    }

    fun setSource(config: Config?, source: Resource?) {
        this.config = config
        this.source = source
    }

    @Throws(PageException::class, IOException::class, BundleException::class, ConverterException::class)
    fun toRHExtension(): RHExtension? {
        if (rhe != null) return rhe
        if (source == null) {
            // MUST try to load the Extension
            throw ApplicationException("ExtensionDefinition does not contain the necessary data to create the requested object.")
        }
        rhe = RHExtension(config, source, false)
        return rhe
    }

    @Throws(ApplicationException::class)
    fun getSource(): Resource? {
        if (source != null) return source
        if (rhe != null) return rhe.getExtensionFile()
        throw ApplicationException("ExtensionDefinition does not contain a source.")
    }
}