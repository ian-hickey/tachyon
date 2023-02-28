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
	

	public void function testStringMap(){
		myStr="123456789";       
        closure=function(item){ 
            return item+5; 
        }
    	assertEquals('67891011121314', myStr.map(closure));
        
    	myString="Hello World"         
        closure=function(val){               
            return (val & 'a')  
        }
    	assertEquals('Haealalaoa aWaoaralada', StringMap(callback=closure,string=myString));
	}
} 
</cfscript>