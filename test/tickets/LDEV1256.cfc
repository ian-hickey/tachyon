component extends="org.tachyon.cfml.test.TachyonTestCase"{
	function run( testResults , testBox ) {
		describe( "Test suite for LDEV-1256", function() {
			it( title='Checking parseDateTime() for timezones greater than 12 hours', body=function( currentSpec ) {

				expect(
					parseDateTime("2017-06-11T14:45:54+14:00", "yyyy-MM-dd'T'HH:nn:ssX")
				).toBe("2017-06-11T00:45:54Z");

				// Expected [2017-06-11T00:45:54Z] but received [{ts '2020-09-11 00:00:54'}]

				expect(
					parseDateTime("2017-06-11T00:45:54-13:00", "yyyy-MM-dd'T'HH:nn:ssX")
				).toBe("2017-06-11T13:45:54Z");
			});
		});
	}
}