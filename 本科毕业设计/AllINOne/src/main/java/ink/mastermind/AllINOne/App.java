package ink.mastermind.AllINOne;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Hello world!
 * 无敌主程序入口
 *
 */
@SpringBootApplication
@EnableScheduling
public class App {
    public static void main( String[] args ) {
        System.out.println( "Hello World!" );
        SpringApplication.run(App.class, args);
    }
}

