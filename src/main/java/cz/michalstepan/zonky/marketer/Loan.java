package cz.michalstepan.zonky.marketer;

import lombok.Data;
import org.joda.time.DateTime;

@Data
public class Loan {
    private int id;
    private DateTime datePublished;
    private String borrowerNo;
    private String url;

    String prettyPrint() {
        return String.format("Loan id: %d, Date published: %s, Borrower: %s, URL: %s", id, datePublished, borrowerNo, url);
    }
}
