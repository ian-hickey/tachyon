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
 ---><cfcomponent extends="org.tachyon.cfml.test.TachyonTestCase">
	<cffunction name="beforeTests">
		<cfapplication action="update" clientmanagement="true">
	</cffunction>
	
	
	<!---
	<cffunction name="beforeTests"></cffunction>
	<cffunction name="afterTests"></cffunction>
	<cffunction name="setUp"></cffunction>
	--->
	<cffunction name="testDeleteClientVariable" localMode="modern">


		<!--- not working in JSR223env --->
		<cfif server.tachyon.environment=="servlet">
			<cflock timeout="1000" throwontimeout="yes" type="exclusive" scope="request">
				<cfset client.susi=1>
				<cfset assertEquals(true,DeleteClientVariable('susi'))>
				<cfset assertEquals(false,DeleteClientVariable('susi'))>
			</cflock>
		</cfif>
	
		
	</cffunction>
	
	<cffunction access="private" name="valueEquals">
		<cfargument name="left">
		<cfargument name="right">
		<cfset assertEquals(arguments.right,arguments.left)>
	</cffunction>
</cfcomponent>