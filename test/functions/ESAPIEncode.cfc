component extends="org.tachyon.cfml.test.TachyonTestCase" labels="esapi"{
	function run( testResults , testBox ) {
		describe( "test case for ESAPIEncode", function() {
			it(title = "Checking with ESAPIEncode", body = function( currentSpec ) {
				enc=ESAPIEncode('html','<script>');
				assertEquals('&lt;script&gt;',enc);
			});
		});	
	}
}