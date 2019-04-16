Based on the chess engine from https://sourceforge.net/projects/javaopenchess/ published by matlak.

4/10/19


This project strips the UI from javaopenchess and provides a stateful RESTful interface using Spring Boot to access and interact with the chess engine instead. 

Oringinaly written as a homewrok assignment for a Cloud Computing course.


relevent packages/files:

.../main/java/

- IChessEngine

- JChessEngine

- JChessApp

.../controllers/

- ResponseController


.../model/

- Response

- DescriptionResponse

- Move

- MoveResponse


.../core

- Game


.../test/java/

- VAP_Testa

- VAPvsVAP


The Game class is a farily modified version of the Game class and several other class originally provided in javaopenchess, stripping UI elements. 


Chess Engine Interface:

public interface IChessEngine {
    Response newGame(boolean firstMove);
    Response move(String start, String end, long sessionId);
    Response quit(long session);
    Response save(long sessionId);
    Response load(long sessionId); }

MoveResponse json response:
{ 	"status": "GOOD",
    "move": {
        "session": 1,
        "startSquare": "",
        "endSquare": "",
        "description": ""
    }}

DescriptionResponse json response:
{ "description": "Game Quit" }

Response is an Interface used as a base for returns to rest callers. MoveResponse and DescriptionResponse implement Response.

ResponseController is the class that is responsable for rest call events via the Spring Boot api.
This class delegates functionality to a IChessEngine that is injected into this class.

JChessEngine delegates engine functionality to Game. It serves as a bridge between the actual chess engine and the REST api for a web service.
It contains a mapping between sessionIds and Games to allow for mulitple seasons. The history of moves for a game is provided in the chess engine and can be get via game.getMoves().

When making a new game, the response recieved may have a 'status' of CREATED and empty start,end,and description fields to indicate that the game has started but no movev has been made.
If the VAP CPU makes a move, these fileds will indicate the move, with a Status of GOOD.

When making moves, the MoveResponse recieved may have statuss indicating 'INVALID', 'FIN', 'GOOD'.
INVALID response indicate that the attempted move can not be made. 
FIN indicates that the game has ended in a Stalemate or in a Checkmate. 
GOOD indicates that the move was valid and that the response represents a CPU move.

To run the program on the command line- from the root project directory- first package it then run the jar.
> mvn package

> java -jar target/jChess-1.5.1.jar

To test VAPvsVAP  play, run:
> mvn -Dtest=VAPvsVAP test

VAPvsVAP runs the engine agaisnt itself using REST calls.
The other testing file should test during packaging.
NOTE: that the container uploaded to DockerHub do not have the same tests. It has VAPvsVAP only.

To run Osv:
> capstan build -p vbox -v Capstanfile

> capstan run -f 8080:8080 -v -p vbox

To run Docker container from source with created packaged jar:
> docker build -f Dockerfile -t rest_chess .

> dockerun 8080:8080 rest_chess

To get and run Docker container from docker hub:
> docker run -p 8080:8080 bjp447/brennan_pohl_hw4:first

Note, that the tag 'first' is neccesary for pulling.

Link to my Dockerhub repository: https://hub.docker.com/r/bjp447/brennan_pohl_hw4


See 'aws_run.txt' for pictures and explanations on a docker container run on AWS EC2.

To start a new game:

(<address:port>/new), (<address:port>/new?first={b}) 

ex. curl -i -X POST localhost:8080/new

Specifing first is optional. The default value for the parameter is true.

* CLI: curl -i -X POST  <address:port>/new
* or                    <address:port>/new?first=true
* Browser (Postman): [POST] <address:port>/new
* or                        <address:port>/new?first=false

To make a move:

(<address:port>/move/{id}/start={st}&end={ed})

ex. curl -i -X PUT localhost:8080/move/1/start="a1"&end="a2"

* CLI: curl -i -X PUT <address:port>/move/1/start="a1"&end="a2"
* Browser (Postman): [PUT] <address:port>/move/1/start="a1"&end="a2"

To quit a game:

(<address:port>/quit/{id})

ex. curl -i -X DELETE localhost:8080/quit/1

* Browser (Postman): [DELETE] <address:port>/quit/1
* CLI: curl -i -X DELETE <address:port>/quit/1





