<!--- 
 *
 * Copyright (c) 2015, Tachyon Assosication Switzerland. All rights reserved.
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
 * License along with this library.  If not, see <http://www.gnu.org/licenses/>.
 * 
 ---><cfscript>
component extends="org.tachyon.cfml.test.TachyonTestCase"	{

	public void function testLoadingClassicExternalJarsUsingContextClassloaderToLoadRes(){
		
	jars=directoryList("LDEV0762");

	createObject("java", "org.apache.lucene.util.NamedSPILoader",jars);
	charArraySet = createObject("java", "org.apache.lucene.analysis.util.CharArraySet",jars);
	analyzer = createObject("java","org.apache.lucene.analysis.standard.StandardAnalyzer",jars).init(charArraySet.EMPTY_SET);
	createObject("java", "org.apache.lucene.index.IndexWriterConfig",jars).getClass().getCLassLoader();
	createObject("java", "org.apache.lucene.index.IndexWriterConfig",jars).init(analyzer);
	}

} 
</cfscript>