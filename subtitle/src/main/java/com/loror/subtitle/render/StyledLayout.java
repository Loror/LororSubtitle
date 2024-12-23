package com.loror.subtitle.render;

import android.text.Layout;
import android.text.Spanned;
import android.text.StaticLayout;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.List;

public class StyledLayout {

    public List<CharSequence> pageLines(CharSequence text, StyledPaint styledPaint, Style topStyle, int width, int split) {
        List<CharSequence> lines = new ArrayList<>();
        if (TextUtils.isEmpty(text)) {
            lines.add(text);
            return lines;
        }
        //多行文本一般包含不同style，可能字体大小不同，各自计算换行
        List<CharSequence> multiLines = new ArrayList<>();
        int start = 0;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == '\n') {
                multiLines.add(text.subSequence(start, ++i));
                start = i;
            }
        }
        if (multiLines.isEmpty()) {
            multiLines.add(text);
        } else if (start < text.length()) {
            multiLines.add(text.subSequence(start, text.length()));
        }
        float saveSize = styledPaint.getTextSize();
        for (CharSequence baseLine : multiLines) {
            if (baseLine instanceof Spanned) {
                Spanned spanned = (Spanned) baseLine;
                StyledSpan[] spans = spanned.getSpans(0, spanned.length(), StyledSpan.class);
                if (spans.length > 0) {
                    Style style = spans[0].getStyle();
                    styledPaint.setMode(StyledPaint.MODE_BODY);
                    styledPaint.setPaint(style);
                }
            }
            final StaticLayout layout = new StaticLayout(baseLine, styledPaint.getTextPaint(), width / split, Layout.Alignment.ALIGN_NORMAL,
                    1.0f, 0f, false);
            int line = layout.getLineCount();
            if (line == 1) {
                lines.add(baseLine);
            } else {
                for (int i = 0; i < line; i++) {
                    CharSequence l = baseLine.subSequence(layout.getLineStart(i), layout.getLineEnd(i));
                    if (!TextUtils.isEmpty(l)) {
                        if (l.charAt(l.length() - 1) == '\n') {
                            lines.add(l.subSequence(0, l.length() - 1));
                        } else {
                            lines.add(l);
                        }
                    }
                }
            }
            styledPaint.setTextSize(saveSize);
        }
        return lines;
    }

}
