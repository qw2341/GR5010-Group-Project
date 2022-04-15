import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Set;

/**
 * MeanReversion - TODO Description
 * 
 * Bugs: none known
 * @author Qingqi Wu
 * @version 1.0
 * 
 */
public class MeanReversion {

  /**
   * @param args
   */
  public static void main(String[] args) {
    DataReader data = new DataReader("DataT.csv");
    //System.out.println(Arrays.toString(data.stocks.get("AAPL")));
    
    Set<String> tickers = data.stocks.keySet();
    
    
    
    double totalYield = 0;
    double weight = 1.00/(double) tickers.size();
    
    double startingBal = 100.00*tickers.size();
    double endingBal= 0.00;
    
    //System.out.println("Size: "+tickers.size()+ " With weight: "+ weight);
    //for each stock
    for (String ticker: tickers) {
      //assuming equal weights
      
      double y = (double) invest(data.stocks, ticker, 30, 1, 100, true, 0.0);
      //System.out.println("Adding Yield: "+y+ " With weight: "+ weight);
      endingBal += y;
      
      
      
    }
    
    double rateOfReturn = ror(startingBal,endingBal);
    double annualY = cagr(startingBal,endingBal,5934/365);
    System.out.println("Overall Portfolio return: " + rateOfReturn);
    System.out.println("Overall Portfolio yield, annualized: " + annualY);
    
  }
  
  /**
   * 
   * @param prices
   * @param n period in days for avg and stdev, e.g., 30
   * @param k times of stdev for upper/lower band, e.g., 1
   * @param money starting money
   * @param allowShort is able to short the stock when too high?
   * @param investAmount percentage of portfolio to invest 1-invest everything; 0.5-invest half; 0-invest in only 1 share
   * @return
   */
  private static double invest(HashMap<String, Double[]> stocks,String ticker, int n, int k, double money, boolean allowShort, double investAmount) {
    double bgBal = money;
    Double[] prices = stocks.get(ticker);
    
    
    double position = 0;
    int days = 0;
    
  //loop each day's price
    for (int i=0;i<prices.length; i++) {
      double p = prices[i];
      //go to next value if the current is invalid
      if (p<0) continue;
      
      days ++;
      //compute moving average
      if(i>=n-1 && prices[i-n+1]>=0) {
        double maSum = 0, maStdev = 0;
        for(int j=i-n+1;j<i;j++) {
          maSum+= prices[j];
        }
        //its mean now
        maSum/=n;
        
        //get Stdev
        for(int j=i-n+1;j<i;j++) {
          maStdev += Math.pow(prices[j] - maSum, 2);
        }
        maStdev = Math.sqrt(maStdev/n);
        
        //this is for exporting purposes
        //Double[] result = new Double[2];
        //result[0] = maSum;
        //result[1] = maStdev;
        
        //get z score
        double z = (prices[i] - maSum) / maStdev;
        
        //how much stock to buy / sell
        int pos;
        if (investAmount <= 0.0001) pos = 1;
        else pos = (int) Math.floor((money/prices[i])*investAmount);// invest half
         //only but/sell one share
        
        
        //invest decisions
        if(z>k && allowShort) {
          //short if z>1
          money += prices[i]*pos;
          position -= pos;
        } else if (z<k) {
          //buy if z>1
          money -= prices[i]*pos;
          position += pos;
          
        } else if (Math.abs(z) <0.5) {
          //close if near moving average
          money += position*prices[i];
          position = 0;
        }
        
      }
      
    }
    
    //252.75 is the trading days in a year
    //annual yield/growth rate
    double yield = ror(bgBal, money);
    
    System.out.println("Invested "+bgBal+" in "+ticker+", gained : "+ money + ", Yield = " + yield);
    return money;
  }
  
  //t is the number of years
  private static double cagr(double bgBal, double endBal, double t) {
    return Math.pow(endBal/bgBal, 1/t)-1;
  }
  
  /**
   * calculated the rate of return
   * @param bgBal
   * @param endBal
   * @return
   */
  private static double ror(double bgBal, double endBal) {
    return endBal/bgBal - 1;
  }
  
  private static double aroi(double roi, double t) {
    return Math.pow((1+roi),1/t)-1;
  }

}
