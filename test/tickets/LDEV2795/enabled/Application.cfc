component {
	this.name = 'tachyon-error-sameFormFieldsAsArray';
	this.sameFormFieldsAsArray = true;

	public function onRequestStart() {
		setting requesttimeout=10;
	}
}