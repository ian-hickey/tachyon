component extends="org.tachyon.cfml.test.TachyonTestCase"{
	function run( testResults , testBox ) {
		describe( title="Test suite for Cos()", body=function() {
			it(title="checking Cos() function", body = function( currentSpec ) {
				assertEquals("-0.9899924966004454","#tostring(cos(3))#");
			});
		});
	}
}