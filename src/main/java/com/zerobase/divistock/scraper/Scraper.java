package com.zerobase.divistock.scraper;

import com.zerobase.divistock.model.Company;
import com.zerobase.divistock.model.ScrapedResult;

public interface Scraper {
     Company scrapCompanyByTicker(String ticker);
     ScrapedResult scrap(Company company);
}
