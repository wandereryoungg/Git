package com.example.coolweather;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.coolweather.util.HttpUtils;
import com.example.coolweather.util.Utility;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Response;

import static com.example.coolweather.R.id.choose_area_fragment;

public class WeatherActivity extends AppCompatActivity {

    private ScrollView weatherLayout;
    private TextView tvTitleCity;
    private TextView tvTitleUpdateTime;
    private TextView tvDegree;
    private TextView tvWeatherInfoText;
    private LinearLayout forecastLayout;
    private TextView aqiText;
    private TextView pm25text;
    private TextView comfortText;
    private TextView carWashText;
    private TextView sportText;
    private ImageView bingPicImg;
    SwipeRefreshLayout refreshLayout;
    private String weatherId;
    DrawerLayout drawerLayout;
    private Button btnSelect;
    private SharedPreferences sharedPreferences;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather);
        Log.d("young","onCreate");
        initViews();

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String weatherString = sharedPreferences.getString("weather",null);
        if(weatherString !=null){
            HeWeatherBean weather = Utility.handWeatherResponse(weatherString);
            weatherId = weather.getBasic().getCid();
            showWeatherInfo(weather);
        }else{
            weatherId = getIntent().getStringExtra("weather_id");
            weatherLayout.setVisibility(View.INVISIBLE);
            requestWeather(weatherId);
        }

        String bingPicImg = sharedPreferences.getString("bing_pic",null);
        if (bingPicImg != null) {
            Glide.with(this).load(bingPicImg).into(this.bingPicImg);
        }else{
            String address = "http://guolin.tech/api/bing_pic";
            requestBingPic(address);
        }

        refreshLayout.setColorSchemeResources(R.color.colorAccent,R.color.colorPrimary,R.color.colorPrimaryDark);
        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                String weatherString = sharedPreferences.getString("weather",null);
                HeWeatherBean weather = Utility.handWeatherResponse(weatherString);
                weatherId = weather.getBasic().getCid();
                requestWeather(weatherId);
            }
        });

        btnSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });
    }

    private void requestBingPic(String address) {
        HttpUtils.sendOkhttpRequest(address, new okhttp3.Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(WeatherActivity.this,"获取背景图片失败...",Toast.LENGTH_LONG).show();
                    }
                });
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                final String responseText = response.body().string();
                if(responseText !=null){
                    SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();
                    editor.putString("bing_pic",responseText);
                    editor.apply();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Glide.with(WeatherActivity.this).load(responseText).into(bingPicImg);
                        }
                    });
                }else{
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(WeatherActivity.this,"获取背景图片失败...",Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }
        });

        drawerLayout.addDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(@NonNull View drawerView, float slideOffset) {
                Log.d("young","onDrawerSlide");
            }

            @Override
            public void onDrawerOpened(@NonNull View drawerView) {
                btnSelect.setVisibility(View.GONE);
                Log.d("young","onDrawerOpened");
            }

            @Override
            public void onDrawerClosed(@NonNull View drawerView) {
                btnSelect.setVisibility(View.VISIBLE);
                Log.d("young","onDrawerClosed");
            }

            @Override
            public void onDrawerStateChanged(int newState) {
                Log.d("young","onDrawerStateChanged");

            }
        });

    }


    void requestWeather(String weatherId) {
        String address = "http://guolin.tech/api/weather?cityid="+weatherId+"&key=18b06362269d4626ae124682ecf7d6d7";
        HttpUtils.sendOkhttpRequest(address, new okhttp3.Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(WeatherActivity.this,"获取天气信息失败...",Toast.LENGTH_LONG).show();
                        refreshLayout.setRefreshing(false);
                    }
                });
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                final String responseText = response.body().string();
                final HeWeatherBean weather = Utility.handWeatherResponse(responseText);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(weather !=null & "ok".equals(weather.getStatus())){
                            SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();
                            editor.putString("weather",responseText);
                            editor.apply();
                            showWeatherInfo(weather);
                        }else {
                            Toast.makeText(WeatherActivity.this,"获取天气信息失败...",Toast.LENGTH_LONG).show();
                        }
                        refreshLayout.setRefreshing(false);
                    }
                });


            }
        });
    }



    private void initViews() {
        weatherLayout = findViewById(R.id.weather_layout);
        tvTitleCity = findViewById(R.id.title_city);
        tvTitleUpdateTime = findViewById(R.id.title_update_time);
        tvDegree = findViewById(R.id.degree_text);
        tvWeatherInfoText = findViewById(R.id.weather_info_text);
        forecastLayout = findViewById(R.id.forecast_layout);
        aqiText = findViewById(R.id.aqi_text);
        pm25text = findViewById(R.id.pm25_text);
        comfortText = findViewById(R.id.comfort_text);
        carWashText = findViewById(R.id.carwash_text);
        sportText = findViewById(R.id.sport_text);
        bingPicImg = findViewById(R.id.bing_pic_img);
        refreshLayout = findViewById(R.id.swipeRefreshLayout);
        drawerLayout = findViewById(R.id.drawerLayout);
        btnSelect = findViewById(R.id.btn_select);
    }

    private void showWeatherInfo(HeWeatherBean weather) {
        tvTitleCity.setText(weather.getBasic().getCity());
        tvTitleUpdateTime.setText(weather.getBasic().getUpdate().getLoc().split(" ")[1]);
        tvDegree.setText(weather.getNow().getTmp()+"℃");
        tvWeatherInfoText.setText(weather.getNow().getCond().getTxt());
        forecastLayout.removeAllViews();
        for(HeWeatherBean.DailyForecastBean forecast: weather.getDaily_forecast()){
            View view = LayoutInflater.from(this).inflate(R.layout.forecast_item,forecastLayout,false);
            TextView dateText = view.findViewById(R.id.date_text);
            TextView infoText = view.findViewById(R.id.info_text);
            TextView maxText = view.findViewById(R.id.max_text);
            TextView minText = view.findViewById(R.id.min_text);
            dateText.setText(forecast.getDate());
            infoText.setText(forecast.getCond().getTxt_d());
            maxText.setText(forecast.getTmp().getMax());
            minText.setText(forecast.getTmp().getMin());
            forecastLayout.addView(view);

        }
        if(weather.getAqi() !=null){
            aqiText.setText(weather.getAqi().getCity().getAqi());
            pm25text.setText(weather.getAqi().getCity().getPm25());
        }
        String comfort = "舒适度: "+weather.getSuggestion().getComf().getTxt();
        String carWash = "洗车指数: "+weather.getSuggestion().getCw().getTxt();
        String sport = "运动指数: "+weather.getSuggestion().getSport().getTxt();

        comfortText.setText(comfort);
        carWashText.setText(carWash);
        sportText.setText(sport);
        weatherLayout.setVisibility(View.VISIBLE);

        Intent intent = new Intent(this,AutoUpdateService.class);
        startService(intent);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d("young","onStart");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d("young","onResume");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d("young","onPause");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d("young","onStop");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d("young","onDestroy");
    }
}
