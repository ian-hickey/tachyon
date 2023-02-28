/*
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
 */
component extends="org.tachyon.cfml.test.TachyonTestCase"	{

	public void function testClassic() localmode="true" {
		qry=query(a:[1,2,3]);
		assertEquals('1,2,3',valueList(qry.a));
		assertEquals('1;2;3',valueList(qry.a,';'));
	}

	/*public void function test() localmode="true" {
		qry=query(a:[1,2,3]);
		assertEquals('1,2,3',valueList(qry,"a"));
	} */

	public void function testMember() localmode="true" {
		qry=query(a:[1,2,3]);
		assertEquals('1,2,3',qry.valueList("a"));
		assertEquals('1;2;3',qry.valueList("a",';'));
	}
} 