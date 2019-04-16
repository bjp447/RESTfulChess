package pl.art.lach.mateusz.javaopenchess;

import pl.art.lach.mateusz.javaopenchess.model.Response;

/*
* implementing class must have @Service above class declaration.
* implementers should not use inherit from implementing class,
*   should be declared final
*/
public interface IChessEngine
{
    Response newGame(boolean firstMove);
    Response move(String start, String end, long sessionId);
    Response quit(long session);
    Response save(long sessionId);
    Response load(long sessionId);
}