package FEC.Backend.services;

import FEC.Backend.models.DonorReceipt;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;


import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class CommitteeIdService {
    private final String apiKey = "sT5EcBtx3MYb9PURfSOW0bCiigbTk7ABmza4TiQT";
    private final RestTemplate restTemplate = new RestTemplate();

    public String getCommitteeId(DonorReceipt receipt){
        String committee_id = receipt.getCommittee_id();
        if (receipt.getMemo_text() != null) {
            String extract = extractCommitteeId(receipt.getMemo_text());
            if (extract != null){
                committee_id = extract;
            }
        }
        return committee_id;
    }

    private static String extractCommitteeId(String memoText) {
        // Regular expression to match text inside parentheses
        String regex = "\\(([^)]+)\\)";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(memoText);
        // If a match is found, return the text inside the parentheses
        if (matcher.find()) {
            return matcher.group(1); // matcher.group(1) contains the text inside parentheses
        }
        return null; // Return null if no parentheses are found
    }

}
