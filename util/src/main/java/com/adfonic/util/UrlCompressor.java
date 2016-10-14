package com.adfonic.util;

/**
 * Specialized URL compression algorithm that Wes came up with.
 */
public final class UrlCompressor {

    private static final char[] HTTP_COLON_SLASH_SLASH_CHARS = "http://".toCharArray();

    // hexlower: "0-9a-f" (4 bit encoding, minimum 2 chars)
    private static final String HEXLOWER_VALUES = "0123456789abcdef";

    // hexupper: "0-9A-F" (4 bit encoding, minimum 2 chars)
    private static final String HEXUPPER_VALUES = "0123456789ABCDEF";

    // dork16 : "0-9./=&;-" (4 bit encoding, minimum 2 chars)
    private static final String DORK16_VALUES = "0123456789./=&;-";

    // low32 : "a-z./=&-_" (5 bit encoding, minimum 4 chars)
    private static final String LOW32_VALUES = "abcdefghijklmnopqrstuvwxyz./=&-_";

    // base64 : "a-zA-Z0-9+/" (6 bit encoding, minimum 2 chars)
    private static final String BASE64_VALUES = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789+/";

    // dork64 : "a-zA-Z0-9=&" (6 bit encoding, minimum 2 chars)
    private static final String DORK64_VALUES = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789=&";
    
    private static final ThreadLocal<UrlCompressor> THREAD_LOCAL_INSTANCE = new ThreadLocal<UrlCompressor>() {
        @Override
        public UrlCompressor initialValue() {
            return new UrlCompressor();
        }
    };

    // escape : followed by any 8-bit character as a literal

    private static final int PAD_4 = 0;
    private static final int HTTP_COLON_SLASH_SLASH = 1;
    private static final int EQUALS = 2;
    private static final int AMPERSAND = 3;
    private static final int QUESTION_MARK = 4;
    private static final int DOT = 5;
    private static final int SEMICOLON = 6;
    private static final int DASH = 7;
    private static final int PERCENT_HEX = 8;
    private static final int HEXLOWER = 9;
    private static final int HEXUPPER = 10;
    private static final int DORK16 = 11;
    private static final int LOW32 = 12;
    private static final int BASE64 = 13;
    private static final int DORK64 = 14;
    private static final int ESCAPE = 15;

    private static final double WORST_CASE_INFLATION_FACTOR = 1.6;

    private char[] inputChars;
    private byte[] output;
    private int nibblePos;
    private int inputLen;
    private int len;
    private char c;
    
    // Instances of this class aren't thread safe, so let's just prevent
    // direct use of it...make the constructor private, and enforce use
    // through static methods that use ThreadLocal instances.
    private UrlCompressor() {
    }

    public static byte[] compress(String url) {
        return THREAD_LOCAL_INSTANCE.get().compressThreadUnsafe(url);
    }

    public static String uncompress(byte[] bytes) {
        return THREAD_LOCAL_INSTANCE.get().uncompressThreadUnsafe(bytes);
    }

    // Private since it's not thread-safe...use the static method and it'll
    // use a ThreadLocal instance.
    private byte[] compressThreadUnsafe(String input) {
        inputLen = input.length();
        inputChars = input.toCharArray();

        int outputLen = (int) Math.round(inputLen * WORST_CASE_INFLATION_FACTOR);
        // See if we can reuse the output buffer
        if (output == null || output.length < outputLen) {
            output = new byte[outputLen];
        }

        nibblePos = 0;
        doCompress();

        byte[] retval = new byte[nibblePos / 2 + nibblePos % 2];
        System.arraycopy(output, 0, retval, 0, retval.length);
        return retval;
    }

    private void accumulate(int value) {
        if (nibblePos % 2 == 0) {
            output[nibblePos / 2] = (byte) (value << 4);
        } else {
            output[nibblePos / 2] = (byte) (output[nibblePos / 2] | value);
        }
        ++nibblePos;
        // System.out.print(value + " ");
    }

