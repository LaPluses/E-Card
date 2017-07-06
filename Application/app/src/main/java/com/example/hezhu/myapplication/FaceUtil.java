package com.example.hezhu.myapplication;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.Log;
import android.util.SparseArray;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;

public class FaceUtil {
    protected static int width = 224, height = 224;

    public static Bitmap transform(Bitmap original, Context context) {
        original = BitmapUtil.resize(original, 1200, 1200);
        FaceDetector detector = new FaceDetector.Builder(context)
                .setTrackingEnabled(false)
                .setMode(FaceDetector.ACCURATE_MODE)
                .build();
        if (!detector.isOperational()) {
            android.media.FaceDetector faceDetector = new android.media.FaceDetector(original.getWidth(), original.getHeight(), 1);
            android.media.FaceDetector.Face[] faces = new android.media.FaceDetector.Face[1];
            int realFaceNum = faceDetector.findFaces(original, faces);
            if (realFaceNum == 0) {
                return null;
            }
            android.media.FaceDetector.Face face = faces[0];
            if (face == null) {
                return null;
            }
            PointF pointF = new PointF();
            face.getMidPoint(pointF);
            float eyesDistance = face.eyesDistance();
            RectF targetRect = new RectF(pointF.x - eyesDistance * 1.75f, pointF.y - eyesDistance * 1.75f,
                    pointF.x + eyesDistance * 1.75f, pointF.y + eyesDistance * 1.75f);
            Rect rect = new Rect();
            targetRect.round(rect);
            Bitmap.Config config = original.getConfig() != null ? original.getConfig() : Bitmap.Config.RGB_565;
            Bitmap result = Bitmap.createBitmap(width, height, config);
            Canvas canvas = new Canvas(result);
            canvas.drawBitmap(original, rect, new Rect(0, 0, 224, 224), null);
            original.recycle();
            return result;
        }
        Log.d("facesize", original.getWidth() + " " + original.getHeight());
        Frame frame = new Frame.Builder().setBitmap(original).build();
        SparseArray<Face> faces = detector.detect(frame);
        detector.release();
        if (faces.size() > 0) {
            Face face = faces.valueAt(0);
            float x = face.getPosition().x + face.getWidth() / 2;
            float y = face.getPosition().y + face.getHeight() / 2;
            float radius = Math.max(face.getWidth(), face.getHeight()) / 2;
            RectF targetRect = new RectF(x - radius, y - radius, x + radius, y + radius);
            Rect rect = new Rect();
            targetRect.round(rect);
            Bitmap.Config config = original.getConfig() != null ? original.getConfig() : Bitmap.Config.RGB_565;
            Bitmap result = Bitmap.createBitmap(width, height, config);
            Canvas canvas = new Canvas(result);
            canvas.drawBitmap(original, rect, new Rect(0, 0, 224, 224), null);
            original.recycle();
            return result;
        } else {
            Log.d("face", "NOFACE");
            return null;
        }
    }
}
