package com.codemountain.slicker.activities;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.codemountain.slicker.R;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private Button mainBtnReg, mainBtnLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mainBtnLogin = findViewById(R.id.mainBtnLogin);
        mainBtnReg = findViewById(R.id.mainBtnReg);

        mainBtnReg.setOnClickListener(this);
        mainBtnLogin.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.mainBtnReg:

                Intent registerIntent = new Intent(MainActivity.this, RegisterActivity.class);
                startActivity(registerIntent);

                break;

            case R.id.mainBtnLogin:

                Intent loginIntent = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(loginIntent);

                break;
        }
    }
}
