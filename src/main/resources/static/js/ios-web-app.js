$(document).ready(function() {
	$("a:not([target]):not([role])").click(
		function()
		{
		    window.location = this.getAttribute("href");
		    return false;
		}
	);
});
