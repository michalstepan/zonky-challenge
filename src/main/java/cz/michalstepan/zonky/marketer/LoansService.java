package cz.michalstepan.zonky.marketer;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class LoansService {

    private final LoansRestService loansRestService;

    @Getter
    @Setter
    private DateTime lastCheck = DateTime.now().withTimeAtStartOfDay();


    @Value("${app.zonky.fetchSize:20}")
    private int fetchSize;

    @Autowired
    public LoansService(LoansRestService loansRestService) {
        this.loansRestService = loansRestService;
    }

    /**
     * This tasks regularly checks marketplace for new loans and prints them. Only today's and more recent
     * loans are returned.
     * <p>
     * Since the API returns data per whole date, loans are filtered out on server instead of on the API.
     */
    @Scheduled(fixedRateString = "${app.zonky.fetchInterval}")
    public void fetchMarketplaceAndPrintRecent() {
        DateTime currentCheckTimestamp = DateTime.now();

        List<Loan> fetchedLoans = fetchLoans(lastCheck);

        List<Loan> recentLoans = filterLoansByDatePublished(fetchedLoans, lastCheck);

        lastCheck = currentCheckTimestamp;

        printLoans(recentLoans);
    }

    /**
     * Fetches all loans from marketplace from lastCheck.
     *
     * @param lastCheck date of last check of marketplace loans
     * @return unmodifiable list of loans
     */
    public List<Loan> fetchLoans(DateTime lastCheck) {
        int page = 0;

        List<Loan> fetchedLoans = new ArrayList<>();
        int total = 0;

        do {
            LoanResponse loanResponse = loansRestService.fetchMarketplace(lastCheck, page++, fetchSize);
            fetchedLoans.addAll(loanResponse.getLoans());
            total = loanResponse.getTotal();
        } while (total > fetchedLoans.size());

        return fetchedLoans;
    }

    private List<Loan> filterLoansByDatePublished(List<Loan> loans, DateTime datePublishedAfter) {
        return loans.stream()
                .filter(loan -> loan.getDatePublished().isAfter(datePublishedAfter))
                .collect(Collectors.toUnmodifiableList());
    }

    private void printLoans(List<Loan> loans) {
        log.info("New loans on marketplace ({})", loans.size());
        log.info("-------------------");

        if (loans.isEmpty()) {
            log.info("No loans.");
        }

        loans.stream()
                .map(Loan::prettyPrint)
                .forEach(log::info);
    }
}
