package de.headshotharp.web.test;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.List;

import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;

import de.headshotharp.web.data.UserDataProvider;
import de.headshotharp.web.data.type.Player;

@Disabled
@SpringBootTest(webEnvironment = WebEnvironment.DEFINED_PORT)
public class Tester {
	@Autowired
	private UserDataProvider userDataProvider;

	@Value("${server.port}")
	private int port;

	private String baseUrl = null;

	@BeforeEach
	public void initialize() throws MalformedURLException {
		baseUrl = "http://localhost:" + port + "/";
	}

	@Test
	public void testConnection() throws InterruptedException, IOException {
		assertThat(port).isGreaterThan(0);
		assertThat(baseUrl).isNotNull();
		Response response = Jsoup.connect(baseUrl).execute();
		assertThat(response).isNotNull();
		assertThat(response.statusCode()).isEqualTo(200);
		Document doc = response.parse();
		assertThat(doc).isNotNull();
	}

	@Test
	public void testPlayerlist() throws IOException {
		Document doc = Jsoup.connect(baseUrl + "playerlist").get();
		assertThat(doc).isNotNull();
		Element elPlayerlist = doc.getElementsByClass("playerlist").first();
		assertThat(elPlayerlist).isNotNull();
		Elements elements = elPlayerlist.getElementsByClass("player");
		assertThat(elements).isNotNull();
		List<Player> players = userDataProvider.getPlayerListActive();
		assertThat(players).isNotNull();
		assertThat(elements.size()).isEqualTo(players.size());
	}
}
