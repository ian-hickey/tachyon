component extends="org.tachyon.cfml.test.TachyonTestCase"{
	function run( testResults , testBox ) {
		describe( "test case for ListContainsNoCase", function() {
			it(title = "Checking with ListContainsNoCase", body = function( currentSpec ) {
				assertEquals("1", "#ListContainsNoCase('abba,bb','bb')#");
				assertEquals("1", "#ListContainsNoCase('abba,bb,AABBCC','BB')#");
				assertEquals("0", "#ListContainsNoCase('abba,bb,AABBCC','ZZ')#");
				assertEquals("0", "#ListContainsNoCase(',,,,abba,bb,AABBCC,,,','ZZ')#");
				assertEquals("0", "#ListContainsNoCase(',,,,abba,bb,AABBCC,,,','ZZ','.,;')#");
				assertEquals("0", "#ListContainsNoCase("evaluate,expression","")#");
				assertEquals("0", "#ListContainsNoCase("evaluate,,expression","")#"); 
				assertEquals("2", "#ListContainsNoCase("evaluate,,expression","expression")#");
				assertEquals("3", "#ListContainsNoCase("evaluate, ,expression","expression")#");
				assertEquals("3", "#ListContainsNoCase("evaluate, ,expression","expression",',',true)#");
				assertEquals("3", "#ListContainsNoCase("evaluate,,expression","expression",',',true)#");
				assertEquals("3", "#ListContainsNoCase("evaluate, ,expression","expression",',',false)#");
				assertEquals("2", "#ListContainsNoCase("evaluate,,expression","expression",',',false)#");
				assertEquals("2", "#ListContainsNoCase("evaluate,,expression","expression")#");
				assertEquals("3", "#ListContainsNoCase("evaluate, ,expression","expression")#");
				assertEquals("3", "#ListContainsNoCase("evaluate, ,expression","expression",',',true)#");
				assertEquals("3", "#ListContainsNoCase("evaluate,,expression","expression",',',true)#");
				assertEquals("3", "#ListContainsNoCase("evaluate, ,expression","expression",',',false)#");
				assertEquals("2", "#ListContainsNoCase("evaluate,,expression","expression",',',false)#");
				assertEquals("2", "#ListContainsNoCase("evaluate,,expression","expression",',;',false,false)#");
				assertEquals("1", "#ListContainsNoCase("evaluate,,expression","expression",',;',false,true)#");
			});
		});	
	}
}