    private void accumulate(char ch) {
        // Instead of calling accumulate twice, and performing possibly
        // redundant shifting, let's avoid the shift left/shift right
        // redundancy if we can.
        if (nibblePos % 2 == 0) {
            output[nibblePos / 2] = (byte) (ch & 240);
            ++nibblePos;
            output[nibblePos / 2] = (byte) (output[nibblePos / 2] | (ch & 15));
            ++nibblePos;
        } else {
            output[nibblePos / 2] = (byte) (output[nibblePos / 2] | ((ch & 240) >> 4));
            ++nibblePos;
            output[nibblePos / 2] = (byte) ((ch & 15) << 4);
            ++nibblePos;
        }
    }

    // Encodes 4 characters to 20 bits
    private void accumulateLow32(int startPos) {
        int value = LOW32_VALUES.indexOf(inputChars[startPos]) + (LOW32_VALUES.indexOf(inputChars[startPos + 1]) << 5) + (LOW32_VALUES.indexOf(inputChars[startPos + 2]) << 10)
                + (LOW32_VALUES.indexOf(inputChars[startPos + 3]) << 15);

        accumulate(value & 15);
        accumulate((value & (16 + 32 + 64 + 128)) >> 4);
        accumulate((value & (256 + 512 + 1024 + 2048)) >> 8);
        accumulate((value & (4096 + 8192 + 16384 + 32768)) >> 12);
        accumulate((value & (65536 + 131072 + 262144 + 524288)) >> 16);
    }

    // Encodes 2 characters to 12 bits
    private void accumulateBase64(int startPos) {
        int value = BASE64_VALUES.indexOf(inputChars[startPos]) + (BASE64_VALUES.indexOf(inputChars[startPos + 1]) << 6);
        accumulate(value & 15);
        accumulate((value & (16 + 32 + 64 + 128)) >> 4);
        accumulate((value & (256 + 512 + 1024 + 2048)) >> 8);
    }

    // Encodes 2 characters to 12 bits
    private void accumulateDork64(int startPos) {
        int value = DORK64_VALUES.indexOf(inputChars[startPos]) + (DORK64_VALUES.indexOf(inputChars[startPos + 1]) << 6);
        accumulate(value & 15);
        accumulate((value & (16 + 32 + 64 + 128)) >> 4);
        accumulate((value & (256 + 512 + 1024 + 2048)) >> 8);
    }

    private static int unhex(char ch) {
        if (ch >= 'a') {
            return 10 + ch - 'a';
        } else if (ch >= 'A') {
            return 10 + ch - 'A';
        } else {
            return ch - '0';
        }
    }

    private static int maybeUnhex(char ch) {
        int result = -1;
        
        switch (ch) {
        case '0':
            result = 0;
            break;
        case '1':
            result = 1;
            break;
        case '2':
            result = 2;
            break;
        case '3':
            result = 3;
            break;
        case '4':
            result = 4;
            break;
        case '5':
            result = 5;
            break;
        case '6':
            result = 6;
            break;
        case '7':
            result = 7;
            break;
        case '8':
            result = 8;
            break;
        case '9':
            result = 9;
            break;
        case 'A':
        case 'a':
            result = 10;
            break;
        case 'B':
        case 'b':
            result = 11;
            break;
        case 'C':
        case 'c':
            result = 12;
            break;
        case 'D':
        case 'd':
            result = 13;
            break;
        case 'E':
        case 'e':
            result = 14;
            break;
        case 'F':
        case 'f':
            result = 15;
            break;
        default:
            break;
        }
        return result;
    }

    private static int undork16(char ch) {
        return DORK16_VALUES.indexOf(ch);
    }

    private void scanHexLower(int pos, int maxLen) {
        int loopPos = pos;
        for (len = 0; loopPos < inputLen && len < maxLen; ++loopPos) {
            c = inputChars[loopPos];
            if ((c >= '0' && c <= '9') || (c >= 'a' && c <= 'f')) {
                ++len;
            } else {
                return;
            }
        }
    }

    private void scanHexUpper(int pos, int maxLen) {
        int loopPos = pos;
        for (len = 0; loopPos < inputLen && len < maxLen; ++loopPos) {
            c = inputChars[loopPos];
            if ((c >= '0' && c <= '9') || (c >= 'A' && c <= 'F')) {
                ++len;
            } else {
                return;
            }
        }
    }

