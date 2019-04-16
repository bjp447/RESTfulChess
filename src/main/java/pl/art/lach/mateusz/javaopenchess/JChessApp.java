package pl.art.lach.mateusz.javaopenchess;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class JChessApp
{
    public static void main(String[] args) {
        SpringApplication.run(JChessApp.class, args);
    }

    public void run(int port) {
        System.setProperty("server.port", Integer.toString(port));
        SpringApplication.run(JChessApp.class);
    }

    public void exit()
    {
//        SpringApplication.exit(this)
    }

}
