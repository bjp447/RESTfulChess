package pl.art.lach.mateusz.javaopenchess.core;

//import javafx.util.Pair;
import pl.art.lach.mateusz.javaopenchess.core.ai.AI;
import pl.art.lach.mateusz.javaopenchess.core.ai.AIFactory;
import pl.art.lach.mateusz.javaopenchess.core.moves.Move;
import pl.art.lach.mateusz.javaopenchess.core.moves.MovesHistory;
import pl.art.lach.mateusz.javaopenchess.core.pieces.Piece;
import pl.art.lach.mateusz.javaopenchess.core.pieces.PieceFactory;
import pl.art.lach.mateusz.javaopenchess.core.pieces.implementation.King;
import pl.art.lach.mateusz.javaopenchess.core.players.Player;
import pl.art.lach.mateusz.javaopenchess.core.players.PlayerFactory;
import pl.art.lach.mateusz.javaopenchess.core.players.PlayerType;
import pl.art.lach.mateusz.javaopenchess.utils.GameModes;
import pl.art.lach.mateusz.javaopenchess.utils.GameTypes;
import pl.art.lach.mateusz.javaopenchess.utils.Settings;
import pl.art.lach.mateusz.javaopenchess.utils.SettingsFactory;

/*
* The class responsible for game behavior and functionality.
* No chessboard representation is offered through this class,
* just raw functionality.
*
 */
public class Game
{
    private Chessboard chessboard;
    private MovesHistory moves;
    private Player activePlayer;
    private AI ai;
    private Settings settings;
    private boolean boardBlocked;
    private boolean isEndOfGame;

    public Game()
    {
        this.settings = new Settings();
        this.moves = new MovesHistory(this);
        this.chessboard = new Chessboard(this.settings, this.moves);
        this.activePlayer = null;
        this.ai = null;
        this.boardBlocked = false;
        this.isEndOfGame = false;
    }

    // Methods to resolve internal engine calls.
    // Not all of these methods are necessary or used in this striped down version.
    public void newGame() { this.newGame(false); }
    public MovesHistory getMoves() { return moves; }
    public void setActivePlayer(Player player) { this.activePlayer = player; }
    public void setBlockedChessboard(boolean b) { this.boardBlocked = b; }
    public Player getActivePlayer() { return activePlayer; }
    public Chessboard getChessboard() { return chessboard; }
    public Settings getSettings() { return settings; }
    public void setSettings(Settings settings) { this.settings = settings; }

    //
    public boolean simulateMove(int beginX, int beginY, int endX, int endY, String promoted)
    {
        try
        {
            Square begin = getChessboard().getSquare(beginX, beginY);
            Square end = getChessboard().getSquare(endX, endY);
            getChessboard().select(begin);
            Piece activePiece = getChessboard().getActiveSquare().getPiece();
            if (activePiece.getAllMoves().contains(end)) // move
            {
                getChessboard().move(begin, end);
                if (null != promoted && !"".equals(promoted))
                {
                    Piece promotedPiece = PieceFactory.getPiece(getChessboard(), end.getPiece().getPlayer().getColor(),
                            promoted, activePlayer);
                    end.setPiece(promotedPiece);
                }
            } else
            {
//                LOG.debug(
//                        String.format("Bad move: beginX: %s beginY: %s endX: %s endY: %s", beginX, beginY, endX, endY));
                return false;
            }
            getChessboard().unselect();
            nextMove();

            return true;

        } catch (StringIndexOutOfBoundsException | ArrayIndexOutOfBoundsException | NullPointerException exc)
        {
//            LOG.error("simulateMove error: ", exc);
            return false;
        }
    }

    // Switch the active player from white to black, and vice versa.
    public void switchActivePlayer()
    {
        if (this.activePlayer == this.settings.getPlayerWhite()) {
            this.activePlayer = this.settings.getPlayerBlack();
        } else {
            this.activePlayer = this.settings.getPlayerWhite();
        }
        //game clock?
    }

    // Switches the active player and blocks board interaction for a player.
    private void nextMove()
    {
        this.switchActivePlayer();
        //LOG here
        if (this.activePlayer.getPlayerType() == PlayerType.LOCAL_USER) {
            this.boardBlocked = false;
        } else if (activePlayer.getPlayerType() == PlayerType.COMPUTER) {
            this.boardBlocked = true;
        }
    }

    // AI does a random move and readys the next move.
    private void doComputerMove()
    {
        Move lastMove = this.moves.getLastMoveFromHistory();
        Move move = this.ai.getMove(this, lastMove); //getMove lvl 1,2 doesnt use lastMove

        this.chessboard.move(move.getFrom(), move.getTo());
        if (move.getPromotedPiece() != null) {
            move.getTo().setPiece(move.getPromotedPiece());
        }
        this.nextMove();
    }

    // Check if the CPU can do a move.
    // Game must be ongoing, game must be against a CPU,
    // the active player must be the CPU, and the game must have an AI.
    private boolean canDoComputerMove()
    {
        return (!this.isEndOfGame && this.settings.isGameVersusComputer()
                && this.activePlayer.getPlayerType() == PlayerType.COMPUTER
                && this.ai != null);
    }

    // Starts a new game with a human vs cpu.
    // CPU may make a move depending on firstMove.

