<cfcomponent extends="org.tachyon.cfml.test.TachyonTestCase">
	<!---
	<cffunction name="beforeTests"></cffunction>
	<cffunction name="afterTests"></cffunction>
	<cffunction name="setUp"></cffunction>
	--->

	<cffunction name="testIncludeEmptyField" localMode="modern">
		<cfset stringtoreplace = "The quick brown fox jumped over the lazy dog.">
		<cfset assertEquals(
			"The quick ferret white jumped over the lazy .",
			ReplaceList(stringtoreplace, "dog:brown:fox:black", "--black-ferret-white", ":", "-", false)
		)>

		<cfset assertEquals(
			"The quick  ferret jumped over the lazy .",
			ReplaceList(stringtoreplace, "dog:brown:fox:black", "--black-ferret-white", ":", "-", true)
		)>
	</cffunction>

	<cffunction name="testReplaceListCase" localMode="modern">
		<cfset assertEquals(
			"xxxA1C3456789xxx0123456789",
			ReplaceList('xxxAbCdefghijxxxabcdefghij','a,b,c,d,e,f,g,h,i,j','0,1,2,3,4,5,6,7,8,9')
		)>
	</cffunction>

	<cffunction name="testReplaceListMember" localMode="modern">
		<cfset assertEquals("xxx0123456789xxx0123456789",'xxxabcdefghijxxxabcdefghij'.ReplaceList('a,b,c,d,e,f,g,h,i,j','0,1,2,3,4,5,6,7,8,9'))>
	</cffunction>

	<cffunction name="testReplaceList" localMode="modern">

		<cfset assertEquals("xxx0123456789xxx0123456789",ReplaceList('xxxabcdefghijxxxabcdefghij','a,b,c,d,e,f,g,h,i,j','0,1,2,3,4,5,6,7,8,9'))>
	
		<cfset assertEquals("xxx0xxx0",ReplaceList('xxxabcdefghijxxxabcdefghij','a,b,c,d,e,f,g,h,i,j','0'))>
	
		<cfset assertEquals("xxx0bcdefghijxxx0bcdefghij",ReplaceList('xxxabcdefghijxxxabcdefghij','a','0,1,2,3,4,5,6,7,8,9'))>
	
		<cfset assertEquals("xxxabcdefghijxxxabcdefghij",ReplaceList('xxxabcdefghijxxxabcdefghij','',''))>
    
		<cfset assertEquals("xxx0,1,2,3,4,5,6,7,8,9xxx0,1,2,3,4,5,6,7,8,9",ReplaceList(
		    	'xxxabcdefghijxxxabcdefghij',
		        'a;b;c;d;e;f;g;h;i;j',
		        '0,1,2,3,4,5,6,7,8,9'
		        ,';'))>
    
		<cfset assertEquals("xxx0123456789xxx0123456789",ReplaceList(
    	'xxxabcdefghijxxxabcdefghij',
        'a;b;c;d;e;f;g;h;i;j',
        '0,1,2,3,4,5,6,7,8,9'
        ,';',','))>
    
		<cfset assertEquals("xxx0123456789xxx0123456789",ReplaceList(
		    	'xxxabcdefghijxxxabcdefghij',
		        'a;b;c;d;e;f;g;h;i;j',
		        '0:1:2:3:4:5:6:7:8:9'
		        ,';',':'))>
		
	</cffunction>
	
</cfcomponent>
