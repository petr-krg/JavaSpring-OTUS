package krg.petr.otusru.service;

import krg.petr.otusru.domain.Student;
import krg.petr.otusru.domain.TestResult;

public interface TestRunnerService {

    TestResult run(Student student);
}
