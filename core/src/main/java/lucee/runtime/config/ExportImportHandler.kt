package lucee.runtime.config

import java.io.ByteArrayInputStream

object ExportImportHandler {
    const val TYPE_CONFIGURATION: Short = 1
    const val TYPE_EXTENSION: Short = 2
    const val TYPE_CORE: Short = 4
    const val TYPE_FILES: Short = 8
    const val TYPE_ALL = (TYPE_CONFIGURATION + TYPE_EXTENSION + TYPE_CORE + TYPE_FILES).toShort()
    @Throws(IOException::class, PageException::class, ConverterException::class)
    fun export(pc: PageContext?, cs: ConfigServer?, types: Short, target: String?, addOptionalArtifacts: Boolean, regularMappingFilter: String?, componentMappingFilter: String?,
               customtagMappingFilter: String?) {
        val tmp: Resource = SystemUtil.getTempDirectory()
        var dir: Resource
        // we need a new directory
        do {
            dir = tmp.getRealResource(CreateUniqueId.invoke())
        } while (dir.isDirectory())
        dir.createDirectory(true)
        val configFile: Resource = dir.getRealResource("config.json")
        try {
            val map: Map<String?, Object?> = HashMap<String?, Object?>()
            val artifacts: Map<String?, Resource?>?
            artifacts = HashMap<String?, Resource?>()
            map.put("artifacts", artifacts)

            // server
            map.put("server", _export(cs as ConfigPro?, types, dir.getRealResource("server"), "/server", addOptionalArtifacts, regularMappingFilter, componentMappingFilter,
                    customtagMappingFilter))

            // webs
            val websDir: Resource = dir.getRealResource("webs")
            val webs: List<Map<String?, Object?>?> = ArrayList<Map<String?, Object?>?>()
            map.put("webs", webs)
            var id: String
            for (cw in cs.getConfigWebs()) {
                id = cw.getIdentification().getId()
                webs.add(_export(cw as ConfigPro, types, websDir.getRealResource(id), "/webs/$id", addOptionalArtifacts, regularMappingFilter, componentMappingFilter,
                        customtagMappingFilter))
            }

            // store config
            IOUtil.copy(toIS(JSONConverter.serialize(pc, map)), configFile, true)

            // zip everything
            CompressUtil.compress(CompressUtil.FORMAT_ZIP, dir, ResourceUtil.toResourceNotExisting(pc, target), false, -1)
        } finally {
            dir.delete()
        }
    }

    private fun toIS(cs: CharSequence?): InputStream? {
        return ByteArrayInputStream(cs.toString().getBytes())
    }

    @Throws(IOException::class)
    private fun _export(config: ConfigPro?, types: Short, dir: Resource?, pathAppendix: String?, addOptionalArtifacts: Boolean, regularMappingFilter: String?,
                        componentMappingFilter: String?, customtagMappingFilter: String?): Map<String?, Object?>? {
        val map: Map<String?, Object?> = HashMap<String?, Object?>()

        // Core
        if (types and TYPE_CONFIGURATION > 0) {
        }
        // Extension
        if (types and TYPE_EXTENSION > 0) {
            val extDir: Resource = dir.getRealResource("extensions")
            extDir.mkdirs()
            val extensions: List<Object?> = ArrayList<Object?>()
            map.put("extensions", extensions)
            var m: Map<String?, String?>?
            for (ext in config!!.getRHExtensions()) {
                m = HashMap<String?, String?>()
                extensions.add(m)
                m.put("id", ext.getId())
                m.put("version", ext.getVersion())
                if (dir != null) {
                    m.put("artifact", pathAppendix.toString() + "/extensions/" + ext.getExtensionFile().getName())
                    if (addOptionalArtifacts) IOUtil.copy(ext.getExtensionFile(), extDir.getRealResource(ext.getExtensionFile().getName()))
                }
            }
        }

        // Core
        if (types and TYPE_CORE > 0 && config is ConfigServer) {
            map.put("core", CFMLEngineFactory.getInstance().getInfo().getVersion().toString())
        }
        // Files
        if (types and TYPE_FILES > 0) {
            val mapDir: Resource = dir.getRealResource("mappings")
            val mappings: HashMap<String?, Object?> = HashMap<String?, Object?>()
            map.put("mappings", mappings)
            mappings.put("regular", exportMapping(config.getMappings(), mapDir.getRealResource("regular"), pathAppendix.toString() + "/mappings/regular/", regularMappingFilter))
            mappings.put("component",
                    exportMapping(config.getComponentMappings(), mapDir.getRealResource("component"), pathAppendix.toString() + "/mappings/component/", componentMappingFilter))
            mappings.put("customtag",
                    exportMapping(config.getCustomTagMappings(), mapDir.getRealResource("customtag"), pathAppendix.toString() + "/mappings/customtag/", customtagMappingFilter))
        }
        return map
    }

    @Throws(IOException::class)
    private fun exportMapping(mappings: Array<Mapping?>?, dir: Resource?, pathAppendix: String?, filter: String?): List<Object?>? {
        val list: List<Object?> = ArrayList<Object?>()
        var m: Map<String?, Object?>?
        for (mapping in mappings!!) {
            val mi: MappingImpl? = mapping as MappingImpl?
            m = HashMap<String?, Object?>()
            list.add(m)
            m.put("virtual", mapping.getVirtual())
            m.put("inspect", ConfigWebUtil.inspectTemplate(mi.getInspectTemplateRaw(), ""))
            m.put("toplevel", mapping.isTopLevel())
            m.put("readonly", mapping.isReadonly())
            m.put("hidden", mapping.isHidden())
            m.put("physicalFirst", mapping.isPhysicalFirst())
            m.put("hidden", mapping.isHidden())

            // archive
            if (mapping.hasArchive()) {
                val archive: Resource = mapping.getArchive()
                if (archive.isFile()) {
                    val arcDir: Resource = dir.getRealResource("archive/")
                    arcDir.mkdir()
                    m.put("archive", pathAppendix.toString() + "archive/" + archive.getName())
                    IOUtil.copy(archive, arcDir.getRealResource(archive.getName()))
                }
            }

            // physical
            if (mapping.hasPhysical()) {
                val physical: Resource = mi.getPhysical()
                if (physical.isDirectory()) {
                    val id: String = CreateUniqueId.invoke()
                    val phyDir: Resource = dir.getRealResource("physical/$id")
                    phyDir.mkdirs()
                    m.put("physical", pathAppendix.toString() + "physical/" + id)
                    var f: ResourceFilter? = null
                    if (!StringUtil.isEmpty(filter)) {
                        f = OrResourceFilter(arrayOf<ResourceFilter?>(WildcardPatternFilter(filter, ","), DirectoryResourceFilter.FILTER))
                    }
                    if (!physical.getAbsolutePath().equals("/")) // PATCH this needs more digging
                        ResourceUtil.copyRecursive(physical, phyDir, f)
                }
            }
        }
        return list
    }

    @Throws(IOException::class)
    private fun toBinary(res: Resource?): ByteArray? {
        return IOUtil.toBytes(res)
    }
}