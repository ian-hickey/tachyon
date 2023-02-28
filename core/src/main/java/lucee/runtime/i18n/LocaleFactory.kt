/**
 *
 * Copyright (c) 2014, the Railo Company Ltd. All rights reserved.
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
package lucee.runtime.i18n

import java.util.Arrays

/**
 * Factory to create Locales by CFML rules
 */
object LocaleFactory {
    // private static Pattern localePattern =
    // Pattern.compile("^\\s*([^\\s\\(]+)\\s*(\\(\\s*([^\\s\\)]+)\\s*\\))?\\s*$");
    private val localePattern: Pattern? = Pattern.compile("^\\s*([^\\(]+)\\s*(\\(\\s*([^\\)]+)\\s*\\))?\\s*$")
    private val localePattern2: Pattern? = Pattern.compile("^([a-z]{2})_([a-z]{2,3})$")
    private val localePattern3: Pattern? = Pattern.compile("^([a-z]{2})_([a-z]{2,3})_([a-z]{2,})$")
    private val locales: Map<String?, Locale?>? = LinkedHashMap<String?, Locale?>()
    private val localeAlias: Map<String?, Locale?>? = LinkedHashMap<String?, Locale?>()
    var localeList: String? = null
        private set

    private fun setLocalAlias(name: String?, locale: Locale?) {
        if (!localeAlias!!.containsKey(name)) localeAlias.put(name, locale)
    }

    /**
     * @param strLocale
     * @param defaultValue
     * @return return locale match to String
     */
    fun getLocale(strLocale: String?, defaultValue: Locale?): Locale? {
        return try {
            getLocale(strLocale)
        } catch (e: ExpressionException) {
            defaultValue
        }
    }

    /**
     * @param strLocale
     * @return return locale match to String
     * @throws ExpressionException
     */
    @Throws(ExpressionException::class)
    fun getLocale(strLocale: String?): Locale? {
        val strLocaleLC: String = strLocale.toLowerCase().trim()
        var l: Locale? = locales!![strLocaleLC]
        if (l != null) return l
        l = localeAlias!![strLocaleLC]
        if (l != null) return l
        var matcher: Matcher = localePattern2.matcher(strLocaleLC)
        if (matcher.find()) {
            val len: Int = matcher.groupCount()
            if (len == 2) {
                val lang: String = matcher.group(1).trim()
                val country: String = matcher.group(2).trim()
                val locale = Locale(lang, country)
                try {
                    locale.getISO3Language()
                    setLocalAlias(strLocaleLC, locale)
                    return locale
                } catch (e: Exception) {
                }
            }
        }
        matcher = localePattern3.matcher(strLocaleLC)
        if (matcher.find()) {
            val len: Int = matcher.groupCount()
            if (len == 3) {
                val lang: String = matcher.group(1).trim()
                val country: String = matcher.group(2).trim()
                val variant: String = matcher.group(3).trim()
                val locale = Locale(lang, country, variant)
                try {
                    locale.getISO3Language()
                    setLocalAlias(strLocaleLC, locale)
                    return locale
                } catch (e: Exception) {
                }
            }
        }
        matcher = localePattern.matcher(strLocaleLC)
        if (matcher.find()) {
            val len: Int = matcher.groupCount()
            if (len == 3) {
                val lang: String = matcher.group(1).trim()
                var country: String? = matcher.group(3)
                if (country != null) country = country.trim()
                var objLocale: Object? = null
                objLocale = if (country != null) locales[lang.toLowerCase().toString() + " (" + country.toLowerCase() + ")"] else locales[lang.toLowerCase()]
                if (objLocale != null) return objLocale as Locale?
                val locale: Locale?
                if (country != null) locale = Locale(lang.toUpperCase(), country.toLowerCase()) else locale = Locale(lang)
                try {
                    locale.getISO3Language()
                } catch (e: Exception) {
                    if (strLocale.indexOf('-') !== -1) return getLocale(strLocale.replace('-', '_'))
                    throw ExpressionException("unsupported Locale [$strLocale]", "supported Locales are:" + supportedLocalesAsString)
                }
                setLocalAlias(strLocaleLC, locale)
                return locale
            }
        }
        throw ExpressionException("can't cast value ($strLocale) to a Locale", "supported Locales are:" + supportedLocalesAsString)
    }

