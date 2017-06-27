package younggng.ttsplayer;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {
    TtsPlay ttsPlay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //audio


        //language set

        /*
         case "kr":
                return new Locale("ko");
            case "en":
                return new Locale("en");
            case "cn":
                return new Locale("zh");
            case "jp":
                return new Locale("ja");
            case "ru":
                return new Locale("ru");
            case "ar":
                return new Locale("ar");
            default:
                return new Locale("ko");
         */


        SharedPreferences sharedPreference = getSharedPreferences("lang", Context.MODE_PRIVATE);
        SharedPreferences.Editor sheditor = sharedPreference.edit();
        sheditor.putString("lang", "en");
        sheditor.apply();

        String ttscontents = "Hello World!";
        TextView currenttimetext, audiotimetext;
        ImageButton btn_play, btn_refresh;
        SeekBar seekBar;
        currenttimetext = (TextView) findViewById(R.id.currenttimetext);
        seekBar = (SeekBar) findViewById(R.id.ttsseekbar);
        audiotimetext = (TextView) findViewById(R.id.audiotimetext);
        btn_play = (ImageButton) findViewById(R.id.btn_play);
        btn_refresh = (ImageButton) findViewById(R.id.btn_refresh);


        ttsPlay = new TtsPlay(MainActivity.this, ttscontents, currenttimetext, seekBar, audiotimetext, btn_play, btn_refresh);
        ttsPlay.play();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (ttsPlay != null) {
            ttsPlay.destory();
        }

    }
}
