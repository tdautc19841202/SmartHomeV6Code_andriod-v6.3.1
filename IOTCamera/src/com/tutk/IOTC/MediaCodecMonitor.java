/**
  * Generated by smali2java 1.0.0.558
  * Copyright (C) 2013 Hensence.com
  */

package com.tutk.IOTC;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.PointF;
import android.graphics.Rect;
import android.media.MediaCodec;
import android.media.MediaFormat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import com.decoder.util.DecH264;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
@SuppressLint("NewApi")
public class MediaCodecMonitor extends SurfaceView implements SurfaceHolder.Callback, IRegisterIOTCListener, IMonitor {
	private final static String TAG ="MediaCodecMonitor";
	private static final float DEFAULT_MAX_ZOOM_SCALE = 2.0f;
	private static final int DRAG = 0x1;
	public static int FFmpegDecoder_Height = 0x0;
	public static int FFmpegDecoder_Width = 0x0;
	private static final int FLING_MIN_DISTANCE = 0x64;
	private static final int FLING_MIN_VELOCITY = 0x0;
	private static final int LIVEVIEW_AVAILABE_TIME = 0xea60;
	private static final int NONE = 0x0;
	private static final int PTZ_DELAY = 0x5dc;
	private static final int PTZ_SPEED = 0x8;
	private static int UNAVAILABLE_HARDWARE_DECODE = 0x0;
	public static AVFrameQueue VideoFrameQueue = null;
	private volatile int  temp_sequence ;
	private static final int ZOOM = 0x2;
	public static int drawFPS;
	public static int keepFPS;
	private Camera mCamera;
	private static Zoom_Listener mClick_Listener;
	public static long mDecodeCount_temp;
	private GestureDetector mGestureDetector;
	private long mLastZoomTime;
	private static Surface mSurface;
	private int mVideoHeight;
	private int mVideoWidth;
	private int vBottom;
	private int vLeft;
	private int vRight;
	private int vTop;
	// made by 郭枫 接收最新的每帧图片 定义的这两张图片要注意不能出现内存溢出，注意查看，赋值的方式。
	// 大容量内存分配一定注意内存溢出的问题 用来备份从AvFrame帧中取出的Byte原始数据很重要的 要用来解码使用

	// 用来保存解码后最新的I帧。 大容量内存要注意溢出的bug 完全可以使用mlastFrame 不用定义这么多。不过现在已经使用了 就先使用者。
	private byte[] frmDataSnap = null;
	// 这函数绝对可以再优化的编写很差劲。
	// private int packectNum = 0;
	public boolean mEnableDither = false;
	private DecodeListener decodeListener = null;
	private Bitmap mLastFrameBitMap = null;
	private SoftDecodeingThread softDecodeingThread = null;
	private SurfaceHolder mSurHolder = null;
	private PointF mMidPoint = new PointF();
	private PointF mMidPointForCanvas = new PointF();
	static {
		UNAVAILABLE_HARDWARE_DECODE = 0x1388;
		FFmpegDecoder_Width = 0x0;
		FFmpegDecoder_Height = 0x0;
		VideoFrameQueue = new AVFrameQueue();
		drawFPS = 0x0;
		keepFPS = 0x0;
		mDecodeCount_temp = 0x0;
	}
	private Rect mRectCanvas = new Rect();
	private Rect mRectMonitor = new Rect();
	private int mPinchedMode = 0x0;
	private int mAVChannel = -0x1;
	private float mCurrentMaxScale = 0.0f;
	private float mOrigDist = 0.0f;
	private PointF mStartPoint = new PointF();
	private MediaCodecMonitor.ThreadRender mThreadRender;
	private ByteBuffer mLastFrame;
	private volatile long mDecodeCount = 0x0;
	private InputStream inputStream;
	private Object mBlock = new Object();
	private float mCurrentScale = 0.0f;
	private int mCurVideoHeight = 0x0;
	private int mCurVideoWidth = 0;
	private long clockLast = 0x0;
	private MonitorClickListener mMonitorClickListener;
	private MediaCodecListener mMediaCodecListener;
	 //private boolean click = false;
	private boolean mLiveViewLock = false;
	private boolean mStartRecv = false;
	private boolean WaitForDecoder = false;
	private boolean mIsSurfaceReady = false;
	private AVFrame avFrame = null;
	private String mMimeType = "video/avc";

