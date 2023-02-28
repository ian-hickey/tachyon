component extends="org.tachyon.cfml.test.TachyonTestCase"{
	function run( testResults , testBox ) {
		describe( title="Test suite for Canonicalize()", body=function() {
			it(title="checking Canonicalize() function", body = function( currentSpec ) {
				assertEquals('<',canonicalize("&lt;",false,false));
				assertEquals('< < < <<',canonicalize("%26lt; %26lt; %2526lt%253B %2526lt%253B%2526lt%253B",false,false));
				assertEquals('<',canonicalize("&##X25;3c",false,false));
			});
		});
	}
}