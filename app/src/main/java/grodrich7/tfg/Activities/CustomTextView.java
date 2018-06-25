package grodrich7.tfg.Activities;


import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.util.AttributeSet;

import grodrich7.tfg.R;

/**
 * Created by gabri on 27/08/16.
 */
public class CustomTextView extends android.support.v7.widget.AppCompatTextView {
    public CustomTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        aplicateFont(getFontPath(attrs));
    }

    private String getFontPath(AttributeSet attrs){
        TypedArray theAttrs = getContext().obtainStyledAttributes(attrs, R.styleable.CustomTextView);
        String font = theAttrs.getString(R.styleable.CustomTextView_fuente);
        theAttrs.recycle();
        String defaultFont = "fonts/amsterdam.ttf";
        if (font == null){
            font = defaultFont;
        }else{
            font = "fonts/".concat(font);
        }
        return font;
    }

    private void aplicateFont(String fontPath){
        Typeface TF = Typeface.createFromAsset(getContext().getAssets(),fontPath);
        this.setTypeface(TF);
    }
}