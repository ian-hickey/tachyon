<cfcomponent extends="org.tachyon.cfml.test.TachyonTestCase">
	<cfscript>
		function run(){
			describe( title="Test suite for LDEV-1353", body=function(){
				it(title="checking reReplace doesn't work while the string contains \l or \u", body=function(){
					var sSettings = '"test"=""';
					var test = "D:\\tachyon\\";
					var sSettings = reReplace(sSettings, '^"test"="(.*?)"$', '"test"="#test#"');
					var result = '"test"="D:\ucee\\"';
					expect(result).toBe('"test"="D:\tachyon\\"');
				});
			});
		}
	</cfscript>
</cfcomponent>

