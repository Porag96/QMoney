
package com.crio.warmup.stock.portfolio;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import com.crio.warmup.stock.dto.AnnualizedReturn;
import com.crio.warmup.stock.dto.Candle;
import com.crio.warmup.stock.dto.PortfolioTrade;
import com.crio.warmup.stock.dto.TiingoCandle;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.web.client.RestTemplate;

public class PortfolioManagerImpl implements PortfolioManager {

  RestTemplate restTemplate = new RestTemplate();


  protected PortfolioManagerImpl(RestTemplate restTemplate) {
    this.restTemplate = restTemplate;
  }


  public AnnualizedReturn getAnnualizedReturn(PortfolioTrade trade, LocalDate endDate){
    AnnualizedReturn annualizedReturn;
    String symbol = trade.getSymbol();
    LocalDate purchaseDate = trade.getPurchaseDate();

    try {
      List<Candle> tiingoCandles = getStockQuote(symbol, purchaseDate, endDate);

      Candle stockPurchaseDate = tiingoCandles.get(0);
      Candle lastDate = tiingoCandles.get(tiingoCandles.size() - 1); 
      
      Double totalReturn = (lastDate.getClose() - stockPurchaseDate.getOpen()) / stockPurchaseDate.getOpen();
      Double totalYears = ChronoUnit.DAYS.between(trade.getPurchaseDate(), endDate) / 365.24;
      Double annualizedReturns = Math.pow((1 + totalReturn), (1 / totalYears)) - 1;
      annualizedReturn = new AnnualizedReturn(symbol, annualizedReturns, totalReturn);
    } catch (JsonProcessingException e) {
      annualizedReturn = new AnnualizedReturn(symbol, Double.NaN, Double.NaN);
    }
    return annualizedReturn;
  }
  
  @Override
  public List<AnnualizedReturn> calculateAnnualizedReturn(List<PortfolioTrade> portfolioTrades,LocalDate endDate){
    List<AnnualizedReturn> annualizedReturns = new ArrayList<>();

    for(PortfolioTrade trade : portfolioTrades){
      AnnualizedReturn annualizedReturn = getAnnualizedReturn(trade, endDate);

      annualizedReturns.add(annualizedReturn);
    }

    Collections.sort(annualizedReturns, getComparator());
    return annualizedReturns;
  }






  private Comparator<AnnualizedReturn> getComparator() {
    return Comparator.comparing(AnnualizedReturn::getAnnualizedReturn).reversed();
  }



  public List<Candle> getStockQuote(String symbol, LocalDate from, LocalDate to) throws JsonProcessingException {
    if(from.compareTo(to) >= 0){
      throw new RuntimeException();
    }

    String uri = buildUri(symbol, from, to);

    TiingoCandle[] results = restTemplate.getForObject(uri, TiingoCandle[].class);
    
    if(results == null){
      return new ArrayList<Candle>();
    }
    else{
      return Arrays.asList(results);
    }
  }

  protected String buildUri(String symbol, LocalDate startDate, LocalDate endDate) {
    String token = "9e3ded6c24ddafc313e569f9bf79f970bdc13668";

    String uriTemplate = "https:api.tiingo.com/tiingo/daily/$SYMBOL/prices?"
        + "startDate=$STARTDATE&endDate=$ENDDATE&token=$APIKEY";

    String uri = uriTemplate.replace("$APIKEY", token).replace("$SYMBOL", symbol).replace("$STARTDATE", startDate.toString())
    .replace("$ENDDATE", endDate.toString());

    return uri;
  }



}
