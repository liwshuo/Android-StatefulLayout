package cz.kinst.jakub.view;

import android.content.Context;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;


/**
 * Created by Jakub Kinst (jakub@kinst.cz)
 */
public class StatefulLayout extends FrameLayout {

	public static final String SAVED_INSTANCE_STATE = "instanceState";
	private static final String SAVED_STATE = "stateful_layout_state";

	private Map<String, View> mStateViews = new HashMap<>();
	private String mState = null;
	private OnStateChangeListener mOnStateChangeListener;
	private boolean mInitialized;


	public interface OnStateChangeListener {
		void onStateChange(String state);
	}


	public StatefulLayout(Context context) {
		super(context);
	}


	public StatefulLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
	}


	public StatefulLayout(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}


	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		if(!mInitialized)
			onSetupContentState();
	}


	@Override
	protected Parcelable onSaveInstanceState() {
		Bundle bundle = new Bundle();
		bundle.putParcelable(SAVED_INSTANCE_STATE, super.onSaveInstanceState());
		saveInstanceState(bundle);
		return bundle;
	}


	@Override
	protected void onRestoreInstanceState(Parcelable state) {
		if(state instanceof Bundle) {
			Bundle bundle = (Bundle) state;
			restoreInstanceState(bundle);
			state = bundle.getParcelable(SAVED_INSTANCE_STATE);
		}
		super.onRestoreInstanceState(state);
	}


	public void setStateView(String state, View view) {
		if(mStateViews.containsKey(state)) {
			removeView(mStateViews.get(state));
		}
		mStateViews.put(state, view);
		if(view.getParent() == null) {
			addView(view);
		}
		view.setVisibility(GONE);
	}


	public String getState() {
		return mState;
	}


	public void setState(String state) {
		if(getStateView(state) == null) {
			throw new IllegalStateException(String.format("Cannot switch to state \"%s\". This state was not defined or the view for this state is null."));
		}
		mState = state;
		for(String s : mStateViews.keySet()) {
			mStateViews.get(s).setVisibility(s.equals(state) ? View.VISIBLE : View.GONE);
		}
		if(mOnStateChangeListener != null) mOnStateChangeListener.onStateChange(state);
	}


	public void setOnStateChangeListener(OnStateChangeListener listener) {
		mOnStateChangeListener = listener;
	}


	public void saveInstanceState(Bundle outState) {
		if(mState != null)
			outState.putString(SAVED_STATE, mState);
	}


	public String restoreInstanceState(Bundle savedInstanceState) {
		String state = savedInstanceState.getString(SAVED_STATE);
		setState(state);
		return state;
	}


	public View getStateView(String state) {
		return mStateViews.get(state);
	}


	public void clearStates() {
		for(String state : new HashSet<>(mStateViews.keySet())) {
			View view = mStateViews.get(state);
			if(!state.equals(State.CONTENT)) {
				removeView(view);
				mStateViews.remove(state);
			}
		}

	}


	protected void onSetupContentState() {
		if(getChildCount() != 1 + mStateViews.size()) {
			throw new IllegalStateException("Invalid child count. StatefulLayout must have exactly one child.");
		}
		View contentView = getChildAt(mStateViews.size());
		removeView(contentView);
		setStateView(State.CONTENT, contentView);
		mInitialized = true;
	}


	public class State {
		public static final String CONTENT = "content";
	}

}