    private void scanDork16(int pos, int maxLen) {
        int loopPos = pos;
        for (len = 0; loopPos < inputLen && len < maxLen; ++loopPos) {
            c = inputChars[loopPos];
            if (c >= '0' && c <= '9') {
                ++len;
            } else {
                switch (c) {
                case '.':
                case '/':
                case '=':
                case '&':
                case ';':
                case '-':
                    ++len;
                    break;
                default:
                    return;
                }
            }
        }
    }

    private void scanLow32(int pos, int maxLen) {
        int loopPos = pos;
        for (len = 0; loopPos < inputLen && len < maxLen; ++loopPos) {
            c = inputChars[loopPos];
            if (c >= 'a' && c <= 'z') {
                ++len;
            } else {
                switch (c) {
                case '.':
                case '/':
                case '=':
                case '&':
                case '-':
                case '_':
                    ++len;
                    break;
                default:
                    return;
                }
            }
        }
    }

    private void scanBase64(int pos, int maxLen) {
        int loopPos = pos;
        for (len = 0; loopPos < inputLen && len < maxLen; ++loopPos) {
            c = inputChars[loopPos];
            if (isAlphanumeric()) {
                ++len;
            } else {
                switch (c) {
                case '+':
                case '/':
                    ++len;
                    break;
                default:
                    return;
                }
            }
        }
    }

    private void scanDork64(int pos, int maxLen) {
        int loopPos = pos;
        for (len = 0; loopPos < inputLen && len < maxLen; ++loopPos) {
            c = inputChars[loopPos];
            if (isAlphanumeric()) {
                ++len;
            } else {
                switch (c) {
                case '=':
                case '&':
                    ++len;
                    break;
                default:
                    return;
                }
            }
        }
    }