    // TODO chnge from ArryObject to string
    private val supportedLocalesAsString: String?
        private get() {
            // TODO chnge from ArryObject to string
            val arr: Array<String?> = locales.keySet().toArray(arrayOfNulls<String?>(locales!!.size()))
            Arrays.sort(arr)
            return ListUtil.arrayToList(arr, ",")
        }

    /**
     * @param locale
     * @return cast a Locale to a String
     */
    fun getDisplayName(locale: Locale?): String? {
        val lang: String = locale.getLanguage()
        val country: String = locale.getCountry()
        synchronized(localeAlias) {
            val it: Iterator<Entry<String?, Locale?>?> = localeAlias.entrySet().iterator()
            var entry: Map.Entry<String?, Locale?>?
            while (it.hasNext()) {
                entry = it.next()
                // Object qkey=it.next();
                val curr: Locale = entry.getValue()
                if (lang.equals(curr.getLanguage()) && country.equals(curr.getCountry())) {
                    return entry.getKey().toString()
                }
            }
        }
        return locale.getDisplayName(Locale.ENGLISH)
    }

    fun toString(locale: Locale?): String? {
        return if (locale == null) "" else locale.toString()
        // return getDisplayName(locale);
    }

    /**
     * @return Returns the locales.
     */
    fun getLocales(): Map<String?, Locale?>? {
        return locales
    }

