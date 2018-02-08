package cn.diyai.robot.clickhelper;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends Activity {

    Button startBtn,testClickBtn;
    EditText sleepTimeET,xLocationET,yLocationET;

    Timer clickTimer = null;
    String spSleepTimeKey = "sleepTime";
    String spXLocationKey = "xLocation";
    String spYLocationKey = "yLocation";
    String spSettingKey = "spSetting";

    String spSleepTime = null;
    String spXLocation = null;
    String spYLocation = null;
    String spSetting = null;

    SharedPreferences preferences = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        preferences = this.getPreferences(MODE_PRIVATE);

        spSleepTime = preferences.getString(spSleepTimeKey,"");
        spXLocation = preferences.getString(spXLocationKey,"");
        spYLocation = preferences.getString(spYLocationKey,"");
        spSetting = preferences.getString(spSettingKey,"");

        System.out.println(String.format("%s:%s:%s",spSleepTime,spXLocation,spYLocation));

        sleepTimeET.setText(spSleepTime);
        xLocationET.setText(spXLocation);
        yLocationET.setText(spYLocation);
        if(spSetting.equals("")){
            spSetting = "开始";
        }
        startBtn.setText(spSetting);


        startBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String status = startBtn.getText().toString();
                System.out.println("strartBtn:"+status);
                if(status.equals("开始")){
                    String sleepTime = sleepTimeET.getText().toString();
                    if (sleepTime.equals("") || sleepTime == null){
                        Toast.makeText(MainActivity.this,R.string.sleepTime,Toast.LENGTH_LONG).show();
                        return;
                    }


                    String xLocation = xLocationET.getText().toString();
                    if (xLocation.equals("") || xLocation == null){
                        Toast.makeText(MainActivity.this,"请输入"+R.string.xLocation,Toast.LENGTH_LONG).show();
                        return;
                    }

                    String yLocation = yLocationET.getText().toString();
                    if (yLocation.equals("") || yLocation == null){
                        Toast.makeText(MainActivity.this,"请输入"+R.string.yLocation,Toast.LENGTH_LONG).show();
                        return;
                    }

                    initTimer(Integer.parseInt(sleepTime));
                    Toast.makeText(MainActivity.this,"每隔"+sleepTime+"秒点击一次",Toast.LENGTH_LONG).show();

//                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
//                    imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);

                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putString(spSleepTimeKey, sleepTime);
                    editor.putString(spXLocationKey, xLocation);
                    editor.putString(spYLocationKey, yLocation);
                    editor.putString(spSettingKey, "停止");
                    editor.commit();
                    startBtn.setText("停止");

                    sleepTimeET.setClickable(false);
                    sleepTimeET.setEnabled(false);

                    xLocationET.setClickable(false);
                    xLocationET.setEnabled(false);

                    yLocationET.setClickable(false);
                    yLocationET.setEnabled(false);

                }else{
                    sleepTimeET.setClickable(true);
                    sleepTimeET.setEnabled(true);

                    xLocationET.setClickable(true);
                    xLocationET.setEnabled(true);

                    yLocationET.setClickable(true);
                    yLocationET.setEnabled(true);

                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putString(spSettingKey, "开始");
                    editor.commit();

                    startBtn.setText("开始");

                    // 停止点击
                    if (clickTimer != null){
                        clickTimer.cancel();
                    }
                }
            }
        });

        testClickBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(MainActivity.this,"有效点击",Toast.LENGTH_LONG).show();
            }
        });

    }

    private void initView(){
        testClickBtn = (Button)findViewById(R.id.testClick);
        startBtn = (Button)findViewById(R.id.start);
        sleepTimeET = (EditText)findViewById(R.id.sleepTime);
        xLocationET = (EditText)findViewById(R.id.xLocation);
        yLocationET = (EditText)findViewById(R.id.yLocation);

    }


    private void initTimer(int seconds) {

        if (clickTimer != null){
            clickTimer.cancel();
        }

        clickTimer = new Timer();
        clickTimer.schedule(new ClickTimerTask(), seconds * 1000, seconds * 1000);
    }

    class ClickTimerTask extends TimerTask {
        @Override
        public void run() {
            System.out.println("点击坐标");
            spXLocation = preferences.getString(spXLocationKey,"");
            spYLocation = preferences.getString(spYLocationKey,"");
            clickPix(spXLocation,spYLocation);
        }
    }

    private void clickPix(String x, String y){
        execCmd(String.format("input tap %s %s",x,y));
    }

    private static String execCmd(String cmd) {

        StringBuffer sb = new StringBuffer();
        try {
            Process suProcess = Runtime.getRuntime().exec(cmd);
            DataOutputStream os = new DataOutputStream(
                    suProcess.getOutputStream());
            os.writeBytes(cmd + "\n");
            os.flush();

            InputStream input = suProcess.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    input));
            String line = "";
            while ((line = reader.readLine()) != null) {
                sb = sb.append(line);
            }
            return sb.toString();
        } catch (Throwable e) {
            e.printStackTrace();
            System.out.println("execCommand:" + e.getMessage());
        }
        return null;
    }
}
