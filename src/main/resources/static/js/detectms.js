function detectIE() {
	var ua = window.navigator.userAgent;
	var msie = ua.indexOf('MSIE ');
	if (msie > 0)
		return true;
	var trident = ua.indexOf('Trident/');
	if (trident > 0)
		return true;
	var edge = ua.indexOf('Edge/');
	if (edge > 0)
		return true;
	return false;
}

if (detectIE()) {
	window.location = "/ms.html";
}