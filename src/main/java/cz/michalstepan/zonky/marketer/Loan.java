package cz.michalstepan.zonky.marketer;

import lombok.Data;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;

@Data
class Loan {

    private static final String PRINT_PATTERN = "dd.MM.yyyy HH:mm:ss";

    private int id;
    private DateTime datePublished;
    private String borrowerNo;
    private String url;

    String prettyPrint() {
        return String.format("Loan id: %d, Date published: %s, Borrower: %s, URL: %s", id, DateTimeFormat.forPattern(PRINT_PATTERN).print(datePublished), borrowerNo, url);
    }
}
