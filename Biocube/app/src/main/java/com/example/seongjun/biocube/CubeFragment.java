package com.example.seongjun.biocube;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
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
import java.net.URLEncoder;
import java.util.Set;
import java.util.concurrent.ExecutionException;



/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link CubeFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link CubeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CubeFragment extends Fragment {

    private OnFragmentInteractionListener mListener;
    String id;//로그인된 id를 받을 것.
    Bluetooth mBluetooth = new Bluetooth();
    Set<BluetoothDevice> mDevices;
    BluetoothAdapter mBluetoothAdapter;
    int mPariedDeviceCount = 0;

    TextView text_temper;
    TextView text_humi_air;
    TextView text_humi_soil;
    TextView text_motor;
    TextView text_led;
    Spinner spinner_cubeName;

    private int readBufferPosition;
    byte[] readBuffer;
    Thread mWorkerThread = null;
    InputStream mInputStream = null;
    char mCharDelimiter =  '\n';

    public CubeFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment CubeFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static CubeFragment newInstance() {
        CubeFragment fragment = new CubeFragment();
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
        View view = inflater.inflate(R.layout.fragment_cube, container, false);
        spinner_cubeName = (Spinner) view.findViewById(R.id.spinner_cube_cubeselect);
        view.findViewById(R.id.btn_connect).setOnClickListener(connectClickListener);
        view.findViewById(R.id.btn_led).setOnClickListener(setLedClickListener);
        view.findViewById(R.id.btn_pump).setOnClickListener(setPumpClickListener);
        view.findViewById(R.id.btn_ledTime).setOnClickListener(setLedTimeClickListener);
        view.findViewById(R.id.btn_pumpTime).setOnClickListener(setPumpTimeClickListener);
        text_temper = (TextView) view.findViewById(R.id.text_temp);
        text_humi_air = (TextView) view.findViewById(R.id.text_humi_air);
        text_humi_soil = (TextView) view.findViewById(R.id.text_humi_soil);
        text_motor = (TextView) view.findViewById(R.id.text_motor);
        text_led = (TextView) view.findViewById(R.id.text_led);
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();//블루투스 목록을 불러옴

        try {//로그인된 아이디 불러옴
            id = new GetId().execute(getActivity()).get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        /* Toolbar 설정 */
        Toolbar toolbar = (Toolbar) view.findViewById(R.id.toolbar_cube);
        ((AppCompatActivity)getActivity()).setSupportActionBar(toolbar);
        ActionBar actionBar = ((AppCompatActivity)getActivity()).getSupportActionBar();
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setDisplayShowTitleEnabled(false);

        Button registCube = (Button) view.findViewById(R.id.btn_cube_regist);
        registCube.setOnClickListener(new View.OnClickListener() {//등록 버튼 누를시
            @Override
            public void onClick(View v) {
                Intent intent;
                intent = new Intent(v.getContext(), CubeRegister.class);//큐브등록을 위해 검색창으로 이동
                startActivity(intent);
            }
        });
        setSpinner();//스피너를 셋팅.
        return view;
    }

    public void setSpinner(){//큐브 목록을 spinner에 설정하는 메소드
        String[] cubeList;

        try {
            String[] getList = new ReturnCubeList().execute(id).get();//자신의 큐브 목록을 불러옴
            cubeList = new String[getList.length-1];
            for(int i=0; i<getList.length-1; i++) {
                cubeList[i] = getList[i+1].toString();
            }
            ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_item, cubeList);
            dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinner_cubeName.setAdapter(dataAdapter);//adapter를 통해 spinner에 셋팅
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }

    ImageButton.OnClickListener setLedClickListener = new ImageButton.OnClickListener(){//LED 버튼 눌렀을 때
        @Override
        public void onClick(View v) {
            if(!id.equals("admin")) {
                ((UserMainActivity) getActivity()).mBluetooth.sendData("led");
            }
            else{
                ((AdminMainActivity) getActivity()).mBluetooth.sendData("led");
            }
            //권한에 맞는 블루투스 객체를 가져와서 'led'라는 신호를 device에 보냄.
        }
    };

    ImageButton.OnClickListener setPumpClickListener = new ImageButton.OnClickListener(){//Pump 버튼 눌렀을 때,

        @Override
        public void onClick(View v) {
            if(!id.equals("admin")) {
                ((UserMainActivity) getActivity()).mBluetooth.sendData("pump");
            }
            else{
                ((AdminMainActivity) getActivity()).mBluetooth.sendData("pump");
            }
            //권한에 맞는 블루투스 객체를 가져와서 'pump'라는 신호를 보냄.
        }
    };

    Button.OnClickListener setLedTimeClickListener = new Button.OnClickListener(){
        //led 시간세팅하는 부분 구현X
        @Override
        public void onClick(View v) {

        }
    };

    Button.OnClickListener setPumpTimeClickListener = new Button.OnClickListener(){
        //pump 시간세팅하는 부분 구현X
        @Override
        public void onClick(View v) {

        }
    };

    Button.OnClickListener connectClickListener= new Button.OnClickListener(){//연결 버튼 눌렀을 때,
        @Override
        public void onClick(View v) {
            String selectedCube = spinner_cubeName.getSelectedItem().toString();//spinner에 선택된 큐브를 가져옴.
            try{
                selectedCube = URLEncoder.encode(selectedCube,"UTF-8");//한글 에러 막기 위해 인코딩
            } catch(Exception e) {
                e.printStackTrace();
            }

            new GetDevice().execute(selectedCube);//선택한 큐브에 대한 device를 얻어와서 블루투스 연결.
        }
    };


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

    class GetDevice extends AsyncTask<String, Void, String> {//디바이스를 찾아 연결시키는 쓰레드.
        @Override
        protected String doInBackground(String... params) {
            String device= "";
            try {
         /* URL 설정하고 접속 */
                URL url = new URL("http://fungdu0624.phps.kr/biocube/getdevice.php");//php 파일 연결.
                HttpURLConnection http = (HttpURLConnection) url.openConnection();

        /* 전송모드 설정 */
                http.setDefaultUseCaches(false);
                http.setDoInput(true);  //서버에서 읽기 모드로 지정
                http.setDoOutput(true);    //서버에서 쓰기 모드로 지정
                http.setRequestMethod("POST");
                http.setRequestProperty("content-type", "application/x-www-form-urlencoded");
                //서버에게 웹에서 <Form>으로 값이 넘어온 것과 같은 방식으로 처리하라는 걸 알려준다

        /* 서버로 값 전송 */
                StringBuffer buffer = new StringBuffer();
                buffer.append("user_id").append("=").append(id).append("&");//사용자 아이디와 큐브이름을 php파일에 넘김.
                buffer.append("cubename").append("=").append(params[0].toString());

                OutputStreamWriter outStream = new OutputStreamWriter(http.getOutputStream(), "EUC-KR");
                PrintWriter writer = new PrintWriter(outStream);
                writer.write(buffer.toString());
                writer.flush();
                writer.close();

        /* 서버에서 전송 받기 */
                InputStream inStream = http.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(inStream, "UTF-8"));
                device = reader.readLine();//php결과로 선택한 큐브에 매칭되는 device의 mac 주소를 가져옴
            } catch(MalformedURLException e) {
                e.printStackTrace();
            } catch(IOException e) {
                e.printStackTrace();
            }
            return device;
        }

        @Override
        protected void onPostExecute(String result){//가져온 디바이스 넘버를 통해 블루투스 연결.
            String deviceNum = result;
            if(!id.equals("admin")) {//권한이 admin일 때,
                switch (((UserMainActivity) getActivity()).mBluetooth.checkBluetooth(getContext())) {
                    case 0:
                        Toast.makeText(getContext(), "기기가 블루투스를 지원하지 않습니다.", Toast.LENGTH_LONG).show();
                        break;
                    case 1:
                        Toast.makeText(getContext(), "현재 블루투스가 비활성 상태입니다.", Toast.LENGTH_LONG).show();
                        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                        startActivityForResult(enableBtIntent, mBluetooth.REQUEST_ENABLE_BT);
                        break;
                    case 2:
                        mDevices = mBluetoothAdapter.getBondedDevices();
                        mPariedDeviceCount = mDevices.size();
                }
                BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(deviceNum);
                if (((UserMainActivity) getActivity()).mBluetooth.connectToSelectedDevice(deviceNum, mDevices, device)) {
                    beginListenForData_1();
                    ((UserMainActivity) getActivity()).mBluetooth.sendData("connect");
                } else {
                    Toast.makeText(getContext(), "블루투스 연결 중 오류가 발생했습니다.", Toast.LENGTH_LONG).show();
                }
            } else{//권한이 일반 user일 때,
                switch (((AdminMainActivity)getActivity()).mBluetooth.checkBluetooth(getContext())){
                    case 0: Toast.makeText(getContext(), "기기가 블루투스를 지원하지 않습니다.", Toast.LENGTH_LONG).show();
                        break;
                    case 1: Toast.makeText(getContext(), "현재 블루투스가 비활성 상태입니다.", Toast.LENGTH_LONG).show();
                        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                        startActivityForResult(enableBtIntent, mBluetooth.REQUEST_ENABLE_BT);
                        break;
                    case 2: mDevices = mBluetoothAdapter.getBondedDevices();
                        mPariedDeviceCount = mDevices.size();
                }
                BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(deviceNum);
                if(((AdminMainActivity)getActivity()).mBluetooth.connectToSelectedDevice(deviceNum, mDevices, device)){
                    beginListenForData_1();
                    ((AdminMainActivity)getActivity()).mBluetooth.sendData("connect");
                }else{
                    Toast.makeText(getContext(), "블루투스 연결 중 오류가 발생했습니다.", Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    //데이터 수신
    void beginListenForData_1() {
        final Handler handler = new Handler();

        readBufferPosition = 0;                 // 버퍼 내 수신 문자 저장 위치.
        readBuffer = new byte[1024];            // 수신 버퍼.

        // 문자열 수신 쓰레드.
        mWorkerThread = new Thread(new Runnable()
        {
            @Override
            public void run() {
                // interrupt() 메소드를 이용 스레드를 종료시키는 예제이다.
                // interrupt() 메소드는 하던 일을 멈추는 메소드이다.
                // isInterrupted() 메소드를 사용하여 멈추었을 경우 반복문을 나가서 스레드가 종료하게 된다.
                while(!Thread.currentThread().isInterrupted()) {
                    try {
                        int byteAvailable;
                        if(!id.equals("admin")) {
                            // InputStream.available() : 다른 스레드에서 blocking 하기 전까지 읽은 수 있는 문자열 개수를 반환함.
                            byteAvailable = ((UserMainActivity) getActivity()).mBluetooth.mInputStream.available();   // 수신 데이터 확인
                        }
                        else{
                            byteAvailable = ((AdminMainActivity) getActivity()).mBluetooth.mInputStream.available();   // 수신 데이터 확인
                        }
                        if(byteAvailable > 0) {                       // 데이터가 수신된 경우.
                            byte[] packetBytes = new byte[byteAvailable];
                            // read(buf[]) : 입력스트림에서 buf[] 크기만큼 읽어서 저장 없을 경우에 -1 리턴.
                            if(!id.equals("admin")) {
                                ((UserMainActivity) getActivity()).mBluetooth.mInputStream.read(packetBytes);
                            }
                            else{
                                ((AdminMainActivity) getActivity()).mBluetooth.mInputStream.read(packetBytes);
                            }
                            for(int i=0; i<byteAvailable; i++) {
                                byte b = packetBytes[i];
                                if(b == mCharDelimiter) {
                                    byte[] encodedBytes = new byte[readBufferPosition];
                                    //  System.arraycopy(복사할 배열, 복사시작점, 복사된 배열, 붙이기 시작점, 복사할 개수)
                                    //  readBuffer 배열을 처음 부터 끝까지 encodedBytes 배열로 복사.
                                    System.arraycopy(readBuffer, 0, encodedBytes, 0, encodedBytes.length);

                                    final String data = new String(encodedBytes, "US-ASCII");
                                    readBufferPosition = 0;

                                    handler.post(new Runnable(){
                                        // 수신된 문자열 데이터에 대한 처리.
                                        @Override
                                        public void run() {
                                            // mStrDelimiter = '\n';
//                                            mEditReceive.setText(mEditReceive.getText().toString() + data+ mStrDelimiter);
                                            String[] datas = data.split(",");//보낸 신호에 응답한 신호에 따라 각각에 맞게 설정
                                            if(datas[0].equals("LEDON")) {
                                                text_led.setText("ON");
                                            }
                                            else if(datas[0].equals("LEDOFF")) {
                                                text_led.setText("OFF");
                                            }
                                            else if(datas[0].equals("PUMPON")){
                                                text_motor.setText("ON");
                                            }
                                            else if(datas[0].equals("PUMPOFF")){
                                                text_motor.setText("OFF");
                                            }
                                            else if(datas[0].equals("TEMPER")){
                                                text_temper.setText(datas[1]);
                                                text_humi_air.setText(datas[2]);
                                                text_humi_soil.setText(datas[3]);
                                            }
                                        }

                                    });
                                }
                                else {
                                    readBuffer[readBufferPosition++] = b;
                                }
                            }
                        }

                    } catch (Exception e) {    // 데이터 수신 중 오류 발생.
                        e.printStackTrace();
                    }
                }
            }
        });
        mWorkerThread.start();//수신 쓰레드 시작.

    }

    @Override
    public void onDestroy() {//앱 종료시,
        try{
            mWorkerThread.interrupt(); // 데이터 수신 쓰레드 종료
            mInputStream.close();
            ((UserMainActivity)getActivity()).mBluetooth.mSocket.close();//소켓 종료
            ((AdminMainActivity)getActivity()).mBluetooth.mSocket.close();
        }catch(Exception e){}
        super.onDestroy();
    }

}
