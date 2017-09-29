package de.headshotharp.web.controller;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Paths;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import de.headshotharp.web.Config;

@Controller
public class UploadController {
	@Autowired
	private Config config;

	@GetMapping("/upload")
	@ResponseBody
	String uploadGet() {
		return "<form method='post' action='/upload' id='upload-file-form'><label>Datei auswählen:</label><input class='form-control' id='upload-file-input' type='file' name='uploadfile' accept='*.png' /></form><p id='upload-result'></p><script src='/webjars/jquery/2.2.1/jquery.min.js'></script><script src='/js/upload.js'></script>";
	}

	@PostMapping("/upload")
	@ResponseBody
	String upload(HttpServletResponse response, @RequestParam("uploadfile") MultipartFile uploadfile) {
		try {
			String filename = uploadfile.getOriginalFilename();
			String directory = config.getPath().getUpload();
			String filepath = Paths.get(directory, filename).toString();
			BufferedOutputStream stream = new BufferedOutputStream(new FileOutputStream(new File(filepath)));
			stream.write(uploadfile.getBytes());
			stream.close();
			response.setStatus(HttpServletResponse.SC_ACCEPTED);
		} catch (Exception e) {
			e.printStackTrace();
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
		}
		return "";
	}
}
