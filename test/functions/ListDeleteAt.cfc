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
component extends="org.tachyon.cfml.test.TachyonTestCase" {
	function testListLast(){


		assertEquals("a,c,d",ListDeleteAt('a,b,c,d',2));
		assertEquals(",,a,b,d,,",ListDeleteAt(',,a,b,c,d,,',3));
		assertEquals(",,b,c,d,,",ListDeleteAt(',,a,b,c,d,,',3,',',true));

	}
}