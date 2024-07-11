package krg.petr.otusru.commands;

import org.h2.tools.Console;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;

import java.sql.SQLException;

@ShellComponent
public class DataBaseCommands {

    @ShellMethod(value = "Open H2 web-console", key = "wc")
    public void openWebConsoleH2() throws SQLException {
        Console.main();
    }
}