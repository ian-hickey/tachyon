/**
 * Copyright (c) 2014, the Railo Company Ltd.
 * Copyright (c) 2015, Lucee Assosication Switzerland
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library.  If not, see <http:></http:>//www.gnu.org/licenses/>.
 *
 */
package lucee.runtime.extension

import lucee.commons.lang.StringUtil

class ExtensionImpl : Extension {
    private var provider: String?
    private var id: String?
    private var strConfig: String? = null
    private var config: Struct? = null
    private var version: String?
    private var name: String?
    private var label: String?
    private var description: String?
    private var category: String?
    private var image: String?
    private var author: String?
    private var codename: String?
    private var video: String?
    private var support: String?
    private var documentation: String?
    private var forum: String?
    private var mailinglist: String?
    private var network: String?
    private var created: DateTime?
    private var type: String? = null

    constructor(strConfig: String?, id: String?, provider: String?, version: String?, name: String?, label: String?, description: String?, category: String?, image: String?, author: String?,
                codename: String?, video: String?, support: String?, documentation: String?, forum: String?, mailinglist: String?, network: String?, created: DateTime?, type: String?) {
        this.strConfig = strConfig
        this.id = id
        this.provider = provider
        this.version = version
        this.name = name
        this.label = label
        this.description = description
        this.category = category
        this.image = image
        this.author = author
        this.codename = codename
        this.video = video
        this.support = support
        this.documentation = documentation
        this.forum = forum
        this.mailinglist = mailinglist
        this.network = network
        this.created = created
        if ("server".equalsIgnoreCase(type)) this.type = "server" else if ("all".equalsIgnoreCase(type)) this.type = "all" else this.type = "web"
    }

    constructor(config: Struct?, id: String?, provider: String?, version: String?, name: String?, label: String?, description: String?, category: String?, image: String?, author: String?,
                codename: String?, video: String?, support: String?, documentation: String?, forum: String?, mailinglist: String?, network: String?, created: DateTime?, type: String?) {
        if (config == null) this.config = StructImpl() else this.config = config
        this.id = id
        this.provider = provider
        this.version = version
        this.name = name
        this.label = label
        this.description = description
        this.category = category
        this.image = image
        this.author = author
        this.codename = codename
        this.video = video
        this.support = support
        this.documentation = documentation
        this.forum = forum
        this.mailinglist = mailinglist
        this.network = network
        this.created = created
        if ("server".equalsIgnoreCase(type)) this.type = "server" else if ("all".equalsIgnoreCase(type)) this.type = "all" else this.type = "web"
    }

    @Override
    fun getAuthor(): String? {
        return author
    }

    @Override
    fun getCodename(): String? {
        return codename
    }

    @Override
    fun getVideo(): String? {
        return video
    }

    @Override
    fun getSupport(): String? {
        return support
    }

    @Override
    fun getDocumentation(): String? {
        return documentation
    }

    @Override
    fun getForum(): String? {
        return forum
    }

    @Override
    fun getMailinglist(): String? {
        return mailinglist
    }

    @Override
    fun getNetwork(): String? {
        return network
    }

    @Override
    fun getCreated(): DateTime? {
        return created
    }

    @Override
    fun getName(): String? {
        return name
    }

    @Override
    fun getLabel(): String? {
        return label
    }

    @Override
    fun getDescription(): String? {
        return description
    }

    @Override
    fun getCategory(): String? {
        return category
    }

    @Override
    fun getImage(): String? {
        return image
    }

    @Override
    fun getVersion(): String? {
        return version
    }

    @Override
    fun getProvider(): String? {
        return provider
    }

    @Override
    fun getId(): String? {
        return id
    }

    @Override
    fun getConfig(pc: PageContext?): Struct? {
        if (config == null) {
            if (StringUtil.isEmpty(strConfig)) config = StructImpl() else {
                config = try {
                    pc.evaluate(strConfig) as Struct
                } catch (e: PageException) {
                    return StructImpl()
                }
            }
        }
        return config
    }

    @Override
    fun getStrConfig(): String? {
        if (config != null && strConfig == null) {
            strConfig = try {
                ScriptConverter().serialize(config)
            } catch (e: ConverterException) {
                return ""
            }
        }
        return strConfig
    }

    @Override
    fun getType(): String? {
        return type
    }
}