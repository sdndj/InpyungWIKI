package com.example.leedongjin.myapplication;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener {

    private SwipeRefreshLayout refreshLayout;
    private EditText editText;
    ListView list;

    String myJSON;
    ArrayList<String> st = new ArrayList<String>();

    private static final String TAG_RESULTS = "results";
    private static final String TAG_NAME = "name";

    JSONArray peoples = null;
    ListAdapter adapter;

    ArrayList<HashMap<String, String>> personList;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        editText = (EditText) findViewById(R.id.name_edittext);
        refreshLayout = (SwipeRefreshLayout) findViewById(R.id.swype_layout); //당겨서 새로고침
        list = (ListView) findViewById(R.id.listView); //데이터를 보여줄 리스트뷰
        personList = new ArrayList<HashMap<String, String>>(); //긁어온 데이터를 어레이리스트에 넣어줌
        refreshLayout.setOnRefreshListener(this);
        getData("http://dongjin.yangs.party/www/html/getdata.php");


        list.setOnItemClickListener(listener);
    }

    AdapterView.OnItemClickListener listener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Intent i = new Intent(getApplicationContext(),SelectActivity.class);
            i.putExtra("name",st.get(position));
            startActivity(i);
        }
    };

    //버튼 원클릭 시 insert되는 부분
    public void insert(View view) {
        String name = editText.getText().toString();
        insertToDatabase(name);
        onRefresh();
    }

    //데이터 입력하는 부분
    private void insertToDatabase(String name) {
        name = name.trim();
        if (name.getBytes().length <= 0) {
            Toast.makeText(getApplicationContext(), "글을 입력하세요.", Toast.LENGTH_SHORT).show();
        } else {
            class InsertData extends AsyncTask<String, Void, String> {
                ProgressDialog loading;


                @Override
                protected void onPreExecute() {
                    super.onPreExecute();
                    loading = ProgressDialog.show(MainActivity.this, "Please Wait", null, true, true);
                }

                @Override
                protected void onPostExecute(String s) {
                    super.onPostExecute(s);
                    loading.dismiss();
                    Toast.makeText(getApplicationContext(), "등록 성공!", Toast.LENGTH_LONG).show();
                }

                @Override
                protected String doInBackground(String... params) {
                    try {
                        String name = (String) params[0];

                        String link = "http://dongjin.yangs.party/www/html/insert.php";
                        String data = URLEncoder.encode("name", "UTF-8") + "=" + URLEncoder.encode(name, "UTF-8");

                        URL url = new URL(link);
                        URLConnection conn = url.openConnection();

                        conn.setDoOutput(true);
                        OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());

                        wr.write(data);
                        wr.flush();

                        BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));

                        StringBuilder sb = new StringBuilder();
                        String line = null;

                        // Read Server Response
                        while ((line = reader.readLine()) != null) {
                            sb.append(line);
                            break;
                        }
                        return sb.toString();
                    } catch (Exception e) {
                        return new String("Exception: " + e.getMessage());
                    }
                }
            }
            InsertData task = new InsertData();
            task.execute(name);
        }

    }

    //디비에서 php에 작성된 태그를 이용하여 json으로 긁어옴
    protected void showList() {
        try {
            JSONObject jsonObj = new JSONObject(myJSON);
            peoples = jsonObj.getJSONArray(TAG_RESULTS); //results 태그로 전체목록 긁어옴

            for (int i = 0; i < peoples.length(); i++) { // 전체목록 수만큼 데이터를 personList에 리스트 추가시킴
                JSONObject c = peoples.getJSONObject(i);
//                String id = c.getString(TAG_ID);
                String name = c.getString(TAG_NAME);
                st.add(name);
                HashMap<String, String> persons = new HashMap<String, String>();

//                persons.put(TAG_ID,id);
                persons.put(TAG_NAME, name); //name 태그 이용해서 name이 붙은 데이터를 persons에 넣어줌
                personList.add(persons);
            }

            adapter = new SimpleAdapter(
                    MainActivity.this, personList, R.layout.listitem,
                    new String[]{TAG_NAME},
                    new int[]{R.id.name}
            );
            list.setAdapter(adapter);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    //데이터를 읽어오는 주소를 연결하는 메소드
    public void getData(String url) {
        class GetDataJSON extends AsyncTask<String, Void, String> {

            @Override
            protected String doInBackground(String... params) {
                String uri = params[0];

                BufferedReader bufferedReader = null;
                try {
                    URL url = new URL(uri);
                    HttpURLConnection con = (HttpURLConnection) url.openConnection();
                    StringBuilder sb = new StringBuilder();

                    bufferedReader = new BufferedReader(new InputStreamReader(con.getInputStream()));

                    String json;
                    while ((json = bufferedReader.readLine()) != null) {
                        sb.append(json + "\n");
                    }
                    return sb.toString().trim();
                } catch (Exception e) {
                    return null;
                }
            }

            @Override
            protected void onPostExecute(String result) {
                myJSON = result;
                showList();
            }
        }
        GetDataJSON g = new GetDataJSON();
        g.execute(url);
    }

    //당겼을 때 새로고침 실행되는 부분
    @Override
    public void onRefresh() {
        personList.clear();
        getData("http://dongjin.yangs.party/www/html/getdata.php");
        refreshLayout.setRefreshing(false);
    }
}