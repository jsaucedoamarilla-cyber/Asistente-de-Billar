package com.punteriauniversal.app;

import android.app.Activity;
import android.os.Bundle;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.view.Gravity;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.io.InputStream;
import org.opencv.android.OpenCVLoader;

public class MainActivity extends Activity {
    private AimView aimView;
    private TextView status;
    private String tool = "cue";
    private static final int PICK_IMAGE = 81;
    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        boolean cvReady = OpenCVLoader.initLocal();
        getWindow().setStatusBarColor(Color.rgb(8,16,29));
        getWindow().setNavigationBarColor(Color.rgb(8,16,29));
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(14),dp(14),dp(14),dp(12));
        root.setBackgroundColor(Color.rgb(8,16,29));
        TextView title = new TextView(this);
        title.setText("🎯 PUNTERÍA UNIVERSAL"); title.setTextColor(Color.WHITE); title.setTextSize(23); title.setGravity(Gravity.CENTER);
        title.setPadding(0,dp(8),0,dp(4)); title.setTypeface(null,1);
        root.addView(title,new LinearLayout.LayoutParams(-1,-2));
        TextView sub = new TextView(this);
        sub.setText("DETECCIÓN EXPERIMENTAL DE BOLAS · VERSIÓN 0.2");
        sub.setTextColor(Color.rgb(65,211,188)); sub.setTextSize(11); sub.setGravity(Gravity.CENTER);
        sub.setPadding(0,0,0,dp(12)); root.addView(sub);
        Button load = button("📷 CARGAR CAPTURA DEL BILLAR");
        Button detect = button("✨ DETECTAR BOLAS AUTOMÁTICAMENTE");
        detect.setOnClickListener(v -> setStatus(cvReady ? aimView.autoDetectBalls() : "No se pudo iniciar el detector."));
        load.setOnClickListener(v -> { Intent i=new Intent(Intent.ACTION_GET_CONTENT); i.setType("image/*"); startActivityForResult(Intent.createChooser(i,"Elegir captura"),PICK_IMAGE); });
        root.addView(load,new LinearLayout.LayoutParams(-1,dp(48)));
        root.addView(detect,new LinearLayout.LayoutParams(-1,dp(48)));
        aimView = new AimView(this);
        LinearLayout.LayoutParams canvasParams = new LinearLayout.LayoutParams(-1,0,1f);
        canvasParams.setMargins(0,dp(10),0,dp(10)); root.addView(aimView,canvasParams);
        TextView hint = new TextView(this);
        hint.setText("Elegí qué marcar y tocá la captura: blanca → objetivo → tronera.");
        hint.setTextColor(Color.LTGRAY); hint.setTextSize(12); hint.setPadding(0,dp(4),0,dp(8)); root.addView(hint);
        LinearLayout tools = new LinearLayout(this); tools.setOrientation(LinearLayout.HORIZONTAL);
        Button cue=button("⚪ Blanca"), ball=button("🎱 Objetivo"), pocket=button("🕳 Tronera");
        tools.addView(cue,new LinearLayout.LayoutParams(0,dp(44),1)); tools.addView(ball,new LinearLayout.LayoutParams(0,dp(44),1)); tools.addView(pocket,new LinearLayout.LayoutParams(0,dp(44),1));
        root.addView(tools);
        cue.setOnClickListener(v->{tool="cue";aimView.setTool(tool);setStatus("Marcá la bola blanca.");});
        ball.setOnClickListener(v->{tool="ball";aimView.setTool(tool);setStatus("Marcá la bola objetivo.");});
        pocket.setOnClickListener(v->{tool="pocket";aimView.setTool(tool);setStatus("Marcá la tronera elegida.");});
        LinearLayout actions = new LinearLayout(this); actions.setOrientation(LinearLayout.HORIZONTAL);
        Button calc=button("🎯 CALCULAR TIRO"), clear=button("BORRAR");
        actions.addView(calc,new LinearLayout.LayoutParams(0,dp(48),2)); actions.addView(clear,new LinearLayout.LayoutParams(0,dp(48),1));
        LinearLayout.LayoutParams actionParams=new LinearLayout.LayoutParams(-1,-2); actionParams.setMargins(0,dp(8),0,dp(6)); root.addView(actions,actionParams);
        calc.setOnClickListener(v->{String result=aimView.calculate();setStatus(result);});
        clear.setOnClickListener(v->{aimView.clearMarks();setStatus("Marcas borradas.");});
        status = new TextView(this); status.setText("Primero cargá una captura de pantalla."); status.setTextColor(Color.rgb(65,211,188)); status.setTextSize(12); status.setPadding(dp(4),dp(4),dp(4),dp(4)); root.addView(status);
        aimView.setTool(tool);
        setContentView(root);
    }
    private void setStatus(String s){ if(status!=null) status.setText(s); }
    private Button button(String text){ Button b=new Button(this); b.setText(text); b.setTextSize(11); b.setTextColor(Color.WHITE); b.setAllCaps(false); b.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.rgb(30,58,78))); return b; }
    private int dp(float n){return (int)(n*getResources().getDisplayMetrics().density+0.5f);}
    @Override protected void onActivityResult(int requestCode,int resultCode,Intent data){
        super.onActivityResult(requestCode,resultCode,data);
        if(requestCode==PICK_IMAGE && resultCode==RESULT_OK && data!=null){
            try(InputStream in=getContentResolver().openInputStream(data.getData())){
                Bitmap bmp=BitmapFactory.decodeStream(in);
                if(bmp!=null){aimView.setBitmap(bmp);setStatus("Captura cargada. Tocá DETECTAR BOLAS para buscar círculos.");}
                else setStatus("No se pudo leer esa imagen.");
            }catch(Exception e){setStatus("No se pudo abrir la captura.");}
        }
    }
}
