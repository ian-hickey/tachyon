component extends="org.tachyon.cfml.test.TachyonTestCase" {
	function run( testResults, textbox ) {
		variables.list="there,tachyon";
		describe("testcase for listReduce()", function() {
			it(title="checking listReduce() function", body=function( currentSpec ) {
				assertEquals( "hello there tachyon", listReduce(list, function(value1,value2) { return (value1 & " " & value2)}, "hello"));
			});
			it(title="checking list.listReduce() member function", body=function( currentSpec ) {
				assertEquals( "hello there tachyon", list.listReduce(function(value1,value2) { return (value1 & " " & value2)}, "hello"));
			});
		});
	}
}