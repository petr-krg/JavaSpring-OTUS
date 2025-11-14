package krg.petr.otusru.service;

import krg.petr.otusru.domain.Student;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.never;
import static org.mockito.BDDMockito.inOrder;


@SpringBootTest
@TestPropertySource(properties = "spring.shell.interactive.enabled=false")
@DisplayName("Сервис StudentServiceImpl")
class StudentServiceImplTest {

    @Autowired
    private StudentServiceImpl service;

    @MockitoBean
    private LocalizedIOService ioService;

    @Nested
    @DisplayName("Позитивные сценарии")
    class HappyPath {

        @Test
        @DisplayName("определяет студента по вводу имени и фамилии")
        void determineCurrentStudent_success() {
            given(ioService.readStringWithPrompt("Please input your first name")).willReturn("Joshua");
            given(ioService.readStringWithPrompt("Please input your last name")).willReturn("Bloch");

            Student student = service.determineCurrentStudent();

            assertThat(student).isNotNull();
            assertThat(student.firstName()).isEqualTo("Joshua");
            assertThat(student.lastName()).isEqualTo("Bloch");

            then(ioService).should().readStringWithPrompt("Please input your first name");
            then(ioService).should().readStringWithPrompt("Please input your last name");
            then(ioService).shouldHaveNoMoreInteractions();
        }

        @Test
        @DisplayName("запрашивает имя и фамилию в правильном порядке")
        void determineCurrentStudent_order() {
            given(ioService.readStringWithPrompt("Please input your first name")).willReturn("Ada");
            given(ioService.readStringWithPrompt("Please input your last name")).willReturn("Lovelace");

            service.determineCurrentStudent();

            InOrder inOrder = inOrder(ioService);
            inOrder.verify(ioService).readStringWithPrompt("Please input your first name");
            inOrder.verify(ioService).readStringWithPrompt("Please input your last name");
            then(ioService).shouldHaveNoMoreInteractions();
        }
    }

    @Nested
    @DisplayName("Негативные сценарии")
    class ErrorPath {

        @Test
        @DisplayName("пробрасывает исключение, если ввод имени завершился ошибкой")
        void determineCurrentStudent_firstNameFails() {
            given(ioService.readStringWithPrompt("Please input your first name"))
                    .willThrow(new RuntimeException("IO error on first name"));

            assertThatThrownBy(service::determineCurrentStudent)
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("IO error");

            then(ioService).should().readStringWithPrompt("Please input your first name");
            then(ioService).should(never()).readStringWithPrompt("Please input your last name");
            then(ioService).shouldHaveNoMoreInteractions();
        }

        @Test
        @DisplayName("пробрасывает исключение, если ввод фамилии завершился ошибкой")
        void determineCurrentStudent_lastNameFails() {
            given(ioService.readStringWithPrompt("Please input your first name")).willReturn("Linus");
            given(ioService.readStringWithPrompt("Please input your last name"))
                    .willThrow(new IllegalStateException("IO error on last name"));

            assertThatThrownBy(service::determineCurrentStudent)
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("IO error");

            then(ioService).should().readStringWithPrompt("Please input your first name");
            then(ioService).should().readStringWithPrompt("Please input your last name");
            then(ioService).shouldHaveNoMoreInteractions();
        }
    }
}