package com.zerobase.divistock.scheduler;

import com.zerobase.divistock.model.Company;
import com.zerobase.divistock.model.ScrapedResult;
import com.zerobase.divistock.model.constant.CacheKey;
import com.zerobase.divistock.persist.CompanyRepository;
import com.zerobase.divistock.persist.DividendRepository;
import com.zerobase.divistock.persist.entity.CompanyEntity;
import com.zerobase.divistock.persist.entity.DividendEntity;
import com.zerobase.divistock.scraper.Scraper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@EnableCaching
@AllArgsConstructor
public class ScraperScheduler {

private final CompanyRepository companyRepository;
private final DividendRepository dividendRepository;

private final Scraper yahooFinanceScraper;

//일정주기마다 수행
    @CacheEvict(value= CacheKey.KEY_FINANCE,allEntries = true)
    @Scheduled(cron="${scheduler.scrap.yahoo}")
        public void yahooFinanceScheduling(){
           log.info("scraping scheduler is started");

            //저장된 회사 목록을 조회
          List<CompanyEntity> companies=  this.companyRepository.findAll();
            // 회사마다 배당금 정보를 새로 스크래핑
               for(var company: companies){
                   log.info("scraping scheduler is started -> "+ company.getName());
              ScrapedResult result = this.yahooFinanceScraper.scrap(
                      new Company (company.getName(),company.getTicker()));


            // 스크래핑한 정보 중 데이터베이스에 없는 값은 저장

              result.getDividendEntities().stream()
                      //디비든 모델을 엔티티로 매핑
                      .map(e -> new DividendEntity(company.getId(),e))
                      //element를 하나씩 삽입함.
                      .forEach(e -> {
                              boolean exist =this.dividendRepository.existsByCompanyIdAndDate(e.getCompanyId(),e.getDate());
                              if(!exist){
                                      this.dividendRepository.save(e);
                                      log.info("insert new dividend-> " +e.toString());
                              }
                      });

                        // 연속적으로 스크래핑 요청을 날리지 않도록 일시정지
                       try {
                               Thread.sleep(3000);
                       }catch(InterruptedException e){
                               Thread.currentThread().interrupt();
                       }
               }

        }

}
