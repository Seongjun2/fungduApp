package com.example.seongjun.biocube;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.media.session.MediaSession;
import android.os.AsyncTask;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by Eunmi on 2017-11-29.
 */

public class GetId extends AsyncTask<Object,Object,String> {
    private TokenDBHelper helper;

    @Override
    protected String doInBackground(Object[] objects) {
        String user_id="";
        helper = new TokenDBHelper((Context) objects[0]);

        try {
            SQLiteDatabase db = helper.getReadableDatabase();
            Cursor cursor;  //여러 개의 데이터가 있을 때 순서대로 접근할 수 있는 포인터
            //select 구문을 실행하고 결과를 cursor에서 접근하도록 설정
            cursor = db.rawQuery("SELECT * FROM TOKEN", null);
            int count = cursor.getCount();
            cursor.moveToFirst();
            String jwt = cursor.getString(0);

            URL url = new URL("http://fungdu0624.phps.kr/biocube/getuserid.php");
            HttpURLConnection http = (HttpURLConnection) url.openConnection();

            http.setDefaultUseCaches(false);
            http.setDoInput(true);  //서버에서 읽기 모드로 지정
            http.setDoOutput(true);    //서버에서 쓰기 모드로 지정
            http.setRequestMethod("POST");
            http.setRequestProperty("content-type", "application/x-www-form-urlencoded");   //서버에게 웹에서 <Form>으로 값이 넘어온 것과 같은 방식으로 처리하라는 걸 알려준다

            StringBuffer buffer = new StringBuffer();
            buffer.append("jwt").append("=").append(jwt);
            OutputStreamWriter outStream = new OutputStreamWriter(http.getOutputStream(), "EUC-KR");
            PrintWriter writer = new PrintWriter(outStream);
            writer.write(buffer.toString());
            writer.flush();
            writer.close();

            InputStream inStream = http.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(inStream, "UTF-8"));
            user_id = reader.readLine();

            inStream.close();
            http.disconnect();
        } catch(MalformedURLException e) {
            e.printStackTrace();
        } catch(IOException e) {
            e.printStackTrace();
        }
        return user_id;
    }
}
