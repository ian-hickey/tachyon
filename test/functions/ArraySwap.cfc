component extends="org.tachyon.cfml.test.TachyonTestCase"{
	function run( testResults , testBox ) {
		describe( title="Test suite for ArraySwap()", body=function() {
			it(title="Checking ArraySwap() function", body = function( currentSpec ) {
				arr=arrayNew(1);
				arr[1]=111;
				arr[2]=22;
				arr[3]=3.5;
				 
				ArraySwap(arr, 1,3);
				assertEquals("3.5",arr[1]);
				assertEquals("22",arr[2]);
				assertEquals("111",arr[3]);

				try{
					ArraySwap(arr, 1,4);
					fail("must throw:4 is an invalid swap index of the array. ");
				} catch(any e){}
			});
		});
	}
}
	
