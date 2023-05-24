package com.taewon.mygallag;


import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import androidx.annotation.NonNull;

import com.taewon.mygallag.sprites.AlienSprite;
import com.taewon.mygallag.sprites.Sprite;
import com.taewon.mygallag.sprites.StarshipSprite;

import java.util.ArrayList;
import java.util.Random;

public class SpaceInvadersView extends SurfaceView implements Runnable, SurfaceHolder.Callback {
    //SurfaceView 는 스레드를 이용해 강제로 화면에 그려주므로 View보다 빠르다. 애니메이션, 영상 처리에 이용
    //SurfaceHolder.Callback Surface의 변화감지를 위해 필요. 지금처럼 SurfaceView 와 거의 같이 사용

    private static int MAX_ENEMY_COUNT = 10;
    private Context context;
    private int characterId;
    private SurfaceHolder ourHolder;
    private Paint paint;
    public int screenW, screenH;
    private Rect src, dst;
    private ArrayList sprites = new ArrayList();
    private Sprite starship;
    private int score, currEnemyCount;
    private Thread gameThread = null;
    private volatile  boolean running;
    private Canvas canvas;
    int mapBitmapY = 0;

    public SpaceInvadersView(Context context, int characterId, int x , int y) {
        super(context);
        this.context = context;
        this.characterId = characterId;
        ourHolder = getHolder();
        paint = new Paint();
        screenW = x;
        screenH = y;
        src = new Rect();
        dst = new Rect();
        dst.set(0,0,screenW,screenH);
        startGame();
    }

    private void startGame() { //게임 초기화
        sprites.clear();
        initSprites();
        score = 0;
    }

    public void endGame() { //게임 종료시 발생 인텐트 이용하여 ResultActivity 로 전환
        Log.e("GameOver", "GameOver");
        Intent intent = new Intent(context, ResultActivity.class);
        intent.putExtra("score", score);
        context.startActivity(intent);
        gameThread.stop();
    }

    public void removeSprite(Sprite sprite) { sprites.remove(sprite); } //스프라이트 제거위한 메서드

    private void initSprites() { //게임 시작할때 받아온 스타쉽 정보를 토대로 스프라이트에 추가
        starship = new StarshipSprite(context, this, characterId, screenW / 2, screenH - 400, 1.5f);
        sprites.add(starship);
        spawnEnemy();
        spawnEnemy();
    }

    public void spawnEnemy() { // 랜덤한 위치에 적 스폰
        Random r = new Random();
        int x = r.nextInt(300)+100;
        int y = r.nextInt(300)+100;
        Sprite alien = new AlienSprite(context, this, R.drawable.ship_0002,100+x,100+y  );
        sprites.add(alien);
        currEnemyCount++;
    }

    public ArrayList getSprites() { return sprites; }

    public void resume() { // 게임 재개
        running = true;
        gameThread = new Thread(this);
        gameThread.start();
    }

    public StarshipSprite getPlayer() { return (StarshipSprite) starship;}

    public int getScore() { return score; }

    public void setScore(int score) { this.score = score;}

    public void setCurrEnemyCount(int currEnemyCount) { this.currEnemyCount = currEnemyCount;}

    public int getCurrEnemyCount() { return currEnemyCount; }

    public void pause() { //일시정지
        running = false;
        try {
            gameThread.join();
        }catch (InterruptedException e){

        }
    }

    public void run() {
        while (running) { //running 이 true 인 동안 다음 코드를 반복적으로 수행함
            Random r = new Random();
            boolean isEnemySpawn = r.nextInt(100)+1 < (getPlayer().speed +
                    (int) (getPlayer().getPowerLevel() / 2 ));

            if (isEnemySpawn && currEnemyCount < MAX_ENEMY_COUNT) spawnEnemy();
                // 적들의 숫자를 계산하여 최대값 보다 낮을경우 적들을 스폰함

            for (int i = 0; i < sprites.size(); i++) {
                Sprite sprite = (Sprite) sprites.get(i);
                sprite.move();
            }

            for(int p = 0; p < sprites.size(); p++) {
                for (int s = p+1; s < sprites.size(); s++) {
                    try {
                        Sprite me = (Sprite) sprites.get(p);
                        Sprite other = (Sprite) sprites.get(s);

                        if(me.checkCollision(other)) {
                            me.handleCollision(other);
                            other.handleCollision(me);
                        }
                    }catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            draw();

            try{
                Thread.sleep(10);
            }catch (Exception e){

            }
        }
    }

    public void draw() {
        if(ourHolder.getSurface().isValid()){
            canvas = ourHolder.lockCanvas();
            canvas.drawColor(Color.BLACK);
            mapBitmapY++;
            if(mapBitmapY<0) mapBitmapY = 0;
            paint.setColor(Color.BLUE);
            for(int i=0; i < sprites.size(); i++){
                Sprite sprite = (Sprite) sprites.get(i);
                sprite.draw(canvas, paint);
            }
            ourHolder.unlockCanvasAndPost(canvas);
        }
    }


    @Override
    public void surfaceCreated(@NonNull SurfaceHolder surfaceHolder) {
        startGame();
    }

    @Override
    public void surfaceChanged(@NonNull SurfaceHolder surfaceHolder, int i, int i1, int i2) {

    }

    @Override
    public void surfaceDestroyed(@NonNull SurfaceHolder surfaceHolder) {

    }




}
