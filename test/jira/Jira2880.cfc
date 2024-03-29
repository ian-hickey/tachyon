<!--- 
 *
 * Copyright (c) 2014, the Railo Company LLC. All rights reserved.
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
	
	//public function beforeTests(){}
	
	//public function afterTests(){}
	
	//public function setUp(){}

	public void function testCreateODBCdate() {
		var orgtz=getTimeZone();
		setTimeZone("UTC");
		try{
			var date=createDatetime(2000,1,2,3,4,5,6,"UTC");
			assertEquals("{d '2000-01-02'}",createODBCdate(date)&"");
			assertEquals("{d '2000-01-02'}",createODBCdate(date,"UTC")&"");
			assertEquals("{d '2000-01-02'}",createODBCdate(date).toString()&"");
			assertEquals("{d '2000-01-02'}",createODBCdate(date,"UTC").toString()&"");
			assertEquals("{d '2000-01-02'}",evaluate('createODBCdate(date)')&"");
			assertEquals("{d '2000-01-02'}",evaluate('createODBCdate(date,"UTC")')&"");
		}
		finally {
			setTimeZone(orgtz);
		}
	}
} 
</cfscript>