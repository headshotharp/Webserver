var lastid = 0;
var shouldscroll = true;
var logout = false;

function updateChat() {
    $.ajax({
        url : '/data/chat/since/' + window.lastid,
        success : function(res) {
            addChat(res);
            setTimeout(updateChat, 3000);
        },
        error: function () {
			setTimeout(updateChat, 20000);
		},
        timeout: 3000
    });
}

function updateChatSpecial() {
    $.ajax({
        url : '/data/chat/since/' + window.lastid,
        success : function(res) {
            addChat(res);
        },
        timeout: 3000
    });
}

function initChat() {
    $.ajax({
        url : '/data/chat/init',
        success : function(res) {
            addChat(res);
            var c = document.getElementById("chat-content");
            c.scrollTop = c.scrollHeight;
        },
        timeout: 10000
    });
}

function clearChat() {
    document.getElementById("chat-content").innerHTML = "";
}

function addChat(res) {
    var c = document.getElementById("chat-content");
    if (res == "ERROR") {
        c.innerHTML = '<p>Du bist ausgeloggt. Bitte logge dich <a href="/login">hier</a> wieder ein.</p>';
        window.logout = true;
        return;
    }
    if(window.logout){
        window.logout = false;
        initChat();
        return;
    }
    window.shouldscroll = (c.scrollTop === (c.scrollHeight - c.offsetHeight));
    var html = c.innerHTML;
    var data = res.split(String.fromCharCode(31));
    var count = parseInt(data[data.length - 1]);
    var updateOnlineList = false;
    if (count > 0) {
        for (var i = 0; i < count; i++) {
            var x = i * 6;
            window.lastid = data[x];
            var name = data[x + 1];
            // var msg = unescape(data[x + 2]);
            var msg = data[x + 2];
            var url = data[x + 3];
            var origin = data[x + 4];
            var timestamp = data[x + 5];
            if (origin == 0) {
                html += "<div class='chat-item'><table class='chat-item-info'><tr><td></td><td><div class='nameplate'><p></p></div></td><td><div class='timeplate'><p>" + timestamp + "</p></div></td></tr></table><table class=\"chat-item-text\"><tr><td><p>" + msg + "</p></td></tr></table></div>";
                updateOnlineList = true;
            } else {
                if (origin == 1) {
                    html += "<div class='chat-item'><table class='chat-item-info'><tr><td><img src='" + url + "' /></td><td><div class='nameplate'><p>&nbsp;<font color='#efbb00'><small>[Web]</small></font> <i>" + name + "</i></p></div></td><td><div class='timeplate'><p>" + timestamp + "</p></div></td></tr></table><table class=\"chat-item-text\"><tr><td><p>" + msg + "</p></td></tr></table></div>";
                } else {
                    html += "<div class='chat-item'><table class='chat-item-info'><tr><td><img src='" + url + "' /></td><td><div class='nameplate'><p>" + name + "</p></div></td><td><div class='timeplate'><p>" + timestamp + "</p></div></td></tr></table><table class=\"chat-item-text\"><tr><td><p>" + msg + "</p></td></tr></table></div>";
                }
            }
        }
        c.innerHTML = html;
        if (window.shouldscroll)
            c.scrollTop = c.scrollHeight;
    }
    if(updateOnlineList) {
    	updateOnlineListSpecial();
    }
}

function sendChat() {
	var msg = document.getElementById("chat-input-field").value;
	if(msg.length > 0)
	{
	    $.ajax({
			type: "POST",
			url: "/data/chat/post",
			data: {
				'msg': document.getElementById("chat-input-field").value
			},
			success: function(data) {
				document.getElementById("chat-input-field").value = "";
				updateChatSpecial();
			},
			error: function(data) {
				document.getElementById("chat-input-field").value = "Fehler beim Senden der Nachricht";
			},
			dataType: 'text'
		});
	}
}

$(document).ready(function() {
	initChat();
	setTimeout(updateChat, 3000);
});