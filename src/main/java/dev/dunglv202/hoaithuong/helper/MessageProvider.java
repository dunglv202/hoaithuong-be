package dev.dunglv202.hoaithuong.helper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@RequiredArgsConstructor
@Slf4j
public class MessageProvider {
    private final MessageSource messageSource;

    public String getLocalizedMessage(String initialMessage) {
        return getLocalizedMessage(initialMessage, LocaleContextHolder.getLocale());
    }

    public String getLocalizedMessage(String initialMessage, Locale locale) {
        Matcher matcher = Pattern.compile("\\{[a-zA-Z0-9._\\-]*}").matcher(initialMessage);

        return matcher.replaceAll(matchResult -> {
            String matchedString = matchResult.group();
            try {
                return messageSource.getMessage(
                    matchedString.substring(1, matchedString.length() - 1),
                    null,
                    locale
                );
            } catch (NoSuchMessageException e) {
                log.warn("No message found for " + matchedString);
                return matchedString;
            }
        });
    }
}
