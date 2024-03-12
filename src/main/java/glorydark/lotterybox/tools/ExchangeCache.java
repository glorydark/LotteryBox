package glorydark.lotterybox.tools;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @author glorydark
 */
@Data
@AllArgsConstructor
public class ExchangeCache {

    String ticketName;

    double moneyCost;

    int ticketBuyCount;
}
