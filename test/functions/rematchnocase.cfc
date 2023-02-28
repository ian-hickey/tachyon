component extends="org.tachyon.cfml.test.TachyonTestCase" {
	function run() {
		describe( title="Test suite for reMatchnocase", body=function() {
			it( title='Test case for reMatchnocase function  ',body=function( currentSpec ) {
				assertEquals('["1","45","38"]',serializeJSON( reMatchnocase("[0-9]+", "1 way to extract any number like 45, 38") ));
				assertEquals('["tachyon","Tachyon"]',serializeJSON( reMatchnocase("(tachyon)+", "I love tachyon Tachyon") ));
				assertEquals('["l","e","tachyon","Tachyon"]',serializeJSON( reMatchnocase("[tachyon]+", "I love tachyon Tachyon") ));

			});

			it( title='Test case for reMatchnocase member function',body=function( currentSpec ) {
				assertEquals('["1","45","38"]',serializeJSON( "1 way to extract any number like 45, 38".reMatchnocase("[0-9]+") ));
				assertEquals('["tachyon","Tachyon"]',serializeJSON( "I love tachyon Tachyon".reMatchnocase("(tachyon)+") ));
				assertEquals('["l","e","tachyon","Tachyon"]',serializeJSON( "I love tachyon Tachyon".reMatchnocase("[tachyon]+") ));

			});
		});
	}
}