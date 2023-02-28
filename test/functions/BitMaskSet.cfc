component extends="org.tachyon.cfml.test.TachyonTestCase"{
	function run( testResults , testBox ) {
		describe( title="Test suite for BitMaskSet()", body=function() {
			it(title="Checking BitMaskSet() function", body = function( currentSpec ) {
				assertEquals("255",BitMaskSet(255, 255, 4, 4));
				assertEquals("15",BitMaskSet(255, 0, 4, 4));
				assertEquals("240",BitMaskSet(0, 15, 4, 4));
			});
		});
	}
}