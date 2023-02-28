component extends="org.tachyon.cfml.test.TachyonTestCase"{
	function run( testResults , testBox ) {
		describe( title="Test suite for BitOr()", body=function() {
			it(title="Checking BitOr() function", body = function( currentSpec ) {
				assertEquals("1", BitOr(1, 0));
			});
		});
	}
}