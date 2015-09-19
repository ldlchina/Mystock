package com.example.rui.mystock;

import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.app.NotificationCompat;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.Collection;
import java.util.HashSet;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeMap;
import java.util.Vector;


public class MainActivity extends AppCompatActivity {

    private static HashSet<String> StockIds_ = new HashSet();
    private static Vector<String> SelectedStockItems_ = new Vector();
    private final static int BackgroundColor_ = Color.WHITE;
    private final static int HighlightColor_ = Color.rgb(210, 233, 255);
    private final static String ShIndex = "sh000001";
    private final static String SzIndex = "sz399001";
    private final static String ChuangIndex = "sz399006";
    private final static String StockIdsKey_ = "StockIds";
    private final static int StockLargeTrade_ = 1000000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        String idsStr = sharedPref.getString(StockIdsKey_, ShIndex + "," + SzIndex + "," + ChuangIndex);

        String[] ids = idsStr.split(",");
        StockIds_.clear();
        for (String id : ids) {
            StockIds_.add(id);
        }

        Timer timer = new Timer("RefreshStocks");
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                refreshStocks();
            }
        }, 0, 2000);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();  // Always call the superclass

        saveStocksToPreferences();
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        saveStocksToPreferences();

        // Always call the superclass so it can save the view hierarchy state
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        else if(id == R.id.action_delete){
            if(SelectedStockItems_.isEmpty())
                return true;

            for (String selectedId : SelectedStockItems_){
                StockIds_.remove(selectedId);
                TableLayout table = (TableLayout)findViewById(R.id.stock_table);
                int count = table.getChildCount();
                for (int i = 1; i < count; i++){
                    TableRow row = (TableRow)table.getChildAt(i);
                    LinearLayout nameId = (LinearLayout)row.getChildAt(0);
                    TextView idText = (TextView)nameId.getChildAt(1);
                    if(idText != null && idText.getText().toString() == selectedId){
                        table.removeView(row);
                        break;
                    }
                }
            }

            SelectedStockItems_.clear();
        }

        return super.onOptionsItemSelected(item);
    }

    private void saveStocksToPreferences(){
        String ids = "";
        for (String id : StockIds_){
            ids += id;
            ids += ",";
        }

        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(StockIdsKey_, ids);
        editor.commit();
    }

    // 浦发银行,15.06,15.16,15.25,15.27,14.96,15.22,15.24,205749026,3113080980,
    // 51800,15.22,55979,15.21,1404740,15.20,1016176,15.19,187800,15.18,300,15.24,457700,15.25,548900,15.26,712266,15.27,1057960,15.28,2015-09-10,15:04:07,00
    public class Stock {
        public String id_, name_;
        public String open_, yesterday_, now_, high_, low_;
        public String b1_, b2_, b3_, b4_, b5_;
        public String bp1_, bp2_, bp3_, bp4_, bp5_;
        public String s1_, s2_, s3_, s4_, s5_;
        public String sp1_, sp2_, sp3_, sp4_, sp5_;
        public String time_;
    }

    public TreeMap<String, Stock> sinaResponseToStocks(String response){
        response = response.replaceAll("\n", "");
        String[] stocks = response.split(";");

        TreeMap<String, Stock> stockMap = new TreeMap();
        for(String stock : stocks) {
            String[] leftRight = stock.split("=");
            if (leftRight.length < 2)
                continue;

            String right = leftRight[1].replaceAll("\"", "");
            if (right.isEmpty())
                continue;

            String left = leftRight[0];
            if (left.isEmpty())
                continue;

            Stock stockNow = new Stock();
            stockNow.id_ = left.split("_")[2];

            String[] values = right.split(",");
            stockNow.name_ = values[0];
            stockNow.open_ = values[1];
            stockNow.yesterday_ = values[2];
            stockNow.now_ = values[3];
            stockNow.high_ = values[4];
            stockNow.low_ = values[5];
            stockNow.b1_ = values[10];
            stockNow.b2_ = values[12];
            stockNow.b3_ = values[14];
            stockNow.b4_ = values[16];
            stockNow.b5_ = values[18];
            stockNow.bp1_ = values[11];
            stockNow.bp2_ = values[13];
            stockNow.bp3_ = values[15];
            stockNow.bp4_ = values[17];
            stockNow.bp5_ = values[19];
            stockNow.s1_ = values[20];
            stockNow.s2_ = values[22];
            stockNow.s3_ = values[24];
            stockNow.s4_ = values[26];
            stockNow.s5_ = values[28];
            stockNow.sp1_ = values[21];
            stockNow.sp2_ = values[23];
            stockNow.sp3_ = values[25];
            stockNow.sp4_ = values[27];
            stockNow.sp5_ = values[29];
            stockNow.time_ = values[values.length - 3] + "_" + values[values.length - 2];
            stockMap.put(stockNow.id_, stockNow);
        }

        return stockMap;
    }

    public void querySinaStocks(String list){
        // Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(this);
        String url ="http://hq.sinajs.cn/list=" + list;
        //http://hq.sinajs.cn/list=sh600000,sh600536

        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        updateStockListView(sinaResponseToStocks(response));
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                    }
                });

        queue.add(stringRequest);
    }

    private void refreshStocks(){
        String ids = "";
        for (String id : StockIds_){
            ids += id;
            ids += ",";
        }
        querySinaStocks(ids);
    }

    public void addStock(View view) {
        EditText editText = (EditText) findViewById(R.id.editText_stockId);
        String stockId = editText.getText().toString();
        if(stockId.length() != 6)
            return;

        if (stockId.startsWith("6")) {
            stockId = "sh" + stockId;
        } else if (stockId.startsWith("0") || stockId.startsWith("3")) {
            stockId = "sz" + stockId;
        } else
            return;

        StockIds_.add(stockId);
    }

    public void sendNotifation(int id, String title, String text){
        NotificationCompat.Builder nBuilder =
                new NotificationCompat.Builder(this);
        nBuilder.setSmallIcon(R.drawable.ic_launcher);
        nBuilder.setContentTitle(title);
        nBuilder.setContentText(text);
        nBuilder.setVibrate(new long[]{100, 100, 100});
        nBuilder.setLights(Color.RED, 1000, 1000);

        NotificationManager notifyMgr = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notifyMgr.notify(id, nBuilder.build());
    }

    public void updateStockListView(TreeMap<String, Stock> stockMap){
//        String stock_name = getResources().getString(R.string.stock_name);
//        String stock_id = getResources().getString(R.string.stock_id);
//        String stock_now = getResources().getString(R.string.stock_now);

//        ListView list = (ListView) findViewById(R.id.listView);
//
//        ArrayList<HashMap<String, String>> stockList = new ArrayList<>();
//        HashMap<String, String> mapTitle = new HashMap<>();
//        mapTitle.put(stock_name, getResources().getString(R.string.stock_name_title));
//        //mapTitle.put(stock_id, "");
//        mapTitle.put(stock_now, getResources().getString(R.string.stock_now_title));
//        stockList.add(mapTitle);
//
//        for(Stock stock : stocks)
//        {
//            HashMap<String, String> map = new HashMap<>();
//            map.put(stock_name, stock.name_);
//            String id = stock.id_.replaceAll("sh", "");
//            id = id.replaceAll("sz", "");
//            map.put(stock_id, id);
//            map.put(stock_now, stock.now_);
//            stockList.add(map);
//        }
//
//        SimpleAdapter adapter = new SimpleAdapter(this,
//                stockList,
//                R.layout.stock_listitem,
//                new String[] {stock_name, stock_id, stock_now},
//                new int[] {R.id.stock_name, R.id.stock_id, R.id.stock_now});
//        list.setAdapter(adapter);

        // Table
        TableLayout table = (TableLayout)findViewById(R.id.stock_table);
        table.setStretchAllColumns(true);
        table.setShrinkAllColumns(true);
        table.removeAllViews();

        // Title
        TableRow rowTitle = new TableRow(this);
        TextView nameTitle = new TextView(this);
        nameTitle.setText(getResources().getString(R.string.stock_name_title));
        rowTitle.addView(nameTitle);
        TextView nowTitle = new TextView(this);
        nowTitle.setGravity(Gravity.CENTER);
        nowTitle.setText(getResources().getString(R.string.stock_now_title));
        rowTitle.addView(nowTitle);
        TextView percentTitle = new TextView(this);
        percentTitle.setGravity(Gravity.CENTER);
        percentTitle.setText(getResources().getString(R.string.stock_increase_percent_title));
        rowTitle.addView(percentTitle);
        TextView increaseTitle = new TextView(this);
        increaseTitle.setGravity(Gravity.CENTER);
        increaseTitle.setText(getResources().getString(R.string.stock_increase_title));
        rowTitle.addView(increaseTitle);
        table.addView(rowTitle);

        Collection<Stock> stocks = stockMap.values();
        for(Stock stock : stocks)
        {
            if(stock.id_.equals(ShIndex) || stock.id_.equals(SzIndex) || stock.id_.equals(ChuangIndex)){
                Double dNow = Double.parseDouble(stock.now_);
                Double dYesterday = Double.parseDouble(stock.yesterday_);
                Double dIncrease = dNow - dYesterday;
                Double dPercent = dIncrease / dYesterday * 100;
                String change = String.format("%.2f", dPercent) + "% " + String.format("%.2f", dIncrease);

                int indexId;
                int changeId;
                if(stock.id_.equals(ShIndex)) {
                    indexId = R.id.stock_sh_index;
                    changeId = R.id.stock_sh_change;
                }
                else if(stock.id_.equals(SzIndex)) {
                    indexId = R.id.stock_sz_index;
                    changeId = R.id.stock_sz_change;
                }
                else{
                    indexId = R.id.stock_chuang_index;
                    changeId = R.id.stock_chuang_change;
                }

                TextView indexText = (TextView)findViewById(indexId);
                indexText.setText(stock.now_);
                int color = Color.BLACK;
                if(dIncrease > 0) {
                    color = Color.RED;
                }
                else if(dIncrease < 0){
                    color = Color.GREEN;
                }
                indexText.setTextColor(color);

                TextView changeText = (TextView)findViewById(changeId);
                changeText.setText(change);

                continue;
            }

            TableRow row = new TableRow(this);
            row.setGravity(Gravity.CENTER_VERTICAL);
            if (SelectedStockItems_.contains(stock.id_)){
                row.setBackgroundColor(HighlightColor_);
            }

            LinearLayout nameId = new LinearLayout(this);
            nameId.setOrientation(LinearLayout.VERTICAL);
            TextView name = new TextView(this);
            name.setText(stock.name_);
            nameId.addView(name);
            TextView id = new TextView(this);
            id.setTextSize(10);
            id.setText(stock.id_);
            nameId.addView(id);
            row.addView(nameId);

            TextView now = new TextView(this);
            now.setGravity(Gravity.RIGHT);
            now.setText(stock.now_);
            row.addView(now);

            TextView percent = new TextView(this);
            percent.setGravity(Gravity.RIGHT);
            TextView increaseValue = new TextView(this);
            increaseValue.setGravity(Gravity.RIGHT);
            Double dOpen = Double.parseDouble(stock.open_);
            Double dB1 = Double.parseDouble(stock.bp1_);
            Double dS1 = Double.parseDouble(stock.sp1_);
            if(dOpen == 0 && dB1 == 0 && dS1 == 0) {
                percent.setText("--");
                increaseValue.setText("--");
            }
            else{
                Double dNow = Double.parseDouble(stock.now_);
                if(dNow == 0) {// before open
                    if(dS1 == 0) {
                        dNow = dB1;
                        now.setText(stock.bp1_);
                    }
                    else {
                        dNow = dS1;
                        now.setText(stock.sp1_);
                    }
                }
                Double dYesterday = Double.parseDouble(stock.yesterday_);
                Double dIncrease = dNow - dYesterday;
                Double dPercent = dIncrease / dYesterday * 100;
                percent.setText(String.format("%.2f", dPercent) + "%");
                increaseValue.setText(String.format("%.2f", dIncrease));
                int color = Color.BLACK;
                if(dIncrease > 0) {
                    color = Color.RED;
                }
                else if(dIncrease < 0){
                    color = Color.GREEN;
                }

                now.setTextColor(color);
                percent.setTextColor(color);
                increaseValue.setTextColor(color);
            }
            row.addView(percent);
            row.addView(increaseValue);
            row.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ViewGroup group = (ViewGroup) v;
                    ViewGroup nameId = (ViewGroup) group.getChildAt(0);
                    TextView idText = (TextView) nameId.getChildAt(1);
                    if (SelectedStockItems_.contains(idText.getText().toString())) {
                        v.setBackgroundColor(BackgroundColor_);
                        SelectedStockItems_.remove(idText.getText().toString());
                    } else {
                        v.setBackgroundColor(HighlightColor_);
                        SelectedStockItems_.add(idText.getText().toString());
                    }
                }
            });

            table.addView(row);

            String sid = stock.id_;
            sid = sid.replaceAll("sh", "");
            sid = sid.replaceAll("sz", "");

            String text = "";
            String sBuy = getResources().getString(R.string.stock_buy);
            String sSell = getResources().getString(R.string.stock_sell);
            if(Double.parseDouble(stock.b1_ )>= StockLargeTrade_) {
                text += sBuy + "1:" + stock.b1_ + ",";
            }
            if(Double.parseDouble(stock.b2_ )>= StockLargeTrade_) {
                text += sBuy + "2:" + stock.b2_ + ",";
            }
            if(Double.parseDouble(stock.b3_ )>= StockLargeTrade_) {
                text += sBuy + "3:" + stock.b3_ + ",";
            }
            if(Double.parseDouble(stock.b4_ )>= StockLargeTrade_) {
                text += sBuy + "4:" + stock.b4_ + ",";
            }
            if(Double.parseDouble(stock.b5_ )>= StockLargeTrade_) {
                text += sBuy + "5:" + stock.b5_ + ",";
            }
            if(Double.parseDouble(stock.s1_ )>= StockLargeTrade_) {
                text += sSell + "1:" + stock.s1_ + ",";
            }
            if(Double.parseDouble(stock.s2_ )>= StockLargeTrade_) {
                text += sSell + "2:" + stock.s2_ + ",";
            }
            if(Double.parseDouble(stock.s3_ )>= StockLargeTrade_) {
                text += sSell + "3:" + stock.s3_ + ",";
            }
            if(Double.parseDouble(stock.s4_ )>= StockLargeTrade_) {
                text += sSell + "4:" + stock.s4_ + ",";
            }
            if(Double.parseDouble(stock.s5_ )>= StockLargeTrade_) {
                text += sSell + "5:" + stock.s5_ + ",";
            }
            if(text.length() > 0)
                sendNotifation(Integer.parseInt(sid), stock.name_, text);
        }
    }
}
