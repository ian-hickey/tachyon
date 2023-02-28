component extends="org.tachyon.cfml.test.TachyonTestCase"{
	function run(){
		describe( title="Test suite for LDEV-1765", body=function(){
			it(title="Checking ImageGetIPTCMetadata() function", body=function(){
				cfimage(source="LDEV1765\test.JPG", name="local.myImage");
				try {
					var result = ImageGetIPTCMetadata(local.myImage);
				} catch ( any e ){
					var result = e.message;
				}
				assertEquals(true, isstruct(result));
				assertEquals("Pacific Palisades", result.city);
			});
		});
	}
}