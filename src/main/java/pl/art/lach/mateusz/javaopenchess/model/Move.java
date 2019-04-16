package pl.art.lach.mateusz.javaopenchess.model;

public class Move {
    private final long session;
    private final String startSquare;
    private final String endSquare;
    private final String description;

    public Move()
    {
        this.session = -1;
        this.startSquare = "";
        this.endSquare = "";
        this.description = "";
    }

    public Move(long sessionID, String startSquare, String endSquare, String description) {
        this.session = sessionID;
        this.startSquare = startSquare;
        this.endSquare = endSquare;
        this.description = description;
    }

    //getters for Jackson JSON library that turns objects into JSON
    public long getSession() { return session; }
    public String getStartSquare() { return startSquare; }
    public String getEndSquare() { return endSquare; }
    public String getDescription() { return description; }
}