	public  MediaCodecMonitor(Context context){
		super(context);
		mSurHolder = getHolder();
		mSurHolder.addCallback(this);
		VideoFrameQueue.setKeepFram(0x1e);
		setLongClickable(true);
	}
	public MediaCodecMonitor(Context context, AttributeSet attrs) {
		super(context, attrs);
		mSurHolder = getHolder();
		mSurHolder.addCallback(this);
		VideoFrameQueue.setKeepFram(0x1e);
		setLongClickable(true);
	}
	public void surfaceCreated(SurfaceHolder holder) {
		Log.i(TAG, "create surface view");
		mSurface = holder.getSurface();
		mIsSurfaceReady = true;
	}
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
		Log.i(TAG, "changed surface view");
		synchronized (this) {
			mRectMonitor.set(0, 0, width, height);
			mRectCanvas.set(0, 0, width, height);
			if (mCurVideoWidth == 0 || mCurVideoHeight == 0) {
				if (height < width) { // landscape layout
					Log.i(TAG, "landscape");
					mRectCanvas.right = 4 * height / 3;
					mRectCanvas.offset((width - mRectCanvas.right) / 2, 0);
				} else { // portrait layout
					Log.i(TAG, "portrait");
					mRectCanvas.bottom = 3 * width / 4;
					Log.i(TAG, "mRectCanvas.bottom("+mRectCanvas.bottom+")");
					mRectCanvas.offset(0, (height - mRectCanvas.bottom) / 2);
				}
			} else {
				Log.i(TAG, "Portrait layout");
				double ratio = (double) mCurVideoWidth / mCurVideoHeight;
				mRectCanvas.bottom = (int) (mRectMonitor.right / ratio);
			}
			vLeft = mRectCanvas.left;
			vTop = mRectCanvas.top;
			vRight = mRectCanvas.right;
			vBottom = mRectCanvas.bottom;
			mCurrentScale = 1.0f;
			parseMidPoint(mMidPoint, vLeft, vTop, vRight, vBottom);
			parseMidPoint(mMidPointForCanvas, vLeft, vTop, vRight, vBottom);
		}
	}
	public void surfaceDestroyed(SurfaceHolder holder) {
		Log.i(TAG, "destroyed surface view");
	}
	private void parseMidPoint(PointF point, float left, float top, float right, float bottom) {
		point.set((left + right) / 2, (top + bottom) / 2);
	}
	public int getVideoWidth() {
		return mVideoWidth;
	}
	public int getVideoHeight() {
		return mVideoHeight;
	}
	public void setDecodeListener(DecodeListener decodeListener) {
		this.decodeListener = decodeListener;
	}
	public Bitmap getBitmapSnap() {
		Bitmap result = null;
		if(frmDataSnap != null&&mLastFrameBitMap == null){
			byte[] bufOut = new byte[1920 * 1080 * 2];
			ByteBuffer bytBuffer = null;
			int[] framePara = new int[4];
			int[] out_width = new int[1];
			int[] out_height = new int[1];
			DecH264.InitDecoder();		
			DecH264.DecoderNal(frmDataSnap, frmDataSnap.length, framePara, bufOut, false);
			out_width[0] = framePara[2];
			out_height[0] = framePara[3];
			result = Bitmap.createBitmap(out_width[0], out_height[0], android.graphics.Bitmap.Config.RGB_565);
			bytBuffer = ByteBuffer.wrap(bufOut);
			result.copyPixelsFromBuffer(bytBuffer);
			DecH264.UninitDecoder();
		} 
		if(frmDataSnap==null&&mLastFrameBitMap!=null){
			result = mLastFrameBitMap;
		}
		return result;
	}
	public void attachCamera(Camera camera, int avChannel) {
		mCamera = camera;
		mCamera.registerIOTCListener(this);
		mAVChannel = avChannel;
	}
	public void deattachCamera() {
		mAVChannel = -0x1;
		mLiveViewLock = false;
		if (mCamera != null) {
			mCamera.unregisterIOTCListener(this);
		}
		if (mThreadRender != null && mThreadRender.mIsRunning) {
			mThreadRender.stopThread();
			mThreadRender = null;
			Log.e(TAG, "hard decode thread stop");
			cleanFrameQueue();
			frmDataSnap = null;
		}
		if(softDecodeingThread!=null&&softDecodeingThread.isRunning){
			try {
				softDecodeingThread.stopThread();
			} catch (Exception e) {
				e.printStackTrace();
			}
			softDecodeingThread = null;
			mLastFrameBitMap = null;
			Log.e(TAG, "soft decode thread stop");
		}
		mIsSurfaceReady = false;
		mStartRecv = false;
	}
  public void setSurfaceReady(){
	    	mIsSurfaceReady = true;
	    }
  public boolean isThreadRun(){
  	if(mThreadRender != null&&mThreadRender.mIsRunning){
  		return true;
  	}
  	return false;
  }
  
  public void surfaceDestroyed(){
  	if (mThreadRender != null && mThreadRender.mIsRunning) {
			mThreadRender.stopThread();
			mThreadRender = null;
			cleanFrameQueue();
			frmDataSnap = null;
			mIsSurfaceReady = false;
			mStartRecv = false;
		}
  }
	 
	public void setReceiveotListener(IReceiveSnapshotListener listener) {
	}

	public long getDecodedCount() {
		mDecodeCount_temp = mDecodeCount;
		return mDecodeCount;
	}

	public void setInputStream(InputStream is) {
		inputStream = is;
	}

	public boolean onDown(MotionEvent e) {
		return false;
	}
	public void onLongPress(MotionEvent e) {
	}

	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
		return true;
	}

	public void onShowPress(MotionEvent e) {
	}

	public boolean onSingleTapUp(MotionEvent e) {
		return false;
	}
  
	private class SoftDecodeingThread extends Thread{
	   private boolean isRunning = true;
	   private Object mWaitObjectForStopThread = new Object();
	   private int waitTime = 35;
	   public void stopThread()throws Exception{
		   this.isRunning = false;
		   mWaitObjectForStopThread.notify();
	   }
	   @Override
	public void run() {
		   Canvas videoCanvas = null ;
		   while(isRunning){
			   if(mLastFrameBitMap!=null&&!mLastFrameBitMap.isRecycled()){
				   try {
						videoCanvas = mSurHolder.lockCanvas();
						if(videoCanvas != null){
							videoCanvas.drawBitmap(mLastFrameBitMap, null, mRectCanvas, null);
						}
				} catch (Exception e) {
					Log.e(TAG, "===soft decode error===");
					e.printStackTrace();
					if(decodeListener!=null){
						decodeListener.decodeAbnormalCondition(mCamera, mAVChannel, Camera.HARD_DECODE,"SoftDecodeingThread");
					}
				}finally{
					if (videoCanvas != null)
						mSurHolder.unlockCanvasAndPost(videoCanvas);
					     videoCanvas = null ;
				}
			  }
			   try {
					synchronized (mWaitObjectForStopThread) {
						mWaitObjectForStopThread.wait(waitTime);
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
					Log.e(TAG, "===mWaitObjectForStopThreadr===");
					if(decodeListener!=null){
						decodeListener.decodeAbnormalCondition(mCamera, mAVChannel, Camera.HARD_DECODE,"SoftDecodeingThread Wait");
					}
				}
		   }
    	}
   }
	@SuppressLint("NewApi")
	private class ThreadRender extends Thread {
		MediaCodec decoder;
		private Surface surface;
		private int VIDEO_WIDTH_720 = 0x2d0;
		private int VIDEO_HEIGHT_720 = 0x240;
		long firstTimeStampFromDevice = 0x0;
		long firstTimeStampFromLocal = 0x0;
		long sleepTime = 0x0;
		long lastFrameTimeStamp = 0x0;
		long delayTime = 0x0;
		long unavailable = 0x0;
		long mUnavailableTime = 0x0;
		long mLastUnavailableTime = 0x0;
		boolean mIsRunning = false;
		boolean mKeepDropFrame = false;
		boolean mHadDropFrame = false;
		int decodeErrorTimes= 0;
		int avChannel;
		public ThreadRender(MediaCodecMonitor p1, Surface surface,int avChannel) {
			this.surface = surface;
			this.avChannel = avChannel;
			mIsRunning = true;
		}
		public void stopThread() {
			mIsRunning = false;
//			clearDecodeError();
		}
//		private int addDecodeError(){
//			return decodeErrorTimes++;
//		}
//		private void clearDecodeError(){
//			decodeErrorTimes = 0;
//		}
		public void run() {
			Log.i(TAG, "create Video Format");
			MediaFormat format = MediaFormat.createVideoFormat(mMimeType, VIDEO_WIDTH_720, VIDEO_HEIGHT_720);
			try {
				decoder = MediaCodec.createDecoderByType(mMimeType);
			} catch (Exception e1) {
				if(decodeListener!=null){
					Log.e(TAG, "Can't create MediaCodec.", e1);
					decodeListener.decodeAbnormalCondition(mCamera, mAVChannel, Camera.SOFT_DECODE,"Create Hard HDecoder");
				}
				return;
			}
			decoder.configure(format, surface, null, 0x0);
			decoder.start();
			//应该是用于延时的 ，延时的其他作用，竟然不知怎么用 很遗憾。
			//int TIMEOUT_USEC = 0x2710;
			ByteBuffer[] inputBuffers = decoder.getInputBuffers();
			// made by guofeng
			ByteBuffer[] outputBuffers = decoder.getOutputBuffers();
			// 解码出来的信息要全局使用，分辨率等信息
			MediaCodec.BufferInfo info = new MediaCodec.BufferInfo();
			boolean bSkipThisRound;
			if ((unavailable > (long) MediaCodecMonitor.UNAVAILABLE_HARDWARE_DECODE) && (mMediaCodecListener != null)) {
				Log.e(TAG, "Can not use media codec!!");
				mMediaCodecListener.Unavailable();
				unavailable = 0x0;
				mIsRunning = false;
			}
			while (mIsRunning) {
				if (MediaCodecMonitor.VideoFrameQueue.getCount() > 0) {
					if (!WaitForDecoder) {
						avFrame = MediaCodecMonitor.VideoFrameQueue.removeHead();
					}
					if ((avFrame != null)) {
						if (avFrame != null) {
							if ((delayTime > 0x7d0) || (mKeepDropFrame)) {
								mKeepDropFrame = true;
								long skipTime = (long) avFrame.getTimeStamp() - lastFrameTimeStamp;
								Log.e(TAG, avFrame.isIFrame() ? "I"
										: ("case 1. low decode performance, drop ") + "P" + " frame, skip time: ");
								String locallong1 = avFrame.isIFrame() ? "I"
										: ("case 1. low decode performance, drop " + "P" + " frame, skip time: "
												+ ((long) avFrame.getTimeStamp() - lastFrameTimeStamp)
												+ ", total skip: " + skipTime).toString();
								lastFrameTimeStamp = (long) avFrame.getTimeStamp();
								delayTime = (delayTime - skipTime);
								if (delayTime <= 0x0) {
									mKeepDropFrame = false;
									mHadDropFrame = true;
								}
							}
							if (mHadDropFrame) {
								if (avFrame.isIFrame()) {
									mHadDropFrame = false;
								}
							}
						}
						try {
							if ((mLastFrame == null) && (avFrame != null)) {
								int inputBufIndex = decoder.dequeueInputBuffer(0x0);
								if (inputBufIndex >= 0) {
									unavailable = 0x0;
									mLastUnavailableTime = 0x0;
									WaitForDecoder = false;
									ByteBuffer inputBuf = inputBuffers[inputBufIndex];
									inputBuf.clear();
									inputBuf.put(avFrame.frmData);
									decoder.queueInputBuffer(inputBufIndex, 0x0, avFrame.frmData.length, 0x0, 0x0);
									if ((avFrame != null) && (firstTimeStampFromDevice != 0)
											&& (firstTimeStampFromLocal != 0)) {
										long currentTime = System.currentTimeMillis();
										sleepTime = ((firstTimeStampFromLocal
												+ ((long) avFrame.getTimeStamp() - firstTimeStampFromDevice))
												- currentTime);
										delayTime = (sleepTime * 0xffffffff);
										if (sleepTime >= 0x0) {
											if (((long) avFrame.getTimeStamp() - lastFrameTimeStamp) > 0x3e8) {
												firstTimeStampFromDevice = (long) avFrame.getTimeStamp();
												firstTimeStampFromLocal = currentTime;
												Log.i(TAG, "RESET base timestamp");
												if (sleepTime > 0x3e8) {
													sleepTime = 0x21;
												}
											}
											if (sleepTime > 0x3e8) {
												sleepTime = 0x3e8;
											}
											try {
												sleep(sleepTime);
											} catch (Exception localException2) {
											}
										}
										lastFrameTimeStamp = (long) avFrame.getTimeStamp();
									}
									if ((firstTimeStampFromDevice == 0) || (firstTimeStampFromLocal == 0x0)) {
										firstTimeStampFromDevice = (long) avFrame.getTimeStamp();
										firstTimeStampFromLocal = System.currentTimeMillis();
									}
								} else {
									Log.e(TAG, unavailable + "");
									WaitForDecoder = true;
									mUnavailableTime = System.currentTimeMillis();
									unavailable = (mUnavailableTime - mLastUnavailableTime);
									if (unavailable > 0x2710) {
										unavailable = 0x0;
										mLastUnavailableTime = mUnavailableTime;
									}
								}
							} else if (mLastFrame != null) {
								int inputBufIndex = decoder.dequeueInputBuffer(10000);
								if ((inputBufIndex >= 0)) {
										ByteBuffer inputBuf = inputBuffers[inputBufIndex];
										inputBuf.clear();
										inputBuf.put(avFrame.frmData);
										decoder.queueInputBuffer(inputBufIndex, 0x0, avFrame.frmData.length, 0x0, 0x0);
										WaitForDecoder = false;
								}
							}
							
						} catch (Exception e) {
							e.printStackTrace();
							Log.e(TAG, "dequeueInputBuffer err continue");
//							mIsRunning = false;
							WaitForDecoder = true;
							mUnavailableTime = System.currentTimeMillis();
							unavailable = (mUnavailableTime - mLastUnavailableTime);
							if (unavailable > 0x2710) {
								unavailable = 0x0;
								mLastUnavailableTime = mUnavailableTime;
							}
						}
						try { // 10000
							int outputBufferIndex = decoder.dequeueOutputBuffer(info, 100000);
							if (outputBufferIndex >= 0) {
								long clockNow = System.currentTimeMillis();
								if ((clockNow - clockLast) >= 0x3e8) {
									clockLast = clockNow;
									MediaCodecMonitor.keepFPS = MediaCodecMonitor.drawFPS;
									MediaCodecMonitor.drawFPS = 0x0;
								}
								decoder.releaseOutputBuffer(outputBufferIndex, true);
								MediaCodecMonitor.drawFPS = (MediaCodecMonitor.drawFPS + 0x1);
								mDecodeCount = (mDecodeCount + 0x1);
								MediaCodecMonitor.mDecodeCount_temp = mDecodeCount;
								if (mLiveViewLock) {
									Log.e(TAG, "60 sec count backward begin.");
									mLiveViewLock = false;
								}
							} else if(outputBufferIndex == -0x2) {
								Log.e(TAG, "===outputBufferIndex("+outputBufferIndex+")===");
									setDecodeListener(null);
							} else if(outputBufferIndex == -0x3){
								Log.e(TAG, "===outputBufferIndex("+outputBufferIndex+")===");
								outputBuffers = decoder.getOutputBuffers();
							} else if(outputBufferIndex == -0x1){
								Log.e(TAG, "===outputBufferIndex("+outputBufferIndex+")===");
//								if(addDecodeError() == 50){
//									clearDecodeError();
//									if(decodeListener!=null){
//										decodeListener.decodeAbnormalCondition(mCamera, mAVChannel,  Camera.SOFT_DECODE," -0x1");
//										mIsRunning = false;
//									}
//								}
							}
						} catch (IllegalStateException e){
							Log.e(TAG, "", e);
						} catch (Exception localMediaCodec4) {
							Log.e(TAG, "===localMediaCodec4===", localMediaCodec4);
//							if(decodeListener!=null){
//								decodeListener.decodeAbnormalCondition(mCamera, mAVChannel,  Camera.SOFT_DECODE,"Hard Output Data");
//							}
//							mIsRunning = false;
						}
					}
				}
				if(avChannel == 1){
				try {
					Thread.sleep(50);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			}
			synchronized (mBlock) {
				mBlock.notifyAll();
				Log.e(TAG, "-------===ThreadRender exit===1");
			}
			try{
				if (decoder != null) {
					decoder.stop();
					decoder.release();
					decoder = null;
					Log.e(TAG, "----------===ThreadRender exit_===2");
				}
			}catch (Exception ex){
				Log.i(TAG,"decoder exception");
				return;
			}
		}
	}
	public void receiveFrameData(Camera camera, int avChannel, Bitmap bmp) {
		if (mAVChannel == avChannel) {
			if(softDecodeingThread == null){
				softDecodeingThread = new SoftDecodeingThread();
				softDecodeingThread.start();
			}
			mLastFrameBitMap = bmp;
			if (bmp.getWidth() > 0 && bmp.getHeight() > 0 &&
				(bmp.getWidth() != mCurVideoWidth || bmp.getHeight() != mCurVideoHeight)) {
				mCurVideoWidth = bmp.getWidth();
				mCurVideoHeight = bmp.getHeight();
				mRectCanvas.set(0, 0, mRectMonitor.right, mRectMonitor.bottom);
				vLeft = mRectCanvas.left;
				vTop = mRectCanvas.top;
				vRight = mRectCanvas.right;
				vBottom = mRectCanvas.bottom;
				mCurrentScale = 1.0f;
				parseMidPoint(mMidPoint, vLeft, vTop, vRight, vBottom);
				parseMidPoint(mMidPointForCanvas, vLeft, vTop, vRight, vBottom);
			}
		}
	}
	public void receiveFrameInfo(Camera camera, int sessionChannel, long bitRate, int frameRate, int onlineNm,
			int frameCount, int incompleteFrameCount) {
	}
	public void receiveChannelInfo(Camera camera, int sessionChannel, int resultCode) {
	}
	public void receiveSessionInfo(Camera camera, int resultCode) {
	}
	public void receiveIOCtrlData(Camera camera, int sessionChannel, int avIOCtrlMsgType, byte[] data) {
	}
	public void receiveFrameDataForMediaCodec(Camera camera, int avChannel, byte[] buf, int length, int pFrmNo,
			byte[] pFrmInfoBuf, boolean isIframe, int codecId) {
		if (codecId == 0x4f) {
			mMediaCodecListener.Unavailable();
			return;
		}
		// 把初始化和渲染解码器放在这个地方，
		if ((mThreadRender == null) && (mIsSurfaceReady)) {
			mMimeType = "video/avc";
			// 这个地方应该有判断来确定是用哪个编解码器 因为我们这里已经确定用在安霸S2L上面所以不在判断
			if (codecId == 0x4c) {
				mMimeType = "video/mp4v-es";
			}
			mThreadRender = new MediaCodecMonitor.ThreadRender(this, mSurface,avChannel);
			mThreadRender.setPriority(Thread.MAX_PRIORITY);
			mThreadRender.start();
			// 新建截图功能的线程 这个线程要随时开关闭。
		}
		try {	
			mLastFrame = ByteBuffer.allocateDirect(length);
			mLastFrame.put(buf, 0x0, length);
			AVFrame mFrame = new AVFrame((long) pFrmNo, (byte) 0, pFrmInfoBuf, mLastFrame.array(), length);			
			if (mFrame.isIFrame()) {
				mStartRecv = true;
				// 截图 使用 计算当前的sps pps 也可以使用这个帧的 必须有 不能改变，不能改变，不能改变的。
				frmDataSnap = mFrame.frmData;
				temp_sequence = 1;
			}
			if(mStartRecv){
				VideoFrameQueue.addLast(mFrame);	//添加缓冲队列
			} else {
				mLastFrame.clear();
			}
		} catch (Exception localException1) {
			Log.e(TAG,"===receiveFrameDataForMediaCodec===");
			if(decodeListener!=null){
				decodeListener.decodeAbnormalCondition(mCamera, avChannel, Camera.SOFT_DECODE,"Hard Build Data");
			}
		}
	}
	static ByteBuffer readToByteBuffer(InputStream inStream) throws IOException {
		byte abyte0[] = new byte[0x20000];
		ByteArrayOutputStream bytearrayoutputstream = new ByteArrayOutputStream(0x20000);
		do {
			int i = inStream.read(abyte0);
			if (i == -1) {
				byte abyte1[] = bytearrayoutputstream.toByteArray();
				ByteBuffer bytebuffer = ByteBuffer.allocateDirect(abyte1.length);
				bytebuffer.put(abyte1);
				return bytebuffer;
			}
			bytearrayoutputstream.write(abyte0, 0, i);
		} while (true);
	}
	public void enableDither(boolean enable) {
		mEnableDither = enable;
	}

	public void setMaxZoom(float value) {
		mCurrentMaxScale = value;
	}

	public void SetClickListener(Zoom_Listener Listener) {
		mClick_Listener = Listener;
	}
	public void cleanFrameQueue() {
		VideoFrameQueue.removeAll();
	}
	public void SetOnMonitorClickListener(MonitorClickListener MonitorClickListener) {
		mMonitorClickListener = MonitorClickListener;
	}
	public void OnClick_Zoom() {
	}
	public void setMediaCodecListener(MediaCodecListener MediaCodecListener) {
		mMediaCodecListener = MediaCodecListener;
	}
	public void setMonitorBackgroundColor(int color) {
	}
	@Override
	public void snapshot() throws IOException {
	}
	@Override
	public void HorizontalScrollTouch(View paramView,
			MotionEvent paramMotionEvent) {
		
	}
	@Override
	public void resetCodec() {
		
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		return false;
	}
}
