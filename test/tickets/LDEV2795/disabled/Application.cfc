component {
	this.name = 'tachyon-error-sameFormFieldsAsArray-false';
	this.sameFormFieldsAsArray = false;

	public function onRequestStart() {
		setting requesttimeout=10;
	}
}