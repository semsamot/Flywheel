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

import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.TypedValue;

import java.util.ArrayList;

public class Item
{
    private Flywheel flywheel;
    Rect rect;

    Drawable image;
    Paint paint;

    String text;
    ArrayList<TextChunk> textChunks;
    int textSize;
    int textColor;
    int textPadding;
    int lineSpacing;

    public Item(Flywheel flywheel)
    {
        this.flywheel = flywheel;
        this.textSize = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 15, flywheel.getResources().getDisplayMetrics());
        this.text = "";
        this.textPadding = 30;
        this.rect = new Rect();
        this.paint = new Paint();
    }

    public void initTextChunks()
    {
        textChunks = new ArrayList<TextChunk>();
        paint.setTextSize(this.textSize);
        Rect bounds = new Rect();
        paint.getTextBounds(this.text, 0, this.text.length(), bounds);
        lineSpacing = bounds.height() + 5;

        int start = 0;
        int breakAt;
        int lastWord;
        boolean mustTruncate = false;

        //FIXME add support for line breaks ( \n )
        do
        {
            breakAt = start + paint.breakText(
                    this.text, start, this.text.length(), true, this.rect.width() - textPadding, null);

            // if breakAt is not at the end of text, put it at last word
            if (breakAt != this.text.length())
            {
                lastWord = this.text.lastIndexOf(" ", breakAt);
                if (lastWord != -1 && lastWord > start) breakAt = lastWord;
            }

            String slice = this.text.substring(start, breakAt);

            // if next line is out of rect bottom then truncate this line and finish.
            if ( (this.textChunks.size()+2) * lineSpacing > this.rect.height() + textPadding )
            {
                mustTruncate = true;
                if (slice.length() >= 3)
                {
                    slice = slice.substring(0, slice.length() - 3) + "...";
                }
            }

            this.textChunks.add(
                    new TextChunk(slice, (this.textChunks.size()+1) * lineSpacing) );

            start = breakAt;

            if (mustTruncate) break;

        }while (breakAt < this.text.length());


        int textChunksHeight = lineSpacing * textChunks.size();
        int offset = (this.rect.height() - textChunksHeight) / 2;

        for (TextChunk textChunk : this.textChunks)
        {
            textChunk.posY += offset;
        }
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
        initTextChunks();
    }

    public void setTextSize(int textSize) {
        this.textSize = textSize;
        initTextChunks();

    }

    public void setTextPadding(int textPadding) {
        this.textPadding = textPadding;
        initTextChunks();

    }

    public void setLineSpacing(int lineSpacing) {
        this.lineSpacing = lineSpacing;
        initTextChunks();
    }

    public class TextChunk
    {
        int posY;
        String text;

        public TextChunk(String text, int posY)
        {
            this.text = text;
            this.posY = posY;
        }
    }
}
