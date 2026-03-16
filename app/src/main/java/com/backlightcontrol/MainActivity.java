package com.backlightcontrol;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;

public class MainActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "BacklightControlPrefs";
    private static final String KEY_BACKLIGHT_PATH = "backlight_path";
    private static final String KEY_QUICK_OFF = "quick_off";

    private Button btnTurnOff;
    private ImageButton btnSettings;
    private String backlightPath;
    private boolean quickOff;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnTurnOff = findViewById(R.id.btn_turn_off);
        btnSettings = findViewById(R.id.btn_settings);

        // 加载设置
        loadSettings();

        // 检查是否需要快速关屏
        if (quickOff) {
            turnOffScreen();
            return;
        }

        // 检查ROOT权限
        if (!checkRootAccess()) {
            Toast.makeText(this, "需要ROOT权限", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // 如果没有背光路径，搜索
        if (backlightPath == null || backlightPath.isEmpty()) {
            searchBacklightPath();
        }

        // 关屏按钮点击事件
        btnTurnOff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                turnOffScreen();
            }
        });

        // 设置按钮点击事件
        btnSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
                startActivity(intent);
            }
        });
    }

    private boolean checkRootAccess() {
        try {
            Process process = Runtime.getRuntime().exec("su -c 'echo test'");
            int exitCode = process.waitFor();
            return exitCode == 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private void searchBacklightPath() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Process process = Runtime.getRuntime().exec("su -c 'find /sys -name \"*backlight\" -type d'");
                    BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                    String line;
                    while ((line = reader.readLine()) != null) {
                        String brightnessPath = line + "/brightness";
                        File brightnessFile = new File(brightnessPath);
                        if (brightnessFile.exists() && brightnessFile.canWrite()) {
                            backlightPath = brightnessPath;
                            saveBacklightPath(backlightPath);
                            break;
                        }
                    }
                    reader.close();

                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            if (backlightPath == null || backlightPath.isEmpty()) {
                                Toast.makeText(MainActivity.this, "未找到背光文件，请在设置中手动添加", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void turnOffScreen() {
        if (backlightPath == null || backlightPath.isEmpty()) {
            Toast.makeText(this, "未找到背光文件，请在设置中手动添加", Toast.LENGTH_SHORT).show();
            return;
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    // 执行关闭背光命令
                    Process process = Runtime.getRuntime().exec("su -c 'echo 0 > " + backlightPath + "'");
                    int exitCode = process.waitFor();

                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            if (exitCode == 0) {
                                Toast.makeText(MainActivity.this, "屏幕已关闭", Toast.LENGTH_SHORT).show();
                                finish();
                            } else {
                                Toast.makeText(MainActivity.this, "关闭背光失败", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(MainActivity.this, "关闭背光失败", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        }).start();
    }

    private void loadSettings() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        backlightPath = prefs.getString(KEY_BACKLIGHT_PATH, "");
        quickOff = prefs.getBoolean(KEY_QUICK_OFF, false);
    }

    private void saveBacklightPath(String path) {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(KEY_BACKLIGHT_PATH, path);
        editor.apply();
    }
}
