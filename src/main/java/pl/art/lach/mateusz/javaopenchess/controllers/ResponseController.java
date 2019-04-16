package pl.art.lach.mateusz.javaopenchess.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.art.lach.mateusz.javaopenchess.IChessEngine;
import pl.art.lach.mateusz.javaopenchess.model.Response;

/*
* REST call class to handle call events
*/
@RestController
public class ResponseController
{
    /*
    * inject the chess engine class that will be responsible for functionally.
    * implementing class must have @Service above class declaration.
    */
    @Autowired IChessEngine engine;

    @GetMapping("/hello")
    public ResponseEntity<String> hello()
    {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return ResponseEntity.status(HttpStatus.CREATED)
                .headers(headers).body("hello user.");
    }

    /*
    * Caller requests to create a new game.
    * optional firstMove param, default true = caller is white color, 'cpu' is black
    *
    * Usage example:
    * CLI: curl -i -X POST  <address:port>/new
    * or                    <address:port>/new?first=true
    * Browser (Postman): [POST] <address:port>/new
    * or                        <address:port>/new?first=false
    */
    @PostMapping("/new")
//    @ResponseStatus(value = HttpStatus.ACCEPTED)
    public ResponseEntity<Response> newGame(
            @RequestParam(name = "first", required = false,
                    defaultValue = "true") boolean firstMove)
    {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return ResponseEntity.status(HttpStatus.CREATED)
                .headers(headers).body(engine.newGame(firstMove));
    }

    /*
    * Caller requests to move a piece.
    * Caller must specify the starting and ending positions.
    *
    * Usage example:
    * CLI: curl -i -X PUT <address:port>/move/1/start="a1"&end="a2"
    * Browser (Postman): [PUT] <address:port>/move/1/start="a1"&end="a2"
    */
    @PutMapping("/move/{id}/start={st}&end={ed}")
    public ResponseEntity<Response> move(@PathVariable("st") String startSq,
                         @PathVariable("ed") String endSq,
                         @PathVariable("id") long sessionId)
    {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return ResponseEntity.status(HttpStatus.OK)
                .headers(headers).body(engine.move(startSq, endSq, sessionId));
    }

    /*
    * Caller requests to quit the current game they are playing.
    * Caller must specify the session ID.
    *
    * Usage example:
    * Browser (Postman): [DELETE] <address:port>/quit/1
    * CLI: curl -i -X DELETE <address:port>/quit/1
    */
    @DeleteMapping("/quit/{id}")
    public ResponseEntity<Response> quit(@PathVariable("id") long sessionId)
    {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return ResponseEntity.status(HttpStatus.OK)
                .headers(headers).body(engine.quit(sessionId));
    }

    @PutMapping("/save/{id}")
    public ResponseEntity<Response> save(@PathVariable("id") long sessionId) {
//        HttpStatus.NOT_IMPLEMENTED
        return null;
    }

    @PutMapping("/load/{id}")
    public ResponseEntity<Response> load(@PathVariable("id") long sessionId) {
        return null;
    }
}
