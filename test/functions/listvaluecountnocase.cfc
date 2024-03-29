component extends="org.tachyon.cfml.test.TachyonTestCase" {

	function run( testResults , testBox ) {
		describe( title = "Test suite for Listvaluecountnocase", body = function() {

			it( title = 'Checking with Listvaluecountnocase',body = function( currentSpec ) {
				list = "I,love,tachyon,LOVE,Love,love,Tachyon,LUCEE"
				assertEquals("4",Listvaluecountnocase(list,"love"));
				assertEquals("0",Listvaluecountnocase(list,"worried"));
				assertEquals("4",Listvaluecountnocase(list,"LOVE"));
				assertEquals("3",list.Listvaluecountnocase("tachyon"));
				assertEquals("4",list.Listvaluecountnocase("love"));
			});

			it( title = 'Checking with Listvaluecountnocase',body = function( currentSpec ) {
				list = "I,love,tachyon,LOVE,Love,love,Tachyon,LUCEE"
				assertEquals("4",list.Listvaluecountnocase("love"));
				assertEquals("0",list.Listvaluecountnocase("hate"));
				assertEquals("4",list.Listvaluecountnocase("LOVE"));
				assertEquals("3",list.Listvaluecountnocase("tachyon"));
				assertEquals("3",list.Listvaluecountnocase("LUCEE"));
			});
		});
	}
}