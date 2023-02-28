component extends="org.tachyon.cfml.test.TachyonTestCase"{
	function run( testResults , testBox ) {
		describe( "test case for decodeForHTML", function() {
			it(title = "Checking with decodeForHTML", body = function( currentSpec ) {
				dec=decodeForHTML('&lt;script&gt;');
				assertEquals('<script>',dec);
			});
			it(title = "Checking with decodeForHTMLMember", body = function( currentSpec ) {
				dec='&lt;script&gt;'.decodeForHTML();
				assertEquals('<script>',dec);
	
			});
		});	
	}
}