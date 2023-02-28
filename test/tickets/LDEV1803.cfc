component extends="org.tachyon.cfml.test.TachyonTestCase"{
	function run(){
		describe( title="Test suite for LDEV-1803", body=function(){
			it(title="Checking toBase64() with arguments", body=function(){
				var input = 251;
				assertEquals("MjUx", toBase64("251"));
				assertEquals("MjUx", toBase64(251));
				assertEquals("MjUx", toBase64(input));
			});
		});
	}
}