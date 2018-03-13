package com.dogusumit.game;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.games.Games;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class FullscreenActivity extends AppCompatActivity {

    private static final int RC_LEADERBOARD_UI = 9004;
    private static final int RC_SIGN_IN = 9003;
    private final Context context = this;
    GoogleSignInAccount signedInAccount;
    RelativeLayout game_layout;
    ImageButton main_button[], game_pause;
    TextView main_text[], game_skor;
    ImageView karakter, arkaplan1, arkaplan2;
    int oyunDurum = -1; //-1=finish,0=pause,1=resume
    int ekranW, ekranH, karakterW, karakterH;
    int cubukAdet = 5;
    ArrayList<Cubuk> cubuklar;
    ImageView bonus;
    int bonusSkor, bonusCache;
    ImageView ceza;
    int cezaSkor, cezaCache;
    boolean asagiBool;
    int ziplaDurum, skor, ustunde;
    double ziplaToplam;
    float sensorYon = 0;
    MediaPlayer mediaGame, mediaJump, mediaGameOver;
    AdView mAdView;
    AdRequest adRequest;
    double katsayi;
    Timer timer;

    void bonusHareket() {
        try {
            if (bonus != null) {
                if (bonus.getY() + bonus.getHeight() > karakter.getY()
                                && bonus.getY() < karakter.getY() + karakterH
                                && bonus.getX() + bonus.getWidth() > karakter.getX()
                                && bonus.getX() < karakter.getX() + karakterW)
                {
                    game_skor.setTextColor(Color.GREEN);
                    game_skor.setText(String.format(Integer.toString(skor+bonusSkor-cezaSkor)+"+5", Locale.US));
                    game_layout.removeView(bonus);
                    bonusSkor += 5;
                    bonus = null;
                } else if (bonus.getY() < ekranH) {
                    bonus.setY(bonus.getY() + (float) (katsayi * 1.5));
                } else {
                    game_layout.removeView(bonus);
                    bonus = null;
                }
            } else {
                if (skor>bonusCache && skor %5 == 0) {
                    bonus = new ImageView(context);
                    bonus.setLayoutParams(new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                    bonus.setImageResource(R.mipmap.ic_bonus);
                    bonus.setY(0);
                    Drawable drawable = getResources().getDrawable(R.mipmap.ic_bonus);
                    bonus.setX(new Random().nextInt(ekranW-drawable.getIntrinsicWidth()));
                    game_layout.addView(bonus);
                    bonusCache = skor;
                }
            }
        } catch (Exception e) {
            String error = "error : bonusHareket\n" + e.getMessage();
            Toast.makeText(context, error, Toast.LENGTH_LONG).show();
        }
    }

    void cezaHareket() {
        try {
            if (ceza != null) {
                if (ceza.getY() + ceza.getHeight() > karakter.getY()
                                && ceza.getY() < karakter.getY() + karakterH
                                && ceza.getX() + ceza.getWidth() > karakter.getX()
                                && ceza.getX() < karakter.getX() + karakterW)
                {
                    game_skor.setTextColor(Color.RED);
                    game_skor.setText(String.format(Integer.toString(skor+bonusSkor-cezaSkor)+"-5", Locale.US));
                    game_layout.removeView(ceza);
                    cezaSkor += 5;
                    ceza = null;
                } else if (ceza.getY() < ekranH) {
                    ceza.setY(ceza.getY() + (float) (katsayi * 1.5));
                } else {
                    game_layout.removeView(ceza);
                    ceza = null;
                }
            } else {
                if (skor>cezaCache && skor %5 == 2) {
                    ceza = new ImageView(context);
                    ceza.setLayoutParams(new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                    ceza.setImageResource(R.mipmap.ic_ceza);
                    ceza.setY(0);
                    Drawable drawable = getResources().getDrawable(R.mipmap.ic_ceza);
                    ceza.setX(new Random().nextInt(ekranW-drawable.getIntrinsicWidth()));
                    game_layout.addView(ceza);
                    cezaCache = skor;
                }
            }
        } catch (Exception e) {
            String error = "error : cezaHareket\n" + e.getMessage();
            Toast.makeText(context, error, Toast.LENGTH_LONG).show();
        }
    }

    void yatayHaraket() {
        try {
            for (Cubuk cubuk : cubuklar) {
                if (cubuk.yon == 1) {
                    if (cubuk.imageView.getX() + cubuk.imageView.getWidth() < ekranW) {
                        cubuk.imageView.setX(cubuk.imageView.getX() + (float)(katsayi*1.5));
                    } else {
                        cubuk.yon = 0;
                    }
                } else {
                    if (cubuk.imageView.getX() > 0) {
                        cubuk.imageView.setX(cubuk.imageView.getX() - (float)(katsayi*1.5));
                    } else {
                        cubuk.yon = 1;
                    }
                }
            }
            if (ustunde > -1 && ziplaDurum == 0) {
                if (cubuklar.get(ustunde).yon == 1) {
                    karakter.setX(karakter.getX() + (float)(katsayi*1.5));
                } else {
                    karakter.setX(karakter.getX() - (float)(katsayi*1.5));
                }
            }
        } catch (Exception e) {
            String error = "error : yatayHareket\n" + e.getMessage();
            Toast.makeText(context, error, Toast.LENGTH_LONG).show();
        }
    }

    void ziplaHareket() {
        try {
            if (ziplaDurum != 0) {
                if (ziplaDurum == 1) {
                    if (ziplaToplam < ((ekranH / cubukAdet) + karakterH)) {
                        karakter.setY(karakter.getY() > (float) (katsayi * 2) ? karakter.getY() - (float) (katsayi * 2) : 0);
                        ziplaToplam += (katsayi * 2);
                        karakter.setImageResource(R.mipmap.karakter_jump);
                        if (!(karakter.getX() < 0 && sensorYon < 0)
                                && !(karakter.getX()+karakterW>ekranW && sensorYon>0))
                            karakter.setX(karakter.getX() + sensorYon);
                    } else {
                        ziplaDurum = -1;
                        ziplaToplam = 0;
                    }
                } else if (ziplaDurum == -1) {
                    if (karakter.getY() + (karakterH / 6 * 5) < ekranH) {
                        karakter.setY(karakter.getY() + (float) (katsayi * 2));
                        karakter.setImageResource(R.mipmap.karakter);
                        if (karakter.getX() + sensorYon >= 0 && karakter.getX() + karakterW + sensorYon <= ekranW)
                            karakter.setX(karakter.getX() + sensorYon);
                    } else {
                        ziplaDurum = 0;
                        if (skor > 0)
                            oyunDurdur(-1);
                    }
                }
                int j = 0;
                for (Cubuk cubuk : cubuklar) {
                    if (
                            (karakter.getY() + karakterH) <
                                    (cubuk.imageView.getY() + cubuk.height) &&
                                    (karakter.getY() + karakterH + (katsayi * 2)) >=
                                            (cubuk.imageView.getY() + cubuk.height)) {
                        if (
                                Math.round(karakter.getX() + (karakterW / 4))
                                        >= Math.round(cubuk.imageView.getX())
                                        && Math.round(karakter.getX() + (karakterW / 4))
                                        <= Math.round(cubuk.imageView.getX()
                                        + cubuk.imageView.getWidth())) {
                            karakter.setImageResource(R.mipmap.karakter);
                            ziplaDurum = 0;
                            ziplaToplam = 0;
                            ustunde = j;
                            skor = cubuk.skor;
                            game_skor.setTextColor(Color.WHITE);
                            game_skor.setText(String.format(Integer.toString(skor+bonusSkor-cezaSkor), Locale.US));
                            asagiBool = true;
                            katsayi = karakterH / 70.0 + (skor * karakterH / 1000.0);
                            break;
                        }
                    }
                    j++;
                }
            }
        } catch (Exception e) {
            String error = "error : ziplaHareket\n" + e.getMessage();
            Toast.makeText(context, error, Toast.LENGTH_LONG).show();
        }
    }

    void asagiHareket() {
        try {
            if (asagiBool) {
                for (int i = 0; i < cubuklar.size(); i++) {
                    if ((cubuklar.get(i).imageView.getY() + (float) katsayi) >= ekranH) {
                        game_layout.removeView(cubuklar.get(i).imageView);
                        int tmp = cubuklar.get(i).skor;
                        cubuklar.set(i, new Cubuk());
                        cubuklar.get(i).skor = tmp + cubukAdet;
                        cubuklar.get(i).imageView.setY(0);
                        game_layout.addView(cubuklar.get(i).imageView);
                    } else {
                        cubuklar.get(i).imageView.setY(cubuklar.get(i).imageView.getY() + (float) katsayi);
                    }
                }
                if (ustunde > -1) {
                    if (karakter.getY() + (karakterH / 4 * 3) >= ekranH) {
                        oyunDurdur(-1);
                    } else {
                        karakter.setY(karakter.getY() + (float) katsayi);
                    }
                }
                if (arkaplan1.getY() + (float) katsayi <= ekranH) {
                    arkaplan1.setY(arkaplan1.getY() + (float) katsayi);
                    arkaplan2.setY(arkaplan2.getY() + (float) katsayi);
                } else {
                    arkaplan1.setY(0);
                    arkaplan2.setY(-ekranH);
                }
            }
        } catch (Exception e) {
            String error = "error : asagiHareket\n" + e.getMessage();
            Toast.makeText(context, error, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);

            requestWindowFeature(Window.FEATURE_NO_TITLE);
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);

            DisplayMetrics metrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(metrics);
            ekranH = metrics.heightPixels;
            ekranW = metrics.widthPixels;

            karakter = new ImageView(context);
            Drawable drawable = getResources().getDrawable(R.mipmap.karakter);
            karakterW = (int) (drawable.getIntrinsicWidth() / 1.0);
            karakterH = (int) (drawable.getIntrinsicHeight() / 1.0);
            karakter.setLayoutParams(new LinearLayout.LayoutParams(karakterW, karakterH));
            karakter.setBackgroundResource(R.mipmap.karakter);

            mediaGame = MediaPlayer.create(this, R.raw.ozzed_cooking_together);
            mediaGame.setLooping(true);
            mediaGame.setVolume(0.5f, 0.5f);
            mediaJump = MediaPlayer.create(this, R.raw.jump_sound_wav);
            mediaJump.setLooping(false);
            mediaJump.setVolume(1.0f, 1.0f);
            mediaGameOver = MediaPlayer.create(this, R.raw.game_over_wav);
            mediaGameOver.setLooping(false);
            mediaGameOver.setVolume(0.5f, 0.5f);

            adRequest = new AdRequest.Builder().build();

            loadMainScreen();

            SensorManager mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
            if (mSensorManager != null) {
                Sensor mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
                if (mSensor != null) {
                    SensorEventListener gyroscopeSensorListener = new SensorEventListener() {
                        @Override
                        public void onSensorChanged(SensorEvent sensorEvent) {
                            if (oyunDurum == 1) {
                                if (sensorEvent.values[0] > +1)
                                    sensorYon = (float) katsayi * sensorEvent.values[0] * -1;
                                else if (sensorEvent.values[0] < -1)
                                    sensorYon = (float) katsayi * sensorEvent.values[0] * -1;
                                else
                                    sensorYon = 0;
                            }
                        }

                        @Override
                        public void onAccuracyChanged(Sensor sensor, int i) {
                        }
                    };
                    mSensorManager.registerListener(gyroscopeSensorListener,
                            mSensor, SensorManager.SENSOR_DELAY_GAME);
                } else
                    Toast.makeText(context, "sensor yok", Toast.LENGTH_SHORT).show();
            } else
                Toast.makeText(context, "manager yok", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            String error = "error : onCreate\n" + e.getMessage();
            Toast.makeText(context, error, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (oyunDurum == 1 && ziplaDurum == 0) {
                    ziplaDurum = 1;
                    ziplaToplam = 0;
                    playJumpSound();
                }
        }
        return false;
    }

    void loadMainScreen() {
        try {
            setContentView(R.layout.main_layout);

            mAdView = findViewById(R.id.adView);
            mAdView.loadAd(adRequest);
            submitScore();

            main_button = new ImageButton[6];
            main_text = new TextView[3];
            main_button[0] = findViewById(R.id.main_button1);
            main_button[1] = findViewById(R.id.main_button2);
            main_button[2] = findViewById(R.id.main_button3);
            main_button[3] = findViewById(R.id.main_button4);
            main_button[4] = findViewById(R.id.main_button5);
            main_button[5] = findViewById(R.id.main_button6);
            main_text[0] = findViewById(R.id.main_text1);
            main_text[1] = findViewById(R.id.main_text2);
            main_text[2] = findViewById(R.id.main_text3);

            SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
            int bestScore = sharedPref.getInt("bestScore", 0);
            int soundOn = sharedPref.getInt("soundOn", 1);
            main_text[1].setText(String.format(getString(R.string.bestscore) + bestScore, Locale.US));
            main_text[2].setText(null);
            if (soundOn == 1)
                main_button[1].setImageResource(R.drawable.ic_volume);
            else
                main_button[1].setImageResource(R.drawable.ic_mute);
            main_button[0].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    loadGameScreen();
                }
            });
            main_button[1].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
                    int soundOn = sharedPref.getInt("soundOn", 1);
                    if (soundOn == 1) {
                        sharedPref.edit().putInt("soundOn", 0).apply();
                        main_button[1].setImageResource(R.drawable.ic_mute);
                    } else {
                        sharedPref.edit().putInt("soundOn", 1).apply();
                        main_button[1].setImageResource(R.drawable.ic_volume);
                    }
                }
            });
            main_button[2].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    signInGoogle();
                }
            });
            main_button[3].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    cikis();
                }
            });
            main_button[4].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    uygulamayiOyla();
                }
            });
            main_button[5].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    marketiAc();
                }
            });

        } catch (Exception e) {
            String error = "error : loadMainScreen\n" + e.getMessage();
            Toast.makeText(context, error, Toast.LENGTH_LONG).show();
        }
    }

    void loadGameScreen() {
        try {
            if (oyunDurum == -1) {
                ziplaDurum = 0;
                ziplaToplam = 0;
                skor = 0;
                ustunde = -1;
                katsayi = karakterH / 70.0;
                bonus = null;
                bonusSkor = 0;
                bonusCache = 1;
                ceza = null;
                cezaSkor = 0;
                cezaCache = 3;
            }
            game_layout = new RelativeLayout(context);
            setContentView(game_layout);
            game_layout.setKeepScreenOn(true);
            if (oyunDurum == -1) {
                arkaplan1 = new ImageView(context);
                arkaplan1.setLayoutParams(new RelativeLayout.LayoutParams(
                        RelativeLayout.LayoutParams.MATCH_PARENT,
                        RelativeLayout.LayoutParams.MATCH_PARENT
                ));
                arkaplan1.setImageResource(R.drawable.arkaplan);
                arkaplan1.setAdjustViewBounds(true);
                arkaplan1.setScaleType(ImageView.ScaleType.FIT_XY);
                arkaplan1.setY(0);
                arkaplan2 = new ImageView(context);
                arkaplan2.setLayoutParams(new RelativeLayout.LayoutParams(
                        RelativeLayout.LayoutParams.MATCH_PARENT,
                        RelativeLayout.LayoutParams.MATCH_PARENT
                ));
                arkaplan2.setImageResource(R.drawable.arkaplan);
                arkaplan2.setAdjustViewBounds(true);
                arkaplan2.setScaleType(ImageView.ScaleType.FIT_XY);
                arkaplan2.setY(-ekranH);
                game_skor = new TextView(context);
                RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                        RelativeLayout.LayoutParams.WRAP_CONTENT,
                        RelativeLayout.LayoutParams.WRAP_CONTENT
                );
                params.addRule(RelativeLayout.ALIGN_PARENT_TOP);
                params.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
                params.setMargins(15, 15, 15, 15);
                game_skor.setLayoutParams(params);
                game_skor.setTextColor(Color.WHITE);
                game_skor.setTextSize(25);
                game_pause = new ImageButton(context);
                params = new RelativeLayout.LayoutParams(
                        RelativeLayout.LayoutParams.WRAP_CONTENT,
                        RelativeLayout.LayoutParams.WRAP_CONTENT
                );
                params.addRule(RelativeLayout.ALIGN_PARENT_TOP);
                params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                game_pause.setLayoutParams(params);
                game_pause.setImageResource(R.drawable.ic_pause);
                game_pause.setBackgroundColor(Color.TRANSPARENT);
                game_pause.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (oyunDurum == 1) {
                            oyunDurdur(0);
                        }
                    }
                });
                karakter.setX(ekranW / 2 - (karakterW / 2));
                karakter.setY(ekranH - karakterH);
                cubuklar = new ArrayList<>();
                for (int i = 1; i <= cubukAdet; i++) {
                    Cubuk tmp = new Cubuk();
                    tmp.skor = i;
                    tmp.imageView.setY(ekranH - ((ekranH / cubukAdet) * i));
                    cubuklar.add(tmp);
                }
            }
            game_skor.setText(String.format(Integer.toString(skor+bonusSkor-cezaSkor), Locale.US));
            game_layout.addView(arkaplan1);
            game_layout.addView(arkaplan2);
            game_layout.addView(game_skor);
            game_layout.addView(game_pause);
            game_layout.addView(karakter);
            for (Cubuk cubuk : cubuklar)
                game_layout.addView(cubuk.imageView);

            asagiBool = false;
            oyunDurum = 1;
            soundStartStop();

            timer = new Timer();
            timer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ziplaHareket();
                            yatayHaraket();
                            asagiHareket();
                            bonusHareket();
                            cezaHareket();
                        }
                    });
                }
            }, 100, 20);

        } catch (Exception e) {
            String error = "error : loadGameScreen\n" + e.getMessage();
            Toast.makeText(context, error, Toast.LENGTH_LONG).show();
        }
    }

    void oyunDurdur(int durum) {
        try {
            timer.cancel();
            oyunDurum = durum;

            soundStartStop();
            if (oyunDurum == -1)
                playGameOverSound();

            game_layout.removeAllViews();
            loadMainScreen();
            if (oyunDurum == -1)
                main_text[0].setText(getString(R.string.game_over));
            else
                main_text[0].setText(getString(R.string.pause));

            SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
            int bestScore = sharedPref.getInt("bestScore", 0);
            if ((skor+bonusSkor-cezaSkor) > bestScore) {
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putInt("bestScore", (skor+bonusSkor-cezaSkor)).apply();
                main_text[1].setText(String.format(getString(R.string.bestscore) + (skor+bonusSkor-cezaSkor), Locale.US));
            } else {
                main_text[1].setText(String.format(getString(R.string.bestscore) + bestScore, Locale.US));
            }
            main_text[2].setText(String.format(getString(R.string.score) + (skor+bonusSkor-cezaSkor), Locale.US));
        } catch (Exception e) {
            String error = "error : oyunDurdur\n" + e.getMessage();
            Toast.makeText(context, error, Toast.LENGTH_LONG).show();
        }
    }

    void cikis() {
        try {
            new AlertDialog.Builder(context)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setMessage(getString(R.string.areyousure))
                    .setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }

                    })
                    .setNegativeButton(getString(R.string.no), null)
                    .show();
        } catch (Exception e) {
            String error = "error : cikis\n" + e.getMessage();
            Toast.makeText(context, error, Toast.LENGTH_LONG).show();
        }
    }

    void soundStartStop() {
        try {
            SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
            int soundOn = sharedPref.getInt("soundOn", 1);
            if (oyunDurum == 1 && soundOn == 1 && !mediaGame.isPlaying())
                mediaGame.start();
            else if (oyunDurum == 0 && mediaGame.isPlaying())
                mediaGame.pause();
            else if (oyunDurum == -1 && mediaGame.isPlaying()) {
                mediaGame.pause();
                mediaGame.seekTo(0);
            }
        } catch (Exception e) {
            String error = "error  : soundStartStop\n" + e.getMessage();
            Toast.makeText(context, error, Toast.LENGTH_LONG).show();
        }
    }

    void playJumpSound() {
        try {
            SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
            int soundOn = sharedPref.getInt("soundOn", 1);
            if (soundOn == 1 && !mediaJump.isPlaying())
                mediaJump.start();
        } catch (Exception e) {
            String error = "error : playJumpSound\n" + e.getMessage();
            Toast.makeText(context, error, Toast.LENGTH_LONG).show();
        }
    }

    void playGameOverSound() {
        try {
            SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
            int soundOn = sharedPref.getInt("soundOn", 1);
            if (soundOn == 1 && !mediaGameOver.isPlaying())
                mediaGameOver.start();
        } catch (Exception e) {
            String error = "error : playGameOverSound\n" + e.getMessage();
            Toast.makeText(context, error, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onBackPressed() {
        if (oyunDurum == 1)
            oyunDurdur(0);
        else
            cikis();
    }

    @Override
    protected void onPause() {
        if (oyunDurum == 1)
            oyunDurdur(0);
        super.onPause();
    }

    private void uygulamayiOyla() {
        Uri uri = Uri.parse("market://details?id=" + getApplicationContext().getPackageName());
        Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
        goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY |
                Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
        try {
            startActivity(goToMarket);
        } catch (ActivityNotFoundException e) {
            try {
                startActivity(new Intent(Intent.ACTION_VIEW,
                        Uri.parse("https://play.google.com/store/apps/details?id=" + getApplicationContext().getPackageName())));
            } catch (Exception ane) {
                Toast.makeText(context, ane.getLocalizedMessage(), Toast.LENGTH_LONG).show();
            }
        }
    }

    private void marketiAc() {
        try {
            Uri uri = Uri.parse("market://developer?id=dogusumit");
            Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
            goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY |
                    Intent.FLAG_ACTIVITY_MULTIPLE_TASK);

            startActivity(goToMarket);
        } catch (ActivityNotFoundException e) {
            try {
                startActivity(new Intent(Intent.ACTION_VIEW,
                        Uri.parse("https://play.google.com/store/apps/developer?id=dogusumit")));
            } catch (Exception ane) {
                Toast.makeText(context, ane.getLocalizedMessage(), Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try {
            if (requestCode == RC_SIGN_IN) {
                GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
                if (result.isSuccess()) {
                    signedInAccount = result.getSignInAccount();
                    showLeaderboard();
                } else {
                    String message = result.getStatus().getStatusMessage();
                    if (message == null || message.isEmpty()) {
                        message = getString(R.string.signError);
                    }
                    new AlertDialog.Builder(context).setMessage(message)
                            .setNeutralButton(android.R.string.ok, null).show();
                }
            }
        } catch (Exception e) {
            String error = "error : onActivityResult\n" + e.getMessage();
            Toast.makeText(context, error, Toast.LENGTH_LONG).show();
        }
    }

    private void submitScore() {
        try {
            if (signedInAccount != null) {
                SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
                int bestScore = sharedPref.getInt("bestScore", 0);
                Games.getLeaderboardsClient(context, signedInAccount)
                        .submitScore(getString(R.string.leaderboard_id), bestScore);
            }
        } catch (Exception e) {
            String error = "error : submitScore\n" + e.getMessage();
            Toast.makeText(context, error, Toast.LENGTH_LONG).show();
        }
    }

    private void showLeaderboard() {
        try {
            if (signedInAccount != null) {
                Games.getLeaderboardsClient(context, signedInAccount)
                        .getLeaderboardIntent(getString(R.string.leaderboard_id))
                        .addOnSuccessListener(new OnSuccessListener<Intent>() {
                            @Override
                            public void onSuccess(Intent intent) {
                                startActivityForResult(intent, RC_LEADERBOARD_UI);
                            }
                        });
            }
        } catch (Exception e) {
            String error = "error : showLeaderboard\n" + e.getMessage();
            Toast.makeText(context, error, Toast.LENGTH_LONG).show();
        }
    }

    private void signInGoogle() {
        try {
            if (signedInAccount == null) {
                GoogleSignInClient signInClient = GoogleSignIn.getClient(context,
                        GoogleSignInOptions.DEFAULT_GAMES_SIGN_IN);
                Intent intent = signInClient.getSignInIntent();
                startActivityForResult(intent, RC_SIGN_IN);
            } else {
                showLeaderboard();
            }
        } catch
                (Exception e) {
            String error = "error : signInGoogle\n" + e.getMessage();
            Toast.makeText(context, error, Toast.LENGTH_LONG).show();
        }
    }

    class Cubuk {
        int yon, skor;
        int width = new Random().nextInt(ekranW / 5) + (ekranW / 5);
        int height = ekranH / 30;
        ImageView imageView;

        Cubuk() {
            try {
                imageView = new ImageView(context);
                imageView.setLayoutParams(new LinearLayout.LayoutParams(width, height));
                imageView.setBackgroundResource(R.drawable.ic_cloud);
                imageView.setAdjustViewBounds(true);
                imageView.setScaleType(ImageView.ScaleType.FIT_XY);
                imageView.setX(new Random().nextInt(ekranW - width));
                yon = new Random().nextInt(1);
                skor = 0;
            } catch (Exception e) {
                String error = "error : Cubuk\n" + e.getMessage();
                Toast.makeText(context, error, Toast.LENGTH_LONG).show();
            }
        }
    }

}