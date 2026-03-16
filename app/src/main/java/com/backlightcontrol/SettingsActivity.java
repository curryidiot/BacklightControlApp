package com.backlightcontrol;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class SettingsActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "BacklightControlPrefs";
    private static final String KEY_BACKLIGHT_PATH = "backlight_path";
    private static final String KEY_QUICK_OFF = "quick_off";

    private EditText etBacklightPath;
    private CheckBox cbQuickOff;
    private Button btnCancel;
    private Button btnConfirm;

    private String currentPath;
    private boolean currentQuickOff;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        etBacklightPath = findViewById(R.id.et_backlight_path);
        cbQuickOff = findViewById(R.id.cb_quick_off);
        btnCancel = findViewById(R.id.btn_cancel);
        btnConfirm = findViewById(R.id.btn_confirm);

        // 加载当前设置
        loadSettings();

        // 取消按钮点击事件
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        // 确定按钮点击事件
        btnConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveSettings();
                finish();
            }
        });
    }

    private void loadSettings() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        currentPath = prefs.getString(KEY_BACKLIGHT_PATH, "");
        currentQuickOff = prefs.getBoolean(KEY_QUICK_OFF, false);

        etBacklightPath.setText(currentPath);
        cbQuickOff.setChecked(currentQuickOff);
    }

    private void saveSettings() {
        String newPath = etBacklightPath.getText().toString().trim();
        boolean newQuickOff = cbQuickOff.isChecked();

        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(KEY_BACKLIGHT_PATH, newPath);
        editor.putBoolean(KEY_QUICK_OFF, newQuickOff);
        editor.apply();

        Toast.makeText(this, "设置已保存", Toast.LENGTH_SHORT).show();
    }
}
