component extends="org.tachyon.cfml.test.TachyonTestCase" {
	function run( testResults, textbox ) {
		describe("testcase for ArrayReduce()", function() {
			it(title="checking ArrayReduce() function", body=function( currentSpec ) {
				arr=["there","tachyon"];
				assertEquals("hello there tachyon", ArrayReduce(arr, function(value1,value2) { return (value1 & " " & value2)}, "hello"));
			});
			it(title="checking Array.Reduce() member function", body=function( currentSpec ) {
				arr=["there","tachyon"];
				assertEquals("hello there tachyon", arr.Reduce(function(value1,value2) { return (value1 & " " & value2)}, "hello"));
			});
		});
	}
}