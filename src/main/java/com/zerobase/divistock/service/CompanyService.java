package com.zerobase.divistock.service;


import com.zerobase.divistock.exception.NoCompanyException;
import com.zerobase.divistock.model.Company;
import com.zerobase.divistock.model.ScrapedResult;
import com.zerobase.divistock.persist.CompanyRepository;
import com.zerobase.divistock.persist.DividendRepository;
import com.zerobase.divistock.persist.entity.CompanyEntity;
import com.zerobase.divistock.persist.entity.DividendEntity;
import com.zerobase.divistock.scraper.Scraper;
import lombok.AllArgsConstructor;
import org.apache.commons.collections4.Trie;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.util.List;
import java.util.stream.Collectors;
// service 인스턴스는 하나만 사용..

@Service
@AllArgsConstructor
public class CompanyService {

    private final Scraper yahooFinanceScraper;
    private final CompanyRepository companyRepository;
    private final DividendRepository dividendRepository;
    private final Trie trie;

    public Company save(String ticker){
      boolean check=  this.companyRepository.existsByTicker(ticker);
                if(check){
                    throw new RuntimeException("already exists ticker->"+ticker);
                }
        return this.storeCompanyDividend(ticker);
    }

    public Page<CompanyEntity> getAllCompany(final Pageable pageable){
        return this.companyRepository.findAll(pageable);
    }

    private Company storeCompanyDividend(String ticker){
       // ticker기준 회사 스크래핑

        Company company = this.yahooFinanceScraper.scrapCompanyByTicker(ticker);
        if(ObjectUtils.isEmpty(company)){
            throw new RuntimeException("failed to scrap ticker -> "+ ticker);
        }
        // 해당 회사가 존재시 회사의 배당금 정보를 스크래핑
        ScrapedResult scrapedResult= this.yahooFinanceScraper.scrap(company);

       //결과
      CompanyEntity companyEntity =  this.companyRepository.save(new CompanyEntity(company));
      List<DividendEntity> dividendEntityList=  scrapedResult.getDividendEntities().stream()
                .map(e -> new DividendEntity(companyEntity.getId(),e))
                //.filter,.sort 필요에 따른 기능
                .collect(Collectors.toList());
        //e 는 컬렉션 아이템 하나하나
                //
        this.dividendRepository.saveAll(dividendEntityList);
        return company;
    }


    public void addAutocompleteKeyword(String keyword){
        this.trie.put(keyword,null);
    }

    public List<String> autocomplete(String keyword){
       return (List<String>) this.trie.prefixMap(keyword).keySet()
                .stream().collect(Collectors.toList());
    }

    public void deleteAutocompleteKeyword(String keyword){
        this.trie.remove(keyword);
    }

    public List<String> getCompanyNamesByKeyword(String keyword){
        Pageable limit= PageRequest.of(0,10);
        Page<CompanyEntity> companyEntities = this.companyRepository.findByNameStartingWithIgnoreCase (keyword,limit);
        return companyEntities.stream()
                .map(e-> e.getName())
                .collect(Collectors.toList());
    }

    public String deleteCompany(String ticker) {

       var companyEntity =this.companyRepository.findByTicker(ticker)
               .orElseThrow(()-> new NoCompanyException());

      this.dividendRepository.deleteAllByCompanyId(companyEntity.getId());
      this.companyRepository.delete(companyEntity);
      this.deleteAutocompleteKeyword(companyEntity.getName());

      return companyEntity.getName();
    }
}
