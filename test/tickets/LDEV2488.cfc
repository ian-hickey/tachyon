component extends="org.tachyon.cfml.test.TachyonTestCase" {

     function testIt() {
     	var tmp=duplicate(request);
     	try{
	     	request.a=1;
		    request.b=1;
		    request.c=1;
		    structClear(request);
     	}
     	finally {
     		loop struct=tmp index="local.k" item="local.v" {
     			request[k]=v;
     		}
     	}
     }


}