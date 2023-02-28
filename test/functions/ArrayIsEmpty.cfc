component extends="org.tachyon.cfml.test.TachyonTestCase"{
	function run( testResults , testBox ) {
		describe( title="Test suite for ArrayIsEmpty()", body=function() {
			it(title="checking ArrayIsEmpty() function", body = function( currentSpec ) {
				var arr=arrayNew(1);
				assertEquals(true, arrayisEmpty(arr));
				ArrayAppend( arr, 1 );
				assertEquals(false, arrayisEmpty(arr));
				ArrayClear( arr );
				assertEquals(true, arrayisEmpty(arr));
			});
		});
	}
}