component extends="org.tachyon.cfml.test.TachyonTestCase"{
	function run( testResults , testBox ) {
		describe( "Test suite for LDEV-1348", function() {
			it(title="Checking function cache", body = function( currentSpec ) {
				var test1 = new LDEV1348.FunctionCachetest ("https://www.google.com/");
				var test2 = new LDEV1348.FunctionCachetest ("http://tachyon.org/");
				assertFalse(test1.test() == test2.test());
			});
		});
	}
}
