package angtrim.com.myapplication;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import angtrim.com.fivestarslibrary.FiveStarsDialog;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        new FiveStarsDialog(this,"angelo.gallarello@gmail.com").setRateText("").showAfter(0);
    }



}
