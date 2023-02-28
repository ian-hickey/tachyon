﻿<!--- 
 *
 * Copyright (c) 2016, Tachyon Assosication Switzerland. All rights reserved.*
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
	variables.qry=query(a:[[1,15,19],[2,25,29]],b:['a','b'])


	public function testValueIsArray() {
		assertEquals('1,15,19',ArrayToList(variables.qry.a));
	}

	public function testValueIsNotArray() {
		assertEquals('a,b',ArrayToList(variables.qry.b));
	}
} 
</cfscript>