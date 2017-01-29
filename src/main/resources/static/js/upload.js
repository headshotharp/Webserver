function uploadFile() {
	$("#upload-result").html('Datei wird hochgeladen...');
	$.ajax({
		url: "/upload",
		type: "POST",
		data: new FormData($("#upload-file-form")[0]),
		enctype: 'multipart/form-data',
		processData: false,
		contentType: false,
		cache: false,
		success: function () {
			$("#upload-result").html('Erfolgreich hochgeladen');
		},
		error: function () {
			$("#upload-result").html('Fehler beim Hochladen');
		}
	});
}

$(document).ready(function() {
	$("#upload-file-input").on("change", uploadFile);
});