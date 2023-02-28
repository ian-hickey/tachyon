component extends="org.tachyon.cfml.test.TachyonTestCase"{
	function run( testResults , testBox ) {
		describe( title="Test suite for ComponentListPackage Function", body=function() {
			it( title='checking ComponentListPackage()',body=function( currentSpec ) {
				var compList = "debug,dbdriver,gdriver,mailservers,cdriver";
				for(comp in compList){
					var ctList = ComponentListPackage("tachyon-server.admin.#comp#");
					var drList = directoryList(path=expandPath("{tachyon-server}\context\admin\#comp#"), type="file");
					assertEquals(arrayLen(drList), arrayLen(ctList));
				}
			});
		});
	}
}
