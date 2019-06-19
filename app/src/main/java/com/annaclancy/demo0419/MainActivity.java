package com.annaclancy.demo0419;

import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import com.annaclancy.demo0419.WavFile.DreamCatcher;
// https://github.com/tensorflow/tensorflow/blob/master/tensorflow/java/README.md
import org.tensorflow.contrib.android.TensorFlowInferenceInterface;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();

    // 在转化该 pb 文件时，读取 python 源程序的 base_model
    // 即 outputs 为 softmax 层的，不包含 ctc 层的模型数据
    private static String MODEL_PATH = "file:///android_asset/model.pb";

    public static List<String> sym_list;
    public static TensorFlowInferenceInterface tf;
    private DreamCatcher WhiteCat;
    private SimplePlayer audioplayer;

    private TextView org;   // 中字
    private TextView str;   // 识别结果

    // 错误率
    private TextView error_ratio;
    List<String> temp;
    public float eratio;

    private Spinner spinner;
    public static final String[] Choice = {
            "测试 train 文件",
            "测试 test 文件",
            "测试语句",
            "实时录音"
    };

    private static void addSymList(String e){
        sym_list.add(e);
    }

    // 初始化 dict.txt 列表
    private void initialSymList(){
        InputStream inputStream = getResources().openRawResource(R.raw.dict);
        InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
        BufferedReader br = new BufferedReader(inputStreamReader);
        String line = new String();
        sym_list = new ArrayList<>();
        try {
            while((line = br.readLine()) != null){
                List<Character> tp = new ArrayList<>();
                // 减1回避读取最末的、不必要的空白块
                for(int i=0;i<line.length()-1;i++)
                    if(line.charAt(i) != '\t')
                        tp.add(line.charAt(i));
                    else break;
                char[] str = new char[tp.size()];
                for(int i=0;i<tp.size();i++)
                    str[i] = tp.get(i);
                addSymList(String.valueOf(str));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void initialView(){
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        org = findViewById(R.id.correct);
        str = findViewById(R.id.pinyin);
        error_ratio = findViewById(R.id.err_ratio);

        spinner = findViewById(R.id.litSpinner);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, Choice);
        spinner.setAdapter(adapter);

//        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
//            }
//        });

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.initialView();
        this.initialSymList();

        tf = new TensorFlowInferenceInterface(getAssets(), MODEL_PATH);
        audioplayer = new SimplePlayer();

        Toast.makeText(this, "app ready!", Toast.LENGTH_SHORT).show();
        // new Chain();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        tf.close();
        audioplayer.stop();
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

        return super.onOptionsItemSelected(item);
    }

    private boolean CatcherStatus = false;
    public void onClick(View view){
        int id = view.getId();
        switch (id) {
            case R.id.ButtonSta:
                switch (spinner.getSelectedItemPosition()) {
                    case 0:
                        clearText();
                        org.setText(R.string.train);
                        new Chain(AnnaUtil.TRAIN_SET_FILE);
                        str.setText(Chain.reMain);
                        Toast.makeText(this, "TrainEx Test End.", Toast.LENGTH_SHORT).show();
                        break;
                    case 1:
                        clearText();
                        org.setText(R.string.test);
                        new Chain(AnnaUtil.TEST_SET_FILE);
                        str.setText(Chain.reMain);
                        Toast.makeText(this, "TestEx Test End.", Toast.LENGTH_SHORT).show();
                        break;
                    case 2:
                        clearText();
                        org.setText(R.string.farewell);
                        CatcherStatus = true;
                        WhiteCat = new DreamCatcher();
                        WhiteCat.startWork();
                        Toast.makeText(this, "Start Catcher!", Toast.LENGTH_SHORT).show();
                        break;
                    case 3:
                        clearText();
                        CatcherStatus = true;
                        WhiteCat = new DreamCatcher();
                        WhiteCat.startWork();
                        Toast.makeText(this, "Start Catcher!", Toast.LENGTH_SHORT).show();
                        break;
                }
                break;
            case R.id.ButtonSto:
                switch (spinner.getSelectedItemPosition()) {
                    case 0: break;
                    case 1: break;
                    case 2: // 提供测试语句与错误率检测
                        if(CatcherStatus){
                            WhiteCat.takeRelax();
                            CatcherStatus = false;
                        }
                        else clearText();
                        Toast.makeText(this, "Stop Catcher!", Toast.LENGTH_SHORT).show();
                        Chain chain = new Chain(AnnaUtil.DEFAULT_FILE);
                        temp = chain.getReplay();
                        str.setText(Chain.reMain);
                        if(!ErrorRatio())
                            error_ratio.setText(R.string.len_error);
                        else{
                            String tp = String.format(getString(R.string.pri_ratio), String.valueOf(eratio));
                            error_ratio.setText(tp);
                        }
                        break;
                    case 3:
                        if(CatcherStatus){
                            WhiteCat.takeRelax();
                            CatcherStatus = false;
                        }
                        else clearText();
                        Toast.makeText(this, "Stop Catcher!", Toast.LENGTH_SHORT).show();
                        new Chain(AnnaUtil.DEFAULT_FILE);
                        str.setText(Chain.reMain);
                        break;
                }
                break;
            case R.id.Play:
                switch (spinner.getSelectedItemPosition()){
                    case 0:
                        audioplayer.start(AnnaUtil.TRAIN_SET_FILE);
                        break;
                    case 1:
                        audioplayer.start(AnnaUtil.TEST_SET_FILE);
                        break;
                    case 2:
                        audioplayer.start(AnnaUtil.DEFAULT_FILE);
                        break;
                    case 3:
                        audioplayer.start(AnnaUtil.DEFAULT_FILE);
                        break;
                }
                break;
        }
    }

    private void clearText(){
        if(org != null)
            org.setText(null);
        if(str != null)
            str.setText(null);
        if(error_ratio != null)
            error_ratio.setText(null);
    }

    // 6.2 添加的简易错误率检测
    public boolean ErrorRatio(){
        if(temp.size() != AnnaUtil.fatewell_pinyin.length)
            return false;
        int error_count = 0;
        for(int i=0;i<temp.size();i++)
            if(!temp.get(i).equals(AnnaUtil.fatewell_pinyin[i]))
                error_count++;
        eratio = (float)error_count/AnnaUtil.fatewell_pinyin.length;
        // Log.e("ErrorRatio", String.valueOf(eratio));
        return true;
    }

}
