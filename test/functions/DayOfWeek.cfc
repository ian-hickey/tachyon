component extends="org.tachyon.cfml.test.TachyonTestCase"{
	function run( testResults , testBox ) {
		describe( title="Test suite for DayOfWeek()", body=function() {
			it(title="checking DayOfWeek() function", body = function( currentSpec ) {
				d1=CreateDateTime(2001, 12, 1, 4, 10, 1); 
				assertEquals("7", "#dayOfWeek(d1)#");
				assertEquals("#DayOfWeek(1)#", "1");

				assertEquals("7", "#DayOfWeek("{ts '2000-1-1 17:26:03'}")#");
				assertEquals("1", "#DayOfWeek("{ts '2000-1-2 17:26:03'}")#");
				assertEquals("2", "#DayOfWeek("{ts '2000-1-3 17:26:03'}")#");
				assertEquals("3", "#DayOfWeek("{ts '2000-1-4 17:26:03'}")#");
				assertEquals("4", "#DayOfWeek("{ts '2000-1-5 17:26:03'}")#");
				assertEquals("5", "#DayOfWeek("{ts '2000-1-6 17:26:03'}")#");
				assertEquals("6", "#DayOfWeek("{ts '2000-1-7 17:26:03'}")#");
				assertEquals("7", "#DayOfWeek("{ts '2000-1-8 17:26:03'}")#");
				assertEquals("1", "#DayOfWeek("{ts '2000-1-9 17:26:03'}")#");
				assertEquals("2", "#DayOfWeek("{ts '2000-1-10 17:26:03'}")#");
				assertEquals("3", "#DayOfWeek("{ts '2000-1-11 17:26:03'}")#");
				assertEquals("4", "#DayOfWeek("{ts '2000-1-12 17:26:03'}")#");
				assertEquals("5", "#DayOfWeek("{ts '2000-1-13 17:26:03'}")#");
				assertEquals("6", "#DayOfWeek("{ts '2000-1-14 17:26:03'}")#");
				assertEquals("7", "#DayOfWeek("{ts '2000-1-15 17:26:03'}")#");
				assertEquals("1", "#DayOfWeek("{ts '2000-1-16 17:26:03'}")#");
				assertEquals("2", "#DayOfWeek("{ts '2000-1-17 17:26:03'}")#");
				assertEquals("3", "#DayOfWeek("{ts '2000-1-18 17:26:03'}")#");
				assertEquals("4", "#DayOfWeek("{ts '2000-1-19 17:26:03'}")#");
				assertEquals("5", "#DayOfWeek("{ts '2000-1-20 17:26:03'}")#");
				assertEquals("6", "#DayOfWeek("{ts '2000-1-21 17:26:03'}")#");
				assertEquals("7", "#DayOfWeek("{ts '2000-1-22 17:26:03'}")#");
				assertEquals("1", "#DayOfWeek("{ts '2000-1-23 17:26:03'}")#");
				assertEquals("2", "#DayOfWeek("{ts '2000-1-24 17:26:03'}")#");
				assertEquals("3", "#DayOfWeek("{ts '2000-1-25 17:26:03'}")#");
				assertEquals("4", "#DayOfWeek("{ts '2000-1-26 17:26:03'}")#");
				assertEquals("5", "#DayOfWeek("{ts '2000-1-27 17:26:03'}")#");
				assertEquals("6", "#DayOfWeek("{ts '2000-1-28 17:26:03'}")#");
				assertEquals("7", "#DayOfWeek("{ts '2000-1-29 17:26:03'}")#");
				assertEquals("1", "#DayOfWeek("{ts '2000-1-30 17:26:03'}")#");
				assertEquals("2", "#DayOfWeek("{ts '2000-1-31 17:26:03'}")#");
				assertEquals("3", "#DayOfWeek("{ts '2000-2-1 17:26:03'}")#");
				assertEquals("4", "#DayOfWeek("{ts '2000-2-2 17:26:03'}")#");
				assertEquals("5", "#DayOfWeek("{ts '2000-2-3 17:26:03'}")#");
				assertEquals("6", "#DayOfWeek("{ts '2000-2-4 17:26:03'}")#");
				assertEquals("7", "#DayOfWeek("{ts '2000-2-5 17:26:03'}")#");
				assertEquals("1", "#DayOfWeek("{ts '2000-2-6 17:26:03'}")#");
				assertEquals("2", "#DayOfWeek("{ts '2000-2-7 17:26:03'}")#");
				assertEquals("3", "#DayOfWeek("{ts '2000-2-8 17:26:03'}")#");
				assertEquals("4", "#DayOfWeek("{ts '2000-2-9 17:26:03'}")#");
				assertEquals("5", "#DayOfWeek("{ts '2000-2-10 17:26:03'}")#");
				assertEquals("6", "#DayOfWeek("{ts '2000-2-11 17:26:03'}")#");
				assertEquals("7", "#DayOfWeek("{ts '2000-2-12 17:26:03'}")#");
				assertEquals("1", "#DayOfWeek("{ts '2000-2-13 17:26:03'}")#");
				assertEquals("2", "#DayOfWeek("{ts '2000-2-14 17:26:03'}")#");
				assertEquals("3", "#DayOfWeek("{ts '2000-2-15 17:26:03'}")#");
				assertEquals("4", "#DayOfWeek("{ts '2000-2-16 17:26:03'}")#");
				assertEquals("5", "#DayOfWeek("{ts '2000-2-17 17:26:03'}")#");
				assertEquals("6", "#DayOfWeek("{ts '2000-2-18 17:26:03'}")#");
				assertEquals("7", "#DayOfWeek("{ts '2000-2-19 17:26:03'}")#");
				assertEquals("1", "#DayOfWeek("{ts '2000-2-20 17:26:03'}")#");
				assertEquals("2", "#DayOfWeek("{ts '2000-2-21 17:26:03'}")#");
				assertEquals("3", "#DayOfWeek("{ts '2000-2-22 17:26:03'}")#");
				assertEquals("4", "#DayOfWeek("{ts '2000-2-23 17:26:03'}")#");
				assertEquals("5", "#DayOfWeek("{ts '2000-2-24 17:26:03'}")#");
				assertEquals("6", "#DayOfWeek("{ts '2000-2-25 17:26:03'}")#");
				assertEquals("7", "#DayOfWeek("{ts '2000-2-26 17:26:03'}")#");
				assertEquals("1", "#DayOfWeek("{ts '2000-2-27 17:26:03'}")#");
				assertEquals("2", "#DayOfWeek("{ts '2000-2-28 17:26:03'}")#");
				assertEquals("3", "#DayOfWeek("{ts '2000-2-29 17:26:03'}")#");
				assertEquals("4", "#DayOfWeek("{ts '2000-3-1 17:26:03'}")#");
				assertEquals("5", "#DayOfWeek("{ts '2000-3-2 17:26:03'}")#");
				assertEquals("6", "#DayOfWeek("{ts '2000-3-3 17:26:03'}")#");
				assertEquals("7", "#DayOfWeek("{ts '2000-3-4 17:26:03'}")#");
				assertEquals("1", "#DayOfWeek("{ts '2000-3-5 17:26:03'}")#");
				assertEquals("2", "#DayOfWeek("{ts '2000-3-6 17:26:03'}")#");
				assertEquals("3", "#DayOfWeek("{ts '2000-3-7 17:26:03'}")#");
				assertEquals("4", "#DayOfWeek("{ts '2000-3-8 17:26:03'}")#");
				assertEquals("5", "#DayOfWeek("{ts '2000-3-9 17:26:03'}")#");
				assertEquals("6", "#DayOfWeek("{ts '2000-3-10 17:26:03'}")#");
				assertEquals("7", "#DayOfWeek("{ts '2000-3-11 17:26:03'}")#");
				assertEquals("1", "#DayOfWeek("{ts '2000-3-12 17:26:03'}")#");
				assertEquals("2", "#DayOfWeek("{ts '2000-3-13 17:26:03'}")#");
				assertEquals("3", "#DayOfWeek("{ts '2000-3-14 17:26:03'}")#");
				assertEquals("4", "#DayOfWeek("{ts '2000-3-15 17:26:03'}")#");
				assertEquals("5", "#DayOfWeek("{ts '2000-3-16 17:26:03'}")#");
				assertEquals("6", "#DayOfWeek("{ts '2000-3-17 17:26:03'}")#");
				assertEquals("7", "#DayOfWeek("{ts '2000-3-18 17:26:03'}")#");
				assertEquals("1", "#DayOfWeek("{ts '2000-3-19 17:26:03'}")#");
				assertEquals("2", "#DayOfWeek("{ts '2000-3-20 17:26:03'}")#");
				assertEquals("3", "#DayOfWeek("{ts '2000-3-21 17:26:03'}")#");
				assertEquals("4", "#DayOfWeek("{ts '2000-3-22 17:26:03'}")#");
				assertEquals("5", "#DayOfWeek("{ts '2000-3-23 17:26:03'}")#");
				assertEquals("6", "#DayOfWeek("{ts '2000-3-24 17:26:03'}")#");
				assertEquals("7", "#DayOfWeek("{ts '2000-3-25 17:26:03'}")#");
				assertEquals("1", "#DayOfWeek("{ts '2000-3-26 17:26:03'}")#");
				assertEquals("2", "#DayOfWeek("{ts '2000-3-27 17:26:03'}")#");
				assertEquals("3", "#DayOfWeek("{ts '2000-3-28 17:26:03'}")#");
				assertEquals("4", "#DayOfWeek("{ts '2000-3-29 17:26:03'}")#");
				assertEquals("5", "#DayOfWeek("{ts '2000-3-30 17:26:03'}")#");
				assertEquals("6", "#DayOfWeek("{ts '2000-3-31 17:26:03'}")#");
				assertEquals("7", "#DayOfWeek("{ts '2000-4-1 17:26:03'}")#");
				assertEquals("1", "#DayOfWeek("{ts '2000-4-2 17:26:03'}")#");
				assertEquals("2", "#DayOfWeek("{ts '2000-4-3 17:26:03'}")#");
				assertEquals("3", "#DayOfWeek("{ts '2000-4-4 17:26:03'}")#");
				assertEquals("4", "#DayOfWeek("{ts '2000-4-5 17:26:03'}")#");
				assertEquals("5", "#DayOfWeek("{ts '2000-4-6 17:26:03'}")#");
				assertEquals("6", "#DayOfWeek("{ts '2000-4-7 17:26:03'}")#");
				assertEquals("7", "#DayOfWeek("{ts '2000-4-8 17:26:03'}")#");
				assertEquals("1", "#DayOfWeek("{ts '2000-4-9 17:26:03'}")#");
				assertEquals("2", "#DayOfWeek("{ts '2000-4-10 17:26:03'}")#");
				assertEquals("3", "#DayOfWeek("{ts '2000-4-11 17:26:03'}")#");
				assertEquals("4", "#DayOfWeek("{ts '2000-4-12 17:26:03'}")#");
				assertEquals("5", "#DayOfWeek("{ts '2000-4-13 17:26:03'}")#");
				assertEquals("6", "#DayOfWeek("{ts '2000-4-14 17:26:03'}")#");
				assertEquals("7", "#DayOfWeek("{ts '2000-4-15 17:26:03'}")#");
				assertEquals("1", "#DayOfWeek("{ts '2000-4-16 17:26:03'}")#");
				assertEquals("2", "#DayOfWeek("{ts '2000-4-17 17:26:03'}")#");
				assertEquals("3", "#DayOfWeek("{ts '2000-4-18 17:26:03'}")#");
				assertEquals("4", "#DayOfWeek("{ts '2000-4-19 17:26:03'}")#");
				assertEquals("5", "#DayOfWeek("{ts '2000-4-20 17:26:03'}")#");
				assertEquals("6", "#DayOfWeek("{ts '2000-4-21 17:26:03'}")#");
				assertEquals("7", "#DayOfWeek("{ts '2000-4-22 17:26:03'}")#");
				assertEquals("1", "#DayOfWeek("{ts '2000-4-23 17:26:03'}")#");
				assertEquals("2", "#DayOfWeek("{ts '2000-4-24 17:26:03'}")#");
				assertEquals("3", "#DayOfWeek("{ts '2000-4-25 17:26:03'}")#");
				assertEquals("4", "#DayOfWeek("{ts '2000-4-26 17:26:03'}")#");
				assertEquals("5", "#DayOfWeek("{ts '2000-4-27 17:26:03'}")#");
				assertEquals("6", "#DayOfWeek("{ts '2000-4-28 17:26:03'}")#");
				assertEquals("7", "#DayOfWeek("{ts '2000-4-29 17:26:03'}")#");
				assertEquals("1", "#DayOfWeek("{ts '2000-4-30 17:26:03'}")#");
				assertEquals("2", "#DayOfWeek("{ts '2000-5-1 17:26:03'}")#");
				assertEquals("3", "#DayOfWeek("{ts '2000-5-2 17:26:03'}")#");
				assertEquals("4", "#DayOfWeek("{ts '2000-5-3 17:26:03'}")#");
				assertEquals("5", "#DayOfWeek("{ts '2000-5-4 17:26:03'}")#");
				assertEquals("6", "#DayOfWeek("{ts '2000-5-5 17:26:03'}")#");
				assertEquals("7", "#DayOfWeek("{ts '2000-5-6 17:26:03'}")#");
				assertEquals("1", "#DayOfWeek("{ts '2000-5-7 17:26:03'}")#");
				assertEquals("2", "#DayOfWeek("{ts '2000-5-8 17:26:03'}")#");
				assertEquals("3", "#DayOfWeek("{ts '2000-5-9 17:26:03'}")#");
				assertEquals("4", "#DayOfWeek("{ts '2000-5-10 17:26:03'}")#");
				assertEquals("5", "#DayOfWeek("{ts '2000-5-11 17:26:03'}")#");
				assertEquals("6", "#DayOfWeek("{ts '2000-5-12 17:26:03'}")#");
				assertEquals("7", "#DayOfWeek("{ts '2000-5-13 17:26:03'}")#");
				assertEquals("1", "#DayOfWeek("{ts '2000-5-14 17:26:03'}")#");
				assertEquals("2", "#DayOfWeek("{ts '2000-5-15 17:26:03'}")#");
				assertEquals("3", "#DayOfWeek("{ts '2000-5-16 17:26:03'}")#");
				assertEquals("4", "#DayOfWeek("{ts '2000-5-17 17:26:03'}")#");
				assertEquals("5", "#DayOfWeek("{ts '2000-5-18 17:26:03'}")#");
				assertEquals("6", "#DayOfWeek("{ts '2000-5-19 17:26:03'}")#");
				assertEquals("7", "#DayOfWeek("{ts '2000-5-20 17:26:03'}")#");
				assertEquals("1", "#DayOfWeek("{ts '2000-5-21 17:26:03'}")#");
				assertEquals("2", "#DayOfWeek("{ts '2000-5-22 17:26:03'}")#");
				assertEquals("3", "#DayOfWeek("{ts '2000-5-23 17:26:03'}")#");
				assertEquals("4", "#DayOfWeek("{ts '2000-5-24 17:26:03'}")#");
				assertEquals("5", "#DayOfWeek("{ts '2000-5-25 17:26:03'}")#");
				assertEquals("6", "#DayOfWeek("{ts '2000-5-26 17:26:03'}")#");
				assertEquals("7", "#DayOfWeek("{ts '2000-5-27 17:26:03'}")#");
				assertEquals("1", "#DayOfWeek("{ts '2000-5-28 17:26:03'}")#");
				assertEquals("2", "#DayOfWeek("{ts '2000-5-29 17:26:03'}")#");
				assertEquals("3", "#DayOfWeek("{ts '2000-5-30 17:26:03'}")#");
				assertEquals("4", "#DayOfWeek("{ts '2000-5-31 17:26:03'}")#");
				assertEquals("5", "#DayOfWeek("{ts '2000-6-1 17:26:03'}")#");
				assertEquals("6", "#DayOfWeek("{ts '2000-6-2 17:26:03'}")#");
				assertEquals("7", "#DayOfWeek("{ts '2000-6-3 17:26:03'}")#");
				assertEquals("1", "#DayOfWeek("{ts '2000-6-4 17:26:03'}")#");
				assertEquals("2", "#DayOfWeek("{ts '2000-6-5 17:26:03'}")#");
				assertEquals("3", "#DayOfWeek("{ts '2000-6-6 17:26:03'}")#");
				assertEquals("4", "#DayOfWeek("{ts '2000-6-7 17:26:03'}")#");
				assertEquals("5", "#DayOfWeek("{ts '2000-6-8 17:26:03'}")#");
				assertEquals("6", "#DayOfWeek("{ts '2000-6-9 17:26:03'}")#");
				assertEquals("7", "#DayOfWeek("{ts '2000-6-10 17:26:03'}")#");
				assertEquals("1", "#DayOfWeek("{ts '2000-6-11 17:26:03'}")#");
				assertEquals("2", "#DayOfWeek("{ts '2000-6-12 17:26:03'}")#");
				assertEquals("3", "#DayOfWeek("{ts '2000-6-13 17:26:03'}")#");
				assertEquals("4", "#DayOfWeek("{ts '2000-6-14 17:26:03'}")#");
				assertEquals("5", "#DayOfWeek("{ts '2000-6-15 17:26:03'}")#");
				assertEquals("6", "#DayOfWeek("{ts '2000-6-16 17:26:03'}")#");
				assertEquals("7", "#DayOfWeek("{ts '2000-6-17 17:26:03'}")#");
				assertEquals("1", "#DayOfWeek("{ts '2000-6-18 17:26:03'}")#");
				assertEquals("2", "#DayOfWeek("{ts '2000-6-19 17:26:03'}")#");
				assertEquals("3", "#DayOfWeek("{ts '2000-6-20 17:26:03'}")#");
				assertEquals("4", "#DayOfWeek("{ts '2000-6-21 17:26:03'}")#");
				assertEquals("5", "#DayOfWeek("{ts '2000-6-22 17:26:03'}")#");
				assertEquals("6", "#DayOfWeek("{ts '2000-6-23 17:26:03'}")#");
				assertEquals("7", "#DayOfWeek("{ts '2000-6-24 17:26:03'}")#");
				assertEquals("1", "#DayOfWeek("{ts '2000-6-25 17:26:03'}")#");
				assertEquals("2", "#DayOfWeek("{ts '2000-6-26 17:26:03'}")#");
				assertEquals("3", "#DayOfWeek("{ts '2000-6-27 17:26:03'}")#");
				assertEquals("4", "#DayOfWeek("{ts '2000-6-28 17:26:03'}")#");
				assertEquals("5", "#DayOfWeek("{ts '2000-6-29 17:26:03'}")#");
				assertEquals("6", "#DayOfWeek("{ts '2000-6-30 17:26:03'}")#");
				assertEquals("7", "#DayOfWeek("{ts '2000-7-1 17:26:03'}")#");
				assertEquals("1", "#DayOfWeek("{ts '2000-7-2 17:26:03'}")#");
				assertEquals("2", "#DayOfWeek("{ts '2000-7-3 17:26:03'}")#");
				assertEquals("3", "#DayOfWeek("{ts '2000-7-4 17:26:03'}")#");
				assertEquals("4", "#DayOfWeek("{ts '2000-7-5 17:26:03'}")#");
				assertEquals("5", "#DayOfWeek("{ts '2000-7-6 17:26:03'}")#");
				assertEquals("6", "#DayOfWeek("{ts '2000-7-7 17:26:03'}")#");
				assertEquals("7", "#DayOfWeek("{ts '2000-7-8 17:26:03'}")#");
				assertEquals("1", "#DayOfWeek("{ts '2000-7-9 17:26:03'}")#");
				assertEquals("2", "#DayOfWeek("{ts '2000-7-10 17:26:03'}")#");
				assertEquals("3", "#DayOfWeek("{ts '2000-7-11 17:26:03'}")#");
				assertEquals("4", "#DayOfWeek("{ts '2000-7-12 17:26:03'}")#");
				assertEquals("5", "#DayOfWeek("{ts '2000-7-13 17:26:03'}")#");
				assertEquals("6", "#DayOfWeek("{ts '2000-7-14 17:26:03'}")#");
				assertEquals("7", "#DayOfWeek("{ts '2000-7-15 17:26:03'}")#");
				assertEquals("1", "#DayOfWeek("{ts '2000-7-16 17:26:03'}")#");
				assertEquals("2", "#DayOfWeek("{ts '2000-7-17 17:26:03'}")#");
				assertEquals("3", "#DayOfWeek("{ts '2000-7-18 17:26:03'}")#");
				assertEquals("4", "#DayOfWeek("{ts '2000-7-19 17:26:03'}")#");
				assertEquals("5", "#DayOfWeek("{ts '2000-7-20 17:26:03'}")#");
				assertEquals("6", "#DayOfWeek("{ts '2000-7-21 17:26:03'}")#");
				assertEquals("7", "#DayOfWeek("{ts '2000-7-22 17:26:03'}")#");
				assertEquals("1", "#DayOfWeek("{ts '2000-7-23 17:26:03'}")#");
				assertEquals("2", "#DayOfWeek("{ts '2000-7-24 17:26:03'}")#");
				assertEquals("3", "#DayOfWeek("{ts '2000-7-25 17:26:03'}")#");
				assertEquals("4", "#DayOfWeek("{ts '2000-7-26 17:26:03'}")#");
				assertEquals("5", "#DayOfWeek("{ts '2000-7-27 17:26:03'}")#");
				assertEquals("6", "#DayOfWeek("{ts '2000-7-28 17:26:03'}")#");
				assertEquals("7", "#DayOfWeek("{ts '2000-7-29 17:26:03'}")#");
				assertEquals("1", "#DayOfWeek("{ts '2000-7-30 17:26:03'}")#");
				assertEquals("2", "#DayOfWeek("{ts '2000-7-31 17:26:03'}")#");
				assertEquals("3", "#DayOfWeek("{ts '2000-8-1 17:26:03'}")#");
				assertEquals("4", "#DayOfWeek("{ts '2000-8-2 17:26:03'}")#");
				assertEquals("5", "#DayOfWeek("{ts '2000-8-3 17:26:03'}")#");
				assertEquals("6", "#DayOfWeek("{ts '2000-8-4 17:26:03'}")#");
				assertEquals("7", "#DayOfWeek("{ts '2000-8-5 17:26:03'}")#");
				assertEquals("1", "#DayOfWeek("{ts '2000-8-6 17:26:03'}")#");
				assertEquals("2", "#DayOfWeek("{ts '2000-8-7 17:26:03'}")#");
				assertEquals("3", "#DayOfWeek("{ts '2000-8-8 17:26:03'}")#");
				assertEquals("4", "#DayOfWeek("{ts '2000-8-9 17:26:03'}")#");
				assertEquals("5", "#DayOfWeek("{ts '2000-8-10 17:26:03'}")#");
				assertEquals("6", "#DayOfWeek("{ts '2000-8-11 17:26:03'}")#");
				assertEquals("7", "#DayOfWeek("{ts '2000-8-12 17:26:03'}")#");
				assertEquals("1", "#DayOfWeek("{ts '2000-8-13 17:26:03'}")#");
				assertEquals("2", "#DayOfWeek("{ts '2000-8-14 17:26:03'}")#");
				assertEquals("3", "#DayOfWeek("{ts '2000-8-15 17:26:03'}")#");
				assertEquals("4", "#DayOfWeek("{ts '2000-8-16 17:26:03'}")#");
				assertEquals("5", "#DayOfWeek("{ts '2000-8-17 17:26:03'}")#");
				assertEquals("6", "#DayOfWeek("{ts '2000-8-18 17:26:03'}")#");
				assertEquals("7", "#DayOfWeek("{ts '2000-8-19 17:26:03'}")#");
				assertEquals("1", "#DayOfWeek("{ts '2000-8-20 17:26:03'}")#");
				assertEquals("2", "#DayOfWeek("{ts '2000-8-21 17:26:03'}")#");
				assertEquals("3", "#DayOfWeek("{ts '2000-8-22 17:26:03'}")#");
				assertEquals("4", "#DayOfWeek("{ts '2000-8-23 17:26:03'}")#");
				assertEquals("5", "#DayOfWeek("{ts '2000-8-24 17:26:03'}")#");
				assertEquals("6", "#DayOfWeek("{ts '2000-8-25 17:26:03'}")#");
				assertEquals("7", "#DayOfWeek("{ts '2000-8-26 17:26:03'}")#");
				assertEquals("1", "#DayOfWeek("{ts '2000-8-27 17:26:03'}")#");
				assertEquals("2", "#DayOfWeek("{ts '2000-8-28 17:26:03'}")#");
				assertEquals("3", "#DayOfWeek("{ts '2000-8-29 17:26:03'}")#");
				assertEquals("4", "#DayOfWeek("{ts '2000-8-30 17:26:03'}")#");
				assertEquals("5", "#DayOfWeek("{ts '2000-8-31 17:26:03'}")#");
				assertEquals("6", "#DayOfWeek("{ts '2000-9-1 17:26:03'}")#");
				assertEquals("7", "#DayOfWeek("{ts '2000-9-2 17:26:03'}")#");
				assertEquals("1", "#DayOfWeek("{ts '2000-9-3 17:26:03'}")#");
				assertEquals("2", "#DayOfWeek("{ts '2000-9-4 17:26:03'}")#");
				assertEquals("3", "#DayOfWeek("{ts '2000-9-5 17:26:03'}")#");
				assertEquals("4", "#DayOfWeek("{ts '2000-9-6 17:26:03'}")#");
				assertEquals("5", "#DayOfWeek("{ts '2000-9-7 17:26:03'}")#");
				assertEquals("6", "#DayOfWeek("{ts '2000-9-8 17:26:03'}")#");
				assertEquals("7", "#DayOfWeek("{ts '2000-9-9 17:26:03'}")#");
				assertEquals("1", "#DayOfWeek("{ts '2000-9-10 17:26:03'}")#");
				assertEquals("2", "#DayOfWeek("{ts '2000-9-11 17:26:03'}")#");
				assertEquals("3", "#DayOfWeek("{ts '2000-9-12 17:26:03'}")#");
				assertEquals("4", "#DayOfWeek("{ts '2000-9-13 17:26:03'}")#");
				assertEquals("5", "#DayOfWeek("{ts '2000-9-14 17:26:03'}")#");
				assertEquals("6", "#DayOfWeek("{ts '2000-9-15 17:26:03'}")#");
				assertEquals("7", "#DayOfWeek("{ts '2000-9-16 17:26:03'}")#");
				assertEquals("1", "#DayOfWeek("{ts '2000-9-17 17:26:03'}")#");
				assertEquals("2", "#DayOfWeek("{ts '2000-9-18 17:26:03'}")#");
				assertEquals("3", "#DayOfWeek("{ts '2000-9-19 17:26:03'}")#");
				assertEquals("4", "#DayOfWeek("{ts '2000-9-20 17:26:03'}")#");
				assertEquals("5", "#DayOfWeek("{ts '2000-9-21 17:26:03'}")#");
				assertEquals("6", "#DayOfWeek("{ts '2000-9-22 17:26:03'}")#");
				assertEquals("7", "#DayOfWeek("{ts '2000-9-23 17:26:03'}")#");
				assertEquals("1", "#DayOfWeek("{ts '2000-9-24 17:26:03'}")#");
				assertEquals("2", "#DayOfWeek("{ts '2000-9-25 17:26:03'}")#");
				assertEquals("3", "#DayOfWeek("{ts '2000-9-26 17:26:03'}")#");
				assertEquals("4", "#DayOfWeek("{ts '2000-9-27 17:26:03'}")#");
				assertEquals("5", "#DayOfWeek("{ts '2000-9-28 17:26:03'}")#");
				assertEquals("6", "#DayOfWeek("{ts '2000-9-29 17:26:03'}")#");
				assertEquals("7", "#DayOfWeek("{ts '2000-9-30 17:26:03'}")#");
				assertEquals("1", "#DayOfWeek("{ts '2000-10-1 17:26:03'}")#");
				assertEquals("2", "#DayOfWeek("{ts '2000-10-2 17:26:03'}")#");
				assertEquals("3", "#DayOfWeek("{ts '2000-10-3 17:26:03'}")#");
				assertEquals("4", "#DayOfWeek("{ts '2000-10-4 17:26:03'}")#");
				assertEquals("5", "#DayOfWeek("{ts '2000-10-5 17:26:03'}")#");
				assertEquals("6", "#DayOfWeek("{ts '2000-10-6 17:26:03'}")#");
				assertEquals("7", "#DayOfWeek("{ts '2000-10-7 17:26:03'}")#");
				assertEquals("1", "#DayOfWeek("{ts '2000-10-8 17:26:03'}")#");
				assertEquals("2", "#DayOfWeek("{ts '2000-10-9 17:26:03'}")#");
				assertEquals("3", "#DayOfWeek("{ts '2000-10-10 17:26:03'}")#");
				assertEquals("4", "#DayOfWeek("{ts '2000-10-11 17:26:03'}")#");
				assertEquals("5", "#DayOfWeek("{ts '2000-10-12 17:26:03'}")#");
				assertEquals("6", "#DayOfWeek("{ts '2000-10-13 17:26:03'}")#");
				assertEquals("7", "#DayOfWeek("{ts '2000-10-14 17:26:03'}")#");
				assertEquals("1", "#DayOfWeek("{ts '2000-10-15 17:26:03'}")#");
				assertEquals("2", "#DayOfWeek("{ts '2000-10-16 17:26:03'}")#");
				assertEquals("3", "#DayOfWeek("{ts '2000-10-17 17:26:03'}")#");
				assertEquals("4", "#DayOfWeek("{ts '2000-10-18 17:26:03'}")#");
				assertEquals("5", "#DayOfWeek("{ts '2000-10-19 17:26:03'}")#");
				assertEquals("6", "#DayOfWeek("{ts '2000-10-20 17:26:03'}")#");
				assertEquals("7", "#DayOfWeek("{ts '2000-10-21 17:26:03'}")#");
				assertEquals("1", "#DayOfWeek("{ts '2000-10-22 17:26:03'}")#");
				assertEquals("2", "#DayOfWeek("{ts '2000-10-23 17:26:03'}")#");
				assertEquals("3", "#DayOfWeek("{ts '2000-10-24 17:26:03'}")#");
				assertEquals("4", "#DayOfWeek("{ts '2000-10-25 17:26:03'}")#");
				assertEquals("5", "#DayOfWeek("{ts '2000-10-26 17:26:03'}")#");
				assertEquals("6", "#DayOfWeek("{ts '2000-10-27 17:26:03'}")#");
				assertEquals("7", "#DayOfWeek("{ts '2000-10-28 17:26:03'}")#");
				assertEquals("1", "#DayOfWeek("{ts '2000-10-29 17:26:03'}")#");
				assertEquals("2", "#DayOfWeek("{ts '2000-10-30 17:26:03'}")#");
				assertEquals("3", "#DayOfWeek("{ts '2000-10-31 17:26:03'}")#");
				assertEquals("4", "#DayOfWeek("{ts '2000-11-1 17:26:03'}")#");
				assertEquals("5", "#DayOfWeek("{ts '2000-11-2 17:26:03'}")#");
				assertEquals("6", "#DayOfWeek("{ts '2000-11-3 17:26:03'}")#");
				assertEquals("7", "#DayOfWeek("{ts '2000-11-4 17:26:03'}")#");
				assertEquals("1", "#DayOfWeek("{ts '2000-11-5 17:26:03'}")#");
				assertEquals("2", "#DayOfWeek("{ts '2000-11-6 17:26:03'}")#");
				assertEquals("3", "#DayOfWeek("{ts '2000-11-7 17:26:03'}")#");
				assertEquals("4", "#DayOfWeek("{ts '2000-11-8 17:26:03'}")#");
				assertEquals("5", "#DayOfWeek("{ts '2000-11-9 17:26:03'}")#");
				assertEquals("6", "#DayOfWeek("{ts '2000-11-10 17:26:03'}")#");
				assertEquals("7", "#DayOfWeek("{ts '2000-11-11 17:26:03'}")#");
				assertEquals("1", "#DayOfWeek("{ts '2000-11-12 17:26:03'}")#");
				assertEquals("2", "#DayOfWeek("{ts '2000-11-13 17:26:03'}")#");
				assertEquals("3", "#DayOfWeek("{ts '2000-11-14 17:26:03'}")#");
				assertEquals("4", "#DayOfWeek("{ts '2000-11-15 17:26:03'}")#");
				assertEquals("5", "#DayOfWeek("{ts '2000-11-16 17:26:03'}")#");
				assertEquals("6", "#DayOfWeek("{ts '2000-11-17 17:26:03'}")#");
				assertEquals("7", "#DayOfWeek("{ts '2000-11-18 17:26:03'}")#");
				assertEquals("1", "#DayOfWeek("{ts '2000-11-19 17:26:03'}")#");
				assertEquals("2", "#DayOfWeek("{ts '2000-11-20 17:26:03'}")#");
				assertEquals("3", "#DayOfWeek("{ts '2000-11-21 17:26:03'}")#");
				assertEquals("4", "#DayOfWeek("{ts '2000-11-22 17:26:03'}")#");
				assertEquals("5", "#DayOfWeek("{ts '2000-11-23 17:26:03'}")#");
				assertEquals("6", "#DayOfWeek("{ts '2000-11-24 17:26:03'}")#");
				assertEquals("7", "#DayOfWeek("{ts '2000-11-25 17:26:03'}")#");
				assertEquals("1", "#DayOfWeek("{ts '2000-11-26 17:26:03'}")#");
				assertEquals("2", "#DayOfWeek("{ts '2000-11-27 17:26:03'}")#");
				assertEquals("3", "#DayOfWeek("{ts '2000-11-28 17:26:03'}")#");
				assertEquals("4", "#DayOfWeek("{ts '2000-11-29 17:26:03'}")#");
				assertEquals("5", "#DayOfWeek("{ts '2000-11-30 17:26:03'}")#");
				assertEquals("6", "#DayOfWeek("{ts '2000-12-1 17:26:03'}")#");
				assertEquals("7", "#DayOfWeek("{ts '2000-12-2 17:26:03'}")#");
				assertEquals("1", "#DayOfWeek("{ts '2000-12-3 17:26:03'}")#");
				assertEquals("2", "#DayOfWeek("{ts '2000-12-4 17:26:03'}")#");
				assertEquals("3", "#DayOfWeek("{ts '2000-12-5 17:26:03'}")#");
				assertEquals("4", "#DayOfWeek("{ts '2000-12-6 17:26:03'}")#");
				assertEquals("5", "#DayOfWeek("{ts '2000-12-7 17:26:03'}")#");
				assertEquals("6", "#DayOfWeek("{ts '2000-12-8 17:26:03'}")#");
				assertEquals("7", "#DayOfWeek("{ts '2000-12-9 17:26:03'}")#");
				assertEquals("1", "#DayOfWeek("{ts '2000-12-10 17:26:03'}")#");
				assertEquals("2", "#DayOfWeek("{ts '2000-12-11 17:26:03'}")#");
				assertEquals("3", "#DayOfWeek("{ts '2000-12-12 17:26:03'}")#");
				assertEquals("4", "#DayOfWeek("{ts '2000-12-13 17:26:03'}")#");
				assertEquals("5", "#DayOfWeek("{ts '2000-12-14 17:26:03'}")#");
				assertEquals("6", "#DayOfWeek("{ts '2000-12-15 17:26:03'}")#");
				assertEquals("7", "#DayOfWeek("{ts '2000-12-16 17:26:03'}")#");
				assertEquals("1", "#DayOfWeek("{ts '2000-12-17 17:26:03'}")#");
				assertEquals("2", "#DayOfWeek("{ts '2000-12-18 17:26:03'}")#");
				assertEquals("3", "#DayOfWeek("{ts '2000-12-19 17:26:03'}")#");
				assertEquals("4", "#DayOfWeek("{ts '2000-12-20 17:26:03'}")#");
				assertEquals("5", "#DayOfWeek("{ts '2000-12-21 17:26:03'}")#");
				assertEquals("6", "#DayOfWeek("{ts '2000-12-22 17:26:03'}")#");
				assertEquals("7", "#DayOfWeek("{ts '2000-12-23 17:26:03'}")#");
				assertEquals("1", "#DayOfWeek("{ts '2000-12-24 17:26:03'}")#");
				assertEquals("2", "#DayOfWeek("{ts '2000-12-25 17:26:03'}")#");
				assertEquals("3", "#DayOfWeek("{ts '2000-12-26 17:26:03'}")#");
				assertEquals("4", "#DayOfWeek("{ts '2000-12-27 17:26:03'}")#");
				assertEquals("5", "#DayOfWeek("{ts '2000-12-28 17:26:03'}")#");
				assertEquals("6", "#DayOfWeek("{ts '2000-12-29 17:26:03'}")#");
				assertEquals("7", "#DayOfWeek("{ts '2000-12-30 17:26:03'}")#");
				assertEquals("1", "#DayOfWeek("{ts '2000-12-31 17:26:03'}")#");
			});
		});
	}
}