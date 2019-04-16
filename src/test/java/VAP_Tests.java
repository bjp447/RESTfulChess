import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.context.junit4.SpringRunner;
import pl.art.lach.mateusz.javaopenchess.JChessApp;
import pl.art.lach.mateusz.javaopenchess.model.DescriptionResponse;
import pl.art.lach.mateusz.javaopenchess.model.MoveResponse;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = JChessApp.class)
public class VAP_Tests
{
    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate testRestTemplate;

    private String host = "http://localhost";

    private MoveResponse newGameRequest(boolean firstMove)
    {
        String url = String.format("%s:%s/new?first=%b", host, port, firstMove);
        HttpEntity<MoveResponse> request = new HttpEntity<>(new MoveResponse());
        ResponseEntity<MoveResponse> response = this.testRestTemplate.exchange(url, HttpMethod.POST, request, MoveResponse.class);
        assertThat(response).isNotEqualTo(null);
        assertThat(response.getBody()).isNotEqualTo(null);
        return (MoveResponse)response.getBody();
    }

    private MoveResponse moveRequest(String start, String end, long sessionId)
    {
        String url = String.format("%s:%s/move/%d/start=%s&end=%s", host, port, sessionId, start, end);
        HttpEntity<MoveResponse> request = new HttpEntity<>(new MoveResponse());
        ResponseEntity<MoveResponse> response = this.testRestTemplate.exchange(url, HttpMethod.PUT, request, MoveResponse.class);
        assertThat(response).isNotEqualTo(null);
        assertThat(response.getBody()).isNotEqualTo(null);
        return (MoveResponse) response.getBody();
    }

    private DescriptionResponse quitRequest(long sessionId)
    {
        String url = String.format("%s:%s/quit/%d", host, port, sessionId);
        HttpEntity<DescriptionResponse> request = new HttpEntity<>(new DescriptionResponse());
        ResponseEntity<DescriptionResponse> response = this.testRestTemplate.exchange(url, HttpMethod.DELETE, request, DescriptionResponse.class);
        assertThat(response).isNotEqualTo(null);
        assertThat(response.getBody()).isNotEqualTo(null);
        return (DescriptionResponse) response.getBody();
    }

    @Test
    public void newGameTest1()
    {
        this.testRestTemplate.getRestTemplate()
                .getMessageConverters().add(new MappingJackson2HttpMessageConverter());
        MoveResponse response = newGameRequest(true);
        assertThat(response.getStatus()).isEqualTo("CREATED");
        DescriptionResponse descriptionResponse = quitRequest(response.getMove().getSession());
    }

    @Test
    public void newGameTest2()
    {
        this.testRestTemplate.getRestTemplate()
                .getMessageConverters().add(new MappingJackson2HttpMessageConverter());
        MoveResponse response = newGameRequest(false);
        assertThat(response.getStatus()).isEqualTo("GOOD");
        DescriptionResponse descriptionResponse = quitRequest(response.getMove().getSession());
    }

    @Test
    public void moveTest()
    {
        this.testRestTemplate.getRestTemplate()
                .getMessageConverters().add(new MappingJackson2HttpMessageConverter());

        MoveResponse response = newGameRequest(true);
        assertThat(response.getStatus()).isEqualTo("CREATED");

        response = moveRequest("", "", response.getMove().getSession());
        assertThat(response.getStatus()).isEqualTo("INVALID");

        response = moveRequest("a2", "a4", response.getMove().getSession());
        assertThat(response.getStatus()).isEqualTo("GOOD");

        DescriptionResponse descriptionResponse = quitRequest(response.getMove().getSession());
    }

    @Test
    public void quitTest()
    {
        this.testRestTemplate.getRestTemplate()
                .getMessageConverters().add(new MappingJackson2HttpMessageConverter());

        MoveResponse response = newGameRequest(false);
        assertThat(response.getStatus()).isEqualTo("GOOD");

        DescriptionResponse descriptionResponse = quitRequest(response.getMove().getSession());
        assertThat(descriptionResponse.getDescription()).isNotEqualTo("");
    }

}
