<!--- 
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
 ---><cfcomponent extends="org.tachyon.cfml.test.TachyonTestCase">
	
	<cffunction name="setUp"></cffunction>
	<cffunction name="test" localmode=true>
		
		<cfset MMTest = createObject('component','Jira0396.onMMTest') />

		<cfset TestMethod = MMTest.aMethodThatDoesntExist("An argument") />
		<cfset assertEquals("An argument",TestMethod.ARGS[1])>
		<cfset assertEquals("aMethodThatDoesntExist",TestMethod.target)>

		<cfset TestMethod = MMTest.aMethodThatDoesntExist(a:"An argument") />
		<cfset assertEquals("An argument",TestMethod.ARGS.a)>
		<cfset assertEquals("aMethodThatDoesntExist",TestMethod.target)>


	</cffunction>
</cfcomponent>