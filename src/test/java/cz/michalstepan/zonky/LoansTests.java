package cz.michalstepan.zonky;

import cz.michalstepan.zonky.marketer.Loan;
import cz.michalstepan.zonky.marketer.LoansService;
import org.joda.time.DateTime;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.Assert;

import java.util.List;

@SpringBootTest
public class LoansTests {

    @Autowired
    public LoansService loansService;

    @Value("${app.zonky.fetchInterval}")
    public Long fetchInterval;

    @Test
    public void givenLatency2s__whenRegularlyFetchingLoans__thenLoansShouldBeFetched() throws InterruptedException {
        int latencyOffset = 2_000;

        DateTime lastCheckOnStart = loansService.getLastCheck();

        Thread.sleep(fetchInterval + latencyOffset);

        DateTime lastCheckOnEnd = loansService.getLastCheck();

        Assert.isTrue(lastCheckOnEnd.isAfter(lastCheckOnStart), "New check should be done by now");
    }

    @Test
    public void givenToday__whenFetchingLoans__thenNoError() {
        DateTime fetchAfter = DateTime.now().withTimeAtStartOfDay();

        List<Loan> loans = loansService.fetchLoans(fetchAfter);

        Assert.isTrue(true, "Loans are fetched");
    }

    @Test
    public void givenTomorrow__whenFetchingLoans__thenNoLoans() {
        DateTime fetchAfter = DateTime.now().plusDays(1).withTimeAtStartOfDay();

        List<Loan> loans = loansService.fetchLoans(fetchAfter);

        Assert.isTrue(loans.size() == 0, "Loan list for future should be empty");
    }
}
