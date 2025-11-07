package krg.petr.otusru.cmdrunner;

import krg.petr.otusru.service.TestRunnerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class TestAppRunner implements CommandLineRunner {

    private final TestRunnerService testRunnerService;

    @Override
    public void run(String... args) {
        log.info("Start: testRunnerService.run()");
        testRunnerService.run();
        log.info("Stop: testRunnerService.run()");
    }
}