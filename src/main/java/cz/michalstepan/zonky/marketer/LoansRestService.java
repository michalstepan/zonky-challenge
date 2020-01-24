package cz.michalstepan.zonky.marketer;

import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Service
class LoansRestService {

    @Value("${app.zonky.rooturi}")
    private String rootUri;

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
     * @param datePublishedFromExclusive dateTime from which should be loans checked. Given time is included in search.
     * @param page                       number of page to fetch
     * @param size                       number of elements to fetch per page
     * @return list of loans published after given date
     */
    @Retryable(value = ResourceAccessException.class)
    public LoanResponse fetchMarketplace(DateTime datePublishedFromExclusive, int page, int size) {
        validateInputs(datePublishedFromExclusive, page, size);

        RequestEntity<String> request = prepareRequest(datePublishedFromExclusive, page, size);

        ResponseEntity<Loan[]> response = restTemplateZonky.exchange(request, Loan[].class);

        return parseResponse(response);
    }

    private void validateInputs(DateTime datePublishedFromExclusive, int page, int size) {
        if (datePublishedFromExclusive == null) {
            throw new RuntimeException("Mandatory parameter datePublished not filled.");
        }
        if (page < 0) {
            throw new RuntimeException("Wrong value of page parameter. Usable values: > 0");
        }
        if (size < 0) {
            throw new RuntimeException("Wrong value of size parameter. Usable values: > 0");
        }
    }

    private RequestEntity<String> prepareRequest(DateTime checkFromExclusive, int page, int size) {
        HttpHeaders headers = new HttpHeaders();

        addSortingHeaders(headers, "-datePublished");
        addPagingHeaders(headers, page, size);

        String url = String.format("%s%s%s",
                urlLoans,
                formatPartialFilter(List.of("id", "datePublished", "borrowerNo", "url")),
                formatPartialQuery("datePublished", "gte", checkFromExclusive.toLocalDate())
        );

        return new RequestEntity<>(headers, HttpMethod.GET, URI.create(rootUri + url));
    }

    private LoanResponse parseResponse(ResponseEntity<Loan[]> response) {
        LoanResponse loanResponse = new LoanResponse();
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
                            throw new RuntimeException("Cannot parse data from " + headerTotal + " header in response. Data might be incomplete.");
                        }
                    })
                    .ifPresent(loanResponse::setTotal);
        }

        return loanResponse;
    }

    private String formatPartialFilter(List<String> fields) {
        if (fields == null || fields.isEmpty()) {
            return null;
        }
        return String.format("?fields=%s", String.join(",", fields));
    }

    private String formatPartialQuery(String property, String activity, Object value) {
        return String.format("&%s__%s=%s", property, activity, value);
    }

    private void addSortingHeaders(HttpHeaders headers, String value) {
        addHeader(headers, headerSort, value);
    }

    private void addPagingHeaders(HttpHeaders headers, int page, int size) {
        addHeader(headers, headerPage, String.valueOf(page));
        addHeader(headers, headerSize, String.valueOf(size));
    }

    private void addHeader(HttpHeaders headers, String property, String value) {
        if (headers != null) {
            headers.add(property, value);
        }
    }
}
