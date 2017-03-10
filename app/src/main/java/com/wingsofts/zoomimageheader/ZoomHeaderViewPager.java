package com.wingsofts.zoomimageheader;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import com.wingsofts.zoomimageheader.support.ZoomOutPageTransformer;

/**
 * Created by wing on 12/4/16.
 */

public class ZoomHeaderViewPager extends ViewPager {
  public boolean canScroll = true;
  public ZoomHeaderViewPager(Context context) {
    super(context);
  }

  public ZoomHeaderViewPager(Context context, AttributeSet attrs) {
    super(context, attrs);
    setPageTransformer(true, new ZoomOutPageTransformer());
  }

  @Override
  public boolean onTouchEvent(MotionEvent event) {
    return
        canScroll &&
            super.onTouchEvent(event);
  }



  @Override protected int getChildDrawingOrder(int childCount, int i) {

    int position = getCurrentItem();
    if(position<0){
      return i;
    }else{
      if(i == childCount - 1){
        if(position>i){
          position=i;
        }
        return position;
      }
      if(i == position){
        return childCount - 1;
      }
    }
    return i;
  }
}
