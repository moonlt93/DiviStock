package com.zerobase.divistock;

import com.zerobase.divistock.scraper.YahooFinanceScraper;

//@SpringBootApplication
public class DiviStockApplication {

    public static void main(String[] args) {
        //    SpringApplication.run(DiviStockApplication.class, args);

       YahooFinanceScraper scraper = new YahooFinanceScraper();
     //   var result = scraper.scrap(Company.builder().ticker("O").build());
        var result = scraper.scrapCompanyByTicker("MMM");

        System.out.println(result);

    }
}
