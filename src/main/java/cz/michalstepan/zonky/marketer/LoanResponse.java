package cz.michalstepan.zonky.marketer;

import lombok.Data;

import java.util.List;

@Data
class LoanResponse {
    private int total;
    private List<Loan> loans;
}
