function updateProgressbar(id, val, max) {
	blocks = val % max;
	val = blocks / max;
	var progressbar = document.getElementById(id);
	progressbar.getElementsByTagName("b")[1].innerHTML = max;
	var progress = progressbar.getElementsByTagName("div")[1];
	var rel = progressbar.getElementsByTagName("p")[0];
	var abs = progressbar.getElementsByTagName("b")[0];
	progress.style.width = (val * 100) + "%";
	rel.innerHTML = parseInt(val * 100) + "%";
	abs.innerHTML = blocks;
}

function updateServerProgressbar(id, val) {
	var max = 1000000;
	var blocks = val;
	val = blocks / max;
	var progressbar = document.getElementById(id);
	if (val > 1) {
		val = 1;
		progressbar.getElementsByClassName("progressbarabsolute")[0].innerHTML = "<b></b>";
	} else {
		progressbar.getElementsByClassName("progressbarabsolute")[0].innerHTML = "<b></b> von <b></b>";
		progressbar.getElementsByTagName("b")[1].innerHTML = max;
	}
	var progress = progressbar.getElementsByTagName("div")[1];
	var rel = progressbar.getElementsByTagName("p")[0];
	var abs = progressbar.getElementsByTagName("b")[0];
	progress.style.width = (val * 100) + "%";
	rel.innerHTML = parseInt(val * 100) + "%";
	abs.innerHTML = blocks;
}

function updateUserProgressbars() {
	$.ajax({
		url : '/data/me/blockbreakplacetoday',
		success : function(res) {
			if (res == "ERROR") {
				return;
			}
			var res = res.split(String.fromCharCode(31));
			// break
			var res_b = parseInt(res[0]);
			document.getElementById('userblockbreaktoday').innerHTML = res_b;
			var max_b = 1000;
			if (res_b >= 50000) {
				max_b = 1000000;
			} else if (res_b >= 20000) {
				max_b = 5000;
			}
			updateProgressbar("progressbar_break", res_b, max_b);
			// place
			var res_p = parseInt(res[1]);
			document.getElementById('userblockplacetoday').innerHTML = res_p;
			var max_p = 1000;
			if (res_p >= 50000) {
				max_p = 1000000;
			} else if (res_p >= 20000) {
				max_p = 5000;
			}
			updateProgressbar("progressbar_place", res_p, max_p);
			setTimeout(updateUserProgressbars, 5010);
		},
		error : function() {
			setTimeout(updateUserProgressbars, 20000);
		},
		timeout : 3000
	});
}

function updateServerProgressbars() {
	$.ajax({
		url : '/data/server/blockbreakplacemonth',
		success : function(res) {
			if (res == "ERROR") {
				return;
			}
			var res = res.split(String.fromCharCode(31));
			var res_b = parseInt(res[0]);
			var res_p = parseInt(res[1]);
			updateServerProgressbar("progressbar_break", res_b);
			updateServerProgressbar("progressbar_place", res_p);
			setTimeout(updateServerProgressbars, 5010);
		},
		error : function() {
			setTimeout(updateServerProgressbars, 20000);
		},
		timeout : 3000
	});
}