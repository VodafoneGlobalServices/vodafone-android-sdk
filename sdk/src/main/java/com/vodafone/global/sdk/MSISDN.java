package com.vodafone.global.sdk;

import com.google.common.base.Optional;

import java.util.*;

public class MSISDN {

    private final String msisdn;
    private final Optional<String> marketCode;

    public MSISDN(String msisdn, Map<String, String> availableMarkets, String phoneNumberRegex) {
        this.msisdn = msisdn;
        marketCode = getMarketCode(availableMarkets, phoneNumberRegex);
    }

    private Optional<String> getMarketCode(Map<String, String> availableMarkets, String phoneNumberRegex) {
        String shortMsisdn = skipUnwantedChars();
        List<String> marketNumbers = new ArrayList<String>(availableMarkets.keySet());
        sort(marketNumbers);
        for (String marketNumber : marketNumbers) {
            if (shortMsisdn.startsWith(marketNumber)) {
                String phoneNumber = shortMsisdn.substring(marketNumber.length());
                if (phoneNumber.matches(phoneNumberRegex)) {
                    return Optional.of(availableMarkets.get(marketNumber));
                }
            }
        }
        return Optional.absent();
    }

    /**
     * Remove leading '0' and '+'.
     */
    private String skipUnwantedChars() {
        char[] chars = msisdn.toCharArray();
        int start = 0;
        for (char aChar : chars) {
            if (aChar == '0' || aChar == '+') {
                start++;
            } else {
                break;
            }
        }
        return msisdn.substring(start);
    }

    /**
     * Sort longest market number first.
     */
    private void sort(List<String> marketNumbers) {
        Collections.sort(marketNumbers, new Comparator<String>() {
            @Override
            public int compare(String lhs, String rhs) {
                int lLength = lhs.length();
                int rLength = rhs.length();
                return lLength > rLength ? -1 : (lLength == rLength ? 0 : 1);
            }
        });
    }

    public boolean isPresent() {
        return msisdn != null && !msisdn.isEmpty();
    }

    public boolean isValid() {
        return isPresent() && marketCode.isPresent();
    }

    public String marketCode() {
        return marketCode.get();
    }

    public String get() {
        return msisdn;
    }
}
