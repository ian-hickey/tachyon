component extends="org.tachyon.cfml.test.TachyonTestCase"{
	function afterALL(){
		if(fileExists(variables.testfile)){
			fileDelete(variables.testfile);
		}
	}

	function run(){
		describe( title="Test suite for LDEV-1706", body=function(){
			it(title="Checking invokeStatic() function", body=function(){
				var dir="#GetDirectoryFromPath(GetCurrentTemplatePath())#LDEV1706\";
				if(!directoryExists(dir)){
					Directorycreate(dir);
				}
				variables.testfile = dir&"foo.cfc";
				fileWrite(variables.testfile,"
					component {
						static function bar() {
							return 'Tachyon';
						}
					}");
				try {
					var result = invokeStatic("foo", "bar");
				} catch ( any e ) {
					var result = e.message;
				}
				assertEquals("tachyon", LDEV1706.foo::bar());
				assertEquals("tachyon", result);
			});
		});
	}
}