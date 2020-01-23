package cz.michalstepan.zonky.marketer;

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

    @Autowired
    private LoansRestService loansRestService;

    private DateTime lastCheck = DateTime.now().withTimeAtStartOfDay();


    @Value("${app.zonky.fetchSize}")
    private int fetchSize;

    /**
     * This tasks regularly checks marketplace for new loans and prints them.
     * <p>
     * Since the API returns data per whole date, we need to filter out the recent loans manually on server.
     */
    @Scheduled(fixedRateString = "${app.zonky.fetchInterval}")
    public void fetchMarketplaceAndPrintRecent() {
        DateTime currentCheckTimestamp = DateTime.now();

        int page = 0;
        int total = 0;
        int size = fetchSize;

        List<Loan> fetchedLoans = new ArrayList<>();

        do {
            LoanResponse loanResponse = loansRestService.fetchMarketplace(lastCheck, page++, size);
            fetchedLoans.addAll(loanResponse.getLoans());
            total = loanResponse.getTotal();
        } while (total > fetchedLoans.size());

        List<Loan> recentLoans = fetchedLoans.stream()
                .filter(loan -> loan.getDatePublished().isAfter(lastCheck))
                .collect(Collectors.toUnmodifiableList());

        lastCheck = currentCheckTimestamp;

        printLoans(recentLoans);
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
