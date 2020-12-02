package fatcats.top.qrcodedemo;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.media.ThumbnailUtils;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.MultiAutoCompleteTextView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

public class LogoQRCode extends AppCompatActivity {

    ImageView logoImg;
    //二维码宽度
    public static final int QR_CODE_WIDTH = 500;
    //logo的尺寸不能高于二维码的20%.大于可能会导致二维码失效
    public static final int LOGO_WIDTH_MAX = QR_CODE_WIDTH / 5;
    //logo的尺寸不能小于二维码的10%，否则不搭
    public static final int LOGO_WIDTH_MIN = QR_CODE_WIDTH / 10;
    //定义黑色
    private static final int BLACK = 0xFF000000;
    //定义白色
    private static final int WHITE = 0xFFFFFFFF;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_q_r_code_demo);
        logoImg = findViewById(R.id.logo_img);

        Bitmap bitmap = createBitmap(((BitmapDrawable) getDrawable(R.drawable.bg)).getBitmap()
                , ((BitmapDrawable) getDrawable(R.drawable.logo)).getBitmap());
        try {
            Bitmap bitmapQR = createQRBitmap(bitmap, "扫描二维码");
            Glide.with(LogoQRCode.this).load(bitmapQR).into(logoImg);
        } catch (WriterException e) {
            e.printStackTrace();
        }
    }

    /*
            生成二维码
     */

    public Bitmap createQRBitmap(Bitmap logoBitmap , String content) throws WriterException {

        int logoWidth = logoBitmap.getWidth();
        int logoHeight = logoBitmap.getHeight();
        //规整化图片
        int logoHaleWidth = logoWidth >= QR_CODE_WIDTH ? LOGO_WIDTH_MIN : LOGO_WIDTH_MAX;
        int logoHaleHeight = logoHeight >= QR_CODE_WIDTH ? LOGO_WIDTH_MIN : LOGO_WIDTH_MAX;
        // 将logo图片按martix设置的信息缩放
        Matrix matrix = new Matrix();
        float sx = (float) logoHaleWidth / logoWidth;
        float sy = (float) logoHaleHeight / logoHeight;
        matrix.setScale(sx,sy);
        //重新绘制Bitmap
        Bitmap matrixLogoBitmap = Bitmap.createBitmap(logoBitmap, 0, 0, logoWidth, logoHeight, matrix, false);

        int mtLogoWidth = matrixLogoBitmap.getWidth();
        int mtLogoHidth = matrixLogoBitmap.getHeight();

        Map<EncodeHintType,Object> hintTypeStringMap = new HashMap<>();
        hintTypeStringMap.put(EncodeHintType.MARGIN,2);//外边距
        hintTypeStringMap.put(EncodeHintType.CHARACTER_SET,"utf8");
        hintTypeStringMap.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);//设置最高错误级别
        hintTypeStringMap.put(EncodeHintType.MAX_SIZE,LOGO_WIDTH_MAX);
        hintTypeStringMap.put(EncodeHintType.MIN_SIZE,LOGO_WIDTH_MIN);


        BitMatrix bitMatrix = new MultiFormatWriter().encode(content, BarcodeFormat.QR_CODE,QR_CODE_WIDTH
                ,QR_CODE_WIDTH,hintTypeStringMap);

        //绘制二维码数组
        int[] arr = new int[bitMatrix.getWidth() * bitMatrix.getHeight()];
            /*

                    二维码位置
                    Left: 屏幕宽度的一半 - 二维码宽度的一半
                    Right: 屏幕宽度的一半 + 二维码宽度的一半
                    logo长度： Right - Left = logoSize

             */
        for (int i = 0; i < bitMatrix.getHeight(); i++) {
            for (int j = 0; j < bitMatrix.getWidth(); j++) {
                /*
                            当坐标像素点恰好处于logo位置时，绘制logo  详情看图解
                 */
                if( j > bitMatrix.getWidth() / 2 - mtLogoWidth / 2 && j < bitMatrix.getWidth() / 2 + mtLogoWidth / 2
                        &&  i > bitMatrix.getHeight() / 2 - mtLogoHidth / 2 && i < bitMatrix.getHeight() / 2 + mtLogoHidth / 2){
                    arr[i * bitMatrix.getWidth() + j] = matrixLogoBitmap.getPixel( j - bitMatrix.getWidth() / 2 +mtLogoWidth / 2
                            ,i - bitMatrix.getHeight()/ 2 + mtLogoHidth / 2);
                }else{
                    arr[i * bitMatrix.getWidth() + j] = bitMatrix.get(i,j)? BLACK : WHITE;
                }
            }
        }

        Bitmap bitmap = Bitmap.createBitmap(arr,bitMatrix.getWidth(), bitMatrix.getHeight(), Bitmap.Config.ARGB_8888);

        return bitmap;
    }

    /*
         创建Logo白底图片
     */
    public Bitmap createBitmap(Bitmap bgBitmap , Bitmap logoBitmap){

        int bgWidth = bgBitmap.getWidth();
        int bgHeight = bgBitmap.getHeight();
        /*
            ThumbnailUtils 压缩logo为背景的 1/2
         */
        logoBitmap = ThumbnailUtils.extractThumbnail(logoBitmap, bgWidth / 2,
                bgHeight / 2, ThumbnailUtils.OPTIONS_RECYCLE_INPUT);

        Bitmap canvasBitmap = Bitmap.createBitmap(bgWidth, bgHeight, Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(canvasBitmap);
        //合成图片
        canvas.drawBitmap(bgBitmap,0,0,null);
        /*
                图片合成
         */
        canvas.drawBitmap(logoBitmap,30,30,null );
        canvas.save(); //保存
        canvas.restore();
        if(canvasBitmap.isRecycled()){
            canvasBitmap.recycle();
        }
        return canvasBitmap;
    }
}