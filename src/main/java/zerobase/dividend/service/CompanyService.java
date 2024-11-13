package zerobase.dividend.service;

import lombok.AllArgsConstructor;
import org.apache.commons.collections4.Trie;
import org.springframework.cache.CacheManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import zerobase.dividend.exception.impl.AlreadyExistTickerError;
import zerobase.dividend.exception.impl.FailedToScrapException;
import zerobase.dividend.exception.impl.NoCompanyException;
import zerobase.dividend.model.Company;
import zerobase.dividend.model.ScrapedResult;
import zerobase.dividend.model.constants.CacheKey;
import zerobase.dividend.persist.CompanyRepository;
import zerobase.dividend.persist.DividendRepository;
import zerobase.dividend.persist.entity.CompanyEntity;
import zerobase.dividend.persist.entity.DividendEntity;
import zerobase.dividend.scraper.Scraper;

import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class CompanyService {

    private final Trie<String, String> trie;
    private final Scraper yahooFinanceScraper;

    private final CompanyRepository companyRepository;
    private final DividendRepository dividendRepository;

    private final CacheManager redisCacheManager;

    public Company save(String ticker) {
        boolean exists = companyRepository.existsByTicker(ticker);
        if (exists) {
            throw new AlreadyExistTickerError();
        }
        return storeCompanyAndDividend(ticker);
    }

    public Page<CompanyEntity> getAllCompany(Pageable pageable) {
        return companyRepository.findAll(pageable);
    }

    private Company storeCompanyAndDividend(String ticker) {
        Company company = yahooFinanceScraper.scrapCompanyByTicker(ticker);
        if (ObjectUtils.isEmpty(company)) {
            throw new FailedToScrapException();
        }

        ScrapedResult scrapedResult = yahooFinanceScraper.scrap(company);

        CompanyEntity companyEntity = companyRepository.save(new CompanyEntity(company));

        List<DividendEntity> dividendEntities = scrapedResult.getDividendEntities().stream()
                .map(e -> new DividendEntity(companyEntity.getId(), e))
                .toList();

        dividendRepository.saveAll(dividendEntities);
        return company;
    }

    public void addAutoCompleteKeyWord(String keyword) {
        trie.put(keyword, null);
    }


    public List<String> autoComplete(String keyword) {
        return trie.prefixMap(keyword).keySet().stream()
                .limit(10).toList();
    }


    public void deleteAutoCompleteKeyWord(String keyword) {
        trie.remove(keyword);
    }

    public String deleteCompany(String keyword) {
        CompanyEntity company = companyRepository.findByTicker(keyword)
                .orElseThrow(NoCompanyException::new);

        dividendRepository.deleteAllByCompanyId(company.getId());
        companyRepository.delete(company);

        deleteAutoCompleteKeyWord(company.getName());
        return company.getName();
    }

    public void clearFinanceCache(String companyName) {
        Optional.ofNullable(redisCacheManager.getCache(CacheKey.KEY_FINANCE))
                .orElseThrow(() -> new RuntimeException("캐시를 찾을 수 없습니다."))
                .evict(companyName);
    }
}
