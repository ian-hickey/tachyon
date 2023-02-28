<cfparam name="FORM.scene" default="">
<cfscript>
    if( form.scene == 1 ){
        res = isArray(ORMExecuteQuery("From test where Ant = :ok",{"ok":'tachyon'}));
    }
    if( form.scene == 2 ){
        res = isArray(ORMExecuteQuery("From test where ant = 'tachyon'"));
    }
    if( form.scene == 3 ){
        try{
            res = isArray(ORMExecuteQuery("From test where ant = :ok",{"ok":'tachyon'}));
        }
        catch(any e){
            res = e.message;
        }
    }
    writeoutput(res);
</cfscript>
