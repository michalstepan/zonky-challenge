package cz.michalstepan.zonky.marketer;

import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;

@Slf4j
@Service
class LoansRestService {

    @Value("${app.zonky.header.sort}")
    private String headerSort;

    @Value("${app.zonky.header.page}")
    private String headerPage;

    @Value("${app.zonky.header.size}")
    private String headerSize;

    @Value("${app.zonky.header.total}")
    private String headerTotal;

    @Value("${app.zonky.url.loans}")
    private String urlLoans;

    @Autowired
    private RestTemplate restTemplateZonky;

    /**
     * Fetches a marketplace for loans published after given date.
     * <p>
     * Currently the API does not support ISO-8601 format (2016-08-20T23:59:59.000+02:00) but only date (2016-08-20).
     * Thus the data are queried per date basis.
     *
     * @param checkFromExclusive dateTime from which should be loans checked. Given time is included in search.
     * @param page               number of page to fetch
     * @param size               number of elements to fetch per page
     * @return list of loans published after given date
     */
    @Retryable(value = Throwable.class)
    public LoanResponse fetchMarketplace(DateTime checkFromExclusive, int page, int size) {
        LoanResponse loanResponse = new LoanResponse();
        HttpHeaders headers = new HttpHeaders();

        headers.add(headerSort, "-datePublished");
        headers.add(headerPage, String.valueOf(page));
        headers.add(headerSize, String.valueOf(size));

        final ResponseEntity<Loan[]> response = restTemplateZonky
                .exchange(String.format("%s?fields=%s%s",
                        urlLoans,
                        "id,datePublished,borrowerNo,url",
                        checkFromExclusive != null ? "&datePublished__gte=" + checkFromExclusive.toLocalDate() : null
                        ),
                        HttpMethod.GET,
                        new HttpEntity<>(headers),
                        Loan[].class);

        Loan[] rawLoans = response.getBody();

        List<Loan> fetchedLoans = rawLoans != null ? Arrays.asList(rawLoans) : List.of();
        loanResponse.setLoans(fetchedLoans);

        List<String> responseTotalRaw = response.getHeaders().get(headerTotal);
        if (responseTotalRaw != null && !responseTotalRaw.isEmpty()) {
            responseTotalRaw.stream()
                    .findFirst()
                    .map(s -> {
                        try {
                            return Integer.parseInt(s);
                        } catch (Exception e) {
                            throw new RuntimeException("Cannot parse data from " + headerTotal + " header in response. Data might be incomplete. Abort.");
                        }
                    })
                    .ifPresent(loanResponse::setTotal);
        }

        return loanResponse;
    }
}
