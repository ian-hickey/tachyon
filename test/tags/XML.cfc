
component extends="org.tachyon.cfml.test.TachyonTestCase"	{
	
	variables.xml='<img src="/tachyon/graph.cfm?img=qq.png&type=png"/>';// the & is the problem
		
	public void function testLenient(){
		xml variable="LOCAL.res" lenient=true {
			echo(variables.xml);
		}
	}
	public void function testNotLenient(){

		var failed=false;
		try {
			xml variable="LOCAL.res" lenient=false {
				echo(variables.xml);
			}
		}
		catch(e) {
			failed=true;
		}
		assertEquals(true,failed);
	}
} 