    init {
        val ls: Array<Locale?> = Locale.getAvailableLocales()
        var key: String
        val sb = StringBuilder()
        for (i in lucee.runtime.i18n.ls.indices) {
            lucee.runtime.i18n.key = lucee.runtime.i18n.ls.get(i).getDisplayName(Locale.US).toLowerCase()
            locales.put(lucee.runtime.i18n.key, lucee.runtime.i18n.ls.get(i))
            if (lucee.runtime.i18n.key.indexOf(',') !== -1) {
                lucee.runtime.i18n.key = lucee.runtime.i18n.ls.get(i).toString()
                // print.ln(key);
            }
            if (i > 0) lucee.runtime.i18n.sb.append(",")
            lucee.runtime.i18n.sb.append(lucee.runtime.i18n.key)
        }
        localeList = lucee.runtime.i18n.sb.toString()
        setLocalAlias("albanian (albania)", LocaleConstant.ALBANIAN_ALBANIA)
        setLocalAlias("arabic (algeria)", LocaleConstant.ARABIC_ALGERIA)
        setLocalAlias("arabic (bahrain)", LocaleConstant.ARABIC_BAHRAIN)
        setLocalAlias("arabic (egypt)", LocaleConstant.ARABIC_EGYPT)
        setLocalAlias("arabic (iraq)", LocaleConstant.ARABIC_IRAQ)
        setLocalAlias("arabic (jordan)", LocaleConstant.ARABIC_JORDAN)
        setLocalAlias("arabic (kuwait)", LocaleConstant.ARABIC_KUWAIT)
        setLocalAlias("arabic (lebanon)", LocaleConstant.ARABIC_LEBANON)
        setLocalAlias("arabic (libya)", LocaleConstant.ARABIC_LIBYA)
        setLocalAlias("arabic (morocco)", LocaleConstant.ARABIC_MAROCCO)
        setLocalAlias("arabic (oman)", LocaleConstant.ARABIC_OMAN)
        setLocalAlias("arabic (qatar)", LocaleConstant.ARABIC_QATAR)
        setLocalAlias("arabic (saudi arabia)", LocaleConstant.ARABIC_SAUDI_ARABIA)
        setLocalAlias("arabic (sudan)", LocaleConstant.ARABIC_SUDAN)
        setLocalAlias("arabic (syria)", LocaleConstant.ARABIC_SYRIA)
        setLocalAlias("arabic (tunisia)", LocaleConstant.ARABIC_TUNISIA)
        setLocalAlias("arabic (united arab emirates)", LocaleConstant.ARABIC_UNITED_ARAB_EMIRATES)
        setLocalAlias("arabic (yemen)", LocaleConstant.ARABIC_YEMEN)
        setLocalAlias("chinese (china)", Locale.CHINA)
        setLocalAlias("chinese (hong kong)", LocaleConstant.CHINESE_HONG_KONG)
        setLocalAlias("chinese (singapore)", LocaleConstant.CHINESE_SINGAPORE)
        setLocalAlias("chinese (taiwan)", LocaleConstant.CHINESE_TAIWAN)
        setLocalAlias("dutch (belgian)", LocaleConstant.DUTCH_BELGIUM)
        setLocalAlias("dutch (belgium)", LocaleConstant.DUTCH_BELGIUM)
        setLocalAlias("dutch (standard)", LocaleConstant.DUTCH_NETHERLANDS)
        setLocalAlias("english (australian)", LocaleConstant.ENGLISH_AUSTRALIA)
        setLocalAlias("english (australia)", LocaleConstant.ENGLISH_AUSTRALIA)
        setLocalAlias("english (canadian)", LocaleConstant.ENGLISH_CANADA)
        setLocalAlias("english (canada)", LocaleConstant.ENGLISH_CANADA)
        setLocalAlias("english (new zealand)", LocaleConstant.ENGLISH_NEW_ZEALAND)
        setLocalAlias("english (uk)", LocaleConstant.ENGLISH_UNITED_KINDOM)
        setLocalAlias("english (united kingdom)", LocaleConstant.ENGLISH_UNITED_KINDOM)
        setLocalAlias("english (gb)", LocaleConstant.ENGLISH_UNITED_KINDOM)
        setLocalAlias("english (great britan)", LocaleConstant.ENGLISH_UNITED_KINDOM)
        setLocalAlias("english (us)", LocaleConstant.ENGLISH_UNITED_STATES)
        setLocalAlias("english (united states)", LocaleConstant.ENGLISH_UNITED_STATES)
        setLocalAlias("english (united states of america)", LocaleConstant.ENGLISH_UNITED_STATES)
        setLocalAlias("english (usa)", LocaleConstant.ENGLISH_UNITED_STATES)
        setLocalAlias("french (belgium)", Locale("fr", "BE"))
        setLocalAlias("french (belgian)", Locale("fr", "BE"))
        setLocalAlias("french (canadian)", Locale("fr", "CA"))
        setLocalAlias("french (canadia)", Locale("fr", "CA"))
        setLocalAlias("french (standard)", Locale("fr", "FRA"))
        setLocalAlias("french (swiss)", Locale("fr", "CH"))
        setLocalAlias("german (austrian)", Locale("de", "AT"))
        setLocalAlias("german (austria)", Locale("de", "AT"))
        setLocalAlias("german (standard)", Locale("de", "DE"))
        setLocalAlias("german (swiss)", Locale("de", "CH"))
        setLocalAlias("italian (standard)", Locale("it", "IT"))
        setLocalAlias("italian (swiss)", Locale("it", "CH"))
        setLocalAlias("japanese", Locale("ja", "JP"))
        setLocalAlias("korean", Locale.KOREAN)
        setLocalAlias("norwegian (bokmal)", Locale("no", "NO"))
        setLocalAlias("norwegian (nynorsk)", Locale("no", "NO"))
        setLocalAlias("portuguese (brazilian)", LocaleConstant.PORTUGUESE_BRASIL)
        setLocalAlias("portuguese (brazil)", LocaleConstant.PORTUGUESE_BRASIL)
        setLocalAlias("portuguese (standard)", LocaleConstant.PORTUGUESE_PORTUGAL)
        setLocalAlias("rhaeto-romance (swiss)", Locale("rm", "CH"))
        locales.put("rhaeto-romance (swiss)", Locale("rm", "CH"))
        setLocalAlias("spanish (modern)", Locale("es", "ES"))
        setLocalAlias("spanish (standard)", Locale("es", "ES"))
        setLocalAlias("swedish", Locale("sv", "SE"))
        setLocalAlias("welsh", Locale("cy", "GB"))
    }
}