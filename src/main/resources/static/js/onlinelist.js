function updateOnlineList() {
    $.ajax({
        url : '/data/server/status',
        success : function(res) {
            setOnlineList(res);
            setTimeout(updateOnlineList, 10000);
        },
        error: function () {
			setTimeout(updateOnlineList, 30000);
		},
        timeout: 3000
    });
}

function updateOnlineListSpecial() {
    $.ajax({
        url : '/data/server/status',
        success : function(res) {
            setOnlineList(res);
        },
        timeout: 3000
    });
}

function setOnlineList(res) {
	if(res == "ERROR") return;
	var data = res.split(String.fromCharCode(31));
	var online = (data[0]==1?"online":"offline");
	var html = "<p><b>Server ist " + online + "</b></p><br>";
	var count = parseInt(data[data.length - 1]);
	if (count > 0) {
		html += "<p><b>Spieler online (" + count + "):</b></p>";
		for (var i = 0; i < count; i++) {
			var id = data[i+1];
			var player = playerListOnline[id];
			if(player != null)
			{
				html += "<p style='background-image: url(\"/skins/" + player.name + "/head.png\");' class='player'>" + player.name + "</p>";
			} else {
				
			}
		}
	} else {
		html += "<p><b>Es sind keine Spieler online</b></p>";
	}
	$("#serveronlineimg").attr("src", "/img/server" + online + ".png");
	$("#onlineamount").html(count);
	$("#serverstatus").html(html);
}

$(document).ready(function() {
	setTimeout(updateOnlineList, 10000);
});
