package br.com.ifpa;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {

    private ViewDoJogo viewDoJogo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        viewDoJogo = findViewById(R.id.viewDoJogo);
    }

    @Override
    protected void onPause() {
        super.onPause();
        viewDoJogo.stopGame();
    }
}
