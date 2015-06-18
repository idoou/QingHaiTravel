/**
 * 
 */

package com.ziyou.selftravel.support;

import android.content.Context;
import android.text.InputFilter;
import android.text.Spanned;

import com.ziyou.selftravel.R;
import com.ziyou.selftravel.util.ToastUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Kuloud
 */
public class EmojiInputFilter implements InputFilter {
    private Context mContext;
    public EmojiInputFilter(Context context) {
        mContext = context;
    }

    private static final Pattern EMOJI = Pattern
            .compile("[\ud83c\udc00-\ud83c\udfff]|[\ud83d\udc00-\ud83d\udfff]|[\u2600-\u27ff]",
                    Pattern.UNICODE_CASE | Pattern.CASE_INSENSITIVE);

    @Override
    public CharSequence filter(CharSequence source, int start, int end,
            Spanned dest,
            int dstart, int dend) {
        Matcher emojiMatcher = EMOJI.matcher(source);
        if (emojiMatcher.find()) {
            ToastUtils.show(mContext, R.string.msg_emoji_not_support);
            return "";
        }
        return null;
    }

}
