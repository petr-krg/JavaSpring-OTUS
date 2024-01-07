package krg.petr.otusru.service;

import krg.petr.otusru.domain.Student;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = StudentServiceImpl.class)
@DisplayName("Test StudentServiceImplTest")
public class StudentServiceImplTest {

    @MockBean
    private LocalizedIOService localizedIOService;

    @Autowired
    private StudentService studentService;

    @BeforeEach
    void setUp() {
        when(localizedIOService.readStringWithPromptLocalized("StudentService.input.first.name"))
                .thenReturn("Joshua");
        when(localizedIOService.readStringWithPromptLocalized("StudentService.input.last.name"))
                .thenReturn("Bloch");
    }

    @Test
    @DisplayName("Create Correct Student")
    public void createCorrectStudent() {
        Student expectedStudent = studentService.determineCurrentStudent();
        assertAll(
                () -> assertEquals("Joshua", expectedStudent.firstName()),
                () -> assertEquals("Bloch", expectedStudent.lastName())
        );
    }

    @Test
    @DisplayName("Get Student Full Name")
    public void getStudentFullName() {
        Student expectedStudent = studentService.determineCurrentStudent();
        assertEquals("Joshua Bloch", expectedStudent.getFullName());
    }
}