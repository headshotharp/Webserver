function preview()
{
	title = document.getElementById('txt_title').value;
	if(title.length == 0) title = "[Kein Titel]";
	msg = document.getElementById('txt_msg').value;
	if(msg.length == 0) msg = "[Keine Nachricht]";
	document.getElementById('preview').innerHTML = "<div class='news'><p class='timeplate'>9.11.2002</p><h1>" + title + "</h1><br />" + nl2br(msg) + "</div>";
}

function nl2br(text)
{
	return text.replace(/(?:\r\n|\r|\n)/g, '<br />');
}
