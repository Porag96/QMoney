
package com.crio.warmup.stock;


import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;
import com.crio.warmup.stock.dto.AnnualizedReturn;
import com.crio.warmup.stock.dto.Candle;
import com.crio.warmup.stock.dto.PortfolioTrade;
import com.crio.warmup.stock.dto.TiingoCandle;
import com.crio.warmup.stock.dto.TotalReturnsDto;
import com.crio.warmup.stock.log.UncaughtExceptionHandler;
import com.crio.warmup.stock.portfolio.PortfolioManager;
import com.crio.warmup.stock.portfolio.PortfolioManagerFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.logging.log4j.ThreadContext;
import org.springframework.web.client.RestTemplate;


public class PortfolioManagerApplication {


  public static String getToken() {
    String token = "9e3ded6c24ddafc313e569f9bf79f970bdc13668";
    return token;
  }
  
  public static List<String> mainReadFile(String[] args) throws IOException, URISyntaxException {
    File file = resolveFileFromResources(args[0]);
    ObjectMapper objectMapper = getObjectMapper();
    PortfolioTrade[] trades = objectMapper.readValue(file, PortfolioTrade[].class);
    List<String> symbols = new ArrayList<>();
    for(PortfolioTrade trade : trades){
      symbols.add(trade.getSymbol());
    }
    return symbols;
  }





  // calculate annualized returns









  // Find out the closing price of each stock on the end_date and return the list

  private static void printJsonObject(Object object) throws IOException {
    Logger logger = Logger.getLogger(PortfolioManagerApplication.class.getCanonicalName());
    ObjectMapper mapper = new ObjectMapper();
    logger.info(mapper.writeValueAsString(object));
  }

  private static File resolveFileFromResources(String filename) throws URISyntaxException {
    return Paths.get(
        Thread.currentThread().getContextClassLoader().getResource(filename).toURI()).toFile();
  }

  private static ObjectMapper getObjectMapper() {
    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.registerModule(new JavaTimeModule());
    return objectMapper;
  }


  public static List<String> debugOutputs() {

     String valueOfArgument0 = "trades.json";
     String resultOfResolveFilePathArgs0 = "/home/crio-user/workspace/poragjyotilakhimpur-ME_QMONEY_V2/qmoney/bin/main/trades.json";
     String toStringOfObjectMapper = "com.fasterxml.jackson.databind.ObjectMapper@5542c4ed";
     String functionNameFromTestFileInStackTrace = "PortfolioManagerApplication.mainReadFile()";
     String lineNumberFromTestFileInStackTrace = "29:1";


    return Arrays.asList(new String[]{valueOfArgument0, resultOfResolveFilePathArgs0,
        toStringOfObjectMapper, functionNameFromTestFileInStackTrace,
        lineNumberFromTestFileInStackTrace});
  }


  // Note:
  // Remember to confirm that you are getting same results for annualized returns as in Module 3.
  public static List<TotalReturnsDto> getTotalReturnsDto(String[] args, PortfolioTrade[] trades) throws IOException, URISyntaxException{
    RestTemplate restTemplate = new RestTemplate();
    List<TotalReturnsDto> totalReturnsDtos = new ArrayList<>();
    for(PortfolioTrade trade : trades ){
      String uri = "https://api.tiingo.com/tiingo/daily/" + trade.getSymbol() + "/prices?startDate=" + trade.getPurchaseDate() + "&endDate=" + args[1] + "&token=9e3ded6c24ddafc313e569f9bf79f970bdc13668";
      TiingoCandle[] results = restTemplate.getForObject(uri, TiingoCandle[].class);
      
      if(results != null){
        totalReturnsDtos.add(new TotalReturnsDto(trade.getSymbol(), results[results.length-1].getClose()));
      }
    }
    return totalReturnsDtos;
  }

  public static List<String> mainReadQuotes(String[] args) throws IOException, URISyntaxException {
    ObjectMapper objectMapper = getObjectMapper();
    PortfolioTrade[] trades = objectMapper.readValue(resolveFileFromResources(args[0]), PortfolioTrade[].class);
    List<TotalReturnsDto> totalReturnsDtos = getTotalReturnsDto(args, trades);
    Collections.sort(totalReturnsDtos, TotalReturnsDto.comparator);
    List<String> symbols = new ArrayList<>();
    for(TotalReturnsDto totalReturnsDto : totalReturnsDtos){
      symbols.add(totalReturnsDto.getSymbol());
    }
    return symbols;
  }


  public static List<PortfolioTrade> readTradesFromJson(String filename) throws IOException, URISyntaxException {
    File file = resolveFileFromResources(filename);
    ObjectMapper objectMapper = getObjectMapper();
    PortfolioTrade[] trades = objectMapper.readValue(file, PortfolioTrade[].class);
    List<PortfolioTrade> portfolioTrades = new ArrayList<>();
    for(PortfolioTrade trade : trades){
      portfolioTrades.add(trade);
    }

    return portfolioTrades;
  }


  // Build the Url using given parameters and use this function in your code to cann the API.
  public static String prepareUrl(PortfolioTrade trade, LocalDate endDate, String token) {
     return "https://api.tiingo.com/tiingo/daily/"+trade.getSymbol()+"/prices?startDate="+trade.getPurchaseDate()+"&endDate="+endDate+"&token="+token;
  }

