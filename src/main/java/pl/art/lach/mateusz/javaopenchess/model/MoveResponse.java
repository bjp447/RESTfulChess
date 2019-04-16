package pl.art.lach.mateusz.javaopenchess.model;

/*
* Response that describes a game and subsequent move made by a user/cpu
 */
public class MoveResponse implements Response
{
    private String status = "GOOD";
    private final Move move;

//    public MoveResponse(long sessionID, String description)
//    {
//        this.move = new Move(sessionID, "", "", description);
//    }

    public MoveResponse()
    {
        this.move = new Move();
    }

    public MoveResponse(long sessionID, String startSquare, String endSquare, String description) {
        this.move = new Move(sessionID, startSquare, endSquare, description);
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }
    public Move getMove() { return move; }
}
