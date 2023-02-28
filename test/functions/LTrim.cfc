component extends="org.tachyon.cfml.test.TachyonTestCase" {

	public function run( testResults , testBox ) {
		describe( title = "Testcase for lTrim()", body = function() {
			it(title = "checking lTrim()", body = function( currentSpec ) {
				assertEquals("welcome ", lTrim("   welcome "));
			});

			it(title = "checking lTrim() member function", body = function( currentSpec ) {
				assertEquals("welcome   ", "   welcome   ".lTrim());
			});
		});
	}
}