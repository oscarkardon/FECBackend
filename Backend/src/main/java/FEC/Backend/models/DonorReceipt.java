package FEC.Backend.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@NoArgsConstructor
@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)

public class DonorReceipt {
    private String contributor_name;
    private double contribution_receipt_amount;
    private String memo_text;
    private String contribution_receipt_date;
}
