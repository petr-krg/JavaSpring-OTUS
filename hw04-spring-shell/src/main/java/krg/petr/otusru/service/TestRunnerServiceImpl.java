package krg.petr.otusru.service;

import krg.petr.otusru.domain.Student;
import krg.petr.otusru.domain.TestResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TestRunnerServiceImpl implements TestRunnerService {

    private final TestService testService;

    @Override
    public TestResult run(Student student) {

        return testService.executeTestFor(student);
    }
}
