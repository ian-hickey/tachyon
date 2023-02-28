component extends="org.tachyon.cfml.test.TachyonTestCase"{
	function run( testResults , testBox ) {
		describe( title="Test suite for LDEV-1419", body=function() {
			it(title="comparing decimal numbers with decimal numbers as string data type ", body = function( currentSpec ) {
				var a = 12345678.90;
				var b = "12345678.90";
				expect(a == b).toBe(true);
			});

			it(title="comparing numbers with numbers as string data type", body = function( currentSpec ) {
				var a = 1234567890;
				var b = "1234567890";
				expect(a == b).toBe(true);
			});
		});
	}
}
