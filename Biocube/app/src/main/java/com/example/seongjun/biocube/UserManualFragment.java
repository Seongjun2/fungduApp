package com.example.seongjun.biocube;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

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
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link UserManualFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link UserManualFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class UserManualFragment extends Fragment  {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    ViewPager pager;

    ManualsAdapter adapter;

    ImageView logo;

    private OnFragmentInteractionListener mListener;

    public UserManualFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment UserManualFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static UserManualFragment newInstance() {
        UserManualFragment fragment = new UserManualFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_user_manual, container, false);

        logo = view.findViewById(R.id.image_user_manual);

        pager = (ViewPager) view.findViewById(R.id.pager_user_manual);
        adapter = new ManualsAdapter(inflater);

        new settingManuals().execute();

        return view;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    /**
     * 쓰레드: 매뉴얼 세팅용
     */
    public class settingManuals extends AsyncTask<Object,Object,Integer> {
        Bitmap logoImg;

        // 실제 params 부분에는 execute 함수에서 넣은 인자 값이 들어 있다.
        @Override
        public Integer doInBackground(Object... params) {
            try {
             /* URL 설정하고 접속 */
                URL url = new URL("http://fungdu0624.phps.kr/biocube/manuals.php");
                HttpURLConnection http = (HttpURLConnection) url.openConnection();

            /* 전송모드 설정 */
                http.setDefaultUseCaches(false);
                http.setDoInput(true);  //서버에서 읽기 모드로 지정
                http.setDoOutput(true);    //서버에서 쓰기 모드로 지정
                http.setRequestMethod("POST");
                http.setRequestProperty("content-type", "application/x-www-form-urlencoded");   //서버에게 웹에서 <Form>으로 값이 넘어온 것과 같은 방식으로 처리하라는 걸 알려준다

            /* 서버에서 전송 받기 */
                InputStream inStream = http.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(inStream, "UTF-8"));
                String str = reader.readLine();

                inStream.close();
                http.disconnect();

                String[] token = str.split(" ");

                /* 매뉴얼 수 setting */
                adapter.setManualNum(Integer.parseInt(token[0]));
                Bitmap[] manualInitArray = new Bitmap[Integer.parseInt(token[0])];

                /* 매뉴얼 처음 이미지 setting */
                for(int i=1; i<=Integer.parseInt(token[0]); i++) {
                    String readURL = "http://fungdu0624.phps.kr/biocube/manual/" + token[i] + "/" + token[i] + ".jpg";
                    url = new URL(readURL);
                    http = (HttpURLConnection) url.openConnection();
                    http.connect();
                    //스트림생성
                    inStream = http.getInputStream();
                    //스트림에서 받은 데이터를 비트맵 변환
                    //인터넷에서 이미지 가져올 때는 Bitmap 사용해야 함
                    manualInitArray[(i-1)] = BitmapFactory.decodeStream(inStream);
                }
                inStream.close();
                http.disconnect();
                adapter.setManualInitImg(manualInitArray);

                /* 매뉴얼 화면에 로고 이미지 가져오기 */
                String readURL = "http://fungdu0624.phps.kr/biocube/img/testimg.jpg";
                url = new URL(readURL);
                http = (HttpURLConnection) url.openConnection();
                http.connect();
                //스트림생성
                inStream = http.getInputStream();
                logoImg = BitmapFactory.decodeStream(inStream);

                inStream.close();
                http.disconnect();

            } catch(MalformedURLException e) {
                e.printStackTrace();
            } catch(IOException e) {
                e.printStackTrace();
            }
            //publishProgress(params);    // 중간 중간에 진행 상태 UI 를 업데이트 하기 위해 사용..
            return -1;
        }

        @Override
        public void onPostExecute(Integer result) {
            super.onPostExecute(result);
            // Todo: doInBackground() 메소드 작업 끝난 후 처리해야할 작업..
            pager.setAdapter(adapter);
            logo.setImageBitmap(logoImg);
        }

        @Override
        protected void onProgressUpdate(Object[] values) {
            super.onProgressUpdate(values);
            // Todo: publishProgress() 메소드 호출시 처리할 작업..
        }
    }


}
