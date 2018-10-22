package com.dhy.apiholder;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

public class BaseUrlUtil {
    /**
     * get full url with append
     */
    public static <API> String getBaseUrl(@NonNull Class<API> api, @Nullable String newBaseUrl) {
        if (api.isAnnotationPresent(BaseUrl.class)) {
            BaseUrl baseUrl = api.getAnnotation(BaseUrl.class);
            String url = newBaseUrl != null ? newBaseUrl : baseUrl.value();
            if (!url.endsWith("/")) url += "/";

            String append = baseUrl.append();
            if (!TextUtils.isEmpty(append)) {
                if (append.startsWith("/")) append = append.substring(1);
                url = url + append;
            }
            if (!url.endsWith("/")) url += "/";
            return url;
        } else {
            throw new IllegalArgumentException(String.format("%s: MUST ANNOTATE WITH '%s'", api.getName(), BaseUrl.class.getName()));
        }
    }


}
