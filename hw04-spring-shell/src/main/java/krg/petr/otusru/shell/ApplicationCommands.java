package krg.petr.otusru.shell;

import krg.petr.otusru.config.AppProperties;
import krg.petr.otusru.domain.Student;
import krg.petr.otusru.domain.TestResult;
import krg.petr.otusru.service.LocalizedIOService;
import krg.petr.otusru.service.ResultService;
import krg.petr.otusru.service.StudentService;
import krg.petr.otusru.service.TestRunnerService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.shell.Availability;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellMethodAvailability;
import org.springframework.shell.standard.ShellOption;
import java.util.Locale;

@ShellComponent
@RequiredArgsConstructor
public class ApplicationCommands {

    private final AppProperties appProperties;

    private final LocalizedIOService localizedIOService;

    private final TestRunnerService testRunnerService;

    private final StudentService studentService;

    private final ResultService resultService;

    private final ConfigurableApplicationContext context;

    private TestResult testResult;

    private Student student;

    @ShellMethod(value = "Show about command", key = {"a", "about"})
    public String showAbout() {
        Locale currentLocale = appProperties.getLocale();
        return getLocalizeText("command.about");
    }

    @ShellMethod(value = "Show current locale command", key = "cl")
    public String showLocale() {
        Locale currentLocale = appProperties.getLocale();
        return getLocalizeText("command.current.locale", currentLocale.toString(), appProperties.getLocale());
    }

    @ShellMethod(value = "Set Locale command", key = "sl")
    public String setLocale(@ShellOption(defaultValue = "en-US") String locale) {
        appProperties.setLocale(locale);
        return getLocalizeText("command.change.locale", appProperties.getLocale());
    }

    @ShellMethod(value = "Login command", key = {"l", "login"})
    public void login() {
        student = studentService.determineCurrentStudent();
    }

    @ShellMethod(value = "Run test command", key = {"r", "run"})
    @ShellMethodAvailability(value = "isRunTestCommandAvailable")
    public void runTest() {
        testResult = testRunnerService.run(student);
    }

    @ShellMethod(value = "Show test result command", key = {"s", "show"})
    @ShellMethodAvailability(value = "isShowTestResultCommandAvailable")
    public void showResultTest() {
        resultService.showResult(testResult);
    }

    @ShellMethod(value = "Full testing flow (login -> run -> show)", key = {"t", "test"})
    public void test() {
        login();
        runTest();
        showResultTest();
    }

    @ShellMethod(value = "Exit application", key = {"x", "exit"})
    public void exit() {
        localizedIOService.printLineLocalized("command.exit.bye");
        int code = SpringApplication.exit(context, () -> 0);
        System.exit(code);
    }

    private Availability isRunTestCommandAvailable() {
        return student == null
                ? Availability.unavailable(getLocalizeText("command.not.logged"))
                : Availability.available();
    }

    private Availability isShowTestResultCommandAvailable() {
        return testResult == null
                ? Availability.unavailable(getLocalizeText("command.not.tested"))
                : Availability.available();
    }

    private String getLocalizeText(String code, Object ...args) {
        return localizedIOService.getMessage(code, args);
    }
}