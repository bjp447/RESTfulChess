package pl.art.lach.mateusz.javaopenchess;

//import javafx.util.Pair;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;
import pl.art.lach.mateusz.javaopenchess.core.Game;
import pl.art.lach.mateusz.javaopenchess.core.moves.Move;
import pl.art.lach.mateusz.javaopenchess.model.DescriptionResponse;
import pl.art.lach.mateusz.javaopenchess.model.MoveResponse;
import pl.art.lach.mateusz.javaopenchess.model.Response;

import java.util.HashMap;
import java.util.concurrent.atomic.AtomicLong;

@Service
public final class JChessEngine implements IChessEngine
{
    //logging
    public final static String LOG_FILE = "log4j.properties";
    private static final Logger LOG = Logger.getLogger(JChessEngine.class);
    //this var is needed for javaopenchess engine internals
    public final static String MAIN_PACKAGE_NAME = JChessEngine.class.getPackage().getName();

    //could be in interface, but implementers may use different names other than 'Game'
    private final HashMap<Long, Game> games = new HashMap<Long, Game>();
    private final AtomicLong counter = new AtomicLong();

    //get cpu's move
    private MoveResponse getCpuMove(long sessionId, Game game)
    {
        //get last move that happened in the game.
        //caller of this function should make sure that the cpu made a move
        //to get the correct functionality.
        Move cpu_move = game.getMoves().getLastMoveFromHistory();
        String fromStr = cpu_move.getFrom().getAlgebraicNotation();
        String toStr = cpu_move.getTo().getAlgebraicNotation();
        return new MoveResponse(sessionId, fromStr, toStr, "");
    }

    @Override
    public Response newGame(boolean firstMove)
    {
        //increments a counter for session identification.
        long c = counter.incrementAndGet();
        //create and setup new game.
        games.put(c, new Game());
        games.get(c).newGame(firstMove); //cpus will make their first move here

        if (firstMove) {
            //return description response to indicate success to user if user is white
            MoveResponse response = new MoveResponse(c, "", "","Game successfully created");
            response.setStatus("CREATED");
            return response;
        } else { //return cpu move if cpu is white
            MoveResponse response = this.getCpuMove(c, games.get(c));
            return response;
        }
    }

    @Override
    public Response move(String start, String end, long sessionId)
    {
        Game game = games.get(sessionId);
        if (game != null)
        {
            String description = game.move(start, end);

            if (description.equalsIgnoreCase("invalid move")) { //invalid move response
                MoveResponse response = new MoveResponse(sessionId,"", "", description);
                response.setStatus("INVALID");
                return response;
            }
            else { //end game response
                if (description.contains("Checkmate") || description.contains("Stalemate")) {
                    MoveResponse response = new MoveResponse(sessionId, "", "", description);
                    response.setStatus("FIN");
                    return response;
                }
                else { //valid move, cpu made move
                    //send cpu's move to user
                    return this.getCpuMove(sessionId, game);
                }
            }
        }
        //invalid session
        MoveResponse response = new MoveResponse(sessionId, "", "","Invalid Session");
        response.setStatus("INVALID");
        return response;
    }

    @Override
    public Response quit(long sessionId)
    {
        //remove from active games, save, report game status to user
        games.remove(sessionId);
        return new DescriptionResponse("Game Quit");
    }

    //TODO

    //save to db
    @Override
    public Response save(long sessionId) {
        return new DescriptionResponse("Implementation incomplete");
    }

    //load from db
    @Override
    public Response load(long sessionId) {
        return new DescriptionResponse("Implementation incomplete");
    }
}
