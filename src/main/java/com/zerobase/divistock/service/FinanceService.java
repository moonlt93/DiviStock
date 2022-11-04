package com.zerobase.divistock.service;

import com.zerobase.divistock.exception.NoCompanyException;
import com.zerobase.divistock.model.Company;
import com.zerobase.divistock.model.Dividend;
import com.zerobase.divistock.model.ScrapedResult;
import com.zerobase.divistock.model.constant.CacheKey;
import com.zerobase.divistock.persist.CompanyRepository;
import com.zerobase.divistock.persist.DividendRepository;
import com.zerobase.divistock.persist.entity.CompanyEntity;
import com.zerobase.divistock.persist.entity.DividendEntity;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
@Slf4j
@Service
@AllArgsConstructor
public class FinanceService {

    private final CompanyRepository companyRepository;
    private final DividendRepository dividendRepository;

    //요청이 자주 들어오는가?
    @Cacheable(key = "#companyName", value = CacheKey.KEY_FINANCE)
    public ScrapedResult getDividendByCompanyName(String companyName) {
        log.info("search company =>"+companyName);
        //1. 회사명을 기준으로 회사 정보를 조회
        CompanyEntity company = this.companyRepository.findByName(companyName)
                .orElseThrow(() -> new NoCompanyException());

        //2. 조회된 회사 id로 배당금 정보를 조회
        List<DividendEntity> dividendEntityList = this.dividendRepository.findAllByCompanyId(company.getId());

        //3. 결과 조합 후 변환.

        List<Dividend> dividends = dividendEntityList.stream()
                .map(e -> new Dividend(e.getDate(), e.getDividend()))
                .collect(Collectors.toList());


        return new ScrapedResult(new Company(company.getTicker(), company.getName()),
                dividends);

    }

}
/*
 * dividendEntities.stream()
 * .map(e -> Dividend.builder().date(e.getDate())
 *           .dividend(e.getDividend())
 *           .build()).collect(Collectors.toList())
 *
 * */


