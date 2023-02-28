component {

	this.name = "tachyontest";
	this.datasources["LDEV2708"] = server.getDatasource("mssql");
	this.datasource = "LDEV2708";

	public function onRequestStart() {
		setting requesttimeout=10;
		query{
			echo("DROP TABLE IF EXISTS LDEV2708");
		}
		query{
			echo("CREATE TABLE LDEV2708( id int, when_created datetime)");
		}
	}

	public function onRequestEnd(){
		query{
			echo("DROP TABLE IF EXISTS LDEV2708");
		}
	}
}
