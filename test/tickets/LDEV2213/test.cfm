<cfscript>
	function doSomething (
	    any onComplete) {
	    writeOutput("I Love Tachyon!");
	    onComplete();
	}

	function test () {
	    var myVar = "";
	    writeOutput("Before: [#myVar#]");
	    doSomething(function () {
	        myVar = "testcase";
	    });
	    writeOutput("After: [#myVar#]");
	}
	test();
</cfscript>