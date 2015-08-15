package mrfu.photochoice.lib;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;

/**
 * @author Mr.Fu
 */
public class SquareImageView extends ImageView {

	public SquareImageView(Context context) {
        super(context);
    }
    
    public SquareImageView(Context context, AttributeSet attrs) {
    	super(context, attrs);
    }
	public SquareImageView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs ,defStyle);
	}
	
	@Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(getDefaultSize(0, widthMeasureSpec), getDefaultSize(0, heightMeasureSpec));
 
        int childWidthSize = getMeasuredWidth();
        heightMeasureSpec = widthMeasureSpec = MeasureSpec.makeMeasureSpec(childWidthSize, MeasureSpec.EXACTLY);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }
}
