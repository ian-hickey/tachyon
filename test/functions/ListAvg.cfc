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
	function testListAvg(){
		var arr="1,2,3";
		assertEquals(2,ListAvg("1,2,3"));
		assertEquals(3.5,ListAvg("1,2,3,4,5,6"));
		assertEquals(3.5,ListAvg("1,2,3,4,5,6",',;.'));
		assertEquals(3.5,ListAvg("1,2,3,4,5,6",',;.'));
		assertEquals(3.5,ListAvg("1,;2,;3,;4,;5,;6",',;',true));
	}
}