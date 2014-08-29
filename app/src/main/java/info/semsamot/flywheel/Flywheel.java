/**
 * Created by semsamot on 6/25/14.
 *
 * Copyright 2014 semsamot
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package info.semsamot.flywheel;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Camera;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Shader;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Scroller;

import java.util.ArrayList;

public class Flywheel extends View {

    private ArrayList<Item> items = new ArrayList<Item>();

    private boolean isVertical = true;
    private int containerWidth, containerHeight;
    private int scrollPos = 0;
    private int maxPositionValue;
    private int horizontalItemPadding = 20;
    private int textBelowImagePadding = 5;
    private int defaultTextColor = Color.DKGRAY;

    private Item selectedItem;

    private Paint paint;

    private Matrix mMatrix;
    private Camera mCamera;

    private LinearGradient pageTopGradient, pageBottomGradient;
    private LinearGradient pageLeftGradient, pageRightGradient;

    private Bitmap pageBitmap;
    private Canvas pageCanvas;

    private Bitmap pageTopBitmap, pageGirdleBitmap, pageBottomBitmap;
    private Bitmap pageLeftBitmap, pageMiddleBitmap, pageRightBitmap;

    private Canvas pageTopCanvas, pageGirdleCanvas, pageBottomCanvas;
    private Canvas pageLeftCanvas, pageMiddleCanvas, pageRightCanvas;

    private Rect vSidesOfPageRect, bottomOfPageRect;
    private Rect sidesOfPageRect, rightOfPageRect;
    private Rect girdleOfPageSrcRect, girdleOfPageDstRect;
    private Rect middleOfPageSrcRect, middleOfPageDstRect;

    private int topSideOfPage, bottomSideOfPage;
    private int leftSideOfPage, rightSideOfPage;

    private boolean page3dEffect = true;
    private int pageBackgroundColor = Color.WHITE;
    private Drawable backgroundDrawable;

    private GestureDetector detector;
    private Scroller scroller;
    private ScrollListener scrollListener;
    private ValueAnimator scrollAnimator;

    private OnAutoCenterListener onAutoCenterListener;

    public Flywheel(Context context) {
        super(context);
    }

    public Flywheel(Context context, AttributeSet attrs) {
//        super(context, attrs);
        this(context, attrs, 0);

    }

    public Flywheel(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        // set text color from "tag" attribute
        if (getTag() != null)
            setDefaultTextColor( Color.parseColor(getTag().toString()) );

        // transfer background color and then remove original background
        this.backgroundDrawable = getBackground();
        this.pageBackgroundColor = getBackgroundColor(this.backgroundDrawable);
        setBackgroundResource(0);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void initGfx()
    {
        containerWidth = getWidth();
        containerHeight = getHeight();

        if (containerWidth == 0 || containerHeight == 0) return;

        this.pageCenterX = containerWidth / 2;
        this.pageCenterY = containerHeight / 2;

        horizontalItemPadding = 7;
        textBelowImagePadding = 2;

        paint = new Paint();
        pageBitmap = Bitmap.createBitmap(containerWidth, containerHeight, Bitmap.Config.ARGB_8888);
        pageCanvas = new Canvas(pageBitmap);

        mCamera = new Camera();
        mMatrix = new Matrix();

        topSideOfPage = containerHeight / 5;
        bottomSideOfPage = topSideOfPage * 4;
        leftSideOfPage = containerWidth / 5;
        rightSideOfPage = leftSideOfPage * 4;

        int middleOfPageWidth = leftSideOfPage * 3;
        int girdleOfPageHeight = topSideOfPage * 3;

        pageTopGradient = new LinearGradient(
                0, 0, 0, topSideOfPage,
                Color.TRANSPARENT, pageBackgroundColor, Shader.TileMode.CLAMP);
        pageBottomGradient = new LinearGradient(
                0, 0, 0, topSideOfPage,
                pageBackgroundColor, Color.TRANSPARENT, Shader.TileMode.CLAMP);

        pageLeftGradient = new LinearGradient(
                0, pageCenterY, leftSideOfPage, pageCenterY,
                Color.TRANSPARENT, pageBackgroundColor, Shader.TileMode.CLAMP);
        pageRightGradient = new LinearGradient(
                0, pageCenterY, leftSideOfPage, pageCenterY,
                pageBackgroundColor, Color.TRANSPARENT, Shader.TileMode.CLAMP);

        pageTopBitmap = Bitmap.createBitmap(containerWidth, topSideOfPage, Bitmap.Config.ARGB_8888);
        pageGirdleBitmap = Bitmap.createBitmap(containerWidth, girdleOfPageHeight, Bitmap.Config.ARGB_8888);
        pageBottomBitmap = Bitmap.createBitmap(containerWidth, topSideOfPage, Bitmap.Config.ARGB_8888);

        pageLeftBitmap = Bitmap.createBitmap(leftSideOfPage, containerHeight, Bitmap.Config.ARGB_8888);
        pageMiddleBitmap = Bitmap.createBitmap(middleOfPageWidth, containerHeight, Bitmap.Config.ARGB_8888);
        pageRightBitmap = Bitmap.createBitmap(leftSideOfPage, containerHeight, Bitmap.Config.ARGB_8888);

        pageTopCanvas = new Canvas(pageTopBitmap);
        pageGirdleCanvas = new Canvas(pageGirdleBitmap);
        pageBottomCanvas = new Canvas(pageBottomBitmap);

        pageLeftCanvas = new Canvas(pageLeftBitmap);
        pageMiddleCanvas = new Canvas(pageMiddleBitmap);
        pageRightCanvas = new Canvas(pageRightBitmap);

        vSidesOfPageRect = new Rect(0, 0, containerWidth, topSideOfPage);
        girdleOfPageSrcRect = new Rect(0, topSideOfPage, containerWidth, bottomSideOfPage);
        girdleOfPageDstRect = new Rect(0, 0, containerWidth, girdleOfPageHeight);
        bottomOfPageRect = new Rect(0, bottomSideOfPage, containerWidth, containerHeight);

        sidesOfPageRect = new Rect(0, 0, leftSideOfPage, containerHeight);
        middleOfPageSrcRect = new Rect(leftSideOfPage, 0, rightSideOfPage, containerHeight);
        middleOfPageDstRect = new Rect(0, 0, middleOfPageWidth, containerHeight);
        rightOfPageRect = new Rect(rightSideOfPage, 0, containerWidth, containerHeight);

        desiredRect = new Rect();

        scrollListener = new ScrollListener();
        Context appContext = getContext();
        detector = new GestureDetector(appContext, scrollListener);

        if (Build.VERSION.SDK_INT >= 11)
            scroller = new Scroller(appContext, null, true);
        else
            scroller = new Scroller(appContext, null);

        // FIXME compatibility issue
        if (Build.VERSION.SDK_INT < 11) return;

        scrollAnimator = ValueAnimator.ofFloat(0, 1);
        scrollAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                if (!scroller.isFinished())
                {
                    scroller.computeScrollOffset();
                    scrollPos = (isVertical) ? scroller.getCurrY() : scroller.getCurrX();
                    postInvalidate();

                    if (scrollPos <= 0 || scrollPos >= maxPositionValue)
                    {
//                        scrollAnimator.cancel();
                        scroller.forceFinished(true);
//                        return;
                    }
                }else {
                    scrollAnimator.cancel();

                    autoCenterItem();
                }
            }
        });

        boolean isSampleItems = false;
        if (items.size() < 1)
        {
            addItem("sample1");
            addItem("sample2");
            addItem("sample3");
            addItem("sample4");
            addItem("sample5");
            isSampleItems = true;
        }

        lastPos = (isVertical) ? containerHeight / 3 : containerWidth / 3;
        for (Item item : items)
        {
            calculateItemRect(item);
            item.initTextChunks();
        }

        this.itemWidth = (isVertical) ? containerHeight / 3 : containerWidth / 3;

        if (isSampleItems) autoCenterItem(2);

        if (runAfterInit != null) runAfterInit.run();
    }

    private Runnable runAfterInit;
    public void setRunAfterInit(Runnable runAfterInit) {
        this.runAfterInit = runAfterInit;
    }

    private int pageCenterX, pageCenterY;
    private int itemWidth;
    private boolean isWhite;
    private Rect desiredRect;
    @Override
    protected void onDraw(Canvas viewCanvas) {
        int itemsSize = items.size();
        int rightOfScreen = scrollPos + containerWidth;
        int bottomOfScreen = scrollPos + containerHeight;

        pageBitmap.eraseColor(Color.TRANSPARENT);

        for (int i=0; i < itemsSize; i++)
        {
            isWhite = !isWhite;

            Item item = items.get(i);
            if ( isVertical && (item.rect.bottom < scrollPos || item.rect.top > bottomOfScreen) )
            {
                continue;
            }else if (!isVertical && (item.rect.right < scrollPos || item.rect.left > rightOfScreen) )
            {
                continue;
            }

            desiredRect.set(item.rect);
            if (isVertical)
            {
                desiredRect.offset(0, -scrollPos);
            }else {
                desiredRect.offset(-scrollPos, 0);
            }

            // uncomment for debugging purposes
            // --------------------------------
            /*paint.setColor( (isWhite) ? Color.WHITE : Color.BLUE );
            pageCanvas.drawRect(desiredRect, paint);*/
            // --------------------------------

            // TODO use LRU Cache for LinearGradient object to achieve best performance
            LinearGradient completeGradient;
            if (isVertical)
            {
                completeGradient = new LinearGradient(
                        0, 0, 0, containerHeight,
                        new int[]{Color.TRANSPARENT, item.textColor, item.textColor, Color.TRANSPARENT},
                        new float[]{0f, 0.3f, 0.7f, 1f}, Shader.TileMode.CLAMP
                );
            }else {
                completeGradient = new LinearGradient(
                        0, pageCenterY, containerWidth, 0,
                        new int[]{Color.TRANSPARENT, item.textColor, item.textColor, Color.TRANSPARENT},
                        new float[]{0f, 0.3f, 0.7f, 1f}, Shader.TileMode.CLAMP
                );
            }

            paint.setShader(completeGradient);
            paint.setStrokeWidth(3);

            if (isVertical)
            {
                pageCanvas.drawLine(desiredRect.left, desiredRect.top,
                        desiredRect.right, desiredRect.top, paint);
                // if its last item, then draw another line on the bottom.
                if (i == itemsSize - 1)
                {
                    pageCanvas.drawLine(desiredRect.left, desiredRect.bottom,
                            desiredRect.right, desiredRect.bottom, paint);
                }
            }else {
                pageCanvas.drawLine(desiredRect.left, desiredRect.top,
                        desiredRect.left, desiredRect.bottom, paint);
                // if its last item, then draw another line on the right.
                if (i == itemsSize - 1)
                {
                    pageCanvas.drawLine(desiredRect.right, desiredRect.top,
                            desiredRect.right, desiredRect.bottom, paint);
                }
            }

