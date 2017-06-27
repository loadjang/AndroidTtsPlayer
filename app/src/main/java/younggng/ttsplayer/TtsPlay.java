package younggng.ttsplayer;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.view.View;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.HashMap;
import java.util.Locale;

/**
 * Created by an-yeong-gug on 2017. 5. 22..
 */

public class TtsPlay {

    private final static String LOADINGMSG="loading...";


    private Handler handler = new Handler();
    private Runnable timerunnable = new Runnable() {
        @Override
        public void run() {
            int millis2 = mediaPlayer.getCurrentPosition();
            int seconds2 = (int) (millis2 / 1000) % 60;            //초
            int minutes2 = (int) ((millis2 / (1000 * 60)) % 60);  //분
            String currenttext = String.format("%02d : %02d", minutes2, seconds2);
            currenttimetext.setText(currenttext);

            handler.postDelayed(timerunnable, 500);

        }
    };


    Context gcontext;

    public void destory() {

        if (handler != null) {
            handler.removeCallbacks(timerunnable);
        }
        if (myTTS != null) {
            if (myTTS.isSpeaking()) {
                myTTS.stop();
                myTTS.shutdown();
            }
        }

        isPlaying = false; // 쓰레드 정지
        if (mediaPlayer != null) {
            mediaPlayer.release(); // 자원해제
        }
        if (ttsfilepath != null) {
            File file = new File(ttsfilepath);

            if (file.exists()) {
                file.delete();

            }
        }

    }

    int pos; // 재생 멈춘 시점

    class MyThread extends Thread {
        @Override
        public void run() { // 쓰레드가 시작되면 콜백되는 메서드
            // 씨크바 막대기 조금씩 움직이기 (노래 끝날 때까지 반복)
            while (isPlaying) {
                seekBar.setProgress(mediaPlayer.getCurrentPosition());


            }
        }
    }

    private void setAudioTime() {

        if (mediaPlayer == null) {
            return;
        }


        int millis = mediaPlayer.getDuration();


        int seconds = (int) (millis / 1000) % 60;            //초
        int minutes = (int) ((millis / (1000 * 60)) % 60);  //분

        String totaltext = String.format("%02d : %02d", minutes, seconds);

        audiotimetext.setText(totaltext);
        //audiotimetext.setText(totaltext+"/"+filesize);

    }

    public static Locale getAppLocale(Context context) {
        String language = context.getSharedPreferences("lang", Context.MODE_PRIVATE).getString("lang", "");
        switch (language.toLowerCase()) {
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
        }

    }

    public static Locale getLocaleFromPref(Context context) {


        Locale locale = getAppLocale(context);


        return locale;
    }
    public void play() {
        setAudioTime();
        btn_play.setImageResource(R.drawable.ico_audio_pause);

        handler.post(timerunnable);
        if (mediaPlayer.getCurrentPosition() > 0) {
            mediaPlayer.seekTo(pos); // 일시정지 시점으로 이동

            mediaPlayer.start(); // 시작
            isPlaying = true; // 재생하도록 flag 변경
            new MyThread().start(); // 쓰레드 시작
        } else {
            mediaPlayer.setLooping(false); // true:무한반복
            mediaPlayer.start(); // 노래 재생 시작

            int a = mediaPlayer.getDuration(); // 노래의 재생시간(miliSecond)
            seekBar.setMax(a);// 씨크바의 최대 범위를 노래의 재생시간으로 설정
            new MyThread().start(); // 씨크바 그려줄 쓰레드 시작
            isPlaying = true; // 씨크바 쓰레드 반복 하도록

        }
    }

    public void playStop() {
        if (mediaPlayer == null) {
            return;
        }
        if (mediaPlayer.isPlaying()) {
            pos = mediaPlayer.getCurrentPosition();
            mediaPlayer.pause(); // 일시중지

            btn_play.setImageResource(R.drawable.ico_audio_play);
            isPlaying = false; // 쓰레드 정지
        }
    }


    private void setAutoStart(boolean autoStart) {
        isAutoStart = autoStart;
    }

    private SeekBar seekBar;
    public TextView audiotimetext, currenttimetext;
    TextToSpeech myTTS;
    private boolean ttsinitflag = false, ttsfileflag = false, isPlaying = false, isAutoStart = true;
    String ttsfilepath = "";
    String ttscontents = "";
    MediaPlayer mediaPlayer;
    private String  filesize="";
    private ImageButton btn_play, btn_refresh;

