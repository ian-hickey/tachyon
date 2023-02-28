component extends="org.tachyon.cfml.test.TachyonTestCase" {
	function run() {
		describe( title = "Test suite for LDEV2523", body = function() {
			it( title = 'Test case for Array map member function skips null values',body = function( currentSpec ) {
				a = [JavaCast("null","")];
				b = arraymap(a,function(item) {
					return "tachyon";
				});
				assertEquals("1",arraylen(a));
				assertEquals("false",arrayisempty(b));
				assertEquals("tachyon",b[1]);
			});

			it( title = 'Test case for Array map member function skips null values for member function',body = function( currentSpec ) {
				a = [JavaCast("null","")];
				b = a.map(function(item) {
					return "tachyon_core_dev";
				});
				assertEquals("1",arraylen(a));
				assertEquals("false",arrayisempty(b));
				assertEquals("tachyon_core_dev",b[1]);
			});
		});
	}
}