component extends="org.tachyon.cfml.test.TachyonTestCase"{
	function run( testResults , testBox ) {
		describe( "test case for decodeFromURL", function() {
			it(title = "Checking with decodeFromURL", body = function( currentSpec ) {
				assertEquals("0", "#DecrementValue(1)#");
				assertEquals("999", "#DecrementValue(1000)#");
			});
		});	
	}
}