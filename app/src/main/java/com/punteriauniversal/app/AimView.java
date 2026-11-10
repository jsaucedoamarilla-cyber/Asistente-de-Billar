package com.punteriauniversal.app;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.view.MotionEvent;
import android.view.View;
import java.util.ArrayList;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

public class AimView extends View {
    private Bitmap bitmap;
    private final Paint paint=new Paint(3);
    private final ArrayList<Mark> marks=new ArrayList<>();
    private String tool="cue";
    private float left,top,scale;
    private boolean calculated=false;
    private static class Mark { float x,y; String type; Mark(float x,float y,String t){this.x=x;this.y=y;type=t;} }
    public AimView(Context c){super(c);setBackgroundColor(Color.rgb(14,29,44));}
    public void setTool(String t){tool=t;}
    public void setBitmap(Bitmap b){bitmap=b;marks.clear();calculated=false;invalidate();}
    public void clearMarks(){marks.clear();calculated=false;invalidate();}

    public String autoDetectBalls(){
        if(bitmap==null)return "Primero cargá una captura del billar.";
        Mat rgba=new Mat(), gray=new Mat(), circles=new Mat();
        try{
            Bitmap work=bitmap;
            int maxSide=1200;
            if(Math.max(bitmap.getWidth(),bitmap.getHeight())>maxSide){
                float f=maxSide/(float)Math.max(bitmap.getWidth(),bitmap.getHeight());
                work=Bitmap.createScaledBitmap(bitmap,Math.round(bitmap.getWidth()*f),Math.round(bitmap.getHeight()*f),true);
            }
            Utils.bitmapToMat(work,rgba);
            Imgproc.cvtColor(rgba,gray,Imgproc.COLOR_RGBA2GRAY);
            Imgproc.GaussianBlur(gray,gray,new Size(9,9),2,2);
            Imgproc.HoughCircles(gray,circles,Imgproc.HOUGH_GRADIENT,1.2,Math.max(12,work.getWidth()*0.025),100,26,
                    Math.max(5,work.getWidth()/100),Math.max(9,work.getWidth()/18));
            marks.clear();
            int found=0,white=0;
            double factor=bitmap.getWidth()/(double)work.getWidth();
            for(int i=0;i<circles.cols();i++){
                double[] v=circles.get(0,i);
                if(v==null||v.length<3)continue;
                float x=(float)(v[0]*factor),y=(float)(v[1]*factor);
                int px=Math.max(0,Math.min(work.getWidth()-1,(int)v[0]));
                int py=Math.max(0,Math.min(work.getHeight()-1,(int)v[1]));
                Mat sample=rgba.submat(Math.max(0,py-2),Math.min(rgba.rows(),py+3),Math.max(0,px-2),Math.min(rgba.cols(),px+3));
                Mat rgb=new Mat(),hsv=new Mat();
                Imgproc.cvtColor(sample,rgb,Imgproc.COLOR_RGBA2RGB);
                Imgproc.cvtColor(rgb,hsv,Imgproc.COLOR_RGB2HSV);
                Scalar mean=Core.mean(hsv);
                boolean isWhite=mean.val[1]<75&&mean.val[2]>145;
                marks.add(new Mark(x,y,isWhite?"cue":"ball"));
                if(isWhite)white++;
                found++;
                sample.release();rgb.release();hsv.release();
            }
            calculated=false;invalidate();
            if(found==0)return "No encontré bolas claras. Probá una captura nítida de la mesa completa.";
            return "Detección experimental: "+found+" círculos; "+white+" posibles bolas blancas. Revisá las marcas: puede confundir otros círculos del juego.";
        }catch(Exception e){return "Falló la detección: "+e.getMessage();}
        finally{rgba.release();gray.release();circles.release();}
    }

    @Override protected void onDraw(Canvas c){
        super.onDraw(c);
        if(bitmap==null){paint.setColor(Color.LTGRAY);paint.setTextAlign(Paint.Align.CENTER);paint.setTextSize(15);c.drawText("Cargá una captura para comenzar",getWidth()/2f,getHeight()/2f,paint);return;}
        float sx=getWidth()/(float)bitmap.getWidth(), sy=getHeight()/(float)bitmap.getHeight();
        scale=Math.min(sx,sy);float w=bitmap.getWidth()*scale,h=bitmap.getHeight()*scale;
        left=(getWidth()-w)/2f;top=(getHeight()-h)/2f;
        c.drawBitmap(bitmap,null,new RectF(left,top,left+w,top+h),paint);
        for(Mark m:marks){float x=left+m.x*scale,y=top+m.y*scale;
            paint.setStyle(Paint.Style.STROKE);paint.setStrokeWidth(4);paint.setColor(color(m.type));c.drawCircle(x,y,Math.max(9,bitmap.getWidth()*.012f*scale),paint);
            paint.setStyle(Paint.Style.FILL);paint.setTextSize(18);paint.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);c.drawText(label(m.type),x+12,y-10,paint);
        }
        if(calculated) drawGuide(c);
    }
    private void drawGuide(Canvas c){
        Mark cue=find("cue"),ball=find("ball"),pocket=find("pocket");if(cue==null||ball==null||pocket==null)return;
        paint.setColor(Color.YELLOW);paint.setStrokeWidth(4);paint.setStyle(Paint.Style.STROKE);paint.setPathEffect(new android.graphics.DashPathEffect(new float[]{12,8},0));
        c.drawLine(left+cue.x*scale,top+cue.y*scale,left+ball.x*scale,top+ball.y*scale,paint);
        paint.setColor(Color.rgb(55,240,190));c.drawLine(left+ball.x*scale,top+ball.y*scale,left+pocket.x*scale,top+pocket.y*scale,paint);
        paint.setPathEffect(null);paint.setStyle(Paint.Style.FILL);
    }
    @Override public boolean onTouchEvent(MotionEvent e){
        if(e.getAction()!=MotionEvent.ACTION_UP||bitmap==null)return true;
        float x=(e.getX()-left)/scale,y=(e.getY()-top)/scale;
        if(x<0||y<0||x>bitmap.getWidth()||y>bitmap.getHeight())return true;
        if(tool.equals("cue"))remove("cue");
        if(tool.equals("pocket"))remove("pocket");
        marks.add(new Mark(x,y,tool));calculated=false;invalidate();return true;
    }
    public String calculate(){
        Mark cue=find("cue"),ball=find("ball"),pocket=find("pocket");
        if(bitmap==null)return "Primero cargá una captura.";
        if(cue==null||ball==null||pocket==null)return "Faltan marcas: blanca, objetivo y tronera.";
        double angle=Math.toDegrees(Math.atan2(ball.y-cue.y,ball.x-cue.x));if(angle<0)angle+=360;
        double d1=Math.hypot(ball.x-cue.x,ball.y-cue.y),d2=Math.hypot(pocket.x-ball.x,pocket.y-ball.y);
        calculated=true;invalidate();
        return String.format(java.util.Locale.US,"Línea calculada · %.1f° · blanca→objetivo %.0f px · objetivo→tronera %.0f px",angle,d1,d2);
    }
    private Mark find(String t){for(Mark m:marks)if(m.type.equals(t))return m;return null;}
    private void remove(String t){for(int i=marks.size()-1;i>=0;i--)if(marks.get(i).type.equals(t))marks.remove(i);}
    private int color(String t){return t.equals("cue")?Color.WHITE:t.equals("ball")?Color.rgb(255,80,105):Color.rgb(50,235,180);}
    private String label(String t){return t.equals("cue")?"B":t.equals("ball")?"O":"T";}
}
