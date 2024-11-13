package zerobase.dividend.scraper;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Component;
import zerobase.dividend.model.Company;
import zerobase.dividend.model.Dividend;
import zerobase.dividend.model.ScrapedResult;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

@Component
public class YahooFinanceScraper implements Scraper {

    private static final long PERIOD1 = LocalDateTime.now().minusYears(1).atZone(ZoneId.of("UTC")).toEpochSecond();
    private static final long PERIOD2 = LocalDateTime.now().atZone(ZoneId.of("UTC")).toEpochSecond();

    private static final String URL = "https://query1.finance.yahoo.com/v8/finance/chart/%s?interval=1mo&period1=%d&period2=%d&events=div";
    private static final String SUMMARY_URL = "https://finance.yahoo.com/quote/%s/";

    @Override
    public ScrapedResult scrap(Company company) {
        ScrapedResult scrapedResult = new ScrapedResult();
        scrapedResult.setCompany(company);
        List<Dividend> dividends = new ArrayList<>();

        String url = String.format(URL, company.getTicker(), PERIOD1, PERIOD2);

        try {
            String jsonResponse = fetchJsonData(url);

            ObjectMapper mapper = new ObjectMapper();
            JsonNode rootNode = mapper.readTree(jsonResponse);
            JsonNode eventsNode = rootNode.at("/chart/result/0/events/dividends");

            if (eventsNode.isObject()) {
                eventsNode.fields().forEachRemaining(field -> {
                    JsonNode dividendNode = field.getValue();
                    long timestamp = dividendNode.get("date").asLong();
                    String dividend = dividendNode.get("amount").toString();

                    LocalDateTime date = Instant.ofEpochSecond(timestamp).atZone(ZoneId.of("UTC")).toLocalDateTime();

                    dividends.add(new Dividend(date, dividend));
                });

                scrapedResult.setDividendEntities(dividends);
            } else {
                System.out.println("배당금 정보가 없습니다.");
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        return scrapedResult;
    }

    @Override
    public Company scrapCompanyByTicker(String ticker) {
        String url = String.format(SUMMARY_URL, ticker);

        try {
            Document document = Jsoup.connect(url).get();
            Element h1Title = document.getElementsByTag("h1").get(1);
            String title = h1Title.text().split("\\(")[0].trim();
            return new Company(ticker, title);
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
        return null;
    }

    private static String fetchJsonData(String urlString) throws IOException, URISyntaxException {
        URL url = new URI(urlString).toURL();
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("User-Agent", "Mozilla/5.0");

        int responseCode = connection.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            return new String(url.openStream().readAllBytes());
        } else {
            throw new IOException("HTTP request failed with code " + responseCode);
        }
    }
}




