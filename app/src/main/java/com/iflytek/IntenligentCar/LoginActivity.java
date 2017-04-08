package com.iflytek.IntenligentCar;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.Surface;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;
import android.widget.Toast;
import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.SpeakerVerifier;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechListener;
import com.iflytek.cloud.VerifierListener;
import com.iflytek.cloud.VerifierResult;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
/**
 * Created by m1320 on 2016/9/6.
 */
public class LoginActivity extends Activity implements OnClickListener {
	private static final String TAG = LoginActivity.class.getSimpleName();
	/***
	 * 自动补全用户标识
	 */
	private AutoCompleteTextView actv;
	private List<String> auto_items;
	private SharedPreferences preferences;

	private static final int PWD_TYPE_TEXT = 1;
	private static final int PWD_TYPE_NUM = 3;
	// 当前声纹密码类型，1、2、3分别为文本、自由说和数字密码
	private int mPwdType = PWD_TYPE_TEXT;
	// 声纹识别对象
	private SpeakerVerifier mVerifier;
	// 声纹AuthId，用户在云平台的身份标识，也是声纹模型的标识
	// 请使用英文字母或者字母和数字的组合，勿使用中文字符
	private String mAuthId = "";
	// 文本声纹密码
	private String mTextPwd = "";
	// 数字声纹密码
	private String mNumPwd = "";
	// 数字声纹密码段，默认有5段
	private String[] mNumPwdSegs;
	private TextView mResultEditText;
	private TextView mAuthIdTextView;
	private RadioGroup mPwdTypeGroup;
	private TextView mShowPwdTextView;
	private TextView mShowMsgTextView;
	private TextView mShowRegFbkTextView;
	private TextView mRecordTimeTextView;
	private AlertDialog mTextPwdSelectDialog;
	private Toast mToast;
	private TextInputLayout textInputLayout;
	private Button btn_register;
	private LinearLayout linearLayout;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_login);
		/***
		 * 设置状态栏颜色
		 */
		linearLayout= (LinearLayout) findViewById(R.id.loginlinearlayout);
		initUi();
		// 将上个页面输入的用户名作为AuthId
		mAuthId = getIntent().getStringExtra("uname");
		mAuthIdTextView.setText(mAuthId);
		mVerifier = SpeakerVerifier.createVerifier(LoginActivity.this, new InitListener() {

			@Override
			public void onInit(int errorCode) {
				if (ErrorCode.SUCCESS == errorCode) {
					showTip("引擎初始化成功");
				} else {
					showTip("引擎初始化失败，错误码：" + errorCode);
				}
			}
		});
	}
	@SuppressLint("ShowToast")
	private void initUi() {
		btn_register= (Button) findViewById(R.id.register);
		btn_register.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				Intent intent=new Intent(LoginActivity.this,RegisterActivity.class);
				startActivity(intent);
			}
		});
		textInputLayout= (TextInputLayout) findViewById(R.id.usersign);
		textInputLayout.setHint("请输入用户标识");


		/***
		 * 自动补全用户标识
		 */
		auto_items= new ArrayList<String>();
		preferences=getSharedPreferences("inteligentcar",MODE_PRIVATE);
		Map<String, ?> allContent = preferences.getAll();
		//注意遍历map的方法
		for(Map.Entry<String, ?>  entry : allContent.entrySet()){
			String ite=preferences.getString(entry.getKey(),"没有历史纪录...");
			auto_items.add(ite);
		}
		actv= (AutoCompleteTextView) textInputLayout.getEditText();
		actv.setThreshold(1);
		ArrayAdapter<String> autoItemsAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_checked,auto_items);
		actv.setAdapter (autoItemsAdapter);
		actv= (AutoCompleteTextView) textInputLayout.getEditText();
		actv.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

			}
			@Override
			public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
				String uname = actv.getText().toString();
				if (TextUtils.isEmpty(uname)) {
					textInputLayout.setErrorEnabled(true);
					textInputLayout.setError("用户名不能为空");
					useridAnimation();
					return;
				} else {
					Pattern p = Pattern.compile("[\u4e00-\u9fa5]");
					Matcher m = p.matcher(uname);
					if (m.find()) {
						textInputLayout.setErrorEnabled(true);
						textInputLayout.setError("不支持中文字符");
						useridAnimation();
						return;
					} else if (uname.contains(" ")) {
						textInputLayout.setErrorEnabled(true);
						textInputLayout.setError("不能包含空格");
						useridAnimation();
						return;
					} else if (!uname.matches("^[a-zA-Z][a-zA-Z0-9_]{5,17}")) {
						textInputLayout.setErrorEnabled(true);
						textInputLayout.setError("6-18个字母、数字或下划线的组合，以字母开头");
						useridAnimation();
						return;
					}
					else{
						textInputLayout.setErrorEnabled(false);
					}
				}
				mAuthId = uname;
				mAuthIdTextView.setText(mAuthId);
			}

			@Override
			public void afterTextChanged(Editable editable) {

			}
		});
		mResultEditText = (TextView) findViewById(R.id.text_result);
		mAuthIdTextView = (TextView) findViewById(R.id.txt_authorid);
		mShowPwdTextView = (TextView) findViewById(R.id.showPwd);
		mShowMsgTextView = (TextView) findViewById(R.id.showMsg);
		mShowRegFbkTextView = (TextView) findViewById(R.id.showRegFbk);
		mRecordTimeTextView = (TextView) findViewById(R.id.recordTime);
		findViewById(R.id.isv_verify).setOnClickListener(LoginActivity.this);
		findViewById(R.id.isv_stop_record).setOnClickListener(LoginActivity.this);
		findViewById(R.id.isv_cancel).setOnClickListener(LoginActivity.this);
		findViewById(R.id.isv_getpassword).setOnClickListener(LoginActivity.this);
		// 密码选择RadioGroup初始化
		mPwdTypeGroup = (RadioGroup) findViewById(R.id.radioGroup);
		mPwdTypeGroup.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				initTextView();
				switch (checkedId) {
					case R.id.radioText:
						mPwdType = PWD_TYPE_TEXT;
						break;
					case R.id.radioNumber:
						mPwdType = PWD_TYPE_NUM;
						break;
					default:
						break;
				}
			}
		});
		mToast = Toast.makeText(LoginActivity.this, "", Toast.LENGTH_SHORT);
		mToast.setGravity(Gravity.BOTTOM|Gravity.CENTER_HORIZONTAL, 0, 0);
	}

	private void useridAnimation() {
		CustomAnimation ca=new CustomAnimation();
		ca.setDuration(1000);
		actv.startAnimation(ca);
	}

	/**
	 * 初始化TextView和密码文本
	 */
	private void initTextView(){
		mTextPwd = null;
		mNumPwd = null;
		mResultEditText.setText("");
		mShowPwdTextView.setText("");
		mShowMsgTextView.setText("");
		mShowRegFbkTextView.setText("");
		mRecordTimeTextView.setText("");
	}

	/**
	 * 设置radio的状态
	 */
	private void setRadioClickable(boolean clickable){
		// 设置RaioGroup状态为非按下状态
		mPwdTypeGroup.setPressed(false);
		findViewById(R.id.radioText).setClickable(clickable);
		findViewById(R.id.radioNumber).setClickable(clickable);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.isv_getpassword:
				// 获取密码之前先终止之前的注册或验证过程
				mVerifier.cancel();
				initTextView();
				setRadioClickable(false);
				if(actv.getText().toString()=="") {
					Snackbar.make(linearLayout, "6-18个字母、数字或下划线的组合，以字母开头", Snackbar.LENGTH_LONG).setAction("Cacle", new OnClickListener() {
						@Override
						public void onClick(View view) {

						}
					});
					return;
				}
				// 清空参数
				SharedPreferences.Editor editor=preferences.edit();
				editor.putString(actv.getText().toString(),actv.getText().toString());
				editor.commit();
				mVerifier.setParameter(SpeechConstant.PARAMS, null);
				mVerifier.setParameter(SpeechConstant.ISV_PWDT, "" + mPwdType);
				mVerifier.getPasswordList(mPwdListenter);
				break;
			case R.id.isv_verify:
				// 清空提示信息
				((TextView) findViewById(R.id.showMsg)).setText("");
				// 清空参数
				mVerifier.setParameter(SpeechConstant.PARAMS, null);
				mVerifier.setParameter(SpeechConstant.ISV_AUDIO_PATH,
						Environment.getExternalStorageDirectory().getAbsolutePath() + "/msc/verify.pcm");
				mVerifier = SpeakerVerifier.getVerifier();
				// 设置业务类型为验证
				mVerifier.setParameter(SpeechConstant.ISV_SST, "verify");
				// 对于某些麦克风非常灵敏的机器，如nexus、samsung i9300等，建议加上以下设置对录音进行消噪处理
//			mVerify.setParameter(SpeechConstant.AUDIO_SOURCE, "" + MediaRecorder.AudioSource.VOICE_RECOGNITION);

				if (mPwdType == PWD_TYPE_TEXT) {
					// 文本密码注册需要传入密码
					if (TextUtils.isEmpty(mTextPwd)) {
						showTip("请获取密码后进行操作");
						return;
					}
					mVerifier.setParameter(SpeechConstant.ISV_PWD, mTextPwd);
					((TextView) findViewById(R.id.showPwd)).setText("请读出："
							+ mTextPwd);
				} else if (mPwdType == PWD_TYPE_NUM) {
					// 数字密码注册需要传入密码
					String verifyPwd = mVerifier.generatePassword(8);
					mVerifier.setParameter(SpeechConstant.ISV_PWD, verifyPwd);
					((TextView) findViewById(R.id.showPwd)).setText("请读出："
							+ verifyPwd);
				}
				setRadioClickable(false);
				// 设置auth_id，不能设置为空
				mVerifier.setParameter(SpeechConstant.AUTH_ID, mAuthId);
				mVerifier.setParameter(SpeechConstant.ISV_PWDT, "" + mPwdType);
				// 开始验证
				mVerifier.startListening(mVerifyListener);
				break;
			case R.id.isv_stop_record:
				mVerifier.stopListening();
				break;
			case R.id.isv_cancel:
				/***
				 * 临时跳过验证
				 */
//				setRadioClickable(true);
//				mVerifier.cancel();
//				initTextView();
				/***
				 * 无验证登陆主界面
				 */
				Intent intent=new Intent(LoginActivity.this,MainActivity.class);
				startActivity(intent);
				break;
			default:
				break;
		}
	}

	private String[] items;
	private SpeechListener mPwdListenter = new SpeechListener() {
		@Override
		public void onEvent(int eventType, Bundle params) {
		}

		@Override
		public void onBufferReceived(byte[] buffer) {
			setRadioClickable(true);

			String result = new String(buffer);
			switch (mPwdType) {
				case PWD_TYPE_TEXT:
					try {
						JSONObject object = new JSONObject(result);
						if (!object.has("txt_pwd")) {
							initTextView();
							return;
						}

						JSONArray pwdArray = object.optJSONArray("txt_pwd");
						items = new String[pwdArray.length()];
						for (int i = 0; i < pwdArray.length(); i++) {
							items[i] = pwdArray.getString(i);
						}
						mTextPwdSelectDialog = new AlertDialog.Builder(
								LoginActivity.this)
								.setTitle("请选择密码文本")
								.setItems(items,
										new DialogInterface.OnClickListener() {
											@Override
											public void onClick(
													DialogInterface arg0, int arg1) {
												mTextPwd = items[arg1];
												mResultEditText.setText("您的密码：" + mTextPwd);
											}
										}).create();
						mTextPwdSelectDialog.show();
					} catch (JSONException e) {
						e.printStackTrace();
					}
					break;
				case PWD_TYPE_NUM:
					StringBuffer numberString = new StringBuffer();
					try {
						JSONObject object = new JSONObject(result);
						if (!object.has("num_pwd")) {
							initTextView();
							return;
						}

						JSONArray pwdArray = object.optJSONArray("num_pwd");
						numberString.append(pwdArray.get(0));
						for (int i = 1; i < pwdArray.length(); i++) {
							numberString.append("-" + pwdArray.get(i));
						}
					} catch (JSONException e) {
						e.printStackTrace();
					}
					mNumPwd = numberString.toString();
					mNumPwdSegs = mNumPwd.split("-");
					mResultEditText.setText("您的密码：\n" + mNumPwd);
					break;
				default:
					break;
			}
		}

		@Override
		public void onCompleted(SpeechError error) {
			setRadioClickable(true);

			if (null != error && ErrorCode.SUCCESS != error.getErrorCode()) {
				showTip("获取失败：" + error.getErrorCode());
			}
		}
	};

	private VerifierListener mVerifyListener = new VerifierListener() {

		@Override
		public void onVolumeChanged(int volume, byte[] data) {
			showTip("当前正在说话，音量大小：" + volume);
			Log.d(TAG, "返回音频数据："+data.length);
		}

		@Override
		public void onResult(VerifierResult result) {
			setRadioClickable(true);
			mShowMsgTextView.setText(result.source);

			if (result.ret == 0) {
				// 验证通过
				mShowMsgTextView.setText("验证通过");
				Intent intent=new Intent(LoginActivity.this,MainActivity.class);
				startActivity(intent);
			}
			else{
				switch (result.err) {
					case VerifierResult.MSS_ERROR_IVP_GENERAL:
						mShowMsgTextView.setText("内核异常");
						break;
					case VerifierResult.MSS_ERROR_IVP_TRUNCATED:
						mShowMsgTextView.setText("出现截幅");
						break;
					case VerifierResult.MSS_ERROR_IVP_MUCH_NOISE:
						mShowMsgTextView.setText("太多噪音");
						break;
					case VerifierResult.MSS_ERROR_IVP_UTTER_TOO_SHORT:
						mShowMsgTextView.setText("录音太短");
						break;
					case VerifierResult.MSS_ERROR_IVP_TEXT_NOT_MATCH:
						mShowMsgTextView.setText("验证不通过，您所读的文本不一致");
						break;
					case VerifierResult.MSS_ERROR_IVP_TOO_LOW:
						mShowMsgTextView.setText("音量太低");
						break;
					case VerifierResult.MSS_ERROR_IVP_NO_ENOUGH_AUDIO:
						mShowMsgTextView.setText("音频长达不到自由说的要求");
						break;
					default:
						mShowMsgTextView.setText("验证不通过");
						break;
				}
			}
		}
		@Override
		public void onEvent(int eventType, int arg1, int arg2, Bundle arg3) {
		}

		@Override
		public void onError(SpeechError error) {
			setRadioClickable(true);

			switch (error.getErrorCode()) {
				case ErrorCode.MSP_ERROR_NOT_FOUND:
					mShowMsgTextView.setText("模型不存在，请先注册");
					break;

				default:
					showTip("onError Code："	+ error.getPlainDescription(true));
					break;
			}
		}
		@Override
		public void onEndOfSpeech() {
			showTip("结束说话");
		}

		@Override
		public void onBeginOfSpeech() {
			showTip("开始说话");
		}
	};

	@Override
	public void finish() {
		if (null != mTextPwdSelectDialog) {
			mTextPwdSelectDialog.dismiss();
		}
		super.finish();
	}

	@Override
	protected void onDestroy() {
		if (null != mVerifier) {
			mVerifier.stopListening();
			mVerifier.destroy();
		}
		super.onDestroy();
	}
	private void showTip(final String str) {
		mToast.setText(str);
		mToast.show();
	}
}
