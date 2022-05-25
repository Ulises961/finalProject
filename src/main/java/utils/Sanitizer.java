package utils;

import org.owasp.html.HtmlPolicyBuilder;
import org.owasp.html.PolicyFactory;


public class Sanitizer {

        public static String sanitizeJsInput(String string) {

                return string.replaceAll("(?i)<script.*?>.*?</script.*?>", "") // case 1 - Open and close
                                .replaceAll("(?i)<script.*?/>", "") // case 1 - Open / close
                                .replaceAll("(?i)<script.*?>", "") // case 1 - Open and !close
                                .replaceAll("(?i)<.*?javascript:.*?>.*?</.*?>", "") // case 2 - Open and close
                                .replaceAll("(?i)<.*?javascript:.*?/>", "") // case 2 - Open / close
                                .replaceAll("(?i)<.*?javascript:.*?>", "") // case 2 - Open and !close
                                .replaceAll("(?i)<.*?\\s+on.*?>.*?</.*?>", "") // case 3 - Open and close
                                .replaceAll("(?i)<.*?\\s+on.*?/>", "") // case 3 - Open / close
                                .replaceAll("(?i)<.*?\\s+on.*?>", ""); // case 3 - Open and !close
        }

        public static String getSanitizedHtml(String unsafeHtml) {

                PolicyFactory sanitizer = new HtmlPolicyBuilder()
                                .allowAttributes("name", "required", "class", "placeholder")
                                .onElements("input", "textarea")
                                .allowAttributes("password", "value", "type").onElements("input")
                                .allowAttributes("wrap").onElements("textarea")
                                .allowAttributes("id", "action", "class", "method").onElements("form")
                                .allowElements("textarea", "input", "form", "hr")
                                .allowCommonBlockElements()
                                .allowCommonInlineFormattingElements()
                                .allowStyling()
                                .allowStandardUrlProtocols()
                                .allowStyling()
                                .allowStandardUrlProtocols().allowElements("a")
                                .allowAttributes("href").onElements("a").requireRelNofollowOnLinks()
                                .allowStandardUrlProtocols()
                                .allowElements(
                                                "table", "tr", "td", "th",
                                                "colgroup", "caption", "col",
                                                "thead", "tbody", "tfoot")
                                .allowAttributes("summary").onElements("table")
                                .allowAttributes("align", "valign")
                                .onElements("table", "tr", "td", "th",
                                                "colgroup", "col",
                                                "thead", "tbody", "tfoot")
                                .allowTextIn("table")
                                .toFactory();

                String safeOutput = sanitizer.sanitize(unsafeHtml);
               
                return safeOutput;

        }

}