    public void initTts(final Context context) {
        myTTS = new TextToSpeech(context, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {

                // TextToSpeech 엔진의 초기화가 완료되어 사용할 수 있도록 준비된 상태인 경우
                if (status == TextToSpeech.SUCCESS) {
                    // 음성 합성하여 출력하기위한 언어를 Locale.US 로 설정한다.
                    // 안드로이드 시스템의 환경 설정에서도 동일한 언어가 선택되어 있어야만
                    // 해당 언어의 문장이 음성변환 될 수 있다.
                    try {

                        Locale locale = getLocaleFromPref(context);

                        int result = myTTS.setLanguage(locale);

                        // 해당 언어에 대한 데이터가 없거나 지원하지 않는 경우
                        if (result == TextToSpeech.LANG_MISSING_DATA ||
                                result == TextToSpeech.LANG_NOT_SUPPORTED) {
                            if (result == TextToSpeech.LANG_MISSING_DATA) {
                                Toast.makeText(context, LOADINGMSG, Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent();
                                intent.setAction("com.android.settings.TTS_SETTINGS");
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                context.startActivity(intent);
                            }
                            if (result == TextToSpeech.LANG_NOT_SUPPORTED) {
                                Toast.makeText(context, LOADINGMSG, Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent();
                                intent.setAction("com.android.settings.TTS_SETTINGS");
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                context.startActivity(intent);
                            }
                            audiotimetext.setText(LOADINGMSG);

                            //   loadingBar.cancel();
                            ttsinitflag = false;
                            // 해당 언어는 사용할 수 없음을 알린다.
                            //  Toast.makeText(context, "지원 되지 않는 언어입니다.", Toast.LENGTH_SHORT).show();

                            // TTS 엔진이 해당 언어를 지원하며 데이터도 가지고 있는 경우
                        } else {
                            // TTS 엔진이 성공적으로 초기화된 경우
                            // EditText 에 쓰여지는 문장을 음성 변환할 수 있도록 버튼을 활성화한다.
                            ttsinitflag = true;
                            myTTS.setSpeechRate(1);
                            ;
                            ttsfilepath = context.getCacheDir().getAbsolutePath() + "/tourAr" + String.valueOf(System.currentTimeMillis()) + ".wav";


                            Thread thread = new Thread(new Runnable() {
                                @Override
                                public void run() {

                                    File file = new File(ttsfilepath);
                                    if (file.exists()) {
                                        file.delete();
                                    }

                                    HashMap<String, String> hashMap = new HashMap<>();
                                    hashMap.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, ttscontents);
                                    Bundle bundle = new Bundle();
                                    bundle.putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, ttscontents);
                                    int result2 = 0;
                                    if (Build.VERSION.SDK_INT >= 21) {
                                        result2 = myTTS.synthesizeToFile(ttscontents, bundle, new File(ttsfilepath), TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID);
                                    } else {
                                        result2 = myTTS.synthesizeToFile(ttscontents, hashMap, ttsfilepath);
                                    }

                                    myTTS.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                                        @Override
                                        public void onStart(final String utteranceId) {

                                            Toast.makeText(context, "Tts sound file start", Toast.LENGTH_SHORT).show();
                                        }

                                        @Override
                                        public void onDone(final String utteranceId) {
                                            //  loadingBar.cancel();

                                            ttsfileflag = true;


                                            File file=new File(ttsfilepath);


                                            ((Activity) context).runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    mediaPlayer = MediaPlayer.create(context, Uri.parse(ttsfilepath));
                                                    setAudioTime();
                                                    if (isAutoStart) {
                                                        btn_play.performClick();
                                                    }
                                                }
                                            });

                                        }

                                        @Override
                                        public void onError(String utteranceId) {
                                            //  loadingBar.cancel();
                                            Toast.makeText(context, "Tts sound file creation failed", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                }
                            });

                            if (!"".equalsIgnoreCase(ttscontents)) {

                                thread.start();
                            }


                        }
                    } catch (IllegalArgumentException e) {
                        ttsinitflag = false;
                        audiotimetext.setText(LOADINGMSG);
                        //Toast.makeText(context, "TTS 초기화 실패 ", Toast.LENGTH_SHORT).show();
                    }

                    // TextToSpeech 엔진 초기화에 실패하여 엔진이 TextToSpeech.ERROR 상태인 경우
                } else {
                    // TextToSpeech 엔진이 초기화되지 못했음을 알린다.
                    ttsinitflag = false;

                    audiotimetext.setText(LOADINGMSG);

                }


            }
        });
    }

    private void initTtsPlay(final Context context, String ttsDesc, TextView _currenttimetext, SeekBar _seekBar, TextView _txt_TotalTime, ImageButton _btn_play, ImageButton _btn_refrech) {
        gcontext = context;
        btn_play = _btn_play;
        btn_refresh = _btn_refrech;
        seekBar = _seekBar;
        audiotimetext = _txt_TotalTime;
        currenttimetext = _currenttimetext;
        ttscontents = ttsDesc;
        audiotimetext.setText("loading..");
        initTts(context);


        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            public void onStopTrackingTouch(SeekBar seekBar) {

                if (!ttsinitflag) {
                    return;
                }
                if (!ttsfileflag) {

                    return;

                }


                if (mediaPlayer == null) {
                    return;
                }
                isPlaying = true;
                int ttt = seekBar.getProgress(); // 사용자가 움직여놓은 위치
                mediaPlayer.seekTo(ttt);
                mediaPlayer.start();
                btn_play.setImageResource(R.drawable.ico_audio_pause);

                new MyThread().start();
            }

            public void onStartTrackingTouch(SeekBar seekBar) {

                if (!ttsinitflag) {
                    return;
                }
                if (!ttsfileflag) {

                    return;

                }


                if (mediaPlayer == null) {
                    return;
                }
                isPlaying = false;

                mediaPlayer.pause();
            }

            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (seekBar.getMax() == progress) {

                    if (!ttsinitflag) {
                        return;
                    }
                    if (!ttsfileflag) {

                        return;

                    }


                    if (mediaPlayer == null) {
                        return;
                    }
                    btn_play.setImageResource(R.drawable.ico_audio_play);
                    seekBar.setProgress(0);
                    mediaPlayer.seekTo(0);
                    // bStart.setVisibility(View.VISIBLE);
                    // bStop.setVisibility(View.INVISIBLE);
                    // bPause.setVisibility(View.INVISIBLE);
                    // bRestart.setVisibility(View.INVISIBLE);
                    isPlaying = false;
                    mediaPlayer.pause();
                }
            }
        });

        btn_refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!ttsinitflag) {
                    return;
                }
                if (!ttsfileflag) {
                    return;
                }
                if (mediaPlayer == null) {
                    return;
                }
                seekBar.setProgress(0);

                mediaPlayer.seekTo(0);
                pos = 0;
                play();

            }
        });


        btn_play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!ttsinitflag) {
                    return;
                }
                if (!ttsfileflag) {
                    return;
                }
                if (mediaPlayer == null) {
                    return;
                }

                String text = ttscontents;


                /*
                if (myTTS != null) {
                    if (myTTS.isSpeaking()) {
                        myTTS.stop();
                    } else {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            ttsGreater21(text);
                        } else {
                            ttsUnder20(text);
                        }
                    }
                } else {
                    AppLog.print("tts", "null");
                }

                */


                if (ttsfileflag) {
                    if (mediaPlayer.isPlaying()) {
                        playStop();
                    } else {

                        play();
                    }

                }
            }
        });
    }

    public TtsPlay(final Context context, String ttsDesc, TextView _currenttimetext, SeekBar _seekBar, TextView _txt_TotalTime, ImageButton _btn_play, ImageButton _btn_refrech, boolean isAutoStart) {
        setAutoStart(isAutoStart);
        initTtsPlay(context, ttsDesc, _currenttimetext, _seekBar, _txt_TotalTime, _btn_play, _btn_refrech);
    }

    public TtsPlay(final Context context, String ttsDesc, TextView _currenttimetext, SeekBar _seekBar, TextView _txt_TotalTime, ImageButton _btn_play, ImageButton _btn_refrech) {
        initTtsPlay(context, ttsDesc, _currenttimetext, _seekBar, _txt_TotalTime, _btn_play, _btn_refrech);
    }
}
