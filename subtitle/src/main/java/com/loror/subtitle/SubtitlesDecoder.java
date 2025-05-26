package com.loror.subtitle;

import android.text.Html;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.SpannedString;
import android.text.TextUtils;

import com.loror.subtitle.model.SubtitlesModel;
import com.loror.subtitle.render.Style;
import com.loror.subtitle.util.SubColorUtil;
import com.loror.subtitle.util.TextUtil;
import com.loror.subtitle.render.StyledSpan;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SubtitlesDecoder {
    /**
     * 一秒=1000毫秒
     */
    private final static int oneSecond = 1000;

    private final static int oneMinute = 60 * oneSecond;

    private final static int oneHour = 60 * oneMinute;

    /**
     * 正则表达式，判断是否是时间的格式
     */
    private final static String SRT_TIME_EXPRESS = "\\d\\d:\\d\\d:\\d\\d,\\d\\d\\d --> \\d\\d:\\d\\d:\\d\\d,\\d\\d\\d";
    private final static String ASS_TIME_EXPRESS = "\\d:\\d\\d:\\d\\d\\.\\d\\d,\\d:\\d\\d:\\d\\d\\.\\d\\d";

    //ass [Script Info]标签
    private final static String ASS_HEAD = "[Script Info]";
    private final static String ASS_HEAD_STYLE = "[V4+ Styles]";
    private final static String ASS_HEAD_STYLE_1 = "[V4 Styles]";
    private final static String ASS_HEAD_STYLE_2 = "[V4 Styles+]";
    private final static String ASS_HEAD_EVENTS = "[Events]";
    private final static String ASS_START = "Dialogue: ";
    private final static String ASS_FORMAT = "Format: ";
    private final static String ASS_STYLE = "Style: ";
    private final static String ASS_RES_X = "PlayResX: ";
    private final static String ASS_RES_Y = "PlayResY: ";
    private final static String ASS_DELAY = "Delay: ";//自定义属性，非标
    private final static String ASS_SCALE_BS = "ScaledBorderAndShadow: ";
    private final static String ASS_WRAP = "WrapStyle: ";

    //ass format标签
    private final static String ASS_DIALOGUE_START = "Start";
    private final static String ASS_DIALOGUE_END = "End";
    private final static String ASS_DIALOGUE_LAYER = "Layer";
    private final static String ASS_DIALOGUE_STYLE = "Style";
    private final static String ASS_DIALOGUE_MARGIN_L = "MarginL";
    private final static String ASS_DIALOGUE_MARGIN_R = "MarginR";
    private final static String ASS_DIALOGUE_MARGIN_V = "MarginV";
    private final static String ASS_DIALOGUE_TEXT = "Text";

    public static final int GRAVITY_UNSET = 0;
    public static final int GRAVITY_AN1 = 1;
    public static final int GRAVITY_AN2 = 2;
    public static final int GRAVITY_AN3 = 3;
    public static final int GRAVITY_AN4 = 4;
    public static final int GRAVITY_AN5 = 5;
    public static final int GRAVITY_AN6 = 6;
    public static final int GRAVITY_AN7 = 7;
    public static final int GRAVITY_AN8 = 8;
    public static final int GRAVITY_AN9 = 9;

    private final Map<String, Style> styles = new HashMap<>();
    private final List<String> subFormat = new ArrayList<>();
    private final AssInfo assInfo = new AssInfo();
    private boolean ignorePath = true;//是否忽略绘画标签

    public static class AssInfo {
        public int delay = 0;//整体延时
        public int playResX = 0;//视频宽
        public int playResY = 0;//视频高
        public boolean scaleBS = false;//是否缩放字幕边框
        public Integer wrap = null;//换行方式
        public String format = "";//字幕格式化行

        public void clear() {
            delay = 0;
            playResX = 0;
            playResY = 0;
            scaleBS = false;
            wrap = null;
            format = "";
        }

        public void copy(AssInfo to) {
            to.delay = this.delay;
            to.playResX = this.playResX;
            to.playResY = this.playResY;
            to.scaleBS = this.scaleBS;
            to.wrap = this.wrap;
            to.format = this.format;
        }
    }

    /**
     * 字幕格式化顺序
     */
    public List<String> getSubFormat() {
        return subFormat;
    }

    /**
     * ass基本信息
     */
    public AssInfo getAssInfo() {
        return assInfo;
    }

    /**
     * ass样式信息
     */
    public Map<String, Style> getStyles() {
        return styles;
    }

    /**
     * 是否忽略绘画标签
     */
    public void setIgnorePath(boolean ignorePath) {
        this.ignorePath = ignorePath;
    }

    /**
     * 读取本地文件
     *
     * @param path path
     */
    public List<SubtitlesModel> decode(String path) {
        FileInputStream is;
        File subtitlesFile = new File(path);

        if (!subtitlesFile.exists() || !subtitlesFile.isFile()) {
            System.err.println("SubtitlesDataCoding:open subtitle file fill");
            return null;
        }
        /**
         * 读取文件，转流，方便读取行
         */
        try {
            is = new FileInputStream(subtitlesFile);
            return decode(is, "UTF-8");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<SubtitlesModel> decode(InputStream inputStream, String charset) {
        assInfo.clear();
        styles.clear();
        subFormat.clear();
        BufferedReader in = null;
        try {
            if (charset == null) {
                in = new BufferedReader(new InputStreamReader(inputStream));
            } else {
                in = new BufferedReader(new InputStreamReader(inputStream, charset));
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            try {
                in = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
            } catch (UnsupportedEncodingException e1) {
                e1.printStackTrace();
            }
        }
        if (in == null) {
            return null;
        }
        long start = System.currentTimeMillis();
        List<SubtitlesModel> list = new ArrayList<>();
        int mode = 0;//0 srt 1 ass
        try {
            int block = 0;//0 默认 1 style 2 events
            int node = 1;
            String line = in.readLine();
            if (line == null) {
                return null;
            }
            //ass字幕开始
            if (line.contains(ASS_HEAD)) {
                mode = 1;
                assInfo.playResX = 384;
                assInfo.playResY = 288;
            }
            System.out.println("mode:" + mode + " line:" + line);
            while ((line = in.readLine()) != null) {
                if (mode == 1) {
                    if (block == 0) {
                        if (line.startsWith(ASS_RES_X)) {
                            try {
                                assInfo.playResX = Integer.parseInt(line.replaceFirst(ASS_RES_X, "").trim());
                            } catch (NumberFormatException e) {
                                System.err.println("parse error playResX:" + line);
                            }
                        } else if (line.startsWith(ASS_RES_Y)) {
                            try {
                                assInfo.playResY = Integer.parseInt(line.replaceFirst(ASS_RES_Y, "").trim());
                            } catch (NumberFormatException e) {
                                System.err.println("parse error playResY:" + line);
                            }
                        } else if (line.startsWith(ASS_DELAY)) {
                            try {
                                assInfo.delay = Integer.parseInt(line.replaceFirst(ASS_DELAY, "").trim());
                            } catch (NumberFormatException e) {
                                System.err.println("parse error Delay:" + line);
                            }
                        } else if (line.startsWith(ASS_SCALE_BS)) {
                            try {
                                assInfo.scaleBS = line.replaceFirst(ASS_SCALE_BS, "").trim().equalsIgnoreCase("yes");
                            } catch (NumberFormatException e) {
                                System.err.println("parse error scaledBorderAndShadow:" + line);
                            }
                        } else if (line.startsWith(ASS_WRAP)) {
                            try {
                                assInfo.wrap = Integer.parseInt(line.replaceFirst(ASS_WRAP, "").trim());
                            } catch (NumberFormatException e) {
                                System.err.println("parse error wrap:" + line);
                            }
                        } else if (line.trim().equalsIgnoreCase(ASS_HEAD_STYLE)
                                || line.trim().equalsIgnoreCase(ASS_HEAD_STYLE_1)
                                || line.trim().equalsIgnoreCase(ASS_HEAD_STYLE_2)) {
                            block = 1;
                        }
                    } else if (block == 1) {
                        if (line.startsWith(ASS_FORMAT)) {
                            assInfo.format = line.replaceFirst(ASS_FORMAT, "");
                        } else if (line.startsWith(ASS_STYLE)) {
                            String style = line.replaceFirst(ASS_STYLE, "");
                            try {
                                Map<String, Style> nameStyle = parseStyle(assInfo.format, style);
                                if (nameStyle != null) {
                                    for (Map.Entry<String, Style> item : nameStyle.entrySet()) {
                                        Style s = item.getValue();
                                        if (s != null) {
                                            s.playResX = assInfo.playResX;
                                            s.playResY = assInfo.playResY;
                                        }
                                        styles.put(item.getKey(), item.getValue());
                                    }
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        } else if (line.startsWith(ASS_HEAD_EVENTS)) {
                            block = 2;
                        }
                    } else if (block == 2) {
                        if (line.startsWith(ASS_FORMAT)) {
                            assInfo.format = line.replaceFirst(ASS_FORMAT, "");
                            subFormat.clear();
                            for (String s : assInfo.format.split(",")) {
                                subFormat.add(s.trim());
                            }
                        } else if (line.startsWith(ASS_START)) {
                            if (ignorePath && line.contains("{\\p0}")) {
                                continue;
                            }
                            try {
                                line = line.substring(ASS_START.length());
                                //ass字幕字幕内容默认在第9个逗号之后
                                int index = subFormat.indexOf(ASS_DIALOGUE_TEXT);
                                String[] texts = splitByCode(line, ',', index == -1 ? 9 : index);
                                String[] form;
                                String text;
                                if (texts.length == 2) {
                                    text = texts[1];
                                    form = texts[0].split(",");
                                } else {
                                    continue;
                                }
                                index = subFormat.indexOf(ASS_DIALOGUE_START);
                                if (index == -1) {
                                    continue;
                                }
                                String startTime = form[index];
                                index = subFormat.indexOf(ASS_DIALOGUE_END);
                                if (index == -1) {
                                    continue;
                                }
                                String endTime = form[index];
                                index = subFormat.indexOf(ASS_DIALOGUE_LAYER);
                                String layer = null;
                                if (index != -1) {
                                    layer = form[index];
                                }
                                index = subFormat.indexOf(ASS_DIALOGUE_STYLE);
                                String style = null;
                                if (index != -1) {
                                    style = form[index];
                                }
                                index = subFormat.indexOf(ASS_DIALOGUE_MARGIN_L);
                                String marginL = null;
                                if (index != -1) {
                                    marginL = form[index];
                                }
                                index = subFormat.indexOf(ASS_DIALOGUE_MARGIN_R);
                                String marginR = null;
                                if (index != -1) {
                                    marginR = form[index];
                                }
                                index = subFormat.indexOf(ASS_DIALOGUE_MARGIN_V);
                                String marginV = null;
                                if (index != -1) {
                                    marginV = form[index];
                                }
                                //=======================获取字段结束===========================

                                SubtitlesModel sm = new SubtitlesModel();
                                sm.node = node++;
                                sm.star = getAssTime(startTime) + assInfo.delay;
                                sm.end = getAssTime(endTime) + assInfo.delay;
                                sm.playResX = assInfo.playResX;
                                sm.playResY = assInfo.playResY;
                                sm.content = text;
                                if (sm.content == null) {
                                    sm.content = "";
                                } else {
                                    sm.content = sm.content.toString().trim();
                                }
                                if (layer != null) {
                                    try {
                                        sm.layer = Integer.parseInt(layer);
                                    } catch (Exception e) {
//                                        e.printStackTrace();
                                        System.err.println("parse error layer:" + layer);
                                    }
                                }
                                //继承v4样式
                                if (style != null) {
                                    sm.styleName = style;
                                }
                                checkContent(sm);
                                checkMargin(sm, marginL, marginR, marginV);
//                                extendStyle(sm, getStyleByName(sm.styleName));
//                                extendStyle(sm, Extra.parseExtra(sm));
                                list.add(sm);
                                if (sm.style != null) {
                                    sm.style.scaleBS = assInfo.scaleBS;
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                } else {
                    try {
                        SubtitlesModel sm = new SubtitlesModel();
                        // 发现部分srt不是按照标准格式生成，-->两边空格数不定
                        if (line.contains("-->")) {
                            String regex = "\\d\\d:\\d\\d:\\d\\d,\\d\\d\\d";
                            Pattern p = Pattern.compile(regex);
                            Matcher m = p.matcher(line);
                            String startTime = null;
                            String endTime = null;
                            if (m.find()) {
                                startTime = m.group();
                            }
                            if (m.find()) {
                                endTime = m.group();
                            }
                            if (startTime != null && endTime != null) {
                                sm.node = node++;
                                // 填充开始时间数据
                                sm.star = getSrtTime(startTime);
                                // 填充结束时间数据
                                sm.end = getSrtTime(endTime);
                                // 填充数据
                                sm.content = in.readLine();
                                while (true) {
                                    line = in.readLine();
                                    if (TextUtil.isInvalid(line)) {
                                        break;
                                    }
//                                  if (TextUtil.isNumber(line)) {
//                                      break;
//                                  }
                                    sm.content += "\n" + line;
                                }
                                checkContent(sm);
//                              extendStyle(sm, Extra.parseExtra(sm));
                                list.add(sm);
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                in.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (!list.isEmpty()) {
            Collections.sort(list, (o1, o2) -> {
                return Integer.compare(o1.star, o2.star);
            });
            // 当前字幕的节点位置
            for (int i = 0; i < list.size(); i++) {
                SubtitlesModel model = list.get(i);
                model.sort = i;
                model.type = mode;
                if (model.style != null) {
                    if (assInfo.wrap != null) {
                        model.style.setWrap(assInfo.wrap);
                    }
                    //默认显示在下方
                    if (model.style.gravity == GRAVITY_UNSET && (!model.style.hasPositionX() && !model.style.hasPositionY() && !model.style.hasMoveAnimation())) {
                        model.style.gravity = GRAVITY_AN2;
                    }
                }
            }
            System.err.println("解码完成，耗时：" + (System.currentTimeMillis() - start));
            return list;
        }
        return null;
    }

    /*
     * 根据标签拆分多条字幕
     * */
    private List<String> getLines(String line) {
        String[] lines = line.split("\\\\N");
        List<String> contents = new ArrayList<>();
        String one = "";
        for (String s : lines) {
            //同一行换行后存在不同标签，拆分处理
            if (s.startsWith("{\\")) {
                if (!one.isEmpty()) {
                    contents.add(one);
                    one = "";
                }
            }
            if (!one.isEmpty()) {
                one += "\\n";
            }
            one += s;
        }
        if (!one.isEmpty()) {
            contents.add(one);
        }
        return contents;
    }

    /**
     * Fontname,Fontsize,PrimaryColour,SecondaryColour,,BackColour,Bold,Italic,BorderStyle,Outline,Shadow,Alignment,MarginL,MarginR,MarginV,,Encoding
     * 字段1:Name.风格(Style)的名称.区分大小写.不能包含逗号.
     * 字段2:Fontname.使用的字体名称,区分大小写.
     * 字段3:Fontsize.字体的字号
     * 字段4:PrimaryColour.设置主要颜色,为蓝-绿-红三色的十六进制代码相排列,BBGGRR.为字幕填充颜色
     * 字段5:SecondaryColour.设置次要颜色,为蓝-绿-红三色的十六进制代码相排列,BBGGRR.在卡拉OK效果中由次要颜色变为主要颜色.
     * 字段6:(),设置轮廓颜色,为蓝-绿-红三色的十六进制代码相排列,BBGGRR.
     * 字段7:BackColour,设置阴影颜色,为蓝-绿-红三色的十六进制代码相排列,BBGGRR.
     * 字段8:Bold.-1为粗体,0为常规
     * 字段9:Italic.-1为斜体,0为常规
     * 字段10:BorderStyle.1=边框+阴影,3=纯色背景.当值为3时,文字下方为轮廓颜色的背景,最下方为阴影颜色背景.
     * 字段11:Outline.当BorderStyle为1时,该值定义文字轮廓宽度,为像素数,常见有0,1,2,3,4.
     * 字段12:Shadow.当BorderStyle为1时,该值定义阴影的深度,为像素数,常见有0,1,2,3,4.
     * 字段13:Alignment.定义字幕的位置.字幕在下方时,1=左对齐,2=居中,3=右对齐.1,2,3加上4后字幕出现在屏幕上方.1,2,3加上8后字幕出现在屏幕中间.例:11=屏幕中间右对齐.
     * 字段14:MarginL.字幕可出现区域与左边缘的距离,为像素数
     * 字段15:MarginR.字幕可出现区域与右边缘的距离,为像素数
     * 字段16:MarginV.垂直距离
     * 字段17:AlphaLevel.SSA字幕用来定义透明度
     * 字段18:Encoding.指明字体的字符集或编码方式.如0为英文,134为简体中文,136为繁体中文.当文件为非UNICODE类型编码时,该值对字幕的显示起作用
     */
    private final static String ASS_FORMAT_NAME = "Name";
    private final static String ASS_FORMAT_FONT = "Fontname";
    private final static String ASS_FORMAT_FONT_SIZE = "Fontsize";
    private final static String ASS_FORMAT_COLOR_PRIMARY = "PrimaryColour";
    private final static String ASS_FORMAT_COLOR_SECONDARY = "SecondaryColour";
    private final static String ASS_FORMAT_COLOR_OUT_LINE = "OutlineColour";
    private final static String ASS_FORMAT_COLOR_BACK = "BackColour";
    private final static String ASS_FORMAT_OUT_LINE = "Outline";
    private final static String ASS_FORMAT_SHADOW = "Shadow";
    private final static String ASS_FORMAT_BOLD = "Bold";
    private final static String ASS_FORMAT_ITALIC = "Italic";
    private final static String ASS_FORMAT_UNDER_LINE = "Underline";
    private final static String ASS_FORMAT_STRIKE_LINE = "Strikeout";
    private final static String ASS_FORMAT_SPACING = "Spacing";
    private final static String ASS_FORMAT_ANGLE = "Angle";
    private final static String ASS_FORMAT_ALIGNMENT = "Alignment";
    private final static String ASS_FORMAT_BORDER_STYLE = "BorderStyle";
    private final static String ASS_FORMAT_MARGIN_L = "MarginL";
    private final static String ASS_FORMAT_MARGIN_R = "MarginR";
    private final static String ASS_FORMAT_MARGIN_V = "MarginV";
    private final static String ASS_FORMAT_SCALE_X = "ScaleX";
    private final static String ASS_FORMAT_SCALE_Y = "ScaleY";

    /**
     * ass定义style更换为指令
     */
    private void replaceStyleIfNeed(SubtitlesModel item, String line) {
        //判断是否重置样式
        String style = find(item.extra, "\\r", '}');
        if (style != null) {
            item.styleName = style;
            extendStyle(item, getStyleByName(style));
        }
    }

    private Style getStyleByName(String name) {
        if (name == null) {
            return null;
        }
        Style style = styles.get(name);
        if (style == null && name.startsWith("*")) {
            style = styles.get(name.substring(1));
        }
        return style;
    }

    /**
     * 从value继承样式，只替换新的已定义部分
     */
    private void extendStyle(SubtitlesModel item, Style value) {
        if (value != null) {
            if (item.style == null) {
                item.style = new Style(value);
            } else {
                item.style.extendStyle(value);
            }
            if (item.playResX != 0 && item.playResY != 0) {
                item.style.playResX = item.playResX;
                item.style.playResY = item.playResY;
            }
        }
    }

    /**
     * 解析ass样式
     */
    private static Map<String, Style> parseStyle(String format, String style) {
        if (TextUtils.isEmpty(format) || TextUtils.isEmpty(style)) {
            return null;
        }
        Map<String, Style> styles = new HashMap<>();
        Style value = new Style();
        List<String> formats = new ArrayList<>();
        List<String> values = new ArrayList<>();
        for (String s : format.split(",")) {
            formats.add(s.trim());
        }
        for (String s : style.split(",")) {
            values.add(s.trim());
        }
        int index = formats.indexOf(ASS_FORMAT_NAME);
        if (index == -1) {
            return null;
        }
        String name = values.get(index);
        index = formats.indexOf(ASS_FORMAT_FONT);
        if (index != -1) {
            value.setFont(values.get(index));
        }
        index = formats.indexOf(ASS_FORMAT_FONT_SIZE);
        if (index != -1) {
            value.setFontSize(values.get(index));
        }
        index = formats.indexOf(ASS_FORMAT_COLOR_PRIMARY);
        if (index != -1) {
            String color = values.get(index);
            if (color.startsWith("&H")) {
                value.setFontColor(color.substring(2));
            }
        }
        index = formats.indexOf(ASS_FORMAT_COLOR_SECONDARY);
        if (index != -1) {
            String color = values.get(index);
            if (color.startsWith("&H")) {
                value.setFontSecondaryColor(color.substring(2));
            }
        }
        index = formats.indexOf(ASS_FORMAT_COLOR_OUT_LINE);
        if (index != -1) {
            String color = values.get(index);
            if (color.startsWith("&H")) {
                value.setBorderColor(color.substring(2));
            }
        }
        index = formats.indexOf(ASS_FORMAT_COLOR_BACK);
        if (index != -1) {
            String color = values.get(index);
            if (color.startsWith("&H")) {
                value.setShadowColor(color.substring(2));
            }
        }
        index = formats.indexOf(ASS_FORMAT_OUT_LINE);
        if (index != -1) {
            value.setBorderWidth(values.get(index));
        }
        index = formats.indexOf(ASS_FORMAT_SHADOW);
        if (index != -1) {
            value.setShadowWidth(values.get(index));
        }
        index = formats.indexOf(ASS_FORMAT_BOLD);
        if (index != -1) {
            value.setBold(values.get(index));
        }
        index = formats.indexOf(ASS_FORMAT_ITALIC);
        if (index != -1) {
            value.setItalic(values.get(index));
        }
        index = formats.indexOf(ASS_FORMAT_UNDER_LINE);
        if (index != -1) {
            value.setUnderline(values.get(index));
        }
        index = formats.indexOf(ASS_FORMAT_STRIKE_LINE);
        if (index != -1) {
            value.setStrikeout(values.get(index));
        }
        index = formats.indexOf(ASS_FORMAT_SPACING);
        if (index != -1) {
            value.setSpace(values.get(index));
        }
        index = formats.indexOf(ASS_FORMAT_BORDER_STYLE);
        if (index != -1) {
            value.setBorderStyle(values.get(index));
        }
        index = formats.indexOf(ASS_FORMAT_MARGIN_L);
        if (index != -1) {
            value.setMarginL(values.get(index));
        }
        index = formats.indexOf(ASS_FORMAT_MARGIN_R);
        if (index != -1) {
            value.setMarginR(values.get(index));
        }
        index = formats.indexOf(ASS_FORMAT_MARGIN_V);
        if (index != -1) {
            value.setMarginV(values.get(index));
        }
        index = formats.indexOf(ASS_FORMAT_ANGLE);
        if (index != -1) {
            value.setDegree(values.get(index));
        }
        index = formats.indexOf(ASS_FORMAT_ALIGNMENT);
        if (index != -1) {
            value.setGravity(values.get(index));
        }
        index = formats.indexOf(ASS_FORMAT_SCALE_X);
        if (index != -1) {
            value.setScaleX(values.get(index));
        }
        index = formats.indexOf(ASS_FORMAT_SCALE_Y);
        if (index != -1) {
            value.setScaleY(values.get(index));
        }
        styles.put(name, value);
        return styles;
    }

    /**
     * @param line 00:05:40,560
     * @return 字幕所在的时间节点
     * 将String类型的时间转换成int的时间类型
     */
    private static int getSrtTime(String line) {
        try {
            return Integer.parseInt(line.substring(0, 2)) * oneHour// 时
                    + Integer.parseInt(line.substring(3, 5)) * oneMinute// 分
                    + Integer.parseInt(line.substring(6, 8)) * oneSecond// 秒
                    + Integer.parseInt(line.substring(9));// 毫秒
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        return -1;
    }

    /**
     * @param line 0:02:04.23
     * @return 字幕所在的时间节点
     * 将String类型的时间转换成int的时间类型
     */
    private static int getAssTime(String line) {
        try {
            return Integer.parseInt(line.substring(0, 1)) * oneHour// 时
                    + Integer.parseInt(line.substring(2, 4)) * oneMinute// 分
                    + Integer.parseInt(line.substring(5, 7)) * oneSecond// 秒
                    + (Integer.parseInt(line.substring(8)) * 10);// 毫秒
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        return -1;
    }

    /**
     * 取出字幕特殊信息
     */
    public void checkContent(SubtitlesModel content) {
        if (TextUtils.isEmpty(content.content)) {
            return;
        }
        List<String> contents = splitContent(content.content);
        content.extra = "";
        SpannableStringBuilder result = new SpannableStringBuilder();
        boolean hasSpan = false;
        boolean create = false;
        Style base = null;
        Style top = null;
        //一段文字存在不同字体样式，只保留第一段样式，颜色各自保留
        for (String text : contents) {
            if (text.startsWith("{\\")) {
                int index = text.lastIndexOf("}");
                if (index == -1 && text.charAt(text.length() - 1) == '\\') {
                    index = text.length() - 1;
                }
                if (index != -1) {
                    String extra = text.substring(0, index + 1);
                    String data = filterString(text.substring(index + 1));
                    String color = findColor(data);
                    if (color != null) {
                        data = Html.fromHtml(data.replace("\n", "<br/>")
                                        .replace(" ", "&nbsp;"))
                                .toString();
                    }
                    if (TextUtils.isEmpty(result)) {
                        content.extra += extra;
                        result.append(data);
                    } else {
                        if (!create) {
                            top = getStyleByName(content.styleName);
                            extendStyle(content, top);
                            extendStyle(content, Extra.parseExtra(content));
                            if (content.style != null) {
                                base = content.style;
                            }
                            create = true;
                        }
                        //继承base
                        Style style = base == null ? null : new Style(base);
                        //更新style
                        String styleName = find(extra, "\\r", '}');
                        if (styleName != null) {
                            //单一\r，恢复到顶部样式
                            if (styleName.isEmpty()) {
                                style = top == null ? null : new Style(top);
                            } else {
                                Style replace = getStyleByName(styleName);
                                if (replace != null) {
                                    if (style == null) {
                                        style = new Style(replace);
                                    } else {
                                        style.extendStyle(replace);
                                    }
                                }
                            }
                        }
                        //替换自有属性
                        Style mine = Extra.parseExtra(extra, content.playResX, content.playResY);
                        if (style == null) {
                            style = mine;
                        } else {
                            style.extendStyle(mine);
                        }
                        if (!TextUtils.isEmpty(color)) {
                            if (style == null) {
                                style = new Style();
                            }
                            try {
                                style.setFontColor(SubColorUtil.parseArgb(color));
                            } catch (Exception e) {
                                e.printStackTrace();
                                System.err.println("SubtitlesView parse c failed:" + color);
                            }
                        }
                        if (style == null) {
                            result.append(data);
                        } else {
                            base = style;
                            SpannableStringBuilder builder = new SpannableStringBuilder();
                            builder.append(data);
                            if (builder.length() > 0) {
                                StyledSpan styledSpan = new StyledSpan(style);
                                builder.setSpan(styledSpan, 0, data.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                                hasSpan = true;
                            }
                            result.append(builder);
                        }
                    }
                    continue;
                }
            }
            CharSequence data = filterString(text);
            String color = findColor(data.toString());
            if (color != null) {
                data = Html.fromHtml(data.toString().replace("\n", "<br/>")
                        .replace(" ", "&nbsp;"));
                hasSpan = true;
            }
            result.append(data);
        }
        if (!create) {
            extendStyle(content, getStyleByName(content.styleName));
            extendStyle(content, Extra.parseExtra(content));
        }
        int wrap = content.style == null ? 0 : content.style.getWrap();
        for (int i = 0; i < result.length(); i++) {
            char c = result.charAt(i);
            if (c == '\r') {
                if (wrap == 2) {
                    result.replace(i, i + 1, "\n");
                } else {
                    result.replace(i, i + 1, " ");
                }
            }
        }
        content.content = hasSpan ? SpannedString.valueOf(result) : result.toString();
    }

    /**
     * 行定义了marginV，使用行定义
     */
    public void checkMargin(SubtitlesModel sm, String marginL, String marginR, String marginV) {
        if (sm.style == null) {
            return;
        }
        if (marginV != null) {
            try {
                int m = Integer.parseInt(marginV);
                if (m != 0) {
                    sm.style.setMarginV(marginV);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (marginL != null) {
            try {
                int m = Integer.parseInt(marginL);
                if (m != 0) {
                    sm.style.setMarginL(marginL);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (marginR != null) {
            try {
                int m = Integer.parseInt(marginR);
                if (m != 0) {
                    sm.style.setMarginR(marginR);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 将字幕内容按样式分组
     */
    private static List<String> splitContent(CharSequence content) {
        String texts = content.toString().replace("\\\\N", "\\n");
        List<String> contents = new ArrayList<>();
//        int i = 0;
//        //{\开头时候，第一个为空
//        for (String s : texts.split("\\{\\\\")) {
//            if (!TextUtils.isEmpty(s)) {
//                if (i == 0) {
//                    if (s.startsWith("{\\")) {
//                        contents.add("{\\" + s);
//                    } else {
//                        contents.add(s);
//                    }
//                } else {
//                    contents.add("{\\" + s);
//                }
//            }
//            i++;
//        }
//        if (i == 1) {
//            contents.clear();
//            contents.add(texts);
//        }
        //{=4}{\an1}字幕{\an2}{=5}字幕1{\an3}字幕2 出现非标签{}
        StringBuilder text = new StringBuilder();
        boolean hasEnd = false;
        boolean inMark = false;
        int end = -10;
        if (texts.length() > 2) {
            if (texts.charAt(0) != '{' || texts.charAt(1) != '\\') {
                hasEnd = true;
            }
            for (int i = 0; i < texts.length(); i++) {
                char c = texts.charAt(i);
                if (c == '{') {
                    if (i < texts.length() - 1 && texts.charAt(i + 1) == '\\') {
                        if (hasEnd) {
                            if (end == i - 1 && inMark) {
                                hasEnd = false;
                            } else {
                                contents.add(text.toString());
                                text = new StringBuilder("{");
                                hasEnd = false;
                                inMark = true;
                                continue;
                            }
                        }
                    } else {
                        inMark = false;
                    }
                } else if (c == '}') {
                    hasEnd = true;
                    end = i;
                }
                text.append(c);
            }
            if (text.length() != 0) {
                contents.add(text.toString());
            }
        } else {
            contents.add(texts);
        }
        return contents;
    }

    private static String findColor(String text) {
        String color = null;
        String font = find(text, "<font", '>');
        if (font != null) {
            if (font.contains("color=")) {
                color = find(font, "color=", '>');
                if (color == null) {
                    color = find(font, "color=", ' ');
                }
//                if (color == null) {
//                    color = find(font, "color=\"", '"');
//                }
                if (color != null && color.startsWith("\"")) {
                    color = color.substring(1);
                    if (color.endsWith("\"")) {
                        color = color.substring(0, color.length() - 1);
                    }
                }
            }
            return color;
        }
        return null;
    }

    private static String filterString(String text) {
        if (TextUtils.isEmpty(text)) {
            return "";
        }
        text = text.replace("\\N", "\n")
                .replace("\\n", "\r")
                .replace("\\h", " ");
        if (text.length() == 1 && text.charAt(0) == ' ') {
            return "";
        }
        return text;
    }

    public static class Extra extends Style {

        private Extra() {
        }

        public static Style parseExtra(SubtitlesModel model) {
            if (model == null) {
                return null;
            }
            return parseExtra(model.extra, model.playResX, model.playResY);
        }

        public static Style parseExtra(String extra, int playResX, int playResY) {
            if (!TextUtils.isEmpty(extra)) {
                Style result = new Style();
                result.playResX = playResX;
                result.playResY = playResY;
                List<String> marks = listMark(extra);
                for (String mark : marks) {
                    if (checkColor(result, mark)) {
                        continue;
                    }
                    if (checkAn(result, mark)) {
                        continue;
                    }
                    try {
                        checkOther(result, mark);
                    } catch (Exception e) {
                        System.err.println("error parse mark:" + mark + " in:" + extra);
                        e.printStackTrace();
                    }
                }
                return result;
            }
            return null;
        }

        /**
         * 拆解为单个标签
         * exam:{\an1\fad(1000,2000)\fn微软雅黑\fs96\b1\i1\shad0\bord0\fsp10\move(209,478,170,406,29,6201)}
         * exam:{\1c&H00005E&}·{\fs96\1c&H001735&}
         * exam:{\t(4200,4800,2,\clip(201,214,201,216))}
         */
        private static List<String> listMark(String extra) {
            List<String> marks = new ArrayList<>();
            StringBuilder tag = new StringBuilder();
            boolean tMark = false;
            int tBracket = 0;
            for (int i = 0; i < extra.length(); i++) {
                char c = extra.charAt(i);
                if (c == '{') {
                    continue;
                }
                //开始、结束符号
                if (c == '\\') {
                    //t标签未结束
                    if (tMark) {
                        if (tBracket > 0) {
                            tag.append(c);
                            continue;
                        }
                    }
                    if (tag.length() > 0 && tag.charAt(0) == '\\') {
                        marks.add(tag.toString());
                    }
                    tag = new StringBuilder("\\");
                    if (i < extra.length() - 2 && extra.charAt(i + 1) == 't' && extra.charAt(i + 2) == '(') {
                        tMark = true;
                        tBracket = 0;
                    }
                } else if (c == '}') {
                    if (tag.length() > 0 && tag.charAt(0) == '\\') {
                        marks.add(tag.toString());
                    }
                    tag = new StringBuilder();
                    tMark = false;
                    tBracket = 0;
                } else {
                    if (tMark) {
                        if (c == '(') {
                            tBracket++;
                        } else if (c == ')') {
                            tBracket--;
                        }
                    }
                    tag.append(c);
                }
            }
            if (tag.length() > 0 && tag.charAt(0) == '\\') {
                marks.add(tag.toString());
            }
            return marks;
        }

        /**
         * |\–	字幕靠左	字幕居中	字幕靠右
         * 顶部	{\an7\a5}    {\an8\a6}    {\an9\a7}
         * 中间	{\an4\a9}    {\an5\a10}    {\an6\a11}
         * 底部	{\an1\a1}    {\an2\a2}    {\an3\a3}
         */
        private static boolean checkAn(Style result, String mark) {
            if (mark.startsWith("\\")) {
                mark = mark.substring(1);
            }
            if (mark.isEmpty() || mark.charAt(0) != 'a') {
                return false;
            }
            switch (mark) {
                case "an1":
                case "a1":
                    result.gravity = GRAVITY_AN1;
                    break;
                case "an2":
                case "a2":
                    result.gravity = GRAVITY_AN2;
                    break;
                case "an3":
                case "a3":
                    result.gravity = GRAVITY_AN3;
                    break;
                case "an4":
                case "a9":
                    result.gravity = GRAVITY_AN4;
                    break;
                case "an5":
                case "a10":
                    result.gravity = GRAVITY_AN5;
                    break;
                case "an6":
                case "a11":
                    result.gravity = GRAVITY_AN6;
                    break;
                case "an7":
                case "a5":
                    result.gravity = GRAVITY_AN7;
                    break;
                case "an8":
                case "a6":
                    result.gravity = GRAVITY_AN8;
                    break;
                case "an9":
                case "a7":
                    result.gravity = GRAVITY_AN9;
                    break;
                default:
                    return false;
            }
            return true;
        }

        /**
         * 字体颜色
         */
        private static boolean checkColor(Style result, String mark) {
            //alpha \1aH&FF&
            //color \1cH&FFFFFF&
            String c1 = find(mark, "\\1c&H", '&');
            if (c1 != null) {
                result.setFontColor(c1);
                return true;
            }
            String c2 = find(mark, "\\2c&H", '&');
            if (c2 != null) {
                result.setFontSecondaryColor(c2);
                return true;
            }
            String c3 = find(mark, "\\3c&H", '&');
            if (c3 != null) {
                result.setBorderColor(c3);
                return true;
            }
            String c4 = find(mark, "\\4c&H", '&');
            if (c4 != null) {
                result.setShadowColor(c4);
                return true;
            }
            String c = find(mark, "\\c&H", '&');
            if (c != null) {
                result.setFontColor(c);
                return true;
            }
            String a1 = find(mark, "\\1a&H", '&');
            if (a1 != null && a1.length() == 2) {
                result.setFontAlpha(a1);
                return true;
            }
            String a2 = find(mark, "\\2a&H", '&');
            if (a2 != null && a2.length() == 2) {
                result.setFontSecondaryAlpha(a2);
                return true;
            }
            String a3 = find(mark, "\\3a&H", '&');
            if (a3 != null && a3.length() == 2) {
                result.setBorderAlpha(a3);
                return true;
            }
            String a4 = find(mark, "\\4a&H", '&');
            if (a4 != null && a4.length() == 2) {
                result.setShadowAlpha(a4);
                return true;
            }
            return false;
        }

        /**
         * \n 空格，宽度超出范围后自动换行
         * \N 硬回车------ 较常用
         * \h 硬空格
         * \b<0/1> 粗体
         * 0关闭，1开启
         * \i<0/1> 斜体
         * \\u<0/1> 下划线
         * \s<0/1> 删除线
         * \bord<width> 边框宽度
         * \shad<depth> 阴影距离
         * \fn<font name> 改变字型，例：\N{\fn冬青黑体简体中文 W3\fs16\c&H00FFFFFF}
         * \fs<font size> 改变字号
         * \fsc<x/y><percent> 字符缩放------正常大小为100%
         * \fsp<pixels> 调整字间距
         * \fr[<x/y/z>]<degrees> 旋转
         * \fr = \frz，和[v4+ Styles]中的Angle效果相同-----旋转所围绕的中心由\a或\an决定
         * \fe<charset> 改变编码
         * \c&H<bbggrr> 改变颜色------ 十六进制00-ff，蓝绿红，例：{\c&H00FFFFFF}
         * \c = \1c
         * \1c&H<bbggrr> 改变主体颜色
         * \2c&H<bbggrr> 改变次要颜色
         * \3c&H<bbggrr> 改变边框颜色
         * \4c&H<bbggrr> 改变阴影颜色
         * \alpha&H<aa> 改变透明度
         * \alpha是同时改变字幕所有部分的透明度
         * \1a&H<aa> 改变主体透明度
         * \2a&H<aa> 改变次要透明度
         * \3a&H<aa> 改变边框透明度
         * \4a&H<aa> 改变阴影透明度
         * \r[<style>] \r恢复成最初的字体
         * \r <style>可以改变成其它的字体
         * \a<alignment> 设置对齐方式
         * 采用SSA的定义
         * 左 中 右
         * 上 5 6 7
         * 中 9 10 11
         * 下 1 2 3
         * 一般正文用\a2，特效用\a10
         * <p>
         * 文本旋转
         * \frx<amount>
         * \fry<amount>
         * \frz<amount>
         * \fr<amount>
         * 沿 X，Y，Z 轴旋转文本。\fr标签是\frz的简写。
         * X 轴在屏幕平面上，沿水平方向。沿 X 轴旋转（角度为正数时）会让文本的顶部看起来“陷”到屏幕里面，而底部“凸”出来。
         * Y 轴在屏幕平面上，沿竖直方向。沿 Y 轴旋转（角度为正数时）会让文本的右边看起来“陷”到屏幕里面，而左边“凸”出来。
         * Z 轴垂直于屏幕平面。沿 Z 轴旋转（角度为正数时），文本会在屏幕平面内逆时针旋转（单位以角度计）。
         */
        private static void checkOther(Style result, String mark) {
            if (!mark.startsWith("\\")) {
                mark = "\\" + mark;
            }
            String t = find(mark, "\\t", ')');
            if (t != null && !t.isEmpty()) {
                result.setT(t);
                String clip = find(mark, "\\clip", ')');
                if (clip != null) {
                    result.addClipAnimation(clip);
                }
                String fs = find(mark, "\\fs", ')');
                if (fs != null) {
                    result.addFontSizeAnimation(fs);
                }
                String blur = find(mark, "\\blur", ')');
                if (blur != null) {
                    result.addBlurAnimation(blur);
                }
                String frz = find(mark, "\\frz", ')');
                if (frz != null) {
                    result.addDegreeAnimation("z", frz);
                }
                String frx = find(mark, "\\frx", ')');
                if (frx != null) {
                    result.addDegreeAnimation("x", frx);
                }
                String fry = find(mark, "\\fry", ')');
                if (fry != null) {
                    result.addDegreeAnimation("y", fry);
                }
                return;
            }
            String pos = find(mark, "\\pos", ')');
            if (pos != null && !pos.isEmpty()) {
                result.setPosition(pos);
                return;
            }
            String move = find(mark, "\\move", ')');
            if (move != null && !move.isEmpty()) {
                result.setMove(move);
                return;
            }
            String fade = find(mark, "\\fade", ')');
            if (fade != null && !fade.isEmpty()) {
                result.setFade(fade);
                return;
            }
            String fad = find(mark, "\\fad", ')');
            if (fad != null && !fad.isEmpty()) {
                result.setFad(fad);
                return;
            }
            String fsp = find(mark, "\\fsp", '\\');
            if (fsp != null) {
                result.setSpace(fsp);
                return;
            }
            String fscx = find(mark, "\\fscx", '\\');
            if (fscx != null) {
                result.setScaleX(fscx);
                return;
            }
            String fscy = find(mark, "\\fscy", '\\');
            if (fscy != null) {
                result.setScaleY(fscy);
                return;
            }
            String fs = find(mark, "\\fs", '\\');
            if (fs != null) {
                result.setFontSize(fs);
                return;
            }
            String fn = find(mark, "\\fn", '\\');
            if (fn != null) {
                result.setFont(fn);
                return;
            }
            //\bord
            String bord = find(mark, "\\bord", '\\');
            if (bord != null) {
                result.setBorderWidth(bord);
                return;
            }
            String shad = find(mark, "\\shad", '\\');
            if (shad != null) {
                result.setShadowWidth(shad);
                return;
            }
            String be = find(mark, "\\be", '\\');
            if (be != null) {
                result.setBe(be);
                return;
            }
            String blur = find(mark, "\\blur", '\\');
            if (blur != null) {
                result.setBlur(blur);
                return;
            }
            String b = find(mark, "\\b", '\\');
            if (b != null) {
                result.setBold(b);
                return;
            }
            String i = find(mark, "\\i", '\\');
            if (i != null) {
                result.setItalic(i);
                return;
            }
            String u = find(mark, "\\u", '\\');
            if (u != null) {
                result.setUnderline(u);
                return;
            }
            String frz = find(mark, "\\frz", '\\');
            if (frz != null) {
                result.setDegree(frz);
                return;
            }
            String frx = find(mark, "\\frx", '\\');
            if (frx != null) {
                result.setDegreeX(frx);
                return;
            }
            String fry = find(mark, "\\fry", '\\');
            if (fry != null) {
                result.setDegreeY(fry);
                return;
            }
            String p = find(mark, "\\p", '\\');
            if (p != null) {
                if (TextUtil.isNumber(p)) {
                    result.isPath = true;
                    result.pathScale = Integer.parseInt(p);
                    return;
                }
            }
            String k = find(mark, "\\K", '\\');
            if (k != null) {
                if (TextUtil.isNumber(k)) {
                    result.setK(k);
                    return;
                }
            }
            String kf = find(mark, "\\kf", '\\');
            if (kf != null) {
                result.setK("-1");
            }
            String clip = find(mark, "\\clip", ')');
            if (clip != null) {
                result.setClip(clip);
            }
            String s = find(mark, "\\s", '\\');
            if (s != null) {
                result.setStrikeout(s);
                return;
            }
        }

        /**
         * 绘图命令：
         * m <x> <y> 将鼠标移至坐标(x,y)，同时将现有的图形封闭(即开始画新的图形)，所有绘画都以这个命令开始
         * n <x> <y> 将鼠标移至坐标(x,y)，同时不封闭原有的图形
         * l <x> <y> 从鼠标原来的坐标位置画一条直线到(x,y)，并从这个点继续绘画
         * b <x1> <y1> <x2> <y2> <x3> <y3> 画一条三度贝塞尔曲线至(x3,y3)，以(x1,y1)，(x2,y2)作为控制点
         * s <x1> <y1> <x2> <y2> <x3> <y3> ... <xN> <yN> 从现有坐标画一条“三次均匀B样条”(cubic uniform b-spline)到点(xN,yN)
         * 该命令至少要含有三个坐标点(三个坐标时等同于贝塞尔曲线)
         * 这个命令实质上是把几条贝塞尔曲线连结到一起
         * p <x> <y> 延长B样条(b-spline)到点(x,y)，作用相当于在s命令后多加一个坐标点(x,y)
         * c 结束B样条(b-spline)
         */
        public static List<SubtitlesModel.Path> parsePath(SubtitlesModel model) {
            Style style = model.style;
            if (style == null) {
                return null;
            }
            if (!style.isPath) {
                return null;
            }
            if (model.content == null) {
                return null;
            }
            String vector = model.content.toString();
            if (vector.startsWith("m")) {
                int end = vector.indexOf("{");
                List<SubtitlesModel.Path> paths = new ArrayList<>();
                String[] marks = (end == -1 ? vector : vector.substring(0, end)).split("&nbsp;");
                String cmd = "";
                List<Float> points = new ArrayList<>();
                for (String mark : marks) {
                    if ("m".equals(mark) || "n".equals(mark) ||
                            "l".equals(mark) || "b".equals(mark) || "s".equals(mark) ||
                            "p".equals(mark) || "c".equals(mark)) {
                        if (!TextUtils.isEmpty(cmd)) {
                            addPoint(paths, cmd, points);
                        }
                        cmd = mark;
                        points.clear();
                    } else {
                        try {
                            points.add(Float.parseFloat(mark));
                        } catch (NumberFormatException e) {
                            e.printStackTrace();
                            return null;
                        }
                    }
                }
                if (!points.isEmpty()) {
                    try {
                        addPoint(paths, cmd, points);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                return paths;
            }
            return null;
        }

        private static void addPoint(List<SubtitlesModel.Path> paths, String cmd, List<Float> points) {
            if (points.size() % 2 != 0) {
                return;
            }
            if ("l".equals(cmd) || "p".equals(cmd)) {
                for (int i = 0; i < points.size(); i++) {
                    SubtitlesModel.Point point = new SubtitlesModel.Point();
                    point.x = points.get(i);
                    point.y = points.get(i + 1);
                    i++;
                    SubtitlesModel.Path path = new SubtitlesModel.Path();
                    path.cmd = cmd;
                    path.points = new ArrayList<>();
                    path.points.add(point);
                    paths.add(path);
                }
            } else if ("b".equals(cmd)) {
                List<SubtitlesModel.Point> ps = new ArrayList<>();
                for (int i = 0; i < points.size(); i++) {
                    SubtitlesModel.Point point = new SubtitlesModel.Point();
                    point.x = points.get(i);
                    point.y = points.get(i + 1);
                    ps.add(point);
                    i++;
                    if (ps.size() == 3) {
                        SubtitlesModel.Path path = new SubtitlesModel.Path();
                        path.cmd = cmd;
                        path.points = new ArrayList<>(ps);
                        paths.add(path);
                        ps.clear();
                    }
                }
            } else {
                SubtitlesModel.Path path = new SubtitlesModel.Path();
                path.cmd = cmd;
                path.points = new ArrayList<>();
                for (int i = 0; i < points.size(); i++) {
                    SubtitlesModel.Point point = new SubtitlesModel.Point();
                    point.x = points.get(i);
                    point.y = points.get(i + 1);
                    path.points.add(point);
                    i++;
                }
                paths.add(path);
            }
        }
    }

    /*******************************************字符串工具**********************************************/

    /**
     * 从字符串中查找指令，返回内容部分
     */
    public static String find(String extra, String start, char end) {
        if (TextUtils.isEmpty(extra)) {
            return null;
        }
        int index = extra.indexOf(start);
        if (index != -1) {
            StringBuilder text = new StringBuilder();
            for (int i = index + start.length(); i < extra.length(); i++) {
                char c = extra.charAt(i);
                if (c == end || c == '\\' || c == '}') {
                    break;
                }
                text.append(c);
            }
            return text.toString();
        }
        return null;
    }

    /**
     * 从字符串中查找times个code后内容
     */
    @Deprecated
    public static String find(String text, char code, int times) {
        if (TextUtils.isEmpty(text)) {
            return null;
        }
        String result = "";
        int find = 0;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (find >= times) {
                result += c;
            }
            if (c == code) {
                find++;
            }
        }
        return result;
    }

    /**
     * 从字符串中查找times个code后内容，并分成两部分
     */
    public static String[] splitByCode(String text, char code, int times) {
        if (TextUtils.isEmpty(text)) {
            return new String[]{text};
        }
        StringBuilder other = new StringBuilder();
        StringBuilder result = new StringBuilder();
        int find = 0;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (find >= times) {
                result.append(c);
            } else {
                other.append(c);
            }
            if (c == code) {
                find++;
            }
        }
        return new String[]{other.toString(), result.toString()};
    }

}
