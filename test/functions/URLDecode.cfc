component extends="org.tachyon.cfml.test.TachyonTestCase"{
	function run( testResults , testBox ) {
		describe( title="Test suite for URLDecode()", body=function() {
			it(title="checking URLDecode() function", body = function( currentSpec ) {
				assertEquals("123", "#URLDecode("123")#");
				assertEquals(" ", "#URLDecode("+")#");
				assertEquals(" ", "#URLDecode("%20")#");

				assertEquals("%25", "#URLEncodedFormat('%')#");
				assertEquals("%", "#URLDecode(URLEncodedFormat('%'))#");

				assertEquals("%25%26%2F", "#URLEncodedFormat('%&/')#");
				assertEquals("%&/", "#URLDecode('%&/')#");
				assertEquals("%", "#URLDecode('%')#");
				assertEquals(" ", "#"+".URLDecode()#");
			});
		});
	}
}