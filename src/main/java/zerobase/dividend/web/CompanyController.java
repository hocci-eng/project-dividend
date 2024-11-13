package zerobase.dividend.web;

import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.*;
import zerobase.dividend.model.Company;
import zerobase.dividend.persist.entity.CompanyEntity;
import zerobase.dividend.service.CompanyService;

import java.util.List;

@RestController
@RequestMapping("/company")
@AllArgsConstructor
@EnableMethodSecurity
public class CompanyController {

    private static final Logger log = LoggerFactory.getLogger(CompanyController.class);
    private final CompanyService companyService;


    @GetMapping("autocomplete")
    public ResponseEntity<?> autoComplete(@RequestParam String keyword) {
        List<String> result = companyService.getCompanyNamesByKeyword(keyword);
        return ResponseEntity.ok(result);
    }

    @GetMapping
    @PreAuthorize("hasRole('READ')")
    public ResponseEntity<?> searchCompany(final Pageable pageable) {
        Page<CompanyEntity> companies = companyService.getAllCompany(pageable);
        log.info("search company page size : {}, page number:{}",
                pageable.getPageSize(), pageable.getPageNumber());
        return ResponseEntity.ok(companies);
    }

    @PostMapping
    @PreAuthorize("hasRole('WRITE')")
    public ResponseEntity<?> addCompany(@RequestBody Company request) {
        String ticker = request.getTicker().trim();
        if (ObjectUtils.isEmpty(ticker)) {
            throw new RuntimeException("Ticker is empty");
        }

        Company savedCompany = companyService.save(ticker);
        companyService.addAutoCompleteKeyWord(savedCompany.getName());
        log.info("add company : {}", savedCompany.getName());
        return ResponseEntity.ok(savedCompany);
    }

    @DeleteMapping("/{ticker}")
    @PreAuthorize("hasRole('WRITE')")
    public ResponseEntity<?> deleteCompany(@PathVariable String ticker) {
        String companyName = companyService.deleteCompany(ticker);
        companyService.clearFinanceCache(companyName);
        log.info("delete company : {}", companyName);
        return ResponseEntity.ok(companyName);
    }
}
