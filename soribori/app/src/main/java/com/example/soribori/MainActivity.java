package com.example.soribori;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.kakao.sdk.newtoneapi.SpeechRecognizeListener;
import com.kakao.sdk.newtoneapi.SpeechRecognizerClient;
import com.kakao.sdk.newtoneapi.SpeechRecognizerManager;
import com.kakao.sdk.newtoneapi.impl.util.PermissionUtils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;


/**
 *
 * @author Daum Communications Corp.
 * @since 2013
 *
 */


public class MainActivity extends AppCompatActivity implements View.OnClickListener, SpeechRecognizeListener {

    private static final int REQUEST_CODE_AUDIO_AND_WRITE_EXTERNAL_STORAGE = 1; //what number..? maybe 1
    private SpeechRecognizerClient client;

    String User_Name;
    boolean AutoRunning = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //앱 해시키가 있어야 API 콜이 정상작동하는데 과연 다른 컴퓨터에서도 될지?
        try {
            PackageInfo info = getPackageManager().getPackageInfo(getPackageName(), PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Log.e("KeyHash:", Base64.encodeToString(md.digest(), Base64.DEFAULT));
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }


        //activity_main을 screen으로 설정
        setContentView(R.layout.activity_main);



        //유저에게 권한 요청
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.RECORD_AUDIO) && ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                ActivityCompat.requestPermissions(this, new String[] { Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CODE_AUDIO_AND_WRITE_EXTERNAL_STORAGE);
            } else {
                // 유저가 거부하면서 다시 묻지 않기를 클릭, 권한이 없다고 유저에게 직접 알림.
            }
        } else {
            //startUsingSpeechSDK();
        }

        // SDK library 초기화
        // API를 사용할 시점이 되면, initializeLibrary(Context)를 호출
        // 사용을 마치면 꼭!!!!!!!!!!!!! finalizeLibrary()를 호출해야 함
        SpeechRecognizerManager.getInstance().initializeLibrary(this);


        // 버튼 클릭 리스너 등록
        findViewById(R.id.Voice_recognition_start).setOnClickListener(this);
        findViewById(R.id.Voice_recognition_stop).setOnClickListener(this);
        findViewById(R.id.Voice_recognition_auto).setOnClickListener(this);
        findViewById(R.id.Voice_recognition_auto_stop).setOnClickListener(this);
        findViewById(R.id.check_sound_classification_items).setOnClickListener(this);
        findViewById(R.id.user_s_custom_sound_resgistration).setOnClickListener(this);
        findViewById(R.id.name_update).setOnClickListener(this);
        setButtonsStatus(true);

        // 클라이언트 생성
        // ex code ===>> SpeechRecognizerClient.Builder builder = new SpeechRecognizerClient.Builder().setServiceType(SpeechRecognizerClient.SERVICE_TYPE_WEB).setUserDictionary(userdict);  // optional

        //등록한 사용자 이름이 날아가면 안되니까
        //저장된 값을 불러오기 위해 같은 네임파일을 찾음.
        SharedPreferences sf = getSharedPreferences("sFile",MODE_PRIVATE);
        //text라는 key에 저장된 값이 있는지 확인. 아무값도 들어있지 않으면 ""를 반환
        String text = sf.getString("UserName","");
        User_Name = sf.getString("UserName","");
        TextView usernametext = (TextView)findViewById(R.id.User_name_textview);
        usernametext.setText(text);

    }

    @Override
    protected void onResume() {
        super.onResume();
    }
    @Override
    public void onDestroy() {
        super.onDestroy();

        //API를 더이상 사용하지 않을 때 finalizeLibrary()를 호출함.
        SpeechRecognizerManager.getInstance().finalizeLibrary();
    }

    private void setButtonsStatus(boolean enabled) {
        findViewById(R.id.Voice_recognition_start).setEnabled(enabled);
        findViewById(R.id.Voice_recognition_stop).setEnabled(!enabled);
    }

    //이건 사용 안할지도?
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_AUDIO_AND_WRITE_EXTERNAL_STORAGE:
                if (grantResults.length > 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                    //startUsingSpeechSDK();
                } else {
                    finish();
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();

        final String serviceType = SpeechRecognizerClient.SERVICE_TYPE_DICTATION;

        // Voice_recognition_start 버튼을 클릭했을때
        if (id == R.id.Voice_recognition_start) {
            if(PermissionUtils.checkAudioRecordPermission(this)) {

                SpeechRecognizerClient.Builder builder = new SpeechRecognizerClient.Builder().
                        setServiceType(serviceType);

                /**
                고립어용
                 if (serviceType.equals(SpeechRecognizerClient.SERVICE_TYPE_DICTATION)) {


                    Log.i("SpeechSampleActivity", "word list : " + wordList.replace('\n', ','));
                }
                */

                client = builder.build();

                client.setSpeechRecognizeListener(this);
                client.startRecording(true);

                setButtonsStatus(false);
            }
        }

        // 음성 인식 자동시작
        // 자동 시작하면 2초정도로 끊어서 계속 인식하게 된다
        else if (id == R.id.Voice_recognition_auto) {
            AutoRunning = true;
            if(PermissionUtils.checkAudioRecordPermission(this)) {

                //쓰레드 시작
                try {
                    ExampleThread thread = new ExampleThread();
                    thread.setDaemon(true);
                    thread.start();
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }

        // 자동시작 stop 하는 버튼
        else if (id == R.id.Voice_recognition_auto_stop) {
            if (client != null) {
                client.stopRecording();
            }
            AutoRunning = false;
        }

        // 음성인식 중지버튼 listener
        else if (id == R.id.Voice_recognition_stop) {
            if (client != null) {
                client.stopRecording();
            }
        }

        // 유저의 커스텀 소리 등록 액티비티로
        else if (id == R.id.user_s_custom_sound_resgistration){
            // 화면 넘겨주기
            Intent i1 = new Intent(getApplicationContext(), UserCustomSound.class);
            startActivity(i1);
        }

        else if (id == R.id.check_sound_classification_items){
            // 화면 넘겨주기
            Intent i2 = new Intent(getApplicationContext(), SoundClassificationItems.class);
            startActivity(i2);
        }

        //이름 업데이트 버튼 클릭했을때
        else if (id == R.id.name_update){
             // 이름 등록 페이지로부터 이름 받아오기
             Intent intent = new Intent(MainActivity.this , Name_registration.class);
             startActivityForResult(intent, 1234);
        }

    }

    private class ExampleThread extends Thread {
        private static final String TAG = "ExampleThread";
            public void run() {
                Log.i("ExampleThread", "THREAD RUN");

                final String serviceType = SpeechRecognizerClient.SERVICE_TYPE_DICTATION;

                    Looper.prepare();
                    try {
                        SpeechRecognizerClient.Builder builder = new SpeechRecognizerClient.Builder().setServiceType(serviceType);
                        client = builder.build();
                        client.setSpeechRecognizeListener(MainActivity.this);
                        client.startRecording(true);
                        Thread.sleep(3000);
                        client.stopRecording();
                        Thread.sleep(200);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    Looper.loop();
            }
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        TextView tx1 = (TextView)findViewById(R.id.User_name_textview);
        TextView thread01_tx2 = (TextView)findViewById(R.id.Thread01_Textview);


        if(requestCode == 1234 && resultCode == RESULT_OK){
            String user_name = data.getStringExtra("UserName");
            tx1.setText(user_name);
        }
        else if (resultCode == RESULT_OK) {
            ArrayList<String> results = data.getStringArrayListExtra(VoiceRecognizeActivity.EXTRA_KEY_RESULT_ARRAY);

            final StringBuilder builder2 = new StringBuilder();

            for (String result : results) {
                builder2.append(result);
                builder2.append("\n");
            }
            thread01_tx2.setText(builder2.toString());
        } else if (requestCode == RESULT_CANCELED) {
            // 음성인식의 오류 등이 아니라 activity의 취소가 발생했을 때.
            if (data == null) {
                return;
            }

            int errorCode = data.getIntExtra(VoiceRecognizeActivity.EXTRA_KEY_ERROR_CODE, -1);
            String errorMsg = data.getStringExtra(VoiceRecognizeActivity.EXTRA_KEY_ERROR_MESSAGE);

            if (errorCode != -1 && !TextUtils.isEmpty(errorMsg)) {
                new AlertDialog.Builder(this).
                        setMessage(errorMsg).
                        setPositiveButton("확인", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        }).show();
            }
        }
    }


    @Override
    public void onReady() {
        //TODO implement interface
    }

    @Override
    public void onBeginningOfSpeech() {
        //TODO implement interface
    }

    @Override
    public void onEndOfSpeech() {
        //TODO implement interface
    }

    @Override
    public void onError(int errorCode, String errorMsg) {
        //TODO implement interface

        Log.i("onError","on error thread start");
        if(AutoRunning == true) {
            try {
                ExampleThread thread = new ExampleThread();
                thread.setDaemon(true);
                thread.start();
            } catch (Exception e) {
                e.printStackTrace();
            }
            Log.i("onError", "on error end");
        }
            client = null;

    }

    @Override
    public void onPartialResult(String partialResult) {
        //TODO implement interface
    }

    @Override
    public void onResults(Bundle results) {
        //TODO implement interface
        //써야할듯
        final StringBuilder builder = new StringBuilder();
        Log.i("SpeechSampleActivity", "onResults");
        final TextView thread01_tx1 = (TextView)findViewById(R.id.Thread01_Textview);
        ArrayList<String> texts = results.getStringArrayList(SpeechRecognizerClient.KEY_RECOGNITION_RESULTS);
        ArrayList<Integer> confs = results.getIntegerArrayList(SpeechRecognizerClient.KEY_CONFIDENCE_VALUES);

        for (int i = 0; i < texts.size(); i++) {
            builder.append(texts.get(i));
            builder.append(" (");
            builder.append(confs.get(i).intValue());
            builder.append(")\n");
        }

        final Activity activity = this;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // finishing일때는 처리하지 않는다.
                if (activity.isFinishing()) return;


                thread01_tx1.setText(builder.toString());

                //이름이 포함되어있으면 알림
                if(builder.toString().contains(User_Name) == true) {
                    Toast.makeText(getApplicationContext(), "누군가가 당신의 이름을 부르고 있습니다!", Toast.LENGTH_LONG).show();
                }


                //이부분은 테스트용 안녕 들리면 알림줌.
                if(builder.toString().contains("안녕") == true){
                    Toast.makeText(getApplicationContext(),"누군가가 당신에게 인사하고 있어요!!", Toast.LENGTH_LONG).show();
                }
                setButtonsStatus(true);
            }
        });

        client = null;

        Log.i("SpeechSampleActivity", "onResults End");
        if(AutoRunning==true) {
            try {
                ExampleThread thread = new ExampleThread();
                thread.setDaemon(true);
                thread.start();
            } catch (Exception e) {
                e.printStackTrace();
            }
            Log.i("ExmapleThread", "onresult loop end");
        }
    }

    @Override
    public void onAudioLevel(float audioLevel) {
        //TODO implement interface
    }

    @Override
    public void onFinished() {
        if(AutoRunning == true){
            try {
                ExampleThread thread = new ExampleThread();
                thread.setDaemon(true);
                thread.start();
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        Log.i("SpeechSampleActivity","onFinished");
        //TODO implement interface
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {
        //TODO implement interface
    }
}
