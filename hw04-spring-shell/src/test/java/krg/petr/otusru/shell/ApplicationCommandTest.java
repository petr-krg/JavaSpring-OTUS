package krg.petr.otusru.shell;

import krg.petr.otusru.config.AppProperties;
import krg.petr.otusru.domain.Student;
import krg.petr.otusru.domain.TestResult;
import krg.petr.otusru.service.LocalizedIOService;
import krg.petr.otusru.service.ResultService;
import krg.petr.otusru.service.StudentService;
import krg.petr.otusru.service.TestRunnerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.shell.Availability;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@SpringBootTest(classes = ApplicationCommands.class)
@TestPropertySource(properties = "spring.shell.interactive.enabled=false")
@DisplayName("ApplicationCommand test")
public class ApplicationCommandTest {

    @MockitoBean
    private AppProperties appProperties;

    @MockitoBean
    private StudentService studentService;

    @MockitoBean
    private ResultService resultService;

    @MockitoBean
    private TestRunnerService runnerService;

    @MockitoBean
    private LocalizedIOService localizedIOService;

    @Autowired
    private ApplicationCommands commands;

    @BeforeEach
    void SetUp() {
        given(localizedIOService.getMessage(anyString(), any()))
                .willAnswer(inv -> "Mocked message for " + inv.getArgument(0, String.class));
        given(runnerService.run(any(Student.class)))
                .willAnswer(inv -> new TestResult(inv.getArgument(0, Student.class)));
    }

    @Test
    @DisplayName("sl: меняет локаль и выводит локализованное подтверждение")
    void setLocale_setsLocaleAndReturnsLocalizedAck() {
        given(appProperties.getLocale()).willReturn(Locale.forLanguageTag("ru-RU"));
        given(localizedIOService.getMessage(eq("command.change.locale"), any()))
                .willReturn("Mocked message for command.change.locale");
        String out = commands.setLocale("ru-RU");
        assertThat(out).isEqualTo("Mocked message for command.change.locale");
        then(appProperties).should().setLocale("ru-RU");
        ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);
        then(localizedIOService).should().getMessage(eq("command.change.locale"), captor.capture());
        Object passed = captor.getValue();
        boolean ok;
        if (passed instanceof Object[] arr && arr.length > 0) {
            Object a = arr[0];
            ok = "ru-RU".equals(a) || "ru_RU".equals(String.valueOf(a)) ||
                    (a instanceof Locale && ((Locale) a).toLanguageTag().equals("ru-RU"));
        } else {
            ok = "ru-RU".equals(passed) || "ru_RU".equals(String.valueOf(passed)) ||
                    (passed instanceof Locale && ((Locale) passed).toLanguageTag().equals("ru-RU"));
        }
        assertThat(ok).as("аргумент сообщения должен содержать ru-RU").isTrue();
        then(localizedIOService).shouldHaveNoMoreInteractions();
    }

    @Test
    @DisplayName("about: возвращает локализованное сообщение о программе")
    void about_returnsLocalizedMessage() {
        given(localizedIOService.getMessage(eq("command.about"), any(Object[].class)))
                .willReturn("Mocked message for command.about");
        String out = commands.showAbout();
        assertThat(out).isEqualTo("Mocked message for command.about");
        then(localizedIOService).should()
                .getMessage(eq("command.about"), any(Object[].class));
        then(localizedIOService).shouldHaveNoMoreInteractions();
    }

    @Test
    @DisplayName("login: вызывает сервис определения студента")
    void login_invokesStudentService() {
        Student mock = new Student("Joshua", "Bloch");
        given(studentService.determineCurrentStudent()).willReturn(mock);
        commands.login();
        then(studentService).should().determineCurrentStudent();
        then(studentService).shouldHaveNoMoreInteractions();
    }

    @Test
    @DisplayName("run + show: прогоняет тест и выводит результат")
    void run_then_show_invokesRunnerAndResultService() {
        Student s = new Student("Ada", "Lovelace");
        given(studentService.determineCurrentStudent()).willReturn(s);
        commands.login();
        commands.runTest();
        commands.showResultTest();
        then(runnerService).should().run(s);
        ArgumentCaptor<TestResult> captor = ArgumentCaptor.forClass(TestResult.class);
        then(resultService).should().showResult(captor.capture());
        assertThat(captor.getValue()).isNotNull();
        assertThat(captor.getValue().getStudent()).isEqualTo(s);
        then(runnerService).shouldHaveNoMoreInteractions();
        then(resultService).shouldHaveNoMoreInteractions();
    }

    @Test
    @DisplayName("run без login: команда должна быть недоступна")
    void run_without_login_isUnavailable() {
        given(localizedIOService.getMessage(anyString(), any(Object[].class)))
                .willAnswer(inv -> "Mocked message for " + inv.getArgument(0, String.class));
        Availability availability = ReflectionTestUtils.invokeMethod(
                commands, "isRunTestCommandAvailable");
        assertThat(availability).isNotNull();
        assertThat(availability.isAvailable()).isFalse();
        assertThat(availability.getReason()).isEqualTo("Mocked message for command.not.logged");
        then(runnerService).shouldHaveNoInteractions();
    }
}