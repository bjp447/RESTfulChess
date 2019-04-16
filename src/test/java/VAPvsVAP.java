import org.junit.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import pl.art.lach.mateusz.javaopenchess.JChessApp;
import pl.art.lach.mateusz.javaopenchess.model.DescriptionResponse;
import pl.art.lach.mateusz.javaopenchess.model.MoveResponse;

import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

public class VAPvsVAP
{
    private String host = "http://localhost";
    private RestTemplate restTemplate = new RestTemplate();

    private static final int P1 = 8080;
    private static final int P2 = 8081;

//    private String hello()
//    {
//        String url = String.format("%s:%s/hello", host, port);
//        HttpEntity<String> request = new HttpEntity<>(new String(""));
//        ResponseEntity<String> response = this.restTemplate.exchange(url, HttpMethod.POST, request, String.class);
//        assertThat(response).isNotEqualTo(null);
//        assertThat(response.getBody()).isNotEqualTo(null);
//        return (String)response.getBody();
//    }

    private MoveResponse newGameRequest(int port, boolean firstMove)
    {
        String url = String.format("%s:%s/new?first=%b", host, port, firstMove); //REST call string
        HttpEntity<MoveResponse> request = new HttpEntity<>(new MoveResponse());

        //make POST request for a MoveResponse
        ResponseEntity<MoveResponse> response = this.restTemplate.exchange(url, HttpMethod.POST, request, MoveResponse.class);

        assertThat(response).isNotEqualTo(null);
        assertThat(response.getBody()).isNotEqualTo(null);
        return (MoveResponse)response.getBody();
    }

    private MoveResponse moveRequest(int port, String start, String end, long sessionId)
    {
        String url = String.format("%s:%s/move/%d/start=%s&end=%s", host, port, sessionId, start, end); //REST call string
        HttpEntity<MoveResponse> request = new HttpEntity<>(new MoveResponse());

        //make PUT request for a MoveResponse
        ResponseEntity<MoveResponse> response = this.restTemplate.exchange(url, HttpMethod.PUT, request, MoveResponse.class);

        assertThat(response).isNotEqualTo(null);
        assertThat(response.getBody()).isNotEqualTo(null);
        return (MoveResponse)response.getBody();
    }

    private DescriptionResponse quitRequest(int port, long sessionId)
    {
        String url = String.format("%s:%s/quit/%d", host, port, sessionId); //REST call string

        HttpEntity<DescriptionResponse> request = new HttpEntity<>(new DescriptionResponse());
        ResponseEntity<DescriptionResponse> response = this.restTemplate.exchange(url, HttpMethod.DELETE, request, DescriptionResponse.class);

        assertThat(response).isNotEqualTo(null);
        assertThat(response.getBody()).isNotEqualTo(null);
        return (DescriptionResponse)response.getBody();
    }

    @Test
    public void VAPvsVAP() {
        //start both VAPs on different local ports
        JChessApp player1 = new JChessApp();
        player1.run(P1);
        JChessApp player2 = new JChessApp();
        player2.run(P2);

        int currPort = P1; //Port represents the player/VAP on the local network
        int turns = 0;

        //p2 goes first, p1's human is p2's CPU
        MoveResponse p1Response = newGameRequest(8080, true); //p1 human is white, on btm
        //no cpu move has been made
        assertThat(p1Response.getStatus()).isEqualTo("CREATED");

        MoveResponse p2Response = newGameRequest(8081, false); //p2 human is black, on top
        //cpu move has been made
        assertThat(p2Response.getStatus()).isEqualTo("GOOD");

        long startTime = System.currentTimeMillis();
        long elapsedTime = 0L;

        //main loop. Moves from one VAP is passed to the other VAP.
        while (true) {
            //timer of 2 minutes so that the game does not run forever
            if (elapsedTime >= 2*60*1000) {
                break;
            }
            ++turns;

            if (p2Response.getStatus().equalsIgnoreCase("good"))
            {
                p1Response = moveRequest(currPort, p2Response.getMove().getStartSquare(),
                        p2Response.getMove().getEndSquare(),
                        p2Response.getMove().getSession());
                p2Response = p1Response;

                //swap the currently used port.
                if (currPort == P1) {
                    ++currPort;
                } else {
                    --currPort;
                }
            }
            //game ending responses for a CPU vs CPU game.
            else if (p2Response.getStatus().equalsIgnoreCase("fin") ||
                    p2Response.getStatus().equalsIgnoreCase("invalid"))
            {
                turns /= 2; //a turn is two loop runs. The loop only takes care of one VAP at a time.
                //quit the games of both VAPs.
                DescriptionResponse response = quitRequest(P1, p2Response.getMove().getSession());
                assertThat(response.getDescription()).isEqualTo("Game Quit");

                response = quitRequest(P2, p2Response.getMove().getSession());
                assertThat(response.getDescription()).isEqualTo("Game Quit");
                break;
            }
            else { //if responses dont match the above cases then there is certainly a error.
                assert false;
            }
            elapsedTime = (new Date()).getTime() - startTime;
        }
        //If there are 0 turns, then there must be an issue with newGame(...)
        assertThat(turns).isGreaterThan(0);

//        RestTemplate restTemplate = new RestTemplate();
        System.out.println("sf");
    }

}
