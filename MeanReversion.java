import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
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
    
    //convert to array for easier looping
    int numTickers = tickers.size();
    String[] tArray = new String[numTickers];
    int t=0;
    for(String tick: tickers) {
      tArray[t++] = tick;
    }
    
    double weight = 1.00/(double) tickers.size();
    
    
  //broker commission fee
    double commFee = 0.0;
    
    //HashMap<String, Double[]> monthlyReturns = new HashMap<>();
    Double[][] monthlyReturns = new Double[numTickers][];
    
    //save file name
    String fileName = "Result.txt";
    
    //System.out.println("Size: "+tickers.size()+ " With weight: "+ weight);
    //for each stock
    for (int i=0;i<numTickers;i++) {
      String ticker = tArray[i];
      //assuming equal weights
      
      
      //calculate monthly returns
      Double[] r = invest(data.dates,data.stocks, ticker, 20, 2, 100, true, 0.0, commFee);
      
      monthlyReturns[i]=r;
      
      System.out.println("Monthly Return for "+ticker+" is: "+ Arrays.toString(r));
      toFile(fileName,ticker+ " " + Arrays.toString(r)+"\n");
    }
    
    //calculate portfolio monthly returns
    int months = monthlyReturns[0].length;
    double[] pMonthRet = new double[months];
    
    //loop through each month
    for(int j=0;j<months;j++) {
      
      double ret = 0.0;
      //loop through each stock's return at that month
      //sum up the weighted returns
      for(int i=0; i<monthlyReturns.length;i++) {
        ret += monthlyReturns[i][j]*weight;
      }
      
      pMonthRet[j] = ret;
      
    }
    
    
    
    System.out.println("Portfolio Monthly Return: " + Arrays.toString(pMonthRet));
    toFile(fileName,"Portfolio " + Arrays.toString(pMonthRet));
    
    
  }
  
  /**
   * Returns an array of monthly returns from 1/2/2006 to 4/1/2022
   * monthly returns are calculated by taking a snapshot of the portfolio 
   * by "closing" all positions on the end of the month and immediately re-open them
   * There are 195 months
   * @param prices
   * @param n period in days for avg and stdev, e.g., 30
   * @param k times of stdev for upper/lower band, e.g., 1
   * @param money starting money
   * @param allowShort is able to short the stock when too high?
   * @param investAmount percentage of portfolio to invest 1-invest everything; 0.5-invest half; 0-invest in only 1 share
   * @return
   */
  private static Double[] invest(List<Calendar> dates, HashMap<String, Double[]> stocks,String ticker, int n, int k, double money, boolean allowShort, double investAmount, double commFee) {
    double bgBal = money;
    Double[] prices = stocks.get(ticker);
    
    //there are 195 months so 195 monthly returns
    Double[] mr = new Double[195];
    
    double position = 0;
    
    //starting date
    String startDate = "01/02/2006";
    String endDate = "04/01/2022";
    SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
    Calendar cur = Calendar.getInstance();
    Calendar end = Calendar.getInstance();
    try {
      cur.setTime(sdf.parse(startDate));
      end.setTime(sdf.parse(endDate));
    } catch (ParseException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    
    
    int i = 0;//the index of price
    int months = 0;
    
    //loop each day
    for(;cur.before(end);cur.add(Calendar.DATE, 1)) {
      
      //if on the last day
      if(cur.get(Calendar.DATE)==cur.getActualMaximum(Calendar.DAY_OF_MONTH)) {
        //generate snapshot
        
        
        //fake closing positions
        double endBal = money;
        endBal += position*prices[i];
        endBal -= position*prices[i]*commFee;
        
        //record the return
        double curMonthRet = ror(bgBal,endBal);
        mr[months]=curMonthRet;
        //System.out.println("On "+cur.getTime().toString()+", Got a Monthly return of "+ curMonthRet);
        
        //set the end as next month's beginning balance
        bgBal = endBal;
        months++;
      }
      
      //check if today is not a trading day
      //i.e., trading day is after day
      //skip today then
      if(dates.get(i).after(cur)) {
        //System.out.println("Skipping because current is "+ cur.getTime().toString() + " and next trading day is " + dates.get(i).getTime().toString());
        continue;
      }
      
      double p = prices[i];
      
      //go to next value if the current is invalid
      if (p<0) {
        i++;
        continue;
        }
      
      //BELOW IS SKIPPED if the current price is invalid
      
      //compute moving average
      if(i>=n-1 && prices[i-n+1]>=0) {
        
        double maSum = 0.0, maStdev = 0.0;
        
        //calculate the moving average
        for(int j=i-n+1;j<i+1;j++) {
          maSum += prices[j];
        }
        maSum = (double) maSum / (double) n;
        
        //get Stdev
        for(int j=i-n+1;j<i+1;j++) {
          maStdev += Math.pow(prices[j] - maSum, 2);
        }
        maStdev = Math.sqrt(maStdev/(double)n);
        
        //this is for exporting purposes
        //Double[] result = new Double[2];
        //result[0] = maSum;
        //result[1] = maStdev;
        
        //get z score
        double z = (double) (prices[i] - maSum) / maStdev;
        
        //how much stock to buy / sell
        int pos;
        if (investAmount <= 0.0001) pos = 1;//only but/sell one share
        else pos = (int) Math.floor((money/prices[i])*investAmount);// invest custom amount
        
        //amount of money to invest
        double amount = prices[i]*pos;
        
        //System.out.printf("On day %d, average of %d days is %f, with stdev of %f, and a z-score of %f %n", days, k, maSum, maStdev, z);
        //invest decisions
        if(z>k && allowShort) {
          //short if z>1
          money += amount;
          money -= amount*commFee;
          position -= pos;
          //System.out.printf("Shorting %d shares, gaining $%f%n",pos,amount);
        } else if (z<-k) {
          //buy if z<-1
          money -= amount;
          position += pos;
          money -= amount*commFee;
          //System.out.printf("Buying %d shares, costing $%f%n",pos,amount);
        } else if (Math.abs(z) <0.5) {
          //close if near moving average
          money += position*prices[i];
          money -= amount*commFee;
          position = 0;
        }
        
      }
      
      i++;
    }
    
    return mr;
  }
  
  /**
   * Calculates annual growth rate
   * @param bgBal beginning balance
   * @param endBal ending bal
   * @param t number of years
   * @return
   */
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
  
  public static void toFile(String fileName,String str) 
      {
        try {
          BufferedWriter writer = new BufferedWriter(new FileWriter(fileName, true));
          writer.append(str);
          writer.close();
        } catch (IOException e) {
          System.out.println("Error occured while writing file!");
          e.printStackTrace();
        }
        
    }

}
