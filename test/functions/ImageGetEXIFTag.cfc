component extends="org.tachyon.cfml.test.TachyonTestCase" labels="image" {
	function run( testResults , testBox ) {
		describe( "test case for ImageGetEXIFTag", function() {
			it(title = "Checking with ImageGetEXIFTag", body = function( currentSpec ) {
				img=imageRead(GetDirectoryFromPath(GetCurrentTemplatePath())&"images/BigBen.jpg");
				assertEquals("6" , "#ImageGetEXIFTag(img,'Compression')#");
				assertEquals("16" , "#ImageGetEXIFTag(img,'Flash')#");
			});
		});	
	}
}