    private boolean isAlphanumeric() {
        boolean bIsLeter = (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z');
        boolean bIsNumber = c >= '0' && c <= '9';
        return bIsLeter || bIsNumber;
    }

    private void doCompress() {
        int pos = 0;

        // Check for http:// at the very beginning of the URL
        if (startsWith(inputChars, HTTP_COLON_SLASH_SLASH_CHARS)) {
            accumulate(HTTP_COLON_SLASH_SLASH);
            pos += HTTP_COLON_SLASH_SLASH_CHARS.length;
        }

        char ch;
        int i, i1, i2;
        while (pos < inputLen) {
            // Try each of the sixteen options in priority order
            // HTTP_COLON_SLASH_SLASH = 7 chars
            ch = inputChars[pos];
            switch (ch) {
            case '=':
                accumulate(EQUALS);
                ++pos;
                break;
            case '&':
                accumulate(AMPERSAND);
                ++pos;
                break;
            case '?':
                accumulate(QUESTION_MARK);
                ++pos;
                break;
            case '.':
                accumulate(DOT);
                ++pos;
                break;
            case ';':
                accumulate(SEMICOLON);
                ++pos;
                break;
            case '-':
                accumulate(DASH);
                ++pos;
                break;
            default:
                if (ch == '%' && (pos + 3 <= inputLen)) {
                    i1 = maybeUnhex(inputChars[pos + 1]);
                    i2 = maybeUnhex(inputChars[pos + 2]);
                    if (i1 != -1 && i2 != -1) {
                        accumulate(PERCENT_HEX);
                        accumulate(i1);
                        accumulate(i2);
                        pos += 3;
                    } else {
                        // Broken URL, just escape the %
                        accumulate(ESCAPE);
                        accumulate(ch);
                        ++pos;
                    }
                } else {
                    scanHexLower(pos, 32);
                    if (len >= 2) {
                        accumulate(HEXLOWER);
                        accumulate(len / 2 - 1);
                        for (i = 0; i < len / 2; i++) {
                            accumulate(unhex(inputChars[pos++]));
                            accumulate(unhex(inputChars[pos++]));
                        }
                    } else {
                        scanHexUpper(pos, 32);
                        if (len >= 2) {
                            accumulate(HEXUPPER);
                            accumulate(len / 2 - 1);
                            for (i = 0; i < len / 2; i++) {
                                accumulate(unhex(inputChars[pos++]));
                                accumulate(unhex(inputChars[pos++]));
                            }
                        } else {
                            scanDork16(pos, 32);
                            if (len >= 2) {
                                accumulate(DORK16);
                                accumulate(len / 2 - 1);
                                for (i = 0; i < len / 2; i++) {
                                    accumulate(undork16(inputChars[pos++]));
                                    accumulate(undork16(inputChars[pos++]));
                                }
                            } else {
                                scanLow32(pos, 64);
                                if (len >= 4) {
                                    accumulate(LOW32);
                                    accumulate(len / 4 - 1);
                                    for (i = 0; i < len / 4; i++) {
                                        accumulateLow32(pos);
                                        pos += 4;
                                    }
                                } else {
                                    scanBase64(pos, 32);
                                    if (len >= 2) {
                                        accumulate(BASE64);
                                        accumulate(len / 2 - 1);
                                        for (i = 0; i < len / 2; i++) {
                                            accumulateBase64(pos);
                                            pos += 2;
                                        }
                                    } else {
                                        scanDork64(pos, 32);
                                        if (len >= 2) {
                                            accumulate(DORK64);
                                            accumulate(len / 2 - 1);
                                            for (i = 0; i < len / 2; i++) {
                                                accumulateDork64(pos);
                                                pos += 2;
                                            }
                                        } else {
                                            accumulate(ESCAPE);
                                            accumulate(ch);
                                            ++pos;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        // Make it an even number of bytes
        if (nibblePos % 2 != 0) {
            accumulate(PAD_4);
        }
    }

    private static int valueAtOffset(byte[] bytes, int offset) {
        if (offset % 2 == 0) {
            return (bytes[offset / 2] & 240) >> 4;
        } else {
            return bytes[offset / 2] & 15;
        }
    }

    // Private since it's not thread-safe...use the static method and it'll
    // use a ThreadLocal instance.
    private String uncompressThreadUnsafe(byte[] bytes) {
        StringBuilder builder = new StringBuilder((int) Math.round(bytes.length * 1.5)); // pre-size
                                                                                         // 50%
                                                                                         // larger
        nibblePos = 0;
        int count = 0;
        while (nibblePos < bytes.length * 2) {
            int value = valueAtOffset(bytes, nibblePos);
            // System.out.println("value = " + value);

            switch (value) {
            case PAD_4:
                ++nibblePos;
                break;
            case HTTP_COLON_SLASH_SLASH:
                builder.append("http://");
                ++nibblePos;
                break;
            case EQUALS:
                builder.append('=');
                ++nibblePos;
                break;
            case AMPERSAND:
                builder.append('&');
                ++nibblePos;
                break;
            case QUESTION_MARK:
                builder.append('?');
                ++nibblePos;
                break;
            case DOT:
                builder.append('.');
                ++nibblePos;
                break;
            case SEMICOLON:
                builder.append(';');
                ++nibblePos;
                break;
            case DASH:
                builder.append('-');
                ++nibblePos;
                break;
            case PERCENT_HEX:
                builder.append('%');
                builder.append(HEXUPPER_VALUES.charAt(valueAtOffset(bytes, nibblePos + 1)));
                builder.append(HEXUPPER_VALUES.charAt(valueAtOffset(bytes, nibblePos + 2)));
                nibblePos += 3;
                break;
            case HEXLOWER:
                count = 1 + valueAtOffset(bytes, nibblePos + 1);
                // System.out.println("[hexlower " + 2 * count + " chars]");
                for (int i = 0; i < count; i++) {
                    builder.append(HEXLOWER_VALUES.charAt(valueAtOffset(bytes, nibblePos + 2 + i * 2)));
                    builder.append(HEXLOWER_VALUES.charAt(valueAtOffset(bytes, nibblePos + 2 + i * 2 + 1)));
                }
                nibblePos += 2 + 2 * count;
                break;
            case HEXUPPER:
                count = 1 + valueAtOffset(bytes, nibblePos + 1);
                // System.out.println("[hexupper " + 2 * count + " chars]");
                for (int i = 0; i < count; i++) {
                    builder.append(HEXUPPER_VALUES.charAt(valueAtOffset(bytes, nibblePos + 2 + i * 2)));
                    builder.append(HEXUPPER_VALUES.charAt(valueAtOffset(bytes, nibblePos + 2 + i * 2 + 1)));
                }
                nibblePos += 2 + 2 * count;
                break;
            case DORK16:
                count = 1 + valueAtOffset(bytes, nibblePos + 1);
                for (int i = 0; i < count; i++) {
                    builder.append(DORK16_VALUES.charAt(valueAtOffset(bytes, nibblePos + 2 + i * 2)));
                    builder.append(DORK16_VALUES.charAt(valueAtOffset(bytes, nibblePos + 2 + i * 2 + 1)));
                }
                // System.out.println("[dork16 " + 2 * count + " chars]");
                nibblePos += 2 + 2 * count;
                break;
            case LOW32: // (20 bits = 4 chars)
                count = 1 + valueAtOffset(bytes, nibblePos + 1);
                // System.out.println("[low32 " + 4 * count + " chars]");
                for (int i = 0; i < count; i++) {
                    decodeLow32(builder, bytes, nibblePos + 2 + 5 * i);
                }
                nibblePos += 2 + 5 * count;
                break;
            case BASE64:
                count = 1 + valueAtOffset(bytes, nibblePos + 1);
                // System.out.println("[base64 " + 2 * count + " chars]");
                for (int i = 0; i < count; i++) {
                    decodeBase64(builder, bytes, nibblePos + 2 + 3 * i);
                }
                nibblePos += 2 + 3 * count;
                break;
            case DORK64:
                count = 1 + valueAtOffset(bytes, nibblePos + 1);
                for (int i = 0; i < count; i++) {
                    decodeDork64(builder, bytes, nibblePos + 2 + 3 * i);
                }
                // System.out.println("[dork64 " + 2 * count + " chars]");
                nibblePos += 2 + 3 * count;
                break;
            case ESCAPE:
                builder.append((char) (valueAtOffset(bytes, nibblePos + 1) * 16 + valueAtOffset(bytes, nibblePos + 2)));
                nibblePos += 3;
                break;
            default:
                throw new IllegalArgumentException("Unexpected value (" + value + ") at position " + nibblePos);
            }
        }
        return builder.toString();
    }

    private static void decodeLow32(StringBuilder builder, byte[] bytes, int offset) {
        int value = valueAtOffset(bytes, offset) + (valueAtOffset(bytes, offset + 1) << 4) + (valueAtOffset(bytes, offset + 2) << 8) + (valueAtOffset(bytes, offset + 3) << 12)
                + (valueAtOffset(bytes, offset + 4) << 16);

        builder.append(LOW32_VALUES.charAt(value & 0x0001F));
        builder.append(LOW32_VALUES.charAt((value & 0x003E0) >> 5));
        builder.append(LOW32_VALUES.charAt((value & 0x07C00) >> 10));
        builder.append(LOW32_VALUES.charAt((value & 0xF8000) >> 15));
    }

    private static void decodeBase64(StringBuilder builder, byte[] bytes, int offset) {
        int value = valueAtOffset(bytes, offset) + (valueAtOffset(bytes, offset + 1) << 4) + (valueAtOffset(bytes, offset + 2) << 8);

        builder.append(BASE64_VALUES.charAt(value & 0x3F));
        builder.append(BASE64_VALUES.charAt((value & 0xFC0) >> 6));
    }

    private static void decodeDork64(StringBuilder builder, byte[] bytes, int offset) {
        int value = valueAtOffset(bytes, offset) + (valueAtOffset(bytes, offset + 1) << 4) + (valueAtOffset(bytes, offset + 2) << 8);

        builder.append(DORK64_VALUES.charAt(value & 0x3F));
        builder.append(DORK64_VALUES.charAt((value & 0xFC0) >> 6));
    }

    private static boolean startsWith(char[] ca1, char[] ca2) {
        if (ca1.length < ca2.length) {
            return false;
        }
        for (int p = 0; p < ca2.length; ++p) {
            if (ca1[p] != ca2[p]) {
                return false;
            }
        }
        return true;
    }

}
//
// Low32 next value x 20 = number of bits, so 1 = 4 characters, 2 = 8, etc.
// Base64 next value x 12 = number of bits, so 1 = 2 characters
//
// Tokenization of:
// http://click8.aditic.net/index_external.php?vid=4e2b1cb02eb09321200613&pid=154d70f6182bdda&idte=1&rr=aHR0cDovL2Mudy5ta2hvai5jb20vYy5hc20vMy90L2JocC9wbzkvMS8xYS9jMy91LzAvMC8wL3gvNTg2ODEwODItMDEzMS0xMDAwLWYzOTgtMDAwNDEwMWEwMDAzLzEvODgyZDAxMTA=&rc=MC4wNQ==
// HTTP_COLON_SLASH_SLASH (4)
// LOW32 1x4 "clic" (8 + 20)
// BASE64 1x2 "k8" (8 + 8)
// LOW32 7x4 ".adi" "tic." "net/" "inde" "x_ex" "tern" "al.p" (8 + 140)
// ESCAPE 2x1 "hp" (8 + 16)
// QUESTION_MARK (4)
// LOW32 1x4 "vid=" (8 + 20) 252
// HEXLOW 11x2 "4e" "2b" "1c" "b0" "2e" "b0" "93" "21" "20" "06" "13" (8 + 88)
// AMPERSAND (4)
// LOW32 1x4 "pid=" (8 + 20) 300
// HEXLOW 7x2 "15" "4d" "70" "f6" "18" "2b" "dd" (8 + 56)
// LOW32 1x4 "a&id" (8 + 20) 392
// DORK64 16x2 "te=1&rr=aHR0cDovL2Mudy5ta2hvai5j" (8 + 192)
// DORK64 16x2 "b20vYy5hc20vMy90L2JocC9wbzkvMS8x" (8 + 192)
// DORK64 16x2 "YS9jMy91LzAvMC8wL3gvNTg2ODEwODIt" (8 + 192)
// DORK64 16x2 "MDEzMS0xMDAwLWYzOTgtMDAwNDEwMWEw" (8 + 192)
// DORK64 16x2 "MDAzLzEvODgyZDAxMTA=&rc=MC4wNQ==" (8 + 192) 1392

// = 174 bytes, original was 253

// Tokenization of:
// http://rrmprod.amobee.com/upsteed/actionpage?as=95&t=1311416446242&h=2221907&pl=1&u=a7b81cb6-20cd-40a8-a5f8-1895af36cbc7&isu=false&i=166.132.164.157&monitor=1&a=1999715
//
// HTTP_COLON_SLASH_SLASH (4 bits)
// LOW32 + 10x4_CHARS (8 bits)
// "rrmprod.amobee.com/upsteed/actionpage/as" (40 chars x 5 = 200 bits)
// EQUALS (4 bits)
// HEXLOWER + 2_CHARS (8 bits)
// "95" (2 chars x 4 = 8 bits)
// DORK64 + 16x2_CHARS (8 bits) 240
// "&t=1311416446242&h=2221907&pl=1&" (32 chars x 6 = 192 bits)
// DORK64 + 5x2_CHARS (8 bits)
// "u=a7b81cb6" (10 chars x 6 = 60 bits)
// DASH (4 bits) 504
// HEXLOWER + 4_CHARS (8 bits)
// "20cd" (4 chars x 4 bits = 16 bits)
// DASH (4 bits)
// HEXLOWER + 4_CHARS (8 bits)
// "40a8" (4 chars x 4 bits = 16 bits)
// DASH (4 bits)
// HEXLOWER + 12_CHARS (8 bits) 568
// "1895af36cbc7" (12 chars x 4 bits = 48 bits)
// LOW32 + 3x4_CHARS (8 bits)
// &isu=false&i (12 chars x 5 bits = 60 bits)
// DORK16 + 16_CHARS (8 bits) 692
// "166.132.164.157&" (16 chars x 4 bits = 64 bits)
// DORK64 + 9x2_CHARS (8 bits)
// "monitor=1&a=199971" (18 chars x 6 bits = 128 bits) 892
// ESCAPE (4 bits)
// "5" (8 bits)

// Total = 904 bits = 113 bytes (reduced from 168)

// http://www.google.com
// HTTP_COLON_SLASH_SLASH (4)
// LOW32 3x4 "www." "goog" "le.c" (8 + 60)
// BASE64 1x2 "om" (8 + 12)
// PAD_4 (4)
// Total = 96, 12 bytes instead of 21
