package cc.wulian.smarthomev6.support.tools.zxing.utils;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.google.zxing.Result;

import cc.wulian.smarthomev6.R;
import cc.wulian.smarthomev6.support.tools.zxing.activity.CaptureActivity;
import cc.wulian.smarthomev6.support.tools.zxing.activity.ICaptureActivity;
import cc.wulian.smarthomev6.support.tools.zxing.camera.CameraManager;
import cc.wulian.smarthomev6.support.tools.zxing.decode.DecodeThread;


/**
 * 扫码
 */
public class CaptureActivityHandler extends Handler {

    private final ICaptureActivity activity;
    private final DecodeThread decodeThread;
    private final CameraManager cameraManager;
    private State state;

    public CaptureActivityHandler(ICaptureActivity activity, CameraManager cameraManager, int decodeMode) {
        this.activity = activity;
        decodeThread = new DecodeThread(activity, decodeMode);
        decodeThread.start();
        state = State.SUCCESS;

        // Start ourselves capturing previews and decoding.
        this.cameraManager = cameraManager;
        cameraManager.startPreview();
        restartPreviewAndDecode();
    }

    @Override
    public void handleMessage(Message message) {
        if (message.what == R.id.restart_preview) {
            restartPreviewAndDecode();

        } else if (message.what == R.id.decode_succeeded) {
            state = State.SUCCESS;
            Bundle bundle = message.getData();

            activity.handleDecode((Result) message.obj, bundle);

        } else if (message.what == R.id.decode_failed) {// We're decoding as fast as possible, so when one
            // decode fails,
            // start another.
            state = State.PREVIEW;
            cameraManager.requestPreviewFrame(decodeThread.getHandler(), R.id.decode);

        } else if (message.what == R.id.return_scan_result) {
            activity.setResult(Activity.RESULT_OK, (Intent) message.obj);
            activity.finish();

        }
    }

    public void quitSynchronously() {
        state = State.DONE;
        cameraManager.stopPreview();
        Message quit = Message.obtain(decodeThread.getHandler(), R.id.quit);
        quit.sendToTarget();
        try {
            // Wait at most half a second; should be enough time, and onPause()
            // will timeout quickly
            decodeThread.join(500L);
        } catch (InterruptedException e) {
            // continue
        }

        // Be absolutely sure we don't send any queued up messages
        removeMessages(R.id.decode_succeeded);
        removeMessages(R.id.decode_failed);
    }

    private void restartPreviewAndDecode() {
        if (state == State.SUCCESS) {
            state = State.PREVIEW;
            cameraManager.requestPreviewFrame(decodeThread.getHandler(), R.id.decode);
        }
    }

    private enum State {
        PREVIEW, SUCCESS, DONE
    }

}
