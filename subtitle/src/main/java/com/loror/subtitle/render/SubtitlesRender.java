package com.loror.subtitle.render;

import android.graphics.Rect;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.SpannedString;
import android.text.TextUtils;
import android.text.style.CharacterStyle;
import android.text.style.ForegroundColorSpan;

import androidx.annotation.Nullable;

import com.loror.subtitle.SubtitlesDecoder;
import com.loror.subtitle.model.RenderedModel;
import com.loror.subtitle.model.SubtitlesAnimation;
import com.loror.subtitle.model.SubtitlesModel;
import com.loror.subtitle.util.SubtitlesUtil;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class SubtitlesRender {

    public static final String TAG = "SubtitlesRender";

    private final long time;
    private final List<SubtitlesModel> models;
    private boolean hasMove;
    public int playResX;
    public int playResY;

    public long getTime() {
        return time;
    }

    public int getSize() {
        return models == null ? 0 : models.size();
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj == null) {
            return false;
        }
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof SubtitlesRender)) {
            return false;
        }
        SubtitlesRender that = (SubtitlesRender) obj;
        if (time == that.time) {
            return true;
        }
        if (models == null) {
            return that.models == null;
        }
        if (that.models == null) {
            return false;
        }
        if (hasMove || that.hasMove) {
            return false;
        }
        if (models.size() != that.models.size()) {
            return false;
        }
        try {
            for (int i = 0; i < models.size(); i++) {
                if (models.get(i).node != that.models.get(i).node) {
                    return false;
                }
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public SubtitlesRender(List<SubtitlesModel> list, int time) {
        models = SubtitlesUtil.searchSubs(list, time);
        if (models != null) {
            if (!models.isEmpty()) {
                Collections.sort(models, (o1, o2) -> {
                    return Integer.compare(o1.node, o2.node);
                });
                SubtitlesModel model = models.get(0);
                playResX = model.playResX;
                playResY = model.playResY;
            }
        }
        this.time = time;
        if (models != null && !models.isEmpty()) {
            for (SubtitlesModel model : models) {
                if (model.style != null && (model.style.getAnimation() != null || model.style.durationK != 0)) {
                    hasMove = true;
                    break;
                }
            }
        }
    }

    public Map<Integer, RenderedModel> render() {
        if (models == null) {
            return null;
        }
        final Map<Integer, RenderedModel> showModels = new HashMap<>();
        for (SubtitlesModel sub : models) {
            sub.setRenderText(null);
            Style extra = sub.style;
            int gravity = SubtitlesDecoder.GRAVITY_AN2;
            if (extra != null) {
                extra.alpha = 1f;
                extra.currentScaleX = 1f;
                extra.currentScaleY = 1f;
                extra.useSecond = false;
                extra.currentClip = null;
                extra.currentFontScale = 1;
                extra.currentDegree = extra.getDegree();
                extra.currentDegreeX = extra.getDegreeX();
                extra.currentDegreeY = extra.getDegreeY();
                extra.currentBlur = extra.getBlur();
                if (extra.hasScaleX()) {
                    extra.currentScaleX = extra.getScaleX();
                }
                if (extra.hasScaleY()) {
                    extra.currentScaleY = extra.getScaleY();
                }
                //srt带标签时，没有尺寸定义，使用默认尺寸
                if (extra.playResX == 0) {
                    extra.playResX = 384;
                }
                if (extra.playResY == 0) {
                    extra.playResY = 288;
                }
                if (extra.hasPositionX()) {
                    extra.x = extra.getPositionX() / extra.playResX;
                }
                if (extra.hasPositionY()) {
                    extra.y = extra.getPositionY() / extra.playResY;
                }
                if (extra.getClip() != null) {
                    Rect clip = extra.getClip();
                    extra.currentClip = new SubtitlesModel.Extent(clip.left * 1f / extra.playResX
                            , clip.top * 1f / extra.playResY
                            , clip.right * 1f / extra.playResX
                            , clip.bottom * 1f / extra.playResY);
                }
                gravity = extra.gravity;
                if (extra.hasPositionX() || extra.hasPositionY() || extra.hasMoveAnimation()) {
                    gravity = SubtitlesDecoder.GRAVITY_UNSET;
                }
                if (extra.isPath) {
                    gravity = SubtitlesDecoder.GRAVITY_UNSET;
                }
                renderAnimation(sub, extra);
                renderChildStyle(sub, extra);
            }

            RenderedModel item = showModels.get(gravity);
            if (item == null) {
                item = new RenderedModel(gravity);
                showModels.put(gravity, item);
            }
            item.add(sub);
//            android.util.Log.e(TAG, "gravity:" + gravity + "sub:" + sub);
        }
        RenderedModel model = showModels.get(SubtitlesDecoder.GRAVITY_AN2);
        if (model != null) {
            model.sort();
        }
        return showModels;
    }

    //渲染子style
    private void renderChildStyle(SubtitlesModel sub, Style extra) {
        if (extra == null) {
            return;
        }
        CharSequence text = sub.text();
        if (text instanceof Spanned) {
            Spanned spanned = (Spanned) text;
            int start = 0;
            int next;
            for (int i = start; i < spanned.length(); i = next) {
                next = spanned.nextSpanTransition(i, spanned.length(), CharacterStyle.class);
                StyledSpan[] spans = spanned.getSpans(i, next, StyledSpan.class);
                if (spans != null && spans.length > 0) {
                    StyledSpan styledSpan = spans[0];
                    Style style = styledSpan.getStyle();
                    if (style != null) {
                        style.useSecond = extra.useSecond;
                        style.scaleBS = extra.scaleBS;
                        if (!style.hasBlur() || Objects.equals(style.getBlur(), extra.getBlur())) {
                            style.currentBlur = extra.currentBlur;
                        }
                        style.alpha = extra.alpha;
                        style.currentFontScale = extra.currentFontScale;
                    }
                }
            }
        }
    }

    private void renderAnimation(SubtitlesModel sub, Style extra) {
        SubtitlesAnimation animation = extra.getAnimation();
        if (animation == null) {
            return;
        }
        Rect lastClip = null;
        float lastDegree = extra.currentDegree;
        float lastDegreeX = extra.currentDegreeX;
        float lastDegreeY = extra.currentDegreeY;
        float lastScaleX = extra.currentScaleX;
        float lastScaleY = extra.currentScaleY;
        float lastBlur = extra.currentBlur;
        long go = Math.max(time - sub.star, 0);
        while (animation != null) {
            if (animation.durationStart < 0) {
                animation.durationStart = 1;
            }
            if (animation.durationEnd < 0) {
                animation.durationEnd = sub.end - sub.star;
            }
            if (animation instanceof SubtitlesAnimation.MoveAnimation) {
                SubtitlesAnimation.MoveAnimation moveAnimation = (SubtitlesAnimation.MoveAnimation) animation;
                if (extra.playResX != 0 && extra.playResY != 0) {
                    if (moveAnimation.durationStart != 0 || moveAnimation.durationEnd != 0) {
                        float x;
                        float y;
                        if (go < moveAnimation.durationStart) {
                            x = moveAnimation.fromX;
                            y = moveAnimation.fromY;
                        } else if (go < moveAnimation.durationEnd) {
                            float bl = Math.min((go - moveAnimation.durationStart) * 1f / (moveAnimation.durationEnd - moveAnimation.durationStart), 1);
                            x = moveAnimation.fromX + (moveAnimation.toX - moveAnimation.fromX) * bl;
                            y = moveAnimation.fromY + (moveAnimation.toY - moveAnimation.fromY) * bl;
                        } else {
                            x = moveAnimation.toX;
                            y = moveAnimation.toY;
                        }
                        extra.x = Math.min(x / extra.playResX, 5f);
                        extra.y = Math.min(y / extra.playResY, 5f);
                    }
                }
            } else if (animation instanceof SubtitlesAnimation.FadeAnimation) {
                SubtitlesAnimation.FadeAnimation fadeAnimation = (SubtitlesAnimation.FadeAnimation) animation;
                if (fadeAnimation.durationStart != 0) {
                    if (go < fadeAnimation.durationStart) {
                        extra.alpha = go * 1f / fadeAnimation.durationStart;
                    }
                }
                if (fadeAnimation.durationEnd != 0) {
                    long last = Math.max(sub.end - time, 0);
                    if (last <= fadeAnimation.durationEnd) {
                        extra.alpha = last * 1f / fadeAnimation.durationEnd;
                    }
                }
            } else if (animation instanceof SubtitlesAnimation.AlphaFadeAnimation) {
                SubtitlesAnimation.AlphaFadeAnimation fadeAnimation = (SubtitlesAnimation.AlphaFadeAnimation) animation;
                if (go > fadeAnimation.durationStart && go < fadeAnimation.durationEnd) {
                    float bl = Math.min((go - fadeAnimation.durationStart) * 1f / (fadeAnimation.durationEnd - fadeAnimation.durationStart), 1);
                    extra.alpha = (255 - (fadeAnimation.toAlpha - fadeAnimation.fromAlpha) * bl + fadeAnimation.fromAlpha) / 255f;
                }
            } else if (animation instanceof SubtitlesAnimation.ClipAnimation) {
                SubtitlesAnimation.ClipAnimation clipAnimation = (SubtitlesAnimation.ClipAnimation) animation;
                if (clipAnimation.toClip != null) {
                    Rect to = clipAnimation.toClip;
                    Rect rect = lastClip == null ? extra.getClip() : lastClip;
                    if (rect != null) {
                        if (go > clipAnimation.durationStart && go < clipAnimation.durationEnd) {
                            float bl = Math.min((go - clipAnimation.durationStart) * 1f / (clipAnimation.durationEnd - clipAnimation.durationStart), 1);
                            float x1 = rect.left + (to.left - rect.left) * Math.min(bl, 1);
                            float x2 = rect.right + (to.right - rect.right) * Math.min(bl, 1);
                            float y1 = rect.top + (to.top - rect.top) * Math.min(bl, 1);
                            float y2 = rect.bottom + (to.bottom - rect.bottom) * Math.min(bl, 1);
                            rect = new Rect((int) x1, (int) y1, (int) x2, (int) y2);
                        } else if (go > clipAnimation.durationEnd) {
                            rect = to;
                        }
                        lastClip = rect;
                        extra.currentClip = new SubtitlesModel.Extent(rect.left * 1f / extra.playResX
                                , rect.top * 1f / extra.playResY
                                , rect.right * 1f / extra.playResX
                                , rect.bottom * 1f / extra.playResY);
                    }
                }
            } else if (animation instanceof SubtitlesAnimation.FsAnimation) {
                if (extra.hasFontSize()) {
                    SubtitlesAnimation.FsAnimation fsAnimation = (SubtitlesAnimation.FsAnimation) animation;
                    if (go > fsAnimation.durationStart && go < fsAnimation.durationEnd) {
                        float bl = Math.min((go - fsAnimation.durationStart) * 1f / (fsAnimation.durationEnd - fsAnimation.durationStart), 1);
                        int size = (int) (extra.getFontSize() + (fsAnimation.fontSize - extra.getFontSize()) * Math.min(bl, 1));
                        extra.currentFontScale = size * 1f / extra.getFontSize();
                    } else if (go > fsAnimation.durationEnd) {
                        extra.currentFontScale = fsAnimation.fontSize * 1f / extra.getFontSize();
                    }
                }
            } else if (animation instanceof SubtitlesAnimation.DegreeAnimation) {
                SubtitlesAnimation.DegreeAnimation degreeAnimation = (SubtitlesAnimation.DegreeAnimation) animation;
                if (Objects.equals("x", degreeAnimation.type)) {
                    if (go > degreeAnimation.durationStart && go < degreeAnimation.durationEnd) {
                        float bl = Math.min((go - degreeAnimation.durationStart) * 1f / (degreeAnimation.durationEnd - degreeAnimation.durationStart), 1);
                        extra.currentDegreeX = (int) (lastDegreeX + (degreeAnimation.degree - lastDegreeX) * Math.min(bl, 1));
                    } else if (go > degreeAnimation.durationEnd) {
                        extra.currentDegreeX = degreeAnimation.degree;
                    }
                    lastDegreeX = extra.currentDegreeX;
                } else if (Objects.equals("y", degreeAnimation.type)) {
                    if (go > degreeAnimation.durationStart && go < degreeAnimation.durationEnd) {
                        float bl = Math.min((go - degreeAnimation.durationStart) * 1f / (degreeAnimation.durationEnd - degreeAnimation.durationStart), 1);
                        extra.currentDegreeY = (int) (lastDegreeY + (degreeAnimation.degree - lastDegreeY) * Math.min(bl, 1));
                    } else if (go > degreeAnimation.durationEnd) {
                        extra.currentDegreeY = degreeAnimation.degree;
                    }
                    lastDegreeY = extra.currentDegreeY;
                } else {
                    if (go > degreeAnimation.durationStart && go < degreeAnimation.durationEnd) {
                        float bl = Math.min((go - degreeAnimation.durationStart) * 1f / (degreeAnimation.durationEnd - degreeAnimation.durationStart), 1);
                        extra.currentDegree = (int) (lastDegree + (degreeAnimation.degree - lastDegree) * Math.min(bl, 1));
                    } else if (go > degreeAnimation.durationEnd) {
                        extra.currentDegree = degreeAnimation.degree;
                    }
                    lastDegree = extra.currentDegree;
                }
            } else if (animation instanceof SubtitlesAnimation.BlurAnimation) {
                SubtitlesAnimation.BlurAnimation blurAnimation = (SubtitlesAnimation.BlurAnimation) animation;
                if (go > blurAnimation.durationStart && go < blurAnimation.durationEnd) {
                    float bl = Math.min((go - blurAnimation.durationStart) * 1f / (blurAnimation.durationEnd - blurAnimation.durationStart), 1);
                    extra.currentBlur = (int) (lastBlur + (blurAnimation.blur - lastBlur) * Math.min(bl, 1));
                } else if (go > blurAnimation.durationEnd) {
                    extra.currentBlur = blurAnimation.blur;
                }
                lastBlur = extra.currentBlur;
            } else if (animation instanceof SubtitlesAnimation.FsScaleAnimation) {
                SubtitlesAnimation.FsScaleAnimation scaleAnimation = (SubtitlesAnimation.FsScaleAnimation) animation;
                if (Objects.equals("x", scaleAnimation.type)) {
                    if (go > scaleAnimation.durationStart && go < scaleAnimation.durationEnd) {
                        float bl = Math.min((go - scaleAnimation.durationStart) * 1f / (scaleAnimation.durationEnd - scaleAnimation.durationStart), 1);
                        extra.currentScaleX = (lastScaleX + (scaleAnimation.scale - lastScaleX) * Math.min(bl, 1));
                    } else if (go > scaleAnimation.durationEnd) {
                        extra.currentScaleX = scaleAnimation.scale;
                    }
                    lastScaleX = extra.currentScaleX;
                } else if (Objects.equals("y", scaleAnimation.type)) {
                    if (go > scaleAnimation.durationStart && go < scaleAnimation.durationEnd) {
                        float bl = Math.min((go - scaleAnimation.durationStart) * 1f / (scaleAnimation.durationEnd - scaleAnimation.durationStart), 1);
                        extra.currentScaleY = (lastScaleY + (scaleAnimation.scale - lastScaleY) * Math.min(bl, 1));
                    } else if (go > scaleAnimation.durationEnd) {
                        extra.currentScaleY = scaleAnimation.scale;
                    }
                    lastScaleY = extra.currentScaleY;
                }
            }

            animation = animation.next;
        }
        if (extra.durationK != 0) {
            if (!TextUtils.isEmpty(sub.content)) {
                long durationK = extra.durationK;
                if (durationK < 0) {
                    durationK = sub.end - sub.star;
                }
                CharSequence text = sub.content;
                float scale = Math.min(go * 1.0f / durationK, 1f);
                extra.useSecond = true;
                float index = scale * text.length();
                if (index > 0) {
                    int intIndex = (int) index;
                    SpannableStringBuilder builder = new SpannableStringBuilder();
                    builder.append(text);
                    if (intIndex == index) {
                        ForegroundColorSpan foregroundColorSpan = new ForegroundColorSpan(extra.getFontColor());
                        builder.setSpan(foregroundColorSpan, 0, intIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    } else {
                        PartForegroundColorSpan foregroundColorSpan = new PartForegroundColorSpan(extra.getFontColor(), index - intIndex);
                        builder.setSpan(foregroundColorSpan, 0, intIndex + 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    }
                    sub.setRenderText(SpannedString.valueOf(builder));
                }
            }
        }
        //文字字幕只缩放x轴，字高缩放通过xy轴设置标准
        if (!extra.isPath) {
            float min = Math.min(extra.currentScaleX, extra.currentScaleY);
            extra.currentScaleX = extra.currentScaleX / min;
            extra.currentScaleY = extra.currentScaleY / min;
            extra.currentFontScale = extra.currentFontScale * min;
        }
    }

    public boolean isHasMove() {
        return hasMove;
    }
}