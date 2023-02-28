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
package tachyon.runtime.i18n

import java.util.Locale

object LocaleConstant {
    val ALBANIAN_ALBANIA: Locale? = Locale("sq", "AL")
    val ARABIC_ALGERIA: Locale? = Locale("ar", "DZ")
    val ARABIC_BAHRAIN: Locale? = Locale("ar", "BH")
    val ARABIC_EGYPT: Locale? = Locale("ar", "EG")
    val ARABIC_IRAQ: Locale? = Locale("ar", "IQ")
    val ARABIC_JORDAN: Locale? = Locale("ar", "JO")
    val ARABIC_KUWAIT: Locale? = Locale("ar", "KW")
    val ARABIC_LEBANON: Locale? = Locale("ar", "LB")
    val ARABIC_LIBYA: Locale? = Locale("ar", "LY")
    val ARABIC_MAROCCO: Locale? = Locale("ar", "MA")
    val ARABIC_OMAN: Locale? = Locale("ar", "OM")
    val ARABIC_QATAR: Locale? = Locale("ar", "QA")
    val ARABIC_SAUDI_ARABIA: Locale? = Locale("ar", "SA")
    val ARABIC_SUDAN: Locale? = Locale("ar", "SD")
    val ARABIC_SYRIA: Locale? = Locale("ar", "SY")
    val ARABIC_TUNISIA: Locale? = Locale("ar", "TN")
    val ARABIC_UNITED_ARAB_EMIRATES: Locale? = Locale("ar", "AE")
    val ARABIC_YEMEN: Locale? = Locale("ar", "YE")
    val CHINESE_HONG_KONG: Locale? = Locale("zh", "HK")
    val CHINESE_SINGAPORE: Locale? = Locale("zh", "SG")
    val CHINESE_TAIWAN: Locale? = Locale("zh", "TW")
    val DUTCH_BELGIUM: Locale? = Locale("nl", "BE")
    val DUTCH_NETHERLANDS: Locale? = Locale("nl", "NL")
    val ENGLISH_AUSTRALIA: Locale? = Locale("en", "AU")
    val ENGLISH_CANADA: Locale? = Locale("en", "CA")
    val ENGLISH_NEW_ZEALAND: Locale? = Locale("en", "NZ")
    val ENGLISH_UNITED_KINDOM: Locale? = Locale("en", "GB")
    val ENGLISH_UNITED_STATES: Locale? = Locale("en", "US")
    val PORTUGUESE_PORTUGAL: Locale? = Locale("pt", "PT")
    val PORTUGUESE_BRASIL: Locale? = Locale("pt", "BR") /*
	 * static { setLocalAlias("albanian (albania)", LocaleConstant.ALBANIAN_ALBANIA);
	 * 
	 * setLocalAlias("arabic (algeria)", LocaleConstant.ARABIC_ALGERIA);
	 * setLocalAlias("arabic (bahrain)", LocaleConstant.ARABIC_BAHRAIN); setLocalAlias("arabic (egypt)",
	 * LocaleConstant.ARABIC_EGYPT); setLocalAlias("arabic (iraq)", LocaleConstant.ARABIC_IRAQ);
	 * setLocalAlias("arabic (jordan)", LocaleConstant.ARABIC_JORDAN); setLocalAlias("arabic (kuwait)",
	 * LocaleConstant.ARABIC_KUWAIT); setLocalAlias("arabic (lebanon)", LocaleConstant.ARABIC_LEBANON);
	 * setLocalAlias("arabic (libya)", LocaleConstant.ARABIC_LIBYA); setLocalAlias("arabic (morocco)",
	 * LocaleConstant.ARABIC_MAROCCO); setLocalAlias("arabic (oman)", LocaleConstant.ARABIC_OMAN);
	 * setLocalAlias("arabic (qatar)", LocaleConstant.ARABIC_QATAR);
	 * setLocalAlias("arabic (saudi arabia)", LocaleConstant.ARABIC_SAUDI_ARABIA);
	 * setLocalAlias("arabic (sudan)", LocaleConstant.ARABIC_SUDAN); setLocalAlias("arabic (syria)",
	 * LocaleConstant.ARABIC_SYRIA); setLocalAlias("arabic (tunisia)", LocaleConstant.ARABIC_TUNISIA);
	 * setLocalAlias("arabic (united arab emirates)", LocaleConstant.ARABIC_UNITED_ARAB_EMIRATES);
	 * setLocalAlias("arabic (yemen)", LocaleConstant.ARABIC_YEMEN);
	 * 
	 * setLocalAlias("chinese (china)", Locale.CHINA);
	 * setLocalAlias("chinese (hong kong)",LocaleConstant.CHINESE_HONG_KONG);
	 * setLocalAlias("chinese (singapore)",LocaleConstant.CHINESE_SINGAPORE);
	 * setLocalAlias("chinese (taiwan)",LocaleConstant.CHINESE_TAIWAN);
	 * setLocalAlias("dutch (belgian)",LocaleConstant.DUTCH_BELGIUM);
	 * setLocalAlias("dutch (belgium)",LocaleConstant.DUTCH_BELGIUM);
	 * setLocalAlias("dutch (standard)",LocaleConstant.DUTCH_NETHERLANDS);
	 * setLocalAlias("english (australian)",LocaleConstant.ENGLISH_AUSTRALIA);
	 * setLocalAlias("english (australia)",LocaleConstant.ENGLISH_AUSTRALIA);
	 * setLocalAlias("english (canadian)",LocaleConstant.ENGLISH_CANADA);
	 * setLocalAlias("english (canada)",LocaleConstant.ENGLISH_CANADA);
	 * setLocalAlias("english (new zealand)",LocaleConstant.ENGLISH_NEW_ZEALAND);
	 * setLocalAlias("english (uk)",LocaleConstant.ENGLISH_UNITED_KINDOM);
	 * setLocalAlias("english (united kingdom)",LocaleConstant.ENGLISH_UNITED_KINDOM);
	 * setLocalAlias("english (gb)",LocaleConstant.ENGLISH_UNITED_KINDOM);
	 * setLocalAlias("english (great britan)",LocaleConstant.ENGLISH_UNITED_KINDOM);
	 * setLocalAlias("english (us)",LocaleConstant.ENGLISH_UNITED_STATES);
	 * setLocalAlias("english (united states)",LocaleConstant.ENGLISH_UNITED_STATES);
	 * setLocalAlias("english (united states of america)",LocaleConstant.ENGLISH_UNITED_STATES);
	 * setLocalAlias("english (usa)",LocaleConstant.ENGLISH_UNITED_STATES);
	 * setLocalAlias("french (belgium)",new Locale("fr","BE")); setLocalAlias("french (belgian)",new
	 * Locale("fr","BE")); setLocalAlias("french (canadian)",new Locale("fr","CA"));
	 * setLocalAlias("french (canadia)",new Locale("fr","CA")); setLocalAlias("french (standard)",new
	 * Locale("fr","FRA")); setLocalAlias("french (swiss)",new Locale("fr","CH"));
	 * setLocalAlias("german (austrian)",new Locale("de","AT")); setLocalAlias("german (austria)",new
	 * Locale("de","AT")); setLocalAlias("german (standard)",new Locale("de","DE"));
	 * setLocalAlias("german (swiss)",new Locale("de","CH")); setLocalAlias("italian (standard)",new
	 * Locale("it","IT")); setLocalAlias("italian (swiss)",new Locale("it","CH"));
	 * setLocalAlias("japanese",new Locale("ja","JP")); setLocalAlias("korean",Locale.KOREAN);
	 * setLocalAlias("norwegian (bokmal)",new Locale("no","NO"));
	 * setLocalAlias("norwegian (nynorsk)",new Locale("no","NO"));
	 * setLocalAlias("portuguese (brazilian)",LocaleConstant.PORTUGUESE_BRASIL);
	 * setLocalAlias("portuguese (brazil)",new LocaleConstant.PORTUGUESE_BRASIL);
	 * setLocalAlias("portuguese (standard)",LocaleConstant.PORTUGUESE_PORTUGAL);
	 * setLocalAlias("rhaeto-romance (swiss)",new Locale("rm","CH"));
	 * locales.put("rhaeto-romance (swiss)",new Locale("rm","CH")); setLocalAlias("spanish (modern)",new
	 * Locale("es","ES")); setLocalAlias("spanish (standard)",new Locale("es","ES"));
	 * setLocalAlias("swedish",new Locale("sv","SE")); } private static void setLocalAlias(String
	 * string, Locale china) {
	 * 
	 * }
	 */
    // TODO add all from http://www.oracle.com/technetwork/java/javase/locales-137662.html
}