    public void newGame(boolean firstMove)
    {
        //init the players based on who goes first
        if (firstMove) //firstMove == true - player (white) goes first
        {
            this.settings.setPlayerWhite(PlayerFactory.getInstance("client", Colors.WHITE, PlayerType.LOCAL_USER));
            this.settings.setPlayerBlack(PlayerFactory.getInstance("cpu", Colors.BLACK, PlayerType.COMPUTER));
            this.settings.setUpsideDown(true); //player on top
        } else //player (black) goes after computer
        {
            this.settings.setPlayerWhite(PlayerFactory.getInstance("cpu", Colors.WHITE, PlayerType.COMPUTER));
            this.settings.setPlayerBlack(PlayerFactory.getInstance("client", Colors.BLACK, PlayerType.LOCAL_USER));
            this.settings.setUpsideDown(false);
        }
//        this.settings.setUpsideDown(false);
        this.ai = AIFactory.getAI(2); //difficulty level 2 AI.

        this.settings.setGameMode(GameModes.NEW_GAME);
        this.settings.setGameType(GameTypes.LOCAL); //not really local, just not using in-engine networking
        //TODO: time?
//        this.settings.setTimeForGame();
        this.activePlayer = settings.getPlayerWhite();

        //set up the chessboard.
        this.chessboard.setPieces4NewGame(this.settings.getPlayerWhite(), settings.getPlayerBlack());

        //block the board if the CPU is making move
        if (this.activePlayer.getPlayerType() != PlayerType.LOCAL_USER) {
            this.boardBlocked = true;
        }

        //have CPU make first move
        if (this.settings.isGameVersusComputer()
                && this.settings.getPlayerWhite().getPlayerType() == PlayerType.COMPUTER)
        {
            doComputerMove();
        }
    }

    // Checks if the start and ending moves are within the range of a chessboard.
    private boolean isMoveInRange(String move)
    {
        return ((move.length() >= 2) &&
                (move.charAt(0) <= 'h' && move.charAt(0) >= 'a') &&
                (move.charAt(1) <= '8' && move.charAt(1) >= '1'));
    }

    // Checks if a square has a piece on it and is owned by the active player.
    private boolean cannotInvokeMoveAction(Square sq)
    {
        return ((sq == null || sq.piece == null) && getChessboard().getActiveSquare() == null)
                || (this.getChessboard().getActiveSquare() == null && sq.piece != null
                && sq.getPiece().getPlayer() != this.activePlayer);
    }

    // Checks if a square is a valid destination based on the
    // currently selected square's possible destinations.
    private boolean canInvokeMoveAction(Square sq)
    {
        Square activeSq = getChessboard().getActiveSquare();
        return activeSq != null && activeSq.piece != null && activeSq.getPiece().getAllMoves().contains(sq);
    }

    // Attempt to move the requested piece via standard chessboard indices notation.
    //
    public String move(String startPos, String endPos)
    {
        String description = "Invalid Move";

        if (this.boardBlocked) {
            return description;
        }

        //check if requested positions are in range.
        if (!(this.isMoveInRange(startPos) && this.isMoveInRange(endPos))) {
            return description;
        }

        //Get the numeric notation for the move.
        //flip y values. Squares are 0 down to 8,
        // instead of how they are on the board
        int x1 = (startPos.charAt(0) - 'a');
        int y1 = 8 - Character.getNumericValue(startPos.charAt(1));
        int x2 = (endPos.charAt(0) - 'a');
        int y2 = 8 - Character.getNumericValue(endPos.charAt(1));

        //get squares from the chessboard
        Square startSq = chessboard.getSquare(x1, y1);
        Square endSq = chessboard.getSquare(x2, y2);
        //check if the src square is empty, select it if not empty
        if (cannotInvokeMoveAction(startSq)) {
            return description;
        }
        chessboard.unselect();
        chessboard.select(startSq);

        //check that the dest square is valid based on the src square's piece
        if (!canInvokeMoveAction(endSq)) {
            return description;
        }

        //do the actual move here. The move is also stored in MoveHistory var
        this.chessboard.move(chessboard.getActiveSquare(),
                chessboard.getSquare(x2, y2));
        chessboard.unselect();

        // switch player
        this.nextMove();

        // check checkmate or stalemate or continue game
        King king;
        if (this.settings.getPlayerWhite() == activePlayer) {
            king = this.chessboard.getKingWhite();
        } else {
            king = this.chessboard.getKingBlack();
        }
        switch (king.getKingState()) {
            case CHECKMATED:
                this.endGame(String.format("Checkmate! %s player loses!", king.getPlayer().getColor().toString()));
                description = String.format("Checkmate! %s player loses!", king.getPlayer().getColor().toString());
                return description;
            case STEALMATED:
                this.endGame("Stalemate! Draw!");
                description = "Stalemate! Draw!";
                return description;
            case FINE:
                break;
        }
        //do computer move if the game is still ongoing
        if (canDoComputerMove()) {
            doComputerMove();
            description = "CPU Moved";
        }
        return description;
    }

    // "End" the game. Sets vars to prevent further move attempts.
    public void endGame(String message)
    {
        this.boardBlocked = true;
        this.isEndOfGame = true;
    }
}
