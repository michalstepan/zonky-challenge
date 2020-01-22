package cz.michalstepan.zonky.marketer;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@Component
public class MarketService {

    @Autowired
    @Lazy
    private MarketService self;

    @Autowired
    private RestTemplate restTemplateZonky;

//    @Retryable(value = Throwable.class)
    public void fetchMarketplace() {
//        if (ThreadLocalRandom.current().nextBoolean()) {
//            log.error("fail");
//            throw new RuntimeException("Test fail");
//        }
        HashMap data = restTemplateZonky
                .getForObject("loans/marketplace", HashMap.class);

        log.info(data.values().toString());
    }

    @Scheduled(fixedRate = 5_000)
    public void scanMarketplace() {
        self.fetchMarketplace();
    }


}
