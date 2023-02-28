<cfscript>
component extends="org.tachyon.cfml.test.TachyonTestCase"	{

	//public function setUp(){}

	public void function testCFM(){
		local.uri=createURI("Jira3070/index.cfm");
		local.result=_InternalRequest(template:uri);
		assertEquals(false,result.filecontent.trim());
	}

	public void function testCFC(){
		variables.test = nullValue();
		assertEquals(false,structKeyExists(variables, "test")); 

	}
	private string function createURI(string calledName){
		var baseURI="/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI&""&calledName;
	}
	
} 	
</cfscript>