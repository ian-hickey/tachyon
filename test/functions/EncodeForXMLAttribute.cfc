component extends="org.tachyon.cfml.test.TachyonTestCase" labels="esapi,xml"{
	function run( testResults , testBox ) {
		describe( "test case for EncodeForXMLAttribute", function() {
			it(title = "Checking with EncodeForXMLAttribute", body = function( currentSpec ) {
				enc=EncodeForXMLAttribute('<script>');
				assertEquals('&##x3c;script&##x3e;',enc);
			});

			it(title = "Checking with EncodeForXMLAttributeMember", body = function( currentSpec ) {
				enc='<script>'.EncodeForXMLAttribute();
				assertEquals('&##x3c;script&##x3e;',enc);
			});
		});	
	}
}