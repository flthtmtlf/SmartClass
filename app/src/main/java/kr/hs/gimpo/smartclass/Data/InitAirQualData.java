package kr.hs.gimpo.smartclass.Data;

import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Locale;

public class InitAirQualData
        extends AsyncTask<Void, Void, Boolean> {

    private String[] airData = new String[15];
    private final String[] airDataFormat =
    {"dataTime", "pm10", "pm25", "o3", "no2", "co", "so2", "khai"};
    private DatabaseReference mDatabase;

    // this constructor is no longer supported and will be deprecated.
    // please call function InitAirQualData()
    public InitAirQualData(
      DatabaseReference mDatabase,
      @NonNull String thisTime
    ) {
        this.mDatabase = mDatabase;
    }

    public InitAirQualData() {
        this.mDatabase = FirebaseDatabase.getInstance().getReference("test");
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected Boolean doInBackground(Void... param) {
      SimpleDateFormat sdf = new SimpleDateFormat("mm", Locale.getDefault());
      int m = Integer.parseInt(sdf.format(Calendar.getInstance().getTime()));

      try{
          // Trying to connect with API and to get raw data
          Document doc = Jsoup.connect("http://openapi.airkorea.or.kr/" +
                  "openapi/services/rest/ArpltnInforInqireSvc/" +
                  "getMsrstnAcctoRltmMesureDnsty?serviceKey="+
                  "uTRaH16OBrv%2BrnhI1l%2BhctIkvNd6DwX%2FxpnCRXHGHL"+ // \
                  "jpRpVqxmQJ7Q4cXR0wucoc%2Bx3v8hg%2BsVZRvhPTzXS1xw%3D%3D"+
                  "&numOfRows=1&" +
                  "pageSize=1&" +
                  "pageNo=1&" +
                  "startPage=1&" +
                  "stationName=%EC82%AC%EC%9A%B0%EB%8F%99&" +
                  "dataTerm=DAILY&" +
                  "ver=1.3").get();

          // Checking data
          String resultCode = doc.getElementsByTag("resultCode").text().trim();

          Log.i("InitAirQualData", "resultCode: " + resultCode);
          // the data can be analyzed with the result code 00.
          if(resultCode.compareTo("00") == 0) {
              int cnt = 0;
              for(String e : airDataFormat) {
                  if(cnt != 0) {
                      airData[cnt] =
                        doc.getElementsByTag(e + "Value").text().trim();
                      airData[cnt+7] =
                        doc.getElementsByTag(e + "Grade").text().trim();
                      airData[cnt+7] =
                        airData[cnt+7].compareTo("") != 0? airData[cnt+7]:"-1";
                  } else {
                      airData[cnt] =
                        doc.getElementsByTag(e).text().trim();
                  }
                  System.out.println(airData[cnt]);
                  cnt++;
              }

              // updating data to the Firebase Database.
              DataFormat.airQualDataFormat =
                new DataFormat.AirQual(Arrays.asList(airData));

              mDatabase.child("airQualDataFormat")
                .setValue(DataFormat.airQualDataFormat);

              // printing info to the console
              Log.i("InitAirQualData",
                "Initialization Completed Successfully.");
          } else {
              // the data cannot be analyzed without the result code 00.
              String resultMessage =
                doc.getElementsByTag("resultMsg").text().trim();

              Log.e("InitAirQualData", "resultMsg: " + resultMessage);

              // printing infor to the console
              Log.e("InitAirQualData", "Initialization Failed!");

              return false;
          }

          return true;
      } catch(IOException e) {
          e.printStackTrace();
          return false;
      }
    }

    @Override
    protected void onPostExecute(Boolean isInitialized) {

    }
}
