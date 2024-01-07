package krg.petr.otusru.shell;

import krg.petr.otusru.config.AppConfig;
import krg.petr.otusru.domain.Student;
import krg.petr.otusru.domain.TestResult;
import krg.petr.otusru.service.LocalizedIOService;
import krg.petr.otusru.service.ResultService;
import krg.petr.otusru.service.StudentService;
import krg.petr.otusru.service.TestRunnerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.Availability;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellMethodAvailability;
import org.springframework.shell.standard.ShellOption;
import java.util.Locale;


@ShellComponent
public class ApplicationCommands {

    private final AppConfig appConfig;

    private final LocalizedIOService localizedIOService;

    private final TestRunnerService testRunnerService;

    private final StudentService studentService;

    private final ResultService resultService;
    
    private TestResult testResult;

    private Student student;

    @Autowired
    public ApplicationCommands(AppConfig appConfig, LocalizedIOService localizedIOService,
                               TestRunnerService testRunnerService, StudentService studentService,
                               ResultService resultService) {
        this.appConfig = appConfig;
        this.localizedIOService = localizedIOService;
        this.testRunnerService = testRunnerService;
        this.studentService = studentService;
        this.resultService = resultService;
    }

    @ShellMethod(value = "Show current locale command", key = "cl")
    public String showLocale() {
        Locale currentLocale = appConfig.getLocale();
        return getLocalizeText("command.current.locale", currentLocale.toString(), appConfig.getLocale());
    }

    @ShellMethod(value = "Set Locale command", key = "sl")
    public String setLocale(@ShellOption(defaultValue = "en-US") String locale) {
        appConfig.setLocale(locale);
        return getLocalizeText("command.change.locale", appConfig.getLocale());
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

    @ShellMethod(value = "Show test result command", key = "str")
    @ShellMethodAvailability(value = "isShowTestResultCommandAvailable")
    public void showResultTest() {
        resultService.showResult(testResult);
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
        return localizedIOService.getMessage(code,  args, appConfig.getLocale());
    }
}