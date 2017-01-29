function updateSkins() {
	$("#updateallskins").html('Skins werden aktualisiert...');
    $.ajax({
        url : '/data/skins/update',
        success : function(res) {
			if (res == "ERROR") {
				$("#updateallskins").html('Du bist nicht berechtigt alle Skins zu aktualisieren.');
				return;
			}
			var data = res.split(String.fromCharCode(31));
			var count = parseInt(data[data.length - 1]);
			var html = "";
			if (count > 0) {
				html = "Es konnten " + count + " Skins nicht aktualisiert werden!<br />";
				for (var i = 0; i < count; i++) {
					html += data[x] + ",";
				}
			}else{
				html = "Es wurden alle Skins aktualisiert";
			}
            $("#updateallskins").html(html);
        },
        error: function () {
			$("#updateallskins").html('Fehler: Der Server konnte die Anfrage nicht bearbeiten!');
		},
        timeout: 90000
    });
}
