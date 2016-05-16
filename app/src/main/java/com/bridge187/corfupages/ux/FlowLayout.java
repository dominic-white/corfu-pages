package com.bridge187.corfupages.ux;
/*
 * modified from source at https://docs.google.com/file/d/0B_VKZCqEnHblNDdiY2UxODgtYWNhNS00MmU4LWE4NDMtZjQ1OWI5MDMxZTVh/edit?num=50&sort=name&layout=list&pli=1#
 */
//TODO This class has a bug whereby sometimes the last image is not shown. It would be best to start from scratch with this class
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

public class FlowLayout extends ViewGroup
{
    private int horizontalOffsetForCenter = Integer.MAX_VALUE;

    public FlowLayout(Context context, AttributeSet attrs)
    {
        super(context, attrs);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
    {
        int widthSize = MeasureSpec.getSize(widthMeasureSpec) - getPaddingRight();
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);

        boolean growHeight = widthMode != MeasureSpec.UNSPECIFIED;

        int width = 0;
        int height = getPaddingTop();

        int currentWidth = getPaddingLeft();
        int currentHeight = 0;

        boolean breakLine = false;
        boolean newLine = false;
        boolean singleLine = true;

        final int count = getChildCount();
        for (int i = 0; i < count; i++)
        {
            View child = getChildAt(i);
            measureChild(child, widthMeasureSpec, heightMeasureSpec);

            LayoutParams lp = (LayoutParams) child.getLayoutParams();

            int right = currentWidth + child.getMeasuredWidth();

            if (growHeight && (breakLine || right > widthSize))
            {
                int tempHorizontalOffsetForCenter = (widthSize - currentWidth) / 2;
                if (tempHorizontalOffsetForCenter < horizontalOffsetForCenter)
                {
                    horizontalOffsetForCenter = tempHorizontalOffsetForCenter;
                }

                height += currentHeight;
                currentHeight = 0;
                width = Math.max(width, currentWidth);
                currentWidth = getPaddingLeft();
                newLine = true;
                singleLine = false;
            }
            else
            {
                newLine = false;
            }

            lp.x = currentWidth;
            lp.y = height;

            currentWidth += child.getMeasuredWidth();
            currentHeight = Math.max(currentHeight, child.getMeasuredHeight());

            breakLine = lp.breakLine;
        }

        if (singleLine)
        {
            horizontalOffsetForCenter = 0;
        }

        if (!newLine)
        {
            height += currentHeight;
            width = Math.max(width, currentWidth);
        }

        width += getPaddingRight();
        height += getPaddingBottom();

        setMeasuredDimension(resolveSize(width, widthMeasureSpec),
                resolveSize(height, heightMeasureSpec));
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b)
    {
        final int count = getChildCount();
        for (int i = 0; i < count; i++)
        {
            View child = getChildAt(i);
            LayoutParams lp = (LayoutParams) child.getLayoutParams();
            child.layout(lp.x + horizontalOffsetForCenter, lp.y, lp.x + child.getMeasuredWidth() + horizontalOffsetForCenter, lp.y + child.getMeasuredHeight());
        }
    }


    @Override
    protected boolean checkLayoutParams(ViewGroup.LayoutParams p)
    {
        return p instanceof LayoutParams;
    }

    @Override
    protected LayoutParams generateDefaultLayoutParams()
    {
        return new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
    }

    @Override
    public LayoutParams generateLayoutParams(AttributeSet attrs)
    {
        return new LayoutParams(getContext(), attrs);
    }

    @Override
    protected LayoutParams generateLayoutParams(ViewGroup.LayoutParams p)
    {
        return new LayoutParams(p.width, p.height);
    }

    private static class LayoutParams extends ViewGroup.LayoutParams
    {
        private int x;
        private int y;

        @SuppressWarnings("unused")
        public boolean breakLine;

        public LayoutParams(Context context, AttributeSet attrs)
        {
            super(context, attrs);
        }

        public LayoutParams(int w, int h)
        {
            super(w, h);
        }
    }
}
