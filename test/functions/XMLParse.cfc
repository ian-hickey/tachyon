
component extends="org.tachyon.cfml.test.TachyonTestCase" labels="xml" {
	
	variables.xml='<img src="/tachyon/graph.cfm?img=qq.png&type=png"/>';// the & is the problem
		
	public void function testLenient(){
		xmlParse(xmlString:variables.xml,lenient:true);
	}
	public void function testNotLenient(){
		var failed=false;
		try{
			xmlParse(xml:variables.xml,lenient:false);
		}
		catch(e) {
			failed=true;
		}
		assertEquals(true,failed);
	}
} 