//            paint.setColor(item.textColor);
            paint.setTextSize(item.textSize);

            int textChunksSize = (item.textChunks != null) ? item.textChunks.size() : 0;
            for (int j=0; j < textChunksSize; j++)
            {
                // center text in the rect
                Item.TextChunk textChunk = item.textChunks.get(j);
                int offset = ( item.rect.width() - (int) paint.measureText(textChunk.text) ) / 2;
//                int position = item.rect.left - scrollPos + offset;
                int position = desiredRect.left + offset;

                if (isVertical)
                {
                    pageCanvas.drawText(
                            textChunk.text, position, desiredRect.top + textChunk.posY, paint);
                }else {
                    pageCanvas.drawText(textChunk.text, position, textChunk.posY, paint);
                }
            }
        }

        paint.setShader(null);
        paint.setColor(Color.BLACK);
        if (isVertical)
        {
            pageCanvas.drawLine(0, 0, 0, containerHeight, paint);
            pageCanvas.drawLine(containerWidth, 0, containerWidth, containerHeight, paint);
        }else {
            pageCanvas.drawLine(0, 0, containerWidth, 0, paint);
            pageCanvas.drawLine(0, containerHeight, containerWidth, containerHeight, paint);
        }

        if (page3dEffect)
        {
            if (isVertical)
            {
                drawPageInVertical3D(pageBitmap, viewCanvas);
            }else {
                drawPageIn3D(pageBitmap, viewCanvas);
            }
        }else {
            viewCanvas.drawBitmap(pageBitmap, 0, 0, null);
        }
    }

    private Paint paint3d = new Paint();
    private void drawPageIn3D(Bitmap pageBitmap, Canvas canvas)
    {
        pageLeftBitmap.eraseColor(Color.TRANSPARENT);
        pageMiddleBitmap.eraseColor(pageBackgroundColor);
        pageRightBitmap.eraseColor(Color.TRANSPARENT);

        paint3d.setShader(pageLeftGradient);
        pageLeftCanvas.drawRect(sidesOfPageRect, paint3d);
        pageLeftCanvas.drawBitmap(pageBitmap, sidesOfPageRect, sidesOfPageRect, paint3d);

//        paint3d.setShader(null);
        pageMiddleCanvas.drawBitmap(pageBitmap, middleOfPageSrcRect, middleOfPageDstRect, null);

        paint3d.setShader(pageRightGradient);
        pageRightCanvas.drawRect(sidesOfPageRect, paint3d);
        pageRightCanvas.drawBitmap(pageBitmap, rightOfPageRect, sidesOfPageRect, paint3d);

        prepareMatrix(mMatrix, 0, -45);


        mMatrix.preTranslate(-leftSideOfPage, -pageCenterY);
        mMatrix.postTranslate(leftSideOfPage, pageCenterY);

        mMatrix.postTranslate(0, 0);

        canvas.drawBitmap(pageLeftBitmap, mMatrix, null);

//        prepareMatrix(mMatrix, 0, 0);
//
//        mMatrix.preTranslate(-pageCenterX, -pageCenterY);
//        mMatrix.postTranslate(pageCenterX, pageCenterY);
//
//        mMatrix.postTranslate(leftSideOfPage, 0);
//
//        canvas.drawBitmap(pageMiddleBitmap, mMatrix, null);
        canvas.drawBitmap(pageMiddleBitmap, null, middleOfPageSrcRect, null);

        prepareMatrix(mMatrix, 0, 45);

        mMatrix.preTranslate(0, -pageCenterY);
        mMatrix.postTranslate(0, pageCenterY);

        mMatrix.postTranslate(rightSideOfPage, 0);

        canvas.drawBitmap(pageRightBitmap, mMatrix, null);
    }

    private void drawPageInVertical3D(Bitmap pageBitmap, Canvas canvas)
    {
        pageTopBitmap.eraseColor(Color.TRANSPARENT);
        pageGirdleBitmap.eraseColor(pageBackgroundColor);
        pageBottomBitmap.eraseColor(Color.TRANSPARENT);

        paint3d.setShader(pageTopGradient);
        pageTopCanvas.drawRect(vSidesOfPageRect, paint3d);
        pageTopCanvas.drawBitmap(pageBitmap, vSidesOfPageRect, vSidesOfPageRect, paint3d);

//        paint3d.setShader(null);
        pageGirdleCanvas.drawBitmap(pageBitmap, girdleOfPageSrcRect, girdleOfPageDstRect, null);

        paint3d.setShader(pageBottomGradient);
        pageBottomCanvas.drawRect(vSidesOfPageRect, paint3d);
        pageBottomCanvas.drawBitmap(pageBitmap, bottomOfPageRect, vSidesOfPageRect, paint3d);


        prepareMatrix(mMatrix, -45, 0);

        mMatrix.preTranslate(-pageCenterX, -topSideOfPage);
        mMatrix.postTranslate(pageCenterX, topSideOfPage);

        mMatrix.postTranslate(0, 0);

        canvas.drawBitmap(pageTopBitmap, mMatrix, null);

//        prepareMatrix(mMatrix, 0, 0);
//
//        mMatrix.preTranslate(-pageCenterX, -pageCenterY);
//        mMatrix.postTranslate(pageCenterX, pageCenterY);
//
//        mMatrix.postTranslate(leftSideOfPage, 0);
//
//        canvas.drawBitmap(pageMiddleBitmap, mMatrix, null);
        canvas.drawBitmap(pageGirdleBitmap, null, girdleOfPageSrcRect, null);

        prepareMatrix(mMatrix, 45, 0);

        mMatrix.preTranslate(-pageCenterX, 0);
        mMatrix.postTranslate(pageCenterX, 0);

        mMatrix.postTranslate(0, bottomSideOfPage);

        canvas.drawBitmap(pageBottomBitmap, mMatrix, null);
    }

    private void prepareMatrix(final Matrix outMatrix, float angleX, float angleY)
    {
        mCamera.save();

//        mCamera.translate(0, 0, 50);
        mCamera.rotateX(angleX);
        mCamera.rotateY(angleY);

        mCamera.getMatrix(outMatrix);
        mCamera.restore();
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);

        initGfx();
    }

    private ObjectAnimator objectAnimator;
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void autoCenterItem()
    {
        if ( objectAnimator != null && objectAnimator.isRunning() ) return;

        Item itemAtCenter = null;
        int rightOfScreen = scrollPos + containerWidth;
        int bottomOfScreen = scrollPos + containerHeight;

        for (Item item : items)
        {
            if (isVertical && (item.rect.bottom < scrollPos || item.rect.top > bottomOfScreen) )
                continue;
            if (!isVertical && (item.rect.right < scrollPos || item.rect.left > rightOfScreen) )
                continue;

            if ( (isVertical && item.rect.contains(0, scrollPos + pageCenterY) ) ||
                (!isVertical && item.rect.contains(scrollPos + pageCenterX, 0)) )
            {
                itemAtCenter = item;
                break;
            }
        }

        if (itemAtCenter == null)
        {
            if ( (items.size() > 0) && (isVertical && scrollPos < items.get(0).rect.top ||
                                       !isVertical && scrollPos < items.get(0).rect.left) )
                autoCenterItem(0);
            else
                autoCenterItem(items.size() - 1);
        }

        autoCenterItem(itemAtCenter);
    }

    public void autoCenterItem(int position)
    {
        if ( position < 0 || position >= items.size() ) return;
        autoCenterItem(items.get(position));
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void autoCenterItem(Item itemAtCenter)
    {
        if (itemAtCenter == null) return;

        int targetScrollPos;

        if (isVertical)
        {
            targetScrollPos = itemAtCenter.rect.centerY() - (containerHeight / 2);
        }else {
            targetScrollPos = itemAtCenter.rect.centerX() - (containerWidth / 2);
        }

        // FIXME compatibility issue
        if (Build.VERSION.SDK_INT < 11) return;
        objectAnimator = ObjectAnimator.ofInt(this, "scrollPos", scrollPos, targetScrollPos);
        objectAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                postInvalidate();
            }
        });
        objectAnimator.setDuration(500);
        objectAnimator.start();

        this.selectedItem = itemAtCenter;
        if (onAutoCenterListener != null) onAutoCenterListener.onAutoCenter(itemAtCenter);
    }

    public void addItem(String text)
    {
        addItem(text, null);
    }

    public void addItem(String text, int textColor)
    {
        addItem(text, textColor, null);
    }

    public void addItem(String text, Drawable image)
    {
        addItem(text, this.defaultTextColor, image);
    }

    public void addItem(String text, int textColor, Drawable image)
    {
        Item item = new Item(this);
        item.text = text;
        item.textColor = textColor;
        item.image = image;

        items.add(item);

        // if it is first item then set to selected
        if (items.size() == 1)
        {
            selectedItem = item;
        }
    }

    private int lastPos;
    private void calculateItemRect(Item item)
    {
        int pageSlice = (isVertical) ? containerHeight / 3 : containerWidth / 3;

        item.rect = new Rect();

        if (isVertical)
        {
            item.rect.left = 0;
            item.rect.right = containerWidth;
            item.rect.top = lastPos;
            item.rect.bottom = lastPos + pageSlice;

            lastPos = item.rect.bottom;
        }else {
            item.rect.left = lastPos;
            item.rect.right = lastPos + pageSlice;
            item.rect.top = 0;
            item.rect.bottom = containerHeight;

            lastPos = item.rect.right;
        }

        maxPositionValue = lastPos - (pageSlice * 2);
    }

    public Item getSelectedItem() {
        return selectedItem;
    }

    public int getSelectedItemIndex() {
        return items.indexOf(selectedItem);
    }

    public void setSelectedItem(Item selectedItem) {
        autoCenterItem(selectedItem);
    }

    public void setSelectedItemIndex(int selectedItemIndex) {
        autoCenterItem(selectedItemIndex);
    }

    public void setSelectedItemByText(String itemText)
    {
        for (Item item : items)
        {
            if (item.getText().equals(itemText))
            {
                autoCenterItem(item);
                return;
            }
        }
    }

    public int getDefaultTextColor() {
        return defaultTextColor;
    }

    public void setDefaultTextColor(int defaultTextColor) {
        setDefaultTextColor(defaultTextColor, false);
    }

    public void setDefaultTextColor(int defaultTextColor, boolean applyToCurrentItems) {
        this.defaultTextColor = defaultTextColor;
        if (applyToCurrentItems)
        {
            for (Item item : items)
            {
                item.textColor = defaultTextColor;
            }
        }
    }

    public int getItemsSize()
    {
        return items.size();
    }

    public void setScrollPos(int scrollPos) {
        this.scrollPos = scrollPos;
    }

    public OnAutoCenterListener getOnAutoCenterListener() {
        return onAutoCenterListener;
    }

    public void setOnAutoCenterListener(OnAutoCenterListener onAutoCenterListener) {
        this.onAutoCenterListener = onAutoCenterListener;
    }

    public String getOrientation() {
        return (isVertical) ? "vertical" : "horizontal";
    }

    public void setOrientation(String orientation) {
        isVertical = (orientation.equals("vertical"));
    }

    public boolean has3dEffect() {
        return page3dEffect;
    }

    public void set3dEffect(boolean page3dEffect) {
        this.page3dEffect = page3dEffect;
    }

    public int getBackgroundColor()
    {
        Drawable background = getBackground();
        return getBackgroundColor(background);
    }

    /*
    * This method for retrieving background color (actual color)
    * of a ColorDrawable is inspired by "jpmcosta" at stackoverflow.com
    * link: http://stackoverflow.com/a/13748610/3922482
    * */
    public int getBackgroundColor(Drawable background)
    {
        // The actual color, not the id.
        int color = Color.WHITE;

        if(background instanceof ColorDrawable) {
            if(Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {

                Bitmap mBitmap;
                Canvas mCanvas;
                Rect mBounds;

                mBitmap = Bitmap.createBitmap(1,1, Bitmap.Config.ARGB_8888);
                mCanvas = new Canvas(mBitmap);
                mBounds = new Rect();

                // If the ColorDrawable makes use of its bounds in the draw method,
                // we may not be able to get the color we want. This is not the usual
                // case before Ice Cream Sandwich (4.0.1 r1).
                // Yet, we change the bounds temporarily, just to be sure that we are
                // successful.
                ColorDrawable colorDrawable = (ColorDrawable) background;

                mBounds.set(colorDrawable.getBounds()); // Save the original bounds.
                colorDrawable.setBounds(0, 0, 1, 1); // Change the bounds.

                colorDrawable.draw(mCanvas);
                color = mBitmap.getPixel(0, 0);

                colorDrawable.setBounds(mBounds); // Restore the original bounds.
            }
            else {
                color = ((ColorDrawable)background).getColor();
            }
        }

        return color;
    }

    /*@Override
    public void setBackground(Drawable background) {
        int color = getBackgroundColor(background);
        setBackgroundColor(color);
    }*/

    @Override
    public void setBackgroundColor(int color) {
        this.pageBackgroundColor = color;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        boolean result = detector.onTouchEvent(event);

        if (!result && event.getAction() == MotionEvent.ACTION_UP)
        {
            if (scroller.isFinished())
            {
                autoCenterItem();
            }
        }

        return result;
    }

    private class ScrollListener extends GestureDetector.SimpleOnGestureListener
    {
        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }

        @TargetApi(Build.VERSION_CODES.HONEYCOMB)
        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY)
        {
            if (isVertical)
            {
                scroller.fling(0, scrollPos,
                        0, (int)-velocityY,
                        0, 0,
                        -containerHeight * 5, maxPositionValue + (containerHeight * 5) );
            }else {
                scroller.fling(scrollPos, 0,
                        (int)-velocityX, 0,
                        -containerWidth * 5, maxPositionValue + (containerWidth * 5),
                        0, 0);
            }
            scrollAnimator.setDuration(scroller.getDuration());
            scrollAnimator.start();

            return true;
        }

        @TargetApi(Build.VERSION_CODES.HONEYCOMB)
        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY)
        {
            if (isVertical && scrollPos + distanceY >= 0 && scrollPos + distanceY <= maxPositionValue)
            {
                scrollAnimator.cancel();
                scrollPos += distanceY;
                postInvalidate();
                return true;
            }
            else if (!isVertical && scrollPos + distanceX >= 0 && scrollPos + distanceX <= maxPositionValue)
            {
                scrollAnimator.cancel();
                scrollPos += distanceX;
                postInvalidate();
                return true;
            }else {
                scrollAnimator.cancel();

//                if ( scrollPos < 0 ) scrollPos = 0;
//                if ( scrollPos > maxPositionValue ) scrollPos = maxPositionValue;

                return false;
            }
        }
    }

    public static interface OnAutoCenterListener
    {
        public void onAutoCenter(Item itemAtCenter);
    }
}
