package cucumber.api;

import cucumber.api.PendingException;
import cucumber.api.Result;
import cucumber.api.Scenario;
import cucumber.api.TestStep;
import cucumber.api.event.TestStepFinished;
import cucumber.api.event.TestStepStarted;
import cucumber.runner.EventBus;
import cucumber.runtime.DefinitionMatch;
import cucumber.runtime.StopWatch;
import gherkin.GherkinDialect;
import org.junit.Test;
import org.mockito.InOrder;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.doThrow;

public class TestStepTest {
    private final EventBus bus = mock(EventBus.class);
    private final GherkinDialect i18n = mock(GherkinDialect.class);
    private final Scenario scenario = mock(Scenario.class);
    private final DefinitionMatch definitionMatch = mock(DefinitionMatch.class);
    private final TestStep step = new TestStep(definitionMatch, StopWatch.SYSTEM);

    @Test
    public void run_wraps_run_step_in_test_step_started_and_finished_events() throws Throwable {
        step.run(bus, i18n, scenario, false);

        InOrder order = inOrder(bus, definitionMatch);
        order.verify(bus).send(isA(TestStepStarted.class));
        order.verify(definitionMatch).runStep(i18n, scenario);
        order.verify(bus).send(isA(TestStepFinished.class));
    }

    @Test
    public void run_does_dry_run_step_when_skip_steps_is_true() throws Throwable {
        step.run(bus, i18n, scenario, true);

        InOrder order = inOrder(bus, definitionMatch);
        order.verify(bus).send(isA(TestStepStarted.class));
        order.verify(definitionMatch).dryRunStep(i18n, scenario);
        order.verify(bus).send(isA(TestStepFinished.class));
    }

    @Test
    public void result_is_passed_when_step_definition_does_not_throw_exception() throws Throwable {
        Result result = step.run(bus, i18n, scenario, false);

        assertEquals(Result.PASSED, result.getStatus());
    }

    @Test
    public void result_is_skipped_when_skip_step_is_true() throws Throwable {
        Result result = step.run(bus, i18n, scenario, true);

        assertEquals(Result.SKIPPED, result);
    }

    @Test
    public void result_is_failed_when_step_definition_throws_exception() throws Throwable {
        doThrow(RuntimeException.class).when(definitionMatch).runStep((GherkinDialect)any(), (Scenario)any());

        Result result = step.run(bus, i18n, scenario, false);

        assertEquals(Result.FAILED, result.getStatus());
    }

    @Test
    public void result_is_pending_when_step_definition_throws_pending_exception() throws Throwable {
        doThrow(PendingException.class).when(definitionMatch).runStep((GherkinDialect)any(), (Scenario)any());

        Result result = step.run(bus, i18n, scenario, false);

        assertEquals(Result.PENDING, result.getStatus());
    }

    @Test
    public void step_execution_time_is_measured() throws Throwable {
        Long duration = new Long(1234);
        TestStep step = new TestStep(definitionMatch, new StopWatch.Stub(duration));

        Result result = step.run(bus, i18n, scenario, false);

        assertEquals(duration, result.getDuration());
    }

}