  //  Ensure all tests are passing using below command

  static Double getOpeningPriceOnStartDate(List<Candle> candles) {
    return candles.get(0).getOpen();
  }


  public static Double getClosingPriceOnEndDate(List<Candle> candles) {
    return candles.get(candles.size()-1).getClose();
  }


  public static List<Candle> fetchCandles(PortfolioTrade trade, LocalDate endDate, String token) {
    RestTemplate restTemplate = new RestTemplate();
    String uri = "https://api.tiingo.com/tiingo/daily/" + trade.getSymbol() + "/prices?startDate=" + trade.getPurchaseDate() + "&endDate=" + endDate + "&token="+token;
    return Arrays.asList(restTemplate.getForObject(uri, TiingoCandle[].class));
  }

  public static AnnualizedReturn getAnnualizedReturn(PortfolioTrade trade, LocalDate endDate) {
    String symbol = trade.getSymbol();
    LocalDate startDate = trade.getPurchaseDate();

    if(startDate.compareTo(endDate) >= 0 ){
      throw new RuntimeException();
    }

    RestTemplate restTemplate = new RestTemplate();

    String uri = "https://api.tiingo.com/tiingo/daily/" + symbol + "/prices?startDate=" + startDate.toString() + "&endDate=" + endDate.toString() + "&token=9e3ded6c24ddafc313e569f9bf79f970bdc13668";
    TiingoCandle[] results = restTemplate.getForObject(uri, TiingoCandle[].class);
    
    if(results != null){
      TiingoCandle purchaseDate = results[0];
      TiingoCandle lastDate = results[results.length - 1];

      Double buyPrice = purchaseDate.getOpen();
      Double sellPrice = lastDate.getClose();

      AnnualizedReturn annualizedReturn = calculateAnnualizedReturns(endDate, trade, buyPrice, sellPrice);
      return annualizedReturn;
    }
    else{
      return new AnnualizedReturn(symbol, Double.NaN, Double.NaN);
    }
  
  }

  public static List<AnnualizedReturn> mainCalculateSingleReturn(String[] args)
      throws IOException, URISyntaxException {
      List<AnnualizedReturn> annualizedReturns = new ArrayList<>();
      LocalDate endDate = LocalDate.parse(args[1]);
      ObjectMapper objectMapper = getObjectMapper();
      PortfolioTrade[] trades = objectMapper.readValue(resolveFileFromResources(args[0]), PortfolioTrade[].class);
      for(PortfolioTrade trade : trades){
        annualizedReturns.add(getAnnualizedReturn(trade, endDate));
      }

      Comparator<AnnualizedReturn> sortByAnnualizedReturn = Comparator.comparing(AnnualizedReturn::getAnnualizedReturn).reversed();
      Collections.sort(annualizedReturns, sortByAnnualizedReturn);
     return annualizedReturns;
  }

  // Return the populated list of AnnualizedReturn for all stocks.
  //  Annualized returns should be calculated in two steps:
  //   1. Calculate totalReturn = (sell_value - buy_value) / buy_value.
  //      1.1 Store the same as totalReturns
  //   2. Calculate extrapolated annualized returns by scaling the same in years span.
  //      The formula is:
  //      annualized_returns = (1 + total_returns) ^ (1 / total_num_years) - 1
  //      2.1 Store the same as annualized_returns


  public static AnnualizedReturn calculateAnnualizedReturns(LocalDate endDate,
      PortfolioTrade trade, Double buyPrice, Double sellPrice) {
        double totalReturns = (sellPrice - buyPrice) / buyPrice;
        double totalYears = ChronoUnit.DAYS.between(trade.getPurchaseDate(), endDate) / 365.24;
        double annualizedReturns = Math.pow((1 + totalReturns), (1 / totalYears)) - 1;

      return new AnnualizedReturn(trade.getSymbol(), annualizedReturns, totalReturns);
  }

  public static RestTemplate restTemplate = new RestTemplate();
  public static PortfolioManager portfolioManager = PortfolioManagerFactory.getPortfolioManager(restTemplate);

  private static String readFileAsString(String filename) throws URISyntaxException {
    return Paths.get(
        Thread.currentThread().getContextClassLoader().getResource(filename).toURI()).toString();
  }

  public static List<AnnualizedReturn> mainCalculateReturnsAfterRefactor(String[] args)throws Exception {

    String file = args[0];
    LocalDate endDate = LocalDate.parse(args[1]);
    String contents = readFileAsString(file);
    ObjectMapper objectMapper = getObjectMapper();
    PortfolioTrade[] portfolioTrades = objectMapper.readValue(contents, PortfolioTrade[].class);
    return portfolioManager.calculateAnnualizedReturn(Arrays.asList(portfolioTrades), endDate);
  }


  public static void main(String[] args) throws Exception {
    Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler());
    ThreadContext.put("runId", UUID.randomUUID().toString());

    printJsonObject(mainReadFile(args));


    printJsonObject(mainReadQuotes(args));



    printJsonObject(mainCalculateSingleReturn(args));






  



    printJsonObject(mainCalculateReturnsAfterRefactor(args));



  }
}

