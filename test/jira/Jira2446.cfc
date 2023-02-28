/**
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
 **/
component extends="org.tachyon.cfml.test.TachyonTestCase"  {


	public function testString() {

		var classpath = directoryList( "#getTicketName()#/lib", false, "path", "*.jar" );

  		var obj = createObject( "java", "org.xbill.DNS.Client", classpath.toList() );
	}


	public function testArray() {

		var classpath = directoryList( "#getTicketName()#/lib", false, "path", "*.jar" );

  		var obj = createObject( "java", "org.xbill.DNS.Client", classpath );
	}


	public function testNone() {

		try {

			var obj = createObject( "java", "org.xbill.DNS.Client" );

			fail( "Expected class not found exception" );
		} 
		catch( ex ) {}
	}


	private function getTicketName() {

		return listFirst( listLast( getCurrentTemplatePath(), '\/' ), '.' );
	}

}