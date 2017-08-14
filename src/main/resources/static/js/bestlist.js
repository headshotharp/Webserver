function numberWithCommas(x) {
	return x.toString().replace(/\B(?=(\d{3})+(?!\d))/g, ".");
}

function formatDate(str) {

	var t = str.split(/[- :]/);
	var d = new Date(t[0], t[1] - 1, t[2], t[3], t[4], t[5]);
	return d.getDate() + '.' + (d.getMonth() + 1) + '.' + d.getFullYear();
}

function createTable(data) {
	var html = "<tr><th><p><a href='javascript:void(0);' onClick='sortName();'>Name</a></p></th><th><p><a href='javascript:void(0);' onClick='sortBreak();'>Abgebaute Blöcke</a></p></th><th><p><a href='javascript:void(0);' onClick='sortPlace();'>Platzierte Blöcke</a></p></th><th><p><a href='javascript:void(0);' onClick='sortMoney();'>Guthaben</a></p></th><th><p><a href='javascript:void(0);' onClick='sortDate();'>Beitrittsdatum</a></p></th></tr>";
	for (var i = 0; i < data.length; i++) {
		html += getRow(data[i]);
	}
	document.getElementById('bestlist').innerHTML = html;
}

function getRow(data) {
	var html = "<tr";
	if (data.id == loggedinUserid)
		html += " style='background-color:#bfbfbf;'";
	return html + "><td class='name' style=\"background-image: url('/skins/"
			+ data.name + "/head.png');\"><p>" + data.prefix + ' ' + data.name
			+ "</p></td><td class='broken'><p>"
			+ numberWithCommas(data.block_break)
			+ " <b>&or;</b></p></td><td class='placed'><p>"
			+ numberWithCommas(data.block_place)
			+ " <b>&and;</b></p></td><td class='money'><p>"
			+ (data.money<0? "<b><font color='red'>" + numberWithCommas(data.money) + " &real;$</font></b>" : numberWithCommas(data.money) + " &real;$")
			+ "</p></td><td class='joined'><p>"
			+ formatDate(data.date) + "</p></td></tr>";
}

$(document).ready(function() {
	sortDate();
	createTable(data);
});

var sort_name = function(a, b) {
	if (a.name < b.name)
		return -1;
	if (a.name > b.name)
		return 1;
	return 0;
}

var sort_block_break = function(a, b) {
	return b.block_break - a.block_break;
}

var sort_block_place = function(a, b) {
	return b.block_place - a.block_place;
}

var sort_money = function(a, b) {
	return b.money - a.money;
}

var sort_date = function(a, b) {
	if (a.date < b.date)
		return -1;
	if (a.date > b.date)
		return 1;
	return 0;
}

function sortName() {
	data.sort(sort_name);
	createTable(data);
}

function sortBreak() {
	data.sort(sort_block_break);
	createTable(data);
}

function sortPlace() {
	data.sort(sort_block_place);
	createTable(data);
}

function sortMoney() {
	data.sort(sort_money);
	createTable(data);
}

function sortDate() {
	data.sort(sort_date);
	createTable(data);
}