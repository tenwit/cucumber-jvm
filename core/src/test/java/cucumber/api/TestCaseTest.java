package cucumber.api;

import cucumber.api.Result;
import cucumber.api.Scenario;
import cucumber.api.TestCase;
import cucumber.api.TestStep;
import cucumber.api.event.TestCaseFinished;
import cucumber.api.event.TestCaseStarted;
import cucumber.runner.EventBus;
import gherkin.GherkinDialect;
import gherkin.pickles.Pickle;
import org.junit.Test;
import org.mockito.InOrder;

import java.util.Arrays;

import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TestCaseTest {

    @Test
    public void run_wraps_execute_in_test_case_started_and_finished_events() throws Throwable {
        EventBus bus = mock(EventBus.class);
        GherkinDialect i18n = mock(GherkinDialect.class);
        TestStep testStep = mock(TestStep.class);
        when(testStep.run(eq(bus), eq(i18n), isA(Scenario.class), anyBoolean())).thenReturn(Result.UNDEFINED);

        TestCase testCase = new TestCase(Arrays.asList(testStep), mock(Pickle.class));
        testCase.run(bus, i18n);

        InOrder order = inOrder(bus, testStep);
        order.verify(bus).send(isA(TestCaseStarted.class));
        order.verify(testStep).run(eq(bus), eq(i18n), isA(Scenario.class), eq(false));
        order.verify(bus).send(isA(TestCaseFinished.class));
    }

    @Test
    public void run_all_steps() throws Throwable {
        EventBus bus = mock(EventBus.class);
        GherkinDialect i18n = mock(GherkinDialect.class);
        TestStep testStep1 = mock(TestStep.class);
        when(testStep1.run(eq(bus), eq(i18n), isA(Scenario.class), anyBoolean())).thenReturn(resultWithStatus(Result.PASSED));
        TestStep testStep2 = mock(TestStep.class);
        when(testStep2.run(eq(bus), eq(i18n), isA(Scenario.class), anyBoolean())).thenReturn(resultWithStatus(Result.PASSED));

        TestCase testCase = new TestCase(Arrays.asList(testStep1, testStep2), mock(Pickle.class));
        testCase.run(bus, i18n);

        InOrder order = inOrder(testStep1, testStep2);
        order.verify(testStep1).run(eq(bus), eq(i18n), isA(Scenario.class), eq(false));
        order.verify(testStep2).run(eq(bus), eq(i18n), isA(Scenario.class), eq(false));
    }

    @Test
    public void skip_steps_after_the_first_non_passed_result() throws Throwable {
        EventBus bus = mock(EventBus.class);
        GherkinDialect i18n = mock(GherkinDialect.class);
        TestStep testStep1 = mock(TestStep.class);
        when(testStep1.run(eq(bus), eq(i18n), isA(Scenario.class), anyBoolean())).thenReturn(Result.UNDEFINED);
        TestStep testStep2 = mock(TestStep.class);
        when(testStep2.run(eq(bus), eq(i18n), isA(Scenario.class), anyBoolean())).thenReturn(Result.SKIPPED);

        TestCase testCase = new TestCase(Arrays.asList(testStep1, testStep2), mock(Pickle.class));
        testCase.run(bus, i18n);

        InOrder order = inOrder(testStep1, testStep2);
        order.verify(testStep1).run(eq(bus), eq(i18n), isA(Scenario.class), eq(false));
        order.verify(testStep2).run(eq(bus), eq(i18n), isA(Scenario.class), eq(true));
    }

    private Result resultWithStatus(String status) {
        return new Result(status, null, null);
    }
}