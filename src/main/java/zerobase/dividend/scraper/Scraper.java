package zerobase.dividend.scraper;

import zerobase.dividend.model.Company;
import zerobase.dividend.model.ScrapedResult;

public interface Scraper {
    ScrapedResult scrap(Company company);
    Company scrapCompanyByTicker(String ticker);
}
