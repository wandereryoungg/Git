package com.example.coolweather;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.coolweather.db.City;
import com.example.coolweather.db.County;
import com.example.coolweather.db.Province;
import com.example.coolweather.util.HttpUtils;
import com.example.coolweather.util.Utility;

import org.jetbrains.annotations.NotNull;
import org.litepal.crud.DataSupport;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Response;

public class ChooseAreaFragment extends Fragment {

    public static final int LEVEL_PROVINCE = 0;
    public static final int LEVEL_CITY = 1;
    public static final int LEVEL_COUNTY = 2;
    private int level;
    Button btnBack;
    TextView tvTitle;
    ListView listView;
    ArrayAdapter arrayAdapter;
    private List<Province> provinceList;
    private List<City> cityList;
    private List<County> countyList;
    private Province selectedProvince;
    private City selectedCity;
    private County selectedCounty;
    private List<String> dataList = new ArrayList<>();
    private ProgressDialog progressDialog;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.choose_area_fragment, container, false);
        btnBack = view.findViewById(R.id.btn_back);
        tvTitle = view.findViewById(R.id.tv_title);
        listView = view.findViewById(R.id.listview);
        arrayAdapter = new ArrayAdapter(getContext(), android.R.layout.simple_list_item_1,dataList);
        listView.setAdapter(arrayAdapter);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(level == LEVEL_PROVINCE){
                    selectedProvince = provinceList.get(position);
                    queryCities();
                }else if(level == LEVEL_CITY){
                    selectedCity = cityList.get(position);
                    queryCounties();
                }else if(level == LEVEL_COUNTY){
                    String weatherId = countyList.get(position).getWeatherId();
                    if(getActivity()instanceof MainActivity){
                        Intent intent = new Intent(getActivity(),WeatherActivity.class);
                        intent.putExtra("weather_id",weatherId);
                        startActivity(intent);
                        getActivity().finish();
                    }else if(getActivity()instanceof WeatherActivity){
                        ((WeatherActivity) getActivity()).drawerLayout.closeDrawers();
                        ((WeatherActivity) getActivity()).refreshLayout.setRefreshing(true);
                        ((WeatherActivity) getActivity()).requestWeather(weatherId);
                    }
                }
            }
        });
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(level == LEVEL_COUNTY){
                    queryCities();
                }else if(level == LEVEL_CITY){
                    queryProvinces();
                }
            }
        });
        queryProvinces();
    }

    private void queryCounties() {
        btnBack.setVisibility(View.VISIBLE);
        tvTitle.setText(selectedCity.getCityName());
        countyList = DataSupport.where("cityId=?",String.valueOf(selectedCity.getCityId())).find(County.class);
        if(countyList.size()>0){
            dataList.clear();
            for(County county:countyList){
                dataList.add(county.getCountyName());
            }
            arrayAdapter.notifyDataSetChanged();
            listView.setSelection(0);
            level = LEVEL_COUNTY;
        }else{
            String address = "http://guolin.tech/api/china"+"/"+selectedProvince.getProvinceId()+"/"+selectedCity.getCityId();
            queryFromServer(address,"county");
        }

    }

    private void queryCities() {
        btnBack.setVisibility(View.VISIBLE);
        tvTitle.setText(selectedProvince.getProvinceName());
        cityList = DataSupport.where("provinceId=?", String.valueOf(selectedProvince.getProvinceId())).find(City.class);
        if(cityList.size()>0){
            dataList.clear();
            for(City city:cityList){
                dataList.add(city.getCityName());
            }
            arrayAdapter.notifyDataSetChanged();
            listView.setSelection(0);
            level = LEVEL_CITY;
        }else{
            String address = "http://guolin.tech/api/china"+"/"+selectedProvince.getProvinceId();
            queryFromServer(address,"city");
        }
    }

    private void queryProvinces() {
        btnBack.setVisibility(View.GONE);
        tvTitle.setText("中国");
        provinceList = DataSupport.findAll(Province.class);
        if(provinceList.size()>0){
            dataList.clear();
            for(Province province:provinceList){
                dataList.add(province.getProvinceName());
            }
            arrayAdapter.notifyDataSetChanged();
            listView.setSelection(0);
            level = LEVEL_PROVINCE;
        }else{
            String address = "http://guolin.tech/api/china";
            queryFromServer(address, "province");
        }
    }

    private void queryFromServer(String address, final String type) {
        HttpUtils.sendOkhttpRequest(address, new okhttp3.Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        closeProgress();
                        Toast.makeText(getContext(),"loading fail....",Toast.LENGTH_LONG).show();
                    }
                });
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                showProgress();
                boolean result = false;
                if ("province".equals(type)) {
                    String responseText = response.body().string();
                    result = Utility.handleProvinceResponse(responseText);
                }else if("city".equals(type)){
                    String responseText = response.body().string();
                    result = Utility.handleCityResponse(responseText, selectedProvince.getProvinceId());
                } else if ("county".equals(type)) {
                    String responseText = response.body().string();
                    result = Utility.handleCountyResponse(responseText, selectedCity.getCityId());
                }
                if(result){
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            closeProgress();
                            if ("province".equals(type)) {
                                queryProvinces();
                            }else if("city".equals(type)){
                                queryCities();
                            }else if("county".equals(type)){
                                queryCounties();
                            }
                        }
                    });
                }
            }
        });
    }

    private void closeProgress() {
        if(progressDialog !=null){
            progressDialog.dismiss();
        }
    }

    private void showProgress() {
        if(progressDialog == null){
            progressDialog = new ProgressDialog(getContext());
            progressDialog.setMessage("loading");
            progressDialog.setCanceledOnTouchOutside(false);
        }
        progressDialog.show();
    